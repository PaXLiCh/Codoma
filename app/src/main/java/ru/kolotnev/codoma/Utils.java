package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.Map;

/**
 * Couple of helpers.
 */
public final class Utils {
	@Nullable
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Map.Entry<T, E> entry : map.entrySet()) {
			if (value.equals(entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Update theme for specified activity by current settings.
	 *
	 * @param activity
	 * 		Activity.
	 */
	public static void setTheme(@NonNull Activity activity) {
		activity.setTheme(PreferenceHelper.isDarkTheme(activity)
				? R.style.AppTheme_Dark
				: R.style.AppTheme_Light);
	}

	public static void updateTheme(@NonNull Activity activity) {
		activity.finish();
		activity.startActivity(new Intent(activity, activity.getClass()));
	}
}
