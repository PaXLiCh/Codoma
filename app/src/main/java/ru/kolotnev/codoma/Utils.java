package ru.kolotnev.codoma;

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
}
