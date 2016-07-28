package ru.kolotnev.codoma;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.PreferenceFragment;

/**
 * List of preferences.
 */
public class CodomaPreferenceFragment extends PreferenceFragment {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);

		ListPreference listPreferenceEncoding = (ListPreference) findPreference(
				getString(R.string.settings_file_encoding_key));

		ListPreference listPreferenceEncodingFallback = (ListPreference) findPreference(
				getString(R.string.settings_file_encoding_fallback_key));

		String[][] supportedEncodings = FileUtils.getFullSupportedEncodings();
		listPreferenceEncoding.setEntryValues(supportedEncodings[0]);
		listPreferenceEncoding.setEntries(supportedEncodings[1]);

		listPreferenceEncodingFallback.setEntryValues(supportedEncodings[1]);
		listPreferenceEncodingFallback.setEntries(supportedEncodings[1]);

		ListPreference listPreferenceFont = (ListPreference) findPreference(
				getString(R.string.settings_view_font_key));
		listPreferenceFont.setEntries(PreferenceHelper.FONT_ENTRIES);
		listPreferenceFont.setEntryValues(PreferenceHelper.FONT_ENTRY_VALUES);
	}
}
