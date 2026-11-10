package com.improvedoverheads;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Window;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.swing.*;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.config.ConfigProfile;
import net.runelite.client.config.FontType;
import net.runelite.client.ui.PluginPanel;
import net.runelite.client.ui.components.ColorJButton;
import net.runelite.client.ui.components.colorpicker.ColorPickerManager;
import net.runelite.client.ui.components.colorpicker.RuneliteColorPicker;

@Singleton
final class OverheadsPanel extends PluginPanel
{
    private OverheadsConfig config;
    private OverheadsProfiles profiles;
    private OverheadsProfiles.Scope scope = OverheadsProfiles.Scope.SELF;
    private final JComboBox<OverheadsProfiles.Scope> actors = new JComboBox<>(new OverheadsProfiles.Scope[]{OverheadsProfiles.Scope.SELF, OverheadsProfiles.Scope.PARTY, OverheadsProfiles.Scope.PLAYERS, OverheadsProfiles.Scope.NPCS});
    @Inject void configureProfiles(OverheadsProfiles profiles) { this.profiles = profiles; }
    private final ConfigManager manager;
    private final ColorPickerManager colorPickers;
    @Inject private OverheadsSetups setups;
    private OverheadsSetupWindow setupWindow;
    private OverheadsSetupWindow themeWindow;
    private Runnable reflow = () -> {};
    private final JTabbedPane tabs = new JTabbedPane();
    private final List<Runnable> bindings = new ArrayList<>();
    private final List<Window> editors = new ArrayList<>();
    private final java.util.concurrent.atomic.AtomicLong editorGeneration = new java.util.concurrent.atomic.AtomicLong();
    private boolean refreshing;
    private Runnable refreshParty = () -> {};

    void setPartyRefresh(Runnable refresh) { refreshParty = refresh; }
    private final java.util.Map<JPanel, JToggleButton> sections = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, JComponent> settingRows = new java.util.LinkedHashMap<>();
    private final java.util.Map<String, Boolean> expandedSections = new java.util.HashMap<>();
    private final java.util.Map<String, OverheadsComponent> selectedComponents = new java.util.HashMap<>();
    private String sectionContext = "";

