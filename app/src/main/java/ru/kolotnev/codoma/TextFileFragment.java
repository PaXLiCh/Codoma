package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link TextFileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link TextFileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TextFileFragment extends Fragment implements
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
	private NewEditorActivity activity;
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
			throw new ClassCastException(context.toString()
					+ " must implement OnFragmentInteractionListener");
		}
		this.activity = (NewEditorActivity) context;
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
			textFile.text = "public static void main(String args..) {\n\tint i = 0;\n}\n";
			Log.e(TAG, "TextFileFragment no text files");
			tempTextField = "created";
		}
		textFile.setupPageSystem(PreferenceHelper.getSplitText(getContext()), activity);
		Log.e(TAG, "loaded file with encoding " + textFile.encoding + " eol " + textFile.eol);
		//}
		Log.e(TAG, "content of temp text field: " + tempTextField);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		// Inflate the layout for this fragment
		View view = inflater.inflate(R.layout.fragment_text_file, container, false);
		//View viewEditor = view.findViewById(R.id.view_editor);
		editText = (ColoredEditText) view.findViewById(R.id.edit_text_colored);
		editText.setupEditor();
		editText.setTextFile(textFile);

		return view;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		textFile.text = textFile.getAllText(editText.getText().toString());
		//Log.e("Codoma", "destroying text file fragment which contains " + textFile.text);
	}

	@Override
	public void onDetach() {
		super.onDetach();
		mListener = null;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
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
						Log.e(TAG, "result of selecting new file to save = " + greatUri.toString());
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
						fileOptionsDialogFragment.show(getFragmentManager().beginTransaction(), FileOptionsDialogFragment.TAG);
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

	public void preferencesChanged() {
		editText.setupEditor();
	}

	public void savedAFile(GreatUri uri, boolean updateList) {
		if (uri != null) {
			// TODO: update list of recent and currents
			/*if (updateList) {
				refreshList(uri, true, false);
				arrayAdapter.selectPosition(uri);
			}*/
		}

		textFile.fileSaved();
		activity.updateTitle();
	}

	private void showKeyboard() {
		InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT);
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
		new SaveTextFileTask(activity,
				textFile.greatUri,
				textFile.getAllText(editText.getText().toString()),
				textFile.encoding,
				new SaveTextFileTask.SaveTextFileListener() {
					@Override
					public void fileSaved(Boolean success) {
						savedAFile(textFile.greatUri, false);
					}
				}).execute();
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
			subActivity.putExtra(SelectFileActivity.EXTRA_ACTION, SelectFileActivity.Actions.SelectFile);
			if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
				subActivity.putExtra(SelectFileActivity.EXTRA_PATH, textFile.greatUri.getFilePath());
				//AnimationUtils.startActivityWithScale(this, subActivity, true, requestCode, view);
			}
			startActivityForResult(subActivity, requestCode);
		}
	}

	public void goToLine() {
		int min = editText.getLineUtils().firstReadLine();
		int max = editText.getLineUtils().lastReadLine();
		NumberPickerDialog dialog = NumberPickerDialog.newInstance
				(NumberPickerDialog.Actions.GO_TO_LINE, min, min, max);
		dialog.setTargetFragment(this, 0);
		dialog.show(getFragmentManager().beginTransaction(), "dialog");
	}

	/**
	 * Close current file.
	 */
	public void close() {
		Log.e(TAG, "close called for " + (textFile.greatUri == null ? "new file" : textFile.greatUri.getUri()));
		if (textFile != null && textFile.isModified()) {
			SaveChangesDialogFragment dialog = SaveChangesDialogFragment.newInstance(textFile.getTitle());
			dialog.setTargetFragment(this, REQUEST_CODE_SAVE_CHANGES);
			dialog.show(getFragmentManager().beginTransaction(), "dialog");
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
		dialog.show(getFragmentManager().beginTransaction(), "dialog");
	}

	/**
	 * Set current page.
	 *
	 * @param page
	 * 		New current page.
	 */
	public void setCurrentPage(int page) {
		if (page == textFile.getCurrentPage()) return;
		textFile.savePage(editText.getText().toString());
		textFile.goToPage(page);
		editText.replaceTextKeepCursor(textFile.getCurrentPageText());
		editText.smoothScrollTo(0, 0);
	}

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
		Log.e(TAG, "result of selecting new file to save = " + greatUri.toString());
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
	public interface OnFragmentInteractionListener {
		void onTextChanged(TextFileFragment fragment);

		void onClose(TextFile textFile);
	}

}
