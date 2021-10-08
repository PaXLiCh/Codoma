package ru.kolotnev.codoma;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.Environment;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;

import java.io.File;

/**
 * Helper for getting preferences.
 */
public final class PreferenceHelper {
	static final String DEFAULT_ENCODING = "UTF-8";

	//public static final String SD_CARD_ROOT = Environment.getExternalStorageDirectory().getAbsolutePath();

	private PreferenceHelper() {
	}

	private static SharedPreferences getPrefs(@NonNull final Context context) {
		return PreferenceManager.getDefaultSharedPreferences(context);
	}

	private static SharedPreferences.Editor getEditor(@NonNull final Context context) {
		return getPrefs(context).edit();
	}

	// Getter Methods

	private static boolean getBoolean(@NonNull final Context context,
			final int resIdKey, final int resIdDefault) {
		Resources r = context.getResources();
		return getPrefs(context).getBoolean(r.getString(resIdKey), r.getBoolean(resIdDefault));
	}

	private static String getString(@NonNull final Context context,
			final int resIdKey, final int resIdDefault) {
		Resources r = context.getResources();
		return getPrefs(context).getString(r.getString(resIdKey), r.getString(resIdDefault));
	}

	private static int getInt(@NonNull final Context context, @StringRes final int resIdKey, final int resIdDefault) {
		Resources r = context.getResources();
		return getPrefs(context).getInt(r.getString(resIdKey), r.getInteger(resIdDefault));
	}

	//region View

	/**
	 * Is interface and syntax highlight must be dark.
	 *
	 * @param context
	 * 		Context for getting preference.
	 *
	 * @return Is color scheme dark.
	 */
	static boolean isDarkTheme(@NonNull final Context context) {
		return getBoolean(context, R.string.settings_view_dark_key, R.bool.settings_view_dark_default);
	}

	static int getTheme(@NonNull final Context context) {
		return getPrefs(context).getInt("theme", 0);
	}

