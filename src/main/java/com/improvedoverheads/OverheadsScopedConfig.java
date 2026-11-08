package com.improvedoverheads;

import java.awt.Color;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.FontType;

final class OverheadsScopedConfig implements OverheadsConfig
{
    private static final OverheadsConfig DEFAULTS = new OverheadsConfig() {};
    private final ConfigManager manager;
    private final String group;
    private volatile Map<String, Object> values = new ConcurrentHashMap<>();

    OverheadsScopedConfig(ConfigManager manager, String group)
    {
        this.manager = manager;
        this.group = group;
    }

    void invalidate() { values = new ConcurrentHashMap<>(); }

    private <T> T read(String key, Class<T> type, java.util.function.Supplier<T> fallback)
    {
        Object value = values.computeIfAbsent(key, ignored ->
        {
            T stored = manager.getConfiguration(group, key, type);
            return stored == null ? fallback.get() : stored;
        });
        return type.cast(value);
    }

    @Override public boolean avoidOverlap() { return read("avoidOverlap", Boolean.class, DEFAULTS::avoidOverlap); }
    @Override public int componentGap() { return read("componentGap", Integer.class, DEFAULTS::componentGap); }
    @Override public boolean prayerRestorePreview() { return read("prayerRestorePreview", Boolean.class, DEFAULTS::prayerRestorePreview); }
    @Override public Color prayerRestorePreviewColor() { return read("prayerRestorePreviewColor", Color.class, DEFAULTS::prayerRestorePreviewColor); }
    @Override public Color prayerLowColor() { return read("prayerLowColor", Color.class, DEFAULTS::prayerLowColor); }
    @Override public boolean disableEnemyHitsplats() { return read("disableEnemyHitsplats", Boolean.class, DEFAULTS::disableEnemyHitsplats); }
    @Override public boolean disableAllyHitsplats() { return read("disableAllyHitsplats", Boolean.class, DEFAULTS::disableAllyHitsplats); }
    @Override public boolean disableMyHitsplats() { return read("disableMyHitsplats", Boolean.class, DEFAULTS::disableMyHitsplats); }
    @Override public int maxHitsplats() { return read("maxHitsplats", Integer.class, DEFAULTS::maxHitsplats); }
    @Override public boolean reuseOldHitsplatSlots() { return read("reuseOldHitsplatSlots", Boolean.class, DEFAULTS::reuseOldHitsplatSlots); }
    @Override public int hitsplatReuseInterval() { return read("hitsplatReuseInterval", Integer.class, DEFAULTS::hitsplatReuseInterval); }
    @Override public int opacityPercent() { return read("opacityPercent", Integer.class, DEFAULTS::opacityPercent); }
    @Override public boolean onlyDisplayMine() { return read("onlyDisplayMine", Boolean.class, DEFAULTS::onlyDisplayMine); }
    @Override public boolean prioritizeMine() { return read("prioritizeMine", Boolean.class, DEFAULTS::prioritizeMine); }
    @Override public boolean hideZeroHitsplats() { return read("hideZeroHitsplats", Boolean.class, DEFAULTS::hideZeroHitsplats); }
    @Override public OverheadsLayoutShape layoutShape() { return read("layoutShape", OverheadsLayoutShape.class, DEFAULTS::layoutShape); }
    @Override public OverheadsLayoutDirection layoutDirection() { return read("layoutDirection", OverheadsLayoutDirection.class, DEFAULTS::layoutDirection); }
    @Override public OverheadsLayoutBehavior layoutBehavior() { return read("layoutBehavior", OverheadsLayoutBehavior.class, DEFAULTS::layoutBehavior); }
    @Override public int minRadius() { return read("minRadius", Integer.class, DEFAULTS::minRadius); }
    @Override public int maxRadius() { return read("maxRadius", Integer.class, DEFAULTS::maxRadius); }
    @Override public int xSpacing() { return read("xSpacingPixels", Integer.class, DEFAULTS::xSpacing); }
    @Override public int ySpacing() { return read("ySpacingPixels", Integer.class, DEFAULTS::ySpacing); }
    @Override public FontType hitsplatFont() { return read("hitsplatFont", FontType.class, DEFAULTS::hitsplatFont); }
    @Override public int hitsplatScalePercent() { return read("hitsplatScalePercent", Integer.class, DEFAULTS::hitsplatScalePercent); }
    @Override public OverheadsTargetDetection largeTargetDetection() { return read("largeTargetDetection", OverheadsTargetDetection.class, DEFAULTS::largeTargetDetection); }
    @Override public int largeTargetSize() { return read("largeTargetSize", Integer.class, DEFAULTS::largeTargetSize); }
    @Override public int largeTargetHealthScale() { return read("largeTargetHealthScale", Integer.class, DEFAULTS::largeTargetHealthScale); }
    @Override public int largeTargetXOffset() { return read("largeTargetXOffset", Integer.class, DEFAULTS::largeTargetXOffset); }
    @Override public int largeTargetYOffset() { return read("largeTargetYOffset", Integer.class, DEFAULTS::largeTargetYOffset); }
    @Override public int fadeInDuration() { return read("fadeInDuration", Integer.class, DEFAULTS::fadeInDuration); }
    @Override public int fullOpacityDuration() { return read("fullOpacityDuration", Integer.class, DEFAULTS::fullOpacityDuration); }
    @Override public int fadeOutDuration() { return read("fadeOutDuration", Integer.class, DEFAULTS::fadeOutDuration); }
    @Override public OverheadsSpriteScalingMode spriteScalingMode() { return read("spriteScalingMode", OverheadsSpriteScalingMode.class, DEFAULTS::spriteScalingMode); }
    @Override public boolean healthBarsEnabled() { return read("healthBarsEnabled", Boolean.class, DEFAULTS::healthBarsEnabled); }
    @Override public OverheadsHealthBarStyle healthBarStyle() { return read("healthBarStyle", OverheadsHealthBarStyle.class, DEFAULTS::healthBarStyle); }
    @Override public OverheadsHealthScaleMode healthBarScaleMode() { return read("healthBarScaleMode", OverheadsHealthScaleMode.class, DEFAULTS::healthBarScaleMode); }
    @Override public int healthBarScalePercent() { return read("healthBarScalePercent", Integer.class, DEFAULTS::healthBarScalePercent); }
    @Override public int healthBarScaleThreshold() { return read("healthBarScaleThreshold", Integer.class, DEFAULTS::healthBarScaleThreshold); }
    @Override public int healthBarLargeScalePercent() { return read("healthBarLargeScalePercent", Integer.class, DEFAULTS::healthBarLargeScalePercent); }
    @Override public int healthBarLargeHeightScalePercent() { return read("healthBarLargeHeightScalePercent", Integer.class, DEFAULTS::healthBarLargeHeightScalePercent); }
    @Override public double healthBarSolidWidth() { return read("healthBarSolidWidth", Double.class, DEFAULTS::healthBarSolidWidth); }
    @Override public double healthBarHeight() { return read("healthBarHeight", Double.class, DEFAULTS::healthBarHeight); }
    @Override public int healthBarXOffset() { return read("healthBarXOffset", Integer.class, DEFAULTS::healthBarXOffset); }
    @Override public int healthBarYOffset() { return read("healthBarYOffset", Integer.class, DEFAULTS::healthBarYOffset); }
    @Override public OverheadsHealthBarFillDirection healthBarFillDirection() { return read("healthBarFillDirection", OverheadsHealthBarFillDirection.class, DEFAULTS::healthBarFillDirection); }
    @Override public OverheadsHealthBarGradientCoordinates healthBarFrontGradientCoordinates() { return read("healthBarFrontGradientCoordinates", OverheadsHealthBarGradientCoordinates.class, DEFAULTS::healthBarFrontGradientCoordinates); }
    @Override public OverheadsHealthBarGradient healthBarFrontGradient() { return read("healthBarFrontGradient", OverheadsHealthBarGradient.class, DEFAULTS::healthBarFrontGradient); }
    @Override public Color healthBarFrontColor() { return read("healthBarFrontColor", Color.class, DEFAULTS::healthBarFrontColor); }
    @Override public Color healthBarFrontSecondaryColor() { return read("healthBarFrontSecondaryColor", Color.class, DEFAULTS::healthBarFrontSecondaryColor); }
    @Override public Color healthBarPoisonedFrontColor() { return read("healthBarPoisonedFrontColor", Color.class, DEFAULTS::healthBarPoisonedFrontColor); }
    @Override public OverheadsHealthBarGradientCoordinates healthBarBackGradientCoordinates() { return read("healthBarBackGradientCoordinates", OverheadsHealthBarGradientCoordinates.class, DEFAULTS::healthBarBackGradientCoordinates); }
    @Override public OverheadsHealthBarGradient healthBarBackGradient() { return read("healthBarBackGradient", OverheadsHealthBarGradient.class, DEFAULTS::healthBarBackGradient); }
    @Override public Color healthBarBackColor() { return read("healthBarBackColor", Color.class, DEFAULTS::healthBarBackColor); }
    @Override public Color healthBarBackSecondaryColor() { return read("healthBarBackSecondaryColor", Color.class, DEFAULTS::healthBarBackSecondaryColor); }
    @Override public boolean healthBarDamageTrailEnabled() { return read("healthBarDamageTrailEnabled", Boolean.class, DEFAULTS::healthBarDamageTrailEnabled); }
    @Override public Color healthBarDamageTrailColor() { return read("healthBarDamageTrailColor", Color.class, DEFAULTS::healthBarDamageTrailColor); }
    @Override public int healthBarDamageTrailHold() { return read("healthBarDamageTrailHold", Integer.class, DEFAULTS::healthBarDamageTrailHold); }
    @Override public int healthBarDamageTrailDrain() { return read("healthBarDamageTrailDrain", Integer.class, DEFAULTS::healthBarDamageTrailDrain); }
    @Override public boolean healthBarSegmentsEnabled() { return read("healthBarSegmentsEnabled", Boolean.class, DEFAULTS::healthBarSegmentsEnabled); }
    @Override public OverheadsHealthBarSegmentValueMode healthBarSegmentValueMode() { return read("healthBarSegmentValueMode", OverheadsHealthBarSegmentValueMode.class, DEFAULTS::healthBarSegmentValueMode); }
    @Override public int healthBarHitpointsPerSegment() { return read("healthBarHitpointsPerSegment", Integer.class, DEFAULTS::healthBarHitpointsPerSegment); }
    @Override public Color healthBarSegmentColor() { return read("healthBarSegmentColor", Color.class, DEFAULTS::healthBarSegmentColor); }
    @Override public double healthBarSegmentThickness() { return read("healthBarSegmentThickness", Double.class, DEFAULTS::healthBarSegmentThickness); }
    @Override public Color healthBarBorderColor() { return read("healthBarBorderColor", Color.class, DEFAULTS::healthBarBorderColor); }
    @Override public double healthBarBorderThickness() { return read("healthBarBorderThickness", Double.class, DEFAULTS::healthBarBorderThickness); }
    @Override public double healthBarCornerRadius() { return read("healthBarCornerRadius", Double.class, DEFAULTS::healthBarCornerRadius); }
    @Override public boolean overheadChatEnabled() { return read("overheadChatEnabled", Boolean.class, DEFAULTS::overheadChatEnabled); }
    @Override public boolean showNpcOverheadChat() { return read("showNpcOverheadChat", Boolean.class, DEFAULTS::showNpcOverheadChat); }
    @Override public FontType overheadChatFont() { return read("overheadChatFont", FontType.class, DEFAULTS::overheadChatFont); }
    @Override public Color overheadChatColor() { return read("overheadChatColor", Color.class, DEFAULTS::overheadChatColor); }
    @Override public boolean overheadChatRelationshipColors() { return read("overheadChatRelationshipColors", Boolean.class, DEFAULTS::overheadChatRelationshipColors); }
    @Override public Color overheadChatFriendColor() { return read("overheadChatFriendColor", Color.class, DEFAULTS::overheadChatFriendColor); }
    @Override public Color overheadChatClanColor() { return read("overheadChatClanColor", Color.class, DEFAULTS::overheadChatClanColor); }
    @Override public Color overheadChatGroupIronColor() { return read("overheadChatGroupIronColor", Color.class, DEFAULTS::overheadChatGroupIronColor); }
    @Override public OverheadsOverheadChatEffect overheadChatEffect() { return read("overheadChatEffect", OverheadsOverheadChatEffect.class, DEFAULTS::overheadChatEffect); }
    @Override public boolean overheadChatShadow() { return read("overheadChatShadow", Boolean.class, DEFAULTS::overheadChatShadow); }
    @Override public Color overheadChatShadowColor() { return read("overheadChatShadowColor", Color.class, DEFAULTS::overheadChatShadowColor); }
    @Override public int overheadChatXOffset() { return read("overheadChatXOffset", Integer.class, DEFAULTS::overheadChatXOffset); }
    @Override public int overheadChatYOffset() { return read("overheadChatYOffset", Integer.class, DEFAULTS::overheadChatYOffset); }
    @Override public boolean headIconsEnabled() { return read("headIconsEnabled", Boolean.class, DEFAULTS::headIconsEnabled); }
    @Override public boolean showPrayerIcons() { return read("showPrayerIcons", Boolean.class, DEFAULTS::showPrayerIcons); }
    @Override public boolean showPrayerIconBackground() { return read("showPrayerIconBackground", Boolean.class, DEFAULTS::showPrayerIconBackground); }
    @Override public boolean showSkullIcons() { return read("showSkullIcons", Boolean.class, DEFAULTS::showSkullIcons); }
    @Override public boolean showNpcIcons() { return read("showNpcIcons", Boolean.class, DEFAULTS::showNpcIcons); }
    @Override public boolean showHintArrows() { return read("showHintArrows", Boolean.class, DEFAULTS::showHintArrows); }
    @Override public int headIconScalePercent() { return read("headIconScalePercent", Integer.class, DEFAULTS::headIconScalePercent); }
    @Override public boolean headIconScaleWithZoom() { return read("headIconScaleWithZoom", Boolean.class, DEFAULTS::headIconScaleWithZoom); }
    @Override public int headIconXOffset() { return read("headIconXOffset", Integer.class, DEFAULTS::headIconXOffset); }
    @Override public int headIconYOffset() { return read("headIconYOffset", Integer.class, DEFAULTS::headIconYOffset); }
    @Override public int headIconZOffset() { return read("headIconZOffset", Integer.class, DEFAULTS::headIconZOffset); }
    @Override public int headIconSpacing() { return read("headIconSpacing", Integer.class, DEFAULTS::headIconSpacing); }
    @Override public boolean alwaysShowSelfHealth() { return read("alwaysShowSelfHealth", Boolean.class, DEFAULTS::alwaysShowSelfHealth); }
    @Override public OverheadsBarDisplay selfHealthDisplay() { return read("selfHealthDisplay", OverheadsBarDisplay.class, DEFAULTS::selfHealthDisplay); }
    @Override public OverheadsBarDisplay npcHealthDisplay() { return read("npcHealthDisplay", OverheadsBarDisplay.class, DEFAULTS::npcHealthDisplay); }
    @Override public OverheadsBarDisplay otherHealthDisplay() { return read("otherHealthDisplay", OverheadsBarDisplay.class, DEFAULTS::otherHealthDisplay); }
    @Override public OverheadsPosition healthPosition() { return read("healthPosition", OverheadsPosition.class, DEFAULTS::healthPosition); }
    @Override public OverheadsOrientation healthOrientation() { return read("healthOrientation", OverheadsOrientation.class, DEFAULTS::healthOrientation); }
    @Override public boolean healthScaleWithZoom() { return read("healthScaleWithZoom", Boolean.class, DEFAULTS::healthScaleWithZoom); }
    @Override public int playerHealthWidth() { return read("playerHealthWidth", Integer.class, DEFAULTS::playerHealthWidth); }
    @Override public int playerHealthHeight() { return read("playerHealthHeight", Integer.class, DEFAULTS::playerHealthHeight); }
    @Override public int healthBarOpacity() { return read("healthBarOpacity", Integer.class, DEFAULTS::healthBarOpacity); }
    @Override public FontType healthTextFont() { return read("healthTextFont", FontType.class, DEFAULTS::healthTextFont); }
    @Override public OverheadsBarTextPosition healthTextPosition() { return read("healthTextPosition", OverheadsBarTextPosition.class, DEFAULTS::healthTextPosition); }
    @Override public Color healthTextColor() { return read("healthTextColor", Color.class, DEFAULTS::healthTextColor); }
    @Override public OverheadsPrayerVisibility prayerVisibility() { return read("prayerVisibility", OverheadsPrayerVisibility.class, DEFAULTS::prayerVisibility); }
    @Override public OverheadsBarDisplay prayerDisplay() { return read("prayerDisplay", OverheadsBarDisplay.class, DEFAULTS::prayerDisplay); }
    @Override public OverheadsPosition prayerPosition() { return read("prayerPosition", OverheadsPosition.class, DEFAULTS::prayerPosition); }
    @Override public OverheadsOrientation prayerOrientation() { return read("prayerOrientation", OverheadsOrientation.class, DEFAULTS::prayerOrientation); }
    @Override public boolean prayerScaleWithZoom() { return read("prayerScaleWithZoom", Boolean.class, DEFAULTS::prayerScaleWithZoom); }
    @Override public int prayerWidth() { return read("prayerWidth", Integer.class, DEFAULTS::prayerWidth); }
    @Override public int prayerHeight() { return read("prayerHeight", Integer.class, DEFAULTS::prayerHeight); }
    @Override public int prayerXOffset() { return read("prayerXOffset", Integer.class, DEFAULTS::prayerXOffset); }
    @Override public int prayerYOffset() { return read("prayerYOffset", Integer.class, DEFAULTS::prayerYOffset); }
    @Override public int prayerOpacity() { return read("prayerOpacity", Integer.class, DEFAULTS::prayerOpacity); }
    @Override public Color prayerColor() { return read("prayerColor", Color.class, DEFAULTS::prayerColor); }
    @Override public Color prayerTextColor() { return read("prayerTextColor", Color.class, DEFAULTS::prayerTextColor); }
    @Override public FontType prayerTextFont() { return read("prayerTextFont", FontType.class, DEFAULTS::prayerTextFont); }
    @Override public OverheadsPosition chatPosition() { return read("chatPosition", OverheadsPosition.class, DEFAULTS::chatPosition); }
    @Override public boolean chatScaleWithZoom() { return read("chatScaleWithZoom", Boolean.class, DEFAULTS::chatScaleWithZoom); }
    @Override public OverheadsHitsplatPosition hitsplatPosition() { return read("hitsplatPosition", OverheadsHitsplatPosition.class, DEFAULTS::hitsplatPosition); }
    @Override public boolean hitsplatScaleWithZoom() { return read("hitsplatScaleWithZoom", Boolean.class, DEFAULTS::hitsplatScaleWithZoom); }
    @Override public OverheadsPosition iconsPosition() { return read("iconsPosition", OverheadsPosition.class, DEFAULTS::iconsPosition); }
    @Override public OverheadsIconShape prayerIconShape() { return read("prayerIconShape", OverheadsIconShape.class, DEFAULTS::prayerIconShape); }
    @Override public Color prayerIconBackground() { return read("prayerIconBackground", Color.class, DEFAULTS::prayerIconBackground); }
    @Override public boolean prayerIconBackgroundTracksPoints() { return read("prayerIconBackgroundTracksPoints", Boolean.class, DEFAULTS::prayerIconBackgroundTracksPoints); }
    @Override public Color prayerIconEmptyBackground() { return read("prayerIconEmptyBackground", Color.class, DEFAULTS::prayerIconEmptyBackground); }
    @Override public Color prayerIconBorder() { return read("prayerIconBorder", Color.class, DEFAULTS::prayerIconBorder); }
    @Override public int prayerIconScalePercent() { return read("prayerIconScalePercent", Integer.class, DEFAULTS::prayerIconScalePercent); }
    @Override public OverheadsNumberFormat healthNumberFormat() { return read("healthNumberFormat", OverheadsNumberFormat.class, DEFAULTS::healthNumberFormat); }
    @Override public OverheadsNumberFormat prayerNumberFormat() { return read("prayerNumberFormat", OverheadsNumberFormat.class, DEFAULTS::prayerNumberFormat); }
    @Override public OverheadsHealthBarStyle prayerBarStyle() { return read("prayerBarStyle", OverheadsHealthBarStyle.class, DEFAULTS::prayerBarStyle); }
    @Override public OverheadsHealthBarFillDirection prayerFillDirection() { return read("prayerFillDirection", OverheadsHealthBarFillDirection.class, DEFAULTS::prayerFillDirection); }
    @Override public Color prayerBackColor() { return read("prayerBackColor", Color.class, DEFAULTS::prayerBackColor); }
    @Override public Color prayerBorderColor() { return read("prayerBorderColor", Color.class, DEFAULTS::prayerBorderColor); }
    @Override public int prayerBorderThickness() { return read("prayerBorderThickness", Integer.class, DEFAULTS::prayerBorderThickness); }
    @Override public int prayerCornerRadius() { return read("prayerCornerRadius", Integer.class, DEFAULTS::prayerCornerRadius); }
    @Override public OverheadsHitsplatStyle hitsplatStyle() { return read("hitsplatStyle", OverheadsHitsplatStyle.class, DEFAULTS::hitsplatStyle); }
    @Override public Color hitsplatDamageColor() { return read("hitsplatDamageColor", Color.class, DEFAULTS::hitsplatDamageColor); }
    @Override public Color hitsplatMissColor() { return read("hitsplatMissColor", Color.class, DEFAULTS::hitsplatMissColor); }
    @Override public Color hitsplatNumberColor() { return read("hitsplatNumberColor", Color.class, DEFAULTS::hitsplatNumberColor); }
    @Override public boolean restorePreview() { return read("restorePreview", Boolean.class, DEFAULTS::restorePreview); }
    @Override public Color restorePreviewColor() { return read("restorePreviewColor", Color.class, DEFAULTS::restorePreviewColor); }
    @Override public boolean prayerTickIndicator() { return read("prayerTickIndicator", Boolean.class, DEFAULTS::prayerTickIndicator); }
    @Override public Color prayerTickColor() { return read("prayerTickColor", Color.class, DEFAULTS::prayerTickColor); }
    @Override public int lowHealthThreshold() { return read("lowHealthThreshold", Integer.class, DEFAULTS::lowHealthThreshold); }
    @Override public int lowPrayerThreshold() { return read("lowPrayerThreshold", Integer.class, DEFAULTS::lowPrayerThreshold); }
    @Override public Color lowResourceColor() { return read("lowResourceColor", Color.class, DEFAULTS::lowResourceColor); }
    @Override public OverheadsVisibility npcHealthVisibility() { return read("npcHealthVisibility", OverheadsVisibility.class, DEFAULTS::npcHealthVisibility); }
    @Override public OverheadsVisibility otherHealthVisibility() { return read("otherHealthVisibility", OverheadsVisibility.class, DEFAULTS::otherHealthVisibility); }
    @Override public String hiddenNpcHealthIds() { return read("hiddenNpcHealthIds", String.class, DEFAULTS::hiddenNpcHealthIds); }
    @Override public OverheadsVisibility selfNameVisibility() { return read("selfNameVisibility", OverheadsVisibility.class, DEFAULTS::selfNameVisibility); }
    @Override public OverheadsVisibility playerNameVisibility() { return read("playerNameVisibility", OverheadsVisibility.class, DEFAULTS::playerNameVisibility); }
    @Override public OverheadsVisibility npcNameVisibility() { return read("npcNameVisibility", OverheadsVisibility.class, DEFAULTS::npcNameVisibility); }
    @Override public OverheadsPosition namePosition() { return read("namePosition", OverheadsPosition.class, DEFAULTS::namePosition); }
    @Override public int nameXOffset() { return read("nameXOffset", Integer.class, DEFAULTS::nameXOffset); }
    @Override public int nameYOffset() { return read("nameYOffset", Integer.class, DEFAULTS::nameYOffset); }
    @Override public boolean nameScaleWithZoom() { return read("nameScaleWithZoom", Boolean.class, DEFAULTS::nameScaleWithZoom); }
    @Override public OverheadsPosition statusPosition() { return read("statusPosition", OverheadsPosition.class, DEFAULTS::statusPosition); }
    @Override public int statusXOffset() { return read("statusXOffset", Integer.class, DEFAULTS::statusXOffset); }
    @Override public int statusYOffset() { return read("statusYOffset", Integer.class, DEFAULTS::statusYOffset); }
    @Override public boolean statusScaleWithZoom() { return read("statusScaleWithZoom", Boolean.class, DEFAULTS::statusScaleWithZoom); }
    @Override public FontType nameFont() { return read("nameFont", FontType.class, DEFAULTS::nameFont); }
    @Override public Color nameColor() { return read("nameColor", Color.class, DEFAULTS::nameColor); }
    @Override public Color nameBackground() { return read("nameBackground", Color.class, DEFAULTS::nameBackground); }
    @Override public boolean showStatusIcons() { return read("showStatusIcons", Boolean.class, DEFAULTS::showStatusIcons); }
    @Override public boolean showTargetingIcon() { return read("showTargetingIcon", Boolean.class, DEFAULTS::showTargetingIcon); }
    @Override public int statusIconSize() { return read("statusIconSize", Integer.class, DEFAULTS::statusIconSize); }
    @Override public boolean separatePrayerPosition() { return read("separatePrayerPosition", Boolean.class, DEFAULTS::separatePrayerPosition); }
    @Override public OverheadsPosition prayerIconPosition() { return read("prayerIconPosition", OverheadsPosition.class, DEFAULTS::prayerIconPosition); }
    @Override public int prayerIconXOffset() { return read("prayerIconXOffset", Integer.class, DEFAULTS::prayerIconXOffset); }
    @Override public int prayerIconYOffset() { return read("prayerIconYOffset", Integer.class, DEFAULTS::prayerIconYOffset); }
    @Override public OverheadsBarDisplay runDisplay() { return read("runDisplay", OverheadsBarDisplay.class, DEFAULTS::runDisplay); }
    @Override public OverheadsHealthBarStyle runStyle() { return read("runStyle", OverheadsHealthBarStyle.class, DEFAULTS::runStyle); }
    @Override public OverheadsPosition runPosition() { return read("runPosition", OverheadsPosition.class, DEFAULTS::runPosition); }
    @Override public OverheadsOrientation runOrientation() { return read("runOrientation", OverheadsOrientation.class, DEFAULTS::runOrientation); }
    @Override public int runWidth() { return read("runWidth", Integer.class, DEFAULTS::runWidth); }
    @Override public int runHeight() { return read("runHeight", Integer.class, DEFAULTS::runHeight); }
    @Override public int runXOffset() { return read("runXOffset", Integer.class, DEFAULTS::runXOffset); }
    @Override public int runYOffset() { return read("runYOffset", Integer.class, DEFAULTS::runYOffset); }
    @Override public Color runColor() { return read("runColor", Color.class, DEFAULTS::runColor); }
    @Override public Color runBackColor() { return read("runBackColor", Color.class, DEFAULTS::runBackColor); }
    @Override public Color runBorderColor() { return read("runBorderColor", Color.class, DEFAULTS::runBorderColor); }
    @Override public int runCornerRadius() { return read("runCornerRadius", Integer.class, DEFAULTS::runCornerRadius); }
    @Override public int runOpacity() { return read("runOpacity", Integer.class, DEFAULTS::runOpacity); }
    @Override public Color runTextColor() { return read("runTextColor", Color.class, DEFAULTS::runTextColor); }
    @Override public FontType runFont() { return read("runFont", FontType.class, DEFAULTS::runFont); }
    @Override public OverheadsNumberFormat runNumberFormat() { return read("runNumberFormat", OverheadsNumberFormat.class, DEFAULTS::runNumberFormat); }
    @Override public boolean runScaleWithZoom() { return read("runScaleWithZoom", Boolean.class, DEFAULTS::runScaleWithZoom); }
    @Override public OverheadsHealthBarFillDirection runFillDirection() { return read("runFillDirection", OverheadsHealthBarFillDirection.class, DEFAULTS::runFillDirection); }
    @Override public OverheadsBarDisplay specialDisplay() { return read("specialDisplay", OverheadsBarDisplay.class, DEFAULTS::specialDisplay); }
    @Override public OverheadsHealthBarStyle specialStyle() { return read("specialStyle", OverheadsHealthBarStyle.class, DEFAULTS::specialStyle); }
    @Override public OverheadsPosition specialPosition() { return read("specialPosition", OverheadsPosition.class, DEFAULTS::specialPosition); }
    @Override public OverheadsOrientation specialOrientation() { return read("specialOrientation", OverheadsOrientation.class, DEFAULTS::specialOrientation); }
    @Override public int specialWidth() { return read("specialWidth", Integer.class, DEFAULTS::specialWidth); }
    @Override public int specialHeight() { return read("specialHeight", Integer.class, DEFAULTS::specialHeight); }
    @Override public int specialXOffset() { return read("specialXOffset", Integer.class, DEFAULTS::specialXOffset); }
    @Override public int specialYOffset() { return read("specialYOffset", Integer.class, DEFAULTS::specialYOffset); }
    @Override public Color specialColor() { return read("specialColor", Color.class, DEFAULTS::specialColor); }
    @Override public Color specialBackColor() { return read("specialBackColor", Color.class, DEFAULTS::specialBackColor); }
    @Override public Color specialBorderColor() { return read("specialBorderColor", Color.class, DEFAULTS::specialBorderColor); }
    @Override public int specialCornerRadius() { return read("specialCornerRadius", Integer.class, DEFAULTS::specialCornerRadius); }
    @Override public int specialOpacity() { return read("specialOpacity", Integer.class, DEFAULTS::specialOpacity); }
    @Override public Color specialTextColor() { return read("specialTextColor", Color.class, DEFAULTS::specialTextColor); }
    @Override public FontType specialFont() { return read("specialFont", FontType.class, DEFAULTS::specialFont); }
    @Override public OverheadsNumberFormat specialNumberFormat() { return read("specialNumberFormat", OverheadsNumberFormat.class, DEFAULTS::specialNumberFormat); }
    @Override public boolean specialScaleWithZoom() { return read("specialScaleWithZoom", Boolean.class, DEFAULTS::specialScaleWithZoom); }
    @Override public OverheadsHealthBarFillDirection specialFillDirection() { return read("specialFillDirection", OverheadsHealthBarFillDirection.class, DEFAULTS::specialFillDirection); }
    @Override public OverheadsHitsplatMotion hitsplatMotion() { return read("hitsplatMotion", OverheadsHitsplatMotion.class, DEFAULTS::hitsplatMotion); }
    @Override public int hitsplatScrollColumns() { return read("hitsplatScrollColumns", Integer.class, DEFAULTS::hitsplatScrollColumns); }
    @Override public int hitsplatScrollDistance() { return read("hitsplatScrollDistance", Integer.class, DEFAULTS::hitsplatScrollDistance); }
    @Override public Color hitsplatPoisonColor() { return read("hitsplatPoisonColor", Color.class, DEFAULTS::hitsplatPoisonColor); }
    @Override public Color hitsplatVenomColor() { return read("hitsplatVenomColor", Color.class, DEFAULTS::hitsplatVenomColor); }
    @Override public Color hitsplatBurnColor() { return read("hitsplatBurnColor", Color.class, DEFAULTS::hitsplatBurnColor); }
    @Override public Color hitsplatMissNumberColor() { return read("hitsplatMissNumberColor", Color.class, DEFAULTS::hitsplatMissNumberColor); }
    @Override public Color hitsplatStatusNumberColor() { return read("hitsplatStatusNumberColor", Color.class, DEFAULTS::hitsplatStatusNumberColor); }
    @Override public Color hitsplatBorderColor() { return read("hitsplatBorderColor", Color.class, DEFAULTS::hitsplatBorderColor); }
    @Override public int hitsplatBorderThickness() { return read("hitsplatBorderThickness", Integer.class, DEFAULTS::hitsplatBorderThickness); }
    @Override public int hitsplatCornerRadius() { return read("hitsplatCornerRadius", Integer.class, DEFAULTS::hitsplatCornerRadius); }
    @Override public boolean hitsplatTextShadow() { return read("hitsplatTextShadow", Boolean.class, DEFAULTS::hitsplatTextShadow); }
    @Override public int hitsplatXOffset() { return read("hitsplatXOffset", Integer.class, DEFAULTS::hitsplatXOffset); }
    @Override public int hitsplatYOffset() { return read("hitsplatYOffset", Integer.class, DEFAULTS::hitsplatYOffset); }
    @Override public String nativeNpcUiIds() { return read("nativeNpcUiIds", String.class, DEFAULTS::nativeNpcUiIds); }
    @Override public boolean relationshipNames() { return read("relationshipNames", Boolean.class, DEFAULTS::relationshipNames); }
    @Override public Color selfNameColor() { return read("selfNameColor", Color.class, DEFAULTS::selfNameColor); }
    @Override public Color friendNameColor() { return read("friendNameColor", Color.class, DEFAULTS::friendNameColor); }
    @Override public Color clanNameColor() { return read("clanNameColor", Color.class, DEFAULTS::clanNameColor); }
    @Override public Color partyNameColor() { return read("partyNameColor", Color.class, DEFAULTS::partyNameColor); }
    @Override public Color playerNameColor() { return read("playerNameColor", Color.class, DEFAULTS::playerNameColor); }
    @Override public Color npcNameColor() { return read("npcNameColor", Color.class, DEFAULTS::npcNameColor); }
    @Override public Color followerNameColor() { return read("followerNameColor", Color.class, DEFAULTS::followerNameColor); }
    @Override public Color shopNameColor() { return read("shopNameColor", Color.class, DEFAULTS::shopNameColor); }
    @Override public boolean localBoostPrayerIcons() { return read("localBoostPrayerIcons", Boolean.class, DEFAULTS::localBoostPrayerIcons); }
    @Override public boolean keepOnScreen() { return read("keepOnScreen", Boolean.class, DEFAULTS::keepOnScreen); }
    @Override public boolean layoutPreview() { return read("layoutPreview", Boolean.class, DEFAULTS::layoutPreview); }
    @Override public OverheadsBarDisplay regenDisplay() { return read("regenDisplay", OverheadsBarDisplay.class, DEFAULTS::regenDisplay); }
    @Override public OverheadsBarDisplay poisonTimerDisplay() { return read("poisonTimerDisplay", OverheadsBarDisplay.class, DEFAULTS::poisonTimerDisplay); }
    @Override public OverheadsBarDisplay partyDisplay() { return read("partyDisplay", OverheadsBarDisplay.class, DEFAULTS::partyDisplay); }
    @Override public int partyDataLifetime() { return read("partyDataLifetime", Integer.class, DEFAULTS::partyDataLifetime); }
    @Override public boolean partyShowHealth() { return read("partyShowHealth", Boolean.class, DEFAULTS::partyShowHealth); }
    @Override public boolean partyShowPrayer() { return read("partyShowPrayer", Boolean.class, DEFAULTS::partyShowPrayer); }
    @Override public boolean partyShowRun() { return read("partyShowRun", Boolean.class, DEFAULTS::partyShowRun); }
    @Override public boolean partyShowSpecial() { return read("partyShowSpecial", Boolean.class, DEFAULTS::partyShowSpecial); }
    @Override public OverheadsPosition indicatorPosition() { return read("indicatorPosition", OverheadsPosition.class, DEFAULTS::indicatorPosition); }
    @Override public OverheadsOrientation indicatorOrientation() { return read("indicatorOrientation", OverheadsOrientation.class, DEFAULTS::indicatorOrientation); }
    @Override public OverheadsHealthBarStyle indicatorStyle() { return read("indicatorStyle", OverheadsHealthBarStyle.class, DEFAULTS::indicatorStyle); }
    @Override public int indicatorWidth() { return read("indicatorWidth", Integer.class, DEFAULTS::indicatorWidth); }
    @Override public int indicatorHeight() { return read("indicatorHeight", Integer.class, DEFAULTS::indicatorHeight); }
    @Override public int indicatorXOffset() { return read("indicatorXOffset", Integer.class, DEFAULTS::indicatorXOffset); }
    @Override public int indicatorYOffset() { return read("indicatorYOffset", Integer.class, DEFAULTS::indicatorYOffset); }
    @Override public boolean indicatorScaleWithZoom() { return read("indicatorScaleWithZoom", Boolean.class, DEFAULTS::indicatorScaleWithZoom); }
    @Override public int indicatorOpacity() { return read("indicatorOpacity", Integer.class, DEFAULTS::indicatorOpacity); }
    @Override public boolean runHideWhenFull() { return read("runHideWhenFull", Boolean.class, DEFAULTS::runHideWhenFull); }
    @Override public boolean specialHideWhenFull() { return read("specialHideWhenFull", Boolean.class, DEFAULTS::specialHideWhenFull); }

