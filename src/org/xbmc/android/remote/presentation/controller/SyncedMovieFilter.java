package org.xbmc.android.remote.presentation.controller;

import org.xbmc.api.object.Actor;
import org.xbmc.api.object.Genre;

/**
 * Updates the "Filter Movie" fields on the XMBC server
 *
 * @author Nicholas Albion
 */
public class SyncedMovieFilter extends MovieFilter {

	public SyncedMovieFilter(Actor actor, Genre genre) {
		super(actor, genre);
	}

}
