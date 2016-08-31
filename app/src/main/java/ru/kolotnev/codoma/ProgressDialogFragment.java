package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * Special dialog fragment for progress.
 */
public class ProgressDialogFragment extends DialogFragment {
	private static final String ARG_TITLE = "title";
	private static final String ARG_MESSAGE = "message";
	private static final String ARG_PROGRESS = "progress";
	private static final String ARG_PROGRESS_TOTAL = "progress_total";

	public static ProgressDialogFragment newInstance(@StringRes int title, @StringRes int message, @PluralsRes int progress, @PluralsRes int progressTotal) {
		ProgressDialogFragment dialog = new ProgressDialogFragment();
		Bundle args = new Bundle();
		args.putInt(ARG_TITLE, title);
		args.putInt(ARG_MESSAGE, message);
		args.putInt(ARG_PROGRESS, progress);
		args.putInt(ARG_PROGRESS_TOTAL, progressTotal);
		dialog.setRetainInstance(true);
		dialog.setCancelable(false);
		dialog.setArguments(args);
		return dialog;
	}

	private TextView textMessage;
	private ProgressBar progressBar;
	private TextView textProgress;

	private View viewTotal;
	private ProgressBar progressBarTotal;
	private TextView textProgressTotal;

	private int progressRes;
	private int progressTotalRes;

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int title = args.getInt(ARG_TITLE);
		int message = args.getInt(ARG_MESSAGE);
		progressRes = args.getInt(ARG_PROGRESS);
		progressTotalRes = args.getInt(ARG_PROGRESS_TOTAL);

		Context context = getContext();

		View v = View.inflate(context, R.layout.dialog_progress, null);
		textMessage = (TextView) v.findViewById(android.R.id.message);
		textMessage.setText(message);

		viewTotal = v.findViewById(android.R.id.primary);
		progressBarTotal = (ProgressBar) viewTotal.findViewById(android.R.id.secondaryProgress);
		textProgressTotal = (TextView) viewTotal.findViewById(android.R.id.text2);

		progressBar = (ProgressBar) v.findViewById(android.R.id.progress);
		textProgress = (TextView) v.findViewById(android.R.id.text1);
		// Disable the back button
		DialogInterface.OnKeyListener keyListener = new DialogInterface.OnKeyListener() {

			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if( keyCode == KeyEvent.KEYCODE_BACK){
					return true;
				}
				return false;
			}


		};
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (title > 0)
			builder.setTitle(title);
		builder.setView(v)
				.setOnKeyListener(keyListener)
				.create();
		return builder.create();
	}

	public void setMessage(@NonNull CharSequence message) {
		textMessage.setText(message);
	}

	public void setMessage(@StringRes int message) {
		textMessage.setText(message);
	}

	public void setProgress(Integer current, Integer max) {
		progressBar.setProgress(current);
		progressBar.setMax(max);
		if (progressRes == 0) return;
		textProgress.setText(getResources().getQuantityString(progressRes, max, current, max));
	}

	public void setProgress(Integer current, Integer max, @NonNull String formattedProgress) {
		progressBar.setProgress(current);
		progressBar.setMax(max);
		textProgress.setText(formattedProgress);
	}

	public void setTotal(boolean isTotal) {
		viewTotal.setVisibility(isTotal ? View.VISIBLE : View.GONE);
	}

	public void setProgressTotal(Integer current, Integer max) {
		progressBarTotal.setProgress(current);
		progressBarTotal.setMax(max);
		if (progressTotalRes == 0) return;
		textProgressTotal.setText(getResources().getQuantityString(progressTotalRes, max, current, max));
	}

	public void setProgressTotal(Integer current, Integer max, @NonNull String formattedProgress) {
		progressBarTotal.setProgress(current);
		progressBarTotal.setMax(max);
		textProgressTotal.setText(formattedProgress);
	}
}
