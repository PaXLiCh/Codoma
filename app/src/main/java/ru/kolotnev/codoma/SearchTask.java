package ru.kolotnev.codoma;

import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.lang.ref.WeakReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Task for searching by specified pattern in the text.
 */
class SearchTask extends AsyncTask<String, Void, SearchResult> {
	@NonNull
	private final String whatToSearch;
	private final boolean isCase;
	private final int start;
	private final int end;
	private IndeterminateProgressDialogFragment progressDialog;
	private OnSearchResultListener listener;
	private WeakReference<AppCompatActivity> activity;

	SearchTask(AppCompatActivity activity, @NonNull String whatToSearch,
			boolean caseSensitive, int start, int end, OnSearchResultListener listener) {
		this.activity = new WeakReference<>(activity);
		this.start = start;
		this.end = end;
		this.whatToSearch = whatToSearch;
		this.isCase = caseSensitive;
		this.listener = listener;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();

		progressDialog = IndeterminateProgressDialogFragment.newInstance(R.string.dialog_progress_search_message);
		progressDialog.show(activity.get().getSupportFragmentManager(), "dialog_progress_search");
	}

	@Override
	protected SearchResult doInBackground(String... params) {
		try {
			int flags = Pattern.MULTILINE;
			if (isCase)
				flags |= Pattern.CASE_INSENSITIVE;
			Matcher matcher = Pattern.compile(whatToSearch, flags).matcher(params[0]);
			matcher = matcher.region(start, end);
			SearchResult result = new SearchResult(whatToSearch);
			while (matcher.find()) {
				result.addResult(matcher.start(), matcher.end());
			}
			return result;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	protected void onPostExecute(SearchResult result) {
		super.onPostExecute(result);
		if (listener != null)
			listener.onResult(result);
		progressDialog.dismiss();
	}

	interface OnSearchResultListener {
		void onResult(SearchResult result);
	}
}
