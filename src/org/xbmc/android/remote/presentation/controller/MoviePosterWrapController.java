package org.xbmc.android.remote.presentation.controller;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Map;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.ControlManager;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.widget.FiveLabelsItemView;
import org.xbmc.android.remote.presentation.widget.JewelView;
import org.xbmc.android.remote.presentation.widget.PagerContainer;
import org.xbmc.android.util.ImportUtilities;
import org.xbmc.android.widget.IdleListener;
import org.xbmc.android.widget.MoviePagerAdapter;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IInfoManager;
import org.xbmc.api.business.IControlManager.NavigateCommand;
import org.xbmc.api.object.INamedCover;
import org.xbmc.api.object.Movie;
import org.xbmc.api.object.ParentFolder;
import org.xbmc.api.presentation.INotifiableController;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.Toast;

/**
 * Mimics the "Poster Wrap" view mode in the Movies screen
 * 
 * // TODO: add "Sync" to sync the on-screen Videos list with the mobile app 
// http://192.168.0.91/jsonrpc?request={"jsonrpc":"2.0","method":"GUI.ActivateWindow","params":{"window":"videos"}}
// http://192.168.0.91/jsonrpc?request={"jsonrpc":"2.0","method":"GUI.ActivateWindow","params":{"window":"videos","parameters":["MovieTitles"]}}
// http://192.168.0.91/jsonrpc?request={"jsonrpc":"2.0","method":"VideoLibrary.GetMovies","params":{"properties":["art","thumbnail","file"],"sort":{"order":"ascending", "method":"label", "ignorearticle":true}}, "id": "libMovies"}
// Control.SetFocus(id,position) - but what is id? is position 0 or 1 based?
// http://192.168.0.91/jsonrpc?request={"jsonrpc":"2.0","method":"XBMC.GetInfoLabels","params": {"labels":["Container.Viewmode","Control.GetLabel(501)","Container(501).NumItems","Container(501).Position","Container(501).ListItem(1).Label"]}, "id":1}
// "Container.Viewmode","ListItem(-2).Label","Control.GetLabel(501)","Container.NumItems","Container.Position","Container.ListItem(1).Label"
 *
 * @author Nicholas Albion
 */
