package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Base class for syntax highliting.
 */
public abstract class TextSyntax {
	public final static char EOF = '\uFFFF';
	public final static char NULL_CHAR = '\u0000';
	public final static char NEWLINE = '\n';
	public final static char BACKSPACE = '\b';
	public final static char TAB = '\t';
	public final static String GLYPH_NEWLINE = "↵";
	public final static String GLYPH_SPACE = "·";
	public final static String GLYPH_TAB = "»";

	private final static char[] BASIC_C_OPERATORS = {
			'(', ')', '{', '}', '.', ',', ';', '=', '+', '-',
			'/', '*', '&', '!', '|', ':', '[', ']', '<', '>',
			'?', '~', '%', '^'
	};


	// Strings
	public static final Pattern GENERAL_STRINGS = Pattern.compile("\"(.*?)\"|'(.*?)'");

	public static final Pattern GENERAL_KEYWORDS = Pattern.compile(
			"(?<=\\b)((alignas)|(alignof)|(and)|(and_eq)|(asm)|(auto)|(bitand)|(bitorbool)" +
					"|(break)|(case)|(catch)|(char)|(char16_t)|(char32_t)|(class)|(compl)|(const)" +
					"|(constexpr)|(const_cast)|(continue)|(decltype)|(default)|(delete)|(do)" +
					"|(double)|(dynamic_cast)|(echo)|(else)|(enum)|(explicit)|(export)|(extern)" +
					"|(false)|(float)|(for)|(friend)|(function)|(goto)|(if)|(inline)|(int)" +
					"|(mutable)|(namespace)|(new)|(noexcept)|(not)|(not_eq)|(null)|(nullptr)" +
					"|(operator)|(or)|(or_eq)|(private)|(protected)|(public)|(register)" +
					"|(reinterpret_cast)|(return)|(short)|(signed)|(sizeof)|(static)" +
					"|(static_assert)|(static_cast)|(struct)|(switch)|(template)|(this)" +
					"|(thread_local)|(throw)|(true)|(try)|(typedef)|(typeid)|(typename)" +
					"|(undefined)|(union)|(unsigned)|(using)|(var)|(virtual)|(void)|(volatile)" +
					"|(wchar_t)|(while)|(xor)|(xor_eq))(?=\\b)", Pattern.CASE_INSENSITIVE);

	// Comments
	public static final Pattern XML_COMMENTS = Pattern.compile("(?s)<!--.*?-->");
	public static final Pattern GENERAL_COMMENTS = Pattern.compile(
			"/\\*(?:.|[\\n\\r])*?\\*/|(?<!:)//.*|#.*");
	// same as GENERAL_COMMENTS but without -> //
	public static final Pattern GENERAL_COMMENTS_NO_SLASH = Pattern.compile(
			"/\\*(?:.|[\\n\\r])*?\\*/|#.*");


	public static final Pattern LINK = android.util.Patterns.WEB_URL;

	private static final Pattern NUMBERS = Pattern.compile("\\b(\\d*[.]?\\d+)\\b");
	public static final Pattern SYMBOLS = Pattern.compile(
			"!|,|\\(|\\)|\\+|\\-|\\*|<|>|=|\\.|\\?|;|\\{|\\}|\\[|\\]|\\|");
	public static final Pattern NUMBERS_OR_SYMBOLS = Pattern.compile("("+NUMBERS.pattern()+")|("+SYMBOLS.pattern()+")");

	private static final Pattern line = Pattern.compile(".*\\n");
	private static final Pattern trailingWhiteSpace = Pattern.compile("[\\t ]+$", Pattern.MULTILINE);

	protected Pattern patternKeyWords = null;

	public Pattern getLines() {
		return line;
	}

	public Pattern getTrailingWhiteSpace() {
		return trailingWhiteSpace;
	}

	public boolean isWhitespace(char c) {
		return (c == ' ' || c == '\n' || c == '\t' ||
				c == '\r' || c == '\f' || c == EOF);
	}

	public boolean isSentenceTerminator(char c) {
		return (c == '.');
	}

	public boolean isEscapeChar(char c) {
		return (c == '\\');
	}

	/**
	 * Derived classes that do not do represent C-like programming languages
	 * should return false; otherwise return true
	 */
	public boolean isProgLang() {
		return true;
	}

	/**
	 * Whether the word after c is a token
	 */
	public boolean isWordStart(char c) {
		return false;
	}

	/**
	 * Whether cSc is a token, where S is a sequence of characters that are on the same line
	 */
	public boolean isDelimiterA(char c) {
		return (c == '"');
	}

	/**
	 * Same concept as isDelimiterA(char), but Language and its subclasses can
	 * specify a second type of symbol to use here
	 */
	public boolean isDelimiterB(char c) {
		return (c == '\'');
	}

	/**
	 * Whether cL is a token, where L is a sequence of characters until the end of the line
	 */
	public boolean isLineAStart(char c) {
		return (c == '#');
	}

	/**
	 * Same concept as isLineAStart(char), but Language and its subclasses can
	 * specify a second type of symbol to use here
	 */
	public boolean isLineBStart(char c) {
		return false;
	}

	/**
	 * Whether c0c1L is a token, where L is a sequence of characters until the end of the line
	 */
	public boolean isLineStart(char c0, char c1) {
		return (c0 == '/' && c1 == '/');
	}

	/**
	 * Whether c0c1 signifies the start of a multi-line token
	 */
	public boolean isMultilineStartDelimiter(char c0, char c1) {
		return (c0 == '/' && c1 == '*');
	}

	/**
	 * Whether c0c1 signifies the end of a multi-line token
	 */
	public boolean isMultilineEndDelimiter(char c0, char c1) {
		return (c0 == '*' && c1 == '/');
	}

	@NonNull
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("constant.numeric", NUMBERS));
		patterns.add(new AbstractMap.SimpleEntry<>("string.quoted", GENERAL_STRINGS));
		patterns.add(new AbstractMap.SimpleEntry<>("punctuation.definition.parameters", SYMBOLS));
		patterns.add(new AbstractMap.SimpleEntry<>("comment", GENERAL_COMMENTS));
		return patterns;
	}
}
