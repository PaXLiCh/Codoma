package ru.kolotnev.codoma;

import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Fragment with find & replace.
 */
public class FindReplaceFragment extends Fragment implements View.OnClickListener {
	private View viewPanelReplace;
	private View viewPanelOptions;
	private EditText editTextFind;
	private EditText editTextReplace;
	private CheckBox buttonReplaceToggle;
	private AlertDialog _optionsDialog;
	private CheckBox _matchWholeWord;
	private CheckBox matchCaseCheck;
	private CheckBox regexCheck;
	private CheckBox checkBoxReplace;

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		// Create dialog for search options
		if (_optionsDialog != null && _optionsDialog.isShowing())
			_optionsDialog.dismiss();
		createOptionsDialog(context);
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.find_replace_panel, container);
		viewPanelReplace = v.findViewById(R.id.find_panel_replace_bar);
		//viewPanelOptions = v.findViewById(R.id.);
		v.findViewById(R.id.find_panel_find_next).setOnClickListener(this);
		v.findViewById(R.id.find_panel_find_prev).setOnClickListener(this);
		v.findViewById(R.id.find_panel_replace).setOnClickListener(this);
		v.findViewById(R.id.find_panel_replace_all).setOnClickListener(this);
		v.findViewById(R.id.find_panel_settings).setOnClickListener(this);
		buttonReplaceToggle = (CheckBox) v.findViewById(R.id.find_panel_replace_bar_toggle);
		buttonReplaceToggle.setOnClickListener(this);
		editTextFind = (EditText) v.findViewById(R.id.find_panel_search_text);
		editTextReplace = (EditText) v.findViewById(R.id.find_panel_replace_text);
		return v;
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.find_panel_find_next:
				Editable e = editTextFind.getText();
				if (e.toString().length() > 0) {

				}
				break;
			case R.id.find_panel_find_prev:
				break;
			case R.id.find_panel_replace_bar_toggle:
				if (viewPanelReplace.getVisibility() == View.VISIBLE) {
					viewPanelReplace.setVisibility(View.GONE);
				} else {
					viewPanelReplace.setVisibility(View.VISIBLE);
				}

				break;
			case R.id.find_panel_replace:
				break;
			case R.id.find_panel_replace_all:
				break;
			case R.id.find_panel_settings:
				displaySettings();
				break;
		}
	}

	public void toggleVisibility() {
		if (getView() == null) return;
		if (getView().getVisibility() == View.VISIBLE) {
			getView().setVisibility(View.GONE);
		} else {
			getView().setVisibility(View.VISIBLE);
			getView().requestFocus();
		}
	}

	private void createOptionsDialog(Context context) {
		//if(isInEditMode()) return;
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

		matchCaseCheck = (CheckBox) settingsLayout.findViewById(R.id.find_panel_case_sensitive);
		_matchWholeWord = (CheckBox) settingsLayout.findViewById(R.id.find_panel_match_whole_word);
		regexCheck = (CheckBox) settingsLayout.findViewById(R.id.find_panel_regex);
	}

	public void displaySettings() { _optionsDialog.show(); }

	//public void setCallback(MainActivity c) { _callback = c; }

	public interface SearchDialogInterface {
		void onSearchDone(SearchResult searchResult);
	}

	private class SearchTask extends AsyncTask<Void, Void, Void> {
		String whatToSearch = editTextFind.getText().toString();
		boolean caseSensitive = matchCaseCheck.isChecked();
		boolean isRegex = regexCheck.isChecked();
		private LinkedList<Integer> foundIndex;
		private boolean foundSomething;

		@Override
		protected Void doInBackground(Void... params) {
			String allText = getArguments().getString("allText");
			foundIndex = new LinkedList<>();
			Matcher matcher = null;
			foundSomething = false;

			if (isRegex) {
				try {
					if (caseSensitive)
						matcher = Pattern.compile(whatToSearch, Pattern.MULTILINE).matcher(allText);
					else
						matcher = Pattern.compile(whatToSearch, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE).matcher(allText);
				} catch (Exception e) {
					isRegex = false;
				}
			}

			if (isRegex) {
				while (matcher.find()) {
					foundSomething = true;

					foundIndex.add(matcher.start());
				}
			} else {
				if (!caseSensitive) { // by default is case sensitive
					whatToSearch = whatToSearch.toLowerCase();
					allText = allText.toLowerCase();
				}
				int index = allText.indexOf(whatToSearch);
				while (index >= 0) {
					foundSomething = true;

					foundIndex.add(index);

					index = allText.indexOf(whatToSearch, index + 1);
				}
			}

			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			if (foundSomething) {
				// the class that called this Dialog should implement the SearchDialogInterface
				SearchDialogInterface searchDialogInterface;
				searchDialogInterface = ((SearchDialogInterface) getTargetFragment());
				if (searchDialogInterface == null)
					searchDialogInterface = ((SearchDialogInterface) getActivity());

				// if who called this has not implemented the interface we return nothing
				if (searchDialogInterface == null)
					return;
					// else we return positions and other things
				else {
					SearchResult searchResult = new SearchResult(foundIndex,
							whatToSearch.length(), checkBoxReplace.isChecked(), whatToSearch, editTextReplace.getText().toString());
					searchDialogInterface.onSearchDone(searchResult);
				}
			} else {

			}
			Toast.makeText(getActivity(), String.format(getString(R.string.occurrences_found), foundIndex.size()), Toast.LENGTH_SHORT).show();
			// dismiss the dialog
			// FindTextDialog.this.dismiss();
		}
	}
}
