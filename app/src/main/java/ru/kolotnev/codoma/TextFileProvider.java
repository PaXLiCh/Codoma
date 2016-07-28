package ru.kolotnev.codoma;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Text files provider.
 */
class TextFileProvider {
	private static final List<TextFile> textFiles = new ArrayList<>();

	public static int size() {
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

	public static int indexOf(TextFile textFile) {
		return textFiles.indexOf(textFile);
	}

}
