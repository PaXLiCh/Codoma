package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Java syntax highlight.
 */
public class JavaTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(void|boolean|byte|char|short" +
			"|int|long|float|double|strictfp|import|package|new|class|interface|extends" +
			"|implements|enum|public|private|protected|static|abstract|final|native|volatile" +
			"|assert|try|throw|throws|catch|finally|instanceof|super|this|if|else|for|do|while" +
			"|switch|case|default|continue|break|return|synchronized|transient|true|false|null)\\b");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
