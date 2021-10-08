package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * When there are unsaved changes to a file and the app is forced to close by
 * the system, RecoveryManager can save a copy of the changed file.
 * When the app starts again, the user can recover the unsaved changes.
 * <p/>
 * The copy is called the recovery file. The original file before the edits
 * is called the head file. Recovery files are named backup_0.bak, backup_1.bak,...
 * The numbering scheme wraps around after MAX_BACKUP_FILES.
 * <p/>
 * If there is a IO error when restoring the recovery file, a copy of it is
 * saved to external storage. This copy is called the safekeeping file.
 * Only one safekeeping file is allowed. It will be overwritten if there is
 * another occurrence of an IO error when recovering a file. The user should
 * save the safekeeping file as soon as possible.
 */
class RecoveryManager {
	public static final int ERROR_NONE = 0;
	public static final int ERROR_RECOVERY_DISABLED = 1;
	public static final int ERROR_FILE_NOT_FOUND = 2;
	public static final int ERROR_READ = 3;
	public static final int ERROR_WRITE = 4;
	private static final String TAG = "Recovery manager";
	/**
	 * recovery file saved successfully
	 */
	private static final int BACKUP_SUCCESS = 0;
	/**
	 * attempted to create a recovery file but failed
	 */
	private static final int BACKUP_FAILED = 1;

	private static final String PREFS_NAME = "recovery";
	private static final String PREFS_TIME = "time";
	private static final String PREFS_KEY_AMOUNT = "files_recovered";
	private static final String PREFS_KEY_URI = "files_%s_uri";
	private static final String PREFS_KEY_ENCODING = "file_%s_encoding";
	private static final String PREFS_KEY_EOL = "file_%s_eol";
	private static final String PREFS_KEY_STORAGE = "file_%s_storage";
	private static final String PREFS_KEY_MODIFIED = "file_%s_modified";
	private static final String FILE_RECOVERED = "backup_%s.bak";
	@NonNull
	public final CodomaApplication app;

	RecoveryManager(@NonNull CodomaApplication app) {
		this.app = app;
	}

	/**
	 * Save all opened files to special place and save params for reading of these files.
	 *
	 * @param textFiles
	 * 		List of files to save.
	 */
	void backupTextFiles(@NonNull List<TextFile> textFiles) {
		Log.v(TAG, "Backup " + textFiles.size() + " files");
		/*BackupTextFileAsyncTask task = new BackupTextFileAsyncTask(app);
		task.execute(textFiles);*/

		int resultCode = BACKUP_SUCCESS;
		SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		int filesNumber = 0;
		for (TextFile textFile : textFiles) {
			try {
				String fileName = String.valueOf(filesNumber);
				byte[] bytes = textFile.getAllText().getBytes(Charset.forName(textFile.encoding));
				FileOutputStream outputStream = app.openFileOutput(String.format(FILE_RECOVERED, fileName), Context.MODE_PRIVATE);
				outputStream.write(bytes);
				//publishProgress(bytes.length, bytes.length);
				outputStream.close();
				//backupToExternalStorage(textFile);

				String uri = "";
				if (textFile.greatUri != null) {
					uri = textFile.greatUri.getUri().toString();
				}
				editor.putString(String.format(PREFS_KEY_URI, fileName), uri);
				editor.putString(String.format(PREFS_KEY_ENCODING, fileName), textFile.encoding);
				editor.putString(String.format(PREFS_KEY_EOL, fileName), textFile.eol.name());
				editor.putString(String.format(PREFS_KEY_STORAGE, fileName), Storage.INTERNAL.name());
				editor.putBoolean(String.format(PREFS_KEY_MODIFIED, fileName), textFile.isModified());
				++filesNumber;
				Log.d(TAG, "Backed up the file " + textFile.getTitle() + " with content " + textFile.getAllText());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(TAG, "Could not create backup in local or external storage. Unsaved changes are lost");
				resultCode = BACKUP_FAILED;
			}
		}
		editor.putInt(PREFS_KEY_AMOUNT, filesNumber);
		editor.putLong(PREFS_TIME, System.currentTimeMillis());
		editor.apply();
		Log.d(TAG, "Backed up " + filesNumber + " files");
		//return resultCode;
	}

	/**
	 * Recover all files from storage and restore reading params.
	 */
	void recoverTextFiles() {
		RecoverTextFilesAsyncTask task = new RecoverTextFilesAsyncTask(app);
		task.execute();
	}

