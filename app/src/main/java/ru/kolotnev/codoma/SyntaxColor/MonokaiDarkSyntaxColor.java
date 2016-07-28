package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;

/**
 * Monokai dark theme.
 */
public class MonokaiDarkSyntaxColor extends SyntaxColor {
	//private static final int background = 0xFF49483E;
	//private static final int foreground = 0xFF75715E;

	private static final int background = 0xFF272822;
	private static final int foreground = 0xFFF8F8F2;

	private static final int keyword = 0xFFF92672;
	private static final int builtin = 0xFFAE81FF;
	private static final int variable = 0xFFA6E22E;
	private static final int string = 0xFFF92672;
	private static final int comment = 0xFF75715E;
	private static final int link = 0xFFF92672;
	private static final int punctuation = 0xFFFFFFFF;
	private static final int number = 0xFF66D9EF;

	/*<key>caret</key>
	<string>#F8F8F0</string>
	<key>invisibles</key>
	<string>#49483E</string>
	<key>lineHighlight</key>
	<string>#49483E</string>
	<key>selection</key>
	<string>#49483E</string>*/

	/*key>background</key>
	<string>#49483E</string>
	<key>divider</key>
	<string>#75715E</string>
	<key>foreground</key>
	<string>#75715E</string>*/


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
		gutterStyle.foreground = 0xFF75715E;
		gutterStyle.background = 0xFF49483E;

		gutterSelectedStyle.foreground = 0xFF75715E;
		gutterSelectedStyle.background = 0xFF49483E;

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

		errorStyle.foreground = 0xFFF8F8F0;
		errorStyle.background = 0xFFF92672;
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
