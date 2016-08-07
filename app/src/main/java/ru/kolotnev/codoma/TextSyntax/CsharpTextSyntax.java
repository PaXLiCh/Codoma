package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * C# syntax highlight.
 */
public class CsharpTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(abstract|as|base|bool|break" +
			"|byte|case|catch|char|checked|class|const|continue|decimal|default|delegate|do" +
			"|double|else|enum|event|explicit|extern|false|finally|fixed|float|for|foreach|goto" +
			"|if|implicit|in|int|interface|internal|is|lock|long|namespace|new|null|object" +
			"|operator|out|override|params|private|protected|public|readonly|ref|return|sbyte" +
			"|sealed|short|sizeof|stackalloc|static|string|struct|switch|this|throw|true|try" +
			"|typeof|uint|ulong|unchecked|unsafe|ushort|using|virtual|void|volatile|while" +
			"|dynamic|get|set|add|remove|global|value|var|yield|alias|partial|from|where|join|on" +
			"|equals|into|let|orderby|ascending|descending|select|group|by)\\b");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
