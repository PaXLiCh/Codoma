package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;
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
public class RecoveryManager {
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
	@NonNull
	public final CodomaApplication app;

	public RecoveryManager(@NonNull CodomaApplication app) {
		this.app = app;
	}

	@NonNull
	private static String getNextFilename(@NonNull String fileName, @NonNull String fileExtension) {
		return "recovery/" + fileName + "_" + new Date(System.currentTimeMillis()).toString() + "." + fileExtension;
	}

	private void showRecoveryFailedDialog(@NonNull AppCompatActivity activity) {
		new RecoveryFailedDialogFragment().show(activity.getSupportFragmentManager(), "recovery_failed");
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

	public void GetListOfRecoveredFiles(
			@NonNull AppCompatActivity activity,
			@NonNull GetListOfRecoveredFilesListener listener) {
		GetListOfRecoveredFilesAsyncTask asyncTask = new GetListOfRecoveredFilesAsyncTask(activity, listener);
		asyncTask.execute();
	}

	public interface GetListOfRecoveredFilesListener {
		void onResult(File[] files);
	}

	/**
	 * List of files which can be recovered.
	 */
	private class GetListOfRecoveredFilesAsyncTask extends AsyncTask<Void, Void, File[]> {
		private AppCompatActivity activity;
		private GetListOfRecoveredFilesListener listener;
		private IndeterminateProgressDialogFragment dialog;

		public GetListOfRecoveredFilesAsyncTask(
				@NonNull AppCompatActivity activity,
				@NonNull GetListOfRecoveredFilesListener listener) {
			this.activity = activity;
			this.listener = listener;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = IndeterminateProgressDialogFragment.newInstance(R.string.app_name);
			dialog.show(activity.getSupportFragmentManager(), "dialog");
		}

		@Override
		protected File[] doInBackground(Void... voids) {
			File recoveryDir = new File(app.getFilesDir(), "recovery");
			return recoveryDir.listFiles();
		}

		@Override
		protected void onPostExecute(File[] files) {
			super.onPostExecute(files);
			listener.onResult(files);
			dialog.dismiss();
		}
	}

	public void backupTextFiles(@NonNull List<TextFile> textFiles) {
		BackupTextFileAsyncTask task = new BackupTextFileAsyncTask();
		task.execute(textFiles.toArray(new TextFile[textFiles.size()]));
	}

	/**
	 * Writes a backup of the working file.
	 * <p/>
	 * Attempts to backup to internal storage first. If unsuccessful, tries
	 * backing up to external storage. If still unsuccessful, the error will be
	 * recorded in PREFS_RECOVERY by setting STATE_TYPE to TYPE_FAILED.
	 * <p/>
	 * A backup will only be made if there are unsaved changes. If the working
	 * file has not been edited yet, STATE_TYPE will be set to TYPE_NO_CHANGES.
	 */
	public class BackupTextFileAsyncTask extends AsyncTask<TextFile, Integer, Integer> {
		@Nullable
		private AppCompatActivity activity;

		public BackupTextFileAsyncTask() {
			/**/
		}

		public BackupTextFileAsyncTask(@NonNull AppCompatActivity activity) {
			this.activity = activity;
		}

		@Override
		protected Integer doInBackground(TextFile... textFiles) {
			int resultCode = BACKUP_SUCCESS;
			for (TextFile textFile : textFiles) {
				if (!textFile.isModified()) continue;
				try {
					String fileName;
					String fileExtension;
					if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
						fileName = textFile.greatUri.getFileName();
						fileExtension = textFile.greatUri.getFileExtension();
					} else {
						fileName = "untitled";
						fileExtension = "";
					}
					byte[] bytes = textFile.getAllText().getBytes(Charset.forName(textFile.encoding));
					FileOutputStream outputStream = app.openFileOutput(getNextFilename(fileName, fileExtension), Context.MODE_PRIVATE);
					outputStream.write(bytes);
					publishProgress(bytes.length, bytes.length);
					outputStream.close();
					//backupToExternalStorage(textFile);
				} catch (IOException e) {
					e.printStackTrace();
					Log.e(CodomaApplication.TAG, "Could not create backup in local or external storage. Unsaved changes are lost");
					resultCode = BACKUP_FAILED;
				}
			}
			return resultCode;
		}
	}

	/**
	 * Attempts to read a previously backup file into editField.
	 * <p/>
	 * The backup file has to be created by a prior call to backup().
	 *
	 * @return Whether the recover was successful
	 */
	public class RecoverTextFileAsyncTask extends AsyncTask<File, Integer, TextFile[]> {
		private AppCompatActivity activity;
		private boolean splitIntoPages = false;
		private int recoveryErrorCode = ERROR_NONE; // the error code of the latest recovery action

		public RecoverTextFileAsyncTask(@NonNull AppCompatActivity activity) {
			this.activity = activity;
			splitIntoPages = PreferenceHelper.getSplitText(activity);
		}

		@Override
		protected TextFile[] doInBackground(File... files) {
			TextFile[] textFiles = new TextFile[files.length];
			for (int f = 0; f < files.length; ++f) {
				FileInputStream fs = null;
				try {
					fs = new FileInputStream(files[f]);
					TextFile textFile = textFiles[f];

					StringBuilder stringBuilder = new StringBuilder();

					if (textFile.encoding == null || textFile.encoding.isEmpty()) {
						fs.reset();
						textFile.encoding = FileUtils.detectEncoding(fs);
						if (textFile.encoding.isEmpty()) {
							textFile.encoding = PreferenceHelper.getEncodingFallback(app);
						}
					}

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
