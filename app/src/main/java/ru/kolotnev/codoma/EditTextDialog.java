package ru.kolotnev.codoma;

import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
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
	private EditText mEditText;

	public static EditTextDialog newInstance(final Actions action) {
		return EditTextDialog.newInstance(action, "");
	}

	public static EditTextDialog newInstance(final Actions action, final String hint) {
		final EditTextDialog f = new EditTextDialog();
		final Bundle args = new Bundle();
		args.putSerializable("action", action);
		args.putString("hint", hint);
		f.setArguments(args);
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Bundle args = getArguments();
		String title = null;
		if (args != null) {
			final Actions action = (Actions) args.getSerializable("action");
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

		View view = View.inflate(getActivity(), R.layout.dialog_edittext, null);
		mEditText = (EditText) view.findViewById(android.R.id.edit);
		mEditText.setText(getArguments().getString("hint"));
		mEditText.requestFocus();
		mEditText.setOnEditorActionListener(this);

		// Show soft keyboard automatically
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		return new AlertDialog.Builder(getActivity())
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
		EditDialogListener target = (EditDialogListener) getTargetFragment();
		if (target == null) {
			target = (EditDialogListener) getActivity();
		}
		target.onEditTextDialogEnded(mEditText.getText().toString(), getArguments().getString("hint"),
				(Actions) getArguments().getSerializable("action"));
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

	public enum Actions {
		NEW_FILE, NEW_FOLDER, RENAME
	}

	public interface EditDialogListener {
		void onEditTextDialogEnded(String result, String hint, Actions action);
	}
}
