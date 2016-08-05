package ru.kolotnev.codoma.TextSyntax;

/**
 * Singleton class containing the symbols and operators of the Objective-C language
 */
public class ObjectiveCTextSyntax extends TextSyntax {
	private final static String[] keywords = {
			"char", "double", "float", "int", "long", "short", "void",
			"auto", "const", "extern", "register", "static", "volatile",
			"signed", "unsigned", "sizeof", "typedef",
			"enum", "struct", "union",
			"break", "case", "continue", "default", "do", "else", "for",
			"goto", "if", "return", "switch", "while",
			"@class", "@implementation", "@interface", "@protocol", "@property",
			"@private", "@protected", "@public", "@optional", "@required",
			"@defs", "@dynamic", "@encode", "@synchronized", "@selector", "@synthesize",
			"@try", "@catch", "@throw", "@finally", "@end",
			"id", "self", "super", "nil", "Nil", "NULL", "SEL", "BOOL", "YES", "NO",
			"in", "out", "inout", "bycopy", "byref", "oneway",
			"getter", "setter", "readwrite", "readonly", "assign", "retain", "copy", "nonatomic"
	};
}
