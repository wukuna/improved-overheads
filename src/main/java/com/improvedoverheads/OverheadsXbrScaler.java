/*
 * This file is a Java ARGB port of the 2x kernel from xBRjs:
 * https://github.com/joseprio/xBRjs
 *
 * Copyright 2020 Josep del Rio
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.improvedoverheads;

import java.awt.image.BufferedImage;

final class OverheadsXbrScaler
{
	private static final int THRESHOLD_Y = 48;
	private static final int THRESHOLD_U = 7;
	private static final int THRESHOLD_V = 6;
	private static final double DIFFERENT_ALPHA_DISTANCE = 1_000_000.0;

	private OverheadsXbrScaler()
	{
	}

	static BufferedImage scale2x(BufferedImage source)
	{
		if (source == null)
		{
			throw new IllegalArgumentException("source must not be null");
		}

		int width = source.getWidth();
		int height = source.getHeight();
		int[] sourcePixels = source.getRGB(0, 0, width, height, null, 0, width);
		int[] scaledPixels = scale2x(sourcePixels, width, height);
		BufferedImage scaled = new BufferedImage(width * 2, height * 2, BufferedImage.TYPE_INT_ARGB);
		scaled.setRGB(0, 0, scaled.getWidth(), scaled.getHeight(), scaledPixels, 0, scaled.getWidth());
		return scaled;
	}

	static int[] scale2x(int[] source, int width, int height)
	{
		if (source == null || width <= 0 || height <= 0
			|| (long) width * height > source.length
			|| (long) width * height * 4 > Integer.MAX_VALUE)
		{
			throw new IllegalArgumentException("invalid source dimensions");
		}

		int scaledWidth = width * 2;
		int[] destination = new int[width * height * 4];
		int[] output = new int[4];
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				scalePixel(source, width, height, x, y, destination, scaledWidth, output);
			}
		}
		return destination;
	}

	private static void scalePixel(
		int[] source,
		int width,
		int height,
		int x,
		int y,
		int[] destination,
		int scaledWidth,
		int[] output)
	{
		int a1 = pixel(source, width, height, x - 1, y - 2);
		int b1 = pixel(source, width, height, x, y - 2);
		int c1 = pixel(source, width, height, x + 1, y - 2);
		int a0 = pixel(source, width, height, x - 2, y - 1);
		int pa = pixel(source, width, height, x - 1, y - 1);
		int pb = pixel(source, width, height, x, y - 1);
		int pc = pixel(source, width, height, x + 1, y - 1);
		int c4 = pixel(source, width, height, x + 2, y - 1);
		int d0 = pixel(source, width, height, x - 2, y);
		int pd = pixel(source, width, height, x - 1, y);
		int pe = pixel(source, width, height, x, y);
		int pf = pixel(source, width, height, x + 1, y);
		int f4 = pixel(source, width, height, x + 2, y);
		int g0 = pixel(source, width, height, x - 2, y + 1);
		int pg = pixel(source, width, height, x - 1, y + 1);
		int ph = pixel(source, width, height, x, y + 1);
		int pi = pixel(source, width, height, x + 1, y + 1);
		int i4 = pixel(source, width, height, x + 2, y + 1);
		int g5 = pixel(source, width, height, x - 1, y + 2);
		int h5 = pixel(source, width, height, x, y + 2);
		int i5 = pixel(source, width, height, x + 1, y + 2);

		output[0] = pe;
		output[1] = pe;
		output[2] = pe;
		output[3] = pe;

		kernel(pe, pi, ph, pf, pg, pc, pd, pb, f4, i4, h5, i5, output, 1, 2, 3);
		kernel(pe, pc, pf, pb, pi, pa, ph, pd, b1, c1, f4, c4, output, 0, 3, 1);
		kernel(pe, pa, pb, pd, pc, pg, pf, ph, d0, a0, b1, a1, output, 2, 1, 0);
		kernel(pe, pg, pd, ph, pa, pi, pb, pf, h5, g5, d0, g0, output, 3, 0, 2);

		int destinationX = x * 2;
		int destinationY = y * 2;
		int row = destinationY * scaledWidth + destinationX;
		destination[row] = output[0];
		destination[row + 1] = output[1];
		destination[row + scaledWidth] = output[2];
		destination[row + scaledWidth + 1] = output[3];
	}

	private static void kernel(
		int pe,
		int pi,
		int ph,
		int pf,
		int pg,
		int pc,
		int pd,
		int pb,
		int f4,
		int i4,
		int h5,
		int i5,
		int[] output,
		int n1Index,
		int n2Index,
		int n3Index)
	{
		if (pe == ph || pe == pf)
		{
			return;
		}

		double e = difference(pe, pc, true)
			+ difference(pe, pg, true)
			+ difference(pi, h5, true)
			+ difference(pi, f4, true)
			+ quadrupled(difference(ph, pf, false));
		double i = difference(ph, pd, true)
			+ difference(ph, i5, true)
			+ difference(pf, i4, true)
			+ difference(pf, pb, true)
			+ quadrupled(difference(pe, pi, true));
		int px = difference(pe, pf, true) <= difference(pe, ph, true) ? pf : ph;

		boolean edgeRule = e < i
			&& (!isEqual(pf, pb, true) && !isEqual(ph, pd, true)
				|| isEqual(pe, pi, true) && (!isEqual(pf, i4, true) && !isEqual(ph, i5, true))
				|| isEqual(pe, pg, true)
				|| isEqual(pe, pc, true));
		if (edgeRule)
		{
			double ke = difference(pf, pg, true);
			double ki = difference(ph, pc, true);
			boolean ex2 = pe != pc && pb != pc;
			boolean ex3 = pe != pg && pd != pg;
			boolean left = doubled(ke) <= ki && ex3;
			boolean up = ke >= doubled(ki) && ex2;
			if (left || up)
			{
				if (left)
				{
					output[n3Index] = blend192(output[n3Index], px);
					output[n2Index] = blend64(output[n2Index], px);
				}
				if (up)
				{
					output[n3Index] = blend192(output[n3Index], px);
					output[n1Index] = blend64(output[n1Index], px);
				}
			}
			else
			{
				output[n3Index] = blend128(output[n3Index], px);
			}
		}
		else if (e <= i)
		{
			output[n3Index] = blend64(output[n3Index], px);
		}
	}

	private static int pixel(int[] source, int width, int height, int x, int y)
	{
		int safeX = Math.max(0, Math.min(width - 1, x));
		int safeY = Math.max(0, Math.min(height - 1, y));
		return source[safeY * width + safeX];
	}

	private static double difference(int first, int second, boolean scaleAlpha)
	{
		int firstAlpha = first >>> 24;
		int secondAlpha = second >>> 24;
		if (firstAlpha == 0 && secondAlpha == 0)
		{
			return 0.0;
		}
		if (!scaleAlpha && (firstAlpha < 255 || secondAlpha < 255))
		{
			return DIFFERENT_ALPHA_DISTANCE;
		}
		if (firstAlpha == 0 || secondAlpha == 0)
		{
			return DIFFERENT_ALPHA_DISTANCE;
		}

		double firstRed = first >> 16 & 0xFF;
		double firstGreen = first >> 8 & 0xFF;
		double firstBlue = first & 0xFF;
		double secondRed = second >> 16 & 0xFF;
		double secondGreen = second >> 8 & 0xFF;
		double secondBlue = second & 0xFF;
		double yDifference = Math.abs(
			(firstRed - secondRed) * 0.299
				+ (firstGreen - secondGreen) * 0.587
				+ (firstBlue - secondBlue) * 0.114);
		double uDifference = Math.abs(
			(firstRed - secondRed) * -0.168736
				+ (firstGreen - secondGreen) * -0.331264
				+ (firstBlue - secondBlue) * 0.5);
		double vDifference = Math.abs(
			(firstRed - secondRed) * 0.5
				+ (firstGreen - secondGreen) * -0.418688
				+ (firstBlue - secondBlue) * -0.081312);
		return yDifference * THRESHOLD_Y
			+ uDifference * THRESHOLD_U
			+ vDifference * THRESHOLD_V;
	}

	private static boolean isEqual(int first, int second, boolean scaleAlpha)
	{
		int firstAlpha = first >>> 24;
		int secondAlpha = second >>> 24;
		if (firstAlpha == 0 && secondAlpha == 0)
		{
			return true;
		}
		if (!scaleAlpha && (firstAlpha < 255 || secondAlpha < 255))
		{
			return false;
		}
		if (firstAlpha == 0 || secondAlpha == 0)
		{
			return false;
		}

		int firstRed = first >> 16 & 0xFF;
		int firstGreen = first >> 8 & 0xFF;
		int firstBlue = first & 0xFF;
		int secondRed = second >> 16 & 0xFF;
		int secondGreen = second >> 8 & 0xFF;
		int secondBlue = second & 0xFF;
		double firstY = firstRed * 0.299 + firstGreen * 0.587 + firstBlue * 0.114;
		double secondY = secondRed * 0.299 + secondGreen * 0.587 + secondBlue * 0.114;
		if (Math.abs(firstY - secondY) > THRESHOLD_Y)
		{
			return false;
		}

		double firstU = firstRed * -0.168736 + firstGreen * -0.331264 + firstBlue * 0.5;
		double secondU = secondRed * -0.168736 + secondGreen * -0.331264 + secondBlue * 0.5;
		if (Math.abs(firstU - secondU) > THRESHOLD_U)
		{
			return false;
		}

		double firstV = firstRed * 0.5 + firstGreen * -0.418688 + firstBlue * -0.081312;
		double secondV = secondRed * 0.5 + secondGreen * -0.418688 + secondBlue * -0.081312;
		return Math.abs(firstV - secondV) <= THRESHOLD_V;
	}

	private static double doubled(double value)
	{
		return (int) value << 1;
	}

	private static double quadrupled(double value)
	{
		return (int) value << 2;
	}

	private static int blend64(int destination, int source)
	{
		return interpolate(destination, source, 3, 1);
	}

	private static int blend128(int destination, int source)
	{
		return interpolate(destination, source, 1, 1);
	}

	private static int blend192(int destination, int source)
	{
		return interpolate(destination, source, 1, 3);
	}

	private static int interpolate(int first, int second, int firstWeight, int secondWeight)
	{
		int firstAlpha = first >>> 24;
		int secondAlpha = second >>> 24;
		int divisor = firstWeight + secondWeight;
		int red;
		int green;
		int blue;
		if (firstAlpha == 0)
		{
			red = second >> 16 & 0xFF;
			green = second >> 8 & 0xFF;
			blue = second & 0xFF;
		}
		else if (secondAlpha == 0)
		{
			red = first >> 16 & 0xFF;
			green = first >> 8 & 0xFF;
			blue = first & 0xFF;
		}
		else
		{
			red = (secondWeight * (second >> 16 & 0xFF)
				+ firstWeight * (first >> 16 & 0xFF)) / divisor;
			green = (secondWeight * (second >> 8 & 0xFF)
				+ firstWeight * (first >> 8 & 0xFF)) / divisor;
			blue = (secondWeight * (second & 0xFF)
				+ firstWeight * (first & 0xFF)) / divisor;
		}
		int alpha = (secondWeight * secondAlpha + firstWeight * firstAlpha) / divisor;
		return alpha << 24 | red << 16 | green << 8 | blue;
	}
}
