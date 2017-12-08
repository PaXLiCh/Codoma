package ru.kolotnev.codoma;

import android.content.Context;
import android.preference.DialogPreference;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import org.adw.library.widgets.discreteseekbar.DiscreteSeekBar;

/**
 * Dialog for selecting font size.
 */
public class FontSizeDialogPreference extends DialogPreference
		implements DiscreteSeekBar.OnProgressChangeListener {
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";
	private static final int DEFAULT = 16;
	private final Context context;
	private final int valueDefault;
	private int value;
	private TextView textViewSample;
	private DiscreteSeekBar seekBar;

	public FontSizeDialogPreference(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;

		valueDefault = attrs.getAttributeIntValue(ANDROID_NS, "defaultValue", DEFAULT);
	}

	@Override
	protected View onCreateDialogView() {
		View v = super.onCreateDialogView();
		textViewSample = v.findViewById(R.id.text_sample);
		textViewSample.setTypeface(PreferenceHelper.getFont(context));
		seekBar = v.findViewById(R.id.seekBar);
		seekBar.setOnProgressChangeListener(this);

		if (shouldPersist())
			value = getPersistedInt(valueDefault);

		seekBar.setMax(30);
		seekBar.setProgress(value);

		return v;
	}

	@Override
	protected void onBindDialogView(@NonNull View v) {
		super.onBindDialogView(v);
		seekBar.setMax(30);
		seekBar.setProgress(value);
	}

	@Override
	protected void onSetInitialValue(boolean restore, Object defaultValue) {
		super.onSetInitialValue(restore, defaultValue);
		if (restore)
			value = shouldPersist() ? getPersistedInt(valueDefault) : 0;
		else
			value = (Integer) defaultValue;
	}


	@Override
	public void onProgressChanged(DiscreteSeekBar discreteSeekBar, int i, boolean b) {
		if (shouldPersist())
			persistInt(i);
		callChangeListener(i);
		textViewSample.setTextSize(i);
	}

	@Override
	public void onStartTrackingTouch(DiscreteSeekBar discreteSeekBar) {

	}

	@Override
	public void onStopTrackingTouch(DiscreteSeekBar discreteSeekBar) {

	}
}
