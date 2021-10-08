package ru.kolotnev.codoma;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.github.clans.fab.FloatingActionButton;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Fragment with list of files.
 */
public class SelectFileFragment extends Fragment implements
		OnBackPressedListener,
		UpdateListOfFilesAsyncTask.UpdateListOfFilesListener,
		SearchView.OnQueryTextListener,
		FileInfoAdapter.OnItemClickListener,
		EditTextDialog.EditDialogListener {

	public static final String EXTRA_ACTION = "action";
	public static final String EXTRA_PATH = "path";
	private static final String TAG = "Select file";
	private static final String ROOT_DIR = "/";
	private static final String PATH_SEPARATOR = "/";
	private static final int MY_PERMISSIONS_REQUEST_READ_STORAGE = 0;
	private static final int REQUEST_CODE_NAME_FILE = 21;
	private static final int REQUEST_CODE_NAME_FOLDER = 22;
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
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		setRetainInstance(true);
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);

		((OnBackPressedSubscriber) getActivity()).setOnBackPressedListener(this);
	}

	@Nullable
	@Override
	public View onCreateView(
			@NonNull LayoutInflater inflater,
			@Nullable ViewGroup container,
			@Nullable Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.activity_select_file, container, false);

		Toolbar toolbar = view.findViewById(R.id.toolbar);
		((AppCompatActivity) getActivity()).setSupportActionBar(toolbar);

		ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		adapter = new FileInfoAdapter(getContext(), this);
		listView = view.findViewById(android.R.id.list);
		listView.setAdapter(adapter);
		textViewEmpty = view.findViewById(R.id.empty_text);
		progressBar = view.findViewById(android.R.id.progress);

		scrollBreadcrumbs = view.findViewById(R.id.scroll_breadcrumbs);
		viewBreadcrumbs = scrollBreadcrumbs.findViewById(R.id.view_breadcrumbs);

		FloatingActionButton mFab = view.findViewById(R.id.menu_item_create_file);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditTextDialog dialog = EditTextDialog.newInstance(EditTextDialog.Actions.NEW_FILE);
				dialog.setTargetFragment(SelectFileFragment.this, REQUEST_CODE_NAME_FILE);
				dialog.show(getFragmentManager(), "dialog");
			}
		});

		mFab = view.findViewById(R.id.menu_item_create_folder);
		mFab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				EditTextDialog dialog = EditTextDialog.newInstance(EditTextDialog.Actions.NEW_FOLDER);
				dialog.setTargetFragment(SelectFileFragment.this, REQUEST_CODE_NAME_FOLDER);
				dialog.show(getFragmentManager(), "dialog");
			}
		});

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			checkPermissionReadStorage();
		}

		if (savedInstanceState == null) {
			// The file selection is started
			Bundle bundle = getArguments();
			String lastNavigatedPath;
			if (bundle != null) {
				action = (Actions) bundle.getSerializable(EXTRA_ACTION);
				lastNavigatedPath = bundle.getString(EXTRA_PATH,
						PreferenceHelper.getWorkingFolder(getContext()));
			} else {
				action = Actions.SelectFile;
				lastNavigatedPath = PreferenceHelper.getWorkingFolder(getContext());
			}

			// Check provided path, fallback to default path
			File file = new File(lastNavigatedPath);
			if (!file.exists()) {
				String defaultFolder = PreferenceHelper.defaultFolder(getContext());
				PreferenceHelper.setWorkingFolder(getContext(), defaultFolder);
				file = new File(defaultFolder);
			}

			currentFolder = file.getAbsolutePath();

			updateList(currentFolder);
			Log.e(TAG, "start with " + currentFolder);
		} else {
			// View was recreated, rebuild list of files
			if (task == null) {
				updateList(currentFolder);
				Log.e(TAG, "restart with " + currentFolder);
			} else {
				Log.e(TAG, "restart with " + currentFolder + " waiting for content");
				textViewEmpty.setVisibility(View.GONE);
				progressBar.setVisibility(View.VISIBLE);
			}
		}

		setDirectoryButtons();

		return view;
	}

	@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
	public void checkPermissionReadStorage() {
		if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_EXTERNAL_STORAGE)
				!= PackageManager.PERMISSION_GRANTED) {

			// Should we show an explanation?
			if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
					Manifest.permission.READ_EXTERNAL_STORAGE)) {
				// Show an explanation to the user *asynchronously* -- don't block
				// this thread waiting for the user's response! After the user
				// sees the explanation, try again to request the permission.
			} else {
				// No explanation needed, we can request the permission.
				ActivityCompat.requestPermissions(getActivity(),
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
					Toast.makeText(this.getActivity(), "We Need permission Storage", Toast.LENGTH_SHORT).show();
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
	public void onItemClick(@NonNull FileInfoAdapter.FileDetail fileDetail) {
		Log.e(TAG, "Click on item " + fileDetail.getName());
		final String name = fileDetail.getName();
		if (name.equals("..")) {
			if (currentFolder.equals(ROOT_DIR)) {
				updateList(PreferenceHelper.getWorkingFolder(this.getContext()));
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
			updateList(PreferenceHelper.getWorkingFolder(getContext()));
			return;
		}

		String path = fileDetail.getUri().getPath();
		final File selectedFile = new File(path);
		Log.e(TAG, "Click on smsth " + path + " " + selectedFile.getAbsoluteFile().exists());
		if (fileDetail.isFolder()) {
			Log.e(TAG, "Click on dir " + path + " root? " + fileDetail.isRootRequired());
			if (action == Actions.SelectFolder) {
				finishWithResult(selectedFile);
			} else {
				updateList(path);
			}
		} else if (action == Actions.SelectFile) {
			Log.e(TAG, "Click on file " + path + " root? " + fileDetail.isRootRequired());
			finishWithResult(selectedFile);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		// Inflate the menu; this adds items to the action bar if it is present.
		inflater.inflate(R.menu.menu_select_file, menu);
		mSearchViewMenuItem = menu.findItem(R.id.im_search);
		searchView = (SearchView) mSearchViewMenuItem.getActionView();
		searchView.setIconifiedByDefault(true);
		searchView.setOnQueryTextListener(this);
		searchView.setSubmitButtonEnabled(false);
		super.onCreateOptionsMenu(menu, inflater);
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		// menu items
		MenuItem imSetAsWorkingFolder = menu.findItem(R.id.im_set_as_working_folder);
		MenuItem imIsWorkingFolder = menu.findItem(R.id.im_is_working_folder);
		MenuItem imSelectFolder = menu.findItem(R.id.im_select_folder);
		boolean isWorkingFolder = currentFolder.equals(PreferenceHelper.getWorkingFolder(getContext()));
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
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int i = item.getItemId();
		if (i == android.R.id.home) {
			getActivity().finish();
			return true;
		} else if (i == R.id.im_set_as_working_folder) {
			PreferenceHelper.setWorkingFolder(getContext(), currentFolder);
			getActivity().invalidateOptionsMenu();
			return true;
		} else if (i == R.id.im_is_working_folder) {
			Toast.makeText(getContext(), R.string.is_the_working_folder, Toast.LENGTH_SHORT).show();
			return true;
		} else if (i == R.id.im_select_folder) {
			finishWithResult(new File(currentFolder));
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onEditTextDialogEnded(final String inputText, final String hint, final EditTextDialog.Actions actions) {
		if (TextUtils.isEmpty(inputText)) {
			Toast.makeText(getContext(), R.string.dialog_error_no_file_name, Toast.LENGTH_LONG).show();
			return;
		}

		if (actions == EditTextDialog.Actions.NEW_FILE) {
			File file = new File(currentFolder, inputText);
			if (file.exists()) {
				if (file.isDirectory()) {
					Toast.makeText(getContext(), R.string.dialog_error_not_a_file, Toast.LENGTH_LONG).show();
				} else if (file.isDirectory()) {
					Toast.makeText(getContext(), "Specified name at current path is a folder\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
				}
			} else {
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
							Toast.makeText(getContext(), R.string.dialog_error_file_write_denied, Toast.LENGTH_LONG).show();
						}
					}
				} catch (IOException e) {
					Toast.makeText(getContext(), "Unable to create file with path\n" + file.getAbsolutePath() + "\n" + e.getMessage(), Toast.LENGTH_SHORT).show();
				}
			}
		} else if (actions == EditTextDialog.Actions.NEW_FOLDER) {
			File file = new File(currentFolder, inputText);
			if (file.exists()) {
				if (file.isFile()) {
					Toast.makeText(getContext(), "Specified name at current path is a file\n" + file.getAbsolutePath() + "\nSelect another name.", Toast.LENGTH_LONG).show();
				} else if (file.isDirectory()) {
					Toast.makeText(getContext(), "Specified folder is already exists at path\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
				}
			} else {
				try {
					if (file.mkdirs()) {
						currentFolder = file.getAbsolutePath();
					} else {
						Toast.makeText(getContext(), "Unable to create folder with path\n" + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
					}
				} catch (SecurityException e) {
					Toast.makeText(getContext(), "Unable to create folder with path\n" + file.getAbsolutePath() + "\n" + e.getMessage(), Toast.LENGTH_LONG).show();
				}
			}
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
			getActivity().setResult(RESULT_OK, new Intent().setData(uri));
		} else {
			getActivity().setResult(RESULT_CANCELED);
		}
		getActivity().finish();
	}

	private void upOneLevel() {
		if (currentFolder.isEmpty() || currentFolder.equals(ROOT_DIR)) {
			getActivity().finish();
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
			View buttonView = View.inflate(getContext(), R.layout.breadcrumbs, null);
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
		getActivity().invalidateOptionsMenu();
		task = null;
	}

	@Override
	public void onCantOpen(@NonNull String fileName, @Nullable String exceptionMessage) {
		String message = getString(R.string.activity_select_file_error_cant_read_folder, fileName);
		if (exceptionMessage != null) {
			message = message + "\n" + exceptionMessage;
		}
		Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
		progressBar.setVisibility(View.GONE);
		task = null;
	}

	private void updateList(String path) {
		if (searchView != null) {
			searchView.setIconified(true);
			mSearchViewMenuItem.collapseActionView();
			searchView.setQuery("", false);
		}

		textViewEmpty.setVisibility(View.GONE);
		progressBar.setVisibility(View.VISIBLE);

		//String[] unopenableExtensions = { "apk", "mp3", "mp4", "png", "jpg", "jpeg" };
		//!FilenameUtils.isExtension(f.getName().toLowerCase(), unopenableExtensions)
		//		f.length() <= CodomaApplication.MAX_FILE_SIZE * org.apache.commons.io.FileUtils.ONE_KB
		if (task != null && !task.isCancelled()) {
			task.cancel(true);
		}
		task = new UpdateListOfFilesAsyncTask(
				this,
				getString(R.string.activity_select_file_file_detail),
				getString(R.string.activity_select_file_folder_detail),
				getString(R.string.home),
				getString(R.string.folder));
		task.execute(path);
	}

	enum Actions {
		SelectFile, SelectFolder
	}
}
