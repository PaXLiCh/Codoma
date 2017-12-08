package ru.kolotnev.codoma;

import android.app.Application;
import android.support.annotation.Nullable;
import android.util.Log;

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

	public RecoveryManager recoveryManager;

	@Override
	public void onCreate() {
		super.onCreate();
		recoveryManager = new RecoveryManager(this);
		recoveryManager.recoverTextFiles();

		final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread thread, Throwable exception) {
				// Save the fact we crashed out.
				//getSharedPreferences(TAG , Context.MODE_PRIVATE).edit()
				//		.putBoolean(KEY_APP_CRASHED, true).apply();
				Log.e(TAG, "onException");
				recoveryManager.backupTextFiles(textFiles);
				// Chain default exception handler.
				if (defaultHandler != null) {
					defaultHandler.uncaughtException(thread, exception);
				}
			}
		});
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Log.e(TAG, "onTrimMemory level (" + level + ")");
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e(TAG, "onLowMemory");
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		Log.e(TAG, "onTerminate");
	}

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

	public void saveFiles() {
		recoveryManager.backupTextFiles(textFiles);
	}
}
