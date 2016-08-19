package ru.kolotnev.codoma;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

public class MainActivity extends AppCompatActivity {
	private String _dialogErrorMsg;
	private RecoveryManager _recoveryManager;

	private void showRecoveryFailedDialog() {
		new recoveryFailedDialogFragment().show(getSupportFragmentManager(), "recovery_failed");
	}

	private void prepareRecoveryFailedMessage() {
		String messageSummary = getString(R.string.dialog_sorry_force_resume);
		String messageDetails;

		int errorCode = _recoveryManager.getRecoveryErrorCode();
		switch (errorCode) {
			case RecoveryManager.ERROR_RECOVERY_DISABLED:
				messageDetails = getString(R.string.dialog_sorry_backup_disabled);
				break;
			case RecoveryManager.ERROR_FILE_NOT_FOUND:
				messageDetails = getString(R.string.dialog_sorry_missing_recovery,
						_recoveryManager.getExternalRecoveryDir());
				break;
			case RecoveryManager.ERROR_WRITE:
				messageDetails = getString(R.string.dialog_sorry_write_error);
				break;
			case RecoveryManager.ERROR_READ:
				messageDetails = getString(R.string.dialog_sorry_read_error,
						_recoveryManager.getSafekeepingFileAbsolutePath());
				break;
			default:
				Log.e(CodomaApplication.TAG, "Unrecognized recovery error code " + errorCode);
				messageDetails = "";
				break;
		}

		_dialogErrorMsg = messageSummary + "\n\n" + messageDetails;
	}

//	@Override
//	//This method is called by various worker threads
//	public void onComplete(final int requestCode, final Object result) {
//		runOnUiThread(new Runnable() {
//			@Override
//			public void run() {
//				switch (requestCode) {
//					case ProgressSource.WRITE:
//						//TODO restore view settings of previous file
//						_filename = _lastSelectedFile;
//						updateTitle();
//						RecentFilesProvider.addRecentFile(_filename);
//						_editField.setEdited(false);
//						Toast.makeText(MainActivity.this,
//								R.string.dialog_file_save_success,
//								Toast.LENGTH_SHORT).show();
//						saveFinishedCallback();
//						_taskWrite = null;
//						break;
//					case ProgressSource.FIND:
//					case ProgressSource.FIND_BACKWARDS:
//						final int foundIndex = ((FindResults) result).foundOffset;
//						final int length = ((FindResults) result).searchTextLength;
//
//						if (foundIndex != -1) {
//							_editField.setSelectionRange(foundIndex, length);
//						} else {
//							Toast.makeText(MainActivity.this,
//									R.string.dialog_find_no_results,
//									Toast.LENGTH_SHORT).show();
//						}
//						_taskFind = null;
//						break;
//					case ProgressSource.REPLACE_ALL:
//						final int replacementCount = ((FindResults) result).replacementCount;
//						final int newCaretPosition = ((FindResults) result).newStartPosition;
//						if (replacementCount > 0) {
//							_editField.setEdited(true);
//							_editField.selectText(false);
//							_editField.moveCaret(newCaretPosition);
//							_editField.respan();
//							_editField.invalidate(); //TODO reduce invalidate calls
//						}
//						Toast.makeText(MainActivity.this,
//								getString(R.string.dialog_replace_all_result) + replacementCount,
//								Toast.LENGTH_SHORT).show();
//						_taskFind = null;
//						break;
//					case ProgressSource.ANALYZE_TEXT:
//						_statistics = (CharEncodingUtils.Statistics) result;
//						_taskAnalyze = null;
//						new statisticsDialogFragment().show(getFragmentManager(), "stats");
//						break;
//				}
//			}
//		});
//	}

	public static class recoveryFailedDialogFragment extends DialogFragment {
		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			final MainActivity context = (MainActivity) getActivity();
			return new AlertDialog.Builder(context)
					.setTitle(R.string.dialog_sorry)
					.setMessage(context._dialogErrorMsg)
					.setPositiveButton(android.R.string.ok, null)
					.setNeutralButton(R.string.menu_main_help,
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int id) {
									dialog.dismiss();
									//context.openHelp();
								}
							})
					.create();
		}
	}
}
