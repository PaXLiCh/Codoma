package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.res.Resources;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v4.provider.DocumentFile;
import android.support.v7.app.AlertDialog;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialog with info about file.
 */
public class FileInfoDialog extends DialogFragment {
	public static final String TAG = "FileInfoDialog";
	private static final String ARG_TEXT_FILE_INDEX = "index";
	private ListView list;

	public static FileInfoDialog newInstance(int textFileIndex) {
		final FileInfoDialog f = new FileInfoDialog();
		final Bundle args = new Bundle();
		args.putInt(ARG_TEXT_FILE_INDEX, textFileIndex);
		f.setArguments(args);
		return f;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final View view = View.inflate(getActivity(), R.layout.dialog_file_info, null);

		list = (ListView) view.findViewById(android.R.id.list);

		new CollectStatAsyncTask().execute();

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.setTitle(R.string.dialog_statistics)
				.setPositiveButton(android.R.string.ok, null)
				.create();
	}

	/**
	 * Fetch info about file in separate thread.
	 */
	private class CollectStatAsyncTask extends AsyncTask<Void, Void, Void> {
		private final List<Pair<String, String>> linesInfo = new ArrayList<>();

		/**
		 * Analyze word and character count from start to end position.
		 */
		@Override
		protected Void doInBackground(Void... params) {
			TextFile textFile = TextFileProvider.get(getArguments().getInt(ARG_TEXT_FILE_INDEX));
			if (textFile == null) return null;

			linesInfo.add(new Pair<>("Encoding", textFile.encoding));

			Resources res = getResources();
			String[] eolNames = res.getStringArray(R.array.settings_file_line_endings_entries);
			linesInfo.add(new Pair<>("Line endings", eolNames[textFile.eol.getIndex()]));

			// Read data about saved file
			if (textFile.greatUri != null && textFile.greatUri.getUri() != Uri.EMPTY) {
				Uri uri = textFile.greatUri.getUri();
				DocumentFile file = DocumentFile.fromFile(new File(AccessStorageApi.getPath(getActivity(), uri)));

				/*if (file == null && Device.hasKitKatApi()) {
					file = DocumentFile.fromSingleUri(getActivity(), (Uri) getArguments().getParcelable("uri"));
				}*/

				linesInfo.add(new Pair<>("File path", file.getUri().getPath()));
				linesInfo.add(new Pair<>("Size", org.apache.commons.io.FileUtils.byteCountToDisplaySize(file.length())));

				// Get the last modification information.
				Long lastModified = file.lastModified();

				// Create a new date object and pass last modified information
				// to the date object.
				Date date = new Date(lastModified);
				linesInfo.add(new Pair<>("Modification date", date.toString()));

				int wordCount = 0;
				int _unitsDone = 0;
				int whiteSpaceCount = 0;
				int lines = 1;
				boolean whiteSpaceRun = false;

				InputStream is;
				try {
					is = new BufferedInputStream(FileInfoDialog.this.getActivity().getContentResolver().openInputStream(uri));
					byte[] c = new byte[1024];
					int readChars;
					while ((readChars = is.read(c)) != -1) {
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
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}

				if (!whiteSpaceRun) {
					// the final word didn't end with whitespace
					++wordCount;
				}

				linesInfo.add(new Pair<>("Words count", String.valueOf(wordCount)));
				linesInfo.add(new Pair<>("Lines", String.valueOf(lines)));
				linesInfo.add(new Pair<>("White space count", String.valueOf(whiteSpaceCount)));
				linesInfo.add(new Pair<>("Symbols", String.valueOf(_unitsDone)));
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void aVoid) {
			super.onPostExecute(aVoid);
			list.setAdapter(new AdapterTwoItem(getActivity(), linesInfo));
		}
	}
}