	/**
	 * Clear all information about backups.
	 */
	void clearBackups() {
		SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		int filesNumber = prefs.getInt(PREFS_KEY_AMOUNT, 0);
		for (int i = 0; i < filesNumber; ++i) {
			app.deleteFile(String.format(FILE_RECOVERED, String.valueOf(i)));
		}
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		editor.apply();
	}

	private enum Storage {
		INTERNAL,
		EXTERNAL
	}

	/**
	 * Dialog for error during recovering file.
	 */
	public static class RecoveryFailedDialogFragment extends DialogFragment {
		private String dialogErrorMsg;

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final Activity context = getActivity();
			String messageSummary = getString(R.string.dialog_sorry_force_resume);
			String messageDetails;

			int errorCode = 0;//getRecoveryErrorCode();
			switch (errorCode) {
				case ERROR_RECOVERY_DISABLED:
					messageDetails = getString(R.string.dialog_sorry_backup_disabled);
					break;
				case ERROR_FILE_NOT_FOUND:
					messageDetails = getString(R.string.dialog_sorry_missing_recovery);
					break;
				case ERROR_WRITE:
					messageDetails = getString(R.string.dialog_sorry_write_error);
					break;
				case ERROR_READ:
					messageDetails = getString(R.string.dialog_sorry_read_error);
					break;
				default:
					Log.e(RecoveryManager.TAG, "Unrecognized recovery error code " + errorCode);
					messageDetails = "";
					break;
			}

