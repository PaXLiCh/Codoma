package ru.kolotnev.codoma;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;

/**
 * Fragment with find & replace.
 */
public class FindReplaceFragment extends Fragment implements View.OnClickListener {
	private Callbacks callback;
	private EditText editTextFind;
	private EditText editTextReplace;
	private AlertDialog dialogOptions;

	private boolean isCase;
	private boolean isWholeWord;
	private boolean isRegex;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.find_replace_panel, container);
		v.findViewById(R.id.find_panel_find_next).setOnClickListener(this);
		v.findViewById(R.id.find_panel_replace).setOnClickListener(this);
		v.findViewById(R.id.find_panel_replace_all).setOnClickListener(this);
		v.findViewById(R.id.find_panel_settings).setOnClickListener(this);

		editTextFind = v.findViewById(R.id.find_panel_search_text);
		editTextFind.setOnKeyListener((v12, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    callback.find(editTextFind.getText().toString(),
                            isCase, isWholeWord, isRegex);
                }
                return true;
            }
            return false;
        });

		editTextReplace = v.findViewById(R.id.find_panel_replace_text);
		editTextReplace.setOnKeyListener((v1, keyCode, event) -> {
            if (keyCode == KeyEvent.KEYCODE_ENTER) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    callback.replace(editTextReplace.getText().toString(),
                            editTextReplace.getText().toString(),
                            isCase, isWholeWord, isRegex);
                }
                return true;
            }
            return false;
        });

		return v;
	}

	@Override
	public void onClick(View v) {
		String strFind = editTextFind.getText().toString();
		String strReplace = editTextReplace.getText().toString();
		switch (v.getId()) {
			case R.id.find_panel_find_next:
				if (!strFind.isEmpty() && callback != null) {
					callback.find(strFind, isCase, isWholeWord, isRegex);
				}
				break;
			case R.id.find_panel_replace:
				if (!strFind.isEmpty() && callback != null) {
					callback.replace(strFind, strReplace, isCase, isWholeWord, isRegex);
				}
				break;
			case R.id.find_panel_replace_all:
				if (!strFind.isEmpty() && callback != null) {
					callback.replaceAll(strFind, strReplace, isCase, isWholeWord, isRegex);
				}
				break;
			case R.id.find_panel_settings:
				displaySettings();
				break;
		}
	}

	public void displaySettings() {
		// Create dialog for search options
		if (dialogOptions != null && dialogOptions.isShowing())
			return;

		Context context = getActivity();

		View v = View.inflate(context, R.layout.dialog_search_options, null);
		final CheckBox checkCase = v.findViewById(R.id.find_panel_case_sensitive);
		final CheckBox checkWholeWord = v.findViewById(R.id.find_panel_match_whole_word);
		final CheckBox checkRegex = v.findViewById(R.id.find_panel_regex);

		checkCase.setChecked(isCase);
		checkWholeWord.setChecked(isWholeWord);
		checkRegex.setChecked(isRegex);

		dialogOptions = new AlertDialog.Builder(context)
				.setTitle(getString(R.string.find_panel_options))
				.setView(v)
				.setPositiveButton(android.R.string.ok, (dialogInterface, i) -> {
                    isCase = checkCase.isChecked();
                    isWholeWord = checkWholeWord.isChecked();
                    isRegex = checkRegex.isChecked();
                })
				.show();
	}

	public void setCallback(Callbacks c) { callback = c; }

	interface Callbacks {
		void find(@NonNull String text, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex);

		void replace(@NonNull String text, @NonNull String replace, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex);

		void replaceAll(@NonNull String text, @NonNull String replace, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex);
	}
}
