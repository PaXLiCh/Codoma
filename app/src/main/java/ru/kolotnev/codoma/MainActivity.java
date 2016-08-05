package ru.kolotnev.codoma;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity
		implements SharedPreferences.OnSharedPreferenceChangeListener {

//	private static final int SAVE_CALLBACK_NEW = 1;
//	private static final int SAVE_CALLBACK_OPEN = 2;
//	private static final int SAVE_CALLBACK_OPEN_RECENT = 3;
//	private static final int SAVE_CALLBACK_EXIT = 4;
	/** used when TextWarrior is already active and another task wants to open a file with it */
//	private static final int SAVE_CALLBACK_SINGLE_TASK_OPEN = 5;
//	private static final int CALLBACK_NONE = -1;
//	private final static String STATE_APP_UI = "appState";
//	private final static String STATE_TEXT_UI = "textUiState";
//	protected int _saveFinishedCallback = CALLBACK_NONE;
//	protected FreeScrollingTextField _editField;
//	private RecoveryManager _recoveryManager;
//	private FindThread _taskFind = null;

	/*
	 * 3 different scenarios are distinguished here.
	 * 1. Fresh start of an activity.
	 * 2. Activity re-created immediately after a configuration change
	 * 3. Activity re-created after the system force-killed its process,
	 * 	for example, to reclaim resources
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
//		_initBundle = savedInstanceState;
//		setContentView(R.layout.activity_main);
//
//		createTextField();
//		createFindPanel();
//		createClipboardPanel();
//		restorePersistentOptions();
//
//		RecentFilesProvider.loadFromPersistentStore(this);
//		_recoveryManager = new RecoveryManager(this);
//
//		NonConfigurationState ncs = (NonConfigurationState) getLastNonConfigurationInstance();
//		if (savedInstanceState == null) {
//			/* Scenario 1 */
//			Intent i = getIntent();
//			String action = i.getAction();
//			if (action.equals(Intent.ACTION_VIEW)
//					|| action.equals(Intent.ACTION_EDIT)) {
//				open(i.getData().getPath());
//			}
//		} else if (ncs != null) {
//			/* Scenario 2 */
//			restoreNonConfigurationState(ncs);
//			restoreUiState(savedInstanceState);
//		} else {
//			/* Scenario 3 */
//			//TODO check if newer file on system
//			boolean isRecovered = _recoveryManager.recover(_editField);
//			Log.d(LOG_TAG, isRecovered
//					? "Backup restored"
//					: "Recovery failed with error code " + _recoveryManager.getRecoveryErrorCode());
//
//			// Workaround to dismiss system-managed dialogs that were at the
//			// foreground when the process was force-killed
//			Handler h = new Handler();
//			h.post(new Runnable() {
//				@Override
//				public void run() {
//					//dismissAllDialogs();
//					//TODO if worker threads were interrupted, display info box
//				}
//			});
//
//			if (isRecovered) {
//				restoreUiState(savedInstanceState);
//			} else {
//				h.post(new Runnable() {
//					@Override
//					public void run() {
//						prepareRecoveryFailedMessage();
//						showRecoveryFailedDialog();
//					}
//				});
//			}
//		}
//
//		updateTitle();
//		updateClipboardButtons();
	}

	@Override
	protected void onNewIntent(Intent intent) {
		String action = intent.getAction();
		if (action.equals(Intent.ACTION_VIEW) || action.equals(Intent.ACTION_EDIT)) {
			setIntent(intent);

//			if (_editField.isEdited()) {
//				_saveFinishedCallback = SAVE_CALLBACK_SINGLE_TASK_OPEN;
//				onPromptSave();
//			} else {
//				open(intent.getData().getPath());
//			}
		}
	}

//	private void createFindPanel() {
//		_findPanel = (FindPanel) findViewById(R.id.find_panel);
//		_findPanel.setCallback(this);
//	}

//	private void restorePersistentOptions() {
//		PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
//		final SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);

