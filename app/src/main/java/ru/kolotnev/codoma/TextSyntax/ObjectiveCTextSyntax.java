package ru.kolotnev.codoma.TextSyntax;

import androidx.annotation.NonNull;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Class containing the symbols and operators of the Objective-C language
 */
public class ObjectiveCTextSyntax extends TextSyntax {
	private static final Pattern KEYWORDS = Pattern.compile("\\b(char|double|float|int|long|short"
			+ "|void|auto|const|extern|register|static|volatile|signed|unsigned|sizeof|typedef|enum"
			+ "|struct|union|break|case|continue|default|do|else|for|goto|if|return|switch|while"
			+ "|id|self|super|nil|Nil|NULL|SEL|BOOL|YES|NO|in|out|inout|bycopy|byref"
			+ "|oneway|getter|setter|readwrite|readonly|assign|retain|copy|nonatomic)\\b");

	private static final Pattern KEYWORDS_CONTROL = Pattern.compile("\\b(@class|@implementation"
			+ "|@interface|@protocol|@property|@private|@protected|@public|@optional|@required"
			+ "|@defs|@dynamic|@encode|@synchronized|@selector|@synthesize"
			+ "|@try|@catch|@throw|@finally|@end)\\b");

	@NonNull
	@Override
	public List<Map.Entry<String, Pattern>> getPatterns() {
		List<Map.Entry<String, Pattern>> patterns = new ArrayList<>();
		patterns.add(new AbstractMap.SimpleEntry<>("keyword", KEYWORDS));
		patterns.add(new AbstractMap.SimpleEntry<>("keyword.control", KEYWORDS_CONTROL));
		patterns.addAll(super.getPatterns());
		return patterns;
	}
}
