package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Syntax coloring for HTML languages.
 */
public class HTMLTextSyntax extends TextSyntax {

	public static final Pattern HTML_TAGS = Pattern.compile(
			"<([A-Za-z][A-Za-z0-9]*)\\b[^>]*>|</([A-Za-z][A-Za-z0-9]*)\\b[^>]*>");
	public static final Pattern HTML_ATTRS = Pattern.compile(
			"(\\S+)=[\"']?((?:.(?![\"']?\\s+(?:\\S+)=|[>\"']))+.)[\"']?");
	public static final Pattern XML_COMMENTS = Pattern.compile("(?s)<!--.*?-->");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("entity.name.class", HTML_TAGS));
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", HTML_ATTRS));
		patterns.add(new AbstractMap.SimpleEntry<>("comment", XML_COMMENTS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
