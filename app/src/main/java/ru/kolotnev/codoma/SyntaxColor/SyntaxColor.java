package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;

/**
 * Color scheme for syntax highlight.
 */
public abstract class SyntaxColor {
	// In ARGB format: 0xAARRGGBB
	private static final int BLACK = 0xFF000000;
	private static final int BLUE = 0xFF0000FF;
	private static final int DARK_RED = 0xFF8B0000;
	private static final int GREY = 0xFF808080;
	private static final int LIGHT_GREY = 0xFFAAAAAA;
	private static final int MAROON = 0xFF800000;
	private static final int INDIGO = 0xFF2A00FF;
	private static final int OLIVE_GREEN = 0xFF3F7F5F;
	private static final int PURPLE = 0xFF7F0055;
	private static final int RED = 0xFFFF0000;
	private static final int WHITE = 0xFFFFFFFF;

	private static final Style NONE = new Style();
	private static final Style WHITESPACE = new Style();

	static {
		NONE.color = Color.BLACK;
		WHITESPACE.color = Color.LTGRAY;
	}

	/**
	 * Whether this color scheme uses a dark back, like black or dark grey.
	 */
	public boolean isDark() { return false; }

	@NonNull
	public Style getStyle(String scope) {
		return NONE;
	}

	@NonNull
	public Style getWhitespaceStyle() {
		return WHITESPACE;
	}

	public int getTextColor() {
		return Color.BLACK;
	}

	public int getBackgroundColor() {
		return Color.WHITE;
	}

	public int getSearchResultColor() {
		return Color.LTGRAY;
	}

	public int getSelectionColor() {
		return Color.GRAY;
	}

	public int getGutterColor() {
		return Color.WHITE;
	}

	public int getGutterColorSelected() {
		return Color.LTGRAY;
	}

	public int getGutterTextColor() {
		return Color.BLACK;
	}

	public int getGutterTextColorSelected() {
		return Color.BLACK;
	}

	/**
	 * Style for scope.
	 */
	public static class Style {
		public Integer color;
		public int fontStyle = Typeface.NORMAL;
		public boolean isUnderline = false;
	}

}
