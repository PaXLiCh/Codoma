package ru.kolotnev.codoma;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.widget.Toast;

import com.spazedog.lib.rootfw4.RootFW;
import com.spazedog.lib.rootfw4.utils.Filesystem;
import com.spazedog.lib.rootfw4.utils.io.FileWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/**
 * Asynchronous saving file.
 */
public class SaveTextFileTask extends AsyncTask<Void, Void, Void> {
	private final Activity activity;
	private final GreatUri uri;
	private final String newContent;
	private final String encoding;
	private String message;
	private String positiveMessage;
	private SaveTextFileListener listener;
	private boolean isSuccessful = false;

	public SaveTextFileTask(Activity activity, GreatUri uri, String newContent, String encoding, SaveTextFileListener listener) {
		this.activity = activity;
		this.uri = uri;
		this.newContent = newContent;
		this.encoding = encoding;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
		positiveMessage = activity.getString(R.string.file_saved_with_success, uri.getFileName());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final Void... voids) {
		boolean isRootNeeded;

		try {
			String filePath = uri.getFilePath();
			// if the uri has no path
			if (TextUtils.isEmpty(filePath)) {
				writeUri(uri.getUri(), newContent, encoding);
			} else {
				isRootNeeded = !uri.isWritable();
				if (!isRootNeeded) {
					writeUri(uri.getUri(), newContent, encoding);
				} else {
					// if we can read the file associated with the uri
					if (RootFW.connect()) {
						Filesystem.Disk systemPart = RootFW.getDisk(uri.getParentFolder());
						systemPart.mount(new String[] { "rw" });

						FileWriter file = RootFW.getFileWriter(uri.getFilePath(), false);
						file.write(newContent.getBytes(Charset.forName(encoding)));

						RootFW.disconnect();
					}
				}
			}

			message = positiveMessage;
			isSuccessful = true;
		} catch (Exception e) {
			e.printStackTrace();
			message = e.getMessage();
		}
		return null;
	}

	private void writeUri(Uri uri, String newContent, String encoding) throws IOException {
		ParcelFileDescriptor pfd = activity.getContentResolver().openFileDescriptor(uri, "w");
		FileOutputStream fileOutputStream = new FileOutputStream(pfd.getFileDescriptor());
		fileOutputStream.write(newContent.getBytes(Charset.forName(encoding)));
		fileOutputStream.close();
		pfd.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final Void aVoid) {
		super.onPostExecute(aVoid);
		Toast.makeText(activity, message, Toast.LENGTH_LONG).show();

		if (listener != null)
			listener.fileSaved(isSuccessful);
	}

	public interface SaveTextFileListener {
		void fileSaved(Boolean success);
	}
}
