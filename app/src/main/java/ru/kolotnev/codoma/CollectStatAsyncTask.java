package ru.kolotnev.codoma;

import android.app.Activity;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import androidx.documentfile.provider.DocumentFile;
import android.util.Pair;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Fetch info about file in separate thread.
 */
class CollectStatAsyncTask extends AsyncTask<TextFile, Void, List<Pair<Integer, String>>> {
	private StatListener listener;

	private WeakReference<Activity> activityWeakReference;

	CollectStatAsyncTask(Activity activity, StatListener listener) {
		activityWeakReference = new WeakReference<>(activity);
		this.listener = listener;
	}

	/**
	 * Analyze word and character count from start to end position.
	 */
	@Override
	protected List<Pair<Integer, String>> doInBackground(TextFile... params) {
		final List<Pair<Integer, String>> linesInfo = new ArrayList<>();

		TextFile textFile = params[0];
		if (textFile == null) return null;
		Activity activity = activityWeakReference.get();

		linesInfo.add(new Pair<>(R.string.dialog_file_info_encoding, textFile.encoding));

		Resources res = activity.getResources();
		String[] eolNames = res.getStringArray(R.array.settings_file_line_endings_entries);
		String eolName;
		if (textFile.eol.getIndex() >= eolNames.length) {
			eolName = textFile.eol.name();
		} else {
			eolName = eolNames[textFile.eol.getIndex()];
		}
		linesInfo.add(new Pair<>(R.string.dialog_file_info_endings, eolName));

		// Read data about saved file
		if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
			Uri uri = textFile.greatUri.getUri();
			DocumentFile file = DocumentFile.fromFile(new File(AccessStorageApi.getPath(activity, uri)));

			/*if (file == null && Device.hasKitKatApi()) {
				file = DocumentFile.fromSingleUri(getActivity(), (Uri) getArguments().getParcelable("uri"));
			}*/

			linesInfo.add(new Pair<>(R.string.dialog_file_info_path, file.getUri().getPath()));
			linesInfo.add(new Pair<>(R.string.dialog_file_info_size, org.apache.commons.io.FileUtils.byteCountToDisplaySize(file.length())));

			// Get the last modification information.
			long lastModified = file.lastModified();

			// Create a new date object and pass last modified information to the date object
			Date date = new Date(lastModified);
			linesInfo.add(new Pair<>(R.string.dialog_file_info_date, date.toString()));

			int wordCount = 0;
			int _unitsDone = 0;
			int whiteSpaceCount = 0;
			int lines = 1;
			boolean whiteSpaceRun = false;

			try {
				InputStream inputStream = activity.getContentResolver().openInputStream(uri);
				if (inputStream != null) {
					BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
					byte[] c = new byte[1024];
					int readChars;
					while ((readChars = bufferedInputStream.read(c)) != -1) {
						for (int i = 0; i < readChars; ++i) {
							// Check line endings
							if (c[i] == '\n') {
								++lines;
							}

							// Check whitespaces
							if (c[i] == ' ' || c[i] == '\t' || c[i] == '\n') {
								++whiteSpaceCount;
								if (!whiteSpaceRun) {
									whiteSpaceRun = true;
									++wordCount;
								}
							} else {
								whiteSpaceRun = false;
							}
						}
						_unitsDone += readChars;
					}
					if (lines == 0)
						lines = 1;
					inputStream.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}

			if (!whiteSpaceRun) {
				// the final word didn't end with whitespace
				++wordCount;
			}

			linesInfo.add(new Pair<>(R.string.dialog_file_info_words, String.valueOf(wordCount)));
			linesInfo.add(new Pair<>(R.string.dialog_file_info_lines, String.valueOf(lines)));
			linesInfo.add(new Pair<>(R.string.dialog_file_info_whitespaces, String.valueOf(whiteSpaceCount)));
			linesInfo.add(new Pair<>(R.string.dialog_file_info_symbols, String.valueOf(_unitsDone)));
		}
		return linesInfo;
	}

	@Override
	protected void onPostExecute(List<Pair<Integer, String>> pairs) {
		super.onPostExecute(pairs);

		listener.onGetStats(pairs);
	}

	interface StatListener {
		void onGetStats(List<Pair<Integer, String>> stats);
	}
}
