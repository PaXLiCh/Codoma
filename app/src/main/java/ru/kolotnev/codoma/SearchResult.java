package ru.kolotnev.codoma;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Search result.
 */
public class SearchResult {
	public static class SearchItem {
		public int start = 0;
		public int end = 0;

		public SearchItem(int start, int end) {
			this.start = start;
			this.end = end;
		}
	}

	public int indexCorrect = 0;
	private final List<SearchItem> items = new ArrayList<>();

	// list of index
	public int textLength;
	public boolean isReplace;
	public String textToReplace;
	public int index;
	public String whatToSearch;


	public SearchResult(@NonNull String whatToSearch) {
		//this.isReplace = isReplace;
		this.whatToSearch = whatToSearch;
		//this.textToReplace = textToReplace;
	}

	public void addResult(int start, int end) {
		items.add(new SearchItem(start, end));
	}

	public int getAmount() {
		return items.size();
	}

	public void doneReplace() {
		items.remove(index);
		int i;
		for (i = index; i < items.size(); ++i) {
			//items.set(i, items.get(i) + textToReplace.length() - textLength);
		}
		--index; // an element was removed so we decrease the index
	}

	public int numberOfResults() {
		return items.size();
	}

	public boolean hasNext() {
		return index < items.size() - 1;
	}

	public boolean hasPrevious() {
		return index > 0;
	}

	public boolean canReplaceSomething() {
		return isReplace && items.size() > 0;
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
}
