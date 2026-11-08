package com.improvedoverheads;

import com.google.common.collect.ImmutableMap;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.LinkedHashMap;
import net.runelite.api.Skill;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.HeadIcon;
import net.runelite.api.NPC;
import net.runelite.api.Perspective;
import net.runelite.api.Player;
import net.runelite.api.Prayer;
import net.runelite.api.Point;
import net.runelite.api.Projection;
import net.runelite.api.WorldView;
import net.runelite.api.coords.LocalPoint;
import net.runelite.api.gameval.SpriteID;

final class OverheadsHeadIconRenderer
{
    @Inject private OverheadsPartyState party;
    private int prayerLevel = -1;
    private OverheadsActorRenderers<OverheadsHeadIconRenderer> scoped;
    @javax.inject.Inject
    void configureProfiles(OverheadsProfiles profiles)
    {
        scoped = new OverheadsActorRenderers<>(this, profiles, selected -> {
            OverheadsHeadIconRenderer child = new OverheadsHeadIconRenderer(client, selected, sprites);
            child.party = party;
            return child;
        });
    }

	private static final int NPC_HINT_SPRITE_INDEX = 0;
	private static final int PLAYER_HINT_SPRITE_INDEX = 1;
	private static final float MIN_PROJECTION_DEPTH = 50.0f;
	private static final double REFERENCE_MAGNIFICATION = 0.375d;
	private static final int MIN_RENDER_SCALE_PERCENT = 10;
	private static final int MAX_RENDER_SCALE_PERCENT = 400;
	private static final int[] RESOURCE_PACK_PRAYER_SPRITE_IDS =
	{
		SpriteID.Prayeron.PROTECT_FROM_MELEE,
		SpriteID.Prayeron.PROTECT_FROM_MISSILES,
		SpriteID.Prayeron.PROTECT_FROM_MAGIC,
		SpriteID.Prayeron.RETRIBUTION,
		SpriteID.Prayeron.SMITE,
		SpriteID.Prayeron.REDEMPTION
	};

	private final Client client;
	private final OverheadsConfig config;
	private final OverheadsActorUiSpriteCache sprites;
	private final List<OverheadsSprite> iconBuffer = new ArrayList<>(4);
	private final List<OverheadsSprite> prayerBuffer = new ArrayList<>(4);
	private final float[] projectionBuffer = new float[3];
	private Actor preparedActor;
	private final Map<Integer, OverheadsSprite> styledSources = new HashMap<>();
	private final Map<Integer, OverheadsSprite> styledIcons = new LinkedHashMap<Integer, OverheadsSprite>(128, .75f, true) {
        @Override protected boolean removeEldestEntry(Map.Entry<Integer, OverheadsSprite> entry) { return size() > 128; }
    };

	@Inject
	OverheadsHeadIconRenderer(
		Client client,
		OverheadsConfig config,
		OverheadsActorUiSpriteCache sprites)
	{
		this.client = client;
		this.config = config;
		this.sprites = sprites;
	}

	boolean prepare(Actor actor)
	{
        OverheadsHeadIconRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.prepare(actor); }

		clearPrepared();
		if (!config.headIconsEnabled() || actor == null)
		{
			return false;
		}

		prayerLevel = prayerLevel(actor);
		if (actor instanceof Player)
		{
			addPlayerIcons(iconBuffer, (Player) actor);
		}
		else if (actor instanceof NPC)
		{
			addNpcIcons(iconBuffer, (NPC) actor);
		}

		addHintArrow(iconBuffer, actor);
		if (iconBuffer.isEmpty() && prayerBuffer.isEmpty())
		{
			return false;
		}

