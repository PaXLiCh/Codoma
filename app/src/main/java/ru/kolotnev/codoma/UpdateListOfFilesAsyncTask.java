package ru.kolotnev.codoma;

import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw4.RootFW;

import org.apache.commons.io.*;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * Fetch list of files at specified directory.
 */
class UpdateListOfFilesAsyncTask extends AsyncTask<String, Void, List<FileInfoAdapter.FileDetail>> {
	private String exceptionMessage;
	private String currentFolder;
	private UpdateListOfFilesListener listener;
	private String[] unopenableExtensions;
	private String formatDetailFile;
	private String formatDetailFolder;
	private String stringHome;
	private String stringFolder;

	UpdateListOfFilesAsyncTask(String directory, UpdateListOfFilesListener listener, String[] unopenableExtensions, String formatDetailFile, String formatDetailFolder, String stringHome, String stringFolder) {
		currentFolder = directory;
		this.listener = listener;
		this.unopenableExtensions = unopenableExtensions;
		this.formatDetailFile = formatDetailFile;
		this.formatDetailFolder = formatDetailFolder;
		this.stringHome = stringHome;
		this.stringFolder = stringFolder;
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected ArrayList<FileInfoAdapter.FileDetail> doInBackground(final String... params) {
		try {

			final String path = params[0];
			if (TextUtils.isEmpty(path)) {
				return null;
			}

			File tempFolder = new File(path);
			if (tempFolder.isFile()) {
				tempFolder = tempFolder.getParentFile();
			}
			if (isSymlink(tempFolder)) {
				Log.e(CodomaApplication.TAG, "Symbolic link " + tempFolder.getAbsolutePath() + " " + tempFolder.getCanonicalPath());
			}

			final DateFormat format = DateFormat.getDateInstance();
			final ArrayList<FileInfoAdapter.FileDetail> fileDetails = new ArrayList<>();
			final ArrayList<FileInfoAdapter.FileDetail> folderDetails = new ArrayList<>();
			if (currentFolder.equals("/")) {
				folderDetails.add(new FileInfoAdapter.FileDetail(null, stringHome, stringFolder, true, true));
			} else {
				folderDetails.add(new FileInfoAdapter.FileDetail(null, "..", stringFolder, true, true));
			}

			if (!tempFolder.canRead()) {
				if (RootFW.connect()) {
					com.spazedog.lib.rootfw4.utils.File folder = RootFW.getFile(currentFolder);
					com.spazedog.lib.rootfw4.utils.File.FileStat[] stats = folder.getDetailedList();

					if (stats != null) {
						for (com.spazedog.lib.rootfw4.utils.File.FileStat stat : stats) {
							if (stat.type().equals("d")) {
								folderDetails.add(new FileInfoAdapter.FileDetail(null, stat.name(),
										String.format(formatDetailFolder, "no date"),
										true, true));
							} else if (!FilenameUtils.isExtension(stat.name().toLowerCase(), unopenableExtensions)
									&& stat.size() <= CodomaApplication.MAX_FILE_SIZE * org.apache.commons.io.FileUtils.ONE_KB) {
								final long fileSize = stat.size();
								String date = format.format(stat);
								String description = String.format(formatDetailFile,
										org.apache.commons.io.FileUtils.byteCountToDisplaySize(fileSize),
										date);
								fileDetails.add(new FileInfoAdapter.FileDetail(
										null,
										stat.name(),
										description,
										true, false));
							}
						}
					}
				} else {
					listener.onCantOpen(tempFolder.getAbsolutePath());
					return null;
				}
			} else {
				currentFolder = tempFolder.getAbsolutePath();

				File[] files = tempFolder.listFiles();

				Arrays.sort(files, getFileNameComparator());

				for (final File f : files) {
					Uri uri = Uri.parse(f.toURI().toString());
					if (f.isDirectory()) {
						String description =
								String.format(formatDetailFolder, format.format(f.lastModified()));
						folderDetails.add(new FileInfoAdapter.FileDetail(uri, f.getName(),
								description,true, true));
					} else if (f.isFile()
							&& !FilenameUtils.isExtension(f.getName().toLowerCase(), unopenableExtensions)
							&& org.apache.commons.io.FileUtils.sizeOf(f) <= CodomaApplication.MAX_FILE_SIZE * org.apache.commons.io.FileUtils.ONE_KB) {
						String description = String.format(
								formatDetailFile,
								org.apache.commons.io.FileUtils.byteCountToDisplaySize(f.length()),
								format.format(f.lastModified()));
						fileDetails.add(new FileInfoAdapter.FileDetail(
								uri, f.getName(), description, true, false));
					}
				}
			}

			// Build final list with folders at top
			folderDetails.addAll(fileDetails);
			return folderDetails;
		} catch (Exception e) {
			exceptionMessage = e.getMessage();
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final List<FileInfoAdapter.FileDetail> names) {
		if (exceptionMessage != null) {
			listener.onException(exceptionMessage);
		} else {
			listener.onUpdateList(names);
		}
		super.onPostExecute(names);
	}

	@SuppressWarnings("unchecked")
	private Comparator<File> getFileNameComparator() {
		return new AlphanumComparator() {
			/**
			 * {@inheritDoc}
			 */
			@Override
			public String getTheString(Object obj) {
				return ((File) obj).getName().toLowerCase();
			}
		};
	}

	private static boolean isSymlink(@NonNull File file) throws IOException {
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}

	interface UpdateListOfFilesListener {
		void onUpdateList(final List<FileInfoAdapter.FileDetail> names);
		void onException(@NonNull String path);
		void onCantOpen(@NonNull String fileName);
	}
}
