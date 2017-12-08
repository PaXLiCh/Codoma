package ru.kolotnev.codoma;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.spazedog.lib.rootfw4.RootFW;
import com.spazedog.lib.rootfw4.utils.Filesystem;
import com.spazedog.lib.rootfw4.utils.io.FileWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;

/**
 * Asynchronous saving file.
 */
class SaveTextFileTask extends AsyncTask<TextFile, Integer, Void> {
	private final WeakReference<AppCompatActivity> activity;
	private SaveTextFileListener listener;
	private ProgressDialogFragment progressDialog;
	private boolean isSuccessful = false;
	private String fileTotalSize;
	private String errorMessage = "";

	SaveTextFileTask(@NonNull AppCompatActivity activity, SaveTextFileListener listener) {
		this.activity = new WeakReference<>(activity);
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialogFragment.newInstance(0, R.string.dialog_progress_save_text_message, 0, R.plurals.dialog_progress_files_amount);
		progressDialog.show(activity.get().getSupportFragmentManager(), "dialog_progress_save_text");
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final TextFile... textFiles) {
		for (TextFile textFile : textFiles) {
			String newContent = textFile.getAllText();

			try {
				String filePath = textFile.greatUri.getFilePath();
				// if the uri has no path
				if (TextUtils.isEmpty(filePath)) {
					writeUri(textFile.greatUri.getUri(), newContent, textFile.encoding);
				} else {
					if (textFile.greatUri.isWritable()) {
						writeUri(textFile.greatUri.getUri(), newContent, textFile.encoding);
					} else {
						// if we can read the file associated with the uri
						if (RootFW.connect()) {
							Filesystem.Disk systemPart = RootFW.getDisk(textFile.greatUri.getParentFolder());
							systemPart.mount(new String[] { "rw" });

							FileWriter file = RootFW.getFileWriter(textFile.greatUri.getFilePath(), false);
							file.write(newContent.getBytes(Charset.forName(textFile.encoding)));

							RootFW.disconnect();
						}
					}
				}

				isSuccessful = true;
			} catch (Exception e) {
				errorMessage = e.getMessage();
			}
		}
		return null;
	}

	private void writeUri(@NonNull Uri uri, @NonNull String newContent, @NonNull String encoding) throws IOException {
		ParcelFileDescriptor pfd = activity.get().getContentResolver().openFileDescriptor(uri, "w");
		FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
		byte[] bytes = newContent.getBytes(Charset.forName(encoding));
		fileOutputStream.write(bytes);
		publishProgress(bytes.length, bytes.length);
		fileTotalSize = org.apache.commons.io.FileUtils.byteCountToDisplaySize(bytes.length);
		fileOutputStream.close();
		pfd.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final Void aVoid) {
		super.onPostExecute(aVoid);
		progressDialog.dismiss();

		if (listener != null) {
			if (isSuccessful) {
				listener.onFileSaved();
			} else {
				listener.onFileSaveError(errorMessage);
			}
		}
	}


	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgressUpdate(Integer... values) {
		super.onProgressUpdate(values);
		progressDialog.setProgress(values[0], values[1],
				org.apache.commons.io.FileUtils.byteCountToDisplaySize(values[0]) + " / " + fileTotalSize);
	}

	interface SaveTextFileListener {
		void onFileSaved();

		void onFileSaveError(String message);
	}
}
