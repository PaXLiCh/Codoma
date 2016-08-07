package ru.kolotnev.codoma.TextSyntax;

import android.support.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Python keywords.
 */
public class PythonTextSyntax extends TextSyntax {

	public static final Pattern PY_KEYWORDS = Pattern.compile(
			"(?<=\\b)(int|float|long|complex|str|unicode|list|tuple|bytearray|buffer|xrange|set" +
					"|frozenset|dict|bool|True|False|None|self|NotImplemented|Ellipsis|__debug__" +
					"|__file__|and|del|from|not|while|as|elif|global|or|with|assert|else|if|pass" +
					"|yield|break|except|import|print|class|exec|in|raise|continue|finally|is" +
					"|return|def|for|lambda|try|ArithmeticError|AssertionError|AttributeError" +
					"|BaseException|DeprecationWarning|EnvironmentError|EOFError|Exception" +
					"|FloatingPointError|FutureWarning|GeneratorExit|IOError|ImportError" +
					"|ImportWarning|IndexError|KeyError|KeyboardInterrupt|LookupError|MemoryError" +
					"|NameError|NotImplementedError|OSError|OverflowError" +
					"|PendingDeprecationWarning|ReferenceError|RuntimeError|RuntimeWarning" +
					"|StandardError|StopIteration|SyntaxError|SyntaxWarning|SystemError" +
					"|SystemExit|TypeError|UnboundLocalError|UserWarning|UnicodeError" +
					"|UnicodeWarning|UnicodeEncodeError|UnicodeDecodeError|UnicodeTranslateError" +
					"|ValueError|Warning|WindowsError|ZeroDivisionError)(?=\\b)",
			Pattern.CASE_INSENSITIVE);

	public static final Pattern PY_SYMBOLS = Pattern.compile(
			"\\(|\\)|\\{|\\}|\\.|,|;|=|\\+|\\-|/|\\*|&|!|\\||:|\\[|\\]|<|>|~|%|\\^");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", PY_KEYWORDS));
		patterns.add(new AbstractMap.SimpleEntry<>("storage", PY_SYMBOLS));
		patterns.addAll(super.getPatterns());
		return patterns;
	}

	@Override
	public boolean isWordStart(char c) { return (c == '@'); }

	@Override
	public boolean isLineAStart(char c) { return false; }

	@Override
	public boolean isLineBStart(char c) { return (c == '#'); }

	@Override
	public boolean isLineStart(char c0, char c1) { return false; }

	@Override
	public boolean isMultilineStartDelimiter(char c0, char c1) { return false; }
}
