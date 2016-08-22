package ru.kolotnev.codoma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.LinkedList;
import java.util.List;

/**
 * Search result.
 */
public class SearchResult {
	@NonNull
	public final String whatToSearch;
	private final List<SearchItem> items = new LinkedList<>();
	private int index;

	public SearchResult(@NonNull String whatToSearch) {
		this.whatToSearch = whatToSearch;
	}

	public void addResult(int start, int end) {
		items.add(new SearchItem(start, end));
	}

	public int getAmount() {
		return items.size();
	}

	public boolean hasNext() {
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

	public boolean cycle() {
		boolean isHasNext = hasNext();
		if (isHasNext)
			++index;
		else {
			index = 0;
		}
		return index < items.size();
	}

	@NonNull
	public List<SearchItem> getItems() {
		return items;
	}

	@Nullable
	public SearchItem getCurrentItem() {
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

	public class SearchItem {
		public int start = 0;
		public int end = 0;

		public SearchItem(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}
}
