package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * CSS highlight.
 */
public class CSSTextSyntax extends TextSyntax {
	private static final Pattern CSS_STYLE_NAME= Pattern.compile("[ \\t\\n\\r\\f](.+?)\\{([^\\)]+)\\}");
	private static final Pattern CSS_ATTRS = Pattern.compile("(.+?):(.+?);");
	private static final Pattern CSS_ATTR_VALUE = Pattern.compile(":[ \t](.+?);");
	private static final Pattern CSS_NUMBERS = Pattern.compile(
			"/^auto$|^[+-]?[0-9]+\\.?([0-9]+)?(px|em|ex|%|in|cm|mm|pt|pc)?$/ig");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("entity.name.class", CSS_STYLE_NAME));
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", CSS_ATTRS));
		patterns.add(new AbstractMap.SimpleEntry<>("variable", CSS_ATTR_VALUE));
		patterns.add(new AbstractMap.SimpleEntry<>("constant.numeric", CSS_NUMBERS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