@SuppressWarnings("serial")
public class MoviePosterWrapController extends ListController 
	implements IMovieListController, Serializable, INotifiableController, ViewPager.OnPageChangeListener
{

	public static final int ITEM_CONTEXT_PLAY = 1;
	private MovieControllerDelegate mDelegate;
	private IControlManager mControlManager;
	
	private boolean mLoadCovers = false;

	private static Bitmap mWatchedBitmap;
	private ViewPager mPager;
	
	
	public void onCreate(Activity activity, Handler handler) {
		final String sdError = ImportUtilities.assertSdCard();
		mLoadCovers = sdError == null;
		
		if (!isCreated()) {
			super.onCreate(activity, handler);
			mDelegate = new MovieControllerDelegate(activity, this, this);
			mControlManager = ManagerFactory.getControlManager(this);
			
			PagerContainer container = (PagerContainer)activity.findViewById(R.id.screenmoviepager_container);
			container.setPageChangeListener(this);
			ViewPager pager = container.getViewPager();
			mPager = pager;
			pager.setOffscreenPageLimit(2);	// we want to show 2 either side of the currently selected movie
			pager.setClipChildren(false);
			pager.setHorizontalFadingEdgeEnabled(true);
			pager.setFadingEdgeLength(30);

			if (!mLoadCovers) {
				Toast toast = Toast.makeText(activity, sdError + " Displaying place holders only.", Toast.LENGTH_LONG);
				toast.show();
			}
			
			mDelegate.initialise(activity);
			activity.registerForContextMenu(mPager);
			
			mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_poster);
			mWatchedBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.check_mark);
			
//			mList.setOnItemClickListener(new OnItemClickListener() {
//				public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//					if(isLoading()) return;
//					final Movie movie = (Movie)mList.getAdapter().getItem(((FiveLabelsItemView)view).position);
//					Intent nextActivity = new Intent(view.getContext(), MovieDetailsActivity.class);
//					nextActivity.putExtra(ListController.EXTRA_MOVIE, movie);
//					mActivity.startActivity(nextActivity);
//				}
//			});
//			mList.setOnKeyListener(new ListControllerOnKeyListener<Movie>());
			mDelegate.fetch(activity);
		}
		
		// Open the Movies list on the XBMC screen
		mControlManager.activateWindow(new DataResponse<Boolean>(), "videos", "MovieTitles", activity);
	}
	
	/**
	 * ViewPager doesn't need the IdleListener
	 */
	@Override
	protected IdleListener setupIdleListener() {
		return null;
	}
	
	@Override
	protected void setNoDataMessage(final String message, final int imageResource) {
		if (mMessageGroup != null) {
			mHandler.post(new Runnable() {
				public void run() {
					mMessageText.setText(message);
					mMessageText.setCompoundDrawablesWithIntrinsicBounds(imageResource, 0, 0, 0);
					mPager.setAdapter(null);
					mMessageGroup.setVisibility(View.VISIBLE);
				}
			});
		}
	}
	
	@Override
	protected void showOnLoading() {
		mHandler.post(new Runnable() {
			public void run() {
				mPager.setAdapter(new LoadingAdapter(mActivity));
				mPager.setVisibility(View.VISIBLE);
			}
		});
	}
	
	@Override
	protected boolean isLoading() {
		return mPager.getAdapter() instanceof LoadingAdapter;
	}
	
	/**
	 * Shows a dialog and refreshes the movie library if user confirmed.
	 * @param activity
	 */
	public void refreshMovieLibrary(final Activity activity) {
		mDelegate.refreshMovieLibrary(activity);
	}

	public void updateMovieList(final ArrayList<Movie> movies) {
		INamedCover parentFolder = new ParentFolder();
		ArrayList<INamedCover> covers = new ArrayList<INamedCover>(movies.size() + 1);
		covers.add(parentFolder);
		covers.addAll(movies);
		final MoviePagerAdapter adapter = new MoviePagerAdapter( covers, mDelegate, mLoadCovers );
		mPager.setAdapter( adapter );
//TODO:NDA probably need to reinstate this to get it to sync with the screen view, but that would break the container repainting	mPager.setOnPageChangeListener( adapter );
//		mViewPager.setCurrentItem(0);
		
		IInfoManager infoManager = ManagerFactory.getInfoManager(this);
		infoManager.getInfoLabels(new DataResponse<Map<String,String>>() {
			@Override
			public void run() {
				String currentTitle = value.get("Control.GetLabel");
				int itemIndex = adapter.getItemPositionByTitle( currentTitle );
				mPager.setCurrentItem( itemIndex, false );
			}
		}, new String[]{"Container.Viewmode", "Control.GetLabel"}, this.mActivity);
		
//		mPager.setCurrentItem( adapter.getCount() >> 1, false );
	}
	
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {}
 
    public void onPageSelected(int position) {
    	// TODO: figure out which way to scroll and by how far
    	mControlManager.navigate(new DataResponse<Boolean>(), NavigateCommand.RIGHT, this.mActivity);
    }
 
    public void onPageScrollStateChanged(int state) {}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		final FiveLabelsItemView view = (FiveLabelsItemView)((AdapterContextMenuInfo)menuInfo).targetView;
		menu.setHeaderTitle(view.title);
		menu.add(0, ITEM_CONTEXT_PLAY, 1, "Play Movie");
//		menu.add(0, ITEM_CONTEXT_INFO, 2, "View Details");
//		menu.add(0, ITEM_CONTEXT_IMDB, 3, "Open IMDb");
	}
	
	@Override
	public void onContextItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
	}
	
	@Override
	public void onCreateOptionsMenu(Menu menu) {
		mDelegate.onCreateOptionsMenu(menu);
	}
	
	@Override
	public void onOptionsItemSelected(MenuItem item) {
		if( !mDelegate.onOptionsItemSelected(item, mActivity) ) {
			super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onActivityPause() {
//		mAdapter.saveState();
		if( mDelegate != null ) {
			mDelegate.onActivityPause();
		}
		super.onActivityPause();
	}

	@Override
	public void onActivityResume(Activity activity) {
		super.onActivityResume(activity);
		if( mDelegate != null ) {
			mDelegate.onActivityResume(activity);
		}
//		mAdapter.restoreState(activity, activity.getClassLoader());
	}
	
	private class LoadingAdapter extends PagerAdapter {
		JewelView loading;
		public LoadingAdapter(Activity act) {
			//super(act, R.layout.loadinglistentry);
//			add("dummy");
			loading = new JewelView(act); // LayoutInflater.from(mActivity).inflate(R.layout.loadinglistentry, null);
			//((TextView)loading.findViewById(R.id.loading_text)).setText("Loading...");
		}
//		@Override
//		public View getView(int position, View convertView, ViewGroup parent) {
//			return row;
//		}
		
		@Override
		public Object instantiateItem( ViewGroup container, int position ) {
			container.addView(loading);
			return loading;
		}
		
		public void destroyItem( ViewGroup container, int position, Object object ) {
			Log.d(TAG, "destroy Loading page #" + position);
			container.removeAllViews();
		}
		
		@Override
		public int getCount() {
			return 1;
		}
		@Override
		public boolean isViewFromObject(View view, Object item) {
			return view == item;
		}
	}
}
