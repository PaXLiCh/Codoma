package ru.kolotnev.codoma;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

public class CodomaPreferenceActivity extends PreferenceActivity
		implements SharedPreferences.OnSharedPreferenceChangeListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		Utils.setTheme(this);
		super.onCreate(savedInstanceState);
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new CodomaPreferenceFragment())
				.commit();

		PreferenceManager
				.getDefaultSharedPreferences(this)
				.registerOnSharedPreferenceChangeListener(this);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
		setResult(RESULT_OK);
	}
}