    @Inject
    OverheadsPanel(OverheadsConfig config, ConfigManager manager, ColorPickerManager colorPickers)
    {
        super(false);
        this.config = config;
        this.manager = manager;
        this.colorPickers = colorPickers;
        setLayout(new BorderLayout());
        setBackground(OverheadsPanelStyle.BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 7, 7, 7));
        OverheadsPanelStyle.tabs(tabs);
        JPanel body = new JPanel(new BorderLayout(0, 6));
        body.setOpaque(false);
        JButton sharing = new JButton("Setups");
        OverheadsPanelStyle.control(sharing);
        OverheadsPanelStyle.control(actors);
        sharing.addActionListener(event ->
        {
            if (setups == null)
            {
                return;
            }
            if (setupWindow == null || !setupWindow.isDisplayable())
            {
                editors.removeIf(window -> !window.isDisplayable());
                setupWindow = new OverheadsSetupWindow(this, setups);
                editors.add(setupWindow);
            }
            setupWindow.setVisible(true);
        });
        JPanel header = new JPanel(new BorderLayout(0, 8));
        header.setOpaque(false);
        JLabel title = new JLabel("IMPROVED OVERHEADS");
        title.setFont(OverheadsPanelStyle.FONT.deriveFont(java.awt.Font.BOLD, 13f));
        title.setForeground(OverheadsPanelStyle.TEXT);
        header.add(title, BorderLayout.NORTH);
        JPanel actions = new JPanel(new BorderLayout(5, 0));
        actions.setOpaque(false);
        actors.setToolTipText("Choose whose overhead settings to edit.");
        actions.add(actors, BorderLayout.CENTER);
        actions.add(sharing, BorderLayout.EAST);
        header.add(actions, BorderLayout.CENTER);
        actors.addActionListener(event ->
        {
            if (profiles == null || refreshing) { return; }
            closeEditors();
            scope = (OverheadsProfiles.Scope) actors.getSelectedItem();
            this.config = profiles.config(scope);
            rebuild();
        });
        body.add(header, BorderLayout.NORTH);
        body.add(tabs, BorderLayout.CENTER);
        add(body, BorderLayout.CENTER);
    }

    void rebuild()
    {
        int selected = tabs.getSelectedIndex();
        bindings.clear();
        settingRows.clear();
        tabs.removeAll();
        addTab("Chat", basic ->
        {
            toggle(basic, OverheadsConfig.OVERHEAD_CHAT_ENABLED_KEY, "Show overhead chat",
                "Draw replacement overhead chat. Filtered messages remain hidden.",
                config::overheadChatEnabled);
            toggle(basic, OverheadsConfig.SHOW_NPC_OVERHEAD_CHAT_KEY, "NPC overhead text",
                "Draw replacement overhead text for NPCs. Player overhead chat remains controlled by Show overhead chat.",
                config::showNpcOverheadChat);
            font(basic, OverheadsConfig.OVERHEAD_CHAT_FONT_KEY, "Font",
                "Font used for overhead chat. The font picker includes RuneLite, custom, and system fonts.",
                config::overheadChatFont);
            color(basic, OverheadsConfig.OVERHEAD_CHAT_COLOR_KEY, "Color",
                "Fallback RGBA color for replacement overhead chat. Recognized local color prefixes override RGB per message or glyph while retaining this alpha.",
                config::overheadChatColor);
            choice(basic, OverheadsConfig.OVERHEAD_CHAT_EFFECT_KEY, "Default Text Effect",
                "Fallback animation for replacement overhead chat. Static draws unanimated text. A matching effect prefix typed by you overrides it for that local message; other senders' native effects cannot be inherited.",
                config::overheadChatEffect, OverheadsOverheadChatEffect.values());
            toggle(basic, OverheadsConfig.OVERHEAD_CHAT_SHADOW_KEY, "Text shadow",
                "Draw a configurable shadow behind replacement overhead chat.",
                config::overheadChatShadow);
        }, advanced ->
        {
            toggle(advanced, OverheadsConfig.OVERHEAD_CHAT_RELATIONSHIP_COLORS_KEY, "Relationship colors",
                "Use separate default overhead-chat colors for group iron teammates, friends, and clan members. Your own typed color prefixes still take priority.",
                config::overheadChatRelationshipColors);
            color(advanced, OverheadsConfig.OVERHEAD_CHAT_FRIEND_COLOR_KEY, "Friend color",
                "Default RGBA overhead-chat color for players on your friends list when relationship colors are enabled.",
                config::overheadChatFriendColor);
            color(advanced, OverheadsConfig.OVERHEAD_CHAT_CLAN_COLOR_KEY, "Clan color",
                "Default RGBA overhead-chat color for members of your clan when relationship colors are enabled.",
                config::overheadChatClanColor);
            color(advanced, OverheadsConfig.OVERHEAD_CHAT_GROUP_IRON_COLOR_KEY, "Group iron color",
                "Default RGBA overhead-chat color for members of your group iron team when relationship colors are enabled.",
                config::overheadChatGroupIronColor);
            color(advanced, OverheadsConfig.OVERHEAD_CHAT_SHADOW_COLOR_KEY, "Shadow color",
                "RGBA color used for the replacement overhead-chat shadow.",
                config::overheadChatShadowColor);
            number(advanced, OverheadsConfig.OVERHEAD_CHAT_X_OFFSET_KEY, "X offset",
                "Horizontal overhead-chat offset in whole pixels.",
                config::overheadChatXOffset, -256, 256);
            number(advanced, OverheadsConfig.OVERHEAD_CHAT_Y_OFFSET_KEY, "Y offset",
                "Vertical overhead-chat offset in whole pixels. Positive values move it upward.",
                config::overheadChatYOffset, -256, 256);
            choice(advanced, "chatPosition", "Chat position",
                "Chat position.",
                config::chatPosition, OverheadsPosition.values());
            toggle(advanced, "chatScaleWithZoom", "Scale chat with zoom", "Scale chat with zoom.", config::chatScaleWithZoom);
        });
        addTab("Health", content ->
        {
            toggle(content, OverheadsConfig.HEALTH_BARS_ENABLED_KEY, "Show HP",
                "Draw replacement health bars. Native health bars are hidden while the plugin is enabled.",
                config::healthBarsEnabled);
            choice(content, OverheadsConfig.HEALTH_BAR_STYLE_KEY, "Style",
                "OSRS artwork or modern styled bars.",
                config::healthBarStyle, OverheadsHealthBarStyle.values());
            toggle(content, "alwaysShowSelfHealth", "Always show my HP",
                "Show your own current HP outside combat too.",
                config::alwaysShowSelfHealth);
            choice(content, "selfHealthDisplay", "HP display",
                "My HP display.",
                config::selfHealthDisplay, OverheadsBarDisplay.values());
            section(content, "HP position and size", group ->
            {
                choice(group, "healthPosition", "HP position",
                    "HP position.",
                    config::healthPosition, OverheadsPosition.values());
                choice(group, "healthOrientation", "HP orientation",
                    "HP orientation.",
                    config::healthOrientation, OverheadsOrientation.values());
                toggle(group, "healthScaleWithZoom", "Scale HP with zoom", "Scale HP with zoom.", config::healthScaleWithZoom);
                number(group, "playerHealthWidth", "Player bar length",
                    "Player bar length.",
                    config::playerHealthWidth, 10, 200);
                number(group, "playerHealthHeight", "Player bar thickness",
                    "Player bar thickness.",
                    config::playerHealthHeight, 2, 30);
                number(group, OverheadsConfig.HEALTH_BAR_X_OFFSET_KEY, "X offset",
                    "Horizontal health-bar offset in pixels. ",
                    config::healthBarXOffset, -256, 256);
                number(group, OverheadsConfig.HEALTH_BAR_Y_OFFSET_KEY, "Y offset",
                    "Vertical health-bar offset in pixels. Positive values move it upward.",
                    config::healthBarYOffset, -256, 256);
            });
            section(content, "Local timers", group ->
            {
                choice(group, "regenDisplay", "HP regeneration", "Estimated from the enabled Regen Meter plugin. Independent cape timing stays hidden.", config::regenDisplay, OverheadsBarDisplay.values());
                choice(group, "poisonTimerDisplay", "Poison / venom", "Estimated after an observed poison hit. Hidden until synchronized, when overdue, or cured.", config::poisonTimerDisplay, OverheadsBarDisplay.values());
            });
            section(content, "Party resources", group ->
            {
                choice(group, "partyDisplay", "Share / show party resources", "Shares your name, HP, prayer, run and special energy with your existing RuneLite party. ~ means last received. Setups do not change this option.", () -> sharingConfig().partyDisplay(), OverheadsBarDisplay.values());
                JButton sync = new JButton("Refresh party data");
                sync.addActionListener(event -> refreshParty.run());
                group.add(sync);
                number(group, "partyDataLifetime", "Data lifetime (seconds)", "Hide values after this long without an update. Run energy arrives rounded.", () -> sharingConfig().partyDataLifetime(), 5, 300);
                toggle(group, "partyShowHealth", "Show shared party HP", "Show received party data.", config::partyShowHealth);
                toggle(group, "partyShowPrayer", "Show shared party prayer", "Show received party data.", config::partyShowPrayer);
                toggle(group, "partyShowRun", "Show shared party run energy", "Show received party data.", config::partyShowRun);
                toggle(group, "partyShowSpecial", "Show shared party special energy", "Show received party data.", config::partyShowSpecial);
            });
            section(content, "Timer / party appearance", group ->
            {
                choice(group, "indicatorPosition", "Position", "Position around the actor.", config::indicatorPosition, OverheadsPosition.values());
                choice(group, "indicatorOrientation", "Orientation", "Horizontal or vertical bars.", config::indicatorOrientation, OverheadsOrientation.values());
                choice(group, "indicatorStyle", "Style", "Bar appearance.", config::indicatorStyle, OverheadsHealthBarStyle.values());
                number(group, "indicatorWidth", "Length", "Length in pixels.", config::indicatorWidth, 20, 200);
                number(group, "indicatorHeight", "Thickness", "Bar thickness in pixels.", config::indicatorHeight, 2, 30);
                number(group, "indicatorXOffset", "X offset", "Horizontal offset.", config::indicatorXOffset, -300, 300);
                number(group, "indicatorYOffset", "Y offset", "Positive moves upward.", config::indicatorYOffset, -300, 300);
                number(group, "indicatorOpacity", "Opacity", "Bars and numbers opacity.", config::indicatorOpacity, 0, 100);
                toggle(group, "indicatorScaleWithZoom", "Scale with zoom", "Resize with camera zoom.", config::indicatorScaleWithZoom);
            });
            section(content, "Spacing", group ->
            {
                toggle(group, "keepOnScreen", "Keep overheads on screen", "Shift the complete actor layout inside the viewport without separating its components.", config::keepOnScreen);
                toggle(group, "layoutPreview", "Alignment preview", "Outline your displayed components while adjusting their positions.", config::layoutPreview);
                toggle(group, "avoidOverlap", "Avoid overlaps", "Space overhead components outward on each side.", config::avoidOverlap);
                number(group, "componentGap", "Component spacing", "Minimum gap in pixels.", config::componentGap, 0, 20);
                JButton reset = new JButton("Reflow spacing");
                reset.setToolTipText("Recalculate automatic spacing while keeping your positions and offsets.");
                reset.addActionListener(event -> reflow.run());
                group.add(reset);
            });
            section(content, "HP appearance", group ->
            {
                number(group, "healthBarOpacity", "HP bar opacity",
                    "Opacity of the bar and border; numbers have their own color.",
                    config::healthBarOpacity, 0, 100);
                font(group, "healthTextFont", "HP number font", "HP number font.", config::healthTextFont);
                choice(group, "healthTextPosition", "HP number position", "Place numbers on, above or below the health bar.",
                    config::healthTextPosition, OverheadsBarTextPosition.values());
                choice(group, "healthNumberFormat", "HP number format", "Current, current / maximum, or percentage. NPC HP is estimated from its visible health bar; unknown maximums use percentages.",
                    config::healthNumberFormat, OverheadsNumberFormat.values());
                color(group, "healthTextColor", "HP number color", "HP number color.", config::healthTextColor);
                color(group, OverheadsConfig.HEALTH_BAR_FRONT_COLOR_KEY, "Front color",
                    "Primary RGBA color for the filled portion of a modern health bar.",
                    config::healthBarFrontColor);
                color(group, OverheadsConfig.HEALTH_BAR_BACK_COLOR_KEY, "Back color",
                    "Primary RGBA color for the missing-health portion of a modern health bar.",
                    config::healthBarBackColor);
                choice(group, OverheadsConfig.HEALTH_BAR_FILL_DIRECTION_KEY, "Fill direction",
                    "Direction in which the filled portion of a modern health bar grows.",
                    config::healthBarFillDirection, OverheadsHealthBarFillDirection.values());
                choice(group, OverheadsConfig.HEALTH_BAR_FRONT_GRADIENT_KEY, "Front gradient",
                    "Solid uses Front color. Horizontal and Vertical blend toward Front second color. Health based blends from the second color at empty health to the primary color at full health.",
                    config::healthBarFrontGradient, OverheadsHealthBarGradient.values());
                color(group, OverheadsConfig.HEALTH_BAR_FRONT_SECONDARY_COLOR_KEY, "Front second color",
                    "Secondary RGBA color used by non-solid front gradients.",
                    config::healthBarFrontSecondaryColor);
                choice(group, OverheadsConfig.HEALTH_BAR_FRONT_GRADIENT_COORDINATES_KEY, "Front coordinates",
                    "Relative restarts the front horizontal or vertical blend inside filled health. Absolute anchors it to the full bar so its colors stay fixed as health changes.",
                    config::healthBarFrontGradientCoordinates, OverheadsHealthBarGradientCoordinates.values());
                color(group, OverheadsConfig.HEALTH_BAR_POISONED_FRONT_COLOR_KEY, "Poisoned front color",
                    "Primary RGBA front color used for your local player's Modern health bar while poisoned or envenomed.",
                    config::healthBarPoisonedFrontColor);
                choice(group, OverheadsConfig.HEALTH_BAR_BACK_GRADIENT_KEY, "Back gradient",
                    "Solid uses Back color. Horizontal and Vertical blend toward Back second color. Health based blends from the second color at empty health to the primary color at full health.",
                    config::healthBarBackGradient, OverheadsHealthBarGradient.values());
                color(group, OverheadsConfig.HEALTH_BAR_BACK_SECONDARY_COLOR_KEY, "Back second color",
                    "Secondary RGBA color used by non-solid back gradients.",
                    config::healthBarBackSecondaryColor);
                choice(group, OverheadsConfig.HEALTH_BAR_BACK_GRADIENT_COORDINATES_KEY, "Back coordinates",
                    "Relative restarts the back horizontal or vertical blend inside missing health. Absolute anchors it to the full bar so its colors stay fixed as health changes.",
                    config::healthBarBackGradientCoordinates, OverheadsHealthBarGradientCoordinates.values());
                color(group, OverheadsConfig.HEALTH_BAR_BORDER_COLOR_KEY, "Border color",
                    "RGBA color for the border around a modern health bar.",
                    config::healthBarBorderColor);
                decimal(group, OverheadsConfig.HEALTH_BAR_BORDER_THICKNESS_KEY, "Border thickness",
                    "Modern health-bar border thickness in pixels. Decimal values allow sub-pixel borders; set to 0 for no border.",
                    config::healthBarBorderThickness, 0, 10);
                decimal(group, OverheadsConfig.HEALTH_BAR_CORNER_RADIUS_KEY, "Corner radius",
                    "Antialiased Modern health-bar corner radius in pixels, limited by half the bar's smaller dimension. Decimal values allow sub-pixel curves.",
                    config::healthBarCornerRadius, 0, 50);
                toggle(group, OverheadsConfig.HEALTH_BAR_DAMAGE_TRAIL_ENABLED_KEY, "Damage trail",
                    "Hold recently lost health, then animate the trail down to the current Modern health-bar value. Healing updates immediately.",
                    config::healthBarDamageTrailEnabled);
                color(group, OverheadsConfig.HEALTH_BAR_DAMAGE_TRAIL_COLOR_KEY, "Damage trail color",
                    "RGBA color for recently lost health in a Modern health bar.",
                    config::healthBarDamageTrailColor);
                number(group, OverheadsConfig.HEALTH_BAR_DAMAGE_TRAIL_HOLD_KEY, "Damage trail hold",
                    "How long recently lost health remains stationary before draining.",
                    config::healthBarDamageTrailHold, 0, 5000);
                number(group, OverheadsConfig.HEALTH_BAR_DAMAGE_TRAIL_DRAIN_KEY, "Damage trail drain",
                    "How long the held damage trail takes to animate down to current health.",
                    config::healthBarDamageTrailDrain, 0, 5000);
                toggle(group, OverheadsConfig.HEALTH_BAR_SEGMENTS_ENABLED_KEY, "HP segments",
                    "Draw League-style dividers every configured hitpoints. Exact values are available for your player and NPCs covered by RuneLite's NPC stats; the selected mode controls unknown actors.",
                    config::healthBarSegmentsEnabled);
                choice(group, OverheadsConfig.HEALTH_BAR_SEGMENT_VALUE_MODE_KEY, "Segment HP source",
                    "Exact HP uses your real Hitpoints level or RuneLite NPC stats. Public fallback keeps segments visible for actors whose maximum HP is unknown, but those fallback units are normalized public health-scale units rather than hitpoints.",
                    config::healthBarSegmentValueMode, OverheadsHealthBarSegmentValueMode.values());
                number(group, OverheadsConfig.HEALTH_BAR_HITPOINTS_PER_SEGMENT_KEY, "HP per segment",
                    "Draw one divider for each multiple of this many hitpoints. In Public scale mode or when using its fallback, the same number is measured in normalized public health-scale units.",
                    config::healthBarHitpointsPerSegment, 1, 10000);
                color(group, OverheadsConfig.HEALTH_BAR_SEGMENT_COLOR_KEY, "Segment color",
                    "RGBA color for health-segment dividers.",
                    config::healthBarSegmentColor);
                decimal(group, OverheadsConfig.HEALTH_BAR_SEGMENT_THICKNESS_KEY, "Segment thickness",
                    "Thickness of Modern health-bar segment dividers in pixels. Decimal values allow sub-pixel lines.",
                    config::healthBarSegmentThickness, 0, 10);
            });
            section(content, "NPCs and other players", group ->
            {
                text(group, "nativeNpcUiIds", "Native NPC UI IDs", "Keep complete native overheads for these NPC IDs, including transformations.", config::nativeNpcUiIds);
                choice(group, "npcHealthVisibility", "NPC HP visibility",
                    "Restrict which NPC HP bars and numbers are shown.", config::npcHealthVisibility, OverheadsVisibility.values());
                choice(group, "otherHealthVisibility", "HP visibility",
                    "Restrict which other player HP bars and numbers are shown.", config::otherHealthVisibility, OverheadsVisibility.values());
                text(group, "hiddenNpcHealthIds", "Hidden NPC HP IDs",
                    "Comma-separated NPC IDs whose ordinary HP display should be hidden. Special encounter bars remain native.", config::hiddenNpcHealthIds);

                choice(group, "npcHealthDisplay", "HP display",
                    "NPC numbers show the visible HP percentage.",
                    config::npcHealthDisplay, OverheadsBarDisplay.values());
                choice(group, "otherHealthDisplay", "HP display",
                    "Ordinary player numbers use visible HP percentages. Shared party HP uses the received value.",
                    config::otherHealthDisplay, OverheadsBarDisplay.values());
                decimal(group, OverheadsConfig.HEALTH_BAR_SOLID_WIDTH_KEY, "NPC bar length",
                    "Length of the NPC health bar in pixels. ",
                    config::healthBarSolidWidth, 10, 200);
                decimal(group, OverheadsConfig.HEALTH_BAR_HEIGHT_KEY, "NPC bar thickness",
                    "NPC bar thickness in pixels. ",
                    config::healthBarHeight, 2, 20);
                choice(group, OverheadsConfig.HEALTH_BAR_SCALE_MODE_KEY, "Scale mode",
                    "Controls boss sizing. Fixed uses Scale for both dimensions; Threshold switches to the boss width and height scales at the configured threshold; Dynamic interpolates each dimension independently. Public health scale is not actual maximum HP.",
                    config::healthBarScaleMode, OverheadsHealthScaleMode.values());
                number(group, OverheadsConfig.HEALTH_BAR_SCALE_PERCENT_KEY, "Scale",
                    "Fixed health-bar size multiplier, or the starting multiplier used by Threshold and Dynamic modes.",
                    config::healthBarScalePercent, 50, 200);
                number(group, OverheadsConfig.HEALTH_BAR_SCALE_THRESHOLD_KEY, "Boss scale threshold",
                    "Public actor health scale that activates the boss width and height scales in Threshold mode or reaches them in Dynamic mode. This is not actual maximum HP.",
                    config::healthBarScaleThreshold, 100, 255);
                number(group, OverheadsConfig.HEALTH_BAR_LARGE_SCALE_PERCENT_KEY, "Boss width scale",
                    "Health-bar width multiplier used at or above the configured public health scale threshold.",
                    config::healthBarLargeScalePercent, 50, 200);
                number(group, OverheadsConfig.HEALTH_BAR_LARGE_HEIGHT_SCALE_PERCENT_KEY, "Boss height scale",
                    "Health-bar height multiplier used at or above the configured public health scale threshold.",
                    config::healthBarLargeHeightScalePercent, 50, 200);
            });
            section(content, "Prayer", group ->
            {
                toggle(group, "prayerRestorePreview", "Prayer restore preview", "Preview prayer restored by the inventory item under the pointer.", config::prayerRestorePreview);
                color(group, "prayerRestorePreviewColor", "Prayer restore color", "Color of the projected prayer restore.", config::prayerRestorePreviewColor);
                color(group, "prayerLowColor", "Low prayer color", "Warning color for low prayer.", config::prayerLowColor);
                choice(group, "prayerVisibility", "Prayer display",
                    "Prayer display.",
                    config::prayerVisibility, OverheadsPrayerVisibility.values());
                choice(group, "prayerDisplay", "Prayer numbers / bar",
                    "Prayer numbers / bar.",
                    config::prayerDisplay, OverheadsBarDisplay.values());
                choice(group, "prayerBarStyle", "Prayer style", "OSRS artwork or Modern prayer bar.",
                    config::prayerBarStyle, OverheadsHealthBarStyle.values());
                choice(group, "prayerNumberFormat", "Prayer number format", "Current, current / maximum, or percentage.",
                    config::prayerNumberFormat, OverheadsNumberFormat.values());
                choice(group, "prayerPosition", "Prayer position",
                    "Prayer position.",
                    config::prayerPosition, OverheadsPosition.values());
                choice(group, "prayerOrientation", "Prayer orientation",
                    "Prayer orientation.",
                    config::prayerOrientation, OverheadsOrientation.values());
                toggle(group, "prayerScaleWithZoom", "Scale prayer with zoom",
                    "Scale prayer with zoom.",
                    config::prayerScaleWithZoom);
                number(group, "prayerWidth", "Prayer bar length", "Prayer bar length.", config::prayerWidth, 10, 200);
                number(group, "prayerHeight", "Prayer bar thickness", "Prayer bar thickness.", config::prayerHeight, 2, 30);
                number(group, "prayerXOffset", "Prayer X offset", "Prayer X offset.", config::prayerXOffset, -300, 300);
                number(group, "prayerYOffset", "Prayer Y offset", "Prayer Y offset.", config::prayerYOffset, -300, 300);
                number(group, "prayerOpacity", "Prayer bar opacity",
                    "Opacity of the prayer bar and border; numbers have their own color.",
                    config::prayerOpacity, 0, 100);
                color(group, "prayerColor", "Prayer fill", "Prayer fill.", config::prayerColor);
                color(group, "prayerBackColor", "Prayer background", "Modern prayer bar background.", config::prayerBackColor);
                color(group, "prayerBorderColor", "Prayer border", "Modern prayer bar border.", config::prayerBorderColor);
                number(group, "prayerBorderThickness", "Prayer border thickness", "Modern border thickness.",
                    config::prayerBorderThickness, 0, 10);
                number(group, "prayerCornerRadius", "Prayer corner radius", "Modern corner radius.",
                    config::prayerCornerRadius, 0, 30);
                choice(group, "prayerFillDirection", "Prayer fill direction", "Direction the filled prayer bar grows.",
                    config::prayerFillDirection, OverheadsHealthBarFillDirection.values());
                choice(group, "prayerTextPosition", "Prayer number position", "Place numbers on, above or below this bar.",
                    config::prayerTextPosition, OverheadsBarTextPosition.values());
                color(group, "prayerTextColor", "Prayer number color", "Prayer number color.", config::prayerTextColor);
                font(group, "prayerTextFont", "Prayer number font", "Prayer number font.", config::prayerTextFont);
            });
                toggle(content, "restorePreview", "Health restore preview",
                    "Preview the actual HP restored by the inventory item under the pointer.", config::restorePreview);
            section(content, "Restore previews and warnings", group ->
            {
                color(group, "restorePreviewColor", "Restore preview color",
                    "Color of the projected restored portion.", config::restorePreviewColor);
                number(group, "lowHealthThreshold", "Low HP threshold",
                    "Use the warning color below this percentage of your HP. Zero disables it.", config::lowHealthThreshold, 0, 100);
                number(group, "lowPrayerThreshold", "Low prayer threshold",
                    "Use the warning color below this percentage of your prayer. Zero disables it.", config::lowPrayerThreshold, 0, 100);
                color(group, "lowResourceColor", "Low health color",
                    "Warning color for your low HP.", config::lowResourceColor);
                toggle(group, "prayerTickIndicator", "Prayer tick indicator",
                    "Sweep once per observed game tick and flash when your prayer points decrease. This does not predict the next drain.", config::prayerTickIndicator);
                color(group, "prayerTickColor", "Prayer tick color",
                    "Color of the tick marker.", config::prayerTickColor);
            });
            section(content, "Run energy", group ->
            {
                toggle(group, "runHideWhenFull", "Hide when full",
                    "Hide your run energy numbers and bar while completely full.", config::runHideWhenFull);
                choice(group, "runDisplay", "Run energy display",
                    "Show your own resource as a bar, numbers, both, or hide it.", config::runDisplay, OverheadsBarDisplay.values());
                choice(group, "runStyle", "Run energy style",
                    "OSRS artwork or Modern appearance.", config::runStyle, OverheadsHealthBarStyle.values());
                choice(group, "runPosition", "Run energy position",
                    "Placement relative to your player.", config::runPosition, OverheadsPosition.values());
                choice(group, "runOrientation", "Run energy orientation",
                    "Horizontal or vertical bar.", config::runOrientation, OverheadsOrientation.values());
                number(group, "runWidth", "Run energy bar length",
                    "Bar length in pixels.", config::runWidth, 10, 200);
                number(group, "runHeight", "Run energy bar thickness",
                    "Bar thickness in pixels.", config::runHeight, 2, 30);
                number(group, "runXOffset", "Run energy X offset",
                    "Horizontal offset in pixels.", config::runXOffset, -300, 300);
                number(group, "runYOffset", "Run energy Y offset",
                    "Vertical offset. Positive moves upward.", config::runYOffset, -300, 300);
                color(group, "runColor", "Run energy fill",
                    "Filled bar color.", config::runColor);
                color(group, "runBackColor", "Run energy background",
                    "Modern missing-resource color.", config::runBackColor);
                color(group, "runBorderColor", "Run energy border",
                    "Modern bar border color.", config::runBorderColor);
                number(group, "runCornerRadius", "Run energy corner radius",
                    "Modern bar corner radius.", config::runCornerRadius, 0, 30);
                number(group, "runOpacity", "Run energy bar opacity",
                    "Opacity of the bar and border; numbers have their own color.", config::runOpacity, 0, 100);
                choice(group, "runTextPosition", "Run energy number position", "Place numbers on, above or below this bar.",
                    config::runTextPosition, OverheadsBarTextPosition.values());
                color(group, "runTextColor", "Run energy number color",
                    "Resource number color.", config::runTextColor);
                font(group, "runFont", "Run energy font",
                    "Resource number font.", config::runFont);
                choice(group, "runNumberFormat", "Run energy number format",
                    "Current, current / maximum, or percentage.", config::runNumberFormat, OverheadsNumberFormat.values());
                toggle(group, "runScaleWithZoom", "Scale run energy with zoom",
                    "Scale with camera zoom.", config::runScaleWithZoom);
                choice(group, "runFillDirection", "Run energy fill direction",
                    "Direction the resource bar grows.", config::runFillDirection, OverheadsHealthBarFillDirection.values());
            });
            section(content, "Special attack", group ->
            {
                toggle(group, "specialHideWhenFull", "Hide when full",
                    "Hide your special energy numbers and bar while completely full.", config::specialHideWhenFull);
                choice(group, "specialDisplay", "Special attack display",
                    "Show your own resource as a bar, numbers, both, or hide it.", config::specialDisplay, OverheadsBarDisplay.values());
                choice(group, "specialStyle", "Special attack style",
                    "OSRS artwork or Modern appearance.", config::specialStyle, OverheadsHealthBarStyle.values());
                choice(group, "specialPosition", "Special attack position",
                    "Placement relative to your player.", config::specialPosition, OverheadsPosition.values());
                choice(group, "specialOrientation", "Special attack orientation",
                    "Horizontal or vertical bar.", config::specialOrientation, OverheadsOrientation.values());
                number(group, "specialWidth", "Special attack bar length",
                    "Bar length in pixels.", config::specialWidth, 10, 200);
                number(group, "specialHeight", "Special attack bar thickness",
                    "Bar thickness in pixels.", config::specialHeight, 2, 30);
                number(group, "specialXOffset", "Special attack X offset",
                    "Horizontal offset in pixels.", config::specialXOffset, -300, 300);
                number(group, "specialYOffset", "Special attack Y offset",
                    "Vertical offset. Positive moves upward.", config::specialYOffset, -300, 300);
                color(group, "specialColor", "Special attack fill",
                    "Filled bar color.", config::specialColor);
                color(group, "specialBackColor", "Special attack background",
                    "Modern missing-resource color.", config::specialBackColor);
                color(group, "specialBorderColor", "Special attack border",
                    "Modern bar border color.", config::specialBorderColor);
                number(group, "specialCornerRadius", "Special attack corner radius",
                    "Modern bar corner radius.", config::specialCornerRadius, 0, 30);
                number(group, "specialOpacity", "Special attack bar opacity",
                    "Opacity of the bar and border; numbers have their own color.", config::specialOpacity, 0, 100);
                choice(group, "specialTextPosition", "Special attack number position", "Place numbers on, above or below this bar.",
                    config::specialTextPosition, OverheadsBarTextPosition.values());
                color(group, "specialTextColor", "Special attack number color",
                    "Resource number color.", config::specialTextColor);
                font(group, "specialFont", "Special attack font",
                    "Resource number font.", config::specialFont);
                choice(group, "specialNumberFormat", "Special attack number format",
                    "Current, current / maximum, or percentage.", config::specialNumberFormat, OverheadsNumberFormat.values());
                toggle(group, "specialScaleWithZoom", "Scale special attack with zoom",
                    "Scale with camera zoom.", config::specialScaleWithZoom);
                choice(group, "specialFillDirection", "Special attack fill direction",
                    "Direction the resource bar grows.", config::specialFillDirection, OverheadsHealthBarFillDirection.values());
            });
        });
        addTab("Hits", basic ->
        {
            JButton themes = new JButton("Hitsplat themes");
            themes.addActionListener(event ->
            {
                if (setups == null)
                {
                    return;
                }
                if (themeWindow == null || !themeWindow.isDisplayable())
                {
                    editors.removeIf(window -> !window.isDisplayable());
                    setups.setThemeScope(scope);
                    themeWindow = new OverheadsSetupWindow(this, setups, true);
                    editors.add(themeWindow);
                }
                themeWindow.setVisible(true);
            });
            basic.add(themes);
            choice(basic, "hitsplatMotion", "Movement", "Fixed arrangement or scrolling columns.",
                config::hitsplatMotion, OverheadsHitsplatMotion.values());
            choice(basic, "hitsplatStyle", "Style", "Damage and miss hitsplat appearance.",
                config::hitsplatStyle, OverheadsHitsplatStyle.values());
            number(basic, OverheadsConfig.MAX_HITSPLATS_KEY, "Max hitsplats",
                "Maximum hitsplats kept per actor, up to 128.",
                config::maxHitsplats, 0, 128);
            toggle(basic, OverheadsConfig.ONLY_DISPLAY_MINE_KEY, "My hits only",
                "Only draw hits marked as yours by the game. Ownership is never inferred from XP.",
                config::onlyDisplayMine);
            toggle(basic, OverheadsConfig.HIDE_ZERO_HITSPLATS_KEY, "Hide zero hits",
                "Do not draw misses or other zero-amount replacement hitsplats.",
                config::hideZeroHitsplats);
            choice(basic, OverheadsConfig.LAYOUT_SHAPE_KEY, "Shape",
                "Hexagonal uses six-sided rings. Diamond uses four-corner rings. Grid uses square rings. X uses diagonal arms.",
                config::layoutShape, OverheadsLayoutShape.values());
            number(basic, OverheadsConfig.HITSPLAT_SCALE_PERCENT_KEY, "Scale",
                "Scale the complete replacement hitsplat, including sprites, text, spacing, and layout geometry.",
                config::hitsplatScalePercent, 50, 200);
        }, advanced ->
        {
            toggle(advanced, OverheadsConfig.DISABLE_ENEMY_HITSPLATS_KEY, "Hide NPC hits",
                "Do not draw replacement hitsplats for NPCs. ",
                config::disableEnemyHitsplats);
            toggle(advanced, OverheadsConfig.DISABLE_ALLY_HITSPLATS_KEY, "Hide other players' hits",
                "Do not draw replacement hitsplats for other players. ",
                config::disableAllyHitsplats);
            toggle(advanced, OverheadsConfig.DISABLE_MY_HITSPLATS_KEY, "Hide hits on my player",
                "Do not draw replacement hitsplats on your player. ",
                config::disableMyHitsplats);
            toggle(advanced, OverheadsConfig.REUSE_OLD_HITSPLAT_SLOTS_KEY, "Reuse old slots",
                "Allow each new hitsplat to replace the oldest hitsplat from an earlier reuse window, even when an empty slot exists. Hits in the same window still use separate slots.",
                config::reuseOldHitsplatSlots);
            number(advanced, OverheadsConfig.HITSPLAT_REUSE_INTERVAL_KEY, "Reuse interval",
                "Minimum age before a hitsplat slot can be reused. 550 ms is approximately one server tick with tolerance for timing differences.",
                config::hitsplatReuseInterval, 50, 60000);
            number(advanced, OverheadsConfig.OPACITY_PERCENT_KEY, "Opacity",
                "Opacity of replacement hitsplats. 100 is fully opaque.",
                config::opacityPercent, 0, 100);
            toggle(advanced, OverheadsConfig.PRIORITIZE_MINE_KEY, "Prioritize my hits",
                "When replacement hitsplats are limited, a new hit marked as yours replaces the oldest hit not marked as yours first.",
                config::prioritizeMine);
            choice(advanced, OverheadsConfig.LAYOUT_DIRECTION_KEY, "Direction",
                "Clockwise starts at top and moves right. Counterclockwise starts at top and moves left.",
                config::layoutDirection, OverheadsLayoutDirection.values());
            choice(advanced, OverheadsConfig.LAYOUT_BEHAVIOR_KEY, "Behavior",
                "Incremental fills slots in order. Symmetrical alternates opposite slots. Random chooses an open slot from the current radius.",
                config::layoutBehavior, OverheadsLayoutBehavior.values());
            number(advanced, OverheadsConfig.MIN_RADIUS_KEY, "Min radius",
                "First layout radius for detected boss-like targets. Smaller targets always start at the center. 0 starts at the center; 1 starts on the first ring.",
                config::minRadius, 0, 64);
            number(advanced, OverheadsConfig.MAX_RADIUS_KEY, "Max radius",
                "Maximum layout radius for detected boss-like targets. Smaller targets are uncapped. 0 means no cap; 1 means center only; 2 means center plus the first ring.",
                config::maxRadius, 0, 64);
            number(advanced, OverheadsConfig.X_SPACING_KEY, "X spacing",
                "Extra horizontal pixels between adjacent hitsplat slots. Negative values pull slots closer together.",
                config::xSpacing, -64, 64);
            number(advanced, OverheadsConfig.Y_SPACING_KEY, "Y spacing",
                "Extra vertical pixels between adjacent hitsplat slots. Negative values pull slots closer together.",
                config::ySpacing, -64, 64);
            section(advanced, "Appearance", appearance ->
            {
                font(appearance, OverheadsConfig.HITSPLAT_FONT_KEY, "Font",
                    "Font used for hitsplat amounts. The font picker includes RuneLite, custom, and system fonts.",
                    config::hitsplatFont);
                color(appearance, "hitsplatDamageColor", "Damage background", "Modern damage hitsplat background.",
                    config::hitsplatDamageColor);
                color(appearance, "hitsplatMissColor", "Miss background", "Modern miss hitsplat background.",
                    config::hitsplatMissColor);
                color(appearance, "hitsplatNumberColor", "Number color", "Modern and Numbers only text color.",
                    config::hitsplatNumberColor);
                number(appearance, "hitsplatScrollColumns", "Scroll columns", "Number of columns for scrolling hitsplats.",
                    config::hitsplatScrollColumns, 1, 4);
                number(appearance, "hitsplatScrollDistance", "Scroll distance", "Pixels travelled over each hitsplat lifetime, before zoom scaling.",
                    config::hitsplatScrollDistance, 0, 256);
                color(appearance, "hitsplatPoisonColor", "Poison background", "Modern poison hitsplat background.",
                    config::hitsplatPoisonColor);
                color(appearance, "hitsplatVenomColor", "Venom background", "Modern venom hitsplat background.",
                    config::hitsplatVenomColor);
                color(appearance, "hitsplatBurnColor", "Burn background", "Modern burn hitsplat background.",
                    config::hitsplatBurnColor);
                color(appearance, "hitsplatMissNumberColor", "Miss number color", "Modern and Numbers only miss text color.",
                    config::hitsplatMissNumberColor);
                color(appearance, "hitsplatStatusNumberColor", "Status number color", "Modern and Numbers only poison, venom and burn text color.",
                    config::hitsplatStatusNumberColor);
                color(appearance, "hitsplatBorderColor", "Border color", "Modern hitsplat border color.",
                    config::hitsplatBorderColor);
                number(appearance, "hitsplatBorderThickness", "Border thickness", "Modern hitsplat border width. Zero hides the border.",
                    config::hitsplatBorderThickness, 0, 5);
                number(appearance, "hitsplatCornerRadius", "Corner radius", "Modern hitsplat corner roundness. Zero gives square corners.",
                    config::hitsplatCornerRadius, 0, 30);
                toggle(appearance, "hitsplatTextShadow", "Number shadow", "Draw a shadow behind Modern and Numbers only hitsplat text.",
                    config::hitsplatTextShadow);
                number(appearance, "hitsplatXOffset", "X offset", "Horizontal offset for every replacement hitsplat group.",
                    config::hitsplatXOffset, -256, 256);
                number(appearance, "hitsplatYOffset", "Y offset", "Vertical offset for every replacement hitsplat group. Positive moves upward.",
                    config::hitsplatYOffset, -256, 256);
            });
            choice(advanced, OverheadsConfig.LARGE_TARGET_DETECTION_KEY, "Boss-like detection",
                "Decide which actors receive configured radius limits and target offsets. RuneLite does not expose a reliable boss flag, so Either combines footprint and public health scale.",
                config::largeTargetDetection, OverheadsTargetDetection.values());
            number(advanced, OverheadsConfig.LARGE_TARGET_SIZE_KEY, "Footprint threshold",
                "Minimum actor footprint used by boss-like detection. 2 means targets at least 2x2 tiles.",
                config::largeTargetSize, 1, 16);
            number(advanced, OverheadsConfig.LARGE_TARGET_HEALTH_SCALE_KEY, "Boss health-scale threshold",
                "Minimum public actor health scale used by boss-like detection. This is not actual maximum HP; standard actors commonly use 30.",
                config::largeTargetHealthScale, 100, 255);
            number(advanced, OverheadsConfig.LARGE_TARGET_X_OFFSET_KEY, "Boss-like X offset",
                "Extra horizontal hitsplat offset for detected boss-like targets.",
                config::largeTargetXOffset, -256, 256);
            number(advanced, OverheadsConfig.LARGE_TARGET_Y_OFFSET_KEY, "Boss-like Y offset",
                "Extra vertical hitsplat offset for detected boss-like targets. Positive values move hitsplats upward.",
                config::largeTargetYOffset, -256, 256);
            number(advanced, OverheadsConfig.FADE_IN_DURATION_KEY, "Fade-in duration",
                "How long replacement hitsplats take to appear. Total lifetime is fade-in + full opacity + fade-out.",
                config::fadeInDuration, 0, 60000);
            number(advanced, OverheadsConfig.FULL_OPACITY_DURATION_KEY, "Full opacity duration",
                "How long replacement hitsplats stay fully visible after fading in.",
                config::fullOpacityDuration, 0, 60000);
            number(advanced, OverheadsConfig.FADE_OUT_DURATION_KEY, "Fade-out duration",
                "How long replacement hitsplats take to disappear after the full-opacity duration.",
                config::fadeOutDuration, 0, 60000);
            choice(advanced, "hitsplatPosition", "Hitsplat position",
                "Native anchors hitsplats on the target without overlap repositioning. Styling, layout and offsets still apply.",
                config::hitsplatPosition, OverheadsHitsplatPosition.values());
            toggle(advanced, "hitsplatScaleWithZoom", "Scale hitsplat with zoom",
                "Scale hitsplat with zoom.",
                config::hitsplatScaleWithZoom);
        });
        addTab("Icons", basic ->
        {
            toggle(basic, OverheadsConfig.HEAD_ICONS_ENABLED_KEY, "Show head icons",
                "Draw replacement head icons. Native head icons are hidden while the plugin is enabled.",
                config::headIconsEnabled);
            toggle(basic, OverheadsConfig.SHOW_PRAYER_ICONS_KEY, "Prayer icons",
                "Show player overhead-prayer icons.",
                config::showPrayerIcons);
            toggle(basic, OverheadsConfig.SHOW_SKULL_ICONS_KEY, "Skull icons",
                "Show player skull icons.",
                config::showSkullIcons);
            toggle(basic, OverheadsConfig.SHOW_NPC_ICONS_KEY, "NPC icons", "Show NPC overhead icons.", config::showNpcIcons);
            number(basic, OverheadsConfig.HEAD_ICON_SCALE_PERCENT_KEY, "Scale",
                "Base head-icon scale as a percentage. Zoom scaling is applied on top when enabled.",
                config::headIconScalePercent, 50, 200);
        }, advanced ->
        {
            choice(advanced, OverheadsConfig.SPRITE_SCALING_MODE_KEY, "Sprite scaling",
                "Resize hitsplats, RuneScape health bars, and head icons with Nearest, Bilinear, Bicubic, or edge-directed xBR. xBR is used for enlarging sprites and smoothly reduced to fractional target sizes.",
                config::spriteScalingMode, OverheadsSpriteScalingMode.values());
            toggle(advanced, "prayerIconSymbolsOnly", "Prayer symbols only",
                "Remove original overhead backings and added row/corner badge backgrounds, keeping the symbols and resource-pack artwork. Off restores your saved backgrounds.",
                config::prayerIconSymbolsOnly);
            toggle(advanced, OverheadsConfig.SHOW_PRAYER_ICON_BACKGROUND_KEY, "Prayer background",
                "Draw the background behind supported Resource Pack overhead-prayer textures. Native prayer icons already include their background.",
                config::showPrayerIconBackground);
            toggle(advanced, OverheadsConfig.SHOW_HINT_ARROWS_KEY, "Hint arrows",
                "Show actor hint arrows.",
                config::showHintArrows);
            toggle(advanced, OverheadsConfig.HEAD_ICON_SCALE_WITH_ZOOM_KEY, "Scale with zoom",
                "Adjust head-icon size with camera zoom so icons stay proportional to the game world.",
                config::headIconScaleWithZoom);
            number(advanced, OverheadsConfig.HEAD_ICON_X_OFFSET_KEY, "X offset",
                "Horizontal head-icon offset in pixels.",
                config::headIconXOffset, -256, 256);
            number(advanced, OverheadsConfig.HEAD_ICON_Y_OFFSET_KEY, "Y offset",
                "Vertical head-icon offset in pixels. Positive values move icons upward.",
                config::headIconYOffset, -256, 256);
            number(advanced, OverheadsConfig.HEAD_ICON_Z_OFFSET_KEY, "Z offset",
                "Head-icon height offset in world units, following camera zoom and angle. Positive values raise icons. Applied in addition to X and Y pixel offsets.",
                config::headIconZOffset, -1024, 1024);
            number(advanced, OverheadsConfig.HEAD_ICON_SPACING_KEY, "Spacing",
                "Gap below the first head icon and between each stacked icon, in pixels.",
                config::headIconSpacing, 0, 20);
            choice(advanced, "iconsPosition", "Icon position",
                "Icon position.",
                config::iconsPosition, OverheadsPosition.values());
            choice(advanced, "prayerIconShape", "Prayer icon frame",
                "Prayer icon frame.",
                config::prayerIconShape, OverheadsIconShape.values());
            toggle(advanced, "prayerIconBackgroundTracksPoints", "Background follows prayer points",
                "Drain and refill the background with your prayer points or available shared party prayer.", config::prayerIconBackgroundTracksPoints);
            color(advanced, "prayerIconEmptyBackground", "Empty prayer background",
                "Color of the empty portion of the prayer icon background.", config::prayerIconEmptyBackground);
            color(advanced, "prayerIconBackground", "Prayer icon background",
                "Prayer icon background.",
                config::prayerIconBackground);
            color(advanced, "prayerIconBorder", "Prayer icon border", "Prayer icon border.", config::prayerIconBorder);
            toggle(advanced, "prayerIconRow2BackgroundEnabled", "Background", "Show the background for prayer icons assigned to Row 2.", config::prayerIconRow2BackgroundEnabled);
            choice(advanced, "prayerIconRow2Shape", "Frame", "Frame shape for Row 2 prayer icons.", config::prayerIconRow2Shape, OverheadsIconShape.values());
            color(advanced, "prayerIconRow2Background", "Background color", "Background color and opacity for Row 2.", config::prayerIconRow2Background);
            color(advanced, "prayerIconRow2Border", "Border color", "Border color and opacity for Row 2.", config::prayerIconRow2Border);
            toggle(advanced, "prayerIconRow2TracksPoints", "Follow prayer points", "Drain and refill Row 2 backgrounds with your prayer points or available shared party prayer.", config::prayerIconRow2TracksPoints);
            color(advanced, "prayerIconRow2EmptyBackground", "Empty background", "Empty background color and opacity for Row 2.", config::prayerIconRow2EmptyBackground);
            choice(advanced, "prayerIconLayout", "Prayer layout", "Attach small secondary prayers to a visible overhead prayer; otherwise use the configured regular row.", config::prayerIconLayout, OverheadsPrayerLayout.values());
            number(advanced, "prayerIconBadgeProtectionSize", "Attached overhead size", "Overhead prayer icon size in pixels in the attached layout, before overall icon and zoom scaling.", config::prayerIconBadgeProtectionSize, 24, 64);
            number(advanced, "prayerIconBadgeSize", "Small badge size", "Secondary icon size in pixels in the attached layout, before overall icon and zoom scaling.", config::prayerIconBadgeSize, 12, 28);
            number(advanced, "prayerIconBadgeSpacing", "Badge spacing", "Extra spacing between the overhead prayer icon and corner badges.", config::prayerIconBadgeSpacing, 0, 8);
            toggle(advanced, "prayerIconBadgeBackground", "Badge backgrounds", "Show a round background and border behind each small badge. Regular rows and overhead prayer backgrounds are unchanged.", config::prayerIconBadgeBackground);
            color(advanced, "prayerIconBadgeBackgroundColor", "Badge background color", "Background color and opacity of attached secondary badges.", config::prayerIconBadgeBackgroundColor);
            color(advanced, "prayerIconBadgeBorderColor", "Badge border color", "Border color and opacity of attached secondary badges.", config::prayerIconBadgeBorderColor);
            number(advanced, "prayerIconScalePercent", "Overhead size",
                "Prayer icon size.",
                config::prayerIconScalePercent, 25, 300);
                toggle(advanced, "localBoostPrayerIcons", "My combat/misc prayers", "Your active combat and miscellaneous prayers.", config::localBoostPrayerIcons);
                choice(advanced, "prayerIconRow", "Overhead row", "Row 1 is above Row 2 when prayer groups share a position.", config::prayerIconRow, OverheadsPrayerRow.values());
                choice(advanced, "prayerIconCombatRow", "Combat/misc row", "Icons on the same row are arranged horizontally.", config::prayerIconCombatRow, OverheadsPrayerRow.values());
                choice(advanced, "prayerIconCombatPosition", "Combat/misc position", "Position of combat and miscellaneous prayer icons.", config::prayerIconCombatPosition, OverheadsPosition.values());
                number(advanced, "prayerIconCombatScalePercent", "Combat/misc size", "Size of combat and miscellaneous prayer icons in percent.", config::prayerIconCombatScalePercent, 25, 300);
                number(advanced, "prayerIconCombatXOffset", "Combat/misc X offset", "Horizontal offset in pixels.", config::prayerIconCombatXOffset, -300, 300);
                number(advanced, "prayerIconCombatYOffset", "Combat/misc Y offset", "Vertical offset in pixels. Positive moves upward.", config::prayerIconCombatYOffset, -300, 300);
                toggle(advanced, "separatePrayerPosition", "Separate prayer groups",
                    "Position prayer icons independently of skulls and other head icons.", config::separatePrayerPosition);
                choice(advanced, "prayerIconPosition", "Overhead position",
                    "Position of independently placed prayer icons.", config::prayerIconPosition, OverheadsPosition.values());
                number(advanced, "prayerIconXOffset", "Overhead X offset",
                    "Offset in pixels. Positive Y moves upward.", config::prayerIconXOffset, -300, 300);
                number(advanced, "prayerIconYOffset", "Overhead Y offset",
                    "Offset in pixels. Positive Y moves upward.", config::prayerIconYOffset, -300, 300);
        });
        JPanel content = (JPanel) tabs.getComponentAt(tabs.getTabCount() - 1);
            section(content, "Name colors", group ->
            {
                toggle(group, "relationshipNames", "Relationship colors", "Self, friend, clan, party, other players and NPC categories.", config::relationshipNames);
                color(group, "selfNameColor", "My name", "Nameplate color, including opacity.", config::selfNameColor);
                color(group, "friendNameColor", "Friends", "Nameplate color, including opacity.", config::friendNameColor);
                color(group, "clanNameColor", "Clan / friends chat", "Nameplate color, including opacity.", config::clanNameColor);
                color(group, "partyNameColor", "Party", "Nameplate color, including opacity.", config::partyNameColor);
                color(group, "playerNameColor", "Other players", "Nameplate color, including opacity.", config::playerNameColor);
                color(group, "npcNameColor", "NPCs", "Nameplate color, including opacity.", config::npcNameColor);
                color(group, "followerNameColor", "Followers", "Nameplate color, including opacity.", config::followerNameColor);
                color(group, "shopNameColor", "Shops", "Nameplate color, including opacity.", config::shopNameColor);
            });
            section(content, "Names", group ->
            {
                choice(group, "selfNameVisibility", "My name",
                    "Choose when this nameplate is displayed.", config::selfNameVisibility, OverheadsVisibility.values());
                choice(group, "playerNameVisibility", "Other player names",
                    "Choose when this nameplate is displayed.", config::playerNameVisibility, OverheadsVisibility.values());
                choice(group, "npcNameVisibility", "NPC names",
                    "Choose when this nameplate is displayed.", config::npcNameVisibility, OverheadsVisibility.values());
                choice(group, "namePosition", "Nameplate position",
                    "Placement relative to the actor.", config::namePosition, OverheadsPosition.values());
                number(group, "nameXOffset", "Nameplate X offset",
                    "Offset in pixels. Positive Y moves upward.", config::nameXOffset, -300, 300);
                number(group, "nameYOffset", "Nameplate Y offset",
                    "Offset in pixels. Positive Y moves upward.", config::nameYOffset, -300, 300);
                toggle(group, "nameScaleWithZoom", "Scale nameplate with zoom",
                    "Scale with camera zoom.", config::nameScaleWithZoom);
                font(group, "nameFont", "Name font",
                    "Nameplate font.", config::nameFont);
                color(group, "nameColor", "Name color",
                    "Nameplate text color.", config::nameColor);
                color(group, "nameBackground", "Name background",
                    "Nameplate background, including opacity.", config::nameBackground);
            });
            section(content, "Status indicators", group ->
            {
                choice(group, "statusPosition", "Status icon position",
                    "Placement relative to the actor.", config::statusPosition, OverheadsPosition.values());
                number(group, "statusXOffset", "Status icon X offset",
                    "Offset in pixels. Positive Y moves upward.", config::statusXOffset, -300, 300);
                number(group, "statusYOffset", "Status icon Y offset",
                    "Offset in pixels. Positive Y moves upward.", config::statusYOffset, -300, 300);
                toggle(group, "statusScaleWithZoom", "Scale status icon with zoom",
                    "Scale with camera zoom.", config::statusScaleWithZoom);
                toggle(group, "showStatusIcons", "My status icons",
                    "Show your current poison, venom and disease status.", config::showStatusIcons);
                toggle(group, "showTargetingIcon", "My current target",
                    "Mark the NPC you are currently interacting with.", config::showTargetingIcon);
                number(group, "statusIconSize", "Status icon size",
                    "Status badge size in pixels.", config::statusIconSize, 10, 40);
            });
        addTab("Healing", restores ->
        {
            toggle(restores, "healingSplatEnabled", "Enabled", "Show actual increases in your HP, including food, regeneration and healing effects.", config::healingSplatEnabled);
            choice(restores, "healingSplatDisplay", "Display", "Show an icon, restored amount, or both.", config::healingSplatDisplay, OverheadsRestoreDisplay.values());
            color(restores, "healingSplatColor", "Number color", "Color and alpha of the restored amount.", config::healingSplatColor);
            font(restores, "healingSplatFont", "Number font", "Font and size of the restored amount.", config::healingSplatFont);
            number(restores, "healingSplatOpacity", "Opacity", "Opacity of the icon and number.", config::healingSplatOpacity, 0, 100);
            number(restores, "healingSplatDuration", "Duration (ms)", "Time each restoration splat remains visible.", config::healingSplatDuration, 200, 5000);
            choice(restores, "healingSplatPosition", "Position", "Native places splats on your character; other positions use overhead spacing.", config::healingSplatPosition, OverheadsHitsplatPosition.values());
            number(restores, "healingSplatXOffset", "X offset", "Horizontal offset in pixels.", config::healingSplatXOffset, -300, 300);
            number(restores, "healingSplatYOffset", "Y offset", "Vertical offset in pixels. Positive moves upward.", config::healingSplatYOffset, -300, 300);
            number(restores, "healingSplatScalePercent", "Scale (%)", "Scale the icon and number together.", config::healingSplatScalePercent, 25, 300);
            toggle(restores, "healingSplatScaleWithZoom", "Scale with zoom", "Scale with camera zoom.", config::healingSplatScaleWithZoom);
        });
        addTab("Prayer restoration", restores ->
        {
            toggle(restores, "prayerSplatEnabled", "Enabled", "Show actual increases in your prayer points.", config::prayerSplatEnabled);
            choice(restores, "prayerSplatDisplay", "Display", "Show an icon, restored amount, or both.", config::prayerSplatDisplay, OverheadsRestoreDisplay.values());
            color(restores, "prayerSplatColor", "Number color", "Color and alpha of the restored amount.", config::prayerSplatColor);
            font(restores, "prayerSplatFont", "Number font", "Font and size of the restored amount.", config::prayerSplatFont);
            number(restores, "prayerSplatOpacity", "Opacity", "Opacity of the icon and number.", config::prayerSplatOpacity, 0, 100);
            number(restores, "prayerSplatDuration", "Duration (ms)", "Time each restoration splat remains visible.", config::prayerSplatDuration, 200, 5000);
            choice(restores, "prayerSplatPosition", "Position", "Native places splats on your character; other positions use overhead spacing.", config::prayerSplatPosition, OverheadsHitsplatPosition.values());
            number(restores, "prayerSplatXOffset", "X offset", "Horizontal offset in pixels.", config::prayerSplatXOffset, -300, 300);
            number(restores, "prayerSplatYOffset", "Y offset", "Vertical offset in pixels. Positive moves upward.", config::prayerSplatYOffset, -300, 300);
            number(restores, "prayerSplatScalePercent", "Scale (%)", "Scale the icon and number together.", config::prayerSplatScalePercent, 25, 300);
            toggle(restores, "prayerSplatScaleWithZoom", "Scale with zoom", "Scale with camera zoom.", config::prayerSplatScaleWithZoom);
        });
        organizeComponents();
        if (selected >= 0 && selected < tabs.getTabCount())
        {
            tabs.setSelectedIndex(selected);
        }
        refresh();
        revalidate();
        repaint();
    }


    private void organizeComponents()
    {
        tabs.removeAll();
        sections.clear();
        componentTab("Chat", OverheadsComponent.CHAT);
        componentTab("Bars", OverheadsComponent.HEALTH, OverheadsComponent.PRAYER, OverheadsComponent.RUN, OverheadsComponent.SPECIAL);
        componentTab("Icons", OverheadsComponent.ICONS);
        componentTab("Hits", OverheadsComponent.HITS, OverheadsComponent.HEALING, OverheadsComponent.RESTORATION);
        componentTab("More", OverheadsComponent.NAMES, OverheadsComponent.TIMERS, OverheadsComponent.LAYOUT);
    }

    private void componentTab(String title, OverheadsComponent... components)
    {
        JPanel container = new JPanel(new BorderLayout(0, 5));
        container.setOpaque(false);
        JPanel cards = new JPanel(new java.awt.CardLayout());
        cards.setOpaque(false);
        List<OverheadsComponent> available = new ArrayList<>();
        for (OverheadsComponent component : components)
        {
            if ((scope == OverheadsProfiles.Scope.NPCS || scope == OverheadsProfiles.Scope.PLAYERS) && (component == OverheadsComponent.PRAYER || component == OverheadsComponent.RUN || component == OverheadsComponent.SPECIAL || component == OverheadsComponent.TIMERS)) { continue; }
            if (scope != OverheadsProfiles.Scope.SELF && (component == OverheadsComponent.HEALING || component == OverheadsComponent.RESTORATION)) continue;
            available.add(component);
            sectionContext = scope.name() + "/" + component.name();
            JPanel basic = column();
            settingRows.forEach((key, row) ->
            {
                if (OverheadsComponent.forKey(key) != component) { return; }
                if (row.getParent() != null) { row.getParent().remove(row); }
            });
            for (String section : new String[]{"Display", "Party sharing", "Styling", "Position and size", "Overhead prayers", "Combat / misc prayers", "Corner badges", "Row 1 background", "Row 2 background", "Effects", "Animation", "Large targets", "Damage trail", "Segments", "Restore and warnings", "Status icons"})
            {
                if (settingRows.keySet().stream().noneMatch(key -> OverheadsComponent.forKey(key) == component && sectionName(component, key).equals(section))) continue;
                section(basic, section, content -> settingRows.forEach((key, row) ->
                {
                    if (OverheadsComponent.forKey(key) == component && sectionName(component, key).equals(section)) content.add(row);
                }));
            }
            if (component == OverheadsComponent.HITS)
            {
                JButton themes = new JButton("Hitsplat themes");
                themes.addActionListener(event ->
                {
                    if (setups == null) { return; }
                    if (themeWindow == null || !themeWindow.isDisplayable())
                    {
                        setups.setThemeScope(scope);
                    themeWindow = new OverheadsSetupWindow(this, setups, true);
                        editors.add(themeWindow);
                    }
                    themeWindow.setVisible(true);
                });
                OverheadsPanelStyle.control(themes);
                basic.add(themes);
            }
            if (component == OverheadsComponent.LAYOUT)
            {
                JButton reset = new JButton("Reflow spacing");
                reset.addActionListener(event -> reflow.run());
                OverheadsPanelStyle.control(reset);
                basic.add(reset);
            }
            if (component == OverheadsComponent.TIMERS && scope == OverheadsProfiles.Scope.PARTY)
            {
                JButton sync = new JButton("Refresh party data");
                sync.addActionListener(event -> refreshParty.run());
                OverheadsPanelStyle.control(sync);
                basic.add(sync);
            }
            cards.add(OverheadsPanelStyle.scroll(basic), component.name());
        }
        if (available.size() > 1)
        {
            JComboBox<OverheadsComponent> select = new JComboBox<>(available.toArray(new OverheadsComponent[0]));
            OverheadsPanelStyle.control(select);
            OverheadsComponent previous = selectedComponents.get(title);
            if (available.contains(previous)) select.setSelectedItem(previous);
            ((java.awt.CardLayout) cards.getLayout()).show(cards, ((OverheadsComponent) select.getSelectedItem()).name());
            select.addActionListener(event ->
            {
                OverheadsComponent component = (OverheadsComponent) select.getSelectedItem();
                selectedComponents.put(title, component);
                ((java.awt.CardLayout) cards.getLayout()).show(cards, component.name());
            });
            container.add(select, BorderLayout.NORTH);
        }
        container.add(cards, BorderLayout.CENTER);
        tabs.addTab(title, container);
    }

    private OverheadsConfig sharingConfig() { return profiles == null ? config : profiles.config(OverheadsProfiles.Scope.SELF); }

    private static String healthSection(String key)
    {
        if (key.startsWith("healthBarDamageTrail")) return "Damage trail";
        if (key.startsWith("healthBarSegment") || key.equals("healthBarHitpointsPerSegment")) return "Segments";
        if (key.startsWith("restore") || key.startsWith("low")) return "Restore and warnings";
        if (key.contains("Offset") || key.contains("Width") || key.contains("Height") || key.contains("Scale")
            || key.equals("healthPosition") || key.equals("healthOrientation")) return "Position and size";
        if (key.endsWith("Display") || key.endsWith("Visibility") || key.equals("healthBarsEnabled")
            || key.equals("alwaysShowSelfHealth") || key.startsWith("partyShow") || key.equals("hiddenNpcHealthIds")) return "Display";
        return "Styling";
    }

    private static String sectionName(OverheadsComponent component, String key)
    {
        if (component == OverheadsComponent.HEALING || component == OverheadsComponent.RESTORATION)
        {
            if (key.endsWith("Enabled") || key.endsWith("Display")) return "Display";
            if (key.endsWith("Duration")) return "Animation";
            if (key.endsWith("Position") || key.contains("Offset") || key.contains("Scale")) return "Position and size";
            return "Styling";
        }
        if (component == OverheadsComponent.HEALTH) return healthSection(key);
        if (key.equals("prayerIconLayout") || key.startsWith("prayerIconBadge")) return "Corner badges";
        if (key.startsWith("prayerIconRow2")) return "Row 2 background";
        if (key.equals("showPrayerIconBackground") || key.equals("prayerIconShape") || key.equals("prayerIconBackground")
            || key.equals("prayerIconBorder") || key.equals("prayerIconBackgroundTracksPoints") || key.equals("prayerIconEmptyBackground")) return "Row 1 background";
        if (key.equals("separatePrayerPosition")) return "Display";
        if (key.startsWith("prayerIconCombat") || key.equals("localBoostPrayerIcons")) return "Combat / misc prayers";
        if (key.equals("prayerIconRow") || key.equals("prayerIconPosition") || key.equals("prayerIconXOffset")
            || key.equals("prayerIconYOffset") || key.equals("prayerIconScalePercent")) return "Overhead prayers";
        if (component == OverheadsComponent.LAYOUT) return "Display";
        if (component == OverheadsComponent.TIMERS && key.startsWith("party")) return "Party sharing";
        if (key.startsWith("largeTarget")) return "Large targets";
        if (key.startsWith("fade") || key.equals("fullOpacityDuration") || key.equals("hitsplatMotion") || key.startsWith("hitsplatScroll")) return "Animation";
        if (key.equals("overheadChatEffect")) return "Effects";
        if (key.startsWith("status") || key.equals("showStatusIcons") || key.equals("showTargetingIcon")) return "Status icons";
        if (key.startsWith("prayerRestore") || key.startsWith("lowPrayer") || key.equals("prayerLowColor")
            || key.startsWith("prayerTick")) return "Restore and warnings";
        if (!key.equals("spriteScalingMode") && (key.contains("Offset") || key.contains("Width") || key.contains("Height")
            || key.contains("Scale") || key.contains("Spacing") || key.endsWith("Position") || key.endsWith("Orientation")
            || key.startsWith("layout") || key.endsWith("Radius") && !key.contains("Corner"))) return "Position and size";
        if (key.startsWith("partyShow") || key.endsWith("Display") || key.endsWith("Visibility") || key.endsWith("HideWhenFull")
            || key.equals("overheadChatEnabled") || key.equals("headIconsEnabled") || key.startsWith("disable")
            || key.equals("showNpcOverheadChat") || key.equals("showPrayerIcons") || key.equals("showSkullIcons")
            || key.equals("showNpcIcons") || key.equals("showHintArrows") || key.equals("localBoostPrayerIcons")
            || key.equals("maxHitsplats") || key.equals("reuseOldHitsplatSlots") || key.equals("hitsplatReuseInterval")
            || key.equals("onlyDisplayMine") || key.equals("prioritizeMine") || key.equals("hideZeroHitsplats")) return "Display";
        return "Styling";
    }

    void refresh()
    {
        refreshing = true;
        try
        {
            bindings.forEach(Runnable::run);
            settingRows.forEach((key, row) -> row.setVisible(settingVisible(key)));
            sections.forEach((content, toggle) -> {
                boolean visible = java.util.Arrays.stream(content.getComponents()).anyMatch(java.awt.Component::isVisible);
                toggle.getParent().setVisible(visible);
                toggle.setVisible(visible);
                content.setVisible(visible && toggle.isSelected());
            });
            revalidate();
            repaint();
        }
        finally
        {
            refreshing = false;
        }
    }

    void closeEditors()
    {
        invalidateSetupSession();
        editors.forEach(Window::dispose);
        editors.clear();
        setupWindow = null;
        themeWindow = null;
    }

    void invalidateSetupSession()
    {
        editorGeneration.incrementAndGet();
        if (setups != null)
        {
            setups.invalidate();
        }
    }

    void setReflow(Runnable reflow)
    {
        this.reflow = reflow;
    }

    private JPanel column()
    {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(OverheadsPanelStyle.BACKGROUND);
        panel.setMinimumSize(new Dimension(0, 0));
        return panel;
    }

    private static final class DisclosureIcon implements Icon
    {
        private final boolean expanded;
        private DisclosureIcon(boolean expanded) { this.expanded = expanded; }
        public int getIconWidth() { return 12; }
        public int getIconHeight() { return 12; }
        public void paintIcon(java.awt.Component c, java.awt.Graphics g, int x, int y)
        {
            g.setColor(OverheadsPanelStyle.ACCENT);
            if (expanded) g.fillPolygon(new int[]{x + 1, x + 9, x + 5}, new int[]{y + 4, y + 4, y + 8}, 3);
            else g.fillPolygon(new int[]{x + 3, x + 3, x + 7}, new int[]{y + 2, y + 10, y + 6}, 3);
        }
    }

    private void addTab(String name, Consumer<JPanel> builder)
    {
        JPanel content = column();
        builder.accept(content);
        tabs.addTab(name, content);
    }

    private void section(JPanel content, String title, Consumer<JPanel> builder)
    {
        JPanel details = column();
        details.setBackground(OverheadsPanelStyle.CARD);
        details.setBorder(BorderFactory.createEmptyBorder(4, 8, 8, 8));
        builder.accept(details);
        details.setVisible(false);
        details.setAlignmentX(LEFT_ALIGNMENT);
        JToggleButton expand = new JToggleButton(title);
        OverheadsPanelStyle.control(expand);
        expand.setHorizontalAlignment(SwingConstants.LEFT);
        expand.setForeground(OverheadsPanelStyle.ACCENT);
        expand.setFont(OverheadsPanelStyle.FONT.deriveFont(java.awt.Font.BOLD));
        expand.setBorder(BorderFactory.createEmptyBorder(7, 8, 7, 8));
        String stateKey = sectionContext + "/" + title;
        expand.setSelected(expandedSections.getOrDefault(stateKey, title.equals("Display") || title.equals("Party sharing")));
        expand.setIcon(new DisclosureIcon(false));
        expand.setSelectedIcon(new DisclosureIcon(true));
        details.setVisible(expand.isSelected());
        sections.put(details, expand);
        expand.setAlignmentX(LEFT_ALIGNMENT);
        expand.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        expand.addActionListener(event ->
        {
            details.setVisible(expand.isSelected());
            expandedSections.put(stateKey, expand.isSelected());
            for (java.awt.Container ancestor = details; ancestor != null; ancestor = ancestor.getParent()) ancestor.invalidate();
            revalidate();
            repaint();
        });
        JPanel group = column();
        group.setAlignmentX(LEFT_ALIGNMENT);
        group.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0), BorderFactory.createLineBorder(OverheadsPanelStyle.BORDER)));
        group.add(expand);
        group.add(details);
        content.add(group);
    }

    private void addTab(String name, Consumer<JPanel> basicBuilder, Consumer<JPanel> advancedBuilder)
    {
        JPanel content = column();
        basicBuilder.accept(content);
        JPanel advanced = column();
        advancedBuilder.accept(advanced);
        advanced.setVisible(false);
        JToggleButton expand = new JToggleButton("More settings");
        expand.setAlignmentX(LEFT_ALIGNMENT);
        expand.addActionListener(event ->
        {
            advanced.setVisible(expand.isSelected());
            content.revalidate();
        });
        content.add(Box.createVerticalStrut(8));
        content.add(expand);
        content.add(advanced);
        tabs.addTab(name, content);
    }

    private void row(JPanel parent, String name, String tip, JComponent editor)
    {
        JPanel row = new JPanel(new BorderLayout(4, 0));
        row.setOpaque(false);
        row.setMinimumSize(new Dimension(0, 0));
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
        JLabel label = new JLabel(name);
        label.setMinimumSize(new Dimension(0, 16));
        OverheadsPanelStyle.control(label);
        OverheadsPanelStyle.control(editor);
        label.setToolTipText(tip);
        editor.setToolTipText(tip);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 55));
        if (editor instanceof ColorJButton)
        {
            editor.setPreferredSize(new Dimension(42, 22));
            row.add(label, BorderLayout.CENTER);
            row.add(editor, BorderLayout.EAST);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 30));
        }
        else
        {
            row.add(label, BorderLayout.NORTH);
            row.add(editor, BorderLayout.CENTER);
        }
        parent.add(row);
        Object key = editor.getClientProperty("settingKey");
        if (key instanceof String)
        {
            settingRows.put((String) key, row);
        }
    }

    private void write(String key, Object value)
    {
        if (!refreshing)
        {
            manager.setConfiguration(key.equals("partyDisplay") || key.equals("partyDataLifetime") ? OverheadsConfig.GROUP : scope.group, key, value);
        }
    }

    private void toggle(JPanel panel, String key, String name, String tip, Supplier<Boolean> getter)
    {
        JCheckBox check = new JCheckBox(name);
        OverheadsPanelStyle.control(check);
        check.putClientProperty("settingKey", key);
        check.setOpaque(false);
        check.setToolTipText(tip);
        check.setAlignmentX(LEFT_ALIGNMENT);
        check.addActionListener(event -> write(key, check.isSelected()));
        bindings.add(() -> check.setSelected(getter.get()));
        panel.add(check);
        settingRows.put(key, check);
    }

    private void number(JPanel panel, String key, String name, String tip, Supplier<Integer> getter, int min, int max)
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Math.max(min, Math.min(max, getter.get())), min, max, 1));
        spinner.addChangeListener(event -> write(key, spinner.getValue()));
        bindings.add(() -> spinner.setValue(Math.max(min, Math.min(max, getter.get()))));
        if (key.endsWith("Offset"))
        {
            JPanel controls = new JPanel(new java.awt.BorderLayout(3, 0));
            controls.setOpaque(false);
            controls.add(spinner, java.awt.BorderLayout.CENTER);
            JButton reset = new JButton("↺");
            reset.setMargin(new java.awt.Insets(0, 4, 0, 4));
            reset.setToolTipText("Reset " + name.toLowerCase(java.util.Locale.ROOT));
            reset.addActionListener(event ->
            {
                manager.unsetConfiguration(scope.group, key);
                refresh();
            });
            controls.add(reset, java.awt.BorderLayout.EAST);
            controls.putClientProperty("settingKey", key);
            row(panel, name, tip, controls);
        }
        else
        {
            spinner.putClientProperty("settingKey", key);
            row(panel, name, tip, spinner);
        }
    }

    private void decimal(JPanel panel, String key, String name, String tip, Supplier<Double> getter, int min, int max)
    {
        JSpinner spinner = new JSpinner(new SpinnerNumberModel(Math.max(min, Math.min(max, getter.get())), min, max, 0.5));
        spinner.addChangeListener(event -> write(key, spinner.getValue()));
        bindings.add(() -> spinner.setValue(Math.max(min, Math.min(max, getter.get()))));
        spinner.putClientProperty("settingKey", key);
        row(panel, name, tip, spinner);
    }

    private <T> void choice(JPanel panel, String key, String name, String tip, Supplier<T> getter, T[] values)
    {
        JComboBox<T> choice = new JComboBox<>(values);
        choice.addActionListener(event -> write(key, choice.getSelectedItem()));
        bindings.add(() -> choice.setSelectedItem(getter.get()));
        choice.putClientProperty("settingKey", key);
        row(panel, name, tip, choice);
    }

    private void color(JPanel panel, String key, String name, String tip, Supplier<Color> getter)
    {
        ColorJButton button = new ColorJButton("", getter.get());
        button.setPreferredSize(new Dimension(100, 24));
        button.addActionListener(event ->
        {
            Long profile = profileId();
            long generation = editorGeneration.get();
            RuneliteColorPicker picker = colorPickers.create(this, getter.get(), name, true);
            editors.removeIf(window -> !window.isDisplayable());
            editors.add(picker);
            picker.setOnColorChange(value ->
            {
                if (editors.contains(picker) && picker.isDisplayable()
                    && generation == editorGeneration.get() && Objects.equals(profile, profileId()))
                {
                    write(key, value);
                }
            });
            picker.setVisible(true);
        });
        bindings.add(() -> button.setColor(getter.get()));
        button.putClientProperty("settingKey", key);
        row(panel, name, tip, button);
    }

    private void font(JPanel panel, String key, String name, String tip, Supplier<FontType> getter)
    {
        JButton button = new JButton();
        bindings.add(() -> button.setText(getter.get().getFamily()));
        button.addActionListener(event ->
        {
            Long profile = profileId();
            long generation = editorGeneration.get();
            FontType current = getter.get();
            JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), name, java.awt.Dialog.ModalityType.MODELESS);
            JPanel content = column();
            JComboBox<String> family = new JComboBox<>(new String[]{FontType.REGULAR.getFamily(), "Dialog", "SansSerif", "Serif", "Monospaced"});
            family.setEditable(true);
            family.setSelectedItem(current.getFamily());
            JSpinner size = new JSpinner(new SpinnerNumberModel(Math.max(8, Math.min(48, current.getSize())), 8, 48, 1));
            JCheckBox bold = new JCheckBox("Bold", current.isBold());
            JCheckBox italic = new JCheckBox("Italic", current.isItalic());
            row(content, "Font", "", family);
            row(content, "Size", "", size);
            content.add(bold);
            content.add(italic);
            JButton apply = new JButton("Apply");
            apply.addActionListener(action ->
            {
                if (editors.contains(dialog) && dialog.isDisplayable()
                    && generation == editorGeneration.get() && Objects.equals(profile, profileId()))
                {
                    FontType value = new FontType().withFamily(String.valueOf(family.getSelectedItem()))
                        .withSize((Integer) size.getValue()).withBold(bold.isSelected()).withItalic(italic.isSelected());
                    write(key, value);
                }
                dialog.dispose();
                editors.remove(dialog);
            });
            content.add(apply);
            OverheadsPanelStyle.control(content);
            dialog.setContentPane(content);
            dialog.pack();
            dialog.setLocationRelativeTo(this);
            editors.add(dialog);
            dialog.setVisible(true);
        });
        JPanel controls = new JPanel(new BorderLayout(4, 0));
        controls.setOpaque(false);
        JSpinner size = new JSpinner(new SpinnerNumberModel(Math.max(8, Math.min(48, getter.get().getSize())), 8, 48, 1));
        size.setToolTipText("Font size");
        size.addChangeListener(event ->
        {
            FontType current = getter.get();
            write(key, new FontType().withFamily(current.getFamily()).withSize((Integer) size.getValue())
                .withBold(current.isBold()).withItalic(current.isItalic()));
        });
        bindings.add(() -> size.setValue(Math.max(8, Math.min(48, getter.get().getSize()))));
        controls.add(button, BorderLayout.CENTER);
        controls.add(size, BorderLayout.EAST);
        controls.putClientProperty("settingKey", key);
        row(panel, name, tip, controls);
        size.setPreferredSize(new Dimension(55, 26));
    }

    private void text(JPanel panel, String key, String name, String tip, Supplier<String> getter)
    {
        JTextField field = new JTextField(getter.get());
        Long profile = profileId();
        long generation = editorGeneration.get();
        Runnable save = () ->
        {
            if (!refreshing && generation == editorGeneration.get()
                && Objects.equals(profile, profileId()) && !Objects.equals(field.getText(), getter.get()))
            {
                write(key, field.getText());
            }
        };
        field.addActionListener(event -> save.run());
        field.addFocusListener(new java.awt.event.FocusAdapter()
        {
            @Override
            public void focusLost(java.awt.event.FocusEvent event)
            {
                save.run();
            }
        });
        bindings.add(() -> { if (!field.hasFocus()) { field.setText(getter.get()); } });
        field.putClientProperty("settingKey", key);
        row(panel, name, tip, field);
    }

    private boolean settingVisible(String key)
    {
        boolean self = scope == OverheadsProfiles.Scope.SELF;
        if (key.startsWith("healingSplat") || key.startsWith("prayerSplat"))
        {
            if (!self) return false;
            boolean prayer = key.startsWith("prayerSplat");
            if (key.endsWith("Enabled")) return true;
            if (!(prayer ? config.prayerSplatEnabled() : config.healingSplatEnabled())) return false;
            OverheadsRestoreDisplay display = prayer ? config.prayerSplatDisplay() : config.healingSplatDisplay();
            return !(key.endsWith("Color") || key.endsWith("Font")) || display != OverheadsRestoreDisplay.ICON_ONLY;
        }
        if (key.equals("prayerIconLayout")) return self;
        if (key.equals("prayerIconScalePercent") && self && config.prayerIconLayout() == OverheadsPrayerLayout.CORNER_BADGES) return false;
        if (key.startsWith("prayerIconBadge"))
        {
            return self && config.prayerIconLayout() == OverheadsPrayerLayout.CORNER_BADGES
                && (!key.endsWith("Color") || config.prayerIconBadgeBackground());
        }
        boolean npc = scope == OverheadsProfiles.Scope.NPCS;
        boolean players = scope == OverheadsProfiles.Scope.PLAYERS || scope == OverheadsProfiles.Scope.PARTY;
        if (key.startsWith("indicator") && scope == OverheadsProfiles.Scope.PARTY) { return false; }
        if (key.startsWith("partyShow") && scope != OverheadsProfiles.Scope.PARTY) { return false; }
        if (players && (key.equals("prayerVisibility") || key.equals("prayerTickIndicator") || key.equals("prayerTickColor") || key.startsWith("prayerRestore") || key.equals("prayerLowColor") || key.equals("lowPrayerThreshold"))) { return false; }
        if ((key.equals("partyDisplay") || key.equals("partyDataLifetime")) && scope != OverheadsProfiles.Scope.PARTY) { return false; }
        if ((key.equals("alwaysShowSelfHealth") || key.equals("restorePreview") || key.equals("restorePreviewColor")
            || key.equals("lowHealthThreshold") || key.equals("lowResourceColor") || key.equals("healthBarPoisonedFrontColor")
            || key.equals("localBoostPrayerIcons") || key.equals("showStatusIcons") || key.equals("regenDisplay") || key.equals("poisonTimerDisplay")) && !self) { return false; }
        if (key.equals("selfHealthDisplay") || key.equals("selfNameVisibility") || key.equals("disableMyHitsplats")) { return self; }
        if (key.equals("otherHealthDisplay") || key.equals("otherHealthVisibility") || key.equals("playerNameVisibility") || key.equals("disableAllyHitsplats")) { return players; }
        if (key.equals("npcHealthDisplay") || key.equals("npcHealthVisibility") || key.equals("npcNameVisibility") || key.equals("disableEnemyHitsplats")
            || key.equals("hiddenNpcHealthIds") || key.equals("nativeNpcUiIds") || key.equals("showNpcOverheadChat") || key.equals("showNpcIcons")) { return npc; }
        if (key.equals("overheadChatEnabled") && npc) { return false; }
        if ((key.equals("playerHealthWidth") || key.equals("playerHealthHeight")) && npc) { return false; }
        if ((key.equals("healthBarSolidWidth") || key.equals("healthBarHeight")) && !npc) { return false; }

        if (key.equals("restorePreviewColor"))
        {
            return config.restorePreview();
        }
        if (key.equals("prayerRestorePreviewColor")) { return config.prayerRestorePreview(); }
        if (key.equals("prayerTickColor"))
        {
            return config.prayerTickIndicator();
        }
        if (key.equals("prayerIconPosition") || key.equals("prayerIconXOffset") || key.equals("prayerIconYOffset")
            || key.equals("prayerIconRow") || key.startsWith("prayerIconCombat") && !key.endsWith("ScalePercent"))
        {
            return config.separatePrayerPosition() || self && config.prayerIconLayout() == OverheadsPrayerLayout.CORNER_BADGES;
        }
        if (key.startsWith("prayerIconRow2"))
        {
            if (!config.separatePrayerPosition() && !(self && config.prayerIconLayout() == OverheadsPrayerLayout.CORNER_BADGES)) return false;
            if (key.equals("prayerIconRow2BackgroundEnabled")) return true;
            if (!config.prayerIconRow2BackgroundEnabled()) return false;
            if (key.equals("prayerIconRow2Shape")) return true;
            if (config.prayerIconRow2Shape() == OverheadsIconShape.ORIGINAL) return false;
            if (key.equals("prayerIconRow2TracksPoints") || key.equals("prayerIconRow2EmptyBackground"))
                return scope != OverheadsProfiles.Scope.NPCS && scope != OverheadsProfiles.Scope.PLAYERS
                    && (!key.equals("prayerIconRow2EmptyBackground") || config.prayerIconRow2TracksPoints());
            return true;
        }
        if (key.equals("prayerIconBackgroundTracksPoints") || key.equals("prayerIconEmptyBackground"))
        {
            return scope != OverheadsProfiles.Scope.NPCS && scope != OverheadsProfiles.Scope.PLAYERS
                && config.showPrayerIconBackground() && config.prayerIconShape() != OverheadsIconShape.ORIGINAL
                && (!key.equals("prayerIconEmptyBackground") || config.prayerIconBackgroundTracksPoints());
        }
        if (key.equals("prayerIconBackground") || key.equals("prayerIconBorder"))
        {
            return config.showPrayerIconBackground() && config.prayerIconShape() != OverheadsIconShape.ORIGINAL;
        }
        if (key.startsWith("run") || key.startsWith("special"))
        {
            boolean special = key.startsWith("special");
            String prefix = special ? "special" : "run";
            if (key.equals(prefix + "Display"))
            {
                return true;
            }
            OverheadsBarDisplay display = special ? config.specialDisplay() : config.runDisplay();
            if (display == OverheadsBarDisplay.HIDDEN)
            {
                return false;
            }
            String suffix = key.substring(prefix.length());
            if (suffix.equals("Font") || suffix.equals("TextColor") || suffix.equals("NumberFormat") || suffix.equals("TextPosition"))
            {
                return display != OverheadsBarDisplay.BAR_ONLY;
            }
            boolean bar = display != OverheadsBarDisplay.NUMBERS_ONLY;
            if (suffix.equals("BackColor") || suffix.equals("BorderColor") || suffix.equals("CornerRadius"))
            {
                return bar && (special ? config.specialStyle() : config.runStyle()) == OverheadsHealthBarStyle.SOLID;
            }
            if (suffix.equals("Style") || suffix.equals("Color") || suffix.equals("Opacity") || suffix.equals("FillDirection"))
            {
                return bar;
            }
        }
        if (key.startsWith("prayer") && !key.startsWith("prayerIcon") && !key.equals("prayerVisibility"))
        {
            if (!players && config.prayerVisibility() == OverheadsPrayerVisibility.NEVER)
            {
                return false;
            }
            if (key.equals("prayerDisplay"))
            {
                return true;
            }
            if (config.prayerDisplay() == OverheadsBarDisplay.HIDDEN)
            {
                return false;
            }
            if (key.equals("prayerTextFont") || key.equals("prayerTextColor") || key.equals("prayerNumberFormat") || key.equals("prayerTextPosition"))
            {
                return config.prayerDisplay() != OverheadsBarDisplay.BAR_ONLY;
            }
            if (key.equals("prayerBackColor") || key.equals("prayerBorderColor") || key.equals("prayerBorderThickness") || key.equals("prayerCornerRadius"))
            {
                return config.prayerDisplay() != OverheadsBarDisplay.NUMBERS_ONLY && config.prayerBarStyle() == OverheadsHealthBarStyle.SOLID;
            }
        }
        if (key.equals("healthTextFont") || key.equals("healthTextColor") || key.equals("healthTextPosition"))
        {
            return numbers(npc ? config.npcHealthDisplay() : players ? config.otherHealthDisplay() : config.selfHealthDisplay());
        }
        if (key.equals("healthNumberFormat"))
        {
            return numbers(npc ? config.npcHealthDisplay() : players ? config.otherHealthDisplay() : config.selfHealthDisplay());
        }
        if (key.equals("healthBarFrontSecondaryColor") || key.equals("healthBarFrontGradientCoordinates"))
        {
            return config.healthBarStyle() == OverheadsHealthBarStyle.SOLID && config.healthBarFrontGradient() != OverheadsHealthBarGradient.SOLID;
        }
        if (key.equals("healthBarBackSecondaryColor") || key.equals("healthBarBackGradientCoordinates"))
        {
            return config.healthBarStyle() == OverheadsHealthBarStyle.SOLID && config.healthBarBackGradient() != OverheadsHealthBarGradient.SOLID;
        }
        if (key.startsWith("healthBarDamageTrail") && !key.equals("healthBarDamageTrailEnabled"))
        {
            return config.healthBarStyle() == OverheadsHealthBarStyle.SOLID && config.healthBarDamageTrailEnabled();
        }
        if (key.startsWith("healthBarFront") || key.startsWith("healthBarBack") || key.startsWith("healthBarBorder")
            || key.equals("healthBarCornerRadius") || key.equals("healthBarPoisonedFrontColor") || key.equals("healthBarDamageTrailEnabled"))
        {
            return config.healthBarStyle() == OverheadsHealthBarStyle.SOLID;
        }
        if (key.equals("overheadChatFriendColor") || key.equals("overheadChatClanColor") || key.equals("overheadChatGroupIronColor"))
        {
            return config.overheadChatRelationshipColors();
        }
        if (key.equals("overheadChatShadowColor"))
        {
            return config.overheadChatShadow();
        }
        if (key.equals("hitsplatDamageColor") || key.equals("hitsplatMissColor")
            || key.equals("hitsplatPoisonColor") || key.equals("hitsplatVenomColor") || key.equals("hitsplatBurnColor")
            || key.startsWith("hitsplatBorder") || key.equals("hitsplatCornerRadius"))
        {
            return config.hitsplatStyle() == OverheadsHitsplatStyle.MODERN;
        }
        if (key.equals("hitsplatNumberColor") || key.equals("hitsplatMissNumberColor")
            || key.equals("hitsplatStatusNumberColor") || key.equals("hitsplatTextShadow"))
        {
            return config.hitsplatStyle() != OverheadsHitsplatStyle.OSRS;
        }
        if (key.equals("hitsplatScrollColumns") || key.equals("hitsplatScrollDistance"))
        {
            return config.hitsplatMotion() != OverheadsHitsplatMotion.FIXED;
        }
        if (key.equals(OverheadsConfig.LAYOUT_SHAPE_KEY) || key.equals(OverheadsConfig.LAYOUT_DIRECTION_KEY)
            || key.equals(OverheadsConfig.LAYOUT_BEHAVIOR_KEY) || key.equals(OverheadsConfig.MIN_RADIUS_KEY)
            || key.equals(OverheadsConfig.MAX_RADIUS_KEY))
        {
            return config.hitsplatMotion() == OverheadsHitsplatMotion.FIXED;
        }
        if (key.startsWith("status"))
        {
            return config.showStatusIcons() || config.showTargetingIcon();
        }
        if (key.startsWith("name"))
        {
            return config.selfNameVisibility() != OverheadsVisibility.HIDDEN || config.playerNameVisibility() != OverheadsVisibility.HIDDEN
                || config.npcNameVisibility() != OverheadsVisibility.HIDDEN;
        }
        return true;
    }

    private static boolean numbers(OverheadsBarDisplay display)
    {
        return display == OverheadsBarDisplay.NUMBERS_ONLY || display == OverheadsBarDisplay.BOTH;
    }

    private Long profileId()
    {
        ConfigProfile profile = manager.getProfile();
        return profile == null ? null : profile.getId();
    }

    static BufferedImage icon()
    {
        BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        Graphics2D graphics = image.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setColor(new Color(63, 178, 186));
        graphics.fillRoundRect(2, 2, 28, 15, 8, 8);
        graphics.fillPolygon(new int[]{8, 8, 15}, new int[]{14, 22, 14}, 3);
        graphics.setColor(new Color(77, 201, 110));
        graphics.fillRoundRect(5, 25, 22, 5, 3, 3);
        graphics.dispose();
        return image;
    }
}
