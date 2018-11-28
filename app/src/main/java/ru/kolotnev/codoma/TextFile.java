package ru.kolotnev.codoma;

import android.content.SharedPreferences;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.style.UnderlineSpan;

import java.util.LinkedList;
import java.util.List;

/**
 * Container for new or opened text file.
 */
public class TextFile implements TextWatcher {
	// TODO: move to preferences
	static final boolean IS_SHOW_EXTENSIONS = true;
	/**
	 * The edit history.
	 */
	private final EditHistory mEditHistory = new EditHistory();
	GreatUri greatUri;
	public String encoding = PreferenceHelper.DEFAULT_ENCODING;
	LineReader.LineEnding eol = LineReader.LineEnding.LF;
	boolean isSplitIntoPages;

	/**
	 * Is undo/redo being performed? This member
	 * signals if an undo/redo operation is
	 * currently being performed. Changes in the
	 * text during undo/redo are not recorded
	 * because it would mess up the undo history.
	 */
	private boolean mIsUndoOrRedo;
	private boolean mShowUndo, mShowRedo;
	private boolean isModified;
	/**
	 * The text that will be removed by the change event.
	 */
	private CharSequence mBeforeChange;

	/**
	 * Instantiate new text file.
	 * Pagination for file.
	 */
	TextFile() {
		setMaxHistorySize(30);
	}

	/**
	 * Return title for editor.
	 *
	 * @return String with name of file.
	 */
	public String getTitle() {
		String title;
		// if no new uri
		if (greatUri == null
				|| greatUri.getUri() == null
				|| greatUri.getUri() == Uri.EMPTY) {
			title = "New file";
		} else {
			title = greatUri.getFileName();
			if (IS_SHOW_EXTENSIONS) {
				String extension = greatUri.getFileExtension();
				if (!extension.isEmpty())
					title += "." + extension;
			}
		}
		return title;
	}

	//region UNDO REDO

	public boolean isModified() {
		return isModified;
	}

	void setModified() {
		this.isModified = true;
	}

	void setModified(boolean isModified) {
		this.isModified = isModified;
	}

	void fileSaved() {
		isModified = false;
		clearHistory();
	}

	/**
	 * Can undo be performed?
	 */
	boolean getCanUndo() {
		return (mEditHistory.mmPosition > 0);
	}

	/**
	 * Can redo be performed?
	 */
	boolean getCanRedo() {
		return (mEditHistory.mmPosition < mEditHistory.mmHistory.size());
	}

	/**
	 * Set the maximum history size. If size is
	 * negative, then history size is only limited
	 * by the device memory.
	 */
	void setMaxHistorySize(int maxHistorySize) {
		mEditHistory.setMaxHistorySize(maxHistorySize);
	}

	/**
	 * Clear edit history.
	 */
	void clearHistory() {
		mEditHistory.clear();
		mShowUndo = getCanUndo();
		mShowRedo = getCanRedo();
	}

	/**
	 * Perform undo.
	 *
	 * @param text
	 * 		Editable text.
	 */
	void undo(@NonNull Editable text) {
		EditItem edit = mEditHistory.getPrevious();
		if (edit == null) {
			return;
		}

		int start = edit.mmStart;
		int end = start + (edit.mmAfter != null
				? edit.mmAfter.length() : 0);

		mIsUndoOrRedo = true;
		text.replace(start, end, edit.mmBefore);
		mIsUndoOrRedo = false;

		// This will get rid of underlines inserted when editor tries to come
		// up with a suggestion.
		for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
			text.removeSpan(o);
		}

