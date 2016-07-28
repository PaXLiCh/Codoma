package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;

/**
 * Solarized (Dark) color theme.
 */
public class SolarizedDarkSyntaxColor extends SyntaxColor {
	private static final int background = 0xFF002B36;
	private static final int foreground = 0xFF839496;

	private static final int keyword = 0xFF859900;
	private static final int builtin = 0xFFB58900;
	private static final int variable = 0xFF268BD2;
	private static final int string = 0xFF2AA198;
	private static final int comment = 0xFF586E75;
	private static final int link = 0xFF859900;
	private static final int punctuation = 0xFF839496;
	private static final int number = 0xFFD33682;

	/*<key>divider</key>
	<string>#586e75</string>
	<key>selectionBackground</key>
	<string>#586e75</string>
	<key>selectionForeground</key>
	<string>#93a1a1</string>*/

	/*<key>caret</key>
	<string>#819090</string>
	<key>invisibles</key>
	<string>#073642</string>
	<key>lineHighlight</key>
	<string>#073642</string>
	<key>selection</key>
	<string>#073642</string>*/

	private static final Style gutterStyle = new Style();
	private static final Style gutterSelectedStyle = new Style();

	private static final Style foregroundStyle = new Style();
	private static final Style keywordStyle = new Style();
	private static final Style builtinStyle = new Style();
	private static final Style variableStyle = new Style();
	private static final Style stringStyle = new Style();
	private static final Style commentStyle = new Style();
	private static final Style linkStyle = new Style();
	private static final Style punctuationStyle = new Style();
	private static final Style numberStyle = new Style();
	private static final Style errorStyle = new Style();

	static {
		gutterStyle.foreground = 0xFF839496;
		gutterStyle.background = 0xFF073642;

		gutterSelectedStyle.foreground = 0xFF586E75;
		gutterSelectedStyle.background = 0xFF93A1A1;

		foregroundStyle.foreground = foreground;
		foregroundStyle.background = background;

		keywordStyle.foreground = keyword;
		builtinStyle.foreground = builtin;
		variableStyle.foreground = variable;

		stringStyle.foreground = string;

		commentStyle.foreground = comment;
		commentStyle.typeface = Typeface.ITALIC;

		linkStyle.foreground = link;
		linkStyle.isUnderline = true;

		punctuationStyle.foreground = punctuation;
		numberStyle.foreground = number;

		errorStyle.foreground = 0xFF839496;
		errorStyle.background = 0xFFD30102;
	}

	@Override
	public Style getStyle(Scope scope) {
		switch (scope) {

			case FOREGROUND:
				return foregroundStyle;
			case KEYWORD:
				return keywordStyle;
			case BUILTIN:
				return builtinStyle;
			case VARIABLE:
				return variableStyle;
			case STRING:
				return stringStyle;
			case COMMENT:
				return commentStyle;
			case LINK:
				return linkStyle;
			case PUNCTUATION:
				return punctuationStyle;
			case NUMBER:
				return numberStyle;
			case ERROR:
				return errorStyle;

			case NON_PRINTING_GLYPH:
				return foregroundStyle;

			case GUTTER:
				return gutterStyle;
			case GUTTER_SELECTED:
				return gutterSelectedStyle;

			default:
				return foregroundStyle;
		}
	}
}
