package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.ScrollView;

/**
 * Dialog with info about app.
 */
public class AboutDialogFragment extends DialogFragment {
	public static final String TAG = "dialog_about";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();
		ScrollView aboutText = (ScrollView) View.inflate(context, R.layout.dialog_about, null);
		return new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_about)
				.setView(aboutText)
				.setNeutralButton(android.R.string.ok, null)
				.create();
	}
}