    @Override public OverheadsBarTextPosition prayerTextPosition() { return read("prayerTextPosition", OverheadsBarTextPosition.class, DEFAULTS::prayerTextPosition); }
    @Override public OverheadsBarTextPosition runTextPosition() { return read("runTextPosition", OverheadsBarTextPosition.class, DEFAULTS::runTextPosition); }
    @Override public OverheadsBarTextPosition specialTextPosition() { return read("specialTextPosition", OverheadsBarTextPosition.class, DEFAULTS::specialTextPosition); }
    @Override public OverheadsPrayerRow prayerIconRow() { return read("prayerIconRow", OverheadsPrayerRow.class, DEFAULTS::prayerIconRow); }
    @Override public OverheadsPrayerRow prayerIconCombatRow() { return read("prayerIconCombatRow", OverheadsPrayerRow.class, DEFAULTS::prayerIconCombatRow); }
    @Override public OverheadsPosition prayerIconCombatPosition() { return read("prayerIconCombatPosition", OverheadsPosition.class, DEFAULTS::prayerIconCombatPosition); }
    @Override public int prayerIconCombatScalePercent() { return read("prayerIconCombatScalePercent", Integer.class, DEFAULTS::prayerIconCombatScalePercent); }
    @Override public int prayerIconCombatXOffset() { return read("prayerIconCombatXOffset", Integer.class, DEFAULTS::prayerIconCombatXOffset); }
    @Override public int prayerIconCombatYOffset() { return read("prayerIconCombatYOffset", Integer.class, DEFAULTS::prayerIconCombatYOffset); }

    @Override public boolean prayerIconRow2BackgroundEnabled() { return read("prayerIconRow2BackgroundEnabled", Boolean.class, DEFAULTS::prayerIconRow2BackgroundEnabled); }
    @Override public OverheadsIconShape prayerIconRow2Shape() { return read("prayerIconRow2Shape", OverheadsIconShape.class, DEFAULTS::prayerIconRow2Shape); }
    @Override public Color prayerIconRow2Background() { return read("prayerIconRow2Background", Color.class, DEFAULTS::prayerIconRow2Background); }
    @Override public Color prayerIconRow2Border() { return read("prayerIconRow2Border", Color.class, DEFAULTS::prayerIconRow2Border); }
    @Override public boolean prayerIconRow2TracksPoints() { return read("prayerIconRow2TracksPoints", Boolean.class, DEFAULTS::prayerIconRow2TracksPoints); }
    @Override public Color prayerIconRow2EmptyBackground() { return read("prayerIconRow2EmptyBackground", Color.class, DEFAULTS::prayerIconRow2EmptyBackground); }
}
