package ru.kolotnev.codoma;

import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.util.Pair;
import android.util.Log;

import com.ibm.icu.text.CharsetDetector;
import com.ibm.icu.text.CharsetMatch;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * File utils.
 */
public class FileUtils {
	/** TAG for log messages. */
	static final String TAG = "FileUtils";

	/**
	 * Detect encoding in specified stream.
	 *
	 * @param is
	 * 		Input stream for detecting encoding.
	 *
	 * @return Detected encoding in specified stream.
	 */
	public static String detectEncoding(InputStream is) {
		String charset = "";

		BufferedInputStream bis = new BufferedInputStream(is);
		CharsetDetector cd = new CharsetDetector();
		try {
			cd.setText(bis);
		} catch (IOException e) {
			e.printStackTrace();
			Log.e("CharsetDetector", "exception " + e.getMessage());
		}
		CharsetMatch cm = cd.detect();

		if (cm != null) {
			charset = cm.getName();
			Log.i("Codoma", "encoding " + charset + " confidence " + cm.getConfidence() + "%");
		} else {
			Log.e("CharsetDetector", "CharsetMatch == null");
		}
		try {
			bis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return charset;
	}

	/**
	 * Detect encoding in specified file.
	 *
	 * @param file
	 * 		File.
	 *
	 * @return Detected encoding in specified file.
	 * @throws FileNotFoundException
	 */
	public static String detectEncoding(File file) throws FileNotFoundException {
		return detectEncoding(new FileInputStream(file));
	}

	/**
	 * Generate list of supported charsets.
	 *
	 * @return Formatted list of all supported charsets.
	 */
	public static String[][] getFullSupportedEncodings() {
		SortedMap<String, Charset> m = Charset.availableCharsets();
		List<Pair<String, String>> list = new ArrayList<>();
		for (Map.Entry<String, Charset> entry : m.entrySet()) {
			String key = entry.getKey();
			Charset charset = entry.getValue();
			if (!charset.isRegistered() || !charset.canEncode()) continue;
			list.add(new Pair<>(key, charset.displayName()));
		}
		String[] encodings = new String[list.size()];
		String[] names = new String[list.size()];
		int i = 0;
		for (Pair<String, String> pair : list) {
			encodings[i] = pair.first;
			names[i] = pair.second;
			++i;
		}

		String[][] result = new String[][] { encodings, names };
		result[0] = encodings;
		result[1] = names;
		return result;
	}

	/**
	 * Whether the URI is a local one.
	 *
	 * @param uri
	 *
	 * @return
	 */
	public static boolean isLocal(String uri) {
		return uri != null && !uri.startsWith("http://");
	}

	/**
	 * Gets the extension of a file name, like ".png" or ".jpg".
	 *
	 * @param uri
	 *
	 * @return Extension including the dot("."); "" if there is no extension;
	 * null if uri was null.
	 */
	public static String getExtension(String uri) {
		if (uri == null) {
			return null;
		}

		int dot = uri.lastIndexOf(".");
		if (dot >= 0) {
			return uri.substring(dot);
		} else {
			// No extension.
			return "";
		}
	}

	/**
	 * Returns true if uri is a media uri.
	 *
	 * @param uri
	 *
	 * @return
	 */
	public static boolean isMediaUri(String uri) {
		return uri.startsWith(MediaStore.Audio.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(MediaStore.Video.Media.INTERNAL_CONTENT_URI.toString())
				|| uri.startsWith(MediaStore.Video.Media.EXTERNAL_CONTENT_URI.toString());
	}

	/**
	 * Convert File into Uri.
	 *
	 * @param file
	 *
	 * @return uri
	 */
	public static Uri getUri(File file) {
		if (file != null) {
			return Uri.fromFile(file);
		}
		return null;
	}

	/**
	 * Convert Uri into File.
	 *
	 * @param uri
	 *
	 * @return file
	 */
	public static File getFile(Uri uri) {
		if (uri != null) {
			String filepath = uri.getPath();
			if (filepath != null) {
				return new File(filepath);
			}
		}
		return null;
	}

	/**
	 * Constructs a file from a path and file name.
	 *
	 * @param curdir
	 * @param file
	 *
	 * @return
	 */
	public static File getFile(String curdir, String file) {
		String separator = "/";
		if (curdir.endsWith("/")) {
			separator = "";
		}
		return new File(curdir + separator + file);
	}

	public static File getFile(File curdir, String file) {
		return getFile(curdir.getAbsolutePath(), file);
	}
}
