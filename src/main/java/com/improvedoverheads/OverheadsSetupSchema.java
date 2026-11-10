package com.improvedoverheads;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import java.awt.Color;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;
import net.runelite.client.config.FontType;

final class OverheadsSetupSchema
{
    static final class Entry<T>
    {
        final String key;
        final String label;
        String group = OverheadsConfig.GROUP;
        String storageKey;
        final Supplier<T> getter;
        final Function<T, JsonElement> encode;
        final Function<JsonElement, T> decode;

        Entry(String key, String label, Supplier<T> getter, Function<T, JsonElement> encode, Function<JsonElement, T> decode)
        {
            this.key = key;
            this.storageKey = key;
            this.label = label;
            this.getter = getter;
            this.encode = encode;
            this.decode = decode;
        }

        JsonElement current()
        {
            return encode.apply(getter.get());
        }
    }

    final Map<String, Entry<?>> entries = new LinkedHashMap<>();

    OverheadsSetupSchema(OverheadsConfig config)
    {
        add("healingSplatEnabled", "Healing: Enabled", config::healingSplatEnabled, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("healingSplatDisplay", "Healing: Display", config::healingSplatDisplay, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsRestoreDisplay.values()));
        add("healingSplatColor", "Healing: Number color", config::healingSplatColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healingSplatFont", "Healing: Number font", config::healingSplatFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("healingSplatOpacity", "Healing: Opacity", config::healingSplatOpacity, JsonPrimitive::new, value -> integerValue(value, 0, 100));
        add("healingSplatDuration", "Healing: Duration (ms)", config::healingSplatDuration, JsonPrimitive::new, value -> integerValue(value, 200, 5000));
        add("healingSplatPosition", "Healing: Position", config::healingSplatPosition, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHitsplatPosition.values()));
        add("healingSplatXOffset", "Healing: X offset", config::healingSplatXOffset, JsonPrimitive::new, value -> integerValue(value, -300, 300));
        add("healingSplatYOffset", "Healing: Y offset", config::healingSplatYOffset, JsonPrimitive::new, value -> integerValue(value, -300, 300));
        add("healingSplatScalePercent", "Healing: Scale (%)", config::healingSplatScalePercent, JsonPrimitive::new, value -> integerValue(value, 25, 300));
        add("healingSplatScaleWithZoom", "Healing: Scale with zoom", config::healingSplatScaleWithZoom, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("prayerSplatEnabled", "Prayer restoration: Enabled", config::prayerSplatEnabled, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("prayerSplatDisplay", "Prayer restoration: Display", config::prayerSplatDisplay, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsRestoreDisplay.values()));
        add("prayerSplatColor", "Prayer restoration: Number color", config::prayerSplatColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerSplatFont", "Prayer restoration: Number font", config::prayerSplatFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("prayerSplatOpacity", "Prayer restoration: Opacity", config::prayerSplatOpacity, JsonPrimitive::new, value -> integerValue(value, 0, 100));
        add("prayerSplatDuration", "Prayer restoration: Duration (ms)", config::prayerSplatDuration, JsonPrimitive::new, value -> integerValue(value, 200, 5000));
        add("prayerSplatPosition", "Prayer restoration: Position", config::prayerSplatPosition, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHitsplatPosition.values()));
        add("prayerSplatXOffset", "Prayer restoration: X offset", config::prayerSplatXOffset, JsonPrimitive::new, value -> integerValue(value, -300, 300));
        add("prayerSplatYOffset", "Prayer restoration: Y offset", config::prayerSplatYOffset, JsonPrimitive::new, value -> integerValue(value, -300, 300));
        add("prayerSplatScalePercent", "Prayer restoration: Scale (%)", config::prayerSplatScalePercent, JsonPrimitive::new, value -> integerValue(value, 25, 300));
        add("prayerSplatScaleWithZoom", "Prayer restoration: Scale with zoom", config::prayerSplatScaleWithZoom, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("prayerRestorePreview", "Prayer restore preview", config::prayerRestorePreview, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("prayerRestorePreviewColor", "Prayer restore color", config::prayerRestorePreviewColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerLowColor", "Low prayer color", config::prayerLowColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("runHideWhenFull", "Hide full run energy", config::runHideWhenFull, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("specialHideWhenFull", "Hide full special energy", config::specialHideWhenFull, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("nativeNpcUiIds", "Native NPC UI IDs", config::nativeNpcUiIds, value -> new JsonPrimitive(value), value -> stringValue(value, 4096));
        add("relationshipNames", "Relationship name colors", config::relationshipNames, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("selfNameColor", "My name color", config::selfNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("friendNameColor", "Friends color", config::friendNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("clanNameColor", "Clan and friends chat color", config::clanNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("partyNameColor", "RuneLite party color", config::partyNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("playerNameColor", "Other players color", config::playerNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("npcNameColor", "Other NPCs color", config::npcNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("followerNameColor", "Followers color", config::followerNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("shopNameColor", "Shops color", config::shopNameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("localBoostPrayerIcons", "My active boost prayers", config::localBoostPrayerIcons, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("keepOnScreen", "Keep overheads on screen", config::keepOnScreen, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("layoutPreview", "Alignment preview", config::layoutPreview, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("regenDisplay", "HP regeneration timer", config::regenDisplay, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("poisonTimerDisplay", "Poison / venom timer", config::poisonTimerDisplay, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("partyDisplay", "Party resources (opt-in)", config::partyDisplay, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("partyDataLifetime", "Party data lifetime", config::partyDataLifetime, value -> new JsonPrimitive(value), value -> integerValue(value, 5, 300));
        add("partyShowHealth", "Party HP", config::partyShowHealth, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("partyShowPrayer", "Party Prayer", config::partyShowPrayer, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("partyShowRun", "Party Run energy", config::partyShowRun, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("partyShowSpecial", "Party Special energy", config::partyShowSpecial, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("indicatorPosition", "Indicator position", config::indicatorPosition, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("indicatorOrientation", "Indicator orientation", config::indicatorOrientation, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOrientation.values()));
        add("indicatorStyle", "Indicator style", config::indicatorStyle, value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarStyle.values()));
        add("indicatorWidth", "Indicator length", config::indicatorWidth, value -> new JsonPrimitive(value), value -> integerValue(value, 20, 200));
        add("indicatorHeight", "Indicator thickness", config::indicatorHeight, value -> new JsonPrimitive(value), value -> integerValue(value, 2, 30));
        add("indicatorXOffset", "Indicator X offset", config::indicatorXOffset, value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("indicatorYOffset", "Indicator Y offset", config::indicatorYOffset, value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("indicatorScaleWithZoom", "Scale indicators with zoom", config::indicatorScaleWithZoom, value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("indicatorOpacity", "Indicator opacity", config::indicatorOpacity, value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("avoidOverlap", "Health: Avoid overlaps", config::avoidOverlap,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("componentGap", "Health: Component spacing", config::componentGap,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 20));
        add("disableEnemyHitsplats", "Hits: Hide NPC hits", config::disableEnemyHitsplats,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("disableAllyHitsplats", "Hits: Hide other players' hits", config::disableAllyHitsplats,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("disableMyHitsplats", "Hits: Hide hits on my player", config::disableMyHitsplats,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("maxHitsplats", "Hits: Max hitsplats", config::maxHitsplats,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 128));
        add("reuseOldHitsplatSlots", "Hits: Reuse old slots", config::reuseOldHitsplatSlots,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("hitsplatReuseInterval", "Hits: Reuse interval", config::hitsplatReuseInterval,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 60000));
        add("opacityPercent", "Hits: Opacity", config::opacityPercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("onlyDisplayMine", "Hits: My hits only", config::onlyDisplayMine,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prioritizeMine", "Hits: Prioritize my hits", config::prioritizeMine,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("hideZeroHitsplats", "Hits: Hide zero hits", config::hideZeroHitsplats,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("layoutShape", "Hits: Shape", config::layoutShape,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsLayoutShape.values()));
        add("layoutDirection", "Hits: Direction", config::layoutDirection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsLayoutDirection.values()));
        add("layoutBehavior", "Hits: Behavior", config::layoutBehavior,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsLayoutBehavior.values()));
        add("minRadius", "Hits: Min radius", config::minRadius, value -> new JsonPrimitive(value), value -> integerValue(value, 0, 64));
        add("maxRadius", "Hits: Max radius", config::maxRadius, value -> new JsonPrimitive(value), value -> integerValue(value, 0, 64));
        add("xSpacingPixels", "Hits: X spacing", config::xSpacing,
            value -> new JsonPrimitive(value), value -> integerValue(value, -64, 64));
        add("ySpacingPixels", "Hits: Y spacing", config::ySpacing,
            value -> new JsonPrimitive(value), value -> integerValue(value, -64, 64));
        add("hitsplatFont", "Hits: Font", config::hitsplatFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("hitsplatScalePercent", "Hits: Scale", config::hitsplatScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 200));
        add("largeTargetDetection", "Hits: Boss-like detection", config::largeTargetDetection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsTargetDetection.values()));
        add("largeTargetSize", "Hits: Footprint threshold", config::largeTargetSize,
            value -> new JsonPrimitive(value), value -> integerValue(value, 1, 16));
        add("largeTargetHealthScale", "Hits: Boss health-scale threshold", config::largeTargetHealthScale,
            value -> new JsonPrimitive(value), value -> integerValue(value, 100, 255));
        add("largeTargetXOffset", "Hits: Boss-like X offset", config::largeTargetXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("largeTargetYOffset", "Hits: Boss-like Y offset", config::largeTargetYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("fadeInDuration", "Hits: Fade-in duration", config::fadeInDuration,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 60000));
        add("fullOpacityDuration", "Hits: Full opacity duration", config::fullOpacityDuration,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 60000));
        add("fadeOutDuration", "Hits: Fade-out duration", config::fadeOutDuration,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 60000));
        add("spriteScalingMode", "Hits: Sprite scaling", config::spriteScalingMode,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsSpriteScalingMode.values()));
        add("healthBarsEnabled", "Health: Show HP", config::healthBarsEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("healthBarStyle", "Health: Style", config::healthBarStyle,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarStyle.values()));
        add("healthBarScaleMode", "Health: Scale mode", config::healthBarScaleMode,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthScaleMode.values()));
        add("healthBarScalePercent", "Health: Scale", config::healthBarScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 200));
        add("healthBarScaleThreshold", "Health: Boss scale threshold", config::healthBarScaleThreshold,
            value -> new JsonPrimitive(value), value -> integerValue(value, 100, 255));
        add("healthBarLargeScalePercent", "Health: Boss width scale", config::healthBarLargeScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 200));
        add("healthBarLargeHeightScalePercent", "Health: Boss height scale", config::healthBarLargeHeightScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 200));
        add("healthBarSolidWidth", "Health: Width", config::healthBarSolidWidth,
            value -> new JsonPrimitive(value), value -> decimalValue(value, 10, 200));
        add("healthBarHeight", "Health: Height", config::healthBarHeight,
            value -> new JsonPrimitive(value), value -> decimalValue(value, 2, 20));
        add("healthBarXOffset", "Health: X offset", config::healthBarXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("healthBarYOffset", "Health: Y offset", config::healthBarYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("healthBarFillDirection", "Health: Fill direction", config::healthBarFillDirection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarFillDirection.values()));
        add("healthBarFrontGradientCoordinates", "Health: Front coordinates", config::healthBarFrontGradientCoordinates,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarGradientCoordinates.values()));
        add("healthBarFrontGradient", "Health: Front gradient", config::healthBarFrontGradient,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarGradient.values()));
        add("healthBarFrontColor", "Health: Front color", config::healthBarFrontColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarFrontSecondaryColor", "Health: Front second color", config::healthBarFrontSecondaryColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarPoisonedFrontColor", "Health: Poisoned front color", config::healthBarPoisonedFrontColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarBackGradientCoordinates", "Health: Back coordinates", config::healthBarBackGradientCoordinates,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarGradientCoordinates.values()));
        add("healthBarBackGradient", "Health: Back gradient", config::healthBarBackGradient,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarGradient.values()));
        add("healthBarBackColor", "Health: Back color", config::healthBarBackColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarBackSecondaryColor", "Health: Back second color", config::healthBarBackSecondaryColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarDamageTrailEnabled", "Health: Damage trail", config::healthBarDamageTrailEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("healthBarDamageTrailColor", "Health: Damage trail color", config::healthBarDamageTrailColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarDamageTrailHold", "Health: Damage trail hold", config::healthBarDamageTrailHold,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 5000));
        add("healthBarDamageTrailDrain", "Health: Damage trail drain", config::healthBarDamageTrailDrain,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 5000));
        add("healthBarSegmentsEnabled", "Health: HP segments", config::healthBarSegmentsEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("healthBarSegmentValueMode", "Health: Segment HP source", config::healthBarSegmentValueMode,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarSegmentValueMode.values()));
        add("healthBarHitpointsPerSegment", "Health: HP per segment", config::healthBarHitpointsPerSegment,
            value -> new JsonPrimitive(value), value -> integerValue(value, 1, 10000));
        add("healthBarSegmentColor", "Health: Segment color", config::healthBarSegmentColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarSegmentThickness", "Health: Segment thickness", config::healthBarSegmentThickness,
            value -> new JsonPrimitive(value), value -> decimalValue(value, 0, 10));
        add("healthBarBorderColor", "Health: Border color", config::healthBarBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("healthBarBorderThickness", "Health: Border thickness", config::healthBarBorderThickness,
            value -> new JsonPrimitive(value), value -> decimalValue(value, 0, 10));
        add("healthBarCornerRadius", "Health: Corner radius", config::healthBarCornerRadius,
            value -> new JsonPrimitive(value), value -> decimalValue(value, 0, 50));
        add("overheadChatEnabled", "Chat: Show overhead chat", config::overheadChatEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showNpcOverheadChat", "Chat: NPC overhead text", config::showNpcOverheadChat,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("overheadChatFont", "Chat: Font", config::overheadChatFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("overheadChatColor", "Chat: Color", config::overheadChatColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("overheadChatRelationshipColors", "Chat: Relationship colors", config::overheadChatRelationshipColors,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("overheadChatFriendColor", "Chat: Friend color", config::overheadChatFriendColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("overheadChatClanColor", "Chat: Clan color", config::overheadChatClanColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("overheadChatGroupIronColor", "Chat: Group iron color", config::overheadChatGroupIronColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("overheadChatEffect", "Chat: Default Text Effect", config::overheadChatEffect,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOverheadChatEffect.values()));
        add("overheadChatShadow", "Chat: Text shadow", config::overheadChatShadow,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("overheadChatShadowColor", "Chat: Shadow color", config::overheadChatShadowColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("overheadChatXOffset", "Chat: X offset", config::overheadChatXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("overheadChatYOffset", "Chat: Y offset", config::overheadChatYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("headIconsEnabled", "Icons: Show head icons", config::headIconsEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showPrayerIcons", "Icons: Prayer icons", config::showPrayerIcons,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconSymbolsOnly", "Icons: Prayer symbols only", config::prayerIconSymbolsOnly, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("prayerIconNativeSymbolsOnly", "Icons: Remove native prayer background", config::prayerIconNativeSymbolsOnly, JsonPrimitive::new, OverheadsSetupSchema::booleanValue);
        add("showPrayerIconBackground", "Icons: Prayer background", config::showPrayerIconBackground,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showSkullIcons", "Icons: Skull icons", config::showSkullIcons,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showNpcIcons", "Icons: NPC icons", config::showNpcIcons,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showHintArrows", "Icons: Hint arrows", config::showHintArrows,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("headIconScalePercent", "Icons: Scale", config::headIconScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 50, 200));
        add("headIconScaleWithZoom", "Icons: Scale with zoom", config::headIconScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("headIconXOffset", "Icons: X offset", config::headIconXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("headIconYOffset", "Icons: Y offset", config::headIconYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("headIconZOffset", "Icons: Z offset", config::headIconZOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -1024, 1024));
        add("headIconSpacing", "Icons: Spacing", config::headIconSpacing,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 20));
        add("alwaysShowSelfHealth", "Health: Always show my HP", config::alwaysShowSelfHealth,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("selfHealthDisplay", "Health: My HP display", config::selfHealthDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("npcHealthDisplay", "Health: NPC HP display", config::npcHealthDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("otherHealthDisplay", "Health: Other player HP display", config::otherHealthDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("healthPosition", "Health: HP position", config::healthPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("healthOrientation", "Health: HP orientation", config::healthOrientation,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOrientation.values()));
        add("healthScaleWithZoom", "Health: Scale HP with zoom", config::healthScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("playerHealthWidth", "Health: Player bar length", config::playerHealthWidth,
            value -> new JsonPrimitive(value), value -> integerValue(value, 10, 200));
        add("playerHealthHeight", "Health: Player bar thickness", config::playerHealthHeight,
            value -> new JsonPrimitive(value), value -> integerValue(value, 2, 30));
        add("healthBarOpacity", "Health: HP bar opacity", config::healthBarOpacity,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("healthTextFont", "Health: HP number font", config::healthTextFont,
            OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("healthTextColor", "Health: HP number color", config::healthTextColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerTextPosition", "Prayer number position", config::prayerTextPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarTextPosition.values()));
        add("runTextPosition", "Run energy number position", config::runTextPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarTextPosition.values()));
        add("specialTextPosition", "Special attack number position", config::specialTextPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarTextPosition.values()));
        add("prayerIconLayout", "Icons: Prayer layout", config::prayerIconLayout,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPrayerLayout.values()));
        add("prayerIconBadgeProtectionSize", "Icons: Attached overhead size", config::prayerIconBadgeProtectionSize,
            value -> new JsonPrimitive(value), value -> integerValue(value, 24, 64));
        add("prayerIconBadgeSize", "Icons: Small badge size", config::prayerIconBadgeSize,
            value -> new JsonPrimitive(value), value -> integerValue(value, 12, 28));
        add("prayerIconBadgeSpacing", "Icons: Badge spacing", config::prayerIconBadgeSpacing,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 8));
        add("prayerIconBadgeBackground", "Icons: Badge backgrounds", config::prayerIconBadgeBackground,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconBadgeBackgroundColor", "Icons: Badge background color", config::prayerIconBadgeBackgroundColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconBadgeBorderColor", "Icons: Badge border color", config::prayerIconBadgeBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconRow", "Overhead row", config::prayerIconRow,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPrayerRow.values()));
        add("prayerIconCombatRow", "Combat/misc row", config::prayerIconCombatRow,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPrayerRow.values()));
        add("prayerIconCombatPosition", "Combat/misc position", config::prayerIconCombatPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("prayerIconCombatScalePercent", "Combat/misc size", config::prayerIconCombatScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 25, 300));
        add("prayerIconCombatXOffset", "Combat/misc X offset", config::prayerIconCombatXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("prayerIconCombatYOffset", "Combat/misc Y offset", config::prayerIconCombatYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("healthTextPosition", "Health: HP number position", config::healthTextPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarTextPosition.values()));
        add("prayerVisibility", "Health: Prayer display", config::prayerVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPrayerVisibility.values()));
        add("prayerDisplay", "Health: Prayer numbers / bar", config::prayerDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("prayerPosition", "Health: Prayer position", config::prayerPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("prayerOrientation", "Health: Prayer orientation", config::prayerOrientation,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOrientation.values()));
        add("prayerScaleWithZoom", "Health: Scale prayer with zoom", config::prayerScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerWidth", "Health: Prayer bar length", config::prayerWidth,
            value -> new JsonPrimitive(value), value -> integerValue(value, 10, 200));
        add("prayerHeight", "Health: Prayer bar thickness", config::prayerHeight,
            value -> new JsonPrimitive(value), value -> integerValue(value, 2, 30));
        add("prayerXOffset", "Health: Prayer X offset", config::prayerXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("prayerYOffset", "Health: Prayer Y offset", config::prayerYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("prayerOpacity", "Health: Prayer bar opacity", config::prayerOpacity,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("prayerColor", "Health: Prayer fill", config::prayerColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerTextColor", "Health: Prayer number color", config::prayerTextColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerTextFont", "Health: Prayer number font", config::prayerTextFont,
            OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("chatPosition", "Chat: Chat position", config::chatPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("chatScaleWithZoom", "Chat: Scale chat with zoom", config::chatScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("hitsplatPosition", "Hits: Hitsplat position", config::hitsplatPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHitsplatPosition.values()));
        add("hitsplatScaleWithZoom", "Hits: Scale hitsplat with zoom", config::hitsplatScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("iconsPosition", "Icons: Icon position", config::iconsPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("prayerIconRow2BackgroundEnabled", "Icons: Row 2 background", config::prayerIconRow2BackgroundEnabled,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconRow2Shape", "Icons: Row 2 frame", config::prayerIconRow2Shape,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsIconShape.values()));
        add("prayerIconRow2Background", "Icons: Row 2 background color", config::prayerIconRow2Background,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconRow2Border", "Icons: Row 2 border color", config::prayerIconRow2Border,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconRow2TracksPoints", "Icons: Row 2 follow prayer points", config::prayerIconRow2TracksPoints,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconRow2EmptyBackground", "Icons: Row 2 empty background", config::prayerIconRow2EmptyBackground,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconBackgroundTracksPoints", "Icons: Background follows prayer points", config::prayerIconBackgroundTracksPoints,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconEmptyBackground", "Icons: Empty prayer background", config::prayerIconEmptyBackground,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconShape", "Icons: Prayer icon frame", config::prayerIconShape,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsIconShape.values()));
        add("prayerIconBackground", "Icons: Prayer icon background", config::prayerIconBackground,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconBorder", "Icons: Prayer icon border", config::prayerIconBorder,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerIconScalePercent", "Icons: Prayer icon size", config::prayerIconScalePercent,
            value -> new JsonPrimitive(value), value -> integerValue(value, 25, 300));
        add("healthNumberFormat", "Health: HP number format", config::healthNumberFormat,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsNumberFormat.values()));
        add("prayerNumberFormat", "Health: Prayer number format", config::prayerNumberFormat,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsNumberFormat.values()));
        add("prayerBarStyle", "Health: Prayer style", config::prayerBarStyle,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarStyle.values()));
        add("prayerFillDirection", "Health: Prayer fill direction", config::prayerFillDirection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarFillDirection.values()));
        add("prayerBackColor", "Health: Prayer background", config::prayerBackColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerBorderColor", "Health: Prayer border", config::prayerBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerBorderThickness", "Health: Prayer border thickness", config::prayerBorderThickness,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 10));
        add("prayerCornerRadius", "Health: Prayer corner radius", config::prayerCornerRadius,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 30));
        add("hitsplatMotion", "Hits: Movement", config::hitsplatMotion,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHitsplatMotion.values()));
        add("hitsplatScrollColumns", "Hits: Scroll columns", config::hitsplatScrollColumns,
            value -> new JsonPrimitive(value), value -> integerValue(value, 1, 4));
        add("hitsplatScrollDistance", "Hits: Scroll distance", config::hitsplatScrollDistance,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 256));
        add("hitsplatPoisonColor", "Hits: Poison background", config::hitsplatPoisonColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatVenomColor", "Hits: Venom background", config::hitsplatVenomColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatBurnColor", "Hits: Burn background", config::hitsplatBurnColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatMissNumberColor", "Hits: Miss number color", config::hitsplatMissNumberColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatStatusNumberColor", "Hits: Status number color", config::hitsplatStatusNumberColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatBorderColor", "Hits: Border color", config::hitsplatBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatBorderThickness", "Hits: Border thickness", config::hitsplatBorderThickness,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 5));
        add("hitsplatCornerRadius", "Hits: Corner radius", config::hitsplatCornerRadius,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 30));
        add("hitsplatTextShadow", "Hits: Number shadow", config::hitsplatTextShadow,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("hitsplatXOffset", "Hits: X offset", config::hitsplatXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("hitsplatYOffset", "Hits: Y offset", config::hitsplatYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -256, 256));
        add("hitsplatStyle", "Hits: Style", config::hitsplatStyle,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHitsplatStyle.values()));
        add("hitsplatDamageColor", "Hits: Damage background", config::hitsplatDamageColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatMissColor", "Hits: Miss background", config::hitsplatMissColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("hitsplatNumberColor", "Hits: Number color", config::hitsplatNumberColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("restorePreview", "Health: Food and restore preview", config::restorePreview,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("restorePreviewColor", "Health: Restore preview color", config::restorePreviewColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("prayerTickIndicator", "Health: Prayer tick indicator", config::prayerTickIndicator,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerTickColor", "Health: Prayer tick color", config::prayerTickColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("lowHealthThreshold", "Health: Low HP threshold", config::lowHealthThreshold,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("lowPrayerThreshold", "Health: Low prayer threshold", config::lowPrayerThreshold,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("lowResourceColor", "Health: Low resource color", config::lowResourceColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("npcHealthVisibility", "Health: NPC HP visibility", config::npcHealthVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsVisibility.values()));
        add("otherHealthVisibility", "Health: Other player HP visibility", config::otherHealthVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsVisibility.values()));
        add("hiddenNpcHealthIds", "Health: Hidden NPC HP IDs", config::hiddenNpcHealthIds,
            value -> new JsonPrimitive(value), value -> stringValue(value, 8192));
        add("selfNameVisibility", "Icons: My name", config::selfNameVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsVisibility.values()));
        add("playerNameVisibility", "Icons: Other player names", config::playerNameVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsVisibility.values()));
        add("npcNameVisibility", "Icons: NPC names", config::npcNameVisibility,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsVisibility.values()));
        add("namePosition", "Icons: Nameplate position", config::namePosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("nameXOffset", "Icons: Nameplate X offset", config::nameXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("nameYOffset", "Icons: Nameplate Y offset", config::nameYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("nameScaleWithZoom", "Icons: Scale nameplate with zoom", config::nameScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("statusPosition", "Icons: Status icon position", config::statusPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("statusXOffset", "Icons: Status icon X offset", config::statusXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("statusYOffset", "Icons: Status icon Y offset", config::statusYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("statusScaleWithZoom", "Icons: Scale status icon with zoom", config::statusScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("nameFont", "Icons: Name font", config::nameFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("nameColor", "Icons: Name color", config::nameColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("nameBackground", "Icons: Name background", config::nameBackground,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("showStatusIcons", "Icons: My status icons", config::showStatusIcons,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("showTargetingIcon", "Icons: NPC targeting indicator", config::showTargetingIcon,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("statusIconSize", "Icons: Status icon size", config::statusIconSize,
            value -> new JsonPrimitive(value), value -> integerValue(value, 10, 40));
        add("separatePrayerPosition", "Icons: Separate prayer position", config::separatePrayerPosition,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("prayerIconPosition", "Icons: Prayer icon position", config::prayerIconPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("prayerIconXOffset", "Icons: Prayer icon X offset", config::prayerIconXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("prayerIconYOffset", "Icons: Prayer icon Y offset", config::prayerIconYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("runDisplay", "Health: Run energy display", config::runDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("runStyle", "Health: Run energy style", config::runStyle,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarStyle.values()));
        add("runPosition", "Health: Run energy position", config::runPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("runOrientation", "Health: Run energy orientation", config::runOrientation,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOrientation.values()));
        add("runWidth", "Health: Run energy bar length", config::runWidth,
            value -> new JsonPrimitive(value), value -> integerValue(value, 10, 200));
        add("runHeight", "Health: Run energy bar thickness", config::runHeight,
            value -> new JsonPrimitive(value), value -> integerValue(value, 2, 30));
        add("runXOffset", "Health: Run energy X offset", config::runXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("runYOffset", "Health: Run energy Y offset", config::runYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("runColor", "Health: Run energy fill", config::runColor, OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("runBackColor", "Health: Run energy background", config::runBackColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("runBorderColor", "Health: Run energy border", config::runBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("runCornerRadius", "Health: Run energy corner radius", config::runCornerRadius,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 30));
        add("runOpacity", "Health: Run energy bar opacity", config::runOpacity,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("runTextColor", "Health: Run energy number color", config::runTextColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("runFont", "Health: Run energy font", config::runFont, OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("runNumberFormat", "Health: Run energy number format", config::runNumberFormat,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsNumberFormat.values()));
        add("runScaleWithZoom", "Health: Scale run energy with zoom", config::runScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("runFillDirection", "Health: Run energy fill direction", config::runFillDirection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarFillDirection.values()));
        add("specialDisplay", "Health: Special attack display", config::specialDisplay,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsBarDisplay.values()));
        add("specialStyle", "Health: Special attack style", config::specialStyle,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarStyle.values()));
        add("specialPosition", "Health: Special attack position", config::specialPosition,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsPosition.values()));
        add("specialOrientation", "Health: Special attack orientation", config::specialOrientation,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsOrientation.values()));
        add("specialWidth", "Health: Special attack bar length", config::specialWidth,
            value -> new JsonPrimitive(value), value -> integerValue(value, 10, 200));
        add("specialHeight", "Health: Special attack bar thickness", config::specialHeight,
            value -> new JsonPrimitive(value), value -> integerValue(value, 2, 30));
        add("specialXOffset", "Health: Special attack X offset", config::specialXOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("specialYOffset", "Health: Special attack Y offset", config::specialYOffset,
            value -> new JsonPrimitive(value), value -> integerValue(value, -300, 300));
        add("specialColor", "Health: Special attack fill", config::specialColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("specialBackColor", "Health: Special attack background", config::specialBackColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("specialBorderColor", "Health: Special attack border", config::specialBorderColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("specialCornerRadius", "Health: Special attack corner radius", config::specialCornerRadius,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 30));
        add("specialOpacity", "Health: Special attack bar opacity", config::specialOpacity,
            value -> new JsonPrimitive(value), value -> integerValue(value, 0, 100));
        add("specialTextColor", "Health: Special attack number color", config::specialTextColor,
            OverheadsSetupSchema::colorJson, OverheadsSetupSchema::colorValue);
        add("specialFont", "Health: Special attack font", config::specialFont,
            OverheadsSetupSchema::fontJson, OverheadsSetupSchema::fontValue);
        add("specialNumberFormat", "Health: Special attack number format", config::specialNumberFormat,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsNumberFormat.values()));
        add("specialScaleWithZoom", "Health: Scale special attack with zoom", config::specialScaleWithZoom,
            value -> new JsonPrimitive(value), OverheadsSetupSchema::booleanValue);
        add("specialFillDirection", "Health: Special attack fill direction", config::specialFillDirection,
            value -> new JsonPrimitive(value.name()), value -> enumValue(value, OverheadsHealthBarFillDirection.values()));
    }

    private <T> void add(String key, String label, Supplier<T> getter, Function<T, JsonElement> encode, Function<JsonElement, T> decode)
    {
        entries.put(key, new Entry<>(key, label, getter, encode, decode));
    }


    void addProfiles(OverheadsProfiles profiles)
    {
        for (OverheadsProfiles.Scope scope : OverheadsProfiles.Scope.values())
        {
            if (scope == OverheadsProfiles.Scope.SELF) { continue; }
            OverheadsSetupSchema source = new OverheadsSetupSchema(profiles.config(scope));
            source.entries.values().forEach(entry -> addScoped(scope, entry));
        }
    }

    static String prefix(OverheadsProfiles.Scope scope)
    {
        return scope == OverheadsProfiles.Scope.SELF ? "" : scope.name().toLowerCase(java.util.Locale.ROOT) + ".";
    }

    private <T> void addScoped(OverheadsProfiles.Scope scope, Entry<T> entry)
    {
        if (entry.key.equals("partyDisplay")) { return; }
        String key = prefix(scope) + entry.key;
        Entry<T> scoped = new Entry<>(key, scope.label + ": " + entry.label, entry.getter, entry.encode, entry.decode);
        scoped.group = scope.group;
        scoped.storageKey = entry.key;
        entries.put(key, scoped);
    }

    JsonObject snapshot()
    {
        JsonObject result = new JsonObject();
        entries.forEach((key, entry) -> { if (!key.equals("partyDisplay")) { result.add(key, entry.current()); } });
        return result;
    }

    static String stringValue(JsonElement value, int maximum)
    {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()
            || value.getAsString().length() > maximum)
        {
            throw new IllegalArgumentException("Expected text of at most " + maximum + " characters.");
        }
        return value.getAsString();
    }

    static boolean booleanValue(JsonElement value)
    {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isBoolean())
        {
            throw new IllegalArgumentException("Expected true or false.");
        }
        return value.getAsBoolean();
    }

    static double decimalValue(JsonElement value, double min, double max)
    {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isNumber())
        {
            throw new IllegalArgumentException("Expected a number.");
        }
        double number = value.getAsDouble();
        if (!Double.isFinite(number) || number < min || number > max)
        {
            throw new IllegalArgumentException("Expected a number from " + min + " to " + max + ".");
        }
        return number;
    }

    static int integerValue(JsonElement value, int min, int max)
    {
        double number = decimalValue(value, min, max);
        if (number != Math.rint(number))
        {
            throw new IllegalArgumentException("Expected a whole number.");
        }
        return (int) number;
    }

    private static <T extends Enum<T>> T enumValue(JsonElement value, T[] options)
    {
        String name = stringValue(value, 100);
        for (T option : options)
        {
            if (option.name().equals(name))
            {
                return option;
            }
        }
        throw new IllegalArgumentException("Unknown option: " + name);
    }

    private static JsonElement colorJson(Color color)
    {
        return new JsonPrimitive(String.format(java.util.Locale.ROOT, "#%08X", color.getRGB()));
    }

    private static Color colorValue(JsonElement value)
    {
        String text = stringValue(value, 9);
        if (!text.matches("#[0-9a-fA-F]{8}"))
        {
            throw new IllegalArgumentException("Expected an #AARRGGBB color.");
        }
        return new Color((int) Long.parseLong(text.substring(1), 16), true);
    }

    private static JsonElement fontJson(FontType font)
    {
        JsonObject value = new JsonObject();
        value.addProperty("family", font.getFamily());
        value.addProperty("size", font.getSize());
        value.addProperty("bold", font.isBold());
        value.addProperty("italic", font.isItalic());
        return value;
    }

    private static FontType fontValue(JsonElement value)
    {
        if (value == null || !value.isJsonObject())
        {
            throw new IllegalArgumentException("Expected a font.");
        }
        JsonObject object = value.getAsJsonObject();
        String family = stringValue(object.get("family"), 128).trim();
        if (family.isEmpty() || family.chars().anyMatch(Character::isISOControl))
        {
            throw new IllegalArgumentException("Invalid font family.");
        }
        return new FontType().withFamily(family).withSize(integerValue(object.get("size"), 6, 96))
            .withBold(booleanValue(object.get("bold"))).withItalic(booleanValue(object.get("italic")));
    }
}
