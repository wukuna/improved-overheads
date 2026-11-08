package com.improvedoverheads;

enum OverheadsComponent
{
    HEALTH("Health"), PRAYER("Prayer"), RUN("Run energy"), SPECIAL("Special energy"),
    CHAT("Chat"), ICONS("Prayer and head icons"), HITS("Hitsplats"), NAMES("Names and status"),
    TIMERS("Timers and party"), LAYOUT("Spacing");

    private final String label;
    OverheadsComponent(String label) { this.label = label; }
    @Override public String toString() { return label; }

    static OverheadsComponent forKey(String key)
    {
        if (key.startsWith("overheadChat") || key.startsWith("chat") || key.equals("showNpcOverheadChat")) return CHAT;
        if (key.equals("partyShowHealth")) return HEALTH;
        if (key.equals("partyShowPrayer")) return PRAYER;
        if (key.equals("partyShowRun")) return RUN;
        if (key.equals("partyShowSpecial")) return SPECIAL;
        if (key.startsWith("run")) return RUN;
        if (key.startsWith("special")) return SPECIAL;
        if (key.startsWith("icons") || key.startsWith("prayerIcon") || key.startsWith("headIcon") || key.startsWith("showPrayer")
            || key.startsWith("showSkull") || key.startsWith("showNpcIcons") || key.startsWith("showHint")
            || key.startsWith("separatePrayer") || key.equals("localBoostPrayerIcons")) return ICONS;
        if (key.startsWith("prayer") || key.startsWith("lowPrayer")) return PRAYER;
        if (key.startsWith("indicator") || key.startsWith("party") || key.equals("regenDisplay") || key.equals("poisonTimerDisplay")) return TIMERS;
        if (key.toLowerCase(java.util.Locale.ROOT).contains("name") || key.startsWith("status") || key.equals("showStatusIcons") || key.equals("showTargetingIcon") || key.equals("markTarget")) return NAMES;
        if (key.equals("keepOnScreen") || key.equals("layoutPreview") || key.equals("avoidOverlap")
            || key.equals("componentGap") || key.equals("nativeNpcUiIds")) return LAYOUT;
        if (key.toLowerCase(java.util.Locale.ROOT).contains("health") || key.equals("restorePreview")
            || key.equals("restorePreviewColor") || key.equals("lowResourceColor")) return HEALTH;
        return HITS;
    }
}
