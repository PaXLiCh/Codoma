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
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;
import com.spazedog.lib.rootfw4.RootFW;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

public class SelectFileActivity extends AppCompatActivity implements
		SearchView.OnQueryTextListener,
		FileInfoAdapter.OnItemClickListener,
		EditTextDialog.EditDialogListener {

	public static final String EXTRA_ACTION = "action";
	public static final String EXTRA_PATH = "path";

	private static final String ROOT_DIR = "/";
	private static final String PATH_SEPARATOR = "/";
	private FileInfoAdapter adapter;
	private String currentFolder;
	private MenuItem mSearchViewMenuItem;
	private SearchView searchView;
	private HorizontalScrollView scrollBreadcrumbs;
	private LinearLayout viewBreadcrumbs;
	private Actions action;
	private View textViewEmpty;
	private RecyclerView listView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setTheme(this);

		super.onCreate(savedInstanceState);

		currentFolder = PreferenceHelper.defaultFolder(this);

		setContentView(R.layout.activity_select_file);

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(toolbar);

		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Bundle bundle = getIntent().getExtras();
		action = (Actions) bundle.getSerializable(EXTRA_ACTION);

		adapter = new FileInfoAdapter(this, this);
		listView = (RecyclerView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		textViewEmpty = findViewById(R.id.empty_text);

		scrollBreadcrumbs = (HorizontalScrollView) findViewById(R.id.scroll_breadcrumbs);
		viewBreadcrumbs = (LinearLayout) scrollBreadcrumbs.findViewById(R.id.view_breadcrumbs);

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
		viewBreadcrumbs.removeAllViews();

		// Adding buttons

		String[] parts = currentFolder.split(PATH_SEPARATOR);
		String dir = "";

		for (String part : parts) {
			dir += PATH_SEPARATOR + part;
			View buttonView = View.inflate(this, R.layout.breadcrumbs, null);
			Button b = (Button) buttonView;
			b.setText(getString(R.string.breadcrumbs_format, part.isEmpty() ? PATH_SEPARATOR : part));
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

		scrollBreadcrumbs.post(new Runnable() {
			@Override
			public void run() {
				scrollBreadcrumbs.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
			}
		});
	}

	public enum Actions {
		SelectFile, SelectFolder, SaveFile
	}

	private class UpdateList extends AsyncTask<String, Void, ArrayList<FileInfoAdapter.FileDetail>> {
		private String exceptionMessage;

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

				String[] unopenableExtensions = { "apk", "mp3", "mp4", "png", "jpg", "jpeg" };

				final DateFormat format = DateFormat.getDateInstance();
				final ArrayList<FileInfoAdapter.FileDetail> fileDetails = new ArrayList<>();
				final ArrayList<FileInfoAdapter.FileDetail> folderDetails = new ArrayList<>();
				if (currentFolder.equals("/")) {
					folderDetails.add(new FileInfoAdapter.FileDetail(null, getString(R.string.home), getString(R.string.folder), true, true));
				} else {
					folderDetails.add(new FileInfoAdapter.FileDetail(null, "..", getString(R.string.folder), true, true));
				}

				if (!tempFolder.canRead()) {
					if (RootFW.connect()) {
						com.spazedog.lib.rootfw4.utils.File folder = RootFW.getFile(currentFolder);
						com.spazedog.lib.rootfw4.utils.File.FileStat[] stats = folder.getDetailedList();

						if (stats != null) {
							for (com.spazedog.lib.rootfw4.utils.File.FileStat stat : stats) {
								if (stat.type().equals("d")) {
									folderDetails.add(new FileInfoAdapter.FileDetail(null, stat.name(),
											getString(R.string.folder),
											true, true));
								} else if (!FilenameUtils.isExtension(stat.name().toLowerCase(), unopenableExtensions)
										&& stat.size() <= CodomaApplication.MAX_FILE_SIZE * FileUtils.ONE_KB) {
									final long fileSize = stat.size();
									String date = format.format(stat);
									String description = getString(
											R.string.file_info_detail,
											FileUtils.byteCountToDisplaySize(fileSize),
											date);
									fileDetails.add(new FileInfoAdapter.FileDetail(
											null,
											stat.name(),
											description,
											true, false));
								}
							}
						}
					}
					exceptionMessage = "Cant read folder " + tempFolder.getAbsolutePath();
					return null;
				} else {
					currentFolder = tempFolder.getAbsolutePath();

					File[] files = tempFolder.listFiles();

					Arrays.sort(files, getFileNameComparator());

					for (final File f : files) {
						Uri uri = Uri.parse(f.toURI().toString());
						if (f.isDirectory()) {
							folderDetails.add(new FileInfoAdapter.FileDetail(uri, f.getName(),
									getString(R.string.folder),
									true, true));
						} else if (f.isFile()
								&& !FilenameUtils.isExtension(f.getName().toLowerCase(), unopenableExtensions)
								&& FileUtils.sizeOf(f) <= CodomaApplication.MAX_FILE_SIZE * FileUtils.ONE_KB) {
							String description = getString(
									R.string.file_info_detail,
									FileUtils.byteCountToDisplaySize(f.length()),
									format.format(f.lastModified()));
							fileDetails.add(new FileInfoAdapter.FileDetail(
									uri, f.getName(), description, true, false));
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
		protected void onPostExecute(final ArrayList<FileInfoAdapter.FileDetail> names) {
			if (names != null) {
				adapter.setFiles(names);
				listView.scrollToPosition(0);
			}
			textViewEmpty.setVisibility(names == null || names.size() < 2 ? View.VISIBLE : View.GONE);
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
