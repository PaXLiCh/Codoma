package ru.kolotnev.codoma;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.CountingOutputStream;
import org.apache.commons.io.output.NullOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.nio.charset.Charset;
import java.util.ArrayList;

/**
 * Async task for opening text files.
 */
class LoadTextFileTask extends AsyncTask<TextFile, Long, Void> {
	private static final String TAG = LoadTextFileTask.class.getName();
	private static final String TAG_DIALOG = "dialog_progress_load_text";

	private final WeakReference<AppCompatActivity> activity;
	private final ArrayList<TextFile> textFiles = new ArrayList<>();
	private final boolean splitIntoPages;
	@NonNull
	private final String encodingDefault;
	@NonNull
	private final String encodingFallback;
	private String message = "";
	private ProgressDialogFragment progressDialog;
	private LoadTextFileListener listener;

	LoadTextFileTask(@NonNull AppCompatActivity activity) {
		super();
		this.activity = new WeakReference<>(activity);
		splitIntoPages = PreferenceHelper.getSplitText(activity);
		encodingDefault = PreferenceHelper.getEncoding(activity);
		encodingFallback = PreferenceHelper.getEncodingFallback(activity);

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
		progressDialog = ProgressDialogFragment.newInstance(
				0,
				R.string.dialog_progress_load_text_message,
				0,
				R.plurals.dialog_progress_files_amount);
		progressDialog.show(activity.get().getSupportFragmentManager(), TAG_DIALOG);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(TextFile... params) {
		int totalFiles = params.length;
		//progressDialog.setTotal(params.length > 1);

		for (int i = 0; i < totalFiles; ++i) {
			publishProgress(0L, 0L, (long) i, (long) totalFiles);
			TextFile textFile = params[i];
			// TODO: add ability to open many files and display progress with 2 bars on dialog
			GreatUri greatUri = textFile.greatUri;
			if (greatUri == null || greatUri.getUri() == null || greatUri.getUri() == Uri.EMPTY) {
				// if no uri, nothing to load, skip this file
				Log.w(TAG, String.format("File %d '%s' cannot be loaded: no URI set.", i, textFile.getTitle()));
				continue;
			}

			String filePath = greatUri.getFilePath();
			try {
				// read the file associated with the uri
				if (TextUtils.isEmpty(filePath)) {
					// if the uri has no path
					readUri(textFile, greatUri, i, totalFiles);
				} else {
					// if the uri has a path
					readUri(textFile, greatUri, i, totalFiles);
					textFiles.add(textFile);
				}
			} catch (Exception e) {
				message = e.getMessage();
			}
		}

		// TODO: replace with real file sizes.
		return null;
	}

	private void readUri(@NonNull TextFile textFile, @NonNull GreatUri uri, long currentFileIndex, long totalFiles) throws IOException {
		LineReader lineReader = null;
		//FileReader reader = null;
		InputStreamReader streamReader = null;
		long fileSize = 0L;

		Context context = activity.get();

		// if we cannot read the file, root permission required
		boolean isRootRequired = !uri.isReadable();
		if (isRootRequired) {
			Log.e(TAG, "Want to read with rootfw");
			textFile.eol = PreferenceHelper.getLineEnding(context);
			if (textFile.encoding == null || textFile.encoding.isEmpty()) {
				textFile.encoding = encodingDefault;
				if (textFile.encoding.isEmpty()) {
					textFile.encoding = encodingFallback;
				}
			}

			// Connect the shared connection
			/*if (RootFW.connect()) {
				com.spazedog.lib.rootfw4.utils.File fileRooted = RootFW.getFile(uri.getFilePath());
				if (fileRooted == null) {
					Log.e(TAG, "Want to read with rootfw (got file) path:" + uri.getFilePath() + " but file == null");
					return;
				}
				Log.e(TAG, "Want to read with rootfw (got file) path:" + uri.getFilePath() + " exist " + fileRooted.exists());
				try {
					reader = new FileReader(uri.getFilePath());
				} catch (Exception e) {
					Log.e(TAG, "Want to read with rootfw (EXCEPTION " + e.getMessage() + ")");
				}
				Log.e(TAG, "Want to read with rootfw (got file reader)");
				lineReader = new LineReader(reader);
				Log.e(TAG, "Want to read with rootfw (got stream reader)");
				fileSize = fileRooted.size();
			}*/
		} else {
			// Read with normal reader
			if (textFile.encoding == null || textFile.encoding.isEmpty()) {
				textFile.encoding = FileUtils.detectEncoding(context.getContentResolver().openInputStream(uri.getUri()));
				if (textFile.encoding.isEmpty()) {
					textFile.encoding = encodingFallback;
				}
			}

			InputStream inputStream = context.getContentResolver().openInputStream(uri.getUri());
			if (inputStream != null) {
				streamReader = new InputStreamReader(inputStream, textFile.encoding);
				lineReader = new LineReader(streamReader);
				fileSize = inputStream.available();
			}
		}
		if (progressDialog != null) {
			progressDialog.calibrateFileSizeMeter(fileSize);
		}

		// Read content of file
		StringBuilder stringBuilder = new StringBuilder();
		long bytesRead = 0L;
		if (lineReader != null) {
			NullOutputStream nullOutputStream = new NullOutputStream();
			CountingOutputStream countingOutputStream = new CountingOutputStream(nullOutputStream);
			Charset charset = Charset.forName(textFile.encoding);

			String line;
			while ((line = lineReader.readLine()) != null) {
				IOUtils.write(line, countingOutputStream, charset);
				bytesRead += countingOutputStream.getByteCount() + lineReader.getLastEol().length();
				countingOutputStream.resetByteCount();
				publishProgress(bytesRead, fileSize, currentFileIndex, totalFiles);
				stringBuilder.append(line).append("\n");
			}
			countingOutputStream.flush();
			countingOutputStream.close();
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (streamReader != null)
				streamReader.close();
			/*if (reader != null)
				reader.close();*/
			textFile.setupPageSystem(stringBuilder.toString(), splitIntoPages);
			if (textFile.eol == null)
				textFile.eol = lineReader.getLineEndings();
		}

		// Current file now read
		publishProgress(fileSize, fileSize, currentFileIndex + 1, totalFiles);
		/*if (isRootRequired)
			RootFW.disconnect();*/
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
			listener.onFileLoaded(textFiles.toArray(new TextFile[textFiles.size()]));
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onProgressUpdate(Long... values) {
		super.onProgressUpdate(values);
		progressDialog.setProgressInBytes(values[0], values[1]);
		progressDialog.setProgressTotal(values[2].intValue(), values[3].intValue());
	}

	/**
	 * Callbacks to activity.
	 */
	interface LoadTextFileListener {
		void onFileLoaded(TextFile... textFile);

		void onFileLoadError(String message);
	}
}
