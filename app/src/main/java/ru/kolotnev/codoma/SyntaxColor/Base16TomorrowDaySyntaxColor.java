package ru.kolotnev.codoma.SyntaxColor;

import android.graphics.Typeface;
import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Base16 Tomorrow
 * A color scheme by Chris Kempson (http://chriskempson.com)
 * Created by Pavel Kolotnev on 09.08.16.
 */
public class Base16TomorrowDaySyntaxColor extends SyntaxColor {

	// Grayscale
	private static final int black = 0xFF1d1f21; // 00
	private static final int very_dark_gray = 0xFF282a2e; // 01
	private static final int dark_gray = 0xFF373b41; // 02
	private static final int gray = 0xFF969896; // 03
	private static final int light_gray = 0xFFb4b7b4; // 04
	private static final int very_light_gray = 0xFFc5c8c6; // 05
	private static final int almost_white = 0xFFe0e0e0; // 06
	private static final int white = 0xFFffffff; // 07

	// Colors
	private static final int red = 0xFFcc6666; // 08
	private static final int orange = 0xFFde935f; // 09
	private static final int yellow = 0xFFf0c674; // 0A
	private static final int green = 0xFFb5bd68; // 0B
	private static final int cyan = 0xFF8abeb7; // 0C
	private static final int blue = 0xFF81a2be; // 0D
	private static final int purple = 0xFFb294bb; // 0E
	private static final int brown = 0xFFa3685a; // 0F

	// Official Syntax Variables

	// General colors
	private static final int syntax_text_color = black;
	private static final int syntax_cursor_color = black;
	private static final int syntax_selection_color = almost_white;
	private static final int syntax_selection_flash_color = very_dark_gray;
	private static final int syntax_background_color = white;

	// Guide colors
	private static final int syntax_wrap_guide_color = almost_white;
	private static final int syntax_indent_guide_color = almost_white;
	private static final int syntax_invisible_character_color = very_light_gray;

	// For find and replace markers
	private static final int syntax_result_marker_color = light_gray;
	private static final int syntax_result_marker_color_selected = very_light_gray;

	// Gutter colors
	private static final int syntax_gutter_text_color = light_gray;
	private static final int syntax_gutter_text_color_selected = dark_gray;
	private static final int syntax_gutter_background_color = syntax_background_color;
	private static final int syntax_gutter_background_color_selected = syntax_selection_color;

	// For git diff info. i.e. in the gutter
	private static final int syntax_color_renamed = blue;
	private static final int syntax_color_added = green;
	private static final int syntax_color_modified = orange;
	private static final int syntax_color_removed = red;

	// For language entity colors
	private static final int syntax_color_variable = red;
	private static final int syntax_color_constant = orange;
	private static final int syntax_color_property = syntax_text_color;
	private static final int syntax_color_value = green;
	private static final int syntax_color_function = blue;
	private static final int syntax_color_method = blue;
	private static final int syntax_color_class = yellow;
	private static final int syntax_color_keyword = purple;
	private static final int syntax_color_tag = red;
	private static final int syntax_color_attribute = orange;
	private static final int syntax_color_import = purple;
	private static final int syntax_color_snippet = green;


	private static final Style comment = new Style();
	private static final Style entity_name_type = new Style();
	private static final Style entity_other_inherited_class = new Style();
	private static final Style keyword = new Style();
	private static final Style keyword_control = new Style();
	private static final Style keyword_operator = new Style();
	private static final Style keyword_other_special_method = new Style();
	private static final Style keyword_other_unit = new Style();
	private static final Style storage = new Style();
	private static final Style constant = new Style();
	private static final Style constant_character_escape = new Style();
	private static final Style constant_numeric = new Style();
	private static final Style constant_other_color = new Style();
	private static final Style constant_other_symbol = new Style();
	private static final Style variable = new Style();
	private static final Style variable_interpolation = new Style();
	private static final Style variable_parameter_function = new Style();
	private static final Style invalid_illegal_background = new Style();
	private static final Style invalid_illegal = new Style();
	private static final Style string = new Style();
	private static final Style string_regexp = new Style();
	private static final Style string_source_ruby_embedded = new Style();
	private static final Style string_other_link = new Style();
	private static final Style punctuation_definition_parameters = new Style();
	private static final Style punctuation_definition_array = new Style();
	private static final Style punctuation_definition_heading = new Style();
	private static final Style punctuation_definition_identity = new Style();
	private static final Style punctuation_definition_bold = new Style();
	private static final Style punctuation_definition_italic = new Style();
	private static final Style punctuation_section_embedded = new Style();
	private static final Style support_class = new Style();
	private static final Style support_function = new Style();
	private static final Style support_function_any_method = new Style();
	private static final Style entity_name_function = new Style();
	private static final Style entity_name_class = new Style();
	private static final Style entity_name_type_class = new Style();
	private static final Style entity_name_section = new Style();
	private static final Style entity_name_tag = new Style();
	private static final Style entity_other_attribute_name = new Style();
	private static final Style entity_other_attribute_name_id = new Style();
	private static final Style meta_class = new Style();
	private static final Style meta_link = new Style();
	private static final Style meta_require = new Style();
	private static final Style meta_selector = new Style();
	private static final Style meta_separator = new Style();
	private static final Style meta_tag = new Style();
	private static final Style none = new Style();
	private static final Style markup_bold = new Style();
	private static final Style markup_changed = new Style();
	private static final Style markup_deleted = new Style();
	private static final Style markup_italic = new Style();
	private static final Style markup_heading = new Style();
	private static final Style markup_punctuation_definition_heading = new Style();
	private static final Style markup_link = new Style();
	private static final Style markup_inserted = new Style();
	private static final Style markup_quote = new Style();
	private static final Style markup_raw = new Style();
	private static final Style source_gfm_link_entity = new Style();


	private static final Style invisible_character = new Style();

	private static final Style indent_guide = new Style();

	private static final Style cursor = new Style();

	private static final Style bracket_matcher = new Style();

	private static final Style whitespace = new Style();


	private static Map<String, Style> styleMap = new HashMap<>();

	static {
		comment.color = gray;
		comment.fontStyle = Typeface.ITALIC;
		entity_name_type.color = syntax_color_class;
		entity_other_inherited_class.color = green;
		keyword.color = syntax_color_keyword;
		keyword_control.color = syntax_color_keyword;
		keyword_operator.color = syntax_text_color;
		keyword_other_special_method.color = blue;
		keyword_other_unit.color = syntax_color_attribute;
		storage.color = purple;
		constant.color = syntax_color_constant;
		constant_character_escape.color = cyan;
		constant_numeric.color = orange;
		constant_other_color.color = cyan;
		constant_other_symbol.color = green;
		variable.color = syntax_color_variable;
		variable_interpolation.color = brown;
		variable_parameter_function.color = syntax_text_color;
		invalid_illegal_background.color = red;
		invalid_illegal.color = syntax_background_color;
		string.color = syntax_color_value;
		string_regexp.color = cyan;
		string_source_ruby_embedded.color = yellow;
		string_other_link.color = red;
		punctuation_definition_parameters.color = blue;
		punctuation_definition_array.color = blue;
		punctuation_definition_heading.color = blue;
		punctuation_definition_identity.color = blue;
		punctuation_definition_bold.color = yellow;
		punctuation_definition_bold.fontStyle = Typeface.BOLD;
		punctuation_definition_italic.color = purple;
		punctuation_definition_italic.fontStyle = Typeface.ITALIC;
		punctuation_section_embedded.color = brown;
		support_class.color = syntax_color_class;
		support_function.color = cyan;
		support_function_any_method.color = syntax_color_method;
		entity_name_function.color = syntax_color_function;
		entity_name_class.color = syntax_color_class;
		entity_name_type_class.color = syntax_color_class;
		entity_name_section.color = blue;
		entity_name_tag.color = syntax_color_tag;
		entity_other_attribute_name.color = syntax_color_attribute;
		entity_other_attribute_name_id.color = blue;
		meta_class.color = syntax_color_class;
		meta_link.color = orange;
		meta_require.color = blue;
		meta_selector.color = syntax_color_keyword;
		meta_separator.color = syntax_text_color;
		meta_tag.color = syntax_text_color;
		none.color = syntax_text_color;
		markup_bold.color = orange;
		markup_bold.fontStyle = Typeface.BOLD;
		markup_changed.color = purple;
		markup_deleted.color = red;
		markup_italic.color = purple;
		markup_italic.fontStyle = Typeface.ITALIC;
		markup_heading.color = red;
		markup_punctuation_definition_heading.color = blue;
		markup_link.color = blue;
		markup_inserted.color = green;
		markup_quote.color = orange;
		markup_raw.color = green;
		source_gfm_link_entity.color = cyan;

		none.color = syntax_text_color;

		invisible_character.color = syntax_invisible_character_color;

		indent_guide.color = syntax_indent_guide_color;

		cursor.color = syntax_cursor_color;

		bracket_matcher.color = syntax_result_marker_color;

		whitespace.color = syntax_text_color + 0xFF101010;


		styleMap.put("comment", comment);
		styleMap.put("entity.name.type", entity_name_type);
		styleMap.put("entity.other.inherited.class", entity_other_inherited_class);
		styleMap.put("keyword", keyword);
		styleMap.put("keyword.control", keyword_control);
		styleMap.put("keyword.operator", keyword_operator);
		styleMap.put("keyword.other.special.method", keyword_other_special_method);
		styleMap.put("keyword.other.unit", keyword_other_unit);
		styleMap.put("storage", storage);
		styleMap.put("constant", constant);
		styleMap.put("constant.character.escape", constant_character_escape);
		styleMap.put("constant.numeric", constant_numeric);
		styleMap.put("constant.other.color", constant_other_color);
		styleMap.put("constant.other.symbol", constant_other_symbol);
		styleMap.put("variable", variable);
		styleMap.put("variable.interpolation", variable_interpolation);
		styleMap.put("variable.parameter.function", variable_parameter_function);
		styleMap.put("invalid.illegal.background", invalid_illegal_background);
		styleMap.put("invalid.illegal", invalid_illegal);
		styleMap.put("string.quoted", string);
		styleMap.put("string.regexp", string_regexp);
		styleMap.put("string.source.ruby.embedded", string_source_ruby_embedded);
		styleMap.put("string.other.link", string_other_link);
		styleMap.put("punctuation.definition.parameters", punctuation_definition_parameters);
		styleMap.put("punctuation.definition.array", punctuation_definition_array);
		styleMap.put("punctuation.definition.heading", punctuation_definition_heading);
		styleMap.put("punctuation.definition.identity", punctuation_definition_identity);
		styleMap.put("punctuation.definition.bold", punctuation_definition_bold);
		styleMap.put("punctuation.definition.italic", punctuation_definition_italic);
		styleMap.put("punctuation.section.embedded", punctuation_section_embedded);
		styleMap.put("support.class", support_class);
		styleMap.put("support.function", support_function);
		styleMap.put("support.function.any.method", support_function_any_method);
		styleMap.put("entity.name.function", entity_name_function);
		styleMap.put("entity.name.class", entity_name_class);
		styleMap.put("entity.name.type.class", entity_name_type_class);
		styleMap.put("entity.name.section", entity_name_section);
		styleMap.put("entity.name.tag", entity_name_tag);
		styleMap.put("entity.other.attribute.name", entity_other_attribute_name);
		styleMap.put("entity.other.attribute.name.id", entity_other_attribute_name_id);
		styleMap.put("meta.class", meta_class);
		styleMap.put("meta.link", meta_link);
		styleMap.put("meta.require", meta_require);
		styleMap.put("meta.selector", meta_selector);
		styleMap.put("meta.separator", meta_separator);
		styleMap.put("meta.tag", meta_tag);
		styleMap.put("none", none);
		styleMap.put("markup.bold", markup_bold);
		styleMap.put("markup.changed", markup_changed);
		styleMap.put("markup.deleted", markup_deleted);
		styleMap.put("markup.italic", markup_italic);
		styleMap.put("markup.heading", markup_heading);
		styleMap.put("markup.punctuation.definition.heading", markup_punctuation_definition_heading);
		styleMap.put("markup.link", markup_link);
		styleMap.put("markup.inserted", markup_inserted);
		styleMap.put("markup.quote", markup_quote);
		styleMap.put("markup.raw", markup_raw);
		styleMap.put("source.gfm.link.entity", source_gfm_link_entity);

		styleMap.put("cursor", cursor);
		styleMap.put("bracket.matcher", bracket_matcher);
	}

	/**
	 * This is dark theme.
	 *
	 * @return true.
	 */
	@Override
	public boolean isDark() {
		return true;
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