			dialogErrorMsg = messageSummary + "\n\n" + messageDetails;
			return new AlertDialog.Builder(context)
					.setTitle(R.string.dialog_sorry)
					.setMessage(dialogErrorMsg)
					.setPositiveButton(android.R.string.ok, null)
					.create();
		}
	}

	/*
	 * Writes a backup of the working file.
	 * <p/>
	 * Attempts to backup to internal storage first. If unsuccessful, tries
	 * backing up to external storage. If still unsuccessful, the error will be
	 * recorded in PREFS_RECOVERY by setting STATE_TYPE to TYPE_FAILED.
	 * */

	/*private static class BackupTextFileAsyncTask extends AsyncTask<TextFile, Integer, Integer> {

		WeakReference<Application> appReference;
		BackupTextFileAsyncTask(Application app) {
			appReference = new WeakReference<>(app);
		}

		@Override
		protected Integer doInBackground(TextFile... textFiles) {

		}
	} */

	/**
	 * Attempts to read a previously backup file into editField.
	 * <p/>
	 * The backup file has to be created by a prior call to backup().
	 */
	private static class RecoverTextFilesAsyncTask extends AsyncTask<Void, Integer, List<TextFile>> {
		private boolean splitIntoPages = false;
		private int recoveryErrorCode = ERROR_NONE; // the error code of the latest recovery action

		private WeakReference<Application> appReference;

		RecoverTextFilesAsyncTask(Application app) {
			this.appReference = new WeakReference<>(app);
			splitIntoPages = PreferenceHelper.getSplitText(app);
		}

		private void readFileProperties(
				@NonNull TextFile textFile,
				@NonNull String fileIndexString,
				@NonNull SharedPreferences prefs,
				@NonNull String encodingFallback,
				@NonNull LineReader.LineEnding endingsFallback) {
			textFile.encoding = prefs.getString(String.format(PREFS_KEY_ENCODING, fileIndexString), encodingFallback);
			String eolString = prefs.getString(String.format(PREFS_KEY_EOL, fileIndexString), "");
			if (eolString.isEmpty()) {
				textFile.eol = endingsFallback;
			} else {
				textFile.eol = LineReader.LineEnding.valueOf(eolString);
			}
			String uriString = prefs.getString(String.format(PREFS_KEY_URI, fileIndexString), "");
			Uri uri;
			if (uriString.isEmpty()) {
				uri = Uri.EMPTY;
			} else {
				uri = Uri.parse(uriString);
			}
			textFile.greatUri = new GreatUri(uri, AccessStorageApi.getPath(appReference.get(), uri));
			Log.v(TAG, "File " + fileIndexString + " with encoding " + textFile.encoding + " eol " + textFile.eol.name() + " URI " + uri.toString());
		}

		private void checkFileForModifications(
				@NonNull TextFile textFile,
				@NonNull String fileIndexString,
				@NonNull SharedPreferences prefs,
				long timeOfBackup) {
			Uri uri = textFile.greatUri.getUri();
			if (uri.equals(Uri.EMPTY)) {
				// Even the path to the file is not defined, obviously, the file must be saved
				textFile.setModified();
				Log.v(TAG, "File was never saved.");
			} else {
				boolean isModified = prefs.getBoolean(String.format(PREFS_KEY_MODIFIED, fileIndexString), true);
				if (isModified) {
					// File was not saved before backup
					textFile.setModified();
					Log.v(TAG, "File was marked for saving to " + uri.getPath());
				} else {
					// File was saved before backup, but may be deleted or modified later
					File fileOriginal = new File(uri.getPath());
					if (fileOriginal.exists()) {
						// If original file was modified after backup
						if (fileOriginal.lastModified() > timeOfBackup) {
							textFile.setModified();
							Log.v(TAG, "Original file " + uri.getPath() + " was modified.");
						} else {
							Log.v(TAG, "Original file " + uri.getPath() + " in actual state.");
						}
					} else {
						// Original file was moved or deleted
						textFile.setModified();
						Log.v(TAG, "Original file " + uri.getPath() + " was deleted.");
					}
				}
			}
		}

		private void readContentOfFile(
				@NonNull TextFile textFile,
				@NonNull FileInputStream fs) throws IOException {
			StringBuilder stringBuilder = new StringBuilder();
			InputStreamReader streamReader = new InputStreamReader(fs, textFile.encoding);
			LineReader lineReader = new LineReader(streamReader);
			int fileSize = fs.available();
			int readBytes = 0;
			String line;
			while ((line = lineReader.readLine()) != null) {
				readBytes += line.length() + 1;
				publishProgress(readBytes, fileSize);
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}
			streamReader.close();
			textFile.setupPageSystem(stringBuilder.toString(), splitIntoPages);
			if (textFile.eol == null)
				textFile.eol = lineReader.getLineEndings();
		}

		@Override
		protected List<TextFile> doInBackground(Void... voids) {
			Application app = appReference.get();
			SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			int filesNumber = prefs.getInt(PREFS_KEY_AMOUNT, 0);
			List<TextFile> textFiles = new ArrayList<>();
			if (filesNumber == 0) {
				Log.v(TAG, "No files for recovering.");
				return textFiles;
			}
			long timeOfBackup = prefs.getLong(PREFS_TIME, System.currentTimeMillis());
			String encodingFallback = PreferenceHelper.getEncodingFallback(app);
			LineReader.LineEnding endingsFallback = PreferenceHelper.getLineEnding(app);
			Log.v(TAG, "Recovering " + filesNumber + " files from backup.");
			for (int fileIndex = 0; fileIndex < filesNumber; ++fileIndex) {
				FileInputStream fs = null;
				try {
					String fileIndexString = String.valueOf(fileIndex);
					Log.v(TAG, "Recovering file " + fileIndexString);
					Storage storage = Storage.valueOf(prefs.getString(String.format(PREFS_KEY_STORAGE, fileIndexString), ""));
					String filePath = String.format(FILE_RECOVERED, fileIndexString);
					if (storage == Storage.INTERNAL) {
						fs = app.openFileInput(filePath);
					} else if (storage == Storage.EXTERNAL) {
						// TODO: read file from external storage
						fs = null;
					}

					if (fs == null) {
						Log.e(TAG, "Unable to read backup for file " + fileIndexString);
						continue;
					}

					TextFile textFile = new TextFile();

					// Read the properties of file
					readFileProperties(textFile, fileIndexString, prefs, encodingFallback, endingsFallback);

					// Is file must be saved after restoring
					checkFileForModifications(textFile, fileIndexString, prefs, timeOfBackup);

					// Read content of file
					readContentOfFile(textFile, fs);

					CodomaApplication.add(textFile);

					// Remove archived file
					File file = new File(app.getFilesDir(), filePath);
					boolean isDeleted = file.delete();
					if (isDeleted) {
						Log.v(TAG, "The archived file " + file.getCanonicalPath() + " is removed from the internal storage after recovery.");
					} else {
						Log.e(TAG, "Unable to remove file " + file.getCanonicalPath() + " from the internal storage");
					}

					textFiles.add(textFile);
					recoveryErrorCode = ERROR_NONE;
				} catch (IOException ex) {
					recoveryErrorCode = ERROR_READ;
				} finally {
					if (fs != null) {
						try { fs.close(); } catch (IOException ex) { /* do nothing */ }
					}
				}
			}
			prefs.edit().clear().apply();
			return textFiles;
		}
	}
}
