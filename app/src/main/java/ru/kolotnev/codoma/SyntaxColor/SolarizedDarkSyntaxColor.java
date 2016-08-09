package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Solarized (Dark) color theme.
 */
public class SolarizedDarkSyntaxColor extends SyntaxColor {
	// Background/Foreground Tones
	private static final int base03 = 0xFF002b36;
	private static final int base02 = 0xFF073642;

	// Content Tones
	private static final int base01 = 0xFF586e75;
	private static final int base00 = 0xFF657b83;
	private static final int base0 = 0xFF839496;
	private static final int base1 = 0xFF93a1a1;

	// Background/Foreground Tones
	private static final int base2 = 0xFFeee8d5;
	private static final int base3 = 0xFFfdf6e3;

	// Accent Colors
	private static final int yellow = 0xFFb58900;
	private static final int orange = 0xFFcb4b16;
	private static final int red = 0xFFdc322f;
	private static final int magenta = 0xFFd33682;
	private static final int violet = 0xFF6c71c4;
	private static final int blue = 0xFF268bd2;
	private static final int cyan = 0xFF2aa198;
	private static final int green = 0xFF859900;

	// Custom variables
	// Warning Don't use in packages

	private static final int syntax_comment_color = base01;
	private static final int syntax_subtle_color = base00;
	private static final int syntax_emphasized_color = base1;


	// General colors
	private static final int syntax_text_color = base0;
	private static final int syntax_cursor_color = base3;
	private static final int syntax_selection_color = base02;
	private static final int syntax_selection_flash_color = base1;
	private static final int syntax_background_color = base03;

	// Guide colors
	private static final int syntax_wrap_guide_color = base02;//lighten(base02, 6%);
	private static final int syntax_indent_guide_color = base02 + 0xFF101010;//lighten(base02, 6%);
	private static final int syntax_invisible_character_color = base02;//lighten(base02, 6%);

	// For find and replace markers
	private static final int syntax_result_marker_color = cyan;
	private static final int syntax_result_marker_color_selected = base3;

	// Gutter colors
	private static final int syntax_gutter_text_color = base0;
	private static final int syntax_gutter_text_color_selected = base0 + 0xFF080808;
	private static final int syntax_gutter_background_color = base02;
	private static final int syntax_gutter_background_color_selected = base02 + 0xFF181818;//lighten(base02, 3%);

	// For git diff info. i.e. in the gutter
	private static final int syntax_color_added = green;
	private static final int syntax_color_renamed = blue;
	private static final int syntax_color_modified = yellow;
	private static final int syntax_color_removed = red;

	// For language entity colors
	private static final int syntax_color_variable = blue;
	private static final int syntax_color_constant = yellow;
	private static final int syntax_color_property = yellow;
	private static final int syntax_color_value = cyan;
	private static final int syntax_color_function = blue;
	private static final int syntax_color_method = blue;
	private static final int syntax_color_class = blue;
	private static final int syntax_color_keyword = green;
	private static final int syntax_color_tag = blue;
	private static final int syntax_color_attribute = syntax_comment_color;
	private static final int syntax_color_import = red;
	private static final int syntax_color_snippet = syntax_color_keyword;

	private static final Style comment = new Style();
	private static final Style string_quoted = new Style();
	private static final Style string_regexp = new Style();
	private static final Style constant_numeric = new Style();
	private static final Style constant_language = new Style();
	private static final Style constant_character = new Style();
	private static final Style constant_other = new Style();
	private static final Style variable = new Style();
	private static final Style keyword = new Style();
	private static final Style storage = new Style();
	private static final Style entity_name_class = new Style();
	private static final Style entity_name_type = new Style();
	private static final Style entity_name_function = new Style();
	private static final Style entity_otherattribute_name = new Style();
	private static final Style support_function = new Style();
	private static final Style support_function_builtin = new Style();
	private static final Style support_type = new Style();
	private static final Style support_class = new Style();
	private static final Style tag_entityname = new Style();
	private static final Style tag_punctuationdefinition_html = new Style();
	private static final Style tag_punctuationdefinition_begin = new Style();
	private static final Style tag_punctuationdefinition_end = new Style();
	private static final Style invalid_deprecated = new Style();
	private static final Style invalid_illegal = new Style();
	private static final Style none = new Style();


