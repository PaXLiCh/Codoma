package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Singleton class containing the symbols and operators of the Javascript language
 */
public class JavaScriptTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(abstract|boolean|break|byte|case"
			+ "|catch|char|class|const|continue|debugger|default|delete|do|double|else|enum|export"
			+ "|extends|false|final|finally|float|for|function|goto|if|implements|import|in"
			+ "|instanceof|int|interface|long|native|new|null|package|private|protected|public"
			+ "|return|short|static|super|switch|synchronized|this|throw|throws|transient|true|try"
			+ "|typeof|var|void|volatile|while|with)");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
