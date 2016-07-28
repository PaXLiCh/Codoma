package ru.kolotnev.codoma;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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

		ArrayList<FileDetail> iconifiedEntries = new ArrayList<>();

		if (fileList.size() > 0) {
			//Drawable iconFile = ContextCompat.getDrawable(context, R.drawable.ic_action_file);
			//Drawable iconMissing = ContextCompat.getDrawable(context, R.drawable.ic_action_close);

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

				Drawable iconFile = ContextCompat.getDrawable(context, R.drawable.ic_action_file);
				Drawable iconMissing = ContextCompat.getDrawable(context, R.drawable.ic_action_close);
				FileDetail entry;
				if (isExists) {
					entry = new FileDetail(fileName, uri, iconFile, dateTime, true);
				} else {
					entry = new FileDetail(
							getString(R.string.file_picker_file_not_found, fileName),
							uri, iconMissing, dateTime, false);
				}
				iconifiedEntries.add(entry);
			}
		}

		View rootView = View.inflate(context, R.layout.dialog_recent_files, null);
		RecyclerView fileListView = (RecyclerView) rootView.findViewById(android.R.id.list);
		IconifiedListAdapter adapter = new IconifiedListAdapter(iconifiedEntries);
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

	public interface Callbacks {
		void onRecentFileSelected(Uri uri);
	}

	public static class FileDetail {
		private final String name;
		private final Uri uri;
		private final String dateModified;
		private final boolean isExist;
		private final Drawable icon;

		public FileDetail(@NonNull String name,
				@NonNull Uri uri,
				@NonNull Drawable icon,
				@NonNull String dateModified,
				boolean isExist) {
			this.name = name;
			this.uri = uri;
			this.icon = icon;
			this.dateModified = dateModified;
			this.isExist = isExist;
		}

		public String getDateModified() {
			return dateModified;
		}

		public Drawable getIcon() {
			return icon;
		}

		public String getName() {
			return name;
		}

		public Uri getUri() {
			return uri;
		}

		public boolean isExist() {
			return isExist;
		}
	}

	private class IconifiedListAdapter extends RecyclerView.Adapter<IconifiedListAdapter.ViewHolder> {
		private List<FileDetail> mItems = new ArrayList<>();
		private List<FileDetail> mOriginalItems = new ArrayList<>();

		public IconifiedListAdapter(List<FileDetail> lit) {
			mOriginalItems = lit;
			mItems.addAll(lit);
		}

		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
			View view = LayoutInflater.from(parent.getContext())
					.inflate(R.layout.item_two_lines_icon, parent, false);
			return new ViewHolder(view);
		}

		@Override
		public void onBindViewHolder(ViewHolder holder, int position) {
			holder.setInfo(mItems.get(position));
		}

		/** @return The number of items in the */
		@Override
		public int getItemCount() {
			return mItems.size();
		}

		public void filter(@Nullable String query) {
			mItems.clear();
			if (query == null || query.isEmpty()) {
				mItems.addAll(mOriginalItems);
			} else {
				CharSequence lowerCs = query.toLowerCase();
				for (FileDetail file : mOriginalItems) {
					if (file.getName().toLowerCase().contains(lowerCs))
						mItems.add(file);
				}
			}
			notifyDataSetChanged();
		}

		protected class ViewHolder extends RecyclerView.ViewHolder {
			// Name of the file
			private final TextView textName;

			// Size of the file
			private final TextView textDetail;

			// Icon of the file
			private final ImageView icon;

			private FileDetail fileDetail;

			public ViewHolder(@NonNull View itemView) {
				super(itemView);
				textName = (TextView) itemView.findViewById(android.R.id.text1);
				textDetail = (TextView) itemView.findViewById(android.R.id.text2);
				icon = (ImageView) itemView.findViewById(android.R.id.icon);
				itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						if (getContext() instanceof Callbacks) {
							Callbacks callbacks = (Callbacks) getContext();
							callbacks.onRecentFileSelected(fileDetail.getUri());
						}
						RecentFilesDialogFragment.this.dismiss();
					}
				});
			}

			private void setInfo(@NonNull FileDetail fileDetail) {
				this.fileDetail = fileDetail;
				setIcon(fileDetail);
				textName.setText(fileDetail.getName());
				textDetail.setText(fileDetail.getDateModified());
			}

			private void setIcon(@NonNull final FileDetail fileDetail) {
				final String fileName = fileDetail.getName();
				if (fileDetail.isExist()) {
					final String ext = FilenameUtils.getExtension(fileName);
					if (Arrays.asList(MimeTypes.MIME_HTML).contains(ext) || ext.endsWith("html")) {
						icon.setImageResource(R.color.file_html);
					} else if (Arrays.asList(MimeTypes.MIME_CODE).contains(ext)
							|| fileName.endsWith("css")
							|| fileName.endsWith("js")) {
						icon.setImageResource(R.color.file_code);
					} else if (Arrays.asList(MimeTypes.MIME_ARCHIVE).contains(ext)) {
						icon.setImageResource(R.color.file_archive);
					} else if (Arrays.asList(MimeTypes.MIME_MUSIC).contains(ext)) {
						icon.setImageResource(R.color.file_media_music);
					} else if (Arrays.asList(MimeTypes.MIME_PICTURE).contains(ext)) {
						icon.setImageResource(R.color.file_media_picture);
					} else if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
						icon.setImageResource(R.color.file_media_video);
					} else {
						icon.setImageResource(R.color.file_text);
					}
				} else {
					icon.setImageDrawable(fileDetail.icon);
				}
			}
		}
	}

}