//		setAutoIndent(pref);
//		setLongPressCaps(pref);
//		setWordWrap(pref);
//		setSyntaxColor(pref);
//		setColorScheme(pref);
//		setHighlightCurrentRow(pref);
//		setNavigationMethod(pref);
//		setTabSpaces(pref);
//		setFont(pref);
//		setZoom(pref);
//		setNonPrintingCharVisibility(pref);
//	}

//	private void showRecoveryFailedDialog() {
//		new recoveryFailedDialogFragment().show(getFragmentManager(), "recovery_failed");
//	}
//
//	private void analyzeTextProperties() {
//		DocumentProvider doc = _editField.createDocumentProvider();
//		int start, end;
//		if (_editField.isSelectText()) {
//			start = _editField.getSelectionStart();
//			end = _editField.getSelectionEnd();
//		} else {
//			start = 0;
//			end = doc.docLength();
//		}
//		_taskAnalyze = new AnalyzeStatisticsThread(doc, start, end);
//		_taskAnalyze.registerObserver(this);
//
//		PollingProgressDialog dialog =
//				new PollingProgressDialog(this, _taskAnalyze,
//						getString(R.string.progress_dialog_analyze), true, true);
//		dialog.startDelayedPollingDialog();
//		_taskAnalyze.start();
//	}
//
//	private void toggleFindPanel() {
//		if (_findPanel.getVisibility() == View.VISIBLE) {
//			_findPanel.setVisibility(View.GONE);
//		} else {
//			_findPanel.setVisibility(View.VISIBLE);
//			_findPanel.requestFocus();
//		}
//	}

//	private void prepareRecoveryFailedMessage() {
//		String messageSummary = getString(R.string.dialog_sorry_force_resume);
//		String messageDetails;
//
//		int errorCode = _recoveryManager.getRecoveryErrorCode();
//		switch (errorCode) {
//			case RecoveryManager.ERROR_RECOVERY_DISABLED:
//				messageDetails = getString(R.string.dialog_sorry_backup_disabled);
//				break;
//			case RecoveryManager.ERROR_FILE_NOT_FOUND:
//				messageDetails = getString(R.string.dialog_sorry_missing_recovery,
//						_recoveryManager.getExternalRecoveryDir());
//				break;
//			case RecoveryManager.ERROR_WRITE:
//				messageDetails = getString(R.string.dialog_sorry_write_error);
//				break;
//			case RecoveryManager.ERROR_READ:
//				messageDetails = getString(R.string.dialog_sorry_read_error,
//						_recoveryManager.getSafekeepingFileAbsolutePath());
//				break;
//			default:
//				TextWarriorException.fail("Unrecognized recovery error code " + errorCode);
//				messageDetails = "";
//				break;
//		}
//
//		_dialogErrorMsg = messageSummary + "\n\n" + messageDetails;
//	}

	/**
	 * Switch focus between the find panel and the main editing area
	 *
	 * @return If the focus was switched successfully between the
	 * find panel and main editing area
	 */
	private boolean togglePanelFocus() {
//		if (_findPanel.getVisibility() == View.VISIBLE) {
//			if (_editField.isFocused()) {
//				_findPanel.requestFocus();
//			} else {
//				_editField.requestFocus();
//			}
//			return true;
//		}
		return false;
	}

	public void find(String what, boolean isCaseSensitive, boolean isWholeWord) {
		if (what.length() > 0) {
//			int startingPosition = _editField.isSelectText()
//					? _editField.getSelectionStart() + 1
//					: _editField.getCaretPosition() + 1;
//
//			_taskFind = FindThread.createFindThread(
//					_editField.createDocumentProvider(),
//					what,
//					startingPosition,
//					true,
//					isCaseSensitive,
//					isWholeWord);
//			_taskFind.registerObserver(this);
//
//			PollingProgressDialog dialog = new PollingProgressDialog(this, _taskFind,
//					getString(R.string.progress_dialog_find), false, false);
//			dialog.startDelayedPollingDialog();
//			_taskFind.start();
		}
	}

	public void findBackwards(String what, boolean isCaseSensitive, boolean isWholeWord) {
		if (what.length() > 0) {
//			int startingPosition = _editField.isSelectText()
//					? _editField.getSelectionStart() - 1
//					: _editField.getCaretPosition() - 1;
//
//			_taskFind = FindThread.createFindThread(
//					_editField.createDocumentProvider(),
//					what,
//					startingPosition,
//					false,
//					isCaseSensitive,
//					isWholeWord);
//			_taskFind.registerObserver(this);
//
//			PollingProgressDialog dialog = new PollingProgressDialog(this, _taskFind,
//					getString(R.string.progress_dialog_find), false, false);
//			dialog.startDelayedPollingDialog();
//			_taskFind.start();
		}
	}

	public void replaceSelection(String replacementText) {
//		if (_editField.isSelectText()) {
//			_editField.paste(replacementText);
//		} else {
			Toast.makeText(MainActivity.this,
					R.string.dialog_replace_no_selection,
					Toast.LENGTH_SHORT).show();
//		}
	}

	public void replaceAll(String what, String replacementText,
			boolean isCaseSensitive, boolean isWholeWord) {
		if (what.length() > 0) {
//			int startingPosition = _editField.getCaretPosition();
//			_taskFind = FindThread.createReplaceAllThread(
//					_editField.createDocumentProvider(),
//					what,
//					replacementText,
//					startingPosition,
//					isCaseSensitive,
//					isWholeWord);
//			_taskFind.registerObserver(this);
//
//			PollingProgressDialog dialog = new PollingProgressDialog(this, _taskFind,
//					getString(R.string.progress_dialog_replace), true, false);
//			dialog.startDelayedPollingDialog();
//			_taskFind.start();
		}
	}

	/**
	 * Preconditions:
	 * 1. filename is not a directory
	 * 2. filename does not contain illegal symbols used by the file system
	 * (For example, in FAT systems, <>!* are not allowed in filenames)
	 * 3. if filename refers to a file that has not been created yet,
	 * the user has write access to the containing directory
	 */
