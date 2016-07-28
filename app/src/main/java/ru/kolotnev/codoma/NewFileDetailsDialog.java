package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

import java.io.File;
import java.io.IOException;

public class NewFileDetailsDialog extends DialogFragment {
	private EditText mName;
	private EditText mFolder;

	GreatUri currentUri;
	String fileText;
	String fileEncoding;

	public static NewFileDetailsDialog newInstance(GreatUri currentUri, String fileText, String fileEncoding) {
		Bundle args = new Bundle();

		NewFileDetailsDialog fragment = new NewFileDetailsDialog();
		fragment.setArguments(args);
		fragment.currentUri = currentUri;
		fragment.fileText = fileText;
		fragment.fileEncoding = fileEncoding;

		return fragment;
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		View view = View.inflate(getActivity(), R.layout.dialog_new_file_details, null);
		mName = (EditText) view.findViewById(android.R.id.text1);
		mFolder = (EditText) view.findViewById(android.R.id.text2);

		boolean noName = TextUtils.isEmpty(currentUri.getFileName());
		boolean noPath = TextUtils.isEmpty(currentUri.getFilePath());

		if (noName) {
			this.mName.setText(".txt");
		} else {
			this.mName.setText(currentUri.getFileName());
		}
		if (noPath) {
			this.mFolder.setText(PreferenceHelper.getWorkingFolder(getActivity()));
		} else {
			this.mFolder.setText(currentUri.getParentFolder());
		}

		// Show soft keyboard automatically
		this.mName.requestFocus();
		this.mName.setSelection(0);
		getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);

		return new AlertDialog.Builder(getActivity())
				.setTitle(R.string.action_save_as)
				.setView(view)
				.setPositiveButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (!mName.getText().toString().isEmpty() && !mFolder.getText().toString().isEmpty()) {
									File file = new File(mFolder.getText().toString(), mName.getText().toString());
									try {
										file.getParentFile().mkdirs();
										file.createNewFile();
									} catch (IOException e) {
										e.printStackTrace();
									}

									final GreatUri newUri = new GreatUri(Uri.fromFile(file), file.getAbsolutePath());

									new SaveTextFileTask(getActivity(), newUri, fileText, fileEncoding, new SaveTextFileTask.SaveTextFileListener() {
										@Override
										public void fileSaved(Boolean success) {
											if (getActivity() != null) {
												Log.e("Codoma ", "Saved file to " + newUri.getFilePath());
												// TODO: make callback
												//((MainActivity) getActivity()).savedAFile(newUri, true);
											}
										}
									}).execute();
								}
							}
						}
				)
				.setNegativeButton(android.R.string.cancel, null)
				.create();
	}

}
