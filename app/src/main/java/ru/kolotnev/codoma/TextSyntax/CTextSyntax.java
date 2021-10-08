package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Singleton class containing the symbols and operators of the C language
 */
public class CTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(char|double|float|int|long|short"
			+ "|void|auto|const|extern|register|static|volatile|signed|unsigned|sizeof|typedef|enum"
			+ "|struct|union|break|case|continue|default|do|else|for|goto|if|return|switch|while)");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
