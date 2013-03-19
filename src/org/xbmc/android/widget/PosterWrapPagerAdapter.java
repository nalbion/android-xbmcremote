package org.xbmc.android.widget;

import java.util.ArrayList;

import org.xbmc.api.object.INamedResource;

import android.support.v4.view.PagerAdapter;

/**
 * From the Android docs: A PagerAdapter may implement a form of View recycling if desired
 */
public abstract class PosterWrapPagerAdapter<T extends INamedResource> extends PagerAdapter {

	protected static final float MIN_SCALE = 1f - 1f / 7f;
	protected static final float MAX_SCALE = 1f;
	
	protected ArrayList<T> mDataItems;
	protected int mFakeCount = 0;
	protected boolean mIsDefaultItemSelected = false;
	
	/**
	 * Must be called on the main UI thread
	 * @param items
	 */
	public void setDataItems( ArrayList<T> items ) {
		this.mDataItems = items;
		mFakeCount = items.size() + 1;
		notifyDataSetChanged();	// the change to the dataset and this call must be called on the main thread
	}
	
	public T getDataItem( int position ) {
		return (mDataItems == null) ? null : mDataItems.get(position);
	}

	@Override
	public int getCount() {
		return mFakeCount;
	}
	
	@Override
	public CharSequence getPageTitle(int position) {
		return mDataItems.get(position).getShortName();
	}
	
	public int getItemPositionByTitle( String title ) {
		int i = mDataItems.size();
		while( i-- != 0 ) {
			T item = mDataItems.get(i);
			if( title.equals(item.getShortName()) ) {
				return i;
			}
		}
		return 0;
	}
}
