/*
 * Copyright (c) 2013 Tah Wei Hoon.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Apache License Version 2.0,
 * with full text available at http://www.apache.org/licenses/LICENSE-2.0.html
 *
 * This software is provided "as is". Use at your own risk.
 */
package ru.kolotnev.codoma;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.webkit.WebView;

public class HelpDialogFragment extends DialogFragment {
	public static final String TAG = "HelpDialogFragment";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View v = View.inflate(getContext(), R.layout.dialog_help, null);

		WebView contents = v.findViewById(R.id.help_content);
		contents.getSettings().setBuiltInZoomControls(true);

		contents.loadUrl(determineHelpFile());
		return new AlertDialog.Builder(getContext())
				.setTitle(R.string.help_title)
				.setView(v)
				.create();
	}

	private String determineHelpFile() {
		String lang = getResources().getConfiguration().locale.getLanguage();
		String helpFile;

		//I hate hard-coding
		switch (lang) {
			case "fr":
				helpFile = "file:///android_asset/help_fr.html";
				break;
			case "es":
				helpFile = "file:///android_asset/help_es.html";
				break;
			case "de":
				helpFile = "file:///android_asset/help_de.html";
				break;
			case "zh":
				String country = getResources().getConfiguration().locale.getCountry();
				if (country.equals("TW") || country.equals("HK")) {
					helpFile = "file:///android_asset/help_zh_tw.html";
				} else {
					helpFile = "file:///android_asset/help_zh_cn.html";
				}
				break;
			default:
				helpFile = "file:///android_asset/help.html";
				break;
		}
		return helpFile;
	}
}
