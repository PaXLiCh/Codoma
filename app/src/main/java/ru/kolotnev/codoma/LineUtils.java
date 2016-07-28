package ru.kolotnev.codoma;

/*
 * Copyright (C) 2014 Vlad Mihalachi
 *
 * This file is part of Turbo Editor.
 *
 * Turbo Editor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Turbo Editor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

import android.support.annotation.NonNull;
import android.text.Layout;
import android.text.TextUtils;
import android.widget.ScrollView;

public class LineUtils {
	private boolean[] toCountLinesArray;
	private int[] realLines;

	public static int getYAtLine(ScrollView scrollView, int lineCount, int line) {
		return scrollView.getChildAt(0).getHeight() / lineCount * line;
	}

	public static int getFirstVisibleLine(ScrollView scrollView, int childHeight, int lineCount) throws ArithmeticException {
		int line = (scrollView.getScrollY() * lineCount) / childHeight;
		if (line < 0) line = 0;
		return line;
	}

	public static int getLastVisibleLine(ScrollView scrollView, int childHeight, int lineCount, int deviceHeight) {
		int line = ((scrollView.getScrollY() + deviceHeight) * lineCount) / childHeight;
		if (line > lineCount) line = lineCount;
		return line;
	}

	/**
	 * Gets the line from the index of the letter in the text
	 *
	 * @param index
	 * @param lineCount
	 * @param layout
	 *
	 * @return
	 */
	public static int getLineFromIndex(int index, int lineCount, Layout layout) {
		int line;
		int currentIndex = 0;

		for (line = 0; line < lineCount; ++line) {
			currentIndex += layout.getLineEnd(line) - layout.getLineStart(line);
			if (currentIndex > index) {
				break;
			}
		}

		return line;
	}

	/**
	 * Returns array with start line and end line for current selection.
	 *
	 * @param layout
	 * 		Layout with text lines.
	 * @param selectionStart
	 * 		Start position of current selection.
	 * @param selectionEnd
	 * 		End position of current selection (default -1).
	 *
	 * @return Integer array with two numbers {start, end}.
	 */
	public static int[] getCurrentCursorLines(@NonNull final Layout layout, int selectionStart, int selectionEnd) {
		int[] result = new int[] { 0, 0 };

		if (selectionStart != -1) {
			result[0] = layout.getLineForOffset(selectionStart) + 1;
			result[1] = result[0];
		}
		if (selectionEnd != -1) {
			result[1] = layout.getLineForOffset(selectionEnd) + 1;
		}

		return result;
	}

	public boolean[] getGoodLines() {
		return toCountLinesArray;
	}

	public int[] getRealLines() {
		return realLines;
	}

	public void updateHasNewLineArray(int startingLine, int lineCount, Layout layout, String text) {

		boolean[] hasNewLineArray = new boolean[lineCount];
		toCountLinesArray = new boolean[lineCount];
		realLines = new int[lineCount];

		if (TextUtils.isEmpty(text)) {
			toCountLinesArray[0] = true;
			realLines[0] = 1;
			return;
		}

		int i;

		if (layout == null) return;

		// for every line on the edittext
		for (i = 0; i < lineCount; ++i) {
			// check if this line contains "\n"
			//hasNewLineArray[i] = text.substring(layout.getLineStart(i), layout.getLineEnd(i)).endsWith("\n");
			hasNewLineArray[i] = text.charAt(layout.getLineEnd(i) - 1) == '\n';
			// if true
			if (hasNewLineArray[i]) {
				int j = i - 1;
				while (j > -1 && !hasNewLineArray[j]) {
					--j;
				}
				toCountLinesArray[j + 1] = true;

			}
		}

		if (toCountLinesArray.length > 0)
			toCountLinesArray[lineCount - 1] = true;

		int realLine = startingLine; // the first line is not 0, is 1. We start counting from 1

		for (i = 0; i < toCountLinesArray.length; ++i) {
			if (toCountLinesArray[i]) {
				++realLine;
			}
			realLines[i] = realLine;
		}
	}

	public int firstReadLine() {
		return realLines[0];
	}

	public int lastReadLine() {
		return realLines[realLines.length - 1];
	}

	public int fakeLineFromRealLine(int realLine) {
		int i;
		int fakeLine = 0;
		for (i = 0; i < realLines.length; ++i) {
			if (realLine == realLines[i]) {
				fakeLine = i;
				break;
			}
		}
		return fakeLine;
	}
}
