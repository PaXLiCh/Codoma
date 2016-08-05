package ru.kolotnev.codoma;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Rect;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TableLayout;
import android.widget.TableRow;

public class FindPanel extends TableLayout {
	private MainActivity _callback;
	private EditText _findText;

	private ImageButton _toggleReplaceBar;
	private TableRow _replaceBar;
	private EditText _replaceText;

	private AlertDialog _optionsDialog;
	private CheckBox _caseSensitive;
	private CheckBox _matchWholeWord;

	public FindPanel(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.find_panel, this);

		_findText = (EditText) findViewById(R.id.find_panel_search_text);
		ImageButton _find = (ImageButton) findViewById(R.id.find_panel_find_next);
		ImageButton _findBackwards = (ImageButton) findViewById(R.id.find_panel_find_prev);
		_toggleReplaceBar = (ImageButton) findViewById(R.id.find_panel_replace_bar_toggle);
		_replaceBar = (TableRow) findViewById(R.id.find_panel_replace_bar);
		_replaceText = (EditText) findViewById(R.id.find_panel_replace_text);
		ImageButton _displayOptions = (ImageButton) findViewById(R.id.find_panel_settings);
		ImageButton _replace = (ImageButton) findViewById(R.id.find_panel_replace);
		ImageButton _replaceAll = (ImageButton) findViewById(R.id.find_panel_replace_all);

		createOptionsDialog(context);

		_toggleReplaceBar.setImageResource(R.drawable.arrow_down);
		_replaceBar.setVisibility(GONE);
		_toggleReplaceBar.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				if (_replaceBar.getVisibility() == VISIBLE) {
					_replaceBar.setVisibility(GONE);
					_toggleReplaceBar.setImageResource(R.drawable.arrow_down);
				} else {
					_replaceBar.setVisibility(VISIBLE);
					_toggleReplaceBar.setImageResource(R.drawable.arrow_up);
				}
			}
		});

		_displayOptions.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				displaySettings();
			}
		});

		_findText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						_callback.find(_findText.getText().toString(),
								_caseSensitive.isChecked(),
								_matchWholeWord.isChecked());
					}
					return true;
				}
				return false;
			}
		});

		_replaceText.setOnKeyListener(new View.OnKeyListener() {
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_ENTER) {
					if (event.getAction() == KeyEvent.ACTION_DOWN) {
						_callback.replaceSelection(_replaceText.getText().toString());
					}
					return true;
				}
				return false;
			}
		});

		_find.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_callback.find(_findText.getText().toString(),
						_caseSensitive.isChecked(),
						_matchWholeWord.isChecked());
			}
		});

		_findBackwards.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_callback.findBackwards(_findText.getText().toString(),
						_caseSensitive.isChecked(),
						_matchWholeWord.isChecked());
			}
		});

		_replace.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_callback.replaceSelection(_replaceText.getText().toString());
			}
		});

		_replaceAll.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				_callback.replaceAll(_findText.getText().toString(),
						_replaceText.getText().toString(),
						_caseSensitive.isChecked(),
						_matchWholeWord.isChecked());
			}
		});

		// EditText views pad the entire row
		setColumnShrinkable(1, true);
		setColumnStretchable(1, true);
	}

	private void createOptionsDialog(Context context) {
		if (isInEditMode()) return;
		AlertDialog.Builder builder = new AlertDialog.Builder(context);

		View settingsLayout = View.inflate(context, R.layout.find_options, null);

		builder.setView(settingsLayout);
		builder.setTitle(context.getString(R.string.find_panel_options));
		builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				dialog.dismiss();
			}
		});

		_optionsDialog = builder.create();

		_caseSensitive = (CheckBox) settingsLayout.findViewById(R.id.find_panel_case_sensitive);
		_matchWholeWord = (CheckBox) settingsLayout.findViewById(R.id.find_panel_match_whole_word);
	}

	public void displaySettings() { _optionsDialog.show(); }

	public void setCallback(MainActivity c) { _callback = c; }

	@Override
	protected boolean onRequestFocusInDescendants(int direction,
			Rect previouslyFocusedRect) {
		_findText.requestFocus();
		return true;
	}

	@Override
	public Parcelable onSaveInstanceState() {
		Parcelable superState = super.onSaveInstanceState();
		FindPanelSavedState ss = new FindPanelSavedState(superState);
		ss.replaceBarVisibility = _replaceBar.getVisibility();
		ss.optionsCaseSensitive = _caseSensitive.isChecked();
		ss.optionsWholeWord = _matchWholeWord.isChecked();
		ss.optionsDialogShown = _optionsDialog.isShowing();
		return ss;
	}

	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if (!(state instanceof FindPanelSavedState)) {
			super.onRestoreInstanceState(state);
			return;
		}

		FindPanelSavedState ss = (FindPanelSavedState) state;
		super.onRestoreInstanceState(ss.getSuperState());

		//default visibility of _replaceBar is GONE
		if (ss.replaceBarVisibility == VISIBLE) {
			_replaceBar.setVisibility(VISIBLE);
			_toggleReplaceBar.setImageResource(R.drawable.arrow_up);
		}
		_caseSensitive.setChecked(ss.optionsCaseSensitive);
		_matchWholeWord.setChecked(ss.optionsWholeWord);
		if (ss.optionsDialogShown) {
			_optionsDialog.show();
		}
	}

	static class FindPanelSavedState extends View.BaseSavedState {
		public static final Parcelable.Creator<FindPanelSavedState> CREATOR =
				new Parcelable.Creator<FindPanelSavedState>() {
					public FindPanelSavedState createFromParcel(Parcel in) { return new FindPanelSavedState(in); }

					public FindPanelSavedState[] newArray(int size) { return new FindPanelSavedState[size]; }
				};
		int replaceBarVisibility;
		boolean optionsCaseSensitive;
		boolean optionsWholeWord;
		boolean optionsDialogShown;

		/**
		 * Constructor called from FindPanel.onSaveInstanceState()
		 */
		FindPanelSavedState(Parcelable superState) {
			super(superState);
		}

		/**
		 * Constructor called from CREATOR
		 */
		private FindPanelSavedState(Parcel in) {
			super(in);
			this.replaceBarVisibility = in.readInt();
			this.optionsCaseSensitive = in.readInt() != 0;
			this.optionsWholeWord = in.readInt() != 0;
			this.optionsDialogShown = in.readInt() != 0;
		}

		@Override
		public void writeToParcel(@NonNull Parcel out, int flags) {
			super.writeToParcel(out, flags);
			out.writeInt(this.replaceBarVisibility);
			out.writeInt(this.optionsCaseSensitive ? 1 : 0);
			out.writeInt(this.optionsWholeWord ? 1 : 0);
			out.writeInt(this.optionsDialogShown ? 1 : 0);
		}
	}

}
