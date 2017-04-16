package ru.kolotnev.codoma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Search result.
 */
class SearchResult {
	@NonNull
	final String whatToSearch;
	private final List<SearchItem> items = new LinkedList<>();
	private int index;

	SearchResult(@NonNull String whatToSearch) {
		this.whatToSearch = whatToSearch;
	}

	void addResult(int start, int end) {
		items.add(new SearchItem(start, end));
	}

	int getAmount() {
		return items.size();
	}

	private boolean hasNext() {
		return index < items.size() - 1;
	}

	public boolean hasPrevious() {
		return index > 0 && index < items.size();
	}

	public boolean gotoNext() {
		boolean isHasNext = hasNext();
		if (isHasNext)
			++index;
		return isHasNext;
	}

	boolean cycle() {
		boolean isHasNext = hasNext();
		if (isHasNext)
			++index;
		else {
			index = 0;
		}
		return index < items.size();
	}

	@NonNull
	List<SearchItem> getItems() {
		return items;
	}

	@Nullable
	SearchItem getCurrentItem() {
		return (index >= 0 && index < items.size()) ? items.get(index) : null;
	}

	/**
	 * Replace the current item with specified text and correct offset of next results.
	 *
	 * @param textToReplace
	 * 		New text for current item.
	 */
	public void replace(@NonNull String textToReplace) {
		if (index < 0 || index >= items.size()) {return;}
		SearchItem itemWhichWillBeReplaced = items.get(index);
		items.remove(index);
		// Length of the current replaced item
		int textLength = itemWhichWillBeReplaced.end - itemWhichWillBeReplaced.start;
		for (int i = index; i < items.size(); ++i) {
			int offset = textToReplace.length() - textLength;
			SearchItem item = items.get(i);
			item.start += offset;
			item.end += offset;
		}
		// If reached end of the list, reset index to the start
		if (index >= items.size())
			index = 0;
	}

	class SearchItem {
		public int start = 0;
		public int end = 0;

		SearchItem(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
}
