package ru.kolotnev.codoma;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;

public class SelectFileActivity extends AppCompatActivity implements
		SearchView.OnQueryTextListener,
		FileInfoAdapter.OnItemClickListener,
		EditTextDialog.EditDialogListener {

	public static final String EXTRA_ACTION = "action";
	public static final String EXTRA_PATH = "path";

	private static final String ROOT_DIR = "/";
	private static final String PATH_SEPARATOR = "/";
	private final FileInfoAdapter adapter = new FileInfoAdapter();
	private String sdCardPath = "";
	private String currentFolder;
	private MenuItem mSearchViewMenuItem;
	private SearchView searchView;
	private LinearLayout viewBreadcrumbs;
	private Actions action;
	private View textViewEmpty;
	private RecyclerView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		currentFolder = PreferenceHelper.defaultFolder(this);

		getSdCardPath();

		setContentView(R.layout.activity_select_file);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Bundle bundle = getIntent().getExtras();
		action = (Actions) bundle.getSerializable(EXTRA_ACTION);

		adapter.setOnItemClickListener(this);
		listView = (RecyclerView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		textViewEmpty = findViewById(R.id.empty_text);

		viewBreadcrumbs = (LinearLayout) findViewById(R.id.view_breadcrumbs);

		FloatingActionButton mFab = (FloatingActionButton) findViewById(R.id.menu_item_create_file);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditTextDialog.newInstance(EditTextDialog.Actions.NEW_FILE).show(getSupportFragmentManager(), "dialog");
			}
		});

		mFab = (FloatingActionButton) findViewById(R.id.menu_item_create_folder);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditTextDialog.newInstance(EditTextDialog.Actions.NEW_FOLDER).show(getSupportFragmentManager(), "dialog");
			}
		});

		String lastNavigatedPath = bundle.getString(EXTRA_PATH,
				PreferenceHelper.getWorkingFolder(this));

		File file = new File(lastNavigatedPath);

		if (!file.exists()) {
			PreferenceHelper.setWorkingFolder(this, PreferenceHelper.defaultFolder(this));
			file = new File(PreferenceHelper.defaultFolder(this));
		}

		new UpdateList().execute(file.getAbsolutePath());
	}

	@Override
	public void onBackPressed() {
		upOneLevel();
	}

	@Override
	public boolean onQueryTextChange(String newText) {
		if (adapter == null)
			return true;

		adapter.filter(newText);
		return true;
	}

	@Override
	public boolean onQueryTextSubmit(String query) {
		return false;
	}

	@Override
	public void onItemClick(FileInfoAdapter.FileDetail fileDetail) {
		final String name = fileDetail.getName();
		if (name.equals("..")) {
			if (currentFolder.equals(ROOT_DIR)) {
				new UpdateList().execute(PreferenceHelper.getWorkingFolder(this));
			} else {
				File tempFile = new File(currentFolder);
				if (tempFile.isFile()) {
					tempFile = tempFile.getParentFile().getParentFile();
				} else {
					tempFile = tempFile.getParentFile();
				}
				new UpdateList().execute(tempFile.getAbsolutePath());
			}
			return;
		} else if (name.equals(getString(R.string.home))) {
			new UpdateList().execute(PreferenceHelper.getWorkingFolder(this));
			return;
		}

		final File selectedFile = new File(currentFolder, name);

		if (selectedFile.isFile() && action == Actions.SelectFile) {
			finishWithResult(selectedFile);
		} else if (selectedFile.isDirectory()) {
			new UpdateList().execute(selectedFile.getAbsolutePath());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_select_file, menu);
		mSearchViewMenuItem = menu.findItem(R.id.im_search);
		searchView = (SearchView) MenuItemCompat.getActionView(mSearchViewMenuItem);
		searchView.setIconifiedByDefault(true);
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(false);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// menu items
		MenuItem imSetAsWorkingFolder = menu.findItem(R.id.im_set_as_working_folder);
		MenuItem imIsWorkingFolder = menu.findItem(R.id.im_is_working_folder);
		MenuItem imSelectFolder = menu.findItem(R.id.im_select_folder);
		boolean isWorkingFolder = currentFolder.equals(PreferenceHelper.getWorkingFolder(SelectFileActivity.this));
		if (imSetAsWorkingFolder != null) {
			// set the imSetAsWorkingFolder visible only if the two folder don't concide
			imSetAsWorkingFolder.setVisible(!isWorkingFolder);
		}
		if (imIsWorkingFolder != null) {
			// set visible is the other is invisible
			imIsWorkingFolder.setVisible(isWorkingFolder);
		}
		if (imSelectFolder != null) {
			imSelectFolder.setVisible(action == Actions.SelectFolder);
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int i = item.getItemId();
		if (i == android.R.id.home) {
			finish();
			return true;
		} else if (i == R.id.im_set_as_working_folder) {
			PreferenceHelper.setWorkingFolder(SelectFileActivity.this, currentFolder);
			invalidateOptionsMenu();
			return true;
		} else if (i == R.id.im_is_working_folder) {
			Toast.makeText(getBaseContext(), R.string.is_the_working_folder, Toast.LENGTH_SHORT).show();
			return true;
		} else if (i == R.id.im_select_folder) {
			finishWithResult(new File(currentFolder));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	@Override
	public void onEditTextDialogEnded(final String inputText, final String hint, final EditTextDialog.Actions actions) {
		if (inputText.length() == 0) {
			Toast.makeText(this, R.string.dialog_error_no_file_name, Toast.LENGTH_LONG).show();
			return;
		}

		if (actions == EditTextDialog.Actions.NEW_FILE && !TextUtils.isEmpty(inputText)) {
			File file = new File(currentFolder, inputText);
			if (file.isDirectory()) {
				Toast.makeText(this, R.string.dialog_error_not_a_file, Toast.LENGTH_LONG).show();
				return;
			}
			try {
				if (file.createNewFile()) {
					finishWithResult(file);
				} else {
					// No easy way to check filename for illegal characters like |?<>*+ because
					// this is dependent on the file system type - FAT32, ext3, NTFS...
					// The main application will catch such errors when it tries
					// to create a file with the illegal name.
					boolean exists = file.exists();
					File parent = file.getParentFile();
					if (!exists && !parent.canWrite()) {
						// misc errors
						Toast.makeText(this, R.string.dialog_error_file_write_denied, Toast.LENGTH_LONG).show();
					}
				}
			} catch (IOException e) {
				Toast.makeText(SelectFileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
			}
		} else if (actions == EditTextDialog.Actions.NEW_FOLDER && !TextUtils.isEmpty(inputText)) {
			File file = new File(currentFolder, inputText);
			file.mkdirs();
			new UpdateList().execute(currentFolder);
		}
	}

	/**
	 * Finish this Activity with a result code and URI of the selected file.
	 *
	 * @param file
	 * 		The file selected.
	 */
	private void finishWithResult(File file) {
		if (file != null) {
			Uri uri = Uri.fromFile(file);
			setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			setResult(RESULT_CANCELED);
		}
		finish();
	}

	private void upOneLevel() {
		if (currentFolder.isEmpty() || currentFolder.equals(ROOT_DIR)) {
			finish();
		} else {
			File file = new File(currentFolder);
			String parentFolder = file.getParent();
			new UpdateList().execute(parentFolder);
		}
	}

	private void setDirectoryButtons() {
		String[] parts = currentFolder.split(PATH_SEPARATOR);

		viewBreadcrumbs.removeAllViews();

		int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;
		int MATCH_PARENT = LinearLayout.LayoutParams.MATCH_PARENT;

		// Add home button separately
		ImageButton ib = new ImageButton(this);
		ib.setImageResource(R.drawable.ic_device_tablet);
		ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
		ib.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				new UpdateList().execute(ROOT_DIR);
			}
		});
		viewBreadcrumbs.addView(ib);

		// Add other buttons

		String dir = "";

		for (int i = 1; i < parts.length; ++i) {
			dir += PATH_SEPARATOR + parts[i];
			if (dir.equalsIgnoreCase(sdCardPath)) {
				// Add SD card button
				ib = new ImageButton(this);
				ib.setImageResource(R.drawable.ic_device_sd_storage);
				ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
				ib.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						new UpdateList().execute(sdCardPath);
					}
				});
				viewBreadcrumbs.addView(ib);
			} else {
				Button b = new Button(this);
				b.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, MATCH_PARENT));
				b.setText(parts[i]);
				b.setTag(dir);
				b.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View view) {
						String dir = (String) view.getTag();
						new UpdateList().execute(dir);
					}
				});
				viewBreadcrumbs.addView(b);
			}
		}
	}

	private void getSdCardPath() {
		sdCardPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
		//Log.e("Codoma", "sdcard path = " + sdCardPath);
	}

	public enum Actions {
		SelectFile, SelectFolder, SaveFile
	}

	private class UpdateList extends AsyncTask<String, Void, LinkedList<FileInfoAdapter.FileDetail>> {
		String exceptionMessage;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (searchView != null) {
				searchView.setIconified(true);
				MenuItemCompat.collapseActionView(mSearchViewMenuItem);
				searchView.setQuery("", false);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected LinkedList<FileInfoAdapter.FileDetail> doInBackground(final String... params) {
			try {

				final String path = params[0];
				if (TextUtils.isEmpty(path)) {
					return null;
				}

				File tempFolder = new File(path);
				if (tempFolder.isFile()) {
					tempFolder = tempFolder.getParentFile();
				}

				String[] unopenableExtensions = { "apk", "mp3", "mp4", "png", "jpg", "jpeg" };

				final LinkedList<FileInfoAdapter.FileDetail> fileDetails = new LinkedList<>();
				final LinkedList<FileInfoAdapter.FileDetail> folderDetails = new LinkedList<>();

				if (!tempFolder.canRead()) {
					/*if (RootFW.connect()) {
						com.spazedog.lib.rootfw4.utils.File folder = RootFW.getFile(currentFolder);
						com.spazedog.lib.rootfw4.utils.File.FileStat[] stats = folder.getDetailedList();

						if (stats != null) {
							for (com.spazedog.lib.rootfw4.utils.File.FileStat stat : stats) {
								if (stat.type().equals("d")) {
									folderDetails.add(new FileInfoAdapter.FileDetail(stat.name(),
											getString(R.string.folder),
											""));
								} else if (!FilenameUtils.isExtension(stat.name().toLowerCase(), unopenableExtensions)
										&& stat.size() <= Build.MAX_FILE_SIZE * org.apache.commons.io.FileUtils.ONE_KB) {
									final long fileSize = stat.size();
									//SimpleDateFormat format = new SimpleDateFormat("MMM dd, yyyy  hh:mm a");
									//String date = format.format("");
									fileDetails.add(new FileInfoAdapter.FileDetail(stat.name(),
											org.apache.commons.io.FileUtils.byteCountToDisplaySize(fileSize), ""));
								}
							}
						}
					}*/
					exceptionMessage = "Cant read folder " + tempFolder.getAbsolutePath();
					return null;
				} else {
					currentFolder = tempFolder.getAbsolutePath();

					File[] files = tempFolder.listFiles();

					Arrays.sort(files, getFileNameComparator());

					for (final File f : files) {
						if (f.isDirectory()) {
							folderDetails.add(new FileInfoAdapter.FileDetail(f.getName(),
									getString(R.string.folder),
									""));
						} else if (f.isFile()
								&& !FilenameUtils.isExtension(f.getName().toLowerCase(), unopenableExtensions)
								&& org.apache.commons.io.FileUtils.sizeOf(f) <= Build.MAX_FILE_SIZE * org.apache.commons.io.FileUtils.ONE_KB) {
							final long fileSize = f.length();
							DateFormat format = DateFormat.getDateTimeInstance();
							String date = format.format(f.lastModified());
							fileDetails.add(new FileInfoAdapter.FileDetail(f.getName(),
									org.apache.commons.io.FileUtils.byteCountToDisplaySize(fileSize), date));
						}
					}
				}

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
		protected void onPostExecute(final LinkedList<FileInfoAdapter.FileDetail> names) {
			if (names != null) {
				adapter.setFiles(getBaseContext(), names, currentFolder.equals("/"));
				listView.scrollToPosition(0);
			}
			textViewEmpty.setVisibility(names == null || names.size() < 1 ? View.VISIBLE : View.GONE);
			if (exceptionMessage != null) {
				Toast.makeText(SelectFileActivity.this, exceptionMessage, Toast.LENGTH_SHORT).show();
			}
			setDirectoryButtons();
			invalidateOptionsMenu();
			super.onPostExecute(names);
		}

		@SuppressWarnings("unchecked")
		public final Comparator<File> getFileNameComparator() {
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
	}
}
