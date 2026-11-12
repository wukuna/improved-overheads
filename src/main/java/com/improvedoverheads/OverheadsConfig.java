package com.improvedoverheads;

import java.awt.Color;
import net.runelite.client.config.Alpha;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.FontType;
import net.runelite.client.config.Range;
import net.runelite.client.config.Units;

@ConfigGroup(OverheadsConfig.GROUP)
public interface OverheadsConfig extends Config
{
    @ConfigItem(keyName = "hideSidePanel", name = "Hide side panel", position = -1,
        description = "Hide the sidebar icon while keeping the plugin enabled. Turn this off here to restore the panel.")
    default boolean hideSidePanel() { return false; }

    @ConfigSection(name = "My player", description = "Quick visibility controls for your player. Use the Improved Overheads side panel for all actor groups and detailed settings.", position = 0)
    String QUICK_SECTION = "quickVisibility";

    @ConfigItem(hidden = true, keyName = "avoidOverlap", name = "Avoid overlaps",
        description = "Space components outward on each side without moving continuously visible components back inward.", section = HEALTH_BARS_SECTION, position = 315)
    default boolean avoidOverlap()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "componentGap", name = "Component spacing",
        description = "Minimum spacing between visible overhead components.", section = HEALTH_BARS_SECTION, position = 316)
    @Range(min = 0, max = 20)
    default int componentGap()
    {
        return 4;
    }

    @ConfigItem(hidden = true, keyName = "healingSplatEnabled", name = "Healing: Enabled",
        description = "Show actual increases in your HP, including food, regeneration and healing effects.", position = 3300)
    default boolean healingSplatEnabled() { return false; }

    @ConfigItem(hidden = true, keyName = "healingSplatDisplay", name = "Healing: Display",
        description = "Show an icon, restored amount, or both.", position = 3301)
    default OverheadsRestoreDisplay healingSplatDisplay() { return OverheadsRestoreDisplay.ICON_AND_NUMBER; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "healingSplatColor", name = "Healing: Number color",
        description = "Color and alpha of the restored amount.", position = 3302)
    default Color healingSplatColor() { return new Color(105, 240, 135); }

    @ConfigItem(hidden = true, keyName = "healingSplatFont", name = "Healing: Number font",
        description = "Font and size of the restored amount.", position = 3303)
    default FontType healingSplatFont() { return FontType.SMALL; }

    @Range(min = 0, max = 100)
    @ConfigItem(hidden = true, keyName = "healingSplatOpacity", name = "Healing: Opacity",
        description = "Opacity of the icon and number.", position = 3304)
    default int healingSplatOpacity() { return 100; }

    @Range(min = 200, max = 5000)
    @ConfigItem(hidden = true, keyName = "healingSplatDuration", name = "Healing: Duration (ms)",
        description = "Time each restoration splat remains visible.", position = 3305)
    default int healingSplatDuration() { return 1200; }

    @ConfigItem(hidden = true, keyName = "healingSplatPosition", name = "Healing: Position",
        description = "Native places splats on your character; other positions use overhead spacing.", position = 3306)
    default OverheadsHitsplatPosition healingSplatPosition() { return OverheadsHitsplatPosition.NATIVE; }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "healingSplatXOffset", name = "Healing: X offset",
        description = "Horizontal offset in pixels.", position = 3307)
    default int healingSplatXOffset() { return -24; }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "healingSplatYOffset", name = "Healing: Y offset",
        description = "Vertical offset in pixels. Positive moves upward.", position = 3308)
    default int healingSplatYOffset() { return 0; }

    @Range(min = 25, max = 300)
    @ConfigItem(hidden = true, keyName = "healingSplatScalePercent", name = "Healing: Scale (%)",
        description = "Scale the icon and number together.", position = 3309)
    default int healingSplatScalePercent() { return 100; }

    @ConfigItem(hidden = true, keyName = "healingSplatScaleWithZoom", name = "Healing: Scale with zoom",
        description = "Scale with camera zoom.", position = 3310)
    default boolean healingSplatScaleWithZoom() { return false; }

    @ConfigItem(hidden = true, keyName = "prayerSplatEnabled", name = "Prayer restoration: Enabled",
        description = "Show actual increases in your prayer points.", position = 3300)
    default boolean prayerSplatEnabled() { return false; }

    @ConfigItem(hidden = true, keyName = "prayerSplatDisplay", name = "Prayer restoration: Display",
        description = "Show an icon, restored amount, or both.", position = 3301)
    default OverheadsRestoreDisplay prayerSplatDisplay() { return OverheadsRestoreDisplay.ICON_AND_NUMBER; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerSplatColor", name = "Prayer restoration: Number color",
        description = "Color and alpha of the restored amount.", position = 3302)
    default Color prayerSplatColor() { return new Color(110, 215, 255); }

    @ConfigItem(hidden = true, keyName = "prayerSplatFont", name = "Prayer restoration: Number font",
        description = "Font and size of the restored amount.", position = 3303)
    default FontType prayerSplatFont() { return FontType.SMALL; }

    @Range(min = 0, max = 100)
    @ConfigItem(hidden = true, keyName = "prayerSplatOpacity", name = "Prayer restoration: Opacity",
        description = "Opacity of the icon and number.", position = 3304)
    default int prayerSplatOpacity() { return 100; }

    @Range(min = 200, max = 5000)
    @ConfigItem(hidden = true, keyName = "prayerSplatDuration", name = "Prayer restoration: Duration (ms)",
        description = "Time each restoration splat remains visible.", position = 3305)
    default int prayerSplatDuration() { return 1200; }

    @ConfigItem(hidden = true, keyName = "prayerSplatPosition", name = "Prayer restoration: Position",
        description = "Native places splats on your character; other positions use overhead spacing.", position = 3306)
    default OverheadsHitsplatPosition prayerSplatPosition() { return OverheadsHitsplatPosition.NATIVE; }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "prayerSplatXOffset", name = "Prayer restoration: X offset",
        description = "Horizontal offset in pixels.", position = 3307)
    default int prayerSplatXOffset() { return 24; }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "prayerSplatYOffset", name = "Prayer restoration: Y offset",
        description = "Vertical offset in pixels. Positive moves upward.", position = 3308)
    default int prayerSplatYOffset() { return 0; }

    @Range(min = 25, max = 300)
    @ConfigItem(hidden = true, keyName = "prayerSplatScalePercent", name = "Prayer restoration: Scale (%)",
        description = "Scale the icon and number together.", position = 3309)
    default int prayerSplatScalePercent() { return 100; }

    @ConfigItem(hidden = true, keyName = "prayerSplatScaleWithZoom", name = "Prayer restoration: Scale with zoom",
        description = "Scale with camera zoom.", position = 3310)
    default boolean prayerSplatScaleWithZoom() { return false; }

    String PRAYER_SECTION = "prayerBar";
    String RUN_SECTION = "runBar";
    String SPECIAL_SECTION = "specialBar";

    @ConfigItem(hidden = true, keyName = "prayerRestorePreview", name = "Prayer restore preview", description = "Preview prayer restored by the inventory item under the pointer.", section = PRAYER_SECTION, position = 230)
    default boolean prayerRestorePreview() { return true; }
    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerRestorePreviewColor", name = "Prayer restore color", description = "Color of the projected prayer restore.", section = PRAYER_SECTION, position = 231)
    default Color prayerRestorePreviewColor() { return new Color(115, 235, 170, 190); }
    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerLowColor", name = "Low prayer color", description = "Warning color for low prayer.", section = PRAYER_SECTION, position = 232)
    default Color prayerLowColor() { return new Color(255, 106, 87); }

	String GROUP = "improvedoverheads";
	String DISABLE_ENEMY_HITSPLATS_KEY = "disableEnemyHitsplats";
	String DISABLE_ALLY_HITSPLATS_KEY = "disableAllyHitsplats";
	String DISABLE_MY_HITSPLATS_KEY = "disableMyHitsplats";
	String MAX_HITSPLATS_KEY = "maxHitsplats";
	String REUSE_OLD_HITSPLAT_SLOTS_KEY = "reuseOldHitsplatSlots";
	String HITSPLAT_REUSE_INTERVAL_KEY = "hitsplatReuseInterval";
	String OPACITY_PERCENT_KEY = "opacityPercent";
	String ONLY_DISPLAY_MINE_KEY = "onlyDisplayMine";
	String PRIORITIZE_MINE_KEY = "prioritizeMine";
	String HIDE_ZERO_HITSPLATS_KEY = "hideZeroHitsplats";
	String LAYOUT_SHAPE_KEY = "layoutShape";
	String LAYOUT_DIRECTION_KEY = "layoutDirection";
	String LAYOUT_BEHAVIOR_KEY = "layoutBehavior";
	String MIN_RADIUS_KEY = "minRadius";
	String MAX_RADIUS_KEY = "maxRadius";
	String X_SPACING_KEY = "xSpacingPixels";
	String Y_SPACING_KEY = "ySpacingPixels";
	String HITSPLAT_FONT_KEY = "hitsplatFont";
	String HITSPLAT_SCALE_PERCENT_KEY = "hitsplatScalePercent";
	String LARGE_TARGET_DETECTION_KEY = "largeTargetDetection";
	String LARGE_TARGET_SIZE_KEY = "largeTargetSize";
	String LARGE_TARGET_HEALTH_SCALE_KEY = "largeTargetHealthScale";
	String LARGE_TARGET_X_OFFSET_KEY = "largeTargetXOffset";
	String LARGE_TARGET_Y_OFFSET_KEY = "largeTargetYOffset";
	String FADE_IN_DURATION_KEY = "fadeInDuration";
	String FULL_OPACITY_DURATION_KEY = "fullOpacityDuration";
	String FADE_OUT_DURATION_KEY = "fadeOutDuration";
	String HEALTH_BARS_ENABLED_KEY = "healthBarsEnabled";
	String HEALTH_BAR_STYLE_KEY = "healthBarStyle";
	String HEALTH_BAR_SCALE_MODE_KEY = "healthBarScaleMode";
	String HEALTH_BAR_SCALE_PERCENT_KEY = "healthBarScalePercent";
	String HEALTH_BAR_SCALE_THRESHOLD_KEY = "healthBarScaleThreshold";
	String HEALTH_BAR_LARGE_SCALE_PERCENT_KEY = "healthBarLargeScalePercent";
	String HEALTH_BAR_LARGE_HEIGHT_SCALE_PERCENT_KEY = "healthBarLargeHeightScalePercent";
	String HEALTH_BAR_SOLID_WIDTH_KEY = "healthBarSolidWidth";
	String HEALTH_BAR_HEIGHT_KEY = "healthBarHeight";
	String HEALTH_BAR_X_OFFSET_KEY = "healthBarXOffset";
	String HEALTH_BAR_Y_OFFSET_KEY = "healthBarYOffset";
	String HEALTH_BAR_FILL_DIRECTION_KEY = "healthBarFillDirection";
	String HEALTH_BAR_FRONT_GRADIENT_COORDINATES_KEY = "healthBarFrontGradientCoordinates";
	String HEALTH_BAR_BACK_GRADIENT_COORDINATES_KEY = "healthBarBackGradientCoordinates";
	String HEALTH_BAR_FRONT_GRADIENT_KEY = "healthBarFrontGradient";
	String HEALTH_BAR_FRONT_COLOR_KEY = "healthBarFrontColor";
	String HEALTH_BAR_FRONT_SECONDARY_COLOR_KEY = "healthBarFrontSecondaryColor";
	String HEALTH_BAR_POISONED_FRONT_COLOR_KEY = "healthBarPoisonedFrontColor";
	String HEALTH_BAR_BACK_GRADIENT_KEY = "healthBarBackGradient";
	String HEALTH_BAR_BACK_COLOR_KEY = "healthBarBackColor";
	String HEALTH_BAR_BACK_SECONDARY_COLOR_KEY = "healthBarBackSecondaryColor";
	String HEALTH_BAR_DAMAGE_TRAIL_ENABLED_KEY = "healthBarDamageTrailEnabled";
	String HEALTH_BAR_DAMAGE_TRAIL_COLOR_KEY = "healthBarDamageTrailColor";
	String HEALTH_BAR_DAMAGE_TRAIL_HOLD_KEY = "healthBarDamageTrailHold";
	String HEALTH_BAR_DAMAGE_TRAIL_DRAIN_KEY = "healthBarDamageTrailDrain";
	String HEALTH_BAR_SEGMENTS_ENABLED_KEY = "healthBarSegmentsEnabled";
	String HEALTH_BAR_SEGMENT_VALUE_MODE_KEY = "healthBarSegmentValueMode";
	String HEALTH_BAR_HITPOINTS_PER_SEGMENT_KEY = "healthBarHitpointsPerSegment";
	String HEALTH_BAR_SEGMENT_COLOR_KEY = "healthBarSegmentColor";
	String HEALTH_BAR_SEGMENT_THICKNESS_KEY = "healthBarSegmentThickness";
	String HEALTH_BAR_BORDER_COLOR_KEY = "healthBarBorderColor";
	String HEALTH_BAR_BORDER_THICKNESS_KEY = "healthBarBorderThickness";
	String HEALTH_BAR_CORNER_RADIUS_KEY = "healthBarCornerRadius";
	String OVERHEAD_CHAT_ENABLED_KEY = "overheadChatEnabled";
	String SHOW_NPC_OVERHEAD_CHAT_KEY = "showNpcOverheadChat";
	String OVERHEAD_CHAT_FONT_KEY = "overheadChatFont";
	String OVERHEAD_CHAT_COLOR_KEY = "overheadChatColor";
	String OVERHEAD_CHAT_RELATIONSHIP_COLORS_KEY = "overheadChatRelationshipColors";
	String OVERHEAD_CHAT_FRIEND_COLOR_KEY = "overheadChatFriendColor";
	String OVERHEAD_CHAT_CLAN_COLOR_KEY = "overheadChatClanColor";
	String OVERHEAD_CHAT_GROUP_IRON_COLOR_KEY = "overheadChatGroupIronColor";
	String OVERHEAD_CHAT_EFFECT_KEY = "overheadChatEffect";
	String OVERHEAD_CHAT_SHADOW_KEY = "overheadChatShadow";
	String OVERHEAD_CHAT_SHADOW_COLOR_KEY = "overheadChatShadowColor";
	String OVERHEAD_CHAT_X_OFFSET_KEY = "overheadChatXOffset";
	String OVERHEAD_CHAT_Y_OFFSET_KEY = "overheadChatYOffset";
	String HEAD_ICONS_ENABLED_KEY = "headIconsEnabled";
	String SHOW_PRAYER_ICONS_KEY = "showPrayerIcons";
	String SHOW_PRAYER_ICON_BACKGROUND_KEY = "showPrayerIconBackground";
	String SHOW_SKULL_ICONS_KEY = "showSkullIcons";
	String SHOW_NPC_ICONS_KEY = "showNpcIcons";
	String SHOW_HINT_ARROWS_KEY = "showHintArrows";
	String HEAD_ICON_SCALE_PERCENT_KEY = "headIconScalePercent";
	String HEAD_ICON_SCALE_WITH_ZOOM_KEY = "headIconScaleWithZoom";
	String HEAD_ICON_X_OFFSET_KEY = "headIconXOffset";
	String HEAD_ICON_Y_OFFSET_KEY = "headIconYOffset";
	String HEAD_ICON_Z_OFFSET_KEY = "headIconZOffset";
	String HEAD_ICON_SPACING_KEY = "headIconSpacing";
	String SPRITE_SCALING_MODE_KEY = "spriteScalingMode";

	String GENERAL_SECTION = "general";

	String DISPLAY_SECTION = "display";

	String HEALTH_BARS_SECTION = "healthBars";

	String OVERHEAD_CHAT_SECTION = "overheadChat";

	String HEAD_ICONS_SECTION = "headIcons";


	@ConfigItem(
		hidden = true,
		keyName = DISABLE_ENEMY_HITSPLATS_KEY,
		name = "Hide NPC hits",
		description = "Do not draw replacement hitsplats for NPCs. ",
		section = DISPLAY_SECTION,
		position = 16
	)
	default boolean disableEnemyHitsplats()
	{
		return false;
	}

	@ConfigItem(
		hidden = true,
		keyName = DISABLE_ALLY_HITSPLATS_KEY,
		name = "Hide other players' hits",
		description = "Do not draw replacement hitsplats for other players. ",
		section = DISPLAY_SECTION,
		position = 17
	)
	default boolean disableAllyHitsplats()
	{
		return false;
	}

	@ConfigItem(
		keyName = DISABLE_MY_HITSPLATS_KEY,
		name = "Hide hitsplats",
		description = "Hide hitsplats on your player.",
		section = QUICK_SECTION,
		position = 3
	)
	default boolean disableMyHitsplats()
	{
		return false;
	}

	@ConfigItem(
		hidden = true,
		keyName = MAX_HITSPLATS_KEY,
		name = "Max hitsplats",
		description = "Maximum hitsplats kept per actor, up to 128.",
		section = DISPLAY_SECTION,
		position = 19
	)
	@Range(
		min = 0,
		max = 128
	)
	default int maxHitsplats()
	{
		return 16;
	}

	@ConfigItem(
		hidden = true,
		keyName = REUSE_OLD_HITSPLAT_SLOTS_KEY,
		name = "Reuse old slots",
		description = "Allow each new hitsplat to replace the oldest hitsplat from an earlier reuse window, even when an empty slot exists. Hits in the same window still use separate slots.",
		section = DISPLAY_SECTION,
		position = 20
	)
	default boolean reuseOldHitsplatSlots()
	{
		return false;
	}

	@ConfigItem(
		hidden = true,
		keyName = HITSPLAT_REUSE_INTERVAL_KEY,
		name = "Reuse interval",
		description = "Minimum age before a hitsplat slot can be reused. 550 ms is approximately one server tick with tolerance for timing differences.",
		section = DISPLAY_SECTION,
		position = 21
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 50,
		max = 60000
	)
	default int hitsplatReuseInterval()
	{
		return 550;
	}

	@ConfigItem(
		hidden = true,
		keyName = OPACITY_PERCENT_KEY,
		name = "Opacity",
		description = "Opacity of replacement hitsplats. 100 is fully opaque.",
		section = DISPLAY_SECTION,
		position = 22
	)
	@Range(
		min = 0,
		max = 100
	)
	@Units(Units.PERCENT)
	default int opacityPercent()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = ONLY_DISPLAY_MINE_KEY,
		name = "My hits only",
		description = "Only draw hits marked as yours by the game. Ownership is never inferred from XP.",
		section = DISPLAY_SECTION,
		position = 1
	)
	default boolean onlyDisplayMine()
	{
		return false;
	}

	@ConfigItem(
		hidden = true,
		keyName = PRIORITIZE_MINE_KEY,
		name = "Prioritize my hits",
		description = "When replacement hitsplats are limited, a new hit marked as yours replaces the oldest hit not marked as yours first.",
		section = DISPLAY_SECTION,
		position = 2
	)
	default boolean prioritizeMine()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = HIDE_ZERO_HITSPLATS_KEY,
		name = "Hide zero hits",
		description = "Do not draw misses or other zero-amount replacement hitsplats.",
		section = DISPLAY_SECTION,
		position = 3
	)
	default boolean hideZeroHitsplats()
	{
		return false;
	}

	@ConfigItem(
		hidden = true,
		keyName = LAYOUT_SHAPE_KEY,
		name = "Shape",
		description = "Hexagonal uses six-sided rings. Diamond uses four-corner rings. Grid uses square rings. X uses diagonal arms.",
		section = DISPLAY_SECTION,
		position = 4
	)
	default OverheadsLayoutShape layoutShape()
	{
		return OverheadsLayoutShape.HEXAGONAL;
	}

	@ConfigItem(
		hidden = true,
		keyName = LAYOUT_DIRECTION_KEY,
		name = "Direction",
		description = "Clockwise starts at top and moves right. Counterclockwise starts at top and moves left.",
		section = DISPLAY_SECTION,
		position = 5
	)
	default OverheadsLayoutDirection layoutDirection()
	{
		return OverheadsLayoutDirection.CLOCKWISE;
	}

	@ConfigItem(
		hidden = true,
		keyName = LAYOUT_BEHAVIOR_KEY,
		name = "Behavior",
		description = "Incremental fills slots in order. Symmetrical alternates opposite slots. Random chooses an open slot from the current radius.",
		section = DISPLAY_SECTION,
		position = 6
	)
	default OverheadsLayoutBehavior layoutBehavior()
	{
		return OverheadsLayoutBehavior.SYMMETRIC;
	}

	@ConfigItem(
		hidden = true,
		keyName = MIN_RADIUS_KEY,
		name = "Min radius",
		description = "First layout radius for detected boss-like targets. Smaller targets always start at the center. 0 starts at the center; 1 starts on the first ring.",
		section = DISPLAY_SECTION,
		position = 9
	)
	@Range(
		min = 0,
		max = 64
	)
	default int minRadius()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = MAX_RADIUS_KEY,
		name = "Max radius",
		description = "Maximum layout radius for detected boss-like targets. Smaller targets are uncapped. 0 means no cap; 1 means center only; 2 means center plus the first ring.",
		section = DISPLAY_SECTION,
		position = 10
	)
	@Range(
		min = 0,
		max = 64
	)
	default int maxRadius()
	{
		return 2;
	}

	@ConfigItem(
		hidden = true,
		keyName = X_SPACING_KEY,
		name = "X spacing",
		description = "Extra horizontal pixels between adjacent hitsplat slots. Negative values pull slots closer together.",
		section = DISPLAY_SECTION,
		position = 11
	)
	@Range(
		min = -64,
		max = 64
	)
	@Units(Units.PIXELS)
	default int xSpacing()
	{
		return -2;
	}

	@ConfigItem(
		hidden = true,
		keyName = Y_SPACING_KEY,
		name = "Y spacing",
		description = "Extra vertical pixels between adjacent hitsplat slots. Negative values pull slots closer together.",
		section = DISPLAY_SECTION,
		position = 12
	)
	@Range(
		min = -64,
		max = 64
	)
	@Units(Units.PIXELS)
	default int ySpacing()
	{
		return 2;
	}

	@ConfigItem(
		hidden = true,
		keyName = HITSPLAT_FONT_KEY,
		name = "Font",
		description = "Font used for hitsplat amounts. The font picker includes RuneLite, custom, and system fonts.",
		section = DISPLAY_SECTION,
		position = 7
	)
	default FontType hitsplatFont()
	{
		return FontType.SMALL;
	}

	@ConfigItem(
		hidden = true,
		keyName = HITSPLAT_SCALE_PERCENT_KEY,
		name = "Scale",
		description = "Scale the complete replacement hitsplat, including sprites, text, spacing, and layout geometry.",
		section = DISPLAY_SECTION,
		position = 8
	)
	@Units(Units.PERCENT)
	@Range(
		min = 50,
		max = 200
	)
	default int hitsplatScalePercent()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = LARGE_TARGET_DETECTION_KEY,
		name = "Boss-like detection",
		description = "Decide which actors receive configured radius limits and target offsets. RuneLite does not expose a reliable boss flag, so Either combines footprint and public health scale.",
		section = DISPLAY_SECTION,
		position = 23
	)
	default OverheadsTargetDetection largeTargetDetection()
	{
		return OverheadsTargetDetection.EITHER;
	}

	@ConfigItem(
		hidden = true,
		keyName = LARGE_TARGET_SIZE_KEY,
		name = "Footprint threshold",
		description = "Minimum actor footprint used by boss-like detection. 2 means targets at least 2x2 tiles.",
		section = DISPLAY_SECTION,
		position = 24
	)
	@Range(
		min = 1,
		max = 16
	)
	default int largeTargetSize()
	{
		return 2;
	}

	@ConfigItem(
		hidden = true,
		keyName = LARGE_TARGET_HEALTH_SCALE_KEY,
		name = "Boss health-scale threshold",
		description = "Minimum public actor health scale used by boss-like detection. This is not actual maximum HP; standard actors commonly use 30.",
		section = DISPLAY_SECTION,
		position = 25
	)
	@Range(
		min = 100,
		max = 255
	)
	default int largeTargetHealthScale()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = LARGE_TARGET_X_OFFSET_KEY,
		name = "Boss-like X offset",
		description = "Extra horizontal hitsplat offset for detected boss-like targets.",
		section = DISPLAY_SECTION,
		position = 26
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int largeTargetXOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = LARGE_TARGET_Y_OFFSET_KEY,
		name = "Boss-like Y offset",
		description = "Extra vertical hitsplat offset for detected boss-like targets. Positive values move hitsplats upward.",
		section = DISPLAY_SECTION,
		position = 27
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int largeTargetYOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = FADE_IN_DURATION_KEY,
		name = "Fade-in duration",
		description = "How long replacement hitsplats take to appear. Total lifetime is fade-in + full opacity + fade-out.",
		section = DISPLAY_SECTION,
		position = 13
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 0,
		max = 60000
	)
	default int fadeInDuration()
	{
		return 30;
	}

	@ConfigItem(
		hidden = true,
		keyName = FULL_OPACITY_DURATION_KEY,
		name = "Full opacity duration",
		description = "How long replacement hitsplats stay fully visible after fading in.",
		section = DISPLAY_SECTION,
		position = 14
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 0,
		max = 60000
	)
	default int fullOpacityDuration()
	{
		return 400;
	}

	@ConfigItem(
		hidden = true,
		keyName = FADE_OUT_DURATION_KEY,
		name = "Fade-out duration",
		description = "How long replacement hitsplats take to disappear after the full-opacity duration.",
		section = DISPLAY_SECTION,
		position = 15
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 0,
		max = 60000
	)
	default int fadeOutDuration()
	{
		return 150;
	}

	@ConfigItem(
		hidden = true,
		keyName = SPRITE_SCALING_MODE_KEY,
		name = "Sprite scaling",
		description = "Resize hitsplats, RuneScape health bars, and head icons with Nearest, Bilinear, Bicubic, or edge-directed xBR. xBR is used for enlarging sprites and smoothly reduced to fractional target sizes.",
		section = GENERAL_SECTION,
		position = 0
	)
	default OverheadsSpriteScalingMode spriteScalingMode()
	{
		return OverheadsSpriteScalingMode.XBR;
	}

	@ConfigItem(
		keyName = HEALTH_BARS_ENABLED_KEY,
		name = "Show health",
		description = "Show the configured HP display for your player.",
		section = QUICK_SECTION,
		position = 1
	)
	default boolean healthBarsEnabled()
	{
		return true;
	}


	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_STYLE_KEY,
		name = "Style",
		description = "OSRS artwork or modern styled bars.",
		section = HEALTH_BARS_SECTION,
		position = 1
	)
	default OverheadsHealthBarStyle healthBarStyle()
	{
		return OverheadsHealthBarStyle.SOLID;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SCALE_MODE_KEY,
		name = "Scale mode",
		description = "Controls boss sizing. Fixed uses Scale for both dimensions; Threshold switches to the boss width and height scales at the configured threshold; Dynamic interpolates each dimension independently. Public health scale is not actual maximum HP.",
		section = HEALTH_BARS_SECTION,
		position = 2
	)
	default OverheadsHealthScaleMode healthBarScaleMode()
	{
		return OverheadsHealthScaleMode.THRESHOLD;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SCALE_PERCENT_KEY,
		name = "Scale",
		description = "Fixed health-bar size multiplier, or the starting multiplier used by Threshold and Dynamic modes.",
		section = HEALTH_BARS_SECTION,
		position = 3
	)
	@Units(Units.PERCENT)
	@Range(
		min = 50,
		max = 200
	)
	default int healthBarScalePercent()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SCALE_THRESHOLD_KEY,
		name = "Boss scale threshold",
		description = "Public actor health scale that activates the boss width and height scales in Threshold mode or reaches them in Dynamic mode. This is not actual maximum HP.",
		section = HEALTH_BARS_SECTION,
		position = 4
	)
	@Range(
		min = 100,
		max = 255
	)
	default int healthBarScaleThreshold()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_LARGE_SCALE_PERCENT_KEY,
		name = "Boss width scale",
		description = "Health-bar width multiplier used at or above the configured public health scale threshold.",
		section = HEALTH_BARS_SECTION,
		position = 5
	)
	@Units(Units.PERCENT)
	@Range(
		min = 50,
		max = 200
	)
	default int healthBarLargeScalePercent()
	{
		return 150;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_LARGE_HEIGHT_SCALE_PERCENT_KEY,
		name = "Boss height scale",
		description = "Health-bar height multiplier used at or above the configured public health scale threshold.",
		section = HEALTH_BARS_SECTION,
		position = 6
	)
	@Units(Units.PERCENT)
	@Range(
		min = 50,
		max = 200
	)
	default int healthBarLargeHeightScalePercent()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SOLID_WIDTH_KEY,
		name = "Width",
		description = "Width of the modern health bar in pixels. ",
		section = HEALTH_BARS_SECTION,
		position = 7
	)
	@Range(
		min = 10,
		max = 200
	)
	default double healthBarSolidWidth()
	{
		return 50.0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_HEIGHT_KEY,
		name = "Height",
		description = "Health-bar height in pixels. ",
		section = HEALTH_BARS_SECTION,
		position = 8
	)
	@Range(
		min = 2,
		max = 20
	)
	default double healthBarHeight()
	{
		return 5.0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_X_OFFSET_KEY,
		name = "X offset",
		description = "Horizontal health-bar offset in pixels. Adjacent spinner changes advance by 2 pixels while the plugin is enabled.",
		section = HEALTH_BARS_SECTION,
		position = 9
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int healthBarXOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_Y_OFFSET_KEY,
		name = "Y offset",
		description = "Vertical health-bar offset in pixels. Positive values move it upward.",
		section = HEALTH_BARS_SECTION,
		position = 10
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int healthBarYOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_FILL_DIRECTION_KEY,
		name = "Fill direction",
		description = "Direction in which the filled portion of a modern health bar grows.",
		section = HEALTH_BARS_SECTION,
		position = 11
	)
	default OverheadsHealthBarFillDirection healthBarFillDirection()
	{
		return OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
	}


	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_FRONT_GRADIENT_COORDINATES_KEY,
		name = "Front coordinates",
		description = "Relative restarts the front horizontal or vertical blend inside filled health. Absolute anchors it to the full bar so its colors stay fixed as health changes.",
		section = HEALTH_BARS_SECTION,
		position = 12
	)
	default OverheadsHealthBarGradientCoordinates healthBarFrontGradientCoordinates()
	{
		return OverheadsHealthBarGradientCoordinates.FULL_BAR;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_FRONT_GRADIENT_KEY,
		name = "Front gradient",
		description = "Solid uses Front color. Horizontal and Vertical blend toward Front second color. Health based blends from the second color at empty health to the primary color at full health.",
		section = HEALTH_BARS_SECTION,
		position = 13
	)
	default OverheadsHealthBarGradient healthBarFrontGradient()
	{
		return OverheadsHealthBarGradient.HORIZONTAL;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_FRONT_COLOR_KEY,
		name = "Front color",
		description = "Primary RGBA color for the filled portion of a modern health bar.",
		section = HEALTH_BARS_SECTION,
		position = 14
	)
	@Alpha
	default Color healthBarFrontColor()
	{
		return new Color(45, 190, 88);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_FRONT_SECONDARY_COLOR_KEY,
		name = "Front second color",
		description = "Secondary RGBA color used by non-solid front gradients.",
		section = HEALTH_BARS_SECTION,
		position = 15
	)
	@Alpha
	default Color healthBarFrontSecondaryColor()
	{
		return new Color(34, 158, 72);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_POISONED_FRONT_COLOR_KEY,
		name = "Poisoned front color",
		description = "Primary RGBA front color used for your local player's Modern health bar while poisoned or envenomed.",
		section = HEALTH_BARS_SECTION,
		position = 16
	)
	@Alpha
	default Color healthBarPoisonedFrontColor()
	{
		return new Color(132, 204, 66);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BACK_GRADIENT_COORDINATES_KEY,
		name = "Back coordinates",
		description = "Relative restarts the back horizontal or vertical blend inside missing health. Absolute anchors it to the full bar so its colors stay fixed as health changes.",
		section = HEALTH_BARS_SECTION,
		position = 17
	)
	default OverheadsHealthBarGradientCoordinates healthBarBackGradientCoordinates()
	{
		return OverheadsHealthBarGradientCoordinates.FULL_BAR;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BACK_GRADIENT_KEY,
		name = "Back gradient",
		description = "Solid uses Back color. Horizontal and Vertical blend toward Back second color. Health based blends from the second color at empty health to the primary color at full health.",
		section = HEALTH_BARS_SECTION,
		position = 18
	)
	default OverheadsHealthBarGradient healthBarBackGradient()
	{
		return OverheadsHealthBarGradient.HORIZONTAL;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BACK_COLOR_KEY,
		name = "Back color",
		description = "Primary RGBA color for the missing-health portion of a modern health bar.",
		section = HEALTH_BARS_SECTION,
		position = 19
	)
	@Alpha
	default Color healthBarBackColor()
	{
		return new Color(48, 53, 64);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BACK_SECONDARY_COLOR_KEY,
		name = "Back second color",
		description = "Secondary RGBA color used by non-solid back gradients.",
		section = HEALTH_BARS_SECTION,
		position = 20
	)
	@Alpha
	default Color healthBarBackSecondaryColor()
	{
		return new Color(35, 40, 49);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_DAMAGE_TRAIL_ENABLED_KEY,
		name = "Damage trail",
		description = "Hold recently lost health, then animate the trail down to the current Modern health-bar value. Healing updates immediately.",
		section = HEALTH_BARS_SECTION,
		position = 21
	)
	default boolean healthBarDamageTrailEnabled()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_DAMAGE_TRAIL_COLOR_KEY,
		name = "Damage trail color",
		description = "RGBA color for recently lost health in a Modern health bar.",
		section = HEALTH_BARS_SECTION,
		position = 22
	)
	@Alpha
	default Color healthBarDamageTrailColor()
	{
		return new Color(0xFFFF001F, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_DAMAGE_TRAIL_HOLD_KEY,
		name = "Damage trail hold",
		description = "How long recently lost health remains stationary before draining.",
		section = HEALTH_BARS_SECTION,
		position = 23
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 0,
		max = 5000
	)
	default int healthBarDamageTrailHold()
	{
		return 445;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_DAMAGE_TRAIL_DRAIN_KEY,
		name = "Damage trail drain",
		description = "How long the held damage trail takes to animate down to current health.",
		section = HEALTH_BARS_SECTION,
		position = 24
	)
	@Units(Units.MILLISECONDS)
	@Range(
		min = 0,
		max = 5000
	)
	default int healthBarDamageTrailDrain()
	{
		return 245;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SEGMENTS_ENABLED_KEY,
		name = "HP segments",
		description = "Draw League-style dividers every configured hitpoints. Exact values are available for your player and NPCs covered by RuneLite's NPC stats; the selected mode controls unknown actors.",
		section = HEALTH_BARS_SECTION,
		position = 25
	)
	default boolean healthBarSegmentsEnabled()
	{
		return false;
	}


	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SEGMENT_VALUE_MODE_KEY,
		name = "Segment HP source",
		description = "Exact HP uses your real Hitpoints level or RuneLite NPC stats. Public fallback keeps segments visible for actors whose maximum HP is unknown, but those fallback units are normalized public health-scale units rather than hitpoints.",
		section = HEALTH_BARS_SECTION,
		position = 26
	)
	default OverheadsHealthBarSegmentValueMode healthBarSegmentValueMode()
	{
		return OverheadsHealthBarSegmentValueMode.EXACT_HP_WITH_PUBLIC_FALLBACK;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_HITPOINTS_PER_SEGMENT_KEY,
		name = "HP per segment",
		description = "Draw one divider for each multiple of this many hitpoints. In Public scale mode or when using its fallback, the same number is measured in normalized public health-scale units.",
		section = HEALTH_BARS_SECTION,
		position = 27
	)
	@Range(
		min = 1,
		max = 10000
	)
	default int healthBarHitpointsPerSegment()
	{
		return 10;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SEGMENT_COLOR_KEY,
		name = "Segment color",
		description = "RGBA color for health-segment dividers.",
		section = HEALTH_BARS_SECTION,
		position = 28
	)
	@Alpha
	default Color healthBarSegmentColor()
	{
		return new Color(0x3F000000, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_SEGMENT_THICKNESS_KEY,
		name = "Segment thickness",
		description = "Thickness of Modern health-bar segment dividers in pixels. Decimal values allow sub-pixel lines.",
		section = HEALTH_BARS_SECTION,
		position = 29
	)
	@Range(
		min = 0,
		max = 10
	)
	default double healthBarSegmentThickness()
	{
		return 0.6;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BORDER_COLOR_KEY,
		name = "Border color",
		description = "RGBA color for the border around a modern health bar.",
		section = HEALTH_BARS_SECTION,
		position = 30
	)
	@Alpha
	default Color healthBarBorderColor()
	{
		return new Color(25, 25, 25, 230);
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_BORDER_THICKNESS_KEY,
		name = "Border thickness",
		description = "Modern health-bar border thickness in pixels. Decimal values allow sub-pixel borders; set to 0 for no border.",
		section = HEALTH_BARS_SECTION,
		position = 31
	)
	@Range(
		min = 0,
		max = 10
	)
	default double healthBarBorderThickness()
	{
		return 1.0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEALTH_BAR_CORNER_RADIUS_KEY,
		name = "Corner radius",
		description = "Antialiased Modern health-bar corner radius in pixels, limited by half the bar's smaller dimension. Decimal values allow sub-pixel curves.",
		section = HEALTH_BARS_SECTION,
		position = 32
	)
	@Range(
		min = 0,
		max = 50
	)
	default double healthBarCornerRadius()
	{
		return 3.0;
	}

	@ConfigItem(
		keyName = OVERHEAD_CHAT_ENABLED_KEY,
		name = "Show overhead chat",
		description = "Show overhead chat for your player. Filtered messages remain hidden.",
		section = QUICK_SECTION,
		position = 0
	)
	default boolean overheadChatEnabled()
	{
		return true;
	}


	@ConfigItem(
		hidden = true,
		keyName = SHOW_NPC_OVERHEAD_CHAT_KEY,
		name = "NPC overhead text",
		description = "Draw replacement overhead text for NPCs. Player overhead chat remains controlled by Show overhead chat.",
		section = OVERHEAD_CHAT_SECTION,
		position = 1
	)
	default boolean showNpcOverheadChat()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_FONT_KEY,
		name = "Font",
		description = "Font used for overhead chat. The font picker includes RuneLite, custom, and system fonts.",
		section = OVERHEAD_CHAT_SECTION,
		position = 2
	)
	default FontType overheadChatFont()
	{
		return FontType.BOLD;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_COLOR_KEY,
		name = "Color",
		description = "Fallback RGBA color for replacement overhead chat. Recognized local color prefixes override RGB per message or glyph while retaining this alpha.",
		section = OVERHEAD_CHAT_SECTION,
		position = 3
	)
	@Alpha
	default Color overheadChatColor()
	{
		return new Color(0xFF, 0xFF, 0x3F, 0xFF);
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_RELATIONSHIP_COLORS_KEY,
		name = "Relationship colors",
		description = "Use separate default overhead-chat colors for group iron teammates, friends, and clan members. Your own typed color prefixes still take priority.",
		section = OVERHEAD_CHAT_SECTION,
		position = 4
	)
	default boolean overheadChatRelationshipColors()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_FRIEND_COLOR_KEY,
		name = "Friend color",
		description = "Default RGBA overhead-chat color for players on your friends list when relationship colors are enabled.",
		section = OVERHEAD_CHAT_SECTION,
		position = 5
	)
	@Alpha
	default Color overheadChatFriendColor()
	{
		return new Color(0xFFA5FF40, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_CLAN_COLOR_KEY,
		name = "Clan color",
		description = "Default RGBA overhead-chat color for members of your clan when relationship colors are enabled.",
		section = OVERHEAD_CHAT_SECTION,
		position = 6
	)
	@Alpha
	default Color overheadChatClanColor()
	{
		return new Color(0xFF40CFFF, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_GROUP_IRON_COLOR_KEY,
		name = "Group iron color",
		description = "Default RGBA overhead-chat color for members of your group iron team when relationship colors are enabled.",
		section = OVERHEAD_CHAT_SECTION,
		position = 7
	)
	@Alpha
	default Color overheadChatGroupIronColor()
	{
		return new Color(0xFFFF4040, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_EFFECT_KEY,
		name = "Default Text Effect",
		description = "Fallback animation for replacement overhead chat. Static draws unanimated text. A matching effect prefix typed by you overrides it for that local message; other senders' native effects cannot be inherited.",
		section = OVERHEAD_CHAT_SECTION,
		position = 8
	)
	default OverheadsOverheadChatEffect overheadChatEffect()
	{
		return OverheadsOverheadChatEffect.STATIC;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_SHADOW_KEY,
		name = "Text shadow",
		description = "Draw a configurable shadow behind replacement overhead chat.",
		section = OVERHEAD_CHAT_SECTION,
		position = 9
	)
	default boolean overheadChatShadow()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_SHADOW_COLOR_KEY,
		name = "Shadow color",
		description = "RGBA color used for the replacement overhead-chat shadow.",
		section = OVERHEAD_CHAT_SECTION,
		position = 10
	)
	@Alpha
	default Color overheadChatShadowColor()
	{
		return new Color(0x80171717, true);
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_X_OFFSET_KEY,
		name = "X offset",
		description = "Horizontal overhead-chat offset in whole pixels.",
		section = OVERHEAD_CHAT_SECTION,
		position = 11
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int overheadChatXOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = OVERHEAD_CHAT_Y_OFFSET_KEY,
		name = "Y offset",
		description = "Vertical overhead-chat offset in whole pixels. Positive values move it upward.",
		section = OVERHEAD_CHAT_SECTION,
		position = 12
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int overheadChatYOffset()
	{
		return 0;
	}

	@ConfigItem(
		keyName = HEAD_ICONS_ENABLED_KEY,
		name = "Show head icons",
		description = "Show the configured prayer, skull and hint icons for your player.",
		section = QUICK_SECTION,
		position = 2
	)
	default boolean headIconsEnabled()
	{
		return true;
	}


	@ConfigItem(
		hidden = true,
		keyName = SHOW_PRAYER_ICONS_KEY,
		name = "Prayer icons",
		description = "Show player overhead-prayer icons.",
		section = HEAD_ICONS_SECTION,
		position = 1
	)
	default boolean showPrayerIcons()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = SHOW_PRAYER_ICON_BACKGROUND_KEY,
		name = "Prayer background",
		description = "Draw the background behind supported Resource Pack overhead-prayer textures. Native prayer icons already include their background.",
		section = HEAD_ICONS_SECTION,
		position = 2
	)
	default boolean showPrayerIconBackground()
	{
		return true;
	}

    @ConfigItem(hidden = true, keyName = "prayerIconSymbolsOnly", name = "Prayer symbols only",
        description = "Remove original overhead backings and added row/corner badge backgrounds, preserving the symbols and resource-pack artwork. Off restores your saved backgrounds.",
        section = HEAD_ICONS_SECTION, position = 987)
    default boolean prayerIconSymbolsOnly() { return false; }

    @ConfigItem(hidden = true, keyName = "prayerIconNativeSymbolsOnly", name = "Remove native prayer background",
        description = "Remove only the original overhead backing, preserving the prayer symbol and your custom row and corner-badge backgrounds.",
        section = HEAD_ICONS_SECTION, position = 988)
    default boolean prayerIconNativeSymbolsOnly() { return false; }

	@ConfigItem(
		hidden = true,
		keyName = SHOW_SKULL_ICONS_KEY,
		name = "Skull icons",
		description = "Show player skull icons.",
		section = HEAD_ICONS_SECTION,
		position = 3
	)
	default boolean showSkullIcons()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = SHOW_NPC_ICONS_KEY,
		name = "NPC icons",
		description = "Show NPC overhead icons.",
		section = HEAD_ICONS_SECTION,
		position = 4
	)
	default boolean showNpcIcons()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = SHOW_HINT_ARROWS_KEY,
		name = "Hint arrows",
		description = "Show actor hint arrows.",
		section = HEAD_ICONS_SECTION,
		position = 5
	)
	default boolean showHintArrows()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_SCALE_PERCENT_KEY,
		name = "Scale",
		description = "Base head-icon scale as a percentage. Zoom scaling is applied on top when enabled.",
		section = HEAD_ICONS_SECTION,
		position = 6
	)
	@Units(Units.PERCENT)
	@Range(
		min = 50,
		max = 200
	)
	default int headIconScalePercent()
	{
		return 100;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_SCALE_WITH_ZOOM_KEY,
		name = "Scale with zoom",
		description = "Adjust head-icon size with camera zoom so icons stay proportional to the game world.",
		section = HEAD_ICONS_SECTION,
		position = 7
	)
	default boolean headIconScaleWithZoom()
	{
		return true;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_X_OFFSET_KEY,
		name = "X offset",
		description = "Horizontal head-icon offset in pixels.",
		section = HEAD_ICONS_SECTION,
		position = 8
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int headIconXOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_Y_OFFSET_KEY,
		name = "Y offset",
		description = "Vertical head-icon offset in pixels. Positive values move icons upward.",
		section = HEAD_ICONS_SECTION,
		position = 9
	)
	@Range(
		min = -256,
		max = 256
	)
	@Units(Units.PIXELS)
	default int headIconYOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_Z_OFFSET_KEY,
		name = "Z offset",
		description = "Head-icon height offset in world units, following camera zoom and angle. Positive values raise icons. Applied in addition to X and Y pixel offsets.",
		section = HEAD_ICONS_SECTION,
		position = 10
	)
	@Range(
		min = -1024,
		max = 1024
	)
	default int headIconZOffset()
	{
		return 0;
	}

	@ConfigItem(
		hidden = true,
		keyName = HEAD_ICON_SPACING_KEY,
		name = "Spacing",
		description = "Gap below the first head icon and between each stacked icon, in pixels.",
		section = HEAD_ICONS_SECTION,
		position = 11
	)
	@Range(
		min = 0,
		max = 20
	)
	@Units(Units.PIXELS)
	default int headIconSpacing()
	{
		return 2;
	}

    @ConfigItem(
		hidden = true,
        keyName = "alwaysShowSelfHealth",
        name = "Always show my HP",
        description = "Show your own current HP outside combat too.",
        section = HEALTH_BARS_SECTION,
        position = 100
    )
    default boolean alwaysShowSelfHealth()
    {
        return false;
    }

    @ConfigItem(
		hidden = true,
        keyName = "selfHealthDisplay",
        name = "My HP display",
        description = "My HP display.",
        section = HEALTH_BARS_SECTION,
        position = 101
    )
    default OverheadsBarDisplay selfHealthDisplay()
    {
        return OverheadsBarDisplay.BOTH;
    }

    @ConfigItem(
		hidden = true,
        keyName = "npcHealthDisplay",
        name = "NPC HP display",
        description = "NPC numbers show the visible HP percentage.",
        section = HEALTH_BARS_SECTION,
        position = 102
    )
    default OverheadsBarDisplay npcHealthDisplay()
    {
        return OverheadsBarDisplay.BAR_ONLY;
    }

    @ConfigItem(
		hidden = true,
        keyName = "otherHealthDisplay",
        name = "Other player HP display",
        description = "Other player numbers use the visible HP percentage.",
        section = HEALTH_BARS_SECTION,
        position = 103
    )
    default OverheadsBarDisplay otherHealthDisplay()
    {
        return OverheadsBarDisplay.BAR_ONLY;
    }

    @ConfigItem(
		hidden = true,
        keyName = "healthPosition",
        name = "HP position",
        description = "Native keeps HP beneath overhead icons. Other positions use custom overhead spacing.",
        section = HEALTH_BARS_SECTION,
        position = 104
    )
    default OverheadsHealthPosition healthPosition()
    {
        return OverheadsHealthPosition.NATIVE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "healthOrientation",
        name = "HP orientation",
        description = "HP orientation.",
        section = HEALTH_BARS_SECTION,
        position = 105
    )
    default OverheadsOrientation healthOrientation()
    {
        return OverheadsOrientation.HORIZONTAL;
    }

    @ConfigItem(
		hidden = true,
        keyName = "healthScaleWithZoom",
        name = "Scale HP with zoom",
        description = "Scale HP with zoom.",
        section = HEALTH_BARS_SECTION,
        position = 106
    )
    default boolean healthScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(
		hidden = true,
        keyName = "playerHealthWidth",
        name = "Player bar length",
        description = "Player bar length.",
        section = HEALTH_BARS_SECTION,
        position = 107
    )
    @Range(min = 10, max = 200)
    default int playerHealthWidth()
    {
        return 50;
    }

    @ConfigItem(
		hidden = true,
        keyName = "playerHealthHeight",
        name = "Player bar thickness",
        description = "Player bar thickness.",
        section = HEALTH_BARS_SECTION,
        position = 108
    )
    @Range(min = 2, max = 30)
    default int playerHealthHeight()
    {
        return 12;
    }

    @ConfigItem(
		hidden = true,
        keyName = "healthBarOpacity",
        name = "HP bar opacity",
        description = "Opacity of the bar and border; numbers have their own color.",
        section = HEALTH_BARS_SECTION,
        position = 109
    )
    @Range(min = 0, max = 100)
    default int healthBarOpacity()
    {
        return 100;
    }

    @ConfigItem(
		hidden = true,
        keyName = "healthTextFont",
        name = "HP number font",
        description = "HP number font.",
        section = HEALTH_BARS_SECTION,
        position = 110
    )
    default FontType healthTextFont()
    {
        return FontType.SMALL;
    }

    @ConfigItem(hidden = true, keyName = "healthTextPosition", name = "HP number position",
        description = "Place HP numbers on, above or below the health bar. Numbers-only displays keep their anchor.",
        section = DISPLAY_SECTION, position = 112)
    default OverheadsBarTextPosition healthTextPosition() { return OverheadsBarTextPosition.ON_BAR; }

    @ConfigItem(
		hidden = true,
        keyName = "healthTextColor",
        name = "HP number color",
        description = "HP number color.",
        section = HEALTH_BARS_SECTION,
        position = 111
    )
    @Alpha
    default Color healthTextColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerVisibility",
        name = "Prayer display",
        description = "Prayer display.",
        section = PRAYER_SECTION,
        position = 112
    )
    default OverheadsPrayerVisibility prayerVisibility()
    {
        return OverheadsPrayerVisibility.NEVER;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerDisplay",
        name = "Prayer numbers / bar",
        description = "Prayer numbers / bar.",
        section = PRAYER_SECTION,
        position = 113
    )
    default OverheadsBarDisplay prayerDisplay()
    {
        return OverheadsBarDisplay.BOTH;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerPosition",
        name = "Prayer position",
        description = "Prayer position.",
        section = PRAYER_SECTION,
        position = 114
    )
    default OverheadsPosition prayerPosition()
    {
        return OverheadsPosition.ABOVE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerOrientation",
        name = "Prayer orientation",
        description = "Prayer orientation.",
        section = PRAYER_SECTION,
        position = 115
    )
    default OverheadsOrientation prayerOrientation()
    {
        return OverheadsOrientation.HORIZONTAL;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerScaleWithZoom",
        name = "Scale prayer with zoom",
        description = "Scale prayer with zoom.",
        section = PRAYER_SECTION,
        position = 116
    )
    default boolean prayerScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerWidth",
        name = "Prayer bar length",
        description = "Prayer bar length.",
        section = PRAYER_SECTION,
        position = 117
    )
    @Range(min = 10, max = 200)
    default int prayerWidth()
    {
        return 50;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerHeight",
        name = "Prayer bar thickness",
        description = "Prayer bar thickness.",
        section = PRAYER_SECTION,
        position = 118
    )
    @Range(min = 2, max = 30)
    default int prayerHeight()
    {
        return 10;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerXOffset",
        name = "Prayer X offset",
        description = "Prayer X offset.",
        section = PRAYER_SECTION,
        position = 119
    )
    @Range(min = -300, max = 300)
    default int prayerXOffset()
    {
        return 0;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerYOffset",
        name = "Prayer Y offset",
        description = "Prayer Y offset.",
        section = PRAYER_SECTION,
        position = 120
    )
    @Range(min = -300, max = 300)
    default int prayerYOffset()
    {
        return 0;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerOpacity",
        name = "Prayer bar opacity",
        description = "Opacity of the prayer bar and border; numbers have their own color.",
        section = PRAYER_SECTION,
        position = 121
    )
    @Range(min = 0, max = 100)
    default int prayerOpacity()
    {
        return 100;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerColor",
        name = "Prayer fill",
        description = "Prayer fill.",
        section = PRAYER_SECTION,
        position = 122
    )
    @Alpha
    default Color prayerColor()
    {
        return new Color(58, 180, 210);
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerTextColor",
        name = "Prayer number color",
        description = "Prayer number color.",
        section = PRAYER_SECTION,
        position = 123
    )
    @Alpha
    default Color prayerTextColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerTextFont",
        name = "Prayer number font",
        description = "Prayer number font.",
        section = PRAYER_SECTION,
        position = 124
    )
    default FontType prayerTextFont()
    {
        return FontType.SMALL;
    }

    @ConfigItem(
		hidden = true,
        keyName = "chatPosition",
        name = "Chat position",
        description = "Chat position.",
        section = OVERHEAD_CHAT_SECTION,
        position = 125
    )
    default OverheadsPosition chatPosition()
    {
        return OverheadsPosition.ABOVE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "chatScaleWithZoom",
        name = "Scale chat with zoom",
        description = "Scale chat with zoom.",
        section = OVERHEAD_CHAT_SECTION,
        position = 126
    )
    default boolean chatScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(
		hidden = true,
        keyName = "hitsplatPosition",
        name = "Hitsplat position",
        description = "Native anchors hitsplats on the target. Other positions place the group outside the target.",
        section = DISPLAY_SECTION,
        position = 127
    )
    default OverheadsHitsplatPosition hitsplatPosition()
    {
        return OverheadsHitsplatPosition.NATIVE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "hitsplatScaleWithZoom",
        name = "Scale hitsplat with zoom",
        description = "Scale hitsplat with zoom.",
        section = DISPLAY_SECTION,
        position = 128
    )
    default boolean hitsplatScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(
		hidden = true,
        keyName = "iconsPosition",
        name = "Icon position",
        description = "Icon position.",
        section = HEAD_ICONS_SECTION,
        position = 129
    )
    default OverheadsPosition iconsPosition()
    {
        return OverheadsPosition.ABOVE;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerIconShape",
        name = "Prayer icon frame",
        description = "Prayer icon frame.",
        section = HEAD_ICONS_SECTION,
        position = 130
    )
    default OverheadsIconShape prayerIconShape()
    {
        return OverheadsIconShape.ORIGINAL;
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerIconBackground",
        name = "Prayer icon background",
        description = "Prayer icon background.",
        section = HEAD_ICONS_SECTION,
        position = 131
    )
    @Alpha
    default Color prayerIconBackground()
    {
        return new Color(35, 40, 46, 210);
    }

    @ConfigItem(hidden = true, keyName = "prayerIconBackgroundTracksPoints", name = "Prayer background follows points",
        description = "Drain and refill the Round or Square prayer icon background using your current prayer points or available shared party prayer. Unknown values keep the normal background.",
        section = HEAD_ICONS_SECTION, position = 134)
    default boolean prayerIconBackgroundTracksPoints() { return false; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconEmptyBackground", name = "Empty prayer background",
        description = "Color behind the empty portion when the prayer icon background follows prayer points.",
        section = HEAD_ICONS_SECTION, position = 135)
    default Color prayerIconEmptyBackground() { return new Color(12, 15, 18, 150); }

    @ConfigItem(
		hidden = true,
        keyName = "prayerIconBorder",
        name = "Prayer icon border",
        description = "Prayer icon border.",
        section = HEAD_ICONS_SECTION,
        position = 132
    )
    @Alpha
    default Color prayerIconBorder()
    {
        return new Color(170, 180, 190);
    }

    @ConfigItem(
		hidden = true,
        keyName = "prayerIconScalePercent",
        name = "Prayer icon size",
        description = "Prayer icon size.",
        section = HEAD_ICONS_SECTION,
        position = 133
    )
    @Range(min = 25, max = 300)
    default int prayerIconScalePercent()
    {
        return 100;
    }

    @ConfigItem(hidden = true, keyName = "healthNumberFormat", name = "HP number format",
        description = "Format for your own HP; other actors use visible percentages.", section = HEALTH_BARS_SECTION, position = 200)
    default OverheadsNumberFormat healthNumberFormat()
    {
        return OverheadsNumberFormat.CURRENT;
    }

    @ConfigItem(hidden = true, keyName = "prayerNumberFormat", name = "Prayer number format",
        description = "Format for your prayer points.", section = PRAYER_SECTION, position = 201)
    default OverheadsNumberFormat prayerNumberFormat()
    {
        return OverheadsNumberFormat.CURRENT;
    }

    @ConfigItem(hidden = true, keyName = "prayerBarStyle", name = "Prayer style",
        description = "OSRS artwork or modern prayer bar.", section = PRAYER_SECTION, position = 202)
    default OverheadsHealthBarStyle prayerBarStyle()
    {
        return OverheadsHealthBarStyle.SOLID;
    }

    @ConfigItem(hidden = true, keyName = "prayerFillDirection", name = "Prayer fill direction",
        description = "Direction the filled prayer bar grows.", section = PRAYER_SECTION, position = 203)
    default OverheadsHealthBarFillDirection prayerFillDirection()
    {
        return OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
    }

    @ConfigItem(hidden = true, keyName = "prayerBackColor", name = "Prayer background",
        description = "Color for missing prayer points in the Modern style.", section = PRAYER_SECTION, position = 204)
    @Alpha
    default Color prayerBackColor()
    {
        return new Color(30, 43, 55);
    }

    @ConfigItem(hidden = true, keyName = "prayerBorderColor", name = "Prayer border",
        description = "Modern prayer bar border color.", section = PRAYER_SECTION, position = 205)
    @Alpha
    default Color prayerBorderColor()
    {
        return new Color(18, 24, 30);
    }

    @ConfigItem(hidden = true, keyName = "prayerBorderThickness", name = "Prayer border thickness",
        description = "Modern prayer bar border thickness.", section = PRAYER_SECTION, position = 206)
    @Range(min = 0, max = 10)
    default int prayerBorderThickness()
    {
        return 1;
    }

    @ConfigItem(hidden = true, keyName = "prayerCornerRadius", name = "Prayer corner radius",
        description = "Modern prayer bar corner radius.", section = PRAYER_SECTION, position = 207)
    @Range(min = 0, max = 30)
    default int prayerCornerRadius()
    {
        return 3;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatStyle", name = "Style",
        description = "Style for damage and miss hitsplats. Special encounter indicators keep their artwork.", section = DISPLAY_SECTION, position = 208)
    default OverheadsHitsplatStyle hitsplatStyle()
    {
        return OverheadsHitsplatStyle.OSRS;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatDamageColor", name = "Damage background",
        description = "Modern damage hitsplat background.", section = DISPLAY_SECTION, position = 209)
    @Alpha
    default Color hitsplatDamageColor()
    {
        return new Color(169, 54, 63, 235);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatMissColor", name = "Miss background",
        description = "Modern miss hitsplat background.", section = DISPLAY_SECTION, position = 210)
    @Alpha
    default Color hitsplatMissColor()
    {
        return new Color(54, 97, 160, 235);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatNumberColor", name = "Number color",
        description = "Text color for Modern and Numbers only hitsplats.", section = DISPLAY_SECTION, position = 211)
    @Alpha
    default Color hitsplatNumberColor()
    {
        return Color.WHITE;
    }
    @ConfigItem(hidden = true, keyName = "restorePreview", name = "Health restore preview",
        description = "Preview the actual HP restored by the inventory item under the pointer.", section = HEALTH_BARS_SECTION, position = 220)
    default boolean restorePreview()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "restorePreviewColor", name = "Restore preview color",
        description = "Color of the projected restored portion.", section = HEALTH_BARS_SECTION, position = 221)
    @Alpha
    default Color restorePreviewColor()
    {
        return new Color(115, 235, 170, 190);
    }

    @ConfigItem(hidden = true, keyName = "prayerTickIndicator", name = "Prayer tick indicator",
        description = "Sweep once per observed game tick and flash when your prayer points decrease. This does not predict the next drain.", section = PRAYER_SECTION, position = 222)
    default boolean prayerTickIndicator()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "prayerTickColor", name = "Prayer tick color",
        description = "Color of the tick marker.", section = PRAYER_SECTION, position = 223)
    @Alpha
    default Color prayerTickColor()
    {
        return new Color(235, 240, 255);
    }

    @ConfigItem(hidden = true, keyName = "lowHealthThreshold", name = "Low HP threshold",
        description = "Use the warning color below this percentage of your HP. Zero disables it.", section = HEALTH_BARS_SECTION, position = 224)
    @Range(min = 0, max = 100)
    default int lowHealthThreshold()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "lowPrayerThreshold", name = "Low prayer threshold",
        description = "Use the warning color below this percentage of your prayer. Zero disables it.", section = PRAYER_SECTION, position = 225)
    @Range(min = 0, max = 100)
    default int lowPrayerThreshold()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "lowResourceColor", name = "Low health color",
        description = "Warning color for your low HP.", section = HEALTH_BARS_SECTION, position = 226)
    @Alpha
    default Color lowResourceColor()
    {
        return new Color(255, 106, 87);
    }

    @ConfigItem(hidden = true, keyName = "npcHealthVisibility", name = "NPC HP visibility",
        description = "Restrict which NPC HP bars and numbers are shown.", section = HEALTH_BARS_SECTION, position = 227)
    default OverheadsVisibility npcHealthVisibility()
    {
        return OverheadsVisibility.ALL;
    }

    @ConfigItem(hidden = true, keyName = "otherHealthVisibility", name = "Other player HP visibility",
        description = "Restrict which other player HP bars and numbers are shown.", section = HEALTH_BARS_SECTION, position = 228)
    default OverheadsVisibility otherHealthVisibility()
    {
        return OverheadsVisibility.ALL;
    }

    @ConfigItem(hidden = true, keyName = "hiddenNpcHealthIds", name = "Hidden NPC HP IDs",
        description = "Comma-separated NPC IDs whose ordinary HP display should be hidden. Special encounter bars remain native.", section = HEALTH_BARS_SECTION, position = 229)
    default String hiddenNpcHealthIds()
    {
        return "";
    }

    @ConfigItem(hidden = true, keyName = "selfNameVisibility", name = "My name",
        description = "Choose when this nameplate is displayed.", section = HEAD_ICONS_SECTION, position = 230)
    default OverheadsVisibility selfNameVisibility()
    {
        return OverheadsVisibility.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "playerNameVisibility", name = "Other player names",
        description = "Choose when this nameplate is displayed.", section = HEAD_ICONS_SECTION, position = 231)
    default OverheadsVisibility playerNameVisibility()
    {
        return OverheadsVisibility.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "npcNameVisibility", name = "NPC names",
        description = "Choose when this nameplate is displayed.", section = HEAD_ICONS_SECTION, position = 232)
    default OverheadsVisibility npcNameVisibility()
    {
        return OverheadsVisibility.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "namePosition", name = "Nameplate position",
        description = "Placement relative to the actor.", section = HEAD_ICONS_SECTION, position = 233)
    default OverheadsPosition namePosition()
    {
        return OverheadsPosition.ABOVE;
    }

    @ConfigItem(hidden = true, keyName = "nameXOffset", name = "Nameplate X offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 234)
    @Range(min = -300, max = 300)
    default int nameXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "nameYOffset", name = "Nameplate Y offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 235)
    @Range(min = -300, max = 300)
    default int nameYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "nameScaleWithZoom", name = "Scale nameplate with zoom",
        description = "Scale with camera zoom.", section = HEAD_ICONS_SECTION, position = 236)
    default boolean nameScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "statusPosition", name = "Status icon position",
        description = "Placement relative to the actor.", section = HEAD_ICONS_SECTION, position = 237)
    default OverheadsPosition statusPosition()
    {
        return OverheadsPosition.ABOVE;
    }

    @ConfigItem(hidden = true, keyName = "statusXOffset", name = "Status icon X offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 238)
    @Range(min = -300, max = 300)
    default int statusXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "statusYOffset", name = "Status icon Y offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 239)
    @Range(min = -300, max = 300)
    default int statusYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "statusScaleWithZoom", name = "Scale status icon with zoom",
        description = "Scale with camera zoom.", section = HEAD_ICONS_SECTION, position = 240)
    default boolean statusScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "nameFont", name = "Name font",
        description = "Nameplate font.", section = HEAD_ICONS_SECTION, position = 241)
    default FontType nameFont()
    {
        return FontType.SMALL;
    }

    @ConfigItem(hidden = true, keyName = "nameColor", name = "Name color",
        description = "Nameplate text color.", section = HEAD_ICONS_SECTION, position = 242)
    @Alpha
    default Color nameColor()
    {
        return new Color(235, 235, 235);
    }

    @ConfigItem(hidden = true, keyName = "nameBackground", name = "Name background",
        description = "Nameplate background, including opacity.", section = HEAD_ICONS_SECTION, position = 243)
    @Alpha
    default Color nameBackground()
    {
        return new Color(12, 18, 24, 170);
    }

    @ConfigItem(hidden = true, keyName = "showStatusIcons", name = "My status icons",
        description = "Show your current poison, venom and disease status.", section = HEAD_ICONS_SECTION, position = 244)
    default boolean showStatusIcons()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "showTargetingIcon", name = "My current target",
        description = "Mark the NPC you are currently interacting with.", section = HEAD_ICONS_SECTION, position = 245)
    default boolean showTargetingIcon()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "statusIconSize", name = "Status icon size",
        description = "Status badge size in pixels.", section = HEAD_ICONS_SECTION, position = 246)
    @Range(min = 10, max = 40)
    default int statusIconSize()
    {
        return 16;
    }

    @ConfigItem(hidden = true, keyName = "separatePrayerPosition", name = "Separate prayer position",
        description = "Position prayer icons independently of skulls and other head icons.", section = HEAD_ICONS_SECTION, position = 247)
    default boolean separatePrayerPosition()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "prayerIconPosition", name = "Prayer icon position",
        description = "Position of independently placed prayer icons.", section = HEAD_ICONS_SECTION, position = 248)
    default OverheadsPosition prayerIconPosition()
    {
        return OverheadsPosition.RIGHT;
    }

    @ConfigItem(hidden = true, keyName = "prayerIconXOffset", name = "Prayer icon X offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 249)
    @Range(min = -300, max = 300)
    default int prayerIconXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "prayerIconYOffset", name = "Prayer icon Y offset",
        description = "Offset in pixels. Positive Y moves upward.", section = HEAD_ICONS_SECTION, position = 250)
    @Range(min = -300, max = 300)
    default int prayerIconYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "runDisplay", name = "Run energy display",
        description = "Show your own resource as a bar, numbers, both, or hide it.", section = RUN_SECTION, position = 251)
    default OverheadsBarDisplay runDisplay()
    {
        return OverheadsBarDisplay.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "runStyle", name = "Run energy style",
        description = "OSRS artwork or Modern appearance.", section = RUN_SECTION, position = 252)
    default OverheadsHealthBarStyle runStyle()
    {
        return OverheadsHealthBarStyle.SOLID;
    }

    @ConfigItem(hidden = true, keyName = "runPosition", name = "Run energy position",
        description = "Placement relative to your player.", section = RUN_SECTION, position = 253)
    default OverheadsPosition runPosition()
    {
        return OverheadsPosition.BELOW;
    }

    @ConfigItem(hidden = true, keyName = "runOrientation", name = "Run energy orientation",
        description = "Horizontal or vertical bar.", section = RUN_SECTION, position = 254)
    default OverheadsOrientation runOrientation()
    {
        return OverheadsOrientation.HORIZONTAL;
    }

    @ConfigItem(hidden = true, keyName = "runWidth", name = "Run energy bar length",
        description = "Bar length in pixels.", section = RUN_SECTION, position = 255)
    @Range(min = 10, max = 200)
    default int runWidth()
    {
        return 50;
    }

    @ConfigItem(hidden = true, keyName = "runHeight", name = "Run energy bar thickness",
        description = "Bar thickness in pixels.", section = RUN_SECTION, position = 256)
    @Range(min = 2, max = 30)
    default int runHeight()
    {
        return 6;
    }

    @ConfigItem(hidden = true, keyName = "runXOffset", name = "Run energy X offset",
        description = "Horizontal offset in pixels.", section = RUN_SECTION, position = 257)
    @Range(min = -300, max = 300)
    default int runXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "runYOffset", name = "Run energy Y offset",
        description = "Vertical offset. Positive moves upward.", section = RUN_SECTION, position = 258)
    @Range(min = -300, max = 300)
    default int runYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "runColor", name = "Run energy fill",
        description = "Filled bar color.", section = RUN_SECTION, position = 259)
    @Alpha
    default Color runColor()
    {
        return new Color(223, 191, 70);
    }

    @ConfigItem(hidden = true, keyName = "runBackColor", name = "Run energy background",
        description = "Modern missing-resource color.", section = RUN_SECTION, position = 260)
    @Alpha
    default Color runBackColor()
    {
        return new Color(30, 38, 46);
    }

    @ConfigItem(hidden = true, keyName = "runBorderColor", name = "Run energy border",
        description = "Modern bar border color.", section = RUN_SECTION, position = 261)
    @Alpha
    default Color runBorderColor()
    {
        return new Color(10, 16, 22);
    }

    @ConfigItem(hidden = true, keyName = "runCornerRadius", name = "Run energy corner radius",
        description = "Modern bar corner radius.", section = RUN_SECTION, position = 262)
    @Range(min = 0, max = 30)
    default int runCornerRadius()
    {
        return 3;
    }

    @ConfigItem(hidden = true, keyName = "runOpacity", name = "Run energy bar opacity",
        description = "Opacity of the bar and border; numbers have their own color.", section = RUN_SECTION, position = 263)
    @Range(min = 0, max = 100)
    default int runOpacity()
    {
        return 100;
    }

    @ConfigItem(hidden = true, keyName = "runTextColor", name = "Run energy number color",
        description = "Resource number color.", section = RUN_SECTION, position = 264)
    @Alpha
    default Color runTextColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(hidden = true, keyName = "runFont", name = "Run energy font",
        description = "Resource number font.", section = RUN_SECTION, position = 265)
    default FontType runFont()
    {
        return FontType.SMALL;
    }

    @ConfigItem(hidden = true, keyName = "runNumberFormat", name = "Run energy number format",
        description = "Current, current / maximum, or percentage.", section = RUN_SECTION, position = 266)
    default OverheadsNumberFormat runNumberFormat()
    {
        return OverheadsNumberFormat.PERCENTAGE;
    }

    @ConfigItem(hidden = true, keyName = "runScaleWithZoom", name = "Scale run energy with zoom",
        description = "Scale with camera zoom.", section = RUN_SECTION, position = 267)
    default boolean runScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "runFillDirection", name = "Run energy fill direction",
        description = "Direction the resource bar grows.", section = RUN_SECTION, position = 268)
    default OverheadsHealthBarFillDirection runFillDirection()
    {
        return OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
    }

    @ConfigItem(hidden = true, keyName = "specialDisplay", name = "Special attack display",
        description = "Show your own resource as a bar, numbers, both, or hide it.", section = SPECIAL_SECTION, position = 269)
    default OverheadsBarDisplay specialDisplay()
    {
        return OverheadsBarDisplay.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "specialStyle", name = "Special attack style",
        description = "OSRS artwork or Modern appearance.", section = SPECIAL_SECTION, position = 270)
    default OverheadsHealthBarStyle specialStyle()
    {
        return OverheadsHealthBarStyle.SOLID;
    }

    @ConfigItem(hidden = true, keyName = "specialPosition", name = "Special attack position",
        description = "Placement relative to your player.", section = SPECIAL_SECTION, position = 271)
    default OverheadsPosition specialPosition()
    {
        return OverheadsPosition.BELOW;
    }

    @ConfigItem(hidden = true, keyName = "specialOrientation", name = "Special attack orientation",
        description = "Horizontal or vertical bar.", section = SPECIAL_SECTION, position = 272)
    default OverheadsOrientation specialOrientation()
    {
        return OverheadsOrientation.HORIZONTAL;
    }

    @ConfigItem(hidden = true, keyName = "specialWidth", name = "Special attack bar length",
        description = "Bar length in pixels.", section = SPECIAL_SECTION, position = 273)
    @Range(min = 10, max = 200)
    default int specialWidth()
    {
        return 50;
    }

    @ConfigItem(hidden = true, keyName = "specialHeight", name = "Special attack bar thickness",
        description = "Bar thickness in pixels.", section = SPECIAL_SECTION, position = 274)
    @Range(min = 2, max = 30)
    default int specialHeight()
    {
        return 6;
    }

    @ConfigItem(hidden = true, keyName = "specialXOffset", name = "Special attack X offset",
        description = "Horizontal offset in pixels.", section = SPECIAL_SECTION, position = 275)
    @Range(min = -300, max = 300)
    default int specialXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "specialYOffset", name = "Special attack Y offset",
        description = "Vertical offset. Positive moves upward.", section = SPECIAL_SECTION, position = 276)
    @Range(min = -300, max = 300)
    default int specialYOffset()
    {
        return -18;
    }

    @ConfigItem(hidden = true, keyName = "specialColor", name = "Special attack fill",
        description = "Filled bar color.", section = SPECIAL_SECTION, position = 277)
    @Alpha
    default Color specialColor()
    {
        return new Color(80, 195, 150);
    }

    @ConfigItem(hidden = true, keyName = "specialBackColor", name = "Special attack background",
        description = "Modern missing-resource color.", section = SPECIAL_SECTION, position = 278)
    @Alpha
    default Color specialBackColor()
    {
        return new Color(30, 38, 46);
    }

    @ConfigItem(hidden = true, keyName = "specialBorderColor", name = "Special attack border",
        description = "Modern bar border color.", section = SPECIAL_SECTION, position = 279)
    @Alpha
    default Color specialBorderColor()
    {
        return new Color(10, 16, 22);
    }

    @ConfigItem(hidden = true, keyName = "specialCornerRadius", name = "Special attack corner radius",
        description = "Modern bar corner radius.", section = SPECIAL_SECTION, position = 280)
    @Range(min = 0, max = 30)
    default int specialCornerRadius()
    {
        return 3;
    }

    @ConfigItem(hidden = true, keyName = "specialOpacity", name = "Special attack bar opacity",
        description = "Opacity of the bar and border; numbers have their own color.", section = SPECIAL_SECTION, position = 281)
    @Range(min = 0, max = 100)
    default int specialOpacity()
    {
        return 100;
    }

    @ConfigItem(hidden = true, keyName = "specialTextColor", name = "Special attack number color",
        description = "Resource number color.", section = SPECIAL_SECTION, position = 282)
    @Alpha
    default Color specialTextColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(hidden = true, keyName = "specialFont", name = "Special attack font",
        description = "Resource number font.", section = SPECIAL_SECTION, position = 283)
    default FontType specialFont()
    {
        return FontType.SMALL;
    }

    @ConfigItem(hidden = true, keyName = "specialNumberFormat", name = "Special attack number format",
        description = "Current, current / maximum, or percentage.", section = SPECIAL_SECTION, position = 284)
    default OverheadsNumberFormat specialNumberFormat()
    {
        return OverheadsNumberFormat.PERCENTAGE;
    }

    @ConfigItem(hidden = true, keyName = "specialScaleWithZoom", name = "Scale special attack with zoom",
        description = "Scale with camera zoom.", section = SPECIAL_SECTION, position = 285)
    default boolean specialScaleWithZoom()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "specialFillDirection", name = "Special attack fill direction",
        description = "Direction the resource bar grows.", section = SPECIAL_SECTION, position = 286)
    default OverheadsHealthBarFillDirection specialFillDirection()
    {
        return OverheadsHealthBarFillDirection.LEFT_TO_RIGHT;
    }


    @ConfigItem(hidden = true, keyName = "hitsplatMotion", name = "Movement",
        description = "Fixed arrangement or scrolling columns of observed hits.", section = DISPLAY_SECTION, position = 300)
    default OverheadsHitsplatMotion hitsplatMotion()
    {
        return OverheadsHitsplatMotion.FIXED;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatScrollColumns", name = "Scroll columns",
        description = "Number of columns for scrolling hitsplats.", section = DISPLAY_SECTION, position = 301)
    @Range(min = 1, max = 4)
    default int hitsplatScrollColumns()
    {
        return 2;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatScrollDistance", name = "Scroll distance",
        description = "Pixels travelled over each hitsplat lifetime, before zoom scaling.", section = DISPLAY_SECTION, position = 302)
    @Range(min = 0, max = 256)
    default int hitsplatScrollDistance()
    {
        return 48;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatPoisonColor", name = "Poison background",
        description = "Modern poison hitsplat background.", section = DISPLAY_SECTION, position = 303)
    @Alpha
    default Color hitsplatPoisonColor()
    {
        return new Color(66, 132, 42, 235);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatVenomColor", name = "Venom background",
        description = "Modern venom hitsplat background.", section = DISPLAY_SECTION, position = 304)
    @Alpha
    default Color hitsplatVenomColor()
    {
        return new Color(39, 85, 57, 235);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatBurnColor", name = "Burn background",
        description = "Modern burn hitsplat background.", section = DISPLAY_SECTION, position = 305)
    @Alpha
    default Color hitsplatBurnColor()
    {
        return new Color(179, 93, 29, 235);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatMissNumberColor", name = "Miss number color",
        description = "Modern and Numbers only miss text color.", section = DISPLAY_SECTION, position = 306)
    @Alpha
    default Color hitsplatMissNumberColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatStatusNumberColor", name = "Status number color",
        description = "Modern and Numbers only poison, venom and burn text color.", section = DISPLAY_SECTION, position = 307)
    @Alpha
    default Color hitsplatStatusNumberColor()
    {
        return Color.WHITE;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatBorderColor", name = "Border color",
        description = "Modern hitsplat border color.", section = DISPLAY_SECTION, position = 308)
    @Alpha
    default Color hitsplatBorderColor()
    {
        return new Color(255, 255, 255, 95);
    }

    @ConfigItem(hidden = true, keyName = "hitsplatBorderThickness", name = "Border thickness",
        description = "Modern hitsplat border width. Zero hides the border.", section = DISPLAY_SECTION, position = 309)
    @Range(min = 0, max = 5)
    default int hitsplatBorderThickness()
    {
        return 1;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatCornerRadius", name = "Corner radius",
        description = "Modern hitsplat corner roundness. Zero gives square corners.", section = DISPLAY_SECTION, position = 310)
    @Range(min = 0, max = 30)
    default int hitsplatCornerRadius()
    {
        return 8;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatTextShadow", name = "Number shadow",
        description = "Draw a shadow behind Modern and Numbers only hitsplat text.", section = DISPLAY_SECTION, position = 311)
    default boolean hitsplatTextShadow()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatXOffset", name = "X offset",
        description = "Horizontal offset for every replacement hitsplat group.", section = DISPLAY_SECTION, position = 312)
    @Range(min = -256, max = 256)
    default int hitsplatXOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "hitsplatYOffset", name = "Y offset",
        description = "Vertical offset for every replacement hitsplat group. Positive moves upward.", section = DISPLAY_SECTION, position = 313)
    @Range(min = -256, max = 256)
    default int hitsplatYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "nativeNpcUiIds", name = "Native NPC UI IDs", description = "Comma-separated NPC IDs that keep their complete original overhead display. Includes transformed IDs.", section = HEALTH_BARS_SECTION, position = 400)
    default String nativeNpcUiIds()
    {
        return "";
    }

    @ConfigItem(hidden = true, keyName = "relationshipNames", name = "Relationship name colors", description = "Use distinct colors for player relationships and observable NPC categories.", section = HEAD_ICONS_SECTION, position = 401)
    default boolean relationshipNames()
    {
        return false;
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "selfNameColor", name = "My name color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 402)
    default Color selfNameColor()
    {
        return new Color(255, 255, 255);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "friendNameColor", name = "Friends color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 403)
    default Color friendNameColor()
    {
        return new Color(100, 235, 140);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "clanNameColor", name = "Clan and friends chat color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 404)
    default Color clanNameColor()
    {
        return new Color(190, 150, 255);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "partyNameColor", name = "RuneLite party color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 405)
    default Color partyNameColor()
    {
        return new Color(100, 210, 255);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "playerNameColor", name = "Other players color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 406)
    default Color playerNameColor()
    {
        return new Color(230, 230, 230);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "npcNameColor", name = "Other NPCs color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 407)
    default Color npcNameColor()
    {
        return new Color(255, 220, 100);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "followerNameColor", name = "Followers color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 408)
    default Color followerNameColor()
    {
        return new Color(130, 220, 210);
    }

    @Alpha
    @ConfigItem(hidden = true, keyName = "shopNameColor", name = "Shops color", description = "Nameplate color, including opacity.", section = HEAD_ICONS_SECTION, position = 409)
    default Color shopNameColor()
    {
        return new Color(255, 175, 100);
    }

    @ConfigItem(hidden = true, keyName = "localBoostPrayerIcons", name = "My active boost prayers", description = "Show your active offensive prayers using the prayer icon frame and placement.", section = HEAD_ICONS_SECTION, position = 410)
    default boolean localBoostPrayerIcons()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "keepOnScreen", name = "Keep overheads on screen", description = "Keep the actor layout inside the viewport. Oversized layouts are clipped.", section = HEALTH_BARS_SECTION, position = 676)
    default boolean keepOnScreen()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "layoutPreview", name = "Alignment preview", description = "Outline your displayed components while adjusting positions.", section = HEALTH_BARS_SECTION, position = 677)
    default boolean layoutPreview()
    {
        return false;
    }

    @ConfigItem(hidden = true, keyName = "regenDisplay", name = "HP regeneration timer", description = "Estimated natural HP timer from the enabled Regen Meter plugin. Hidden for independent cape timers or unknown timing.", section = HEALTH_BARS_SECTION, position = 678)
    default OverheadsBarDisplay regenDisplay()
    {
        return OverheadsBarDisplay.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "poisonTimerDisplay", name = "Poison / venom timer", description = "Estimate after an observed poison or venom hit. Hidden until synchronized and when overdue or cured.", section = HEALTH_BARS_SECTION, position = 679)
    default OverheadsBarDisplay poisonTimerDisplay()
    {
        return OverheadsBarDisplay.HIDDEN;
    }

    @ConfigItem(hidden = true, keyName = "partyDisplay", name = "Party resources (opt-in)", description = "Shares your name, HP, prayer, run and special energy with your existing RuneLite party. Shows received member data; ~ marks last received values. Not copied by setups.", section = HEALTH_BARS_SECTION, position = 680)
    default OverheadsBarDisplay partyDisplay()
    {
        return OverheadsBarDisplay.HIDDEN;
    }

    @Range(min = 5, max = 300)
    @ConfigItem(hidden = true, keyName = "partyDataLifetime", name = "Party data lifetime", description = "Hide values after this many seconds without an update. Run energy is rounded by RuneLite.", section = HEALTH_BARS_SECTION, position = 681)
    default int partyDataLifetime()
    {
        return 60;
    }

    @ConfigItem(hidden = true, keyName = "partyShowHealth", name = "Party HP", description = "Show this received resource when party data is enabled.", section = HEALTH_BARS_SECTION, position = 682)
    default boolean partyShowHealth()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "partyShowPrayer", name = "Party Prayer", description = "Show this received resource when party data is enabled.", section = HEALTH_BARS_SECTION, position = 683)
    default boolean partyShowPrayer()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "partyShowRun", name = "Party Run energy", description = "Show this received resource when party data is enabled.", section = HEALTH_BARS_SECTION, position = 684)
    default boolean partyShowRun()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "partyShowSpecial", name = "Party Special energy", description = "Show this received resource when party data is enabled.", section = HEALTH_BARS_SECTION, position = 685)
    default boolean partyShowSpecial()
    {
        return true;
    }

    @ConfigItem(hidden = true, keyName = "indicatorPosition", name = "Indicator position", description = "Position for local timers and party resources.", section = HEALTH_BARS_SECTION, position = 686)
    default OverheadsPosition indicatorPosition()
    {
        return OverheadsPosition.RIGHT;
    }

    @ConfigItem(hidden = true, keyName = "indicatorOrientation", name = "Indicator orientation", description = "Horizontal or vertical bars.", section = HEALTH_BARS_SECTION, position = 687)
    default OverheadsOrientation indicatorOrientation()
    {
        return OverheadsOrientation.HORIZONTAL;
    }

    @ConfigItem(hidden = true, keyName = "indicatorStyle", name = "Indicator style", description = "OSRS or modern bar appearance.", section = HEALTH_BARS_SECTION, position = 688)
    default OverheadsHealthBarStyle indicatorStyle()
    {
        return OverheadsHealthBarStyle.SOLID;
    }

    @Range(min = 20, max = 200)
    @ConfigItem(hidden = true, keyName = "indicatorWidth", name = "Indicator length", description = "Length in pixels.", section = HEALTH_BARS_SECTION, position = 689)
    default int indicatorWidth()
    {
        return 70;
    }

    @Range(min = 2, max = 30)
    @ConfigItem(hidden = true, keyName = "indicatorHeight", name = "Indicator thickness", description = "Bar thickness in pixels.", section = HEALTH_BARS_SECTION, position = 690)
    default int indicatorHeight()
    {
        return 12;
    }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "indicatorXOffset", name = "Indicator X offset", description = "Horizontal offset.", section = HEALTH_BARS_SECTION, position = 691)
    default int indicatorXOffset()
    {
        return 0;
    }

    @Range(min = -300, max = 300)
    @ConfigItem(hidden = true, keyName = "indicatorYOffset", name = "Indicator Y offset", description = "Positive moves upward.", section = HEALTH_BARS_SECTION, position = 692)
    default int indicatorYOffset()
    {
        return 0;
    }

    @ConfigItem(hidden = true, keyName = "indicatorScaleWithZoom", name = "Scale indicators with zoom", description = "Resize indicators with camera zoom.", section = HEALTH_BARS_SECTION, position = 693)
    default boolean indicatorScaleWithZoom()
    {
        return false;
    }

    @Range(min = 0, max = 100)
    @ConfigItem(hidden = true, keyName = "indicatorOpacity", name = "Indicator opacity", description = "Opacity for bars and their numbers.", section = HEALTH_BARS_SECTION, position = 694)
    default int indicatorOpacity()
    {
        return 100;
    }
    @ConfigItem(hidden = true, keyName = "runHideWhenFull", name = "Hide full run energy",
        description = "Hide your run-energy numbers and bar while energy is completely full.", section = RUN_SECTION, position = 710)
    default boolean runHideWhenFull() { return false; }

    @ConfigItem(hidden = true, keyName = "specialHideWhenFull", name = "Hide full special energy",
        description = "Hide your special-attack numbers and bar while energy is completely full.", section = SPECIAL_SECTION, position = 711)
    default boolean specialHideWhenFull() { return false; }


    @ConfigItem(hidden = true, keyName = "prayerTextPosition", name = "Prayer number position",
        description = "Place numbers on, above or below this bar.", section = PRAYER_SECTION, position = 500)
    default OverheadsBarTextPosition prayerTextPosition() { return OverheadsBarTextPosition.ON_BAR; }

    @ConfigItem(hidden = true, keyName = "runTextPosition", name = "Run energy number position",
        description = "Place numbers on, above or below this bar.", section = RUN_SECTION, position = 501)
    default OverheadsBarTextPosition runTextPosition() { return OverheadsBarTextPosition.ON_BAR; }

    @ConfigItem(hidden = true, keyName = "specialTextPosition", name = "Special attack number position",
        description = "Place numbers on, above or below this bar.", section = SPECIAL_SECTION, position = 502)
    default OverheadsBarTextPosition specialTextPosition() { return OverheadsBarTextPosition.ON_BAR; }

    @ConfigItem(hidden = true, keyName = "prayerIconRow", name = "Overhead row",
        description = "Row 1 is above Row 2 when prayer groups share a position.", section = HEAD_ICONS_SECTION, position = 503)
    default OverheadsPrayerRow prayerIconRow() { return OverheadsPrayerRow.ROW_1; }

    @ConfigItem(hidden = true, keyName = "prayerIconCombatRow", name = "Combat/misc row",
        description = "Icons on the same row are arranged horizontally.", section = HEAD_ICONS_SECTION, position = 504)
    default OverheadsPrayerRow prayerIconCombatRow() { return OverheadsPrayerRow.ROW_2; }

    @ConfigItem(hidden = true, keyName = "prayerIconCombatPosition", name = "Combat/misc position",
        description = "Position of combat and miscellaneous prayer icons.", section = HEAD_ICONS_SECTION, position = 505)
    default OverheadsPosition prayerIconCombatPosition() { return OverheadsPosition.RIGHT; }

    @ConfigItem(hidden = true, keyName = "prayerIconCombatScalePercent", name = "Combat/misc size",
        description = "Size of combat and miscellaneous prayer icons in percent.", section = HEAD_ICONS_SECTION, position = 506)
    @Range(min = 25, max = 300)
    default int prayerIconCombatScalePercent() { return 100; }

    @ConfigItem(hidden = true, keyName = "prayerIconCombatXOffset", name = "Combat/misc X offset",
        description = "Horizontal offset in pixels.", section = HEAD_ICONS_SECTION, position = 507)
    @Range(min = -300, max = 300)
    default int prayerIconCombatXOffset() { return 0; }

    @ConfigItem(hidden = true, keyName = "prayerIconCombatYOffset", name = "Combat/misc Y offset",
        description = "Vertical offset in pixels. Positive moves upward.", section = HEAD_ICONS_SECTION, position = 508)
    @Range(min = -300, max = 300)
    default int prayerIconCombatYOffset() { return 0; }


    @ConfigItem(hidden = true, keyName = "prayerIconRow2BackgroundEnabled", name = "Row 2: Background",
        description = "Show the background for prayer icons assigned to Row 2.", section = HEAD_ICONS_SECTION, position = 520)
    default boolean prayerIconRow2BackgroundEnabled() { return true; }

    @ConfigItem(hidden = true, keyName = "prayerIconRow2Shape", name = "Row 2: Frame",
        description = "Frame shape for Row 2 prayer icons.", section = HEAD_ICONS_SECTION, position = 521)
    default OverheadsIconShape prayerIconRow2Shape() { return OverheadsIconShape.ORIGINAL; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconRow2Background", name = "Row 2: Background color",
        description = "Background color and opacity for Row 2.", section = HEAD_ICONS_SECTION, position = 522)
    default Color prayerIconRow2Background() { return new Color(35, 40, 46, 210); }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconRow2Border", name = "Row 2: Border color",
        description = "Border color and opacity for Row 2.", section = HEAD_ICONS_SECTION, position = 523)
    default Color prayerIconRow2Border() { return new Color(170, 180, 190); }

    @ConfigItem(hidden = true, keyName = "prayerIconRow2TracksPoints", name = "Row 2: Follow prayer points",
        description = "Drain and refill Row 2 backgrounds with your prayer points or available shared party prayer.", section = HEAD_ICONS_SECTION, position = 524)
    default boolean prayerIconRow2TracksPoints() { return false; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconRow2EmptyBackground", name = "Row 2: Empty background",
        description = "Empty background color and opacity for Row 2.", section = HEAD_ICONS_SECTION, position = 525)
    default Color prayerIconRow2EmptyBackground() { return new Color(12, 15, 18, 150); }


    @ConfigItem(hidden = true, keyName = "prayerIconLayout", name = "Prayer layout",
        description = "Attach small secondary prayers to a visible overhead prayer; otherwise use the configured regular row.", section = HEAD_ICONS_SECTION, position = 980)
    default OverheadsPrayerLayout prayerIconLayout() { return OverheadsPrayerLayout.ROWS; }

    @Range(min = 24, max = 64)
    @ConfigItem(hidden = true, keyName = "prayerIconBadgeProtectionSize", name = "Attached overhead size",
        description = "Overhead prayer icon size in pixels in the attached layout, before overall icon and zoom scaling.", section = HEAD_ICONS_SECTION, position = 981)
    default int prayerIconBadgeProtectionSize() { return 34; }

    @Range(min = 12, max = 28)
    @ConfigItem(hidden = true, keyName = "prayerIconBadgeSize", name = "Small badge size",
        description = "Secondary icon size in pixels in the attached layout, before overall icon and zoom scaling.", section = HEAD_ICONS_SECTION, position = 982)
    default int prayerIconBadgeSize() { return 18; }

    @Range(min = 0, max = 8)
    @ConfigItem(hidden = true, keyName = "prayerIconBadgeSpacing", name = "Badge spacing",
        description = "Extra spacing between the overhead prayer icon and corner badges.", section = HEAD_ICONS_SECTION, position = 983)
    default int prayerIconBadgeSpacing() { return 0; }

    @ConfigItem(hidden = true, keyName = "prayerIconBadgeBackground", name = "Badge backgrounds",
        description = "Show a round background and border behind each small badge. Regular rows and overhead prayer backgrounds are unchanged.", section = HEAD_ICONS_SECTION, position = 984)
    default boolean prayerIconBadgeBackground() { return false; }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconBadgeBackgroundColor", name = "Badge background color",
        description = "Background color and opacity of attached secondary badges.", section = HEAD_ICONS_SECTION, position = 985)
    default Color prayerIconBadgeBackgroundColor() { return new Color(35, 40, 46, 210); }

    @Alpha
    @ConfigItem(hidden = true, keyName = "prayerIconBadgeBorderColor", name = "Badge border color",
        description = "Border color and opacity of attached secondary badges.", section = HEAD_ICONS_SECTION, position = 986)
    default Color prayerIconBadgeBorderColor() { return new Color(170, 180, 190); }
}
