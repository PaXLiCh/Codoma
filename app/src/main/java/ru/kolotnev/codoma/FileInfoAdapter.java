package ru.kolotnev.codoma;

import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Adapter for recycler view with file details.
 */
class FileInfoAdapter extends RecyclerView.Adapter<FileInfoAdapter.ViewHolder> {
	private final ArrayList<FileDetail> files = new ArrayList<>();
	private final ArrayList<FileDetail> visibleObjects = new ArrayList<>();
	private String lastQuery = null;
	@NonNull
	private final Context context;
	@Nullable
	private final OnItemClickListener listener;

	FileInfoAdapter(@NonNull final Context context, @Nullable OnItemClickListener listener) {
		this.context = context;
		this.listener = listener;
	}

	public void setFiles(@NonNull List<FileDetail> files) {
		this.files.clear();
		this.files.addAll(files);
		filter(lastQuery);
	}

	@Override
	public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
		View view = LayoutInflater.from(parent.getContext())
				.inflate(R.layout.item_two_lines_icon, parent, false);
		return new ViewHolder(view);
	}

	@Override
	public void onBindViewHolder(ViewHolder holder, int position) {
		holder.setInfo(visibleObjects.get(position));
	}

	@Override
	public int getItemCount() {
		return visibleObjects.size();
	}

	void filter(@Nullable String query) {
		lastQuery = query;
		visibleObjects.clear();
		if (query == null || query.isEmpty()) {
			visibleObjects.addAll(files);
		} else {
			CharSequence lowerCs = query.toLowerCase();
			for (FileDetail item : files) {
				if (item.getName().toLowerCase().contains(lowerCs))
					visibleObjects.add(item);
			}
		}
		notifyDataSetChanged();
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * Adapter has been clicked.
	 */
	interface OnItemClickListener {

		/**
		 * Callback method to be invoked when an item in this Adapter has
		 * been clicked.
		 * <p/>
		 * Implementers can call getItemAtPosition(position) if they need
		 * to access the data associated with the selected item.
		 *
		 * @param fileDetail
		 * 		clicked file details.
		 */
		void onItemClick(@NonNull FileDetail fileDetail);
	}

	static class FileDetail {
		@NonNull
		private final String name;
		private final String description;
		private final boolean isFolder;
		private final boolean isExist;
		@NonNull
		private final Uri uri;
		@Nullable
		private final Uri symlinkUri;
		private final boolean isRootRequired;

		FileDetail(@NonNull Uri uri, @NonNull String name, @NonNull String description, boolean isExist, boolean isFolder) {
			this(uri, null, name, description, isExist, isFolder, false);
		}

		FileDetail(@NonNull Uri uri, @NonNull String name, @NonNull String description, boolean isExist, boolean isFolder, boolean isRootRequired) {
			this(uri, null, name, description, isExist, isFolder, isRootRequired);
		}

		FileDetail(@NonNull Uri canonicalUri, @Nullable Uri symlinkUri, @NonNull String name, @NonNull String description, boolean isExist, boolean isFolder) {
			this(canonicalUri, symlinkUri, name, description, isExist, isFolder, false);
		}

		FileDetail(@NonNull Uri canonicalUri, @Nullable Uri symlinkUri, @NonNull String name, @NonNull String description, boolean isExist, boolean isFolder, boolean isRootRequired) {
			this.uri = canonicalUri;
			this.symlinkUri = symlinkUri;
			if (symlinkUri != null) {
				this.name = name + " -> " + canonicalUri.getPath();
			} else {
				this.name = name;
			}
			this.description = description;
			this.isExist = isExist;
			this.isFolder = isFolder;
			this.isRootRequired = isRootRequired;
		}

		@NonNull
		public Uri getUri() {
			return uri;
		}

		@Nullable
		public Uri getSymlinkUri() { return symlinkUri; }

		@NonNull
		public String getName() {
			return name;
		}

		public String getDescription() {
			return description;
		}

		public boolean isExist() {
			return isExist;
		}

		public boolean isFolder() {
			return isFolder;
		}

		public boolean isRootRequired() { return isRootRequired; }
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private final GradientDrawable iconBack;
		private final ImageView iconImage;
		private final TextView textView1;
		private final TextView textView2;
		private FileDetail fileDetail;

		ViewHolder(View itemView) {
			super(itemView);
			iconImage = itemView.findViewById(android.R.id.icon);
			iconBack = (GradientDrawable) iconImage.getBackground();
			textView1 = itemView.findViewById(android.R.id.text1);
			textView2 = itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (listener != null)
						listener.onItemClick(fileDetail);
				}
			});
		}

		private void setInfo(@NonNull FileDetail fileDetail) {
			this.fileDetail = fileDetail;
			setIcon(fileDetail);
			textView1.setText(fileDetail.getName());
			textView2.setText(fileDetail.getDescription());
		}

		private void setIcon(@NonNull final FileDetail fileDetail) {
			final String fileName = fileDetail.getName();
			final String ext = FilenameUtils.getExtension(fileName);
			int color;
			if (fileDetail.isFolder()) {
				color = R.color.file_folder;
			} else if (Arrays.asList(MimeTypes.MIME_HTML).contains(ext) || ext.endsWith("html")) {
				color = R.color.file_html;
			} else if (Arrays.asList(MimeTypes.MIME_CODE).contains(ext)) {
				color = R.color.file_code;
			} else if (Arrays.asList(MimeTypes.MIME_ARCHIVE).contains(ext)) {
				color = R.color.file_archive;
			} else if (Arrays.asList(MimeTypes.MIME_MUSIC).contains(ext)) {
				color = R.color.file_media_music;
			} else if (Arrays.asList(MimeTypes.MIME_PICTURE).contains(ext)) {
				color = R.color.file_media_picture;
			} else if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
				color = R.color.file_media_video;
			} else {
				color = R.color.file_text;
			}

			if (fileDetail.symlinkUri != null) {
				color = android.R.color.black;
			}
			((GradientDrawable) iconBack.mutate()).setColor(ContextCompat.getColor(context, color));

			int image;
			if (fileDetail.isExist()) {
				if (fileDetail.isFolder()) {
					image = R.drawable.ic_folder_white_24dp;
				} else {
					image = R.drawable.ic_insert_drive_file_white_24dp;
				}
			} else {
				image = R.drawable.ic_action_close;
			}
			iconImage.setImageResource(image);
		}
	}
}