//	public void save(String filename, boolean overwrite) {
//		_lastSelectedFile = filename;
//		File outputFile = new File(filename);
//
//		if (_recoveryManager.isInRecoveryPath(outputFile)) {
//			_dialogErrorMsg = getString(R.string.dialog_error_attempt_overwrite_recovery);
//			onSaveAgain();
//			return;
//		}
//
//		if (outputFile.exists()) {
//			if (!outputFile.canWrite()) {
//				_dialogErrorMsg = getString(R.string.dialog_error_attempt_overwrite_read_only);
//				onSaveAgain();
//				return;
//			}
//
//			if (!overwrite) {
//				onConfirmOverwrite();
//				return;
//			}
//		}
//
//		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//		String encoding = prefs.getString(
//				getString(R.string.settings_key_file_output_format),
//				getString(R.string.settings_file_output_format_default));
//		String eolChar = prefs.getString(
//				getString(R.string.settings_key_line_terminator_style),
//				getString(R.string.settings_line_terminator_style_default));
//
//		_taskWrite = new WriteThread(outputFile, _editField.createDocumentProvider(), encoding, eolChar);
//		_taskWrite.registerObserver(this);
//
//		PollingProgressDialog dialog = new PollingProgressDialog(this, _taskWrite,
//				getString(R.string.progress_dialog_save), true, true);
//		dialog.startDelayedPollingDialog();
//		_taskWrite.start();
//	}

