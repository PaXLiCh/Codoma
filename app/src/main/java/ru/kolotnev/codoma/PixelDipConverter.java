package ru.kolotnev.codoma;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Helper for converting between DPI and pixels.
 */
public final class PixelDipConverter {

	private PixelDipConverter() { /* do nothing */ }

	/**
	 * This method convets dp unit to equivalent device specific value in pixels.
	 *
	 * @param dp
	 * 		A value in dp(Device independent pixels) unit. Which we need to convert into pixels
	 * @param context
	 * 		Context to get resources and device specific display metrics
	 *
	 * @return A float value to represent Pixels equivalent to dp according to device
	 */
	public static float convertDpToPixel(final float dp, final Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		return dp * metrics.densityDpi / 160f;
	}

	/**
	 * This method converts device specific pixels to device independent pixels.
	 *
	 * @param px
	 * 		A value in px (pixels) unit. Which we need to convert into db
	 * @param context
	 * 		Context to get resources and device specific display metrics
	 *
	 * @return A float value to represent db equivalent to px value
	 */
	public static float convertPixelsToDp(final float px, final Context context) {
		final Resources resources = context.getResources();
		final DisplayMetrics metrics = resources.getDisplayMetrics();
		return px / (metrics.densityDpi / 160f);
	}
}
