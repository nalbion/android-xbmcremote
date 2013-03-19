package org.xbmc.android.widget;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.presentation.controller.MovieControllerDelegate;
import org.xbmc.android.remote.presentation.widget.JewelView;
import org.xbmc.api.business.IManager;
import org.xbmc.api.object.INamedCover;
import org.xbmc.api.object.ParentFolder;
import org.xbmc.api.type.ThumbSize;

import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

public class MoviePagerAdapter extends PosterWrapPagerAdapter<INamedCover> {
	private static final String TAG = "MoviePagerAdapter";
	private static final int mThumbSize = ThumbSize.MEDIUM;
	private final boolean loadCovers;
	private MovieControllerDelegate mDelegate;
	
	
	public MoviePagerAdapter( ArrayList<INamedCover> movies, MovieControllerDelegate delegate, boolean mLoadCovers ) {
		this.mDelegate = delegate;
		this.loadCovers = mLoadCovers;
		this.setDataItems(movies);
	}

	@Override
	public Object instantiateItem( ViewGroup container, int position ) {
		// make the size larger, and change the position
        // to trick viewpager into paging forever
        if (position >= mDataItems.size() - 1) {
            int newPosition = position % mDataItems.size();

            position = newPosition;
            mFakeCount++;
        }
		
		JewelView view = new JewelView( container.getContext() );
//	    jewel.setLayoutParams( LayoutParams.);

		final INamedCover item = getDataItem(position);
		Log.d(TAG, "instantiateItem #" + position + ", " + item);
//		view.reset();
//		view.position = position;
//		view.posterOverlay = movie.numWatched > 0 ? mWatchedBitmap : null;
//		view.title = movie.title;
//		view.subtitle = movie.genres;
//		view.subtitleRight = movie.year > 0 ? String.valueOf(movie.year) : "";
//		view.bottomtitle = movie.runtime;
//		view.bottomright = String.valueOf(movie.rating);
//		
		if(loadCovers) {
			IManager videoManager = mDelegate.getVideoManager();
			if( item instanceof ParentFolder ) {
				view.setCover(R.drawable.parent_folder);
			} else {
				if(videoManager.coverLoaded(item, mThumbSize)){
					view.setCover(videoManager.getCoverSync(item, mThumbSize));
				}else{
					view.setCover(null);
//TODO: load cover				view.getResponse().load(movie, !mPostScrollLoader.isListIdle());
				}
			}
		}
		
		view.setId(position);
		// TODO: not unique, but all we get from the JSON-RPC regarding the currently displayed movie is:
		// Container.ListItem(1).Label = movie.getShortName()
		// Container.ListItem(1).Label2 = rating
//		view.setTag( movie.getShortName() );
		view.setTag( item.getId() );			// guaranteed to be unique
		container.addView(view); //, position);
//		return movie;
		
//		view.setGravity(Gravity.CENTER) ;
		if (!mIsDefaultItemSelected) {
            view.setScaleX(MAX_SCALE);
            view.setScaleY(MAX_SCALE);
            mIsDefaultItemSelected = true;
        } else {
            view.setScaleX(MIN_SCALE);
            view.setScaleY(MIN_SCALE);
        }
		
		return view;
	}
	
	@Override
	public void destroyItem( ViewGroup container, int position, Object object ) {
		Log.d(TAG, "destroy Movie page #" + position + ", " + object);
//		container.removeViewAt(position);
//		mDataItems.remove(position);
		container.removeView( (View)object );
	}
	
	@Override
	public boolean isViewFromObject( View pageView, Object item ) {
//		Movie movie = (Movie)item;
//		return pageView.getTag().equals( movie.getId() );
		return pageView == item;
	}
	
	@Override
	public void setPrimaryItem( ViewGroup container, int position, Object item ) {
//		Log.i(TAG, "setPrimaryItem #" + position + ": " + item);
		super.setPrimaryItem(container, position, item);
		// TODO: de-emphasise currently emphasised Jewel
		// TODO: emphasise the jewel for this item
	}
}