//	@Override
//	//This method is called by various worker threads
//	public void onComplete(final int requestCode, final Object result) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				switch (requestCode) {
//					case ProgressSource.READ:
//						//TODO save view settings of previous file if it was clean
//						changeModel(_inputingDoc);
//						_filename = _lastSelectedFile;
//						RecentFilesProvider.addRecentFile(_lastSelectedFile);
//						//TODO restore view settings of new file
//						updateTitle();
//						_inputingDoc = null;
//						_taskRead = null;
//						break;
//					case ProgressSource.WRITE:
//						//TODO restore view settings of previous file
//
//						_filename = _lastSelectedFile;
//						updateTitle();
//						RecentFilesProvider.addRecentFile(_filename);
//						_editField.setEdited(false);
//						Toast.makeText(MainActivity.this,
//								R.string.dialog_file_save_success,
//								Toast.LENGTH_SHORT).show();
//						saveFinishedCallback();
//						_taskWrite = null;
//						break;
//					case ProgressSource.FIND:
//					case ProgressSource.FIND_BACKWARDS:
//						final int foundIndex = ((FindResults) result).foundOffset;
//						final int length = ((FindResults) result).searchTextLength;
//
//						if (foundIndex != -1) {
//							_editField.setSelectionRange(foundIndex, length);
//						} else {
//							Toast.makeText(MainActivity.this,
//									R.string.dialog_find_no_results,
//									Toast.LENGTH_SHORT).show();
//						}
//						_taskFind = null;
//						break;
//					case ProgressSource.REPLACE_ALL:
//						final int replacementCount = ((FindResults) result).replacementCount;
//						final int newCaretPosition = ((FindResults) result).newStartPosition;
//						if (replacementCount > 0) {
//							_editField.setEdited(true);
//							_editField.selectText(false);
//							_editField.moveCaret(newCaretPosition);
//							_editField.respan();
//							_editField.invalidate(); //TODO reduce invalidate calls
//						}
//						Toast.makeText(MainActivity.this,
//								getString(R.string.dialog_replace_all_result) + replacementCount,
//								Toast.LENGTH_SHORT).show();
//						_taskFind = null;
//						break;
//					case ProgressSource.ANALYZE_TEXT:
//						_statistics = (CharEncodingUtils.Statistics) result;
//						_taskAnalyze = null;
//						new statisticsDialogFragment().show(getFragmentManager(), "stats");
//						break;
//				}
//			}
//		});
//	}
//	@Override
//	public void onRowChange(int newRowIndex) {
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		boolean showLineNumbers = pref.getBoolean(
//				getString(R.string.settings_key_show_row_number),
//				getResources().getBoolean(R.bool.settings_show_row_number_default));
//		if (showLineNumbers) {
//			// change from 0-based to 1-based indexing
//			setTitle(createTitle(newRowIndex + 1));
//		}
//	}
//
//	@Override
//	public void onSelectionModeChanged(boolean active) {
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		if (active && _editField.isFocused() && !_clipboardPanel.isOpen()) {
//			boolean autoOpen = pref.getBoolean(
//					getString(R.string.settings_key_auto_open_clipboard),
//					getResources().getBoolean(R.bool.settings_auto_open_clipboard_default));
//			if (autoOpen) {
//				_clipboardPanel.setOpen(true, true);
//			}
//		} else if (!active && _editField.isFocused() && _clipboardPanel.isOpen()) {
//			boolean autoClose = pref.getBoolean(
//					getString(R.string.settings_key_auto_close_clipboard),
//					getResources().getBoolean(R.bool.settings_auto_close_clipboard_default));
//			if (autoClose) {
//				_clipboardPanel.setOpen(false, true);
//			}
//		}
//		updateClipboardButtons();
//	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences pref, String key) {
//		if (key.equals(getString(R.string.settings_key_zoom_size))) {
//			setZoom(pref);
//		} else if (key.equals(getString(R.string.settings_key_word_wrap))) {
//			setWordWrap(pref);
//		} else if (key.equals(getString(R.string.settings_key_font))) {
//			setFont(pref);
//		} else if (key.equals(getString(R.string.settings_key_navigation_method))) {
//			setNavigationMethod(pref);
//		} else if (key.equals(getString(R.string.settings_key_color_scheme))) {
//			setColorScheme(pref);
//		} else if (key.equals(getString(R.string.settings_view_syntax_highlight_key))) {
//			setSyntaxColor(pref);
//		} else if (key.equals(getString(R.string.settings_key_tab_spaces))) {
//			setTabSpaces(pref);
//		} else if (key.equals(getString(R.string.settings_key_show_row_number))) {
//			updateTitle();
//		} else if (key.equals(getString(R.string.settings_key_highlight_current_row))) {
//			setHighlightCurrentRow(pref);
//		} else if (key.equals(getString(R.string.settings_key_auto_indent))) {
//			setAutoIndent(pref);
//		} else if (key.equals(getString(R.string.settings_key_long_press_capitalize))) {
//			setLongPressCaps(pref);
//		} else if (key.equals(getString(R.string.settings_key_show_nonprinting))) {
//			setNonPrintingCharVisibility(pref);
//		} else if (key.equals(getString(R.string.settings_key_auto_backup))) {
//			onAutoBackupChanged(pref);
//		} else if (key.equals(getString(R.string.settings_key_chirality))) {
//			setChirality(pref);
//		}
	}


	//-----------------------------------------------------------------------
	//------------------- Android lifecycle methods -------------------------

//	private void setColorScheme(SharedPreferences pref) {
//		String colorSchemeKey = getString(R.string.settings_key_color_scheme);
//		String colorSchemeName = pref.getString(colorSchemeKey, getString(R.string.settings_color_scheme_default));
//		ColorScheme colorScheme;
//
//		if (colorSchemeName.equals(getString(R.string.settings_color_scheme_light))) {
//			colorScheme = new ColorSchemeLight();
//		} else if (colorSchemeName.equals(getString(R.string.settings_color_scheme_dark))) {
//			colorScheme = new ColorSchemeDark();
//		} else if (colorSchemeName.equals(getString(R.string.settings_color_scheme_solarized_light))) {
//			colorScheme = new ColorSchemeSolarizedLight();
//		} else if (colorSchemeName.equals(getString(R.string.settings_color_scheme_solarized_dark))) {
//			colorScheme = new ColorSchemeSolarizedDark();
//		} else if (colorSchemeName.equals(getString(R.string.settings_color_scheme_obsidian))) {
//			colorScheme = new ColorSchemeObsidian();
//		} else {
//			TextWarriorException.fail("Unsupported color scheme");
//			return;
//		}
//
//		_editField.setColorScheme(colorScheme);
//		_clipboardPanel.setColorScheme(colorScheme);
//	}
//
//	private void setSyntaxColor(SharedPreferences pref) {
//		String syntaxKey = getString(R.string.settings_view_syntax_highlight_key);
//		String lang = pref.getString(syntaxKey, getString(R.string.settings_syntax_color_default));
//
//		if (lang.equals(getString(R.string.settings_syntax_c))) {
//			Lexer.setLanguage(LanguageC.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_cpp))) {
//			Lexer.setLanguage(LanguageCpp.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_csharp))) {
//			Lexer.setLanguage(LanguageCsharp.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_java))) {
//			Lexer.setLanguage(LanguageJava.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_javascript))) {
//			Lexer.setLanguage(LanguageJavascript.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_objc))) {
//			Lexer.setLanguage(LanguageObjectiveC.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_php))) {
//			Lexer.setLanguage(LanguagePHP.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_python))) {
//			Lexer.setLanguage(LanguagePython.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_ruby))) {
//			Lexer.setLanguage(LanguageRuby.getInstance());
//			_editField.respan();
//		} else if (lang.equals(getString(R.string.settings_syntax_none))) {
//			Lexer.setLanguage(LanguageNonProg.getInstance());
//			_editField.cancelSpanning();
//			_editField.createDocumentProvider().clearSpans();
//		} else {
//			TextWarriorException.fail("Unsupported language for syntax highlighting");
//		}
//	}

//	private void setFont(SharedPreferences pref) {
//		Typeface font = PreferenceHelper.getFont(this);
//		if (font != null) {
//			_editField.setTypeface(font);
//		}
//	}

//	private void setZoom(SharedPreferences pref) {
//		String zoomKey = getString(R.string.settings_key_zoom_size);
//		String zoomStr = pref.getString(zoomKey,
//				getString(R.string.settings_zoom_size_default));
//		float zoom = Integer.parseInt(zoomStr) / 100.0f;
//		float size = PixelDipConverter.convertDpToPixel(zoom, this);
//		_editField.setZoom(size);
//	}

//	public boolean isWordWrap() {
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		String wordWrapKey = getString(R.string.settings_key_word_wrap);
//		return pref.getBoolean(wordWrapKey,
//				getResources().getBoolean(R.bool.settings_word_wrap_default));
//	}


//	// The only sure way of saving edited text to a temp (rescue) file is in
//	// onPause() or onSaveInstanceState(). However, this is problematic because
//	// saving of files can take an arbitrarily long time, and these methods are
//	// supposed to be lightweight. Therefore, the user gets to decide through
//	// an app setting if auto-backup should take place every time the app is paused.
//
//	private void setWordWrap(SharedPreferences pref) {
//		_editField.setWordWrap(isWordWrap());
//	}
//
//	private void setTabSpaces(SharedPreferences pref) {
//		String tabSpacesKey = getString(R.string.settings_key_tab_spaces);
//		String tabSpacesStr = pref.getString(tabSpacesKey,
//				getString(R.string.settings_tab_spaces_default));
//		_editField.setTabSpaces(Integer.parseInt(tabSpacesStr));
//	}
//
//	private void setHighlightCurrentRow(SharedPreferences pref) {
//		String highlightKey = getString(R.string.settings_key_highlight_current_row);
//		boolean isHighlight = pref.getBoolean(highlightKey,
//				getResources().getBoolean(R.bool.settings_highlight_current_row_default));
//		_editField.setHighlightCurrentRow(isHighlight);
//	}
//
//	private void setNonPrintingCharVisibility(SharedPreferences pref) {
//		String showKey = getString(R.string.settings_key_show_nonprinting);
//		boolean isShow = pref.getBoolean(showKey,
//				getResources().getBoolean(R.bool.settings_show_nonprinting_default));
//		_editField.setNonPrintingCharVisibility(isShow);
//	}
//
//	private void setAutoIndent(SharedPreferences pref) {
//		String autoIndentKey = getString(R.string.settings_key_auto_indent);
//		boolean isAutoIndent = pref.getBoolean(autoIndentKey,
//				getResources().getBoolean(R.bool.settings_auto_indent_default));
//		_editField.setAutoIndent(isAutoIndent);
//	}
//
//	private void setLongPressCaps(SharedPreferences pref) {
//		String longPressCapsKey = getString(R.string.settings_key_long_press_capitalize);
//		boolean isLongPressCaps = pref.getBoolean(longPressCapsKey,
//				getResources().getBoolean(R.bool.settings_long_press_capitalize_default));
//		_editField.setLongPressCaps(isLongPressCaps);
//	}
//
//	private void onAutoBackupChanged(SharedPreferences pref) {
//		_recoveryManager.clearRecoveryState();
//	}
//
//	private boolean isAutoBackup() {
//		SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//		return pref.getBoolean(getString(R.string.settings_key_auto_backup),
//				getResources().getBoolean(R.bool.settings_auto_backup_default));
//	}

	@Override
	protected void onPause() {
		super.onPause();
//		RecentFilesProvider.save(this);
//		_editField.onPause();
	}

//	private void restoreNonConfigurationState(NonConfigurationState ncState) {
//		if (ncState != null) {
//			//attach newly configured FreeScrollingTextField
//			ncState.mDocProvider.setMetrics(_editField);
//			if (ncState.mTempDoc != null) {
//				ncState.mTempDoc.setMetrics(_editField);
//			}
//
//			_editField.setDocumentProvider(ncState.mDocProvider);
//			_editField.setEdited(ncState.mDirty);
//			_filename = ncState.mFilename;
//			_lastSelectedFile = ncState.mPrevFilename;
//			_dialogErrorMsg = ncState.mDialogErrMsg;
//			_saveFinishedCallback = ncState.mSaveCallback;
//			_inputingDoc = ncState.mTempDoc;
//			_statistics = ncState.mStatistics;
//
//			restoreDisplayedDialogs(ncState);
//		}
//	}

	@Override
	protected void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
//		outState.putParcelable(STATE_APP_UI, new AppUiState(this));
//
//		if (!_editField.hasLayout() && _initBundle != null) {
//			// _editField is still being initialized and is in an inconsistent state
//			// Use the previous UI state instead
//			// Log.d(LOG_TAG, "Text field is still being created. Saving previous UI state.");
//			outState.putParcelable(STATE_TEXT_UI, _initBundle.getParcelable(STATE_TEXT_UI));
//		} else {
//			outState.putParcelable(STATE_TEXT_UI, _editField.getUiState());
//		}
//
//		if (isAutoBackup()) {
//			_recoveryManager.backup(_editField, _filename);
//		}
	}

	@Override
	protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
