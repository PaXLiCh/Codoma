package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Highlighting for PHP.
 */
public class PHPTextSyntax extends TextSyntax {

	private static final Pattern KEYWORDS = Pattern.compile("\\b(abstract|and|array|as|break" +
			"|case|catch|class|clone|const|continue|declare|default|do|else|elseif|enddeclare" +
			"|endfor|endforeach|endif|endswitch|endwhile|extends|final|for|foreach|function" +
			"|global|goto|if|implements|interface|instanceof|namespace|new|or|private|protected" +
			"|public|switch|throw|try|use|var|while|xor|die|echo|empty|exit|eval|include" +
			"|include_once|isset|list|require|require_once|return|print|unset|self|static|parent" +
			"|true|TRUE|false|FALSE|null|NULL\n)\\b");

	private static final Pattern SYMBOLS = Pattern.compile("\\(|\\)|\\{|\\}|.|,|;|=|\\+|-|/|\\*|&|!|||:|\\[|\\]|<|>|\\?|~|%|^|`|@");

	public static final Pattern PHP_VARIABLES = Pattern.compile("\\$\\s*(\\w+)");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.add(new AbstractMap.SimpleEntry<>("constant.character", SYMBOLS));
		patterns.add(new AbstractMap.SimpleEntry<>("variable", PHP_VARIABLES));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
