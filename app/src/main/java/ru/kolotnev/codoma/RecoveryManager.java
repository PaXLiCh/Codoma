package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.List;

/**
 * When there are unsaved changes to a file and the app is forced to close by
 * the system, RecoveryManager can save a copy of the changed file.
 * When the app starts again, the user can recover the unsaved changes.
 * <p/>
 * The copy is called the recovery file. The original file before the edits
 * is called the head file. Recovery files are named backup0.txt, backup1.txt,...
 * The numbering scheme wraps around after MAX_BACKUP_FILES.
 * <p/>
 * If there is a IO error when restoring the recovery file, a copy of it is
 * saved to external storage. This copy is called the safekeeping file.
 * Only one safekeeping file is allowed. It will be overwritten if there is
 * another occurrence of an IO error when recovering a file. The user should
 * save the safekeeping file as soon as possible.
 */
class RecoveryManager {
	/**
	 * no recovery file needed because the head file was unchanged
	 */
	public static final int ERROR_NONE = 0;
	public static final int ERROR_RECOVERY_DISABLED = 1;
	public static final int ERROR_FILE_NOT_FOUND = 2;
	public static final int ERROR_READ = 3;
	public static final int ERROR_WRITE = 4;

	private static final int BACKUP_SUCCESS = 0;
	/**
	 * recovery file saved successfully
	 */
	private static final int BACKUP_NONE = 1;
	/**
	 * no attempt to create a recovery file
	 */
	private static final int BACKUP_FAILED = 2;
	/**
	 * attempted to create a recovery file but failed
	 */
	private static final int BACKUP_NO_CHANGES = 3;
	private static final String PREFS_NAME = "recovery";
	private static final String PREFS_KEY_AMOUNT = "files_recovered";
	private static final String PREFS_KEY_URI = "files_%s_uri";
	private static final String PREFS_KEY_ENCODING = "file_%s_encoding";
	private static final String PREFS_KEY_EOL = "file_%s_eol";
	private static final String PREFS_KEY_STORAGE = "file_%s_storage";
	private static final String FILE_RECOVERED = "recovered_%s.bat";
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
		Log.d("Codoma", "ZALUPA");
		/*BackupTextFileAsyncTask task = new BackupTextFileAsyncTask(app);
		TextFile[] files = new TextFile[textFiles.size()];
		textFiles.toArray(files);
		task.execute(files);*/

		int resultCode = BACKUP_SUCCESS;
		SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.clear();
		int filesNumber = 0;
		Log.d(CodomaApplication.TAG, "Backing up of the " + textFiles.size() + " files");
		for (TextFile textFile : textFiles) {
			//if (!textFile.isModified()) continue;
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
				++filesNumber;
				Log.d(CodomaApplication.TAG, "Backed up the file " + textFile.getTitle() + " with content " + textFile.getAllText());
			} catch (IOException e) {
				e.printStackTrace();
				Log.e(CodomaApplication.TAG, "Could not create backup in local or external storage. Unsaved changes are lost");
				resultCode = BACKUP_FAILED;
			}
		}
		editor.putInt(PREFS_KEY_AMOUNT, filesNumber);
		editor.apply();
		Log.d(CodomaApplication.TAG, "Backed up " + filesNumber + " files");
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
					Log.e(CodomaApplication.TAG, "Unrecognized recovery error code " + errorCode);
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
	 * <p/>
	 * A backup will only be made if there are unsaved changes. If the working
	 * file has not been edited yet, STATE_TYPE will be set to TYPE_NO_CHANGES.

	private static class BackupTextFileAsyncTask extends AsyncTask<TextFile, Integer, Integer> {

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
	private static class RecoverTextFilesAsyncTask extends AsyncTask<Void, Integer, TextFile[]> {
		private boolean splitIntoPages = false;
		private int recoveryErrorCode = ERROR_NONE; // the error code of the latest recovery action

		WeakReference<Application> appReference;

		RecoverTextFilesAsyncTask(Application app) {
			this.appReference = new WeakReference<>(app);
			splitIntoPages = PreferenceHelper.getSplitText(app);
		}

		@Override
		protected TextFile[] doInBackground(Void... voids) {
			Application app = appReference.get();
			SharedPreferences prefs = app.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
			int filesNumber = prefs.getInt(PREFS_KEY_AMOUNT, 0);
			TextFile[] textFiles = new TextFile[filesNumber];
			String encodingFallback = PreferenceHelper.getEncodingFallback(app);
			LineReader.LineEnding endingsFallback = PreferenceHelper.getLineEnding(app);
			Log.e(CodomaApplication.TAG, "files to recover " + filesNumber);
			for (int f = 0; f < filesNumber; ++f) {
				FileInputStream fs = null;
				try {
					String fileName = String.valueOf(f);
					Log.i(CodomaApplication.TAG, "Recovering file " + fileName);
					Storage storage = Storage.valueOf(prefs.getString(String.format(PREFS_KEY_STORAGE, fileName), ""));
					if (storage == Storage.INTERNAL) {
						fs = app.openFileInput(String.format(FILE_RECOVERED, fileName));
					} else if (storage == Storage.EXTERNAL) {
						// TODO: read file from external storage
						fs = null;
					}
					TextFile textFile = new TextFile();
					textFiles[f] = textFile;
					textFile.encoding = prefs.getString(String.format(PREFS_KEY_ENCODING, fileName), encodingFallback);
					String eolString = prefs.getString(String.format(PREFS_KEY_EOL, fileName), "");
					if (eolString.isEmpty()) {
						textFile.eol = endingsFallback;
					} else {
						textFile.eol = LineReader.LineEnding.valueOf(eolString);
					}
					String uriString = prefs.getString(String.format(PREFS_KEY_URI, fileName), "");
					Uri uri;
					if (uriString.isEmpty()) {
						uri = Uri.EMPTY;
					} else {
						uri = Uri.parse(uriString);
					}
					textFile.greatUri = new GreatUri(uri, AccessStorageApi.getPath(app, uri));

					StringBuilder stringBuilder = new StringBuilder();
					if (fs != null) {
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

						CodomaApplication.add(textFile);
					}

					recoveryErrorCode = ERROR_NONE;
				} catch (IOException ex) {
					recoveryErrorCode = ERROR_READ;
				} finally {
					if (fs != null) {
						try { fs.close(); } catch (IOException ex) { /* do nothing */ }
					}
				}
			}
			return textFiles;
		}
	}
}
