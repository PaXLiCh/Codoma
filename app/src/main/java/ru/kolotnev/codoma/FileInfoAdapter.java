package ru.kolotnev.codoma;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.apache.commons.io.FilenameUtils;

import java.util.Arrays;
import java.util.LinkedList;

/**
 * Adapter for recycler view with file details.
 */
public class FileInfoAdapter extends RecyclerView.Adapter<FileInfoAdapter.ViewHolder> {
	private final LinkedList<FileDetail> files = new LinkedList<>();
	private final LinkedList<FileDetail> visibleObjects = new LinkedList<>();
	private String lastQuery = null;
	private OnItemClickListener listener;

	public void setFiles(@NonNull Context context, @NonNull LinkedList<FileDetail> files, final boolean isRoot) {
		this.files.clear();
		this.files.addAll(files);
		if (isRoot) {
			this.files.addFirst(new FileDetail(context.getString(R.string.home), context.getString(R.string.folder), ""));
		} else {
			this.files.addFirst(new FileDetail("..", context.getString(R.string.folder), ""));
		}
		filter(lastQuery);
	}

	public void setOnItemClickListener(OnItemClickListener listener) {
		this.listener = listener;
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

	public void filter(@Nullable String query) {
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

	protected class ViewHolder extends RecyclerView.ViewHolder {
		private final ImageView imageView;
		private final TextView textView1;
		private final TextView textView2;
		private FileDetail fileDetail;

		public ViewHolder(View itemView) {
			super(itemView);
			imageView = (ImageView) itemView.findViewById(android.R.id.icon);
			textView1 = (TextView) itemView.findViewById(android.R.id.text1);
			textView2 = (TextView) itemView.findViewById(android.R.id.text2);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					listener.onItemClick(fileDetail);
				}
			});
		}

		private void setInfo(FileDetail fileDetail) {
			this.fileDetail = fileDetail;
			setIcon(fileDetail);
			textView1.setText(fileDetail.getName());
			textView2.setText(
					textView2.getContext().getString(R.string.file_info_detail,
							fileDetail.getSize(), fileDetail.getDateModified()));
		}

		private void setIcon(final FileDetail fileDetail) {
			final String fileName = fileDetail.getName();
			final String ext = FilenameUtils.getExtension(fileName);
			if (fileDetail.isFolder()) {
				imageView.setImageResource(R.color.file_folder);
			} else if (Arrays.asList(MimeTypes.MIME_HTML).contains(ext) || ext.endsWith("html")) {
				imageView.setImageResource(R.color.file_html);
			} else if (Arrays.asList(MimeTypes.MIME_CODE).contains(ext)
					|| fileName.endsWith("css")
					|| fileName.endsWith("js")) {
				imageView.setImageResource(R.color.file_code);
			} else if (Arrays.asList(MimeTypes.MIME_ARCHIVE).contains(ext)) {
				imageView.setImageResource(R.color.file_archive);
			} else if (Arrays.asList(MimeTypes.MIME_MUSIC).contains(ext)) {
				imageView.setImageResource(R.color.file_media_music);
			} else if (Arrays.asList(MimeTypes.MIME_PICTURE).contains(ext)) {
				imageView.setImageResource(R.color.file_media_picture);
			} else if (Arrays.asList(MimeTypes.MIME_VIDEO).contains(ext)) {
				imageView.setImageResource(R.color.file_media_video);
			} else {
				imageView.setImageResource(R.color.file_text);
			}
		}
	}

	public static class FileDetail {
		private final String name;
		private final String size;
		private final String dateModified;
		private final boolean isFolder;

		public FileDetail(String name, String size, String dateModified) {
			this.name = name;
			this.size = size;
			this.dateModified = dateModified;
			isFolder = TextUtils.isEmpty(dateModified);
		}

		public String getDateModified() {
			return dateModified;
		}

		public String getSize() {
			return size;
		}

		public String getName() {
			return name;
		}

		public boolean isFolder() {
			return isFolder;
		}
	}

	/**
	 * Interface definition for a callback to be invoked when an item in this
	 * Adapter has been clicked.
	 */
	public interface OnItemClickListener {

		/**
		 * Callback method to be invoked when an item in this Adapter has
		 * been clicked.
		 * <p>
		 * Implementers can call getItemAtPosition(position) if they need
		 * to access the data associated with the selected item.
		 *
		 * @param fileDetail clicked file details.
		 */
		void onItemClick(FileDetail fileDetail);
	}
}
