package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.TextView;

/**
 * Dialog fragment with the radial progress bar for the indeterminate background tasks.
 */
public class IndeterminateProgressDialogFragment extends DialogFragment {
	private static final String ARG_MESSAGE = "message";

	public static IndeterminateProgressDialogFragment newInstance(@StringRes int message) {
		Bundle args = new Bundle();
		args.putInt(ARG_MESSAGE, message);
		IndeterminateProgressDialogFragment dialog = new IndeterminateProgressDialogFragment();
		dialog.setCancelable(false);
		dialog.setArguments(args);
		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Context context = getContext();
		Bundle args = getArguments();
		int message = args.getInt(ARG_MESSAGE);
		View v = View.inflate(context, R.layout.dialog_progress_indeterminate, null);
		TextView textView = (TextView) v.findViewById(android.R.id.message);
		textView.setText(message);
		return new AlertDialog.Builder(context)
				.setView(v)
				.create();
	}
}
