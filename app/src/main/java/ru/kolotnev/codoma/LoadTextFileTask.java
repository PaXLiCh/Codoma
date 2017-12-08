package ru.kolotnev.codoma;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.spazedog.lib.rootfw4.RootFW;
import com.spazedog.lib.rootfw4.utils.io.FileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;

/**
 * Async task for opening text files.
 */
class LoadTextFileTask extends AsyncTask<TextFile, Integer, Void> {
	private final WeakReference<AppCompatActivity> activity;

	private String message = "";
	private boolean isRootRequired = false;
	private ProgressDialogFragment progressDialog;
	private TextFile[] textFiles;
	private LoadTextFileListener listener;
	private String fileTotalSize = "";
	private boolean splitIntoPages = false;

	LoadTextFileTask(@NonNull AppCompatActivity activity) {
		super();
		this.activity = new WeakReference<>(activity);
		splitIntoPages = PreferenceHelper.getSplitText(activity);

		try {
			this.listener = (LoadTextFileListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException("Context must implement LoadTextFileListener.");
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		progressDialog = ProgressDialogFragment.newInstance(0, R.string.dialog_progress_load_text_message, 0, R.plurals.dialog_progress_files_amount);
		progressDialog.show(activity.get().getSupportFragmentManager(), "dialog_progress_load_text");
	}

	private int totalFiles = 0;
	private int currentFile = 0;

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(TextFile... params) {
		textFiles = params;
		//progressDialog.setTotal(params.length > 1);
		totalFiles = textFiles.length;
		for (int i = 0; i < textFiles.length; ++i) {
			currentFile = i;
			publishProgress(0, 0);
			TextFile textFile = params[currentFile];
			// TODO: add ability to open many files and display progress with 2 bars on dialog
			GreatUri newUri = textFile.greatUri;

			try {
				// if no new uri
				if (newUri == null || newUri.getUri() == null || newUri.getUri() == Uri.EMPTY) {
					// file just empty
				} else {
					String filePath = newUri.getFilePath();

					if (TextUtils.isEmpty(filePath)) {
						// if the uri has no path
						readUri(textFile, newUri.getUri(), filePath, false);
					} else {
						// if the uri has a path

						// if we cannot read the file, root permission required
						isRootRequired = !newUri.isReadable();
						// read the file associated with the uri
						readUri(textFile, newUri.getUri(), filePath, isRootRequired);
					}

				}
			} catch (Exception e) {
				message = e.getMessage();
			}
		}

		// TODO: replace with real file sizes.
		return null;
	}

	private void readUri(@NonNull TextFile textFile, Uri uri, String path, boolean asRoot) throws IOException {
		LineReader lineReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		FileReader reader = null;
		InputStreamReader streamReader = null;
		int fileSize = 0;

		Context context = activity.get();

		if (textFile.encoding == null || textFile.encoding.isEmpty()) {
			textFile.encoding = FileUtils.detectEncoding(context.getContentResolver().openInputStream(uri));
			if (textFile.encoding.isEmpty()) {
				textFile.encoding = PreferenceHelper.getEncodingFallback(context);
			}
		}

		if (asRoot) {
			// Connect the shared connection
			if (RootFW.connect()) {
				reader = RootFW.getFileReader(path);
				lineReader = new LineReader(reader);
			}
		} else {
			InputStream inputStream = context.getContentResolver().openInputStream(uri);
			if (inputStream != null) {
				streamReader = new InputStreamReader(inputStream, textFile.encoding);
				lineReader = new LineReader(streamReader);
				fileSize = inputStream.available();
				fileTotalSize = org.apache.commons.io.FileUtils.byteCountToDisplaySize(fileSize);
			}
		}

		int readBytes = 0;
		if (lineReader != null) {
			String line;
			while ((line = lineReader.readLine()) != null) {
				readBytes += line.length() + 1;
				publishProgress(readBytes, fileSize);
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}
			if (streamReader != null)
				streamReader.close();
			if (reader != null)
				reader.close();
			textFile.setupPageSystem(stringBuilder.toString(), splitIntoPages);
			if (textFile.eol == null)
				textFile.eol = lineReader.getLineEndings();
		}

		if (isRootRequired)
			RootFW.disconnect();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(Void result) {
		super.onPostExecute(result);
		progressDialog.dismiss();

		if (!TextUtils.isEmpty(message)) {
			listener.onFileLoadError(message);
		} else {
			listener.onFileLoaded(textFiles);
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
		progressDialog.setProgressTotal(currentFile + 1, totalFiles);
	}

	/**
	 * Callbacks to activity.
	 */
	interface LoadTextFileListener {
		void onFileLoaded(TextFile... textFile);

		void onFileLoadError(String message);
	}
}