		preparedActor = actor;
		return true;
	}

	int renderPrepared(Graphics2D graphics, Actor actor, int projectionHeight, Point anchor,
		Point iconAnchor, int stackAnchorY, int occupiedTopY)
	{
		return renderPrepared(graphics, actor, projectionHeight, anchor, iconAnchor, stackAnchorY, occupiedTopY, null);
	}

	int renderPrepared(Graphics2D graphics, Actor actor, int projectionHeight, Point anchor,
		Point iconAnchor, int stackAnchorY, int occupiedTopY, OverheadsCollisionLayout layout)
	{
        OverheadsHeadIconRenderer selected = scoped == null ? this : scoped.forActor(actor);
        if (selected != this) { return selected.renderPrepared(graphics, actor, projectionHeight, anchor, iconAnchor, stackAnchorY, occupiedTopY, layout); }

		if (actor != preparedActor || anchor == null || iconAnchor == null)
		{
			clearPrepared();
			return occupiedTopY;
		}
		try
		{
			int top = renderGroup(graphics, actor, projectionHeight, anchor, iconAnchor, stackAnchorY,
				layout == null ? occupiedTopY : Integer.MAX_VALUE, iconBuffer, config.iconsPosition(), config.headIconXOffset(), config.headIconYOffset(), layout, "icons");
			return renderGroup(graphics, actor, projectionHeight, anchor, anchor, stackAnchorY,
				layout == null ? top : Integer.MAX_VALUE, prayerBuffer, config.prayerIconPosition(), config.prayerIconXOffset(), config.prayerIconYOffset(), layout, "prayerIcon");
		}
		finally
		{
			clearPrepared();
		}
	}

	private int renderGroup(Graphics2D graphics, Actor actor, int projectionHeight, Point anchor,
		Point iconAnchor, int stackAnchorY, int occupiedTopY, List<OverheadsSprite> iconBuffer,
		OverheadsPosition position, int xOffset, int yOffset, OverheadsCollisionLayout layout, String key)
	{
		if (iconBuffer.isEmpty())
		{
			return occupiedTopY;
		}

		boolean scaleWithZoom = config.headIconScaleWithZoom();
		int percent = effectiveScalePercent(
			config.headIconScalePercent(),
			scaleWithZoom,
			client.getScale(),
			scaleWithZoom ? projectionDepth(actor, projectionHeight) : Float.NaN);
		int spacing = Math.max(0, Math.min(20, config.headIconSpacing()));
		int cursorBottomY = stackBottomY(
			stackAnchorY,
			occupiedTopY,
			yOffset,
			spacing) + iconAnchor.getY() - anchor.getY();
		if (position != OverheadsPosition.ABOVE)
		{
			int width = 1;
			int height = 0;
			for (OverheadsSprite icon : iconBuffer)
			{
				width = Math.max(width, scaled(icon.getMaxWidth(), percent));
				height += scaled(icon.getMaxHeight(), percent) + spacing;
			}
			java.awt.Rectangle bounds = OverheadsPlacement.bounds(actor, iconAnchor, position,
				width, Math.max(1, height - spacing), 0, 0);
			if (bounds == null)
			{
				return occupiedTopY;
			}
			iconAnchor = OverheadsPlacement.bottomCenter(bounds);
			cursorBottomY = bounds.y + bounds.height - yOffset;
		}

		int occupiedTop = occupiedTopY;
		OverheadsSpriteScalingMode scalingMode = config.spriteScalingMode();
		List<IconDraw> draws = new ArrayList<>();
		java.awt.Rectangle painted = null;
		for (OverheadsSprite icon : iconBuffer)
		{
			int canvasWidth = scaled(icon.getMaxWidth(), percent);
			int canvasHeight = scaled(icon.getMaxHeight(), percent);
			int imageWidth = scaledSpan(icon.getOffsetX(), icon.getWidth(), percent);
			int imageHeight = scaledSpan(icon.getOffsetY(), icon.getHeight(), percent);
			int canvasX = iconLeft(iconAnchor.getX(), canvasWidth, xOffset);
			int canvasY = cursorBottomY - canvasHeight;
			java.awt.Rectangle imageBounds = new java.awt.Rectangle(canvasX + scaledEdge(icon.getOffsetX(), percent),
				canvasY + scaledEdge(icon.getOffsetY(), percent), imageWidth, imageHeight);
			draws.add(new IconDraw(icon.getImageForScaling(scalingMode, imageWidth, imageHeight), imageBounds));
			painted = painted == null ? imageBounds : painted.union(imageBounds);
			if (position == OverheadsPosition.ABOVE)
			{
				occupiedTop = Math.min(occupiedTop, canvasY);
			}
			cursorBottomY = nextIconBottomY(canvasY, spacing);
		}
		OverheadsCollisionLayout.draw(layout, graphics, key, position, painted, target ->
		{
			Graphics2D g = (Graphics2D) target.create();
			try
			{
				g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
					(scalingMode == null ? OverheadsSpriteScalingMode.NEAREST : scalingMode).getInterpolationHint());
				for (IconDraw draw : draws)
				{
					g.drawImage(draw.image, draw.bounds.x, draw.bounds.y, draw.bounds.width, draw.bounds.height, null);
				}
			}
			finally
			{
				g.dispose();
			}
		});
		return occupiedTop;
	}

	private static final class IconDraw
	{
		private final BufferedImage image;
		private final java.awt.Rectangle bounds;

		private IconDraw(BufferedImage image, java.awt.Rectangle bounds)
		{
			this.image = image;
			this.bounds = bounds;
		}
	}

    private static final Map<Prayer, Integer> BOOST_PRAYERS = new ImmutableMap.Builder<Prayer, Integer>()
        .put(Prayer.BURST_OF_STRENGTH, SpriteID.Prayeron.BURST_OF_STRENGTH)
        .put(Prayer.CLARITY_OF_THOUGHT, SpriteID.Prayeron.CLARITY_OF_THOUGHT)
        .put(Prayer.SHARP_EYE, SpriteID.Prayeron.SHARP_EYE)
        .put(Prayer.MYSTIC_WILL, SpriteID.Prayeron.MYSTIC_WILL)
        .put(Prayer.SUPERHUMAN_STRENGTH, SpriteID.Prayeron.SUPERHUMAN_STRENGTH)
        .put(Prayer.IMPROVED_REFLEXES, SpriteID.Prayeron.IMPROVED_REFLEXES)
        .put(Prayer.HAWK_EYE, SpriteID.Prayeron.HAWK_EYE)
        .put(Prayer.MYSTIC_LORE, SpriteID.Prayeron.MYSTIC_LORE)
        .put(Prayer.ULTIMATE_STRENGTH, SpriteID.Prayeron.ULTIMATE_STRENGTH)
        .put(Prayer.INCREDIBLE_REFLEXES, SpriteID.Prayeron.INCREDIBLE_REFLEXES)
        .put(Prayer.EAGLE_EYE, SpriteID.Prayeron.EAGLE_EYE)
        .put(Prayer.MYSTIC_MIGHT, SpriteID.Prayeron.MYSTIC_MIGHT)
        .put(Prayer.CHIVALRY, SpriteID.Prayeron.CHIVALRY)
        .put(Prayer.PIETY, SpriteID.Prayeron.PIETY)
        .put(Prayer.ZEAL, SpriteID.Prayeron.ZEAL)
        .put(Prayer.RIGOUR, SpriteID.Prayeron.RIGOUR)
        .put(Prayer.AUGURY, SpriteID.Prayeron.AUGURY)
        .put(Prayer.DEADEYE, SpriteID.Prayeron.DEADEYE)
        .put(Prayer.MYSTIC_VIGOUR, SpriteID.Prayeron.MYSTIC_VIGOUR)
        .put(Prayer.RP_ANCIENT_STRENGTH, SpriteID.IconPrayerZaros01_30x30.ANCIENT_STRENGTH)
        .put(Prayer.RP_ANCIENT_SIGHT, SpriteID.IconPrayerZaros01_30x30.ANCIENT_SIGHT)
        .put(Prayer.RP_ANCIENT_WILL, SpriteID.IconPrayerZaros01_30x30.ANCIENT_WILL)
        .put(Prayer.RP_TRINITAS, SpriteID.IconPrayerZaros01_30x30.TRINITAS)
        .put(Prayer.RP_DECIMATE, SpriteID.IconPrayerZaros01_30x30.DECIMATE)
        .put(Prayer.RP_ANNIHILATE, SpriteID.IconPrayerZaros01_30x30.ANNIHILATE)
        .put(Prayer.RP_VAPORISE, SpriteID.IconPrayerZaros01_30x30.VAPORISE)
        .put(Prayer.RP_FUMUS_VOW, SpriteID.IconPrayerZaros01_30x30.FUMUS_VOW)
        .put(Prayer.RP_UMBRA_VOW, SpriteID.IconPrayerZaros01_30x30.UMBRAS_VOW)
        .put(Prayer.RP_CRUORS_VOW, SpriteID.IconPrayerZaros01_30x30.CRUORS_VOW)
        .put(Prayer.RP_GLACIES_VOW, SpriteID.IconPrayerZaros01_30x30.GLACIES_VOW)
        .put(Prayer.RP_INTENSIFY, SpriteID.IconPrayerZaros01_30x30.INTENSIFY)
        .build();

	private void addPlayerIcons(List<OverheadsSprite> icons, Player player)
	{
        addBoostPrayers(icons, player);
		if (config.showSkullIcons())
		{
			add(icons, sprites.get(SpriteID.HEADICONS_PK, player.getSkullIcon()));
		}

		if (config.showPrayerIcons())
		{
			HeadIcon prayer = player.getOverheadIcon();
			if (prayer != null)
			{
				add(config.separatePrayerPosition() ? prayerBuffer : icons, getPrayerIcon(prayer.ordinal()));
			}
		}
	}

    private void addBoostPrayers(List<OverheadsSprite> icons, Player player)
    {
        if (player != client.getLocalPlayer() || !config.localBoostPrayerIcons()) { return; }
        for (Map.Entry<Prayer, Integer> entry : BOOST_PRAYERS.entrySet())
        {
            if (client.isPrayerActive(entry.getKey()))
            {
                OverheadsSprite source = sprites.getResourcePackOverride(entry.getValue());
                if (source == null) { source = sprites.get(entry.getValue(), 0); }
                add(config.separatePrayerPosition() ? prayerBuffer : icons, stylePrayerIcon(-entry.getKey().ordinal() - 1, source));
            }
        }
    }

	private void addNpcIcons(List<OverheadsSprite> icons, NPC npc)
	{
		if (!config.showNpcIcons())
		{
			return;
		}

		int[] archiveIds = npc.getOverheadArchiveIds();
		short[] spriteIds = npc.getOverheadSpriteIds();
		if (archiveIds == null || spriteIds == null)
		{
			return;
		}

		int count = Math.min(archiveIds.length, spriteIds.length);
		for (int i = 0; i < count; i++)
		{
			int spriteId = spriteIds[i];
			if (archiveIds[i] >= 0 && spriteId >= 0)
			{
				add(config.separatePrayerPosition() && archiveIds[i] == SpriteID.HEADICONS_PRAYER ? prayerBuffer : icons, archiveIds[i] == SpriteID.HEADICONS_PRAYER
					? getPrayerIcon(spriteId)
					: sprites.get(archiveIds[i], spriteId));
			}
		}
	}

	private OverheadsSprite getPrayerIcon(int prayerIndex)
	{
		OverheadsSprite source = sprites.getPrayerIcon(
			prayerIndex,
			resourcePackPrayerSpriteId(prayerIndex),
			config.showPrayerIconBackground() && config.prayerIconShape() == OverheadsIconShape.ORIGINAL);
        return stylePrayerIcon(prayerIndex, source);
    }

    private OverheadsSprite stylePrayerIcon(int prayerIndex, OverheadsSprite source)
    {
        if (source == null) { return null; }
        if (styledSources.get(prayerIndex) != source)
        {
            styledIcons.keySet().removeIf(key -> (key >> 8) == prayerIndex);
            styledSources.put(prayerIndex, source);
        }
        int key = (prayerIndex << 8) | (prayerLevel + 1);
        OverheadsSprite cached = styledIcons.get(key);
        if (cached != null) return cached;
		OverheadsSprite styled = OverheadsPrayerIconStyle.create(source,
			config.showPrayerIconBackground() ? config.prayerIconShape() : OverheadsIconShape.ORIGINAL,
			config.prayerIconBackground(), config.prayerIconBorder(), config.prayerIconScalePercent(),
            prayerLevel < 0 ? 1 : prayerLevel / 100.0, config.prayerIconEmptyBackground());
		styledSources.put(prayerIndex, source);
		styledIcons.put(key, styled);
		return styled;
	}

    int prayerLevel(Actor actor)
    {
        if (!config.prayerIconBackgroundTracksPoints() || !config.showPrayerIconBackground()
            || config.prayerIconShape() == OverheadsIconShape.ORIGINAL) return -1;
        int current, maximum;
        if (actor == client.getLocalPlayer())
        {
            current = client.getBoostedSkillLevel(Skill.PRAYER);
            maximum = client.getRealSkillLevel(Skill.PRAYER);
        }
        else if (actor instanceof Player && party != null)
        {
            OverheadsPartyState.Value value = party.value((Player) actor, 1);
            if (value == null) return -1;
            current = value.current; maximum = value.maximum;
        }
        else return -1;
        return maximum <= 0 || current < 0 ? -1 : (int) Math.round(Math.max(0, Math.min(1, current / (double) maximum)) * 100);
    }

	static int resourcePackPrayerSpriteId(int prayerIndex)
	{
		return prayerIndex >= 0 && prayerIndex < RESOURCE_PACK_PRAYER_SPRITE_IDS.length
			? RESOURCE_PACK_PRAYER_SPRITE_IDS[prayerIndex]
			: -1;
	}

	private void addHintArrow(List<OverheadsSprite> icons, Actor actor)
	{
		if (!config.showHintArrows() || !client.hasHintArrow() || client.getGameCycle() % 20 >= 10)
		{
			return;
		}

		if (actor == client.getHintArrowNpc())
		{
			add(icons, sprites.get(SpriteID.HEADICONS_HINT, NPC_HINT_SPRITE_INDEX));
		}
		else if (actor == client.getHintArrowPlayer())
		{
			add(icons, sprites.get(SpriteID.HEADICONS_HINT, PLAYER_HINT_SPRITE_INDEX));
		}
	}

	private static void add(List<OverheadsSprite> icons, OverheadsSprite icon)
	{
		if (icon != null)
		{
			icons.add(icon);
		}
	}

	private float projectionDepth(Actor actor, int projectionHeight)
	{
		LocalPoint localLocation = actor.getLocalLocation();
		WorldView worldView = actor.getWorldView();
		Projection projection = worldView == null ? null : worldView.getCanvasProjection();
		if (localLocation == null || projection == null)
		{
			return Float.NaN;
		}

		int worldZ = Perspective.getFootprintTileHeight(
			client,
			localLocation,
			worldView.getPlane(),
			actor.getFootprintSize())
			- actor.getAnimationHeightOffset()
			- projectionHeight;
		float[] projected = projection.project(
			localLocation.getX(),
			worldZ,
			localLocation.getY(),
			projectionBuffer);
		return projected == null || projected.length < 3
			? Float.NaN
			: projected[2];
	}

	static int effectiveScalePercent(
		int configuredPercent,
		boolean scaleWithZoom,
		int projectionScale,
		float projectionDepth)
	{
		int basePercent = clampConfiguredScalePercent(configuredPercent);
		if (!scaleWithZoom
			|| projectionScale <= 0
			|| !Float.isFinite(projectionDepth)
			|| projectionDepth < MIN_PROJECTION_DEPTH)
		{
			return basePercent;
		}

		double magnification = projectionScale / (double) projectionDepth;
		double effectivePercent = basePercent * magnification / REFERENCE_MAGNIFICATION;
		if (!Double.isFinite(effectivePercent))
		{
			return basePercent;
		}
		return (int) clampRenderScalePercent(Math.round(effectivePercent));
	}

	static int scaled(int dimension, int percent)
	{
		long scaled = (long) dimension * clampRenderScalePercent(percent) / 100L;
		return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, scaled));
	}

	static int iconLeft(int anchorX, int canvasWidth, int xOffset)
	{
		return anchorX - canvasWidth / 2 + xOffset;
	}

	static int stackBottomY(int anchorY, int occupiedTopY, int yOffset, int spacing)
	{
		int bottomY = occupiedTopY == OverheadsHealthBarRenderer.NO_OCCUPIED_TOP
			? anchorY
			: Math.min(anchorY, occupiedTopY);
		return bottomY - clampedSpacing(spacing) - yOffset;
	}

	static int nextIconBottomY(int previousIconTopY, int spacing)
	{
		return previousIconTopY - clampedSpacing(spacing);
	}

	static int scaledEdge(int coordinate, int percent)
	{
		long scaled = scaledEdge((long) coordinate, percent);
		return (int) Math.max(Integer.MIN_VALUE, Math.min(Integer.MAX_VALUE, scaled));
	}

	static int scaledSpan(int offset, int dimension, int percent)
	{
		long start = scaledEdge((long) offset, percent);
		long end = scaledEdge((long) offset + dimension, percent);
		return (int) Math.max(1L, Math.min(Integer.MAX_VALUE, end - start));
	}

	private static long scaledEdge(long coordinate, int percent)
	{
		return Math.floorDiv(coordinate * clampRenderScalePercent(percent), 100L);
	}

	private static int clampConfiguredScalePercent(int percent)
	{
		return Math.max(50, Math.min(200, percent));
	}

	private static int clampRenderScalePercent(int percent)
	{
		return (int) clampRenderScalePercent((long) percent);
	}

	private static long clampRenderScalePercent(long percent)
	{
		return Math.max(MIN_RENDER_SCALE_PERCENT, Math.min(MAX_RENDER_SCALE_PERCENT, percent));
	}

	private static int clampedSpacing(int spacing)
	{
		return Math.max(0, Math.min(20, spacing));
	}

	void clearCache()
	{
        if (scoped != null) scoped.forEach(child -> child.clearCache());

		clearPrepared();
		sprites.clear();
        styledSources.clear();
        styledIcons.clear();
	}

	private void clearPrepared()
	{
		preparedActor = null;
		iconBuffer.clear();
		prayerBuffer.clear();
	}
}
