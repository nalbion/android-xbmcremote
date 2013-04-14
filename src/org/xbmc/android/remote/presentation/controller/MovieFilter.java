package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.api.business.DataResponse;
import org.xbmc.api.business.IVideoManager;
import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;
import org.xbmc.api.object.Movie;

import android.app.Activity;

public class MovieFilter {
	protected Actor mActor;
	protected Genre mGenre;
	
	public MovieFilter( Actor actor, Genre genre ) {
		this.mActor = actor;
		this.mGenre = genre;
	}
	
	@Override
	public String toString() {
		return mActor != null ? mActor.name + " - " : mGenre != null ? mGenre.name + " - " : "" + "Movies";
	}
	
	public boolean isFilterSet() {
		return mActor != null || mGenre != null;
	}
	
	public void getMovies( IVideoManager videoManager, DataResponse<ArrayList<Movie>> response, Activity activity ) {
		if (mActor != null) {						// movies with a certain actor
			videoManager.getMovies(response, mActor, activity.getApplicationContext());
		} else if (mGenre != null) {					// movies of a genre
			videoManager.getMovies(response, mGenre, activity.getApplicationContext());
		} else {									// all movies
			videoManager.getMovies(response, activity.getApplicationContext());
		}
	}
	
	public void getMovies( IVideoManager videoManager, DataResponse<ArrayList<Movie>> response, Activity activity, int sortBy, String sortOrder ) {
		videoManager.getMovies(response, activity.getApplicationContext(), sortBy, sortOrder);
	}
}
