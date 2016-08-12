package ru.kolotnev.codoma;

import android.os.Build;

/**
 * Contains params of current device. This is nice because we can override
 * some here to test compatibility with old API.
 *
 * @author Artem Chepurnoy
 */
public class Device {

	/**
	 * @return {@code true} if device is device supports given API version,
	 * {@code false} otherwise.
	 */
	public static boolean hasTargetApi(int api) {
		return Build.VERSION.SDK_INT >= api;
	}

	/**
	 * @return {@code true} if device is running
	 * {@link Build.VERSION_CODES#} or higher, {@code false} otherwise.
	 */
	public static boolean hasLollipopApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
	}

	/**
	 * @return {@code true} if device is running
	 * {@link Build.VERSION_CODES#KITKAT KitKat} or higher, {@code false} otherwise.
	 */
	public static boolean hasKitKatApi() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
	}

	/**
	 * @return {@code true} if device is running
	 * {@link Build.VERSION_CODES#KITKAT KitKat} {@code false} otherwise.
	 */
	public static boolean isKitKatApi() {
		return Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT;
	}

	/**
	 * @return {@code true} if device is running
	 * {@link Build.VERSION_CODES#JELLY_BEAN_MR2 Jelly Bean 4.3} or higher, {@code false} otherwise.
	 */
	public static boolean hasJellyBeanMR2Api() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2;
	}

	/**
	 * @return {@code true} if device is running
	 * {@link Build.VERSION_CODES#JELLY_BEAN_MR1 Jelly Bean 4.2} or higher, {@code false} otherwise.
	 */
	public static boolean hasJellyBeanMR1Api() {
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1;
	}
}
