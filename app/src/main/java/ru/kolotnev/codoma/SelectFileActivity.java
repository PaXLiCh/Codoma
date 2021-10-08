package ru.kolotnev.codoma;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Activity to select files.
 */
public class SelectFileActivity extends AppCompatActivity
		implements OnBackPressedSubscriber {
	private OnBackPressedListener onBackPressedListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Utils.setTheme(this);
		super.onCreate(savedInstanceState);

		if (savedInstanceState == null) {
			// During initial setup, plug in the details fragment.
			SelectFileFragment details = new SelectFileFragment();
			details.setArguments(getIntent().getExtras());
			getSupportFragmentManager().beginTransaction().add(
					android.R.id.content, details).commit();
		}
	}

	@Override
	public void onBackPressed() {
		if (onBackPressedListener != null) {
			onBackPressedListener.onBackPressed();
		}
	}

	@Override
	public void setOnBackPressedListener(OnBackPressedListener listener) {
		onBackPressedListener = listener;
	}
}
