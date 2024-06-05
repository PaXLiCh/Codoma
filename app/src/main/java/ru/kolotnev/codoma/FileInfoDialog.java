package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.util.Pair;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog with info about file.
 */
public class FileInfoDialog extends DialogFragment implements CollectStatAsyncTask.StatListener {
	public static final String TAG = "FileInfoDialog";
	private ListView list;
	private ProgressBar progressBar;
	private final List<Pair<String, String>> lines = new ArrayList<>();
	private CollectStatAsyncTask task;

	public static FileInfoDialog newInstance(@NonNull TextFile textFile, @NonNull Activity activity) {
		final FileInfoDialog f = new FileInfoDialog();
		f.task = new CollectStatAsyncTask(activity, f);
		f.task.execute(textFile);
		return f;
	}

	@Override
	public void onDestroyView() {
		if (getDialog() != null && getRetainInstance()) {
			getDialog().setDismissMessage(null);
		}
		super.onDestroyView();
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		setRetainInstance(true);
		final View view = View.inflate(getActivity(), R.layout.dialog_file_info, null);
		list = view.findViewById(android.R.id.list);
		progressBar = view.findViewById(android.R.id.progress);
		updateStats();

		return new AlertDialog.Builder(getActivity())
				.setView(view)
				.setTitle(R.string.menu_main_file_info)
				.setPositiveButton(android.R.string.ok, null)
				.create();
	}

	@Override
	public void onDismiss(DialogInterface dialog) {
		super.onDismiss(dialog);
		if (!task.isCancelled()) {
			task.cancel(false);
		}
	}

	@Override
	public void onGetStats(List<Pair<Integer, String>> stats) {
		if (!isAdded()) return;
		for (Pair<Integer, String> stat : stats) {
			Pair<String, String> line = new Pair<>(getString(stat.first), stat.second);
			lines.add(line);
		}
		updateStats();
	}

	private void updateStats() {
		if (lines.isEmpty()) return;
		progressBar.setVisibility(View.GONE);
		list.setVisibility(View.VISIBLE);
		list.setAdapter(new AdapterTwoItem(getActivity(), lines));
	}
}
