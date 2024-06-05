package ru.kolotnev.codoma;

import android.app.Application;
import androidx.annotation.Nullable;
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
	public static EditorActivity.ScreenSlidePagerAdapter pagerAdapter;
	public RecoveryManager recoveryManager;

	public static int amountOfOpenedFiles() {
		return textFiles.size();
	}

	public static void remove(TextFile textFile) {
		textFiles.remove(textFile);
		if (pagerAdapter != null) {
			pagerAdapter.notifyDataSetChanged();
		}
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
		if (pagerAdapter != null) {
			pagerAdapter.notifyDataSetChanged();
		}
		return textFiles.indexOf(textFile);
	}

	public static int indexOfOpenedFile(TextFile textFile) {
		return textFiles.indexOf(textFile);
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate application");
		recoveryManager = new RecoveryManager(this);
		recoveryManager.recoverTextFiles();

		final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
		Thread.setDefaultUncaughtExceptionHandler((thread, exception) -> {
            // Save the fact we crashed out.
            //getSharedPreferences(TAG , Context.MODE_PRIVATE).edit()
            //		.putBoolean(KEY_APP_CRASHED, true).apply();
            Log.e(TAG, "onException");
            recoveryManager.backupTextFiles(textFiles);
            // Chain default exception handler.
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, exception);
            }
        });
	}

	@Override
	public void onTrimMemory(int level) {
		super.onTrimMemory(level);
		Log.e(TAG, "onTrimMemory level (" + level + ")");


		//TRIM_MEMORY_BACKGROUND
		//Constant Value: 40 (0x00000028)
		//added in API level 14
		//the process has gone on to the LRU list. This is a good opportunity to clean up resources that can efficiently and quickly be re-built if the user returns to the app.

		//TRIM_MEMORY_COMPLETE
		//Constant Value: 80 (0x00000050)
		//added in API level 14
		//the process is nearing the end of the background LRU list, and if more memory isn't found soon it will be killed.

		//TRIM_MEMORY_MODERATE
		//Constant Value: 60 (0x0000003c)
		//added in API level 14
		//the process is around the middle of the background LRU list; freeing memory can help the system keep other processes running later in the list for better overall performance.

		//TRIM_MEMORY_RUNNING_CRITICAL
		//Constant Value: 15 (0x0000000f)
		//added in API level 16
		//the process is not an expendable background process, but the device is running extremely low on memory and is about to not be able to keep any background processes running.
		// Your running process should free up as many non-critical resources as it can to allow that memory to be used elsewhere.
		// The next thing that will happen after this is onLowMemory() called to report that nothing at all can be kept in the background,
		// a situation that can start to notably impact the user.

		//TRIM_MEMORY_RUNNING_LOW
		//Constant Value: 10 (0x0000000a)
		//added in API level 16
		//the process is not an expendable background process, but the device is running low on memory.
		// Your running process should free up unneeded resources to allow that memory to be used elsewhere.

		//TRIM_MEMORY_RUNNING_MODERATE
		//Constant Value: 5 (0x00000005)
		//added in API level 16
		//the process is not an expendable background process, but the device is running moderately low on memory.
		// Your running process may want to release some unneeded resources for use elsewhere.

		//TRIM_MEMORY_UI_HIDDEN
		//Constant Value: 20 (0x00000014)
		//added in API level 14
		//the process had been showing a user interface, and is no longer doing so. Large allocations with the UI should be released at this point to allow memory to be better managed.

	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
		Log.e(TAG, "onLowMemory");
	}

	public void saveFiles() {
		recoveryManager.backupTextFiles(textFiles);
	}
}
