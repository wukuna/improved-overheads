package com.improvedoverheads;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import javax.inject.Inject;
import net.runelite.api.Actor;
import net.runelite.api.Client;
import net.runelite.api.NPC;
import net.runelite.api.NPCComposition;
import net.runelite.api.Point;
import net.runelite.api.Player;
import net.runelite.api.clan.ClanID;
import net.runelite.api.clan.ClanSettings;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.FontManager;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

public class OverheadsOverlay extends Overlay
{
	private static final BufferedImage ANCHOR_IMAGE = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
	private static final int SLOT_WIDTH = 30;
	private static final int SLOT_HEIGHT = 20;
	private static final int SLOT_EDGE_CUT = 7;
	private static final int CACHE_FAILURE_RETRY_CYCLES = 10;
	private static final int MAX_TRANSFORM_DEPTH = 8;
	private static final int NATIVE_UPPER_UI_HEIGHT_PADDING = 15;

	private static final int NATIVE_STACK_CANVAS_ADJUSTMENT = 4;
	private static final Comparator<Actor> ACTOR_RENDER_ORDER = Comparator
		.comparingInt(OverheadsOverlay::actorTypeOrder)
		.thenComparingInt(OverheadsOverlay::actorIndex)
		.thenComparingInt(System::identityHashCode);
	private static final Predicate<Integer> RETRY_FAILED_NPC_OVERHEAD_HEIGHT =
		height -> height == OverheadsNpcOverheadHeight.LOAD_FAILED;

	private final Client client;
	private final OverheadsPlugin plugin;
	private final OverheadsConfig config;
	private final OverheadsSettings settings;
	private final OverheadsActorUiVisibilityTracker actorUiVisibilityTracker;
	private final OverheadsHealthBarRenderer healthBarRenderer;
	private final OverheadsHeadIconRenderer headIconRenderer;
	private final OverheadsOverheadChatRenderer overheadChatRenderer;
	private final OverheadsLocalChatEffectTracker localChatEffectTracker;
	private final OverheadsExtrasRenderer extras;
    private OverheadsIndicators indicators;
    private OverheadsProfiles profiles;
    @Inject
    void configureProfiles(OverheadsProfiles profiles)
    {
        this.profiles = profiles;
        extras.configureProfiles(profiles);
        if (indicators != null) { indicators.configureProfiles(profiles); }
    }
    private OverheadsConfig configFor(Actor actor) { return profiles == null ? config : profiles.config(actor); }
    private OverheadsSettings settingsFor(Actor actor) { return profiles == null ? settings : profiles.settings(actor); }


    @Inject
    void configureIndicators(OverheadsPartyState party, OverheadsLocalTimers timers)
    {
        indicators = new OverheadsIndicators(client, config, party, timers);
        if (profiles != null) { indicators.configureProfiles(profiles); }
        extras.setPartyMember(party::contains);
    }
	private final OverheadsRetryCache<Integer, OverheadsHitmarkDefinition> hitmarkDefinitions =
		new OverheadsRetryCache<>(CACHE_FAILURE_RETRY_CYCLES);
	private final OverheadsActorUiSpriteCache sprites;
	private final OverheadsRetryCache<Integer, Integer> npcOverheadHeights =
		new OverheadsRetryCache<>(CACHE_FAILURE_RETRY_CYCLES);
	private final IntFunction<OverheadsHitmarkDefinition> hitmarkDefinitionLoader =
		this::loadDefinition;
	private final Function<Integer, Integer> npcOverheadHeightLoader =
		this::loadNpcOverheadHeight;
	private final Map<Actor, SlotSize> actorSlotSizes = new HashMap<>();
    private final Map<Actor, OverheadsScrollingHitsplats> scrollingHits = new HashMap<>();
	private final Map<Actor, OverheadsCollisionLayout.Memory> collisionMemories = new java.util.IdentityHashMap<>();

