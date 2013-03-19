package org.xbmc.android.remote.presentation.controller;

import java.util.ArrayList;

import org.xbmc.api.object.Movie;

public interface IMovieListController extends IController {
	public void updateMovieList( ArrayList<Movie> movies );
}
