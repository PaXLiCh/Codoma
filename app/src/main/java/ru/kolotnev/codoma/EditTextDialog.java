package ru.kolotnev.codoma;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link ru.kolotnev.codoma.EditTextDialog.EditDialogListener} interface
 * to handle interaction events.
 * Use the {@link EditTextDialog#newInstance} factory method to
 * create an instance of this fragment.
 */
public class EditTextDialog extends DialogFragment implements TextView.OnEditorActionListener {
	private static final String ARG_ACTION = "action";
	private static final String ARG_HINT = "hint";
	private EditText mEditText;

	public static EditTextDialog newInstance(final Actions action) {
		return EditTextDialog.newInstance(action, "");
	}

	public static EditTextDialog newInstance(final Actions action, final String hint) {
		final EditTextDialog f = new EditTextDialog();
		final Bundle args = new Bundle();
		args.putSerializable(ARG_ACTION, action);
		args.putString(ARG_HINT, hint);
		f.setArguments(args);
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		String title = null;
		if (args != null) {
			final Actions action = (Actions) args.getSerializable(ARG_ACTION);
			if (action == null) {
				title = null;
			} else {
				switch (action) {
					case NEW_FILE:
						title = getString(R.string.dialog_edit_name_title_file);
						break;
					case NEW_FOLDER:
						title = getString(R.string.dialog_edit_name_title_folder);
						break;
					case RENAME:
						title = getString(R.string.dialog_edit_name_title_rename);
						break;
					default:
						title = null;
						break;
				}
			}
		}

		FragmentActivity activity = getActivity();
		View view = View.inflate(activity, R.layout.dialog_edittext, null);
		mEditText = view.findViewById(android.R.id.edit);
		mEditText.setText(getArguments().getString(ARG_HINT));
		mEditText.requestFocus();
		mEditText.setOnEditorActionListener(this);

		// Show soft keyboard automatically
		if (activity != null) {
			activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}

		return new AlertDialog.Builder(activity)
				.setTitle(title)
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								returnData();
							}
						}
				)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	void returnData() {
		Bundle args = getArguments();
		EditDialogListener target = (EditDialogListener) getTargetFragment();
		if (target == null) {
			target = (EditDialogListener) getActivity();
		}
		if (target != null && args != null) {
			target.onEditTextDialogEnded(
					mEditText.getText().toString(),
					args.getString(ARG_HINT),
					(Actions) args.getSerializable(ARG_ACTION));
		}
		this.dismiss();
	}

	@Override
	public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
		if (EditorInfo.IME_ACTION_DONE == actionId) {
			returnData();
			return true;
		}
		return false;
	}

	enum Actions {
		NEW_FILE, NEW_FOLDER, RENAME
	}

	interface EditDialogListener {
		void onEditTextDialogEnded(String result, String hint, Actions action);
	}
}