	@Inject
	OverheadsOverlay(
		Client client,
		OverheadsPlugin plugin,
		OverheadsConfig config,
		OverheadsSettings settings,
		OverheadsActorUiVisibilityTracker actorUiVisibilityTracker,
		OverheadsHealthBarRenderer healthBarRenderer,
		OverheadsHeadIconRenderer headIconRenderer,
		OverheadsOverheadChatRenderer overheadChatRenderer,
		OverheadsLocalChatEffectTracker localChatEffectTracker)
	{
		this.client = client;
		this.sprites = new OverheadsActorUiSpriteCache(client);
		this.plugin = plugin;
		this.config = config;
		this.settings = settings;
		this.actorUiVisibilityTracker = actorUiVisibilityTracker;
		this.healthBarRenderer = healthBarRenderer;
		this.headIconRenderer = headIconRenderer;
		this.overheadChatRenderer = overheadChatRenderer;
		this.localChatEffectTracker = localChatEffectTracker;
		this.extras = new OverheadsExtrasRenderer(client, config);

		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.UNDER_WIDGETS);
		setPriority(PRIORITY_HIGH);
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		int gameCycle = client.getGameCycle();
		List<Actor> actors = new ArrayList<>();
		actorUiVisibilityTracker.copyAcceptedInto(actors, gameCycle);
		if (actors.isEmpty())
		{
			actorSlotSizes.clear();
        scrollingHits.clear();
			collisionMemories.clear();
			return null;
		}
		actors.sort(ACTOR_RENDER_ORDER);
		collisionMemories.keySet().retainAll(new java.util.HashSet<>(actors));

		Graphics2D overlayGraphics = (Graphics2D) graphics.create();
		try
		{
            ClanSettings groupIronSettings = client.getClanSettings(ClanID.GROUP_IRONMAN);
			Player localPlayer = client.getLocalPlayer();
			overlayGraphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
			overlayGraphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);

