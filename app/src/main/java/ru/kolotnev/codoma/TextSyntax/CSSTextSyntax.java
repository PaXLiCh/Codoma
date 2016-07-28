package ru.kolotnev.codoma.TextSyntax;

import java.util.regex.Pattern;

/**
 * CSS highlight.
 */
public class CSSTextSyntax extends TextSyntax {

	//static final Pattern CSS_STYLE_NAME= Pattern.compile("[ \\t\\n\\r\\f](.+?)\\{([^\\)]+)\\}");
	public static final Pattern CSS_ATTRS = Pattern.compile("(.+?):(.+?);");
	public static final Pattern CSS_ATTR_VALUE = Pattern.compile(":[ \t](.+?);");
	//public static final Pattern CSS_NUMBERS = Pattern.compile(
	// "/^auto$|^[+-]?[0-9]+\\.?([0-9]+)?(px|em|ex|%|in|cm|mm|pt|pc)?$/ig");


	@Override
	public Pattern getKeywords() {
		return CSS_ATTRS;
	}

	@Override
	public Pattern getVariables() {
		return CSS_ATTR_VALUE;
	}
}
