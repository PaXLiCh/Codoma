package ru.kolotnev.codoma.TextSyntax;

import java.util.regex.Pattern;

/**
 * C++ syntax highlight.
 */
public class CppTextSyntax extends TextSyntax {
	private static final Pattern keywords = Pattern.compile(
			"\\b(bool|char|double|float|int|long|short|void|wchar_t|auto|const" +
					"|extern|mutable|register|static|volatile|signed|unsigned|true|false" +
					"|new|delete|sizeof|typedef|typeid|typename|const_cast|dynamic_cast" +
					"|reinterpret_cast|static_cast|class|enum|explicit|operator|struct" +
					"|template|union|virtual|private|protected|public|friend|this|break" +
					"|case|catch|continue|default|do|else|for|goto|if|return|switch|throw" +
					"|try|while|export|namespace|using|asm|inline|and|and_eq|bitand|bitor" +
					"|compl|not|not_eq|or|or_eq|xor|xor_eq)\\b"
	);

	@Override
	public Pattern getKeywords() {
		return keywords;
	}
}