			actorSlotSizes.keySet().removeIf(actor -> !plugin.getHitsplats().containsKey(actor));
            scrollingHits.keySet().removeIf(actor -> !plugin.getHitsplats().containsKey(actor));
			List<Rectangle> occupiedChatBounds = new ArrayList<>();
            Map<OverheadsConfig, OverheadsOverheadChatRenderer.Style> chatStyles = new java.util.IdentityHashMap<>();
            Map<OverheadsConfig, Font> hitFonts = new java.util.IdentityHashMap<>();
			for (Actor actor : actors)
			{
                OverheadsConfig config = configFor(actor);
                OverheadsSettings settings = settingsFor(actor);
                int hitsplatScalePercent = OverheadsRenderLayout.clampScalePercent(settings.hitsplatScalePercent());
                Font hitsplatFont = hitFonts.computeIfAbsent(config, selected ->
                {
                    FontType type = settings.hitsplatFont();
                    return scaleFont(type == null ? FontManager.getRunescapeSmallFont() : type.getFont(), hitsplatScalePercent);
                });
                overlayGraphics.setFont(hitsplatFont);
                OverheadsOverheadChatRenderer.Style chatStyle = chatStyles.computeIfAbsent(config, OverheadsOverheadChatRenderer::captureStyle);

				if (!hasRenderableState(actor))
				{
					continue;
				}
				if (actorUiVisibilityTracker.retainedNativeUi(actor, gameCycle))
				{

					continue;
				}

                try (OverheadsCollisionLayout collision = (config.avoidOverlap() || config.keepOnScreen() || config.layoutPreview())
                    ? new OverheadsCollisionLayout(collisionMemories.computeIfAbsent(actor, ignored -> new OverheadsCollisionLayout.Memory()),
                        config.componentGap(), occupiedChatBounds) : null)
                {
                    if (collision != null)
                    {
                        Rectangle viewport = config.keepOnScreen() && client.getViewportWidth() > 0 && client.getViewportHeight() > 0
                            ? new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(), client.getViewportWidth(), client.getViewportHeight()) : null;
                        collision.configure(viewport, config.layoutPreview() && actor == localPlayer, config.avoidOverlap());
                    }
	                OverheadsOverheadChatRenderer.Style actorChatStyle = chatStyle.scaled(
	                    OverheadsPlacement.zoom(client, actor, config.chatScaleWithZoom()));
	                int chatAscent = overlayGraphics.getFontMetrics(actorChatStyle.getFont()).getAscent();
					boolean drawOthers = (client.getDraw2DMask() & Client.DRAW_2D_OTHERS) != 0;
					boolean chatEnabled = (client.getDraw2DMask() & Client.DRAW_2D_OVERHEAD_TEXT) != 0
						&& actorChatStyle.shouldRender(actor instanceof NPC);
					OverheadsLocalChatEffectTracker.MessageState messageState = null;
					OverheadsOverheadChatEffect chatEffect = null;
					Color chatColor = null;
					boolean renderChat = false;
					if (chatEnabled)
					{
						int chatLifetimeCycles = OverheadsOverheadChatRenderer.chatLifetimeCycles(actor);
						messageState = localChatEffectTracker.messageStateFor(
							actor,
							actor.getOverheadText(),
							actor.getOverheadCycle(),
							gameCycle,
							chatLifetimeCycles);
						chatEffect = localChatEffectTracker.effectFor(
							actor,
							messageState.getText(),
							actorChatStyle.getFallbackEffect(),
							gameCycle);
						Color fallbackChatColor = actorChatStyle.getColor();
						if (actorChatStyle.usesRelationshipColors()
							&& messageState.getOverheadCycle() > 0
							&& messageState.getText() != null
							&& !messageState.getText().isEmpty())
						{
							fallbackChatColor = relationshipChatColor(
								actor,
								localPlayer,
								groupIronSettings,
								actorChatStyle);
						}
						chatColor = localChatEffectTracker.colorFor(
							actor,
							messageState.getText(),
							fallbackChatColor,
							gameCycle);
						renderChat = OverheadsOverheadChatRenderer.hasRenderableMessage(
							actor,
							messageState,
							actorChatStyle,
							chatColor);
					}

					boolean renderHealthBar = drawOthers && healthBarRenderer.hasRenderableHealthBar(actor);
					boolean renderHeadIcons = drawOthers && headIconRenderer.prepare(actor);
					boolean projectActorTop = needsActorTop(
						renderHealthBar,
						renderHeadIcons,
						renderChat) || drawOthers && (extras.hasContent(actor) || indicators != null && indicators.hasContent(actor));
					int projectionHeight = projectActorTop
						? actorProjectionHeight(actorBaseOverheadHeight(actor, gameCycle))
						: 0;
					Point actorTop = projectActorTop
						? actorTopAnchor(actor, projectionHeight)
						: null;
					int upperStackAnchorY = actorTop == null
						? 0
						: actorTop.getY();
					if (collision == null && actorTop != null && renderChat && config.chatPosition() == OverheadsPosition.ABOVE)
					{
						upperStackAnchorY = OverheadsOverheadChatRenderer.chatLaneTop(
							actorTop.getY(),
							chatAscent,
							chatEffect);
					}
					int healthBarTopY = renderHealthBar && (indicators == null || !indicators.replacesHealth(actor))
						? healthBarRenderer.render(
							overlayGraphics,
							actor,
							actorTop,
							upperStackAnchorY, collision)
						: OverheadsHealthBarRenderer.NO_OCCUPIED_TOP;
					int headIconProjectionHeight = projectionHeight;
					if (drawOthers && actorTop != null)
					{
						healthBarTopY = extras.render(overlayGraphics, actor, actorTop, collision == null ? healthBarTopY : Integer.MAX_VALUE, collision);
                        if (indicators != null) { indicators.render(overlayGraphics, actor, actorTop, collision); }
					}
					Point headIconAnchor = actorTop;
					if (renderHeadIcons && actorTop != null)
					{
						int zOffset = Math.max(-1024, Math.min(1024, config.headIconZOffset()));
						if (zOffset != 0)
						{
							headIconProjectionHeight += zOffset;
							headIconAnchor = actorTopAnchor(actor, headIconProjectionHeight);
						}
					}
					int occupiedTopY = headIconRenderer.renderPrepared(
						overlayGraphics,
						actor,
						headIconProjectionHeight,
						actorTop,
						headIconAnchor,
						upperStackAnchorY,
						healthBarTopY, collision);
					if (renderChat)
					{
	                    Point chatAnchor = actorTop;
	                    int chatOccupiedTop = occupiedTopY;
	                    if (actorTop != null && config.chatPosition() != OverheadsPosition.ABOVE)
	                    {
	                        java.awt.FontMetrics metrics = overlayGraphics.getFontMetrics(actorChatStyle.getFont());
	                        Rectangle bounds = OverheadsPlacement.bounds(actor, actorTop, config.chatPosition(),
	                            metrics.stringWidth(OverheadsOverheadChatRenderer.displayText(messageState.getText())),
	                            metrics.getHeight(), 0, 0);
	                        chatAnchor = bounds == null ? null : new Point(bounds.x + bounds.width / 2,
	                            bounds.y + metrics.getAscent() - 2);
	                        chatOccupiedTop = OverheadsHealthBarRenderer.NO_OCCUPIED_TOP;
	                    }
						overheadChatRenderer.render(
							overlayGraphics,
							actor,
							chatAnchor,
							messageState,
							actorChatStyle,
							chatColor,
							chatEffect,
							gameCycle,
							chatOccupiedTop,
							upperStackAnchorY,
							occupiedChatBounds, collision, config.chatPosition());
					}

					List<OverheadsHitsplat> actorHitsplats = plugin.getHitsplats().get(actor);
					if (drawOthers && actorHitsplats != null && !actorHitsplats.isEmpty())
					{
	                    int actorScale = OverheadsRenderLayout.clampScalePercent((int) Math.round(hitsplatScalePercent
	                        * OverheadsPlacement.zoom(client, actor, config.hitsplatScaleWithZoom())));
	                    overlayGraphics.setFont(scaleFont(hitsplatFont, actorScale));
						renderActorHitsplats(
							overlayGraphics,
							actor,
							actorHitsplats,
							gameCycle,
							actorScale, collision);
					}
	                if (collision != null)
	                {
	                    collision.render();
	                }
                }
			}
		}
		finally
		{
			overlayGraphics.dispose();
		}

		return null;
	}

	void gameTick()
	{
		healthBarRenderer.vitals().gameTick();
	}

	void clientTick()
	{
		healthBarRenderer.vitals().clientTick();
	}

	void clearVitals()
	{
		healthBarRenderer.vitals().clear();
	}

	void reflow()
	{
		collisionMemories.clear();
	}

	static Color relationshipChatColor(
		Actor actor,
		Player localPlayer,
		ClanSettings groupIronSettings,
		OverheadsOverheadChatRenderer.Style style)
	{
		if (!(actor instanceof Player) || actor == localPlayer)
		{
			return style == null ? Color.YELLOW : style.getColor();
		}

		Player player = (Player) actor;
		String playerName = player.getName();
		boolean groupIronMember = groupIronSettings != null
			&& playerName != null
			&& groupIronSettings.findMember(playerName) != null;
		return OverheadsOverheadChatRenderer.relationshipColor(
			style,
			groupIronMember,
			player.isFriend(),
			player.isClanMember());
	}

	private static boolean hasRenderableState(Actor actor)
	{
		if (actor instanceof NPC)
		{
			return ((NPC) actor).getTransformedComposition() != null;
		}

		if (actor instanceof Player)
		{
			Player player = (Player) actor;
			return player.getName() != null && player.getPlayerComposition() != null;
		}

		return true;
	}

	Point actorTopAnchor(Actor actor, int projectionHeight)
	{
		if (actor == null)
		{
			return null;
		}

		Point projected = actor.getCanvasImageLocation(
			ANCHOR_IMAGE,
			projectionHeight);
		return nativeStackAnchor(projected);
	}

	private int actorBaseOverheadHeight(Actor actor, int gameCycle)
	{
		int logicalHeight = actor.getLogicalHeight();
		if (!(actor instanceof NPC))
		{
			return logicalHeight;
		}

		NPCComposition composition = ((NPC) actor).getComposition();
		if (composition == null)
		{
			return logicalHeight;
		}

		int definitionHeight = npcOverheadHeights.get(
			composition.getId(),
			gameCycle,
			npcOverheadHeightLoader,
			RETRY_FAILED_NPC_OVERHEAD_HEIGHT);
		return selectBaseOverheadHeight(logicalHeight, definitionHeight);
	}

	static int selectBaseOverheadHeight(int logicalHeight, int definitionHeight)
	{
		return definitionHeight >= 0 ? definitionHeight : logicalHeight;
	}

	static int actorProjectionHeight(int baseOverheadHeight)
	{

		return baseOverheadHeight + NATIVE_UPPER_UI_HEIGHT_PADDING;
	}

	static Point nativeStackAnchor(Point projected)
	{
		return projected == null
			? null
			: new Point(
				projected.getX(),
				projected.getY() + NATIVE_STACK_CANVAS_ADJUSTMENT);
	}

	static boolean needsActorTop(
		boolean renderHealthBar,
		boolean renderHeadIcons,
		boolean renderChat)
	{
		return renderHealthBar || renderHeadIcons || renderChat;
	}

	private static int actorTypeOrder(Actor actor)
	{
		return actor instanceof Player ? 0 : actor instanceof NPC ? 1 : 2;
	}

	private static int actorIndex(Actor actor)
	{
		if (actor instanceof Player)
		{
			return ((Player) actor).getId();
		}
		if (actor instanceof NPC)
		{
			return ((NPC) actor).getIndex();
		}
		return 0;
	}

	private boolean renderActorHitsplats(
		Graphics2D graphics,
		Actor actor,
		List<OverheadsHitsplat> hitsplats,
		int gameCycle,
		int scalePercent, OverheadsCollisionLayout collision)
	{
        OverheadsConfig config = configFor(actor);
        OverheadsSettings settings = settingsFor(actor);
		if (plugin.shouldDisableHitsplatsForActor(actor) || opacityMultiplier(config) <= 0)
		{
			return false;
		}

		List<OverheadsHitsplat> visibleHitsplats = new ArrayList<>(hitsplats.size());
		for (OverheadsHitsplat hitsplat : hitsplats)
		{
			if (hitsplat.isExpired(gameCycle)
				|| settings.hideZeroHitsplats() && hitsplat.getAmount() == 0
				|| settings.onlyDisplayMine() && !hitsplat.isMine())
			{
				continue;
			}

			visibleHitsplats.add(hitsplat);
		}

		if (visibleHitsplats.isEmpty())
		{
			return false;
		}

        Point top = actorTopAnchor(actor, actorProjectionHeight(actorBaseOverheadHeight(actor, gameCycle)));
        if (top == null)
		{
			return true;
		}

		visibleHitsplats.sort(Comparator
			.comparing(OverheadsHitsplat::isMine)
			.thenComparingLong(OverheadsHitsplat::getSequence));
		List<PreparedHitsplat> preparedHitsplats = new ArrayList<>(visibleHitsplats.size());
		int slotWidth = OverheadsRenderLayout.scaleDimension(SLOT_WIDTH, scalePercent);
		int slotHeight = OverheadsRenderLayout.scaleDimension(SLOT_HEIGHT, scalePercent);
		int maxHitsplats = config.maxHitsplats();
		OverheadsSpriteScalingMode scalingMode = spriteScalingMode(config);
		boolean bossLike = plugin.isBossLikeTarget(actor);
		int minRadius = plugin.minRadiusFor(actor, bossLike);
		int maxRadius = plugin.maxRadiusFor(actor, bossLike);
		for (OverheadsHitsplat hitsplat : visibleHitsplats)
		{
			if (maxHitsplats > 0 && hitsplat.getPosition() >= maxHitsplats)
			{
				continue;
			}

			if (!OverheadsLayout.isPositionWithinRadiusLimit(
				hitsplat.getPosition(),
				minRadius,
				maxRadius,
				settings.layoutShape()))
			{
				continue;
			}

			PreparedHitsplat preparedHitsplat = prepareHitsplat(config,
				graphics,
				hitsplat,
				scalePercent,
				scalingMode);
			if (preparedHitsplat == null)
			{
				continue;
			}

			preparedHitsplats.add(preparedHitsplat);
			Rectangle modernBounds = preparedHitsplat.style == OverheadsHitsplatStyle.OSRS ? null
				: OverheadsModernHitsplatRenderer.paintedBounds(graphics, preparedHitsplat.amountText, scalePercent, preparedHitsplat.style, config);
			slotWidth = Math.max(slotWidth, modernBounds == null ? preparedHitsplat.layout.getWidth() : 2 * Math.max(-modernBounds.x, modernBounds.x + modernBounds.width));
			slotHeight = Math.max(slotHeight, modernBounds == null ? preparedHitsplat.layout.getHeight() : 2 * Math.max(-modernBounds.y, modernBounds.y + modernBounds.height));
		}

		SlotSize slotSize = actorSlotSizes.computeIfAbsent(actor, ignored -> new SlotSize());
		slotSize.growTo(slotWidth, slotHeight, scalePercent);
        boolean scrolling = config.hitsplatMotion() != OverheadsHitsplatMotion.FIXED;
        Map<Long, OverheadsOffset> scrollOffsets = null;
        if (scrolling)
        {
            List<OverheadsHitsplat> prepared = new ArrayList<>(preparedHitsplats.size());
            for (PreparedHitsplat hit : preparedHitsplats)
            {
                prepared.add(hit.hitsplat);
            }
            scrollOffsets = scrollingHits.computeIfAbsent(actor, ignored -> new OverheadsScrollingHitsplats())
                .layout(prepared, gameCycle, slotSize.width, slotSize.height, scalePercent,
                    config.hitsplatScrollColumns(), settings.xSpacing(), settings.ySpacing(),
                    config.hitsplatScrollDistance(), config.hitsplatMotion());
        }
		List<OverheadsOffset> offsets = new ArrayList<>(preparedHitsplats.size());
		Rectangle layoutBounds = null;
		for (PreparedHitsplat preparedHitsplat : preparedHitsplats)
		{
			OverheadsOffset offset = scrolling ? scrollOffsets.get(preparedHitsplat.hitsplat.getSequence()) : OverheadsLayout.offsetFor(
				preparedHitsplat.hitsplat.getPosition(),
				slotSize.width,
				slotSize.height,
				OverheadsRenderLayout.scaleDimension(SLOT_EDGE_CUT, scalePercent),
				settings.layoutShape(),
				settings.layoutDirection(),
				settings.layoutBehavior(),
				OverheadsRenderLayout.scaleCoordinate(settings.xSpacing(), scalePercent),
				OverheadsRenderLayout.scaleCoordinate(settings.ySpacing(), scalePercent),
				minRadius);
			offsets.add(offset);
			Rectangle slot = new Rectangle(offset.getX() - slotSize.width / 2,
				offset.getY() - slotSize.height / 2, slotSize.width, slotSize.height);
			layoutBounds = layoutBounds == null ? slot : layoutBounds.union(slot);
		}
		if (layoutBounds == null)
		{
			return true;
		}
        int baseWidth = scrolling ? Math.max(1, Math.min(4, config.hitsplatScrollColumns())) * slotSize.width
            + (Math.max(1, Math.min(4, config.hitsplatScrollColumns())) - 1)
                * OverheadsRenderLayout.scaleCoordinate(Math.max(0, settings.xSpacing()) + 2, scalePercent)
            : layoutBounds.width;
        Rectangle group = OverheadsPlacement.bounds(actor, top, config.hitsplatPosition(),
            baseWidth, scrolling ? slotSize.height : layoutBounds.height, 0, 0);
		if (group == null)
		{
			return true;
		}
        Point anchor = scrolling ? new Point(group.x + group.width / 2, group.y + group.height / 2)
            : new Point(group.x - layoutBounds.x, group.y - layoutBounds.y);
        Rectangle painted = new Rectangle(layoutBounds);
        painted.translate(anchor.getX() + xOffsetFor(config, bossLike), anchor.getY() - yOffsetFor(config, bossLike));
        painted.grow(2, 2);
		OverheadsCollisionLayout.draw(collision, graphics, "hits", config.hitsplatPosition(), painted, target ->
		{
		for (int i = 0; i < preparedHitsplats.size(); i++)
		{
			PreparedHitsplat preparedHitsplat = preparedHitsplats.get(i);
			OverheadsOffset offset = offsets.get(i);
			drawHitsplat(config,
				target,
				preparedHitsplat,
				anchor.getX() + offset.getX() + xOffsetFor(config, bossLike),
				anchor.getY() + offset.getY() - yOffsetFor(config, bossLike),
				gameCycle);
		}

		});

		return true;
	}

	private PreparedHitsplat prepareHitsplat(OverheadsConfig config,
		Graphics2D graphics,
		OverheadsHitsplat hitsplat,
		int scalePercent,
		OverheadsSpriteScalingMode scalingMode)
	{
		OverheadsHitmarkDefinition definition = getDefinition(hitsplat.getHitsplatType());
		if (definition == null || !definition.hasRenderableContent())
		{
			return null;
		}

		OverheadsHitsplatStyle style = OverheadsModernHitsplatRenderer.supports(hitsplat.getHitsplatType())
			? config.hitsplatStyle() : OverheadsHitsplatStyle.OSRS;
		boolean nativeStyle = style == OverheadsHitsplatStyle.OSRS;
		OverheadsSprite first = nativeStyle ? getSprite(definition.getFirstSpriteId()) : null;
		OverheadsSprite middle = nativeStyle ? getSprite(definition.getMiddleSpriteId()) : null;
		OverheadsSprite second = nativeStyle ? getSprite(definition.getSecondSpriteId()) : null;
		OverheadsSprite last = nativeStyle ? getSprite(definition.getLastSpriteId()) : null;

		String amountText = definition.formatAmount(hitsplat.getAmount());
		if (amountText.isEmpty() && first == null && middle == null && second == null && last == null)
		{
			return null;
		}

		OverheadsRenderLayout layout = definition.isFallbackDefinition()
			? OverheadsRenderLayout.singleSprite(first, amountText, graphics.getFontMetrics(), scalePercent)
			: OverheadsRenderLayout.create(
			first,
			middle,
			second,
			last,
			amountText,
			graphics.getFontMetrics(),
			definition.getTextYOffset(),
			scalePercent);
		return new PreparedHitsplat(
			hitsplat,
			definition,
			first,
			middle,
			second,
			last,
			amountText,
			layout,
			scalePercent,
			scalingMode,
			style);
	}

	private void drawHitsplat(OverheadsConfig config, Graphics2D graphics, PreparedHitsplat prepared, int centerX, int centerY, int gameCycle)
	{
		Graphics2D hitsplatGraphics = (Graphics2D) graphics.create();
		float alpha = prepared.hitsplat.getAlpha(gameCycle) * opacityMultiplier(config);
		try
		{
			hitsplatGraphics.setComposite(AlphaComposite.SrcOver.derive(alpha));
			if (prepared.style != OverheadsHitsplatStyle.OSRS)
			{
				OverheadsModernHitsplatRenderer.render(hitsplatGraphics, prepared.amountText,
					prepared.hitsplat.getHitsplatType(), centerX, centerY, prepared.scalePercent, prepared.style,
					config);
				return;
			}
			hitsplatGraphics.setRenderingHint(
				RenderingHints.KEY_INTERPOLATION,
				prepared.scalingMode.getInterpolationHint());
			drawScaledSprite(
				hitsplatGraphics,
				prepared.first,
				centerX + prepared.layout.getFirstX(),
				centerY + prepared.layout.getSpriteY(),
				prepared.scalePercent,
				prepared.scalingMode);
			drawScaledSprite(
				hitsplatGraphics,
				prepared.second,
				centerX + prepared.layout.getSecondX(),
				centerY + prepared.layout.getSpriteY(),
				prepared.scalePercent,
				prepared.scalingMode);
			for (int i = 0; i < prepared.layout.getMiddleRepeatCount(); i++)
			{
				drawScaledSprite(
					hitsplatGraphics,
					prepared.middle,
					centerX + prepared.layout.getMiddleX()
						+ i * OverheadsRenderLayout.width(prepared.middle, prepared.scalePercent),
					centerY + prepared.layout.getSpriteY(),
					prepared.scalePercent,
					prepared.scalingMode);
			}
			drawScaledSprite(
				hitsplatGraphics,
				prepared.last,
				centerX + prepared.layout.getLastX(),
				centerY + prepared.layout.getSpriteY(),
				prepared.scalePercent,
				prepared.scalingMode);

			int shadowOffset = OverheadsRenderLayout.scaleDimension(1, prepared.scalePercent);
			hitsplatGraphics.setColor(Color.BLACK);
			hitsplatGraphics.drawString(
				prepared.amountText,
				centerX + prepared.layout.getTextX() + shadowOffset,
				centerY + prepared.layout.getTextBaseline() + shadowOffset);
			hitsplatGraphics.setColor(new Color(prepared.definition.getTextColor() | 0xFF000000, true));
			hitsplatGraphics.drawString(
				prepared.amountText,
				centerX + prepared.layout.getTextX(),
				centerY + prepared.layout.getTextBaseline());
		}
		finally
		{
			hitsplatGraphics.dispose();
		}
	}

	OverheadsHitmarkDefinition getDefinition(int hitsplatType)
	{
		return resolveDefinition(hitsplatType, client, hitmarkDefinitionLoader);
	}

	static OverheadsHitmarkDefinition resolveDefinition(
		int hitsplatType,
		Client client,
		IntFunction<OverheadsHitmarkDefinition> loader)
	{
		OverheadsHitmarkDefinition definition = loader.apply(hitsplatType);
		for (int depth = 0; definition != null && depth < MAX_TRANSFORM_DEPTH; depth++)
		{
			if (!definition.hasTransforms())
			{
				return definition;
			}

			int transformedType = definition.getTransformedType(client);
			if (transformedType == OverheadsHitmarkDefinition.TRANSFORM_UNAVAILABLE)
			{
				return definition;
			}
			if (transformedType < 0)
			{
				return null;
			}
			if (transformedType == hitsplatType)
			{
				return definition;
			}

			hitsplatType = transformedType;
			definition = loader.apply(hitsplatType);
		}

		return definition;
	}

	private OverheadsHitmarkDefinition loadDefinition(int hitsplatType)
	{
		return hitmarkDefinitions.get(
			hitsplatType,
			safeGameCycle(),
			id -> OverheadsHitmarkDefinition.load(client, id),
			OverheadsHitmarkDefinition::isFallbackDefinition);
	}

	private Integer loadNpcOverheadHeight(Integer npcDefinitionId)
	{
		return OverheadsNpcOverheadHeight.load(client, npcDefinitionId);
	}

	OverheadsSprite getSprite(int spriteId)
	{
		if (spriteId < 0)
		{
			return null;
		}

		OverheadsSprite override = sprites.getResourcePackOverride(spriteId);
		return override == null ? sprites.get(spriteId, 0) : override;
	}

	static int nativeAppearanceGameCycle(
		OverheadsHitmarkDefinition definition,
		int disappearsOnGameCycle,
		int eventGameCycle)
	{
		if (definition == null || definition.isFallbackDefinition())
		{

			return eventGameCycle;
		}

		return definition.appearanceGameCycle(disappearsOnGameCycle);
	}

	void clearCaches()
	{
		hitmarkDefinitions.clear();
		sprites.clear();
		npcOverheadHeights.clear();
		actorSlotSizes.clear();
        scrollingHits.clear();
		collisionMemories.clear();
		healthBarRenderer.clearCache();
		headIconRenderer.clearCache();
		extras.clear();
        if (indicators != null) { indicators.clear(); }
	}

	void removeActor(Actor actor)
	{
		actorSlotSizes.remove(actor);
        scrollingHits.remove(actor);
		collisionMemories.remove(actor);
		healthBarRenderer.remove(actor);
	}

	void recordHealthBarDamage(Actor actor, int hitsplatType, int amount)
	{
		healthBarRenderer.recordDamage(actor, hitsplatType, amount);
	}

	void clearHitsplatLayoutCache()
	{
		actorSlotSizes.clear();
        scrollingHits.clear();
		collisionMemories.clear();
	}

	private int safeGameCycle()
	{
		try
		{
			return client.getGameCycle();
		}
		catch (RuntimeException ex)
		{
			return 0;
		}
	}

	static void drawSprite(Graphics2D graphics, OverheadsSprite sprite, int x, int y)
	{
		if (sprite != null)
		{
			graphics.drawImage(
				sprite.getImage(),
				x + sprite.getOffsetX(),
				y + sprite.getOffsetY(),
				null);
		}
	}

	static void drawScaledSprite(
		Graphics2D graphics,
		OverheadsSprite sprite,
		int x,
		int y,
		int scalePercent,
		OverheadsSpriteScalingMode scalingMode)
	{
		if (sprite == null)
		{
			return;
		}

		int targetWidth = OverheadsRenderLayout.scaleDimension(sprite.getWidth(), scalePercent);
		int targetHeight = OverheadsRenderLayout.scaleDimension(sprite.getHeight(), scalePercent);
		BufferedImage image = sprite.getImageForScaling(scalingMode, targetWidth, targetHeight);
		graphics.drawImage(
			image,
			x + OverheadsRenderLayout.scaleCoordinate(sprite.getOffsetX(), scalePercent),
			y + OverheadsRenderLayout.scaleCoordinate(sprite.getOffsetY(), scalePercent),
			targetWidth,
			targetHeight,
			null);
	}

	static Font scaleFont(Font font, int scalePercent)
	{
		float scaledSize = font.getSize2D()
			* OverheadsRenderLayout.clampScalePercent(scalePercent)
			/ 100.0f;
		return font.deriveFont(Math.max(1.0f, scaledSize));
	}

	private OverheadsSpriteScalingMode spriteScalingMode(OverheadsConfig config)
	{
		OverheadsSpriteScalingMode scalingMode = config.spriteScalingMode();
		return scalingMode == null ? OverheadsSpriteScalingMode.XBR : scalingMode;
	}

	private float opacityMultiplier(OverheadsConfig config)
	{
		return Math.max(0, Math.min(100, config.opacityPercent())) / 100.0f;
	}

	private int xOffsetFor(OverheadsConfig config, boolean bossLike)
	{
		return config.hitsplatXOffset() + (bossLike ? config.largeTargetXOffset() : 0);
	}

	private int yOffsetFor(OverheadsConfig config, boolean bossLike)
	{
		return config.hitsplatYOffset() + (bossLike ? config.largeTargetYOffset() : 0);
	}

	private static final class PreparedHitsplat
	{
		private final OverheadsHitsplat hitsplat;
		private final OverheadsHitmarkDefinition definition;
		private final OverheadsSprite first;
		private final OverheadsSprite middle;
		private final OverheadsSprite second;
		private final OverheadsSprite last;
		private final String amountText;
		private final OverheadsRenderLayout layout;
		private final int scalePercent;
		private final OverheadsSpriteScalingMode scalingMode;
		private final OverheadsHitsplatStyle style;

		private PreparedHitsplat(
			OverheadsHitsplat hitsplat,
			OverheadsHitmarkDefinition definition,
			OverheadsSprite first,
			OverheadsSprite middle,
			OverheadsSprite second,
			OverheadsSprite last,
			String amountText,
			OverheadsRenderLayout layout,
			int scalePercent,
			OverheadsSpriteScalingMode scalingMode, OverheadsHitsplatStyle style)
		{
			this.hitsplat = hitsplat;
			this.definition = definition;
			this.first = first;
			this.middle = middle;
			this.second = second;
			this.last = last;
			this.amountText = amountText;
			this.layout = layout;
			this.scalePercent = scalePercent;
			this.scalingMode = scalingMode;
			this.style = style;
		}
	}

	private static final class SlotSize
	{
		private int width;
		private int height;
		private int scale;

		private void growTo(int width, int height, int scale)
		{
			if (this.scale != scale)
			{
				this.width = 0;
				this.height = 0;
				this.scale = scale;
			}
			this.width = Math.max(this.width, width);
			this.height = Math.max(this.height, height);
		}
	}
}