	/**
	 * Show line numbers on gutter.
	 *
	 * @param context
	 * 		Context for getting preference.
	 *
	 * @return Is line numbers must be shown on gutter.
	 */
	static boolean getLineNumbers(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_view_line_numbers_key,
				R.bool.settings_view_line_numbers_default);
	}

	private static boolean getHighlightCurrentRow(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_view_highlight_current_row_key,
				R.bool.settings_view_highlight_current_row_default);
	}

	/**
	 * Is file must be loaded by pages.
	 *
	 * @param context
	 * 		Context for getting preference.
	 *
	 * @return Enable or disable paging system.
	 */
	static boolean getSplitText(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_file_page_key,
				R.bool.settings_file_page_default);
	}

	/**
	 * Is enabled or disabled syntax coloring for text.
	 *
	 * @param context
	 * 		Context for getting preference.
	 *
	 * @return Enabled or disabled syntax coloring.
	 */
	static boolean getSyntaxHighlight(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_view_syntax_highlight_key,
				R.bool.settings_view_syntax_highlight_default);
	}

	static boolean getWrapContent(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_view_wordwrap_key,
				R.bool.settings_view_wordwrap_default);
	}

	static int getFontSize(@NonNull final Context context) {
		return getInt(context,
				R.string.settings_view_font_size_key,
				R.integer.settings_view_font_size_default);
	}

	static boolean getWhitespaces(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_view_whitespace_key,
				R.bool.settings_view_whitespace_default);
	}

	static int getTabWidth(@NonNull final Context context) {
		return Integer.parseInt(getString(context,
				R.string.settings_view_tab_width_key,
				R.string.settings_view_tab_width_default));
	}

	static int getUpdateDelay(@NonNull final Context context) {
		return Integer.parseInt(getString(context,
				R.string.settings_view_update_delay_key,
				R.string.settings_view_update_delay_default));
	}

	// endregion

	// region Editing

	static boolean getTabToSpaces(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_edit_tab_to_spaces_key,
				R.bool.settings_edit_tab_to_spaces_default);
	}

	static boolean getAutoIndent(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_edit_indent_key,
				R.bool.settings_edit_indent_default);
	}

	// endregion

	public static boolean getUseMonospace(@NonNull final Context context) {
		return true;//getPrefs(context).getBoolean("use_monospace", false);
	}

	public static boolean getUseAccessoryView(@NonNull final Context context) {
		return getPrefs(context).getBoolean("accessory_view", true);
	}

	// region FILE IO

	static LineReader.LineEnding getLineEnding(@NonNull final Context context) {
		String lineEndingString = getString(context,
				R.string.settings_file_line_endings_key,
				R.string.settings_file_line_endings_default);
		return LineReader.LineEnding.valueOf(lineEndingString);
	}

	static boolean getUseStorageAccessFramework(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_file_saf_key,
				R.bool.settings_file_saf_default);
	}

	static boolean getSuggestionActive(@NonNull final Context context) {
		return getPrefs(context).getBoolean("suggestion_active", false);
	}

	static String getEncoding(@NonNull final Context context) {
		return getString(context,
				R.string.settings_file_encoding_key,
				R.string.settings_file_encoding_default);
	}

	static String getEncodingFallback(@NonNull final Context context) {
		return getString(context,
				R.string.settings_file_encoding_fallback_key,
				R.string.settings_file_encoding_fallback_default);
	}

	private boolean getAutoBackup(@NonNull final Context context) {
		return getBoolean(context,
				R.string.settings_file_auto_backup_key,
				R.bool.settings_file_auto_backup_default);
	}

	// endregion

	static String defaultFolder(@NonNull final Context context) {
		String folder;
		File externalFolder = context.getExternalFilesDir(null);

		// TODO: какая-то замуть с киткатом
		if (externalFolder != null && Device.isKitKatApi()) {
			folder = externalFolder.getAbsolutePath();
		} else {
			folder = Environment.getExternalStorageDirectory().getAbsolutePath();
		}
		//folder = context.getExternalFilesDir(null).getAbsolutePath();
		//folder = Environment.getExternalStorageDirectory().getAbsolutePath();
		return folder;
	}

	static String getWorkingFolder(@NonNull final Context context) {
		return getPrefs(context).getString("working_folder2", defaultFolder(context));
	}

	private final static String FONT_PATH_BITSTREAM_VERA = "typefaces/VeraMono.ttf";
	private final static String FONT_PATH_PROGGY_CLEAN = "typefaces/ProggyCleanSZ.ttf";

	private static final String FONT_MONOSPACE = "(Android) Monospace";
	private static final String FONT_SANS_SERIF = "(Android) Sans serif";
	private static final String FONT_SERIF = "(Android) Serif";
	private static final String FONT_BITESTREAM_VERA = "(Builtin) Bitstream Vera";
	private static final String FONT_PROGGY_CLAN = "(Builtin) Proggy Clean";

	private static final String FONT_MONOSPACE_VALUE = "monospace";
	private static final String FONT_SANS_SERIF_VALUE = "sans_serif";
	private static final String FONT_SERIF_VALUE = "serif";
	private static final String FONT_BITESTREAM_VERA_VALUE = "bitstream_vera";
	private static final String FONT_PROGGY_CLAN_VALUE = "proggy_clean";

	static final String[] FONT_ENTRIES = new String[] {
			FONT_MONOSPACE,
			FONT_SANS_SERIF,
			FONT_SERIF,
			FONT_BITESTREAM_VERA,
			FONT_PROGGY_CLAN
	};


	static final String[] FONT_ENTRY_VALUES = new String[] {
			FONT_MONOSPACE_VALUE,
			FONT_SANS_SERIF_VALUE,
			FONT_SERIF_VALUE,
			FONT_BITESTREAM_VERA_VALUE,
			FONT_PROGGY_CLAN_VALUE
	};


	@Nullable
	static Typeface getFont(@NonNull final Context context) {
		String font = getString(context,
				R.string.settings_view_font_key,
				R.string.settings_view_font_default);

		switch (font) {
			case FONT_MONOSPACE_VALUE:
				return Typeface.MONOSPACE;
			case FONT_SANS_SERIF_VALUE:
				return Typeface.SANS_SERIF;
			case FONT_SERIF_VALUE:
				return Typeface.SERIF;
			case FONT_BITESTREAM_VERA_VALUE:
				return Typeface.createFromAsset(context.getAssets(), FONT_PATH_BITSTREAM_VERA);
			case FONT_PROGGY_CLAN_VALUE:
				return Typeface.createFromAsset(context.getAssets(), FONT_PATH_PROGGY_CLEAN);
			default:
				return null;
		}
	}

	@NonNull
	public static String[] getSavedPaths(@NonNull final Context context) {
		return getPrefs(context).getString("savedPaths2", "").split(",");
	}

	public static boolean getPageSystemButtonsPopupShown(@NonNull final Context context) {
		return getPrefs(context).getBoolean("page_system_button_popup_shown", false);
	}

	public static boolean getAutoSave(@NonNull final Context context) {
		return getPrefs(context).getBoolean("auto_save", false);
	}

	static boolean getReadOnly(@NonNull final Context context) {
		return getPrefs(context).getBoolean("read_only", false);
	}

	public static boolean getIgnoreBackButton(@NonNull final Context context) {
		return getPrefs(context).getBoolean("ignore_back_button", false);
	}


	// Setter methods

	public static void setUseMonospace(@NonNull final Context context, boolean value) {
		getEditor(context).putBoolean("use_monospace", value).commit();
	}

	public static void setUseAccessoryView(@NonNull final Context context, boolean value) {
		getEditor(context).putBoolean("accessory_view", value).commit();
	}

	public static void setUseStorageAccessFramework(Context context, boolean value) {
		getEditor(context).putBoolean("storage_access_framework", value).commit();
	}

	public static void setLineNumbers(Context context, boolean value) {
		getEditor(context).putBoolean("editor_line_numbers", value).commit();
	}

	public static void setSyntaxHighlight(Context context, boolean value) {
		getEditor(context).putBoolean("editor_syntax_highlight", value).commit();
	}

	public static void setWrapContent(Context context, boolean value) {
		getEditor(context).putBoolean("editor_wrap_content", value).commit();
	}

	public static void setAutoencoding(Context context, boolean value) {
		getEditor(context).putBoolean("autoencoding", value).commit();
	}

	public static void setFontSize(Context context, int value) {
		getEditor(context).putInt("font_size", value).commit();
	}

	static void setWorkingFolder(Context context, String value) {
		getEditor(context).putString("working_folder2", value).commit();
	}

	public static void setSavedPaths(Context context, StringBuilder stringBuilder) {
		getEditor(context).putString("savedPaths2", stringBuilder.toString()).commit();
	}

	public static void setPageSystemButtonsPopupShown(Context context, boolean value) {
		getEditor(context).putBoolean("page_system_button_popup_shown", value).commit();
	}

	public static void setReadOnly(Context context, boolean value) {
		getEditor(context).putBoolean("read_only", value).commit();
	}

	public static void setTheme(Context context, int value) {
		getEditor(context).putInt("theme", value).commit();
	}

	public static void setSuggestionsActive(Context context, boolean value) {
		getEditor(context).putBoolean("suggestion_active", value).commit();
	}

	public static void setAutoSave(Context context, boolean value) {
		getEditor(context).putBoolean("auto_save", value).commit();
	}

	public static void setIgnoreBackButton(Context context, boolean value) {
		getEditor(context).putBoolean("ignore_back_button", value).commit();
	}

	public static void setSplitText(Context context, boolean value) {
		getEditor(context).putBoolean("page_system_active", value).commit();
	}

	public static void setEncoding(Context context, String value) {
		getEditor(context).putString("editor_encoding", value).commit();
	}
}
