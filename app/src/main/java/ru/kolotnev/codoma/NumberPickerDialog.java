package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.NumberPicker;

public class NumberPickerDialog extends DialogFragment {
	private NumberPicker mSeekBar;

	public static NumberPickerDialog newInstance(final Actions action) {
		return NumberPickerDialog.newInstance(action, 0, 50, 100);
	}

	public static NumberPickerDialog newInstance(final Actions action, final int min, final int current, final int max) {
		final NumberPickerDialog f = new NumberPickerDialog();
		final Bundle args = new Bundle();
		args.putSerializable("action", action);
		args.putInt("min", min);
		args.putInt("current", current);
		args.putInt("max", max);
		f.setArguments(args);
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {

		Actions action = (Actions) getArguments().getSerializable("action");
		int title;
		switch (action) {
			case FONT_SIZE:
				title = R.string.settings_label_font;//font_size;
				break;
			case GO_TO_PAGE:
				title = R.string.menu_main_go_to_page;
				break;
			case GO_TO_LINE:
				title = R.string.menu_main_go_to_line;
				break;
			default:
				title = R.string.app_name;//nome_app_turbo_editor;
				break;
		}

		/*View view = new DialogHelper.Builder(getActivity())
				.setTitle(title)
				.setView(R.layout.dialog_fragment_seekbar)
				.createSkeletonView();*/

		View view = View.inflate(getActivity(), R.layout.dialog_seekbar, null);

		this.mSeekBar = (NumberPicker) view.findViewById(android.R.id.input);
		this.mSeekBar.setMaxValue(getArguments().getInt("max"));
		this.mSeekBar.setMinValue(getArguments().getInt("min"));
		this.mSeekBar.setValue(getArguments().getInt("current"));
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
					(Actions) getArguments().getSerializable("action"),
					mSeekBar.getValue()
			);
		}
		this.dismiss();
	}

	public enum Actions {
		FONT_SIZE, GO_TO_PAGE, GO_TO_LINE
	}

	public interface INumberPickerDialog {
		void onNumberPickerDialogDismissed(Actions action, int value);
	}
}
