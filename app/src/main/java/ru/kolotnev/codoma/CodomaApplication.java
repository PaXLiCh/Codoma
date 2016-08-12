package ru.kolotnev.codoma;

import android.app.Application;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Main class for editor.
 */
public class CodomaApplication extends Application {
	public static final String TAG = "Codoma";

	public static final String SUPPORT_EMAIL = "paxlich@gmail.com";

	public static final String GOOGLE_PLAY_PUBLIC_KEY = "";

	public static final int MAX_FILE_SIZE = 20_000;

	public static final String GITHUB = "http://github.com/PaXLiCh/Codoma";

	public static final String XDA = "http://forum.xda-developers.com/android/apps-games/";

	public static final String TRANSLATE = "";

	public static final String PLAY_STORE = "market://search?q=pub:Kolotnev";

	private static final List<TextFile> textFiles = new ArrayList<>();

	public static int amountOfOpenedFiles() {
		return textFiles.size();
	}

	public static void remove(TextFile textFile) {
		textFiles.remove(textFile);
	}

	@Nullable
	public static TextFile get(int location) {
		if (location > -1 && location < textFiles.size()) {
			return textFiles.get(location);
		}
		return null;
	}

	public static int add(TextFile textFile) {
		textFiles.add(textFile);
		return textFiles.indexOf(textFile);
	}

	public static int indexOfOpenedFile(TextFile textFile) {
		return textFiles.indexOf(textFile);
	}

}
