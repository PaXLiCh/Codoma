package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerDialog extends DialogFragment {
	private static final String ARG_ACTION = "action";
	private static final String ARG_MIN = "min";
	private static final String ARG_MAX = "max";
	private static final String ARG_CURRENT = "current";
	private NumberPicker mSeekBar;

	public static NumberPickerDialog newInstance(final Actions action) {
		return NumberPickerDialog.newInstance(action, 0, 50, 100);
	}

	public static NumberPickerDialog newInstance(final Actions action, final int min, final int current, final int max) {
		final NumberPickerDialog f = new NumberPickerDialog();
		final Bundle args = new Bundle();
		args.putSerializable(ARG_ACTION, action);
		args.putInt(ARG_MIN, min);
		args.putInt(ARG_CURRENT, current);
		args.putInt(ARG_MAX, max);
		f.setArguments(args);
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.dialog_seekbar, null);
		mSeekBar = (NumberPicker) view.findViewById(android.R.id.input);
		int title = R.string.app_name;

		Bundle args = getArguments();
		if (args == null) {
			Log.e(CodomaApplication.TAG, "Number picker dialog created without arguments.");
		} else {
			Actions action = (Actions) args.getSerializable(ARG_ACTION);
			switch (action != null ? action : Actions.NONE) {
				case FONT_SIZE:
					title = R.string.settings_view_font_size_title;
					break;
				case GO_TO_PAGE:
					title = R.string.menu_main_go_to_page;
					break;
				case GO_TO_LINE:
					title = R.string.menu_main_go_to_line;
					break;
				default:
					title = R.string.app_name;
					break;
			}

			mSeekBar.setMaxValue(args.getInt(ARG_MAX));
			mSeekBar.setMinValue(args.getInt(ARG_MIN));
			mSeekBar.setValue(args.getInt(ARG_CURRENT));
		}
		return new AlertDialog.Builder(getActivity())
				.setTitle(title)
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) { returnData(); }
						}
				)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	void returnData() {
		INumberPickerDialog target = (INumberPickerDialog) getTargetFragment();
		if (target == null) {
			if (getActivity() instanceof INumberPickerDialog)
				target = (INumberPickerDialog) getActivity();
		}
		if (target != null) {
			target.onNumberPickerDialogDismissed(
					(Actions) getArguments().getSerializable(ARG_ACTION),
					mSeekBar.getValue()
			);
		}
		this.dismiss();
	}

	enum Actions {
		NONE, FONT_SIZE, GO_TO_PAGE, GO_TO_LINE
	}

	interface INumberPickerDialog {
		void onNumberPickerDialogDismissed(Actions action, int value);
	}
}