//		_recoveryManager.clearRecoveryState();
	}

//	private void restoreUiState(Bundle uiState) {
//		_editField.restoreUiState(uiState.getParcelable(STATE_TEXT_UI));
//
//		AppUiState appUiState = uiState.getParcelable(STATE_APP_UI);
//		if (appUiState.clipboardOpen) {
//			_clipboardPanel.setOpen(true, false); //closed by default
//		}
//		_findPanel.setVisibility(appUiState.findPanelVisibility);
//	}
//
//	private void restoreDisplayedDialogs(NonConfigurationState ss) {
//		_taskRead = ss.mReadTask;
//		_taskWrite = ss.mWriteTask;
//		_taskFind = ss.mFindTask;
//		_taskAnalyze = ss.mAnalyzeTask;
//
//		// Observers previously attached to worker threads are invalid after
//		// the activity restarts.
//		// Reassign listeners and show progress dialogs if worker threads are active
//
//		//FIXME There is a small possibility that worker tasks might signal an
//		// error or cancel event in the interval between onSaveInstanceState() and
//		// onRestoreInstanceState()/onCreate(). Hence, the error/cancel message
//		// is lost. To solve this, worker tasks have to implement additional
//		// error/cancel states that can be queried here.
//		if (_taskRead != null) {
//			if (_taskRead.isDone()) {
//				onComplete(ProgressSource.READ, null);
//			} else {
//				_taskRead.registerObserver(this);
//				PollingProgressDialog dialog = new PollingProgressDialog(this, _taskRead,
//						getString(R.string.progress_dialog_open), true, true);
//				dialog.startPollingDialog();
//			}
//		} else if (_taskWrite != null) {
//			if (_taskRead.isDone()) {
//				onComplete(ProgressSource.WRITE, null);
//			} else {
//				_taskWrite.registerObserver(this);
//				PollingProgressDialog dialog = new PollingProgressDialog(this, _taskWrite,
//						getString(R.string.progress_dialog_save), true, true);
//				dialog.startPollingDialog();
//			}
//		} else if (_taskFind != null) {
//			if (_taskAnalyze.isDone()) {
//				onComplete(_taskFind.getRequestCode(), _taskFind.getResults());
//			} else {
//				_taskFind.registerObserver(this);
//				PollingProgressDialog dialog;
//
//				if (_taskFind.getRequestCode() == ProgressSource.REPLACE_ALL) {
//					dialog = new PollingProgressDialog(this, _taskFind, null, true, false);
//				} else {
//					dialog = new PollingProgressDialog(this, _taskFind,
//							getString(R.string.progress_dialog_find), false, false);
//				}
//				dialog.startPollingDialog();
//			}
//		} else if (_taskAnalyze != null) {
//			if (_taskAnalyze.isDone()) {
//				onComplete(ProgressSource.ANALYZE_TEXT, _taskAnalyze.getResults());
//			} else {
//				_taskAnalyze.registerObserver(this);
//				PollingProgressDialog dialog = new PollingProgressDialog(this, _taskAnalyze,
//						getString(R.string.progress_dialog_analyze), true, true);
//				dialog.startPollingDialog();
//			}
//		}
//	}

