package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.android.remote.R;
import org.xbmc.android.remote.business.AbstractManager;
import org.xbmc.android.remote.business.ManagerFactory;
import org.xbmc.android.remote.presentation.activity.NowPlayingActivity;
import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IControlManager;
import org.xbmc.api.business.IManager;
import org.xbmc.api.business.ISortableManager;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;
import org.xbmc.api.type.SortType;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.widget.Toast;

public class MovieControllerDelegate {
	public static final int MENU_PLAY_ALL = 1;
	public static final int MENU_SORT = 2;
	public static final int MENU_SORT_BY_TITLE_ASC = 21;
	public static final int MENU_SORT_BY_TITLE_DESC = 22;
	public static final int MENU_SORT_BY_YEAR_ASC = 23;
	public static final int MENU_SORT_BY_YEAR_DESC = 24;
	public static final int MENU_SORT_BY_RATING_ASC = 25;
	public static final int MENU_SORT_BY_RATING_DESC = 26;
	public static final int MENU_SORT_BY_DATE_ADDED_ASC = 27;
	public static final int MENU_SORT_BY_DATE_ADDED_DESC = 28;
	
	private ListController mController;
	private IMovieListController mMovieListController;
	private IVideoManager mVideoManager;
	private IControlManager mControlManager;
	private MovieFilter mMovieFilter;

	public MovieControllerDelegate( Activity activity, ListController controller, IMovieListController movieListController ) {
		mController = controller;
		mMovieListController = movieListController;
		
		mVideoManager = ManagerFactory.getVideoManager(controller);
		mControlManager = ManagerFactory.getControlManager(controller);
		
		((ISortableManager)mVideoManager).setSortKey(AbstractManager.PREF_SORT_KEY_MOVIE);
		((ISortableManager)mVideoManager).setPreferences(activity.getPreferences(Context.MODE_PRIVATE));
	}
	
	void initialise( Activity activity ) {
		Actor actor = (Actor)activity.getIntent().getSerializableExtra(ListController.EXTRA_ACTOR);
		Genre genre = (Genre)activity.getIntent().getSerializableExtra(ListController.EXTRA_GENRE);
		initMovieFilter( actor, genre );
//		activity.registerForContextMenu(mList);
//		
//		mFallbackBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.default_poster);
//		mWatchedBitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.check_mark);
	}
	
	protected void initMovieFilter( Actor actor, Genre genre ) {
		mMovieFilter = new MovieFilter( actor, genre );
	}
	
	public IManager getVideoManager() {
		return mVideoManager;
	}
	
	public void fetch(final Activity activity) {
		final String title = mMovieFilter.toString();
		DataResponse<ArrayList<Movie>> response = new DataResponse<ArrayList<Movie>>() {
			public void run() {
				if (value.size() > 0) {
					mController.setTitle(title + " (" + value.size() + ")");
					mMovieListController.updateMovieList(value);
				} else {
					mController.setTitle(title);
					mController.setNoDataMessage("No movies found.", R.drawable.icon_movie_dark);
				}
			}
		};
		
		mController.showOnLoading();
		mController.setTitle(title + "...");
		mMovieFilter.getMovies(mVideoManager, response, activity);
	}
	
	/**
	 * Shows a dialog and refreshes the movie library if user confirmed.
	 * @param activity
	 */
	public void refreshMovieLibrary(final Activity activity) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage("Are you sure you want XBMC to rescan your movie library?")
			.setCancelable(false)
			.setPositiveButton("Yes!", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					mControlManager.updateLibrary(new DataResponse<Boolean>() {
						public void run() {
							final String message;
							if (value) {
								message = "Movie library updated has been launched.";
							} else {
								message = "Error launching movie library update.";
							}
							Toast toast = Toast.makeText(activity, message, Toast.LENGTH_SHORT);
							toast.show();
						}
					}, "video", activity.getApplicationContext());
				}
			})
			.setNegativeButton("Uh, no.", new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
				}
			});
		builder.create().show();
	}
	
	public void playMovie( final Activity activity, Movie movie ) {
		mControlManager.playFile(new DataResponse<Boolean>() {
			public void run() {
				if (value) {
					activity.startActivity(new Intent(activity, NowPlayingActivity.class));
				}
			}
		}, movie.getPath(), 1, activity.getApplicationContext());
	}
	
	public void onCreateOptionsMenu(Menu menu) {
		if (mMovieFilter.isFilterSet()) {
			menu.add(0, MENU_PLAY_ALL, 0, "Play all").setIcon(R.drawable.menu_album);
		}
		SubMenu sortMenu = menu.addSubMenu(0, MENU_SORT, 0, "Sort").setIcon(R.drawable.menu_sort);
		sortMenu.add(2, MENU_SORT_BY_TITLE_ASC, 0, "by Title ascending");
		sortMenu.add(2, MENU_SORT_BY_TITLE_DESC, 0, "by Title descending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_ASC, 0, "by Year ascending");
		sortMenu.add(2, MENU_SORT_BY_YEAR_DESC, 0, "by Year descending");
		sortMenu.add(2, MENU_SORT_BY_RATING_ASC, 0, "by Rating ascending");
		sortMenu.add(2, MENU_SORT_BY_RATING_DESC, 0, "by Rating descending");
		sortMenu.add(2, MENU_SORT_BY_DATE_ADDED_ASC, 0, "by Date Added ascending");
		sortMenu.add(2, MENU_SORT_BY_DATE_ADDED_DESC, 0, "by Date Added descending");
//		menu.add(0, MENU_SWITCH_VIEW, 0, "Switch view").setIcon(R.drawable.menu_view);
		mController.createShowHideWatchedToggle(menu);
	}
	
	public boolean onOptionsItemSelected(MenuItem item, Activity activity) {
		final SharedPreferences.Editor ed;
		switch (item.getItemId()) {
		case MENU_PLAY_ALL:
			break;
		case MENU_SORT_BY_TITLE_ASC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_TITLE_DESC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.TITLE);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_YEAR_ASC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_YEAR_DESC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.YEAR);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_RATING_ASC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_RATING_DESC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.RATING);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_DATE_ADDED_ASC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.DATE_ADDED);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_ASC);
			ed.commit();
			fetch(activity);
			break;
		case MENU_SORT_BY_DATE_ADDED_DESC:
			ed = activity.getPreferences(Context.MODE_PRIVATE).edit();
			ed.putInt(AbstractManager.PREF_SORT_BY_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.DATE_ADDED);
			ed.putString(AbstractManager.PREF_SORT_ORDER_PREFIX + AbstractManager.PREF_SORT_KEY_MOVIE, SortType.ORDER_DESC);
			ed.commit();
			fetch(activity);
			break;
		default:
			return false;
		}
		return true;
	}
	
	public void onActivityPause() {
		if (mVideoManager != null) {
			mVideoManager.setController(null);
			mVideoManager.postActivity();
		}
		if (mControlManager != null) {
			mControlManager.setController(null);
		}
	}

	public void onActivityResume(Activity activity) {
		if (mVideoManager != null) {
			mVideoManager.setController(mController);
		}
		if (mControlManager != null) {
			mControlManager.setController(mController);
		}
	}
}
