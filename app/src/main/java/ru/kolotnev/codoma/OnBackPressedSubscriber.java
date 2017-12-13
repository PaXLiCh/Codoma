package ru.kolotnev.codoma;

/**
 * Interface to Subscribe listener to Activity.onBackPressed
 */
interface OnBackPressedSubscriber {
	void setOnBackPressedListener(OnBackPressedListener listener);
}
