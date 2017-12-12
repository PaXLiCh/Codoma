package ru.kolotnev.codoma;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
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

import java.io.File;
import java.io.IOException;
import java.util.List;

public class SelectFileActivity extends AppCompatActivity implements
		UpdateListOfFilesAsyncTask.UpdateListOfFilesListener,
		SearchView.OnQueryTextListener,
		FileInfoAdapter.OnItemClickListener,
		EditTextDialog.EditDialogListener {

	public static final String EXTRA_ACTION = "action";
	public static final String EXTRA_PATH = "path";

	private static final String ROOT_DIR = "/";
	private static final String PATH_SEPARATOR = "/";
	private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
	private FileInfoAdapter adapter;
	private String currentFolder;
	private MenuItem mSearchViewMenuItem;
	private SearchView searchView;
	private HorizontalScrollView scrollBreadcrumbs;
	private LinearLayout viewBreadcrumbs;
	private Actions action;
	private View textViewEmpty;
	private View progressBar;
	private RecyclerView listView;
	@Nullable
	private UpdateListOfFilesAsyncTask task;

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
		String lastNavigatedPath;
		if (bundle != null) {
			action = (Actions) bundle.getSerializable(EXTRA_ACTION);
			lastNavigatedPath = bundle.getString(EXTRA_PATH,
					PreferenceHelper.getWorkingFolder(this));
		} else {
			action = Actions.SelectFile;
			lastNavigatedPath = PreferenceHelper.getWorkingFolder(this);
		}

		adapter = new FileInfoAdapter(this, this);
		listView = (RecyclerView) findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		textViewEmpty = findViewById(R.id.empty_text);
		progressBar = findViewById(android.R.id.progress);

		scrollBreadcrumbs = (HorizontalScrollView) findViewById(R.id.scroll_breadcrumbs);
		viewBreadcrumbs = scrollBreadcrumbs.findViewById(R.id.view_breadcrumbs);

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

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkPermissionReadStorage();
		}

		File file = new File(lastNavigatedPath);

		if (!file.exists()) {
			PreferenceHelper.setWorkingFolder(this, PreferenceHelper.defaultFolder(this));
			file = new File(PreferenceHelper.defaultFolder(this));
		}

		updateList(file.getAbsolutePath());
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	public void checkPermissionReadStorage() {
		if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(this,
					Manifest.permission.READ_EXTERNAL_STORAGE)) {
				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(this,
						new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
						MY_PERMISSIONS_REQUEST_READ_STORAGE);

				// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
				// app-defined int constant. The callback method gets the
				// result of the request.
			}
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
		switch (requestCode) {
			case MY_PERMISSIONS_REQUEST_READ_STORAGE: {
				//permission to read storage
				if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

					// permission was granted, yay! Do the
					// contacts-related task you need to do.
				} else {
					// permission denied, boo! Disable the
					// functionality that depends on this permission.
					Toast.makeText(this, "We Need permission Storage", Toast.LENGTH_SHORT).show();
				}
				return;
			}

			// other 'case' lines to check for other
			// permissions this app might request
		}
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
				updateList(PreferenceHelper.getWorkingFolder(this));
			} else {
				File tempFile = new File(currentFolder);
				if (tempFile.isFile()) {
					tempFile = tempFile.getParentFile().getParentFile();
				} else {
					tempFile = tempFile.getParentFile();
				}
				updateList(tempFile.getAbsolutePath());
			}
			return;
		} else if (name.equals(getString(R.string.home))) {
			updateList(PreferenceHelper.getWorkingFolder(this));
			return;
		}

		final File selectedFile = new File(currentFolder, name);

		if (selectedFile.isFile() && action == Actions.SelectFile) {
			finishWithResult(selectedFile);
		} else if (selectedFile.isDirectory()) {
			updateList(selectedFile.getAbsolutePath());
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_select_file, menu);
		mSearchViewMenuItem = menu.findItem(R.id.im_search);
		searchView = (SearchView) mSearchViewMenuItem.getActionView();
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
			updateList(currentFolder);
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
			updateList(parentFolder);
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
			b.setText(getString(R.string.activity_select_file_breadcrumbs_format, part.isEmpty() ? PATH_SEPARATOR : part));
			b.setTag(dir);
			b.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View view) {
					String dir = (String) view.getTag();
					updateList(dir);
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

	@Override
	public void onUpdateList(@NonNull String currentDirectory, @NonNull List<FileInfoAdapter.FileDetail> names) {
		adapter.setFiles(names);
		listView.scrollToPosition(0);
		textViewEmpty.setVisibility(names.size() < 2 ? View.VISIBLE : View.GONE);
		progressBar.setVisibility(View.GONE);
		currentFolder = currentDirectory;
		setDirectoryButtons();
		invalidateOptionsMenu();
		task = null;
	}

	@Override
	public void onCantOpen(@NonNull String fileName, @Nullable String exceptionMessage) {
		String message = getString(R.string.activity_select_file_error_cant_read_folder, fileName);
		if (exceptionMessage != null) {
			message = message + "\n" + exceptionMessage;
		}
		Toast.makeText(SelectFileActivity.this, message, Toast.LENGTH_SHORT).show();
		progressBar.setVisibility(View.GONE);
		task = null;
	}

	enum Actions {
		SelectFile, SelectFolder, SaveFile
	}

	private void updateList(String path) {
		if (searchView != null) {
			searchView.setIconified(true);
			mSearchViewMenuItem.collapseActionView();
			searchView.setQuery("", false);
		}

		textViewEmpty.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);

		String[] unopenableExtensions = { "apk", "mp3", "mp4", "png", "jpg", "jpeg" };

		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
		task = new UpdateListOfFilesAsyncTask(
				this,
				unopenableExtensions,
				getString(R.string.activity_select_file_file_detail),
				getString(R.string.activity_select_file_folder_detail),
				getString(R.string.home),
				getString(R.string.folder));
		task.execute(path);
	}
}
