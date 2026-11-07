package com.improvedoverheads;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import org.junit.Test;

public class OverheadsXbrScalerTest
{
	@Test
	public void solidPixelRemainsSolidAtTwiceTheSize()
	{
		assertArrayEquals(
			new int[]{0xFF336699, 0xFF336699, 0xFF336699, 0xFF336699},
			OverheadsXbrScaler.scale2x(new int[]{0xFF336699}, 1, 1));
	}

	@Test
	public void diagonalEdgeMatchesThePermissiveReferenceKernel()
	{
		int white = 0xFFFFFFFF;
		int black = 0xFF000000;
		int[] source = {
			white, white, black,
			white, black, black,
			black, black, black
		};
		int[] expected = {
			white, white, white, 0xFFBFBFBF, black, black,
			white, white, white, 0xFF3F3F3F, black, black,
			white, white, 0xFF7F7F7F, black, black, black,
			0xFFBFBFBF, 0xFF3F3F3F, black, black, black, black,
			black, black, black, black, black, black,
			black, black, black, black, black, black
		};

		assertArrayEquals(expected, OverheadsXbrScaler.scale2x(source, 3, 3));
	}

	@Test
	public void transparentDiagonalScalesAlphaWithoutDarkFringes()
	{
		int transparent = 0;
		int red = 0xFFFF0000;
		int[] source = {
			transparent, transparent, transparent,
			transparent, red, red,
			transparent, red, red
		};
		int[] expected = {
			0, 0, 0, 0, 0, 0,
			0, 0, 0, 0, 0, 0,
			0, 0, 0x0FFF0000, 0xBFFF0000, red, red,
			0, 0, 0xBFFF0000, red, red, red,
			0, 0, red, red, red, red,
			0, 0, red, red, red, red
		};

		assertArrayEquals(expected, OverheadsXbrScaler.scale2x(source, 3, 3));
	}

	@Test
	public void variedArgbNeighborhoodMatchesTheReferenceKernel()
	{
		int[] source = new int[7 * 5];
		long state = 0x12345678L;
		int[] alpha = {0, 64, 128, 192, 255};
		for (int i = 0; i < source.length; i++)
		{
			state = (state * 1_664_525L + 1_013_904_223L) & 0xFFFFFFFFL;
			source[i] = alpha[i % alpha.length] << 24 | (int) state & 0xFFFFFF;
		}

		assertEquals(
			598_272_104,
			Arrays.hashCode(OverheadsXbrScaler.scale2x(source, 7, 5)));
	}

	@Test
	public void bufferedImageAdapterUsesArgbAndDoublesDimensions()
	{
		BufferedImage source = new BufferedImage(2, 1, BufferedImage.TYPE_INT_ARGB);
		source.setRGB(0, 0, 0x80112233);
		source.setRGB(1, 0, 0xFF445566);

		BufferedImage scaled = OverheadsXbrScaler.scale2x(source);

		assertEquals(4, scaled.getWidth());
		assertEquals(2, scaled.getHeight());
		assertEquals(0x80112233, scaled.getRGB(0, 0));
		assertEquals(0xFF445566, scaled.getRGB(3, 1));
	}
}