//	public static class promptSaveDialogFragment extends DialogFragment {
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final MainActivity context = (MainActivity) getActivity();
//			AlertDialog.Builder builder = new AlertDialog.Builder(context);
//			builder.setTitle(R.string.dialog_prompt_save);
//			builder.setPositiveButton(R.string.dialog_button_save,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.dismiss();
//							context.onSave();
//						}
//					});
//			builder.setNeutralButton(R.string.dialog_button_discard,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.dismiss();
//							context.saveFinishedCallback();
//						}
//					});
//			builder.setNegativeButton(android.R.string.cancel,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.cancel();
//						}
//					});
//			return builder.create();
//		}
//
//		@Override
//		public void onCancel(DialogInterface dialog) {
//			//cancel pending callback
//			((MainActivity) getActivity())._saveFinishedCallback = CALLBACK_NONE;
//		}
//	}

//	public static class openAgainDialogFragment extends DialogFragment {
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final MainActivity context = (MainActivity) getActivity();
//			AlertDialog.Builder builder = new AlertDialog.Builder(context);
//			builder.setTitle(context._dialogErrorMsg);
//			builder.setIcon(android.R.drawable.ic_dialog_alert);
//			builder.setNeutralButton(R.string.dialog_button_go_back,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.dismiss();
//							context.onOpen();
//						}
//					});
//			builder.setNegativeButton(android.R.string.cancel,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.cancel();
//						}
//					});
//			return builder.create();
//		}
//	}
//
//	public static class saveAgainDialogFragment extends DialogFragment {
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final MainActivity context = (MainActivity) getActivity();
//			AlertDialog.Builder builder = new AlertDialog.Builder(context);
//			builder.setTitle(context._dialogErrorMsg);
//			builder.setIcon(android.R.drawable.ic_dialog_alert);
//			builder.setNeutralButton(R.string.dialog_button_save_different_name,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.dismiss();
//							context.onSaveAs();
//						}
//					});
//			builder.setNegativeButton(android.R.string.cancel,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.cancel();
//						}
//					});
//			return builder.create();
//		}
//
//		@Override
//		public void onCancel(DialogInterface dialog) {
//			//cancel pending callback
//			((MainActivity) getActivity())._saveFinishedCallback = CALLBACK_NONE;
//		}
//	}
//
//	public static class recoveryFailedDialogFragment extends DialogFragment {
//		@Override
//		public Dialog onCreateDialog(Bundle savedInstanceState) {
//			final MainActivity context = (MainActivity) getActivity();
//			AlertDialog.Builder builder = new AlertDialog.Builder(context);
//			builder.setTitle(R.string.dialog_sorry);
//			builder.setIcon(android.R.drawable.ic_dialog_alert);
//
//			builder.setMessage(context._dialogErrorMsg);
//
//			builder.setPositiveButton(android.R.string.ok,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							// this dialog is only needed once in the app lifecycle;
//							// removeDialog frees resources while dismiss() merely hides the dialog
//							dialog.dismiss();
//						}
//					});
//
//			builder.setNeutralButton(R.string.menu_help,
//					new DialogInterface.OnClickListener() {
//						@Override
//						public void onClick(DialogInterface dialog, int id) {
//							dialog.dismiss();
//							context.openHelp();
//						}
//					});
//
//			return builder.create();
//		}
//	}
}