	private static final Style invisible_character = new Style();

	private static final Style cursor = new Style();

	private static final Style bracket_matcher = new Style();

	private static final Style whitespace = new Style();


	private static Map<String, Style> styleMap = new HashMap<>();

	static {
		comment.color = syntax_comment_color;
		comment.fontStyle = Typeface.ITALIC;
		string_quoted.color = cyan;
		string_regexp.color = red;
		constant_numeric.color = magenta;
		constant_language.color = yellow;
		constant_character.color = orange;
		constant_other.color = orange;
		variable.color = blue;
		keyword.color = green;
		storage.color = green;
		entity_name_class.color = blue;
		entity_name_type.color = blue;
		entity_name_function.color = blue;
		entity_otherattribute_name.color = syntax_subtle_color;
		support_function.color = blue;
		support_function_builtin.color = green;
		support_type.color = green;
		support_class.color = green;
		tag_entityname.color = blue;
		tag_punctuationdefinition_html.color = syntax_comment_color;
		tag_punctuationdefinition_begin.color = syntax_comment_color;
		tag_punctuationdefinition_end.color = syntax_comment_color;
		invalid_deprecated.color = yellow;
		invalid_deprecated.isUnderline = true;
		invalid_illegal.color = red;
		invalid_illegal.isUnderline = true;

		none.color = syntax_text_color;

		invisible_character.color = syntax_invisible_character_color;

		cursor.color = syntax_cursor_color;

		bracket_matcher.color = magenta;

		whitespace.color = base03 + 0xFF101010;



		styleMap.put("comment", comment);
		styleMap.put("string.quoted", string_quoted);
		styleMap.put("string.regexp", string_regexp);
		styleMap.put("constant.numeric", constant_numeric);
		styleMap.put("constant.language", constant_language);
		styleMap.put("constant.character", constant_character);
		styleMap.put("constant.other", constant_other);
		styleMap.put("variable", variable);
		styleMap.put("keyword", keyword);
		styleMap.put("storage", storage);
		styleMap.put("entity.name.class", entity_name_class);
		styleMap.put("entity.name.type", entity_name_type);
		styleMap.put("entity.name.function", entity_name_function);
		styleMap.put("entity.otherattribute.name", entity_otherattribute_name);
		styleMap.put("support.function", support_function);
		styleMap.put("support.function.builtin", support_function_builtin);
		styleMap.put("support.type", support_type);
		styleMap.put("support.class", support_class);
		styleMap.put("tag.entityname", tag_entityname);
		styleMap.put("tag.punctuationdefinition.html", tag_punctuationdefinition_html);
		styleMap.put("tag.punctuationdefinition.begin", tag_punctuationdefinition_begin);
		styleMap.put("tag.punctuationdefinition.end", tag_punctuationdefinition_end);
		styleMap.put("invalid.deprecated", invalid_deprecated);
		styleMap.put("invalid.illegal", invalid_illegal);
		styleMap.put("cursor", cursor);
		styleMap.put("bracket.matcher", bracket_matcher);
	}

	@NonNull
	@Override
	public Style getStyle(String scope) {
		Style style = styleMap.get(scope);
		if (style == null) return none;
		return style;
	}

	@NonNull
	public Style getWhitespaceStyle() {
		return invisible_character;
	}

	@Override
	public int getTextColor() {
		return syntax_text_color;
	}

	@Override
	public int getBackgroundColor() {
		return syntax_background_color;
	}

	@Override
	public int getSelectionColor() {
		return syntax_selection_color;
	}

	@Override
	public int getGutterColor() {
		return syntax_gutter_background_color;
	}

	@Override
	public int getGutterColorSelected() {
		return syntax_gutter_background_color_selected;
	}

	@Override
	public int getGutterTextColor() {
		return syntax_gutter_text_color;
	}

	@Override
	public int getGutterTextColorSelected() {
		return syntax_gutter_text_color_selected;
	}
}
