package ru.kolotnev.codoma;

import android.app.Dialog;
import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import java.util.Arrays;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link Callbacks} interface
 * to handle interaction events.
 * Use the {@link FileOptionsDialogFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FileOptionsDialogFragment
		extends DialogFragment
		implements DialogInterface.OnClickListener {
	public static final String TAG = "FileOptionsDialog";
	private static final String ARG_URI = "uri";
	private static final String ARG_ENCODING = "encoding";
	private static final String ARG_EOL = "eol";

	private Uri uri;
	private String encoding;
	private LineReader.LineEnding eol;

	private String encodingDefault;
	private LineReader.LineEnding eolDefault;

	public FileOptionsDialogFragment() {
		// Required empty public constructor
	}

	/**
	 * Use this factory method to create a new instance of
	 * this fragment using the provided parameters.
	 *
	 * @param encoding
	 * 		Parameter 1.
	 * @param eol
	 * 		Parameter 2.
	 *
	 * @return A new instance of fragment FileOptionsDialogFragment.
	 */
	public static FileOptionsDialogFragment newInstance(Uri uri, String encoding, LineReader.LineEnding eol) {
		FileOptionsDialogFragment fragment = new FileOptionsDialogFragment();
		Bundle args = new Bundle();
		args.putParcelable(ARG_URI, uri);
		args.putString(ARG_ENCODING, encoding);
		args.putSerializable(ARG_EOL, eol);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		Bundle bundle = getArguments();
		if (bundle != null) {
			uri = bundle.getParcelable(ARG_URI);
			encodingDefault = encoding = bundle.getString(ARG_ENCODING);
			eolDefault = eol = (LineReader.LineEnding) bundle.getSerializable(ARG_EOL);
		}
		Context context = getActivity();
		View v = View.inflate(context, R.layout.dialog_file_options, null);
		Spinner spinnerEncoding = v.findViewById(R.id.spinner_encodings);
		Spinner spinnerEnding = v.findViewById(R.id.spinner_endings);

		Resources res = getResources();
		final String[] valuesEndings = res
				.getStringArray(R.array.settings_file_line_endings_values);
		ArrayAdapter<String> adapterEndings = new ArrayAdapter<>(context,
				android.R.layout.simple_spinner_dropdown_item,
				res.getStringArray(R.array.settings_file_line_endings_entries));
		spinnerEnding.setAdapter(adapterEndings);
		spinnerEnding.setSelection(Arrays.asList(valuesEndings).indexOf(eolDefault.name()));
		spinnerEnding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				eol = LineReader.LineEnding.valueOf(valuesEndings[position]);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				eol = eolDefault;
			}
		});

		final String[][] encodings = FileUtils.getFullSupportedEncodings();
		ArrayAdapter<String> adapterEncodings = new ArrayAdapter<>(context,
				android.R.layout.simple_spinner_dropdown_item,
				encodings[1]);
		spinnerEncoding.setAdapter(adapterEncodings);
		spinnerEncoding.setSelection(Arrays.asList(encodings[0]).indexOf(encodingDefault));
		spinnerEncoding.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				encoding = encodings[0][position];
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
				encoding = encodingDefault;
			}
		});

		return new AlertDialog.Builder(context)
				.setTitle(R.string.dialog_file_options_title)
				.setPositiveButton(android.R.string.ok, this)
				.setNegativeButton(android.R.string.cancel, null)
				.setView(v)
				.create();
	}

	@Override
	public void onClick(DialogInterface dialog, int which) {
		Callbacks callbacks = null;
		if (getTargetFragment() instanceof Callbacks) {
			callbacks = (Callbacks) getTargetFragment();
		}
		if (callbacks == null) {
			if (getActivity() instanceof Callbacks)
				callbacks = (Callbacks) getActivity();
		}
		if (callbacks != null) {
			callbacks.onSelectFileOptions(uri, eol, encoding);
		}
	}

	/**
	 * This interface must be implemented by activities that contain this
	 * fragment to allow an interaction in this fragment to be communicated
	 * to the activity and potentially other fragments contained in that
	 * activity.
	 * <p/>
	 * See the Android Training lesson <a href=
	 * "http://developer.android.com/training/basics/fragments/communicating.html"
	 * >Communicating with Other Fragments</a> for more information.
	 */
	interface Callbacks {
		// TODO: Update argument type and name
		void onSelectFileOptions(Uri uri, LineReader.LineEnding eol, String encoding);
	}

}
