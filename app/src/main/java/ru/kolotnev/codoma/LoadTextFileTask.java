package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.ProgressDialog;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.spazedog.lib.rootfw4.RootFW;
import com.spazedog.lib.rootfw4.utils.io.FileReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Async task for opening text files.
 */
public class LoadTextFileTask extends AsyncTask<TextFile, Integer, Long> {
	private final Activity activity;

	private String message = "";
	private boolean isRootRequired = false;
	private ProgressDialog progressDialog;
	private TextFile[] textFiles;
	private LoadTextFileListener listener;

	public LoadTextFileTask(Activity activity) {
		super();
		this.activity = activity;

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
		// Close the drawer
		//mDrawerLayout.closeDrawer(Gravity.START);
		progressDialog = new ProgressDialog(activity);
		//progressDialog.setMessage(getString(R.string.please_wait));
		progressDialog.setMessage("Please wait");
		progressDialog.show();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Long doInBackground(TextFile... params) {
		textFiles = params;
		for (TextFile textFile : params) {
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
		return (long) 0;
	}

	private void readUri(@NonNull TextFile textFile, Uri uri, String path, boolean asRoot) throws IOException {
		LineReader lineReader = null;
		StringBuilder stringBuilder = new StringBuilder();
		String line;
		FileReader reader = null;
		InputStreamReader streamReader = null;

		if (asRoot) {
			// Connect the shared connection
			if (RootFW.connect()) {
				reader = RootFW.getFileReader(path);
				lineReader = new LineReader(reader);
			}
		} else {
			if (textFile.encoding == null || textFile.encoding.isEmpty()) {
				textFile.encoding = FileUtils.detectEncoding(activity.getContentResolver().openInputStream(uri));
				if (textFile.encoding.isEmpty()) {
					textFile.encoding = PreferenceHelper.getEncodingFallback(activity);
				}
			}

			InputStream inputStream = activity.getContentResolver().openInputStream(uri);
			if (inputStream != null) {
				streamReader = new InputStreamReader(inputStream, textFile.encoding);
				lineReader = new LineReader(streamReader);
			}
		}

		// TODO: make real loading progress
		//publishProgress((int) ((i / (float) count) * 100));
		publishProgress(10);

		if (lineReader != null) {
			while ((line = lineReader.readLine()) != null) {
				stringBuilder.append(line);
				stringBuilder.append("\n");
			}
			if (streamReader != null)
				streamReader.close();
			if (reader != null)
				reader.close();
			textFile.text = stringBuilder.toString();
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
	protected void onPostExecute(Long result) {
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
		progressDialog.setProgress(values[0]);
	}

	/**
	 * Callbacks to activity.
	 */
	public interface LoadTextFileListener {
		void onFileLoaded(TextFile... textFile);

		void onFileLoadError(String message);
	}
}
