package ru.kolotnev.codoma;

import android.app.Activity;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.ParcelFileDescriptor;
import android.text.TextUtils;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

/*import com.spazedog.lib.rootfw4.RootFW;
import com.spazedog.lib.rootfw4.Shell;
import com.spazedog.lib.rootfw4.utils.File;
import com.spazedog.lib.rootfw4.utils.Filesystem;*/

/**
 * Asynchronous saving file.
 */
public class SaveTextFileTask extends AsyncTask<Void, Void, Void> {
	private final Activity activity;
	private final GreatUri uri;
	private final String newContent;
	private final String encoding;
	private String message;
	private String positiveMessage, negativeMessage;
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
		negativeMessage = activity.getString(R.string.err_occurred);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected Void doInBackground(final Void... voids) {
		//boolean isRootNeeded = false;
		//Shell.Result resultRoot = null;

		try {
			String filePath = uri.getFilePath();
			// if the uri has no path
			if (TextUtils.isEmpty(filePath)) {
				writeUri(uri.getUri(), newContent, encoding);
			} else {
				//isRootNeeded = !uri.isWritable();
				//if (isRootNeeded == false) {
					writeUri(uri.getUri(), newContent, encoding);
				/*} else {
					// if we can read the file associated with the uri
					if (RootFW.connect()) {
						Filesystem.Disk systemPart = RootFW.getDisk(uri.getParentFolder());
						systemPart.mount(new String[]{"rw"});

						File file = RootFW.getFile(uri.getFilePath());
						resultRoot = file.writeResult(newContent);

						RootFW.disconnect();
					}
				}*/
			}

			/*if (isRootNeeded) {
				if (resultRoot != null && resultRoot.wasSuccessful()) {
					isSuccessful = true;
					message = positiveMessage;
				} else if (resultRoot != null) {
					isSuccessful = false;
					message = negativeMessage +
							" command number: " +
							resultRoot.getCommandNumber() +
							" result code: " +
							resultRoot.getResultCode() +
							" error lines: " +
							resultRoot.getString();
				} else
					message = negativeMessage;
					isSuccessful = false;
			} else*/
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

		/*android.content.ClipboardManager clipboard = (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
		android.content.ClipData clip = android.content.ClipData.newPlainText("Clip",message);
		clipboard.setPrimaryClip(clip);*/

		if (listener != null)
			listener.fileSaved(isSuccessful);
	}

	public interface SaveTextFileListener {
		void fileSaved(Boolean success);
	}
}
