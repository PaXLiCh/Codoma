package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Dialog;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Dialog with list of recent files.
 */
public class RecentFilesDialogFragment extends DialogFragment {
	public static final String TAG = "DialogRecentFiles";

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Activity context = getActivity();

		// Populating list of recent files
		List<RecentFilesProvider.RecentFile> fileList = RecentFilesProvider.getRecentFiles();

		ArrayList<FileInfoAdapter.FileDetail> iconifiedEntries = new ArrayList<>();

		if (fileList.size() > 0) {
			for (RecentFilesProvider.RecentFile recentFile : fileList) {
				String uriString = recentFile.getFileName();

				Uri uri = Uri.parse(uriString);
				String fullPath = AccessStorageApi.getPath(context, uri);
				String fileName = FilenameUtils.getName(fullPath);
				File file = new File(fullPath);
				boolean isExists = file.exists() && file.isFile();
				String dateTime;
				try {
					DateFormat format = DateFormat.getDateTimeInstance();
					Date netDate = (new Date(recentFile.getTimestamp()));
					dateTime = format.format(netDate);
				} catch (Exception ex) {
					dateTime = "xx";
				}

				FileInfoAdapter.FileDetail entry;
				if (isExists) {
					entry = new FileInfoAdapter.FileDetail(uri, fileName, dateTime, true, false);
				} else {
					entry = new FileInfoAdapter.FileDetail(
							uri,
							getString(R.string.file_picker_file_not_found, fileName),
							dateTime, false, false);
				}
				iconifiedEntries.add(entry);
			}
		}

		FileInfoAdapter.OnItemClickListener listener = new FileInfoAdapter.OnItemClickListener() {
			@Override
			public void onItemClick(@NonNull FileInfoAdapter.FileDetail fileDetail) {
				if (context instanceof Callbacks) {
					Callbacks callbacks = (Callbacks) getContext();
					callbacks.onRecentFileSelected(fileDetail.getUri());
				}
				dismiss();
			}
		};

		View rootView = View.inflate(context, R.layout.dialog_recent_files, null);
		RecyclerView fileListView = rootView.findViewById(android.R.id.list);
		FileInfoAdapter adapter = new FileInfoAdapter(context, listener);
		adapter.setFiles(iconifiedEntries);
		fileListView.setAdapter(adapter);
		fileListView.setVisibility(iconifiedEntries.size() > 0 ? View.VISIBLE : View.GONE);

		rootView.findViewById(android.R.id.empty)
				.setVisibility(iconifiedEntries.size() == 0 ? View.VISIBLE : View.GONE);

		return new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_recent_files)
				.setView(rootView)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

	interface Callbacks {
		void onRecentFileSelected(Uri uri);
	}

}
