package com.improvedoverheads;

import com.google.gson.JsonObject;
import java.util.Set;

final class OverheadsHitsplatThemes
{
    private static final Set<String> KEYS = Set.of(
        "hitsplatStyle", "hitsplatFont", "hitsplatScalePercent", "hitsplatScaleWithZoom", "hitsplatPosition",
        "hitsplatDamageColor", "hitsplatMissColor", "hitsplatNumberColor", "hitsplatPoisonColor", "hitsplatVenomColor",
        "hitsplatBurnColor", "hitsplatMissNumberColor", "hitsplatStatusNumberColor", "hitsplatBorderColor",
        "hitsplatBorderThickness", "hitsplatCornerRadius", "hitsplatTextShadow", "hitsplatXOffset", "hitsplatYOffset",
        "hitsplatMotion", "hitsplatScrollColumns", "hitsplatScrollDistance", "opacityPercent",
        "layoutShape", "layoutDirection", "layoutBehavior", "minRadius", "maxRadius", "xSpacingPixels", "ySpacingPixels",
        "largeTargetXOffset", "largeTargetYOffset", "fadeInDuration", "fullOpacityDuration", "fadeOutDuration");

    private OverheadsHitsplatThemes()
    {
    }

    static boolean includes(String key)
    {
        return KEYS.contains(key);
    }

    static JsonObject select(JsonObject settings)
    {
        JsonObject result = new JsonObject();
        settings.entrySet().forEach(entry ->
        {
            if (includes(entry.getKey()))
            {
                result.add(entry.getKey(), entry.getValue());
            }
        });
        return result;
    }
}
