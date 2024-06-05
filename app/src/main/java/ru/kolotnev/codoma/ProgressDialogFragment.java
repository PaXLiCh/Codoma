package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.PluralsRes;
import androidx.annotation.StringRes;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.math.BigInteger;

/**
 * Special dialog fragment for progress.
 */
public class ProgressDialogFragment extends DialogFragment {
	private static final String ARG_TITLE = "title";
	private static final String ARG_MESSAGE = "message";
	private static final String ARG_PROGRESS = "progress";
	private static final String ARG_PROGRESS_TOTAL = "progress_total";
	private static final BigInteger TWO_BYTES = BigInteger.valueOf(2);
	private TextView textMessage;
	private ProgressBar progressBar;
	private TextView textProgress;
	private View viewTotal;
	private ProgressBar progressBarTotal;
	private TextView textProgressTotal;
	private int progressRes;
	private int progressTotalRes;
	@NonNull
	private BigInteger fileSizeDivider = BigInteger.ONE;
	private int powerOfSize = 0;

	@NonNull
	public static ProgressDialogFragment newInstance(
			@StringRes int title,
			@StringRes int message,
			@PluralsRes int progress,
			@PluralsRes int progressTotal) {
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

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle args = getArguments();
		int title = 0;
		String message = "";
		if (args != null) {
			title = args.getInt(ARG_TITLE);
			message = getString(args.getInt(ARG_MESSAGE));
			progressRes = args.getInt(ARG_PROGRESS);
			progressTotalRes = args.getInt(ARG_PROGRESS_TOTAL);
		}

		Context context = getContext();

		View v = View.inflate(context, R.layout.dialog_progress, null);
		textMessage = v.findViewById(android.R.id.message);
		textMessage.setText(message);

		viewTotal = v.findViewById(android.R.id.primary);
		progressBarTotal = viewTotal.findViewById(android.R.id.secondaryProgress);
		textProgressTotal = viewTotal.findViewById(android.R.id.text2);

		progressBar = v.findViewById(android.R.id.progress);
		textProgress = v.findViewById(android.R.id.text1);
		// Disable the back button
		DialogInterface.OnKeyListener keyListener = (dialog, keyCode, event) -> keyCode == KeyEvent.KEYCODE_BACK;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);
		if (title > 0)
			builder.setTitle(title);
		builder.setView(v)
				.setOnKeyListener(keyListener)
				.create();
		return builder.create();
	}

	public void setMessage(@NonNull final CharSequence message) {
		textMessage.setText(message);
	}

	public void setMessage(@StringRes final int message) {
		textMessage.setText(message);
	}

	public void setProgress(final int current, final int max) {
		progressBar.setProgress(current);
		progressBar.setMax(max);
		if (progressRes == 0) return;
		textProgress.setText(getResources().getQuantityString(progressRes, max, current, max));
	}

	public void setProgress(final int current, final int max, @NonNull final String formattedProgress) {
		progressBar.setProgress(current);
		progressBar.setMax(max);
		textProgress.setText(formattedProgress);
	}

	public void setTotal(final boolean isTotal) {
		viewTotal.setVisibility(isTotal ? View.VISIBLE : View.GONE);
	}

	/**
	 * Adjust representation of file size.
	 *
	 * @param sizeOfFile
	 * 		Maximal file size.
	 */
	public void calibrateFileSizeMeter(final long sizeOfFile) {
		final BigInteger size = BigInteger.valueOf(sizeOfFile);
		int powerChecked = 0;
		int powerToCheck = 0;
		BigInteger b;
		boolean isNeedToDivide = true;
		while (isNeedToDivide) {
			b = TWO_BYTES.pow(powerToCheck);
			if (size.divide(b).compareTo(BigInteger.ZERO) > 0) {
				powerChecked = powerToCheck;
				powerToCheck += 10;
			} else {
				isNeedToDivide = false;
				powerOfSize = powerChecked / 10;
				fileSizeDivider = TWO_BYTES.pow(powerChecked);
			}
		}
	}

	/**
	 * Set progress in bytes.
	 *
	 * @param bytesRead
	 * 		Bytes read.
	 * @param bytesTotal
	 * 		Total number of bytes.
	 */
	public void setProgressInBytes(final long bytesRead, final long bytesTotal) {
		final BigInteger bytesReadInt = BigInteger.valueOf(bytesRead).divide(fileSizeDivider);
		final BigInteger bytesTotalInt = BigInteger.valueOf(bytesTotal).divide(fileSizeDivider);
		Context context = getContext();
		String formatted;
		if (context == null) {
			formatted = bytesReadInt + " / " + org.apache.commons.io.FileUtils.byteCountToDisplaySize(bytesTotalInt);
		} else {
			Resources resources = context.getResources();
			formatted = getString(
					R.string.dialog_progress_size,
					bytesReadInt,
					bytesTotalInt,
					resources.getStringArray(R.array.dialog_progress_size_prefix)[powerOfSize]);
		}
		setProgress(bytesReadInt.intValue(), bytesTotalInt.intValue(), formatted);
	}

	public void setProgressTotal(final int current, final int max) {
		progressBarTotal.setProgress(current);
		progressBarTotal.setMax(max);
		if (progressTotalRes == 0) return;
		textProgressTotal.setText(getResources().getQuantityString(progressTotalRes, max, current, max));
	}

	public void setProgressTotal(final int current, final int max, @NonNull final String formattedProgress) {
		progressBarTotal.setProgress(current);
		progressBarTotal.setMax(max);
		textProgressTotal.setText(formattedProgress);
	}
}
