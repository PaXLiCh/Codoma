package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;

/**
 * Monokai dark theme.
 */
public class MonokaiDarkSyntaxColor extends SyntaxColor {
	//private static final int back = 0xFF49483E;
	//private static final int color = 0xFF75715E;

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

	/*key>back</key>
	<string>#49483E</string>
	<key>divider</key>
	<string>#75715E</string>
	<key>color</key>
	<string>#75715E</string>*/

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
		keywordStyle.color = keyword;
		builtinStyle.color = builtin;
		variableStyle.color = variable;

		stringStyle.color = string;

		commentStyle.color = comment;
		commentStyle.fontStyle = Typeface.ITALIC;

		linkStyle.color = link;
		linkStyle.isUnderline = true;

		punctuationStyle.color = punctuation;
		numberStyle.color = number;

		errorStyle.color = 0xFFF8F8F0;
		//errorStyle.back = 0xFFF92672;
	}

	@Override
	public int getTextColor() {
		return foreground;
	}

	@Override
	public int getBackgroundColor() {
		return background;
	}

	@Override
	public int getSelectionColor() {
		return 0xFF49483E;
	}

	@Override
	public int getGutterColor() {
		return 0xFF49483E;
	}

	@Override
	public int getGutterColorSelected() {
		return 0xFF49483E;
	}

	@Override
	public int getGutterTextColor() {
		return 0xFF75715E;
	}

	@Override
	public int getGutterTextColorSelected() {
		return 0xFF75715E;
	}
}
