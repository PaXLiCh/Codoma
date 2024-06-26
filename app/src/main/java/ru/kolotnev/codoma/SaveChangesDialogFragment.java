package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;

public class SaveChangesDialogFragment extends DialogFragment {
	final static String ARG_FILE_NAME = "file_name";
	private SaveChangesInFileDialogListener listener;

	public SaveChangesDialogFragment() { /* do nothing */ }

	/**
	 * Create new dialog to save or decline changes in file with fileName.
	 *
	 * @param fileName
	 * 		Visible file name.
	 *
	 * @return Dialog for choosing.
	 */
	public static SaveChangesDialogFragment newInstance(final String fileName) {
		SaveChangesDialogFragment dialog = new SaveChangesDialogFragment();
		Bundle args = new Bundle();
		args.putString(ARG_FILE_NAME, fileName);
		dialog.setArguments(args);
		return dialog;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Context context = getActivity();
		listener = (SaveChangesInFileDialogListener) getTargetFragment();
		if (listener == null) {
			listener = (SaveChangesInFileDialogListener) context;
		}

		final String fileName = getArguments().getString(ARG_FILE_NAME);

		return new AlertDialog.Builder(context)
				.setTitle(R.string.menu_main_save)
				.setMessage(getString(R.string.save_changes, fileName))
				.setPositiveButton(R.string.menu_main_save,
                        (dialog, which) -> {
                            if (listener != null)
                                listener.userWantToSave();
                        }
                )
				.setNeutralButton(android.R.string.cancel,
                        (dialog, which) -> {
                            if (listener != null)
                                listener.userDoesNotWantToDoAny();
                        }
                )
				.setNegativeButton(R.string.no,
                        (dialog, which) -> {
                            if (listener != null)
                                listener.userDoesNotWantToSave();
                        }
                )
				.create();
	}

	interface SaveChangesInFileDialogListener {
		void userWantToSave();

		void userDoesNotWantToSave();

		void userDoesNotWantToDoAny();
	}
}
