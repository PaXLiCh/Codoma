package ru.kolotnev.codoma;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class CodomaPreferenceActivity extends PreferenceActivity {
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getFragmentManager()
				.beginTransaction()
				.replace(android.R.id.content, new CodomaPreferenceFragment())
				.commit();
	}
}
