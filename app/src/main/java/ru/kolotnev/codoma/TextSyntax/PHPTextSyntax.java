package ru.kolotnev.codoma.TextSyntax;

import java.util.regex.Pattern;

/**
 * Highlighting for PHP.
 */
public class PHPTextSyntax extends TextSyntax {

	private static final Pattern KEYWORDS = Pattern.compile("\\b(abstract|and|array|as|break" +
			"|case|catch|class|clone|const|continue|declare|default|do|else|elseif|enddeclare" +
			"|endfor|endforeach|endif|endswitch|endwhile|extends|final|for|foreach|function" +
			"|global|goto|if|implements|interface|instanceof|namespace|new|or|private|protected" +
			"|public|static|switch|throw|try|use|var|while|xor|die|echo|empty|exit|eval|include" +
			"|include_once|isset|list|require|require_once|return|print|unset|self|static|parent" +
			"|true|TRUE|false|FALSE|null|NULL\n)\\b");

	private static final Pattern SYMBOLS = Pattern.compile("\\(|\\)|\\{|\\}|.|,|;|=|\\+|-|/|\\*|&|!|||:|\\[|\\]|<|>|\\?|~|%|^|`|@");

	public static final Pattern PHP_VARIABLES = Pattern.compile("\\$\\s*(\\w+)");

	@Override
	public Pattern getKeywords() {
		return KEYWORDS;
	}

	@Override
	public Pattern getVariables() {
		return PHP_VARIABLES;
	}

	@Override
	public Pattern getSymbols() {
		return SYMBOLS;
	}
}
