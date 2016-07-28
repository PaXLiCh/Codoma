package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;

import java.util.HashMap;

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
	protected HashMap<Scope, Integer> _colors = generateDefaultColors();

	protected void setColor(Scope scope, int color) {
		_colors.put(scope, color);
	}

	public Style getStyle(Scope scope) {
		switch (scope) {
			default: return null;
		}
	}

	/**
	 * Whether this color scheme uses a dark background, like black or dark grey.
	 */
	public boolean isDark() { return false; }

	private HashMap<Scope, Integer> generateDefaultColors() {
		// High-contrast, black-on-white color scheme
		HashMap<Scope, Integer> colors = new HashMap<>(Scope.values().length);
		colors.put(Scope.FOREGROUND, BLACK);
		colors.put(Scope.LINE_HIGHLIGHT, RED);
		colors.put(Scope.NON_PRINTING_GLYPH, LIGHT_GREY);
		colors.put(Scope.COMMENT, OLIVE_GREEN); //  Eclipse default color
		colors.put(Scope.KEYWORD, PURPLE); // Eclipse default color
		colors.put(Scope.LITERAL, INDIGO); // Eclipse default color
		colors.put(Scope.SECONDARY, DARK_RED);

		colors.put(Scope.SELECTION_FOREGROUND, WHITE);
		colors.put(Scope.SELECTION_BACKGROUND, MAROON);

		colors.put(Scope.CARET_FOREGROUND, WHITE);
		colors.put(Scope.CARET_BACKGROUND, BLUE);
		colors.put(Scope.CARET_DISABLED, GREY);

		return colors;
	}

	/**
	 * Style for scope.
	 */
	public static class Style {
		public Integer foreground;
		public Integer background;
		public int typeface = Typeface.NORMAL;
		public boolean isUnderline = false;
	}

	/**
	 * Scope for syntax highlighting.
	 */
	public enum Scope {
		FOREGROUND,
		KEYWORD,
		BUILTIN,
		VARIABLE,
		STRING,
		COMMENT,
		LINK,
		PUNCTUATION,
		NUMBER,
		ERROR,

		NON_PRINTING_GLYPH,

		GUTTER,
		GUTTER_SELECTED,

		// TODO: sort and clear this
		SELECTION_FOREGROUND, SELECTION_BACKGROUND,
		CARET_FOREGROUND, CARET_BACKGROUND, CARET_DISABLED, LINE_HIGHLIGHT,
		LITERAL,
		SECONDARY
	}
}