		Selection.setSelection(text,
				edit.mmBefore == null
						? start
						: (start + edit.mmBefore.length()));
	}

	/**
	 * Perform redo.
	 *
	 * @param text
	 * 		Editable text.
	 */
	void redo(@NonNull Editable text) {
		EditItem edit = mEditHistory.getNext();
		if (edit == null) {
			return;
		}

		int start = edit.mmStart;
		int end = start + (edit.mmBefore != null
				? edit.mmBefore.length() : 0);

		mIsUndoOrRedo = true;
		text.replace(start, end, edit.mmAfter);
		mIsUndoOrRedo = false;

		// This will get rid of underlines inserted when editor tries to come
		// up with a suggestion.
		for (Object o : text.getSpans(0, text.length(), UnderlineSpan.class)) {
			text.removeSpan(o);
		}

		Selection.setSelection(text,
				edit.mmAfter == null
						? start
						: (start + edit.mmAfter.length()));
	}

	//endregion

	/**
	 * Store preferences.
	 */
	public void storePersistentState(SharedPreferences.Editor editor, String prefix) {
		// Store hash code of text in the editor so that we can check if the
		// editor contents has changed.
		//editor.putString(prefix + ".hash",
		//		String.valueOf(editText.getText().toString().hashCode()));
		editor.putInt(prefix + ".maxSize",
				mEditHistory.mmMaxHistorySize);
		editor.putInt(prefix + ".position",
				mEditHistory.mmPosition);
		editor.putInt(prefix + ".size",
				mEditHistory.mmHistory.size());

		int i = 0;
		for (EditItem ei : mEditHistory.mmHistory) {
			String pre = prefix + "." + i;

			editor.putInt(pre + ".start", ei.mmStart);
			editor.putString(pre + ".before",
					ei.mmBefore.toString());
			editor.putString(pre + ".after",
					ei.mmAfter.toString());

			++i;
		}
	}


	/**
	 * Restore preferences.
	 *
	 * @param prefix
	 * 		The preference key prefix
	 * 		used when state was stored.
	 *
	 * @return did restore succeed? If this is
	 * false, the undo history will be empty.
	 */
	public boolean restorePersistentState(SharedPreferences sp, String prefix)
			throws IllegalStateException {

		boolean ok = doRestorePersistentState(sp, prefix);
		if (!ok) {
			mEditHistory.clear();
		}

		return ok;
	}

	private boolean doRestorePersistentState(SharedPreferences sp, String prefix) {
		String hash = sp.getString(prefix + ".hash", null);
		if (hash == null) {
			// No state to be restored.
			return true;
		}

		//if (Integer.valueOf(hash) != editText.getText().toString().hashCode()) {
		//	return false;
		//}

		mEditHistory.clear();
		mEditHistory.mmMaxHistorySize = sp.getInt(prefix + ".maxSize", -1);

		int count = sp.getInt(prefix + ".size", -1);
		if (count == -1) {
			return false;
		}

		for (int i = 0; i < count; ++i) {
			String pre = prefix + "." + i;

			int start = sp.getInt(pre + ".start", -1);
			String before = sp.getString(pre + ".before", null);
			String after = sp.getString(pre + ".after", null);

			if (start == -1 || before == null || after == null) {
				return false;
			}
			mEditHistory.add(new EditItem(start, before, after));
		}

		mEditHistory.mmPosition = sp.getInt(prefix + ".position", -1);
		return mEditHistory.mmPosition != -1;
	}

	/**
	 * Class that listens to changes in the text.
	 */

	@Override
	public void beforeTextChanged(CharSequence s, int start, int count, int after) {
		if (mIsUndoOrRedo) {
			return;
		}

		mBeforeChange = s.subSequence(start, start + count);
	}

	@Override
	public void onTextChanged(CharSequence s, int start, int before, int count) {
		if (mIsUndoOrRedo) {
			return;
		}

		// The text that was inserted by the change event
		CharSequence afterChange = s.subSequence(start, start + count);
		mEditHistory.add(new EditItem(start, mBeforeChange, afterChange));
	}

	@Override
	public void afterTextChanged(Editable s) {
		boolean showUndo = getCanUndo();
		boolean showRedo = getCanRedo();
		isModified = showUndo;
		if (showUndo != mShowUndo || showRedo != mShowRedo) {
			mShowUndo = showUndo;
			mShowRedo = showRedo;
		}
		// Update content of current page
		setCurrentPageText(s.toString());
	}

	//region PAGE SYSTEM

	private final List<String> pages = new LinkedList<>();
	private int[] startingLines;
	private int currentPage = 0;
	private PageSystemListener pageSystemListener;

	void setupPageSystem(@NonNull String text, boolean pageSystemEnabled) {
		final int charForPage = 20000;
		final int firstPageChars = 50000;

		final int textLength = text.length();

		if (pageSystemEnabled) {
			int i = 0;
			while (i < textLength) {
				// first page is longer
				int to = i + (i == 0 ? firstPageChars : charForPage);
				int nextIndexOfReturn = text.indexOf("\n", to);
				if (nextIndexOfReturn > to) to = nextIndexOfReturn;
				if (to > text.length()) to = text.length();
				pages.add(text.substring(i, to));
				i = to + 1;
			}

			if (i == 0)
				pages.add("");
		} else {
			pages.add(text);
		}

		startingLines = new int[pages.size()];
		setStartingLines();

		this.isSplitIntoPages = pageSystemEnabled && pages.size() > 1;
	}

	void setPageSystemListener(@Nullable PageSystemListener pageSystemListener) {
		this.pageSystemListener = pageSystemListener;
	}

	int getStartingLine() {
		return startingLines[currentPage];
	}

	String getCurrentPageText() {
		return pages.get(currentPage);
	}

	public String getTextOfNextPages(boolean includeCurrent, int nOfPages) {
		StringBuilder stringBuilder = new StringBuilder();
		int i;
		for (i = includeCurrent ? 0 : 1; i < nOfPages; ++i) {
			if (pages.size() > (currentPage + i)) {
				stringBuilder.append(pages.get(currentPage + 1));
			}
		}

		return stringBuilder.toString();
	}

	private void setCurrentPageText(@NonNull String currentText) {
		pages.set(currentPage, currentText);
	}

	public void nextPage() {
		if (!canReadNextPage()) return;
		goToPage(currentPage + 1);
	}

	public void prevPage() {
		if (!canReadPrevPage()) return;
		goToPage(currentPage - 1);
	}

	void goToPage(int page) {
		if (page >= pages.size()) page = pages.size() - 1;
		if (page < 0) page = 0;
		boolean shouldUpdateLines = page > currentPage && canReadNextPage();
		if (shouldUpdateLines) {
			String text = getCurrentPageText();
			// normally the last line is not counted so we have to add 1
			int nOfNewLineNow = (text.length() - text.replace("\n", "").length()) + 1;
			int nOfNewLineBefore = startingLines[currentPage + 1] - startingLines[currentPage];
			int difference = nOfNewLineNow - nOfNewLineBefore;
			updateStartingLines(currentPage + 1, difference);
		}
		currentPage = page;
		pageSystemListener.onPageChanged(page);
	}

	void setStartingLines() {
		int i;
		int startingLine;
		int nOfNewLines;
		String text;
		startingLines[0] = 0;
		for (i = 1; i < pages.size(); ++i) {
			text = pages.get(i - 1);
			nOfNewLines = text.length() - text.replace("\n", "").length() + 1;
			startingLine = startingLines[i - 1] + nOfNewLines;
			startingLines[i] = startingLine;
		}
	}

	void updateStartingLines(int fromPage, int difference) {
		if (difference == 0)
			return;
		int i;
		if (fromPage < 1) fromPage = 1;
		for (i = fromPage; i < pages.size(); ++i) {
			startingLines[i] += difference;
		}
	}

	int getMaxPage() {
		return pages.size() - 1;
	}

	int getCurrentPage() {
		return currentPage;
	}

	String getAllText() {
		int i;
		StringBuilder allText = new StringBuilder();
		for (i = 0; i < pages.size(); ++i) {
			allText.append(pages.get(i));
			if (i < pages.size() - 1)
				allText.append("\n");
		}
		return allText.toString();
	}

	boolean canReadNextPage() {
		return currentPage < pages.size() - 1;
	}

	boolean canReadPrevPage() {
		return currentPage >= 1;
	}

	public interface PageSystemListener {
		void onCurrentTextChanged();
		void onPageChanged(int page);
	}

	//endregion

	//region EDIT HISTORY

	/**
	 * Keeps track of all the edit history of a text.
	 */
	private final class EditHistory {
		/**
		 * The list of edits in chronological order.
		 */
		private final LinkedList<EditItem> mmHistory = new LinkedList<>();

		/**
		 * The position from which an EditItem will
		 * be retrieved when getNext() is called. If
		 * getPrevious() has not been called, this
		 * has the same value as mmHistory.size().
		 */
		private int mmPosition = 0;

		/**
		 * Maximum undo history size.
		 */
		private int mmMaxHistorySize = -1;

		private int size() {
			return mmHistory.size();
		}

		/**
		 * Clear history.
		 */
		private void clear() {
			mmPosition = 0;
			mmHistory.clear();
		}

		/**
		 * Adds a new edit operation to the history
		 * at the current position. If executed
		 * after a call to getPrevious() removes all
		 * the future history (elements with
		 * positions >= current history position).
		 */
		private void add(EditItem item) {
			while (mmHistory.size() > mmPosition) {
				mmHistory.removeLast();
			}
			mmHistory.add(item);
			++mmPosition;

			if (mmMaxHistorySize >= 0) {
				trimHistory();
			}
		}

		/**
		 * Trim history when it exceeds max history size.
		 */
		private void trimHistory() {
			while (mmHistory.size() > mmMaxHistorySize) {
				mmHistory.removeFirst();
				--mmPosition;
			}

			if (mmPosition < 0) {
				mmPosition = 0;
			}
		}

		/**
		 * Set the maximum history size. If size is
		 * negative, then history size is only
		 * limited by the device memory.
		 */
		private void setMaxHistorySize(int maxHistorySize) {
			mmMaxHistorySize = maxHistorySize;
			if (mmMaxHistorySize >= 0) {
				trimHistory();
			}
		}

		/**
		 * Traverses the history backward by one
		 * position, returns and item at that
		 * position.
		 */
		@Nullable
		private EditItem getPrevious() {
			if (mmPosition == 0) {
				return null;
			}
			--mmPosition;
			return mmHistory.get(mmPosition);
		}

		/**
		 * Traverses the history forward by one
		 * position, returns and item at that
		 * position.
		 */
		@Nullable
		private EditItem getNext() {
			if (mmPosition >= mmHistory.size()) {
				return null;
			}

			EditItem item = mmHistory.get(mmPosition);
			++mmPosition;
			return item;
		}
	}

	/**
	 * Represents the changes performed by a
	 * single edit operation.
	 */
	private final class EditItem {
		private final int mmStart;
		private final CharSequence mmBefore;
		private final CharSequence mmAfter;

		/**
		 * Constructs EditItem of a modification
		 * that was applied at position start and
		 * replaced CharSequence before with
		 * CharSequence after.
		 */
		EditItem(int start, CharSequence before, CharSequence after) {
			mmStart = start;
			mmBefore = before;
			mmAfter = after;
		}
	}
	//endregion

}
