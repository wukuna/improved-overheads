package com.improvedoverheads;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.zip.CRC32;

final class OverheadsNativePrayerSymbols
{
    private static final long[] ORIGINALS = {
        2932578400L, 4126660400L, 1655351577L, 2269946334L, 1113085138L, 3421506084L,
        3927908410L, 917807132L, 485525166L, 4154412061L, 1758325569L, 2899314705L,
        1036023985L, 2137093768L, 191858212L, 2486936876L, 3600460565L, 2727631801L,
        2617292120L, 3424226255L, 4066606223L, 1159317516L
    };
    private static final int[] BACKING_COLORS = {
        0x7449A7, 0x7C4EA7, 0x8353A7, 0x8E5BA7, 0x925EA7, 0x92886D, 0x9662A7,
        0x998D6D, 0x9E66A7, 0x9E916D, 0xA2936D, 0xA36AA7, 0xAB70A7, 0xAC9C6D,
        0xAD79B1, 0xB074A7, 0xB1A688, 0xB479AC, 0xB5A16D, 0xB97FAC, 0xBAA56D,
        0xBC7EA7, 0xBDAB79, 0xBE80A8, 0xBFA96D, 0xC283A7, 0xC3AB6D, 0xC3AF7B,
        0xC6B074, 0xC9B273, 0xCDB36D, 0xD0B570, 0xD2B66D
    };

    private OverheadsNativePrayerSymbols() { }

    static OverheadsSprite removeBacking(int index, OverheadsSprite source)
    {
        if (source == null || index < 0 || index >= ORIGINALS.length
            || source.getWidth() != 25 || source.getHeight() != 25)
        {
            return source;
        }
        BufferedImage image = source.getImage();
        int[] pixels = image.getRGB(0, 0, 25, 25, null, 0, 25);
        CRC32 fingerprint = new CRC32();
        for (int pixel : pixels)
        {
            int normalized = pixel == 0xFF000000 ? 0xFF000001 : pixel;
            fingerprint.update(normalized >>> 24);
            fingerprint.update(normalized >>> 16);
            fingerprint.update(normalized >>> 8);
            fingerprint.update(normalized);
        }
        if (fingerprint.getValue() != ORIGINALS[index])
        {
            return source;
        }
        for (int i = 0; i < pixels.length; i++)
        {
            if ((pixels[i] >>> 24) != 0 && Arrays.binarySearch(BACKING_COLORS, pixels[i] & 0xFFFFFF) >= 0)
            {
                pixels[i] = 0;
            }
        }
        BufferedImage transparent = new BufferedImage(25, 25, BufferedImage.TYPE_INT_ARGB);
        transparent.setRGB(0, 0, 25, 25, pixels, 0, 25);
        return new OverheadsSprite(transparent, source.getMaxWidth(), source.getMaxHeight(), source.getOffsetX(), source.getOffsetY());
    }
}
