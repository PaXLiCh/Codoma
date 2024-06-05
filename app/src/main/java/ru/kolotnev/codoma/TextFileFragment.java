package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import android.text.Layout;
import android.text.Selection;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.regex.Pattern;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextFileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextFileFragment extends Fragment implements
		SaveTextFileTask.SaveTextFileListener,
		SaveChangesDialogFragment.SaveChangesInFileDialogListener,
		NumberPickerDialog.INumberPickerDialog,
		FileOptionsDialogFragment.Callbacks {

	private static final String TAG = "TextFileFragment";

	private static final String ARG_INDEX = "uri";
	private static final int REQUEST_CODE_SAVE_NEW = 100;
	private static final int REQUEST_CODE_SAVE_AS = 101;
	private static final int REQUEST_CODE_SAVE_CHANGES = 102;
	private static final int
			ID_SELECT_ALL = android.R.id.selectAll,
			ID_CUT = android.R.id.cut,
			ID_COPY = android.R.id.copy,
			ID_PASTE = android.R.id.paste,
			ID_UNDO = R.id.action_undo,
			ID_REDO = R.id.action_redo;
	private TextFile textFile;
	private EditorActivity activity;
	private ColoredEditText editText;
	private OnFragmentInteractionListener mListener;

	public TextFileFragment() {
		// Required empty public constructor
	}

	/**
	 * Instantiate new editor with new file.
	 *
	 * @return Created fragment with editor for new file.
	 */
	@NonNull
	public static TextFileFragment newInstance() {
		return new TextFileFragment();
	}

	/**
	 * Instantiate new editor with specified file.
	 *
	 * @param location
	 * 		Number of opened / new editable file.
	 *
	 * @return Created fragment with editor for file.
	 */
	public static TextFileFragment newInstance(int location) {
		TextFileFragment fragment = new TextFileFragment();
		Bundle bundle = new Bundle();
		bundle.putInt(ARG_INDEX, location);
		fragment.setArguments(bundle);
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
		try {
			mListener = (OnFragmentInteractionListener) context;
		} catch (ClassCastException e) {
			throw new ClassCastException(context
					+ " must implement OnFragmentInteractionListener");
		}
		this.activity = (EditorActivity) context;
		Log.e(TAG, "TextFileFragment onAttach");
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setRetainInstance(true);
		//if (savedInstanceState == null) {
		Bundle bundle = getArguments();
		String tempTextField;
		if (bundle != null) {
			int location = bundle.getInt(ARG_INDEX);
			textFile = CodomaApplication.get(location);
			tempTextField = "loaded";
		} else {
			textFile = new TextFile();
			textFile.setupPageSystem("public static void main(String args..) {\n\tint i = 0;\n}\n", PreferenceHelper.getSplitText(activity));
			Log.e(TAG, "TextFileFragment no text files");
			tempTextField = "created";
		}
		textFile.setPageSystemListener(activity);
		Log.e(TAG, "loaded file with encoding " + textFile.encoding + " eol " + textFile.eol);
		//}
		Log.e(TAG, "content of temp text field: " + tempTextField);
	}

	@Override
	public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_text_file, container, false);
		//View viewEditor = view.findViewById(R.id.view_editor);
		editText = view.findViewById(R.id.edit_text_colored);
		editText.setupEditor();
		editText.setTextFile(textFile);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		textFile.setPageSystemListener(null);
		//Log.e("Codoma", "destroying text file fragment which contains " + textFile.text);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
			case REQUEST_CODE_SAVE_NEW:
				switch (resultCode) {
					case Activity.RESULT_OK:
						final Uri uri = data.getData();
						GreatUri greatUri = new GreatUri(uri, AccessStorageApi.getPath(activity, uri));
						Log.e(TAG, "result of selecting new file to save = " + greatUri);
						textFile.greatUri = greatUri;
						saveFile();
						break;
					case Activity.RESULT_CANCELED:
						break;
				}
				return;
			case REQUEST_CODE_SAVE_AS:
				switch (resultCode) {
					case Activity.RESULT_OK:
						final Uri uri = data.getData();
						FileOptionsDialogFragment fileOptionsDialogFragment = FileOptionsDialogFragment.newInstance(uri, textFile.encoding, textFile.eol);
						fileOptionsDialogFragment.setTargetFragment(this, REQUEST_CODE_SAVE_CHANGES);
						fileOptionsDialogFragment.show(getFragmentManager(), FileOptionsDialogFragment.TAG);
						break;
					case Activity.RESULT_CANCELED:
						break;
				}
				return;
			case REQUEST_CODE_SAVE_CHANGES:
				saveFile();
				break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}

	@Override
	public void onFileSaved() {
		// TODO: update list of recent and currents
		/*if (updateList) {
			refreshList(uri, true, false);
			arrayAdapter.selectPosition(uri);
		}*/

		Toast.makeText(activity, getString(R.string.file_saved_with_success, textFile.greatUri.getFileName()), Toast.LENGTH_LONG).show();

		textFile.fileSaved();
		activity.updateTitle();
	}

	@Override
	public void onFileSaveError(String message) {
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		if (imm != null) {
			imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
		}
	}

	/**
	 * Save current text to new or current file.
	 */
	public void save() {
		Log.e(TAG, "save called " + (textFile.greatUri == null ? "new file" : textFile.greatUri.getUri()));
		if (textFile.greatUri == null || textFile.greatUri.getUri() == Uri.EMPTY) {
			selectFileToSave(REQUEST_CODE_SAVE_NEW);
		} else {
			saveFile();
		}
	}

	/**
	 * Save file to specified place with special name and with special encoding and line endings.
	 */
	public void saveAs() {
		selectFileToSave(REQUEST_CODE_SAVE_AS);
	}

	public void undo() {
		if (editText == null) {
			return;
		}
		textFile.undo(editText.getEditableText());
	}

	public void redo() {
		if (editText == null) {
			return;
		}
		textFile.redo(editText.getEditableText());
	}

	/**
	 * Write file by current path.
	 */
	private void saveFile() {
		new SaveTextFileTask(activity, this).execute(textFile);
	}

	private void selectFileToSave(int requestCode) {
		if (Device.hasKitKatApi() && PreferenceHelper.getUseStorageAccessFramework(activity)) {
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.setType("*/*");
			// TODO: add file extension by selected highlight
			if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
				intent.putExtra(Intent.EXTRA_TITLE, textFile.greatUri.getFileName());
			}
			startActivityForResult(intent, requestCode);
		} else {
			Intent subActivity = new Intent(activity, SelectFileActivity.class);
			subActivity.putExtra(SelectFileFragment.EXTRA_ACTION, SelectFileFragment.Actions.SelectFile);
			if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
				subActivity.putExtra(SelectFileFragment.EXTRA_PATH, textFile.greatUri.getFilePath());
				//AnimationUtils.startActivityWithScale(this, subActivity, true, requestCode, view);
			}
			startActivityForResult(subActivity, requestCode);
		}
	}

	public void goToLine() {
		int min = editText.getLineUtils().firstReadLine();
		int max = editText.getLineUtils().lastReadLine();
		int current = getCurrentCursorLine();
		NumberPickerDialog dialog = NumberPickerDialog.newInstance
				(NumberPickerDialog.Actions.GO_TO_LINE, min, current, max);
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), "dialog");
	}

	public int getCurrentCursorLine()
	{
		int selectionStart = Selection.getSelectionStart(editText.getText()) + 1;
		Layout layout = editText.getLayout();

		if (!(selectionStart == -1)) {
			return layout.getLineForOffset(selectionStart);
		}

		return -1;
	}

	/**
	 * Close current file.
	 */
	public void close() {
		Log.e(TAG, "close called for " + (textFile.greatUri == null ? "new file" : textFile.greatUri.getUri()));
		if (textFile != null && textFile.isModified()) {
			SaveChangesDialogFragment dialog = SaveChangesDialogFragment.newInstance(textFile.getTitle());
			dialog.setTargetFragment(this, REQUEST_CODE_SAVE_CHANGES);
			dialog.show(getFragmentManager(), "dialog");
		} else {
			mListener.onClose(textFile);
		}
	}

	public void gotoNextPage() {
		setCurrentPage(textFile.getCurrentPage() + 1);
	}

	public void gotoPrevPage() {
		setCurrentPage(textFile.getCurrentPage() - 1);
	}

	public void gotoPage() {
		NumberPickerDialog dialog = NumberPickerDialog.newInstance
				(NumberPickerDialog.Actions.GO_TO_PAGE, 1, textFile.getCurrentPage() + 1, textFile.getMaxPage() + 1);
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager(), "dialog");
	}

	/**
	 * Set current page.
	 *
	 * @param page
	 * 		New current page.
	 */
	public void setCurrentPage(int page) {
		if (page == textFile.getCurrentPage()) return;
		textFile.goToPage(page);
		editText.replaceTextKeepCursor(textFile.getCurrentPageText());
		editText.smoothScrollTo(0, 0);
	}

	// region Search
	private SearchTask taskSearch;

	public void find(@NonNull String what,
			boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		boolean isSameResult;
		String whatToSearch = compileSearchText(what, isWholeWord, isRegex);
		if (editText.searchResult == null) {
			isSameResult = false;
		} else {
			if (isCaseSensitive || isRegex) {
				isSameResult = editText.searchResult.whatToSearch.equals(whatToSearch);
			} else {
				isSameResult = editText.searchResult.whatToSearch.equalsIgnoreCase(whatToSearch);
			}
		}
		if (isSameResult) {
			// Results was not reset, go to next result
			editText.searchResult.cycle();
			doSearch();
		} else {
			int selectionStart = editText.getSelectionStart();
			int selectionEnd = editText.getSelectionEnd();
			if (selectionStart == selectionEnd) {
				selectionEnd = editText.length();
			}
			SearchTask.OnSearchResultListener listener = result -> {
                editText.searchResult = result;
                String msg;
                if (result == null) {
                    msg = getContext().getString(R.string.search_occurrences_error);
                } else {
                    int amount = result.getAmount();
                    if (amount > 0) {
                        doSearch();
                        msg = getResources().getQuantityString(R.plurals.search_occurrences_found, amount, amount);
                    } else {
                        msg = getContext().getString(R.string.search_occurrences_found_zero);
                    }
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                editText.replaceTextKeepCursor(null);
            };
			if (taskSearch != null) {
				taskSearch.cancel(true);
			}
			taskSearch = new SearchTask(
					activity,
					whatToSearch,
					isCaseSensitive,
					selectionStart,
					selectionEnd,
					listener);
			taskSearch.execute(editText.getText().toString());
		}
	}

	private void doSearch() {
		if (editText.searchResult == null)
			return;
		SearchResult.SearchItem item = editText.searchResult.getCurrentItem();
		if (item != null) {
			editText.requestFocus();
			editText.setSelection(item.start, item.end);
		}
	}

	public void replaceText(@NonNull String what, @NonNull final String replacementText,
			boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		boolean isSameResult;
		String whatToSearch = compileSearchText(what, isWholeWord, isRegex);
		if (editText.searchResult == null) {
			isSameResult = false;
		} else {
			if (isCaseSensitive || isRegex) {
				isSameResult = editText.searchResult.whatToSearch.equals(whatToSearch);
			} else {
				isSameResult = editText.searchResult.whatToSearch.equalsIgnoreCase(whatToSearch);
			}
		}
		if (isSameResult) {
			// Results was not reset, go to next result
			//searchResult.cycle();
			editText.doReplace(replacementText);
		} else {
			int selectionStart = editText.getSelectionStart();
			int selectionEnd = editText.getSelectionEnd();
			if (selectionStart == selectionEnd) {
				selectionEnd = editText.length();
			}
			SearchTask.OnSearchResultListener listener = result -> {
                editText.searchResult = result;
                String msg;
                if (result == null) {
                    msg = getContext().getString(R.string.search_occurrences_error);
                } else {
                    int amount = result.getAmount();
                    if (amount > 0) {
                        editText.doReplace(replacementText);
                        msg = getResources().getQuantityString(R.plurals.search_occurrences_found, amount, amount);
                    } else {
                        msg = getContext().getString(R.string.search_occurrences_found_zero);
                    }
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                editText.replaceTextKeepCursor(null);
            };
			if (taskSearch != null) {
				taskSearch.cancel(true);
			}
			taskSearch = new SearchTask(
					activity,
					whatToSearch,
					isCaseSensitive,
					selectionStart,
					selectionEnd,
					listener);
			taskSearch.execute(editText.getText().toString());
		}

	}

	public void replaceAll(@NonNull String what, @NonNull final String replacementText,
			boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		boolean isSameResult;
		String whatToSearch = compileSearchText(what, isWholeWord, isRegex);
		if (editText.searchResult == null) {
			isSameResult = false;
		} else {
			if (isCaseSensitive || isRegex) {
				isSameResult = editText.searchResult.whatToSearch.equals(whatToSearch);
			} else {
				isSameResult = editText.searchResult.whatToSearch.equalsIgnoreCase(whatToSearch);
			}
		}
		if (isSameResult) {
			editText.doReplaceAll(replacementText);
		} else {
			int selectionStart = editText.getSelectionStart();
			int selectionEnd = editText.getSelectionEnd();
			if (selectionStart == selectionEnd) {
				selectionEnd = editText.length();
			}
			SearchTask.OnSearchResultListener listener = result -> {
                editText.searchResult = result;
                String msg;
                if (result == null) {
                    msg = getContext().getString(R.string.search_occurrences_error);
                } else {
                    int amount = result.getAmount();
                    if (amount > 0) {
                        editText.doReplaceAll(replacementText);
                        msg = getResources().getQuantityString(R.plurals.search_occurrences_found, amount, amount);
                    } else {
                        msg = getContext().getString(R.string.search_occurrences_found_zero);
                    }
                }
                Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
                editText.replaceTextKeepCursor(null);
            };
			if (taskSearch != null) {
				taskSearch.cancel(true);
			}
			taskSearch = new SearchTask(
					activity,
					whatToSearch,
					isCaseSensitive,
					selectionStart,
					selectionEnd,
					listener);
			taskSearch.execute(editText.getText().toString());
		}
	}


	/**
	 * Prepare regular expression pattern by parameters.
	 *
	 * @param whatToSearch
	 * 		Text to search.
	 * @param isWord
	 * 		Search the whole word.
	 * @param isRegex
	 * 		Already prepared regular expression.
	 *
	 * @return Prepared regular expression.
	 */
	@NonNull
	private static String compileSearchText(@NonNull String whatToSearch, boolean isWord, boolean isRegex) {
		String preparedString;
		if (!isRegex) {
			preparedString = Pattern.quote(whatToSearch);
			if (isWord) {
				preparedString = "\\b" + preparedString + "\\b";
			}
		} else {
			preparedString = whatToSearch;
		}
		return preparedString;
	}


	// endregion

	@Override
	public void userWantToSave() {
		save();
	}

	@Override
	public void userDoesNotWantToSave() {
		mListener.onClose(textFile);
	}

	@Override
	public void userDoesNotWantToDoAny() { /* do nothing */ }

	@Override
	public void onNumberPickerDialogDismissed(NumberPickerDialog.Actions action, int value) {
		if (action == NumberPickerDialog.Actions.GO_TO_PAGE) {
			setCurrentPage(value - 1);
		} else if (action == NumberPickerDialog.Actions.GO_TO_LINE) {
			editText.setCurrentLine(value - 1);
			showKeyboard();
		}
	}

	@Override
	public void onSelectFileOptions(Uri uri, LineReader.LineEnding eol, String encoding) {
		GreatUri greatUri = new GreatUri(uri, AccessStorageApi.getPath(activity, uri));
		Log.e(TAG, "result of selecting new file to save = " + greatUri);
		textFile.greatUri = greatUri;
		textFile.encoding = encoding;
		textFile.eol = eol;
		saveFile();
	}


	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	interface OnFragmentInteractionListener {
		void onTextChanged(TextFileFragment fragment);

		void onClose(TextFile textFile);
	}

}
