package ru.kolotnev.codoma;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.util.Log;
import android.view.ViewGroup;

/**
 * Extension of FragmentStatePagerAdapter which intelligently caches
 * all active fragments and manages the fragment lifecycles.
 * Usage involves extending from SmartFragmentStatePagerAdapter as you would any other PagerAdapter.
 */
public abstract class SmartFragmentPagerAdapter<T extends Fragment> extends FragmentStatePagerAdapter {
	/**
	 * Sparse array to keep track of registered fragments in memory.
	 */
	private final SparseArrayCompat<T> registeredFragments = new SparseArrayCompat<>();

	private final Class<T> clazz;

	public SmartFragmentPagerAdapter(FragmentManager fragmentManager, Class<T> clazz) {
		super(fragmentManager);
		this.clazz = clazz;
	}

	// Register the fragment when the item is instantiated
	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		Object o = super.instantiateItem(container, position);
		T fragment = clazz.cast(o);
		registeredFragments.put(position, fragment);
		return o;
	}

	// Unregister when the item is inactive
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		registeredFragments.remove(position);
		super.destroyItem(container, position, object);
		Log.e("Codoma", "removing fragment at position " + position);

		if (position <= getCount()) {
			FragmentManager manager = ((Fragment) object).getFragmentManager();
			manager.beginTransaction().remove((Fragment) object).commit();
		}
	}

	/**
	 * Returns the fragment for the position (if instantiated).
	 *
	 * @param position
	 * 		Position of instantiated fragment.
	 *
	 * @return Fragment for the specified position.
	 */
	public T getRegisteredFragment(int position) {
		return registeredFragments.get(position);
	}

	/**
	 * Returns list of registered fragments.
	 *
	 * @return Array of registered fragments.
	 */
	@NonNull
	public SparseArrayCompat<T> getListOfRegisteredFragments() { return registeredFragments; }
}

