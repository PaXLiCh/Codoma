package ru.kolotnev.codoma.TextSyntax;

import java.util.regex.Pattern;

/**
 * Singleton class containing the symbols and operators of the C language
 */
public class CTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(char|double|float|int|long|short"
			+ "|void|auto|const|extern|register|static|volatile|signed|unsigned|sizeof|typedef|enum"
			+ "|struct|union|break|case|continue|default|do|else|for|goto|if|return|switch|while)");

	@Override
	public Pattern getKeywords() {
		return KEYWORDS;
	}
}
