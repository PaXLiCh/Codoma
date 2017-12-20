package ru.kolotnev.codoma;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

/**
 * New editor.
 */
public class EditorActivity extends AppCompatActivity implements
		TextFileFragment.OnFragmentInteractionListener,
		LoadTextFileTask.LoadTextFileListener,
		TextFile.PageSystemListener,
		FindReplaceFragment.Callbacks,
		FileOptionsDialogFragment.Callbacks,
		RecentFilesDialogFragment.Callbacks {

	private static final int
			REQUEST_CODE_CREATE = 43,
			REQUEST_CODE_SELECT_FILE = 121,
			REQUEST_CODE_SELECT_FOLDER = 122,
			REQUEST_CODE_SELECT_FILE_AS = 143,
			REQUEST_CODE_PREFERENCES = 200;
	private ActionBar actionBar;
	private ViewPager viewPager;
	private FindReplaceFragment _findPanel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setTheme(this);

		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_main);

		RecentFilesProvider.loadFromPersistentStore(this);

		Toolbar toolbar = findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);
		actionBar = getSupportActionBar();

		CodomaApplication.pagerAdapter = new ScreenSlidePagerAdapter();
		viewPager = findViewById(R.id.view_pager);
		viewPager.setAdapter(CodomaApplication.pagerAdapter);

		if (CodomaApplication.amountOfOpenedFiles() == 0) {
			textFileFromText("welcome to Codoma!");
		}

		// Bind the tabs to the ViewPager
		TabLayout tabs = findViewById(android.R.id.tabs);
		tabs.setupWithViewPager(viewPager);

		_findPanel = (FindReplaceFragment) getSupportFragmentManager().findFragmentById(R.id.find_replace);
		_findPanel.setCallback(this);
		View findPanelView = _findPanel.getView();
		if (findPanelView != null) {
			findPanelView.setVisibility(View.GONE);
		}

		updateTitle();

		Log.e(CodomaApplication.TAG, "onCreated activity with " + CodomaApplication.amountOfOpenedFiles() + " files, current item " + viewPager.getCurrentItem());

		// Parse the intent
		parseIntent(getIntent());
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		RecentFilesProvider.save(this);
		Log.e(CodomaApplication.TAG, "EditorActivity.onPause");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		CodomaApplication.pagerAdapter = null;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu_main, menu);

		onPrepareOptionsMenu(menu);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		TextFile textFile = CodomaApplication.get(viewPager.getCurrentItem());
		boolean isTextFileOpened = textFile != null;

		menu.setGroupVisible(R.id.menu_group_file, isTextFileOpened);
		menu.setGroupVisible(R.id.menu_group_file_page, textFile != null && textFile.isSplitIntoPages);
		if (isTextFileOpened) {
			menu.findItem(R.id.action_save).setEnabled(textFile.isModified());
			menu.findItem(R.id.action_undo).setEnabled(textFile.getCanUndo());
			menu.findItem(R.id.action_redo).setEnabled(textFile.getCanRedo());
			menu.findItem(R.id.action_page_previous).setEnabled(textFile.canReadPrevPage());
			menu.findItem(R.id.action_page_next).setEnabled(textFile.canReadNextPage());
		}
		Log.d(CodomaApplication.TAG, "onPrepareOptionsMenu");
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.recent_files:
				new RecentFilesDialogFragment()
						.show(getSupportFragmentManager(), RecentFilesDialogFragment.TAG);
				return true;
			case R.id.new_file:
				createFile();
				return true;
			case R.id.action_open:
				openFile(false);
				return true;
			case R.id.action_open_as:
				openFile(true);
				return true;
			case R.id.action_save:
				saveFile();
				return true;
			case R.id.action_save_as:
				saveFileAs();
				return true;
			case R.id.action_close:
				closeFile();
				return true;
			case R.id.action_undo:
				undo();
				return true;
			case R.id.action_redo:
				redo();
				return true;
			case R.id.go_to_line:
				goToLine();
				return true;
			case R.id.statistics:
				TextFile textFile = CodomaApplication.get(viewPager.getCurrentItem());
				if (textFile != null) {
					FileInfoDialog.newInstance(textFile, this)
							.show(getSupportFragmentManager(), FileInfoDialog.TAG);
				}
				return true;
			case R.id.settings:
				Intent i = new Intent(this, CodomaPreferenceActivity.class);
				startActivityForResult(i, REQUEST_CODE_PREFERENCES);
				return true;

			case R.id.find_panel_toggle:
				toggleFindPanel();
				return true;

			// Pages
			case R.id.action_page_previous:
				goToPagePrevious();
				return true;
			case R.id.action_page_next:
				goToPageNext();
				return true;
			case R.id.action_page_go:
				goToPage();
				return true;

			case R.id.change_input_method:
				InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (im != null) {
					im.showInputMethodPicker();
				}
				return true;

			case R.id.about:
				new AboutDialogFragment()
						.show(getSupportFragmentManager(), AboutDialogFragment.TAG);
				return true;

			case R.id.help:
				new HelpDialogFragment()
						.show(getSupportFragmentManager(), HelpDialogFragment.TAG);
				return true;

			default:
				return super.onOptionsItemSelected(item);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
		super.onActivityResult(requestCode, resultCode, intent);
		if (requestCode == REQUEST_CODE_PREFERENCES && resultCode == RESULT_OK) {
			Utils.updateTheme(this);
			//for (int i = 0; i < pagerAdapter.getCount(); ++i) {
			//	pagerAdapter.getRegisteredFragment(i).preferencesChanged();
			//}
			return;
		}

		if (resultCode == RESULT_OK) {
			final Uri uri = intent.getData();
			if (uri == null) {
				return;
			}
			if (Device.hasKitKatApi() && PreferenceHelper.getUseStorageAccessFramework(this)) {
				if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
					final int takeFlags = intent.getFlags()
							& (Intent.FLAG_GRANT_READ_URI_PERMISSION
							| Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
					// Check for the freshest data
					//noinspection ResourceType
					getContentResolver().takePersistableUriPermission(uri, takeFlags);
				}
			}
			switch (requestCode) {
				case REQUEST_CODE_CREATE:
				case REQUEST_CODE_SELECT_FILE:
					textFileByUri(uri, null, null);
					break;
				case REQUEST_CODE_SELECT_FILE_AS:
					FileOptionsDialogFragment.newInstance(uri,
							PreferenceHelper.getEncoding(this),
							PreferenceHelper.getLineEnding(this))
							.show(getSupportFragmentManager(), "FileOptions");
					break;
				case REQUEST_CODE_SELECT_FOLDER:
					GreatUri greatUri = new GreatUri(uri, AccessStorageApi.getPath(this, uri));
					Log.v(CodomaApplication.TAG, "result of selecting folder = " + greatUri.toString());
					break;
			}
		}
	}

	@Override
	public boolean dispatchKeyEvent(@NonNull KeyEvent event) {
		boolean handled = false;

		// Intercept keystroke shortcuts
		if (KeysInterpreter.isSwitchPanel(event) &&
				event.getAction() == KeyEvent.ACTION_DOWN) {
			//the view gaining focus must be able to ignore the corresponding
			//key up event sent to it when the key is released
			//handled = togglePanelFocus();
		}

		if (!handled) {
			handled = super.dispatchKeyEvent(event);
		}
		return handled;
	}

	@Override
	public boolean onKeyDown(int keyCode, @NonNull KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH) {
			// handle accidental touching of virtual hard keys as described in
			// http://android-developers.blogspot.com/2009/12/back-and-other
			// -hard-keys-three-stories.html
			event.startTracking();
			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public boolean onKeyUp(int keyCode, @NonNull KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_SEARCH &&
				!event.isCanceled() &&
				event.isTracking()) {
			toggleFindPanel();
			return true;
		}

		return super.onKeyUp(keyCode, event);
	}

	@Override
	public void onBackPressed() {
//		if (_editField.isEdited()) {
//			_saveFinishedCallback = SAVE_CALLBACK_EXIT;
//			onPromptSave();
//		} else {
//			finish();
//		}
	}

	@Override
	public void onTextChanged(TextFileFragment fragment) {

	}

	@Override
	public void onClose(TextFile textFile) {
		CodomaApplication.remove(textFile);
		//viewPager.setCurrentItem(0);
		updateTitle();
	}


	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		parseIntent(intent);
	}

	private void toggleFindPanel() {
		View v = _findPanel.getView();
		if (v == null) return;
		if (v.getVisibility() == View.VISIBLE) {
			v.setVisibility(View.GONE);
		} else {
			v.setVisibility(View.VISIBLE);
			v.requestFocus();
		}
	}

	/**
	 * Switch focus between the find panel and the main editing area
	 *
	 * @return If the focus was switched successfully between the
	 * find panel and main editing area
	 */
	private boolean togglePanelFocus() {
		View v = _findPanel.getView();
		if (v == null) return false;
		if (v.getVisibility() == View.VISIBLE) {
			//if (_editField.isFocused()) {
				v.requestFocus();
			//} else {
			//	_editField.requestFocus();
			//}
			return true;
		}
		return false;
	}

	/**
	 * Parses the intent
	 */
	private void parseIntent(Intent intent) {
		final String action = intent.getAction();
		final String type = intent.getType();

		Log.e(CodomaApplication.TAG, "parsing intent " + action + " type " + type);
		if (type != null
				&& Intent.ACTION_VIEW.equals(action)
				|| Intent.ACTION_EDIT.equals(action)
				|| Intent.ACTION_PICK.equals(action)) {
			// Post event
			//newFileToOpen(new File(intent.getData().getPath()), "");
			Uri uri = intent.getData();
			textFileByUri(uri, null, null);
		} else if (Intent.ACTION_SEND.equals(action) && type != null) {
			if ("text/plain".equals(type)) {
				textFileFromText(intent.getStringExtra(Intent.EXTRA_TEXT));
			}
		}

		/*if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_EDIT)) {
			setIntent(intent);

			if (_editField.isEdited()) {
				_saveFinishedCallback = SAVE_CALLBACK_SINGLE_TASK_OPEN;
				onPromptSave();
			} else {
				open(intent.getData().getPath());
			}
		}*/

	}

	//region Calls from the layout
	public void openFile(boolean openAs) {
		Intent intent;
		if (Device.hasKitKatApi() && PreferenceHelper.getUseStorageAccessFramework(this)) {
			// ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser.
			intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
			// Filter to only show results that can be "opened", such as a
			// file (as opposed to a list of contacts or timezones)
			intent.addCategory(Intent.CATEGORY_OPENABLE);
			intent.setType("*/*");
		} else {
			intent = new Intent(this, SelectFileActivity.class);
			intent.putExtra("action", SelectFileFragment.Actions.SelectFile);
		}
		startActivityForResult(intent,
				openAs ? REQUEST_CODE_SELECT_FILE_AS : REQUEST_CODE_SELECT_FILE);
	}

	private void createFile() {
		if (Device.hasKitKatApi() && PreferenceHelper.getUseStorageAccessFramework(this)) {
			Log.e(CodomaApplication.TAG, "open file with request code create");
			Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
			intent.setType("*/*");
			intent.putExtra(Intent.EXTRA_TITLE, ".txt");
			startActivityForResult(intent, REQUEST_CODE_CREATE);
		} else {
			textFileFromText("");
		}
	}

	private void saveFile() {
		getCurrentFile().save();
	}

	private void saveFileAs() {
		getCurrentFile().saveAs();
	}

	private void closeFile() {
		getCurrentFile().close();
	}

	private void undo() {
		getCurrentFile().undo();
	}

	private void redo() {
		getCurrentFile().redo();
	}

	private void goToLine() {
		getCurrentFile().goToLine();
	}

	private void goToPagePrevious() {
		getCurrentFile().gotoPrevPage();
	}

	private void goToPageNext() {
		getCurrentFile().gotoNextPage();
	}

	private void goToPage() {
		getCurrentFile().gotoPage();
	}

	@Override
	public void find(@NonNull String text, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		getCurrentFile().find(text, isCaseSensitive, isWholeWord, isRegex);
	}

	@Override
	public void replace(@NonNull String text, @NonNull String replace, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		getCurrentFile().replaceText(text, replace, isCaseSensitive, isWholeWord, isRegex);
	}

	@Override
	public void replaceAll(@NonNull String text, @NonNull String replace, boolean isCaseSensitive, boolean isWholeWord, boolean isRegex) {
		getCurrentFile().replaceAll(text, replace, isCaseSensitive, isWholeWord, isRegex);
	}

	private TextFileFragment getFile(int index) {
		return CodomaApplication.pagerAdapter.getRegisteredFragment(index);
	}

	private TextFileFragment getCurrentFile() {
		return CodomaApplication.pagerAdapter.getRegisteredFragment(viewPager.getCurrentItem());
	}

	private void textFileByUri(Uri uri, LineReader.LineEnding lineEnding, String encoding) {
		if (uri == Uri.EMPTY) {
			TextFile textFile = new TextFile();
			textFile.encoding = encoding;
			textFile.eol = lineEnding;
			onFileLoaded(textFile);
		} else {
			// If opening file, check already opened files
			if (encoding != null && lineEnding != null) {
				// TODO: add ability to reload file with new options
			} else {
				for (int i = 0; i < CodomaApplication.amountOfOpenedFiles(); ++i) {
					TextFile textFile = CodomaApplication.get(i);
					if (textFile != null
							&& textFile.greatUri != null
							&& textFile.greatUri.getUri().equals(uri)) {
						//viewPager.setCurrentItem(i);
						//return;
					}
				}
			}
			// File was never opened, open file
			TextFile textFile = new TextFile();
			textFile.encoding = encoding;
			textFile.eol = lineEnding;
			textFile.greatUri = new GreatUri(uri, AccessStorageApi.getPath(this, uri));
			Log.e(CodomaApplication.TAG, uri.toString());
			new LoadTextFileTask(this).execute(textFile);
		}
	}

	private void textFileFromText(@NonNull final String text) {
		TextFile textFile = new TextFile();
		textFile.encoding = PreferenceHelper.getEncoding(this);
		textFile.eol = PreferenceHelper.getLineEnding(this);
		textFile.setupPageSystem(text, PreferenceHelper.getSplitText(this));
		onFileLoaded(textFile);
	}

	@Override
	public void onFileLoaded(TextFile... textFiles) {
		for (TextFile textFile : textFiles) {
			//textFile.setupPageSystem(PreferenceHelper.getSplitText(this), this);
			//textFile.detectSyntax();

			int position = CodomaApplication.add(textFile);
			viewPager.setCurrentItem(position);

			updateTitle();

			if (textFile.greatUri != null && !textFile.greatUri.getFilePath().isEmpty()) {
				RecentFilesProvider.addRecentFile(textFile.greatUri.getUri().toString());
			}
		}
	}

	@Override
	public void onFileLoadError(String message) {
		Toast.makeText(this, message, Toast.LENGTH_LONG).show();
	}

	/**
	 * Update title of application and update menu.
	 */
	public void updateTitle() {
		TextFile textFile = CodomaApplication.get(viewPager.getCurrentItem());
		if (textFile == null)
			actionBar.setTitle(R.string.app_name);
		else
			actionBar.setTitle(textFile.getTitle());
		supportInvalidateOptionsMenu();
		Log.e(CodomaApplication.TAG, "update title");

		//tabs.setVisibility(TextFileProvider.size() > 1 ? View.VISIBLE : View.GONE);
	}

	@Override
	public void onPageChanged(int page) {
		// TODO: rework search and replace
		//pageSystemButtons.updateVisibility(false);
		//searchResult = null;
		TextFile textFile = CodomaApplication.get(viewPager.getCurrentItem());
		if (textFile != null) {
			textFile.clearHistory();
		}
		supportInvalidateOptionsMenu();
		closeKeyBoard();
	}


	/**
	 * Closes the soft keyboard.
	 */
	private void closeKeyBoard() {
		// Central system API to the overall input method framework (IMF) architecture
		InputMethodManager inputManager =
				(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		if (inputManager == null) return;

		// Base interface for a remote object
		View focusedView = getCurrentFocus();
		if (focusedView == null) return;
		IBinder windowToken = focusedView.getWindowToken();

		// Hide type
		int hideType = InputMethodManager.HIDE_NOT_ALWAYS;

		// Hide the KeyBoard
		inputManager.hideSoftInputFromWindow(windowToken, hideType);
	}

	@Override
	public void onSelectFileOptions(Uri uri, LineReader.LineEnding eol, String encoding) {
		textFileByUri(uri, eol, encoding);
	}

	@Override
	public void onRecentFileSelected(Uri uri) {
		textFileByUri(uri, null, null);
	}

	/**
	 * Adapter for pages with opened files.
	 */
	class ScreenSlidePagerAdapter extends SmartFragmentPagerAdapter<TextFileFragment> {
		ScreenSlidePagerAdapter() {
			super(getSupportFragmentManager(), TextFileFragment.class);
		}

		@Override
		public Fragment getItem(int position) {
			Log.d(CodomaApplication.TAG, "Creating page for position " + position);
			Fragment fragment = getRegisteredFragment(position);
			if (fragment == null)
				fragment = TextFileFragment.newInstance(position);
			return fragment;
			//return TextFileFragment.newInstance(position);
		}

		@Override
		public int getCount() {
			return CodomaApplication.amountOfOpenedFiles();
		}

		@Override
		public CharSequence getPageTitle(int position) {
			TextFile textFile = CodomaApplication.get(position);
			return textFile == null ? "(empty)" : textFile.getTitle();
		}

		@Override
		public int getItemPosition(Object object) {
			return POSITION_NONE;
		}
	}

}
