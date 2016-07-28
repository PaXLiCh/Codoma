package ru.kolotnev.codoma.TextSyntax;

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

	@Override
	public Pattern getKeywords() {
		return HTML_TAGS;
	}

	@Override
	public Pattern getVariables() {
		return HTML_ATTRS;
	}

	@Override
	public Pattern getComments() {
		return XML_COMMENTS;
	}
}
