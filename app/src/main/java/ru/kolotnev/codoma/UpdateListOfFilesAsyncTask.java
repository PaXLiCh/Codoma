package ru.kolotnev.codoma;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;

import com.spazedog.lib.rootfw4.RootFW;

import org.apache.commons.io.FilenameUtils;

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
	private static final String TAG = UpdateListOfFilesAsyncTask.class.getSimpleName();
	private String exceptionMessage;
	@NonNull
	private String currentDirectory;
	@Nullable
	private UpdateListOfFilesListener listener;
	@NonNull
	private String formatDetailFile;
	@NonNull
	private String formatDetailFolder;
	@NonNull
	private String stringHome;
	@NonNull
	private String stringFolder;

	UpdateListOfFilesAsyncTask(
			@NonNull UpdateListOfFilesListener listener,
			@NonNull String formatDetailFile,
			@NonNull String formatDetailFolder,
			@NonNull String stringHome,
			@NonNull String stringFolder) {
		this.listener = listener;
		this.formatDetailFile = formatDetailFile;
		this.formatDetailFolder = formatDetailFolder;
		this.stringHome = stringHome;
		this.stringFolder = stringFolder;
		currentDirectory = "";
	}

	private static boolean isSymbolicLink(@NonNull File file) throws IOException {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			return java.nio.file.Files.isSymbolicLink(file.toPath());
		}
		File canon;
		if (file.getParent() == null) {
			canon = file;
		} else {
			File canonDir = file.getParentFile().getCanonicalFile();
			canon = new File(canonDir, file.getName());
		}
		return !canon.getCanonicalFile().equals(canon.getAbsoluteFile());
	}

	@Override
	protected void onPreExecute() {
		super.onPreExecute();
	}

	private void AddItemFileDetail(
			@NonNull List<FileInfoAdapter.FileDetail> list,
			@NonNull Uri uri,
			@Nullable Uri canonicalUri,
			@NonNull String fileName,
			@NonNull String description,
			boolean isFolder,
			boolean isRootRequired) {
		list.add(new FileInfoAdapter.FileDetail(
				uri,
				fileName,
				description,
				true,
				isFolder,
				isRootRequired));
		if (canonicalUri != null) {
			list.add(new FileInfoAdapter.FileDetail(
					canonicalUri, uri,
					fileName,
					description,
					true,
					isFolder,
					isRootRequired));
		}
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

			currentDirectory = tempFolder.getAbsolutePath();

			final DateFormat format = DateFormat.getDateInstance();
			final ArrayList<FileInfoAdapter.FileDetail> fileDetails = new ArrayList<>();
			final ArrayList<FileInfoAdapter.FileDetail> folderDetails = new ArrayList<>();

			if (currentDirectory.equals("/")) {
				folderDetails.add(new FileInfoAdapter.FileDetail(Uri.EMPTY, stringHome, stringFolder, true, true));
			} else {
				folderDetails.add(new FileInfoAdapter.FileDetail(Uri.EMPTY, "..", stringFolder, true, true));
			}

			if (!tempFolder.canRead()) {
				if (RootFW.connect()) {
					com.spazedog.lib.rootfw4.utils.File folder = RootFW.getFile(currentDirectory);
					Log.v(TAG, "Superuser permissions are obtained");
					String[] files = folder.getList();
					for (String fileName : files) {
						String filePath = FilenameUtils.concat(currentDirectory, fileName);
						com.spazedog.lib.rootfw4.utils.File file = RootFW.getFile(filePath);
						File f = new File(filePath);
						Uri uri = Uri.parse(f.toURI().toString());
						Uri uriCanon = null;
						if (isSymbolicLink(f)) {
							Log.v(TAG, "Symlink " + f.getAbsolutePath() + " -> " + f.getCanonicalPath());
							uriCanon = Uri.parse(new File(f.getCanonicalPath()).toString());
						}
						boolean isDirectory = file.isDirectory();
						if (!isDirectory && !file.isFile()) {
							continue;
						}
						List<FileInfoAdapter.FileDetail> list;
						String description;
						if (isDirectory) {
							list = folderDetails;
							description = String.format(formatDetailFolder,
									format.format(f.lastModified()));
						} else {
							list = fileDetails;
							description = String.format(formatDetailFile,
									org.apache.commons.io.FileUtils.byteCountToDisplaySize(file.size()),
									format.format(f.lastModified()));
						}
						AddItemFileDetail(
								list,
								uri, uriCanon,
								fileName,
								description,
								isDirectory,
								true);
					}
				}
			} else {
				File[] files = tempFolder.listFiles();
				Arrays.sort(files, getFileNameComparator());

				for (final File f : files) {
					Uri uri = Uri.parse(f.toURI().toString());
					Uri uriCanon = null;
					if (isSymbolicLink(f)) {
						Log.v(TAG, "Symlink " + f.getAbsolutePath() + " -> " + f.getCanonicalPath());
						uriCanon = Uri.parse(new File(f.getCanonicalPath()).toString());
					}
					boolean isDirectory = f.isDirectory();
					if (!isDirectory && !f.isFile()) {
						continue;
					}
					List<FileInfoAdapter.FileDetail> list;
					String fileName = f.getName();
					String description;
					if (isDirectory) {
						list = folderDetails;
						description = String.format(
								formatDetailFolder,
								format.format(f.lastModified()));
					} else {
						list = fileDetails;
						description = String.format(
								formatDetailFile,
								org.apache.commons.io.FileUtils.byteCountToDisplaySize(f.length()),
								format.format(f.lastModified()));
					}
					AddItemFileDetail(
							list,
							uri, uriCanon,
							fileName,
							description,
							isDirectory,
							false
					);
				}
			}

			// Build final list with folders at top
			folderDetails.addAll(fileDetails);
			return folderDetails;
		} catch (Exception e) {
			String message = e.getMessage();
			if (message == null) {
				exceptionMessage = e.toString();
			} else {
				exceptionMessage = message;
			}
			Log.e(TAG, exceptionMessage);
			return null;
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void onPostExecute(final List<FileInfoAdapter.FileDetail> names) {
		super.onPostExecute(names);
		if (listener == null) return;
		if (names == null) {
			listener.onCantOpen(currentDirectory, exceptionMessage);
		} else {
			listener.onUpdateList(currentDirectory, names);
		}
	}

	@SuppressWarnings("unchecked")
	@NonNull
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

	interface UpdateListOfFilesListener {
		void onUpdateList(
				@NonNull String currentDirectory,
				@NonNull final List<FileInfoAdapter.FileDetail> names);

		void onCantOpen(@NonNull String directoryPath, @Nullable String exceptionMessage);
	}
}
