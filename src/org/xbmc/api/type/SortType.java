/*
 *      Copyright (C) 2005-2009 Team XBMC
 *      http://xbmc.org
 *
 *  This Program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2, or (at your option)
 *  any later version.
 *
 *  This Program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with XBMC Remote; see the file license.  If not, write to
 *  the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 *  http://www.gnu.org/copyleft/gpl.html
 *
 */

package org.xbmc.api.type;

public abstract class SortType {
	
	public static final String ORDER_ASC = "ASC";
	public static final String ORDER_DESC = "DESC";
	
	public static final int ALBUM = 1;
	public static final int ARTIST = 2;
	public static final int TITLE = 3;
	public static final int FILENAME = 4;
	public static final int TRACK = 5;
	public static final int RATING = 6;
	public static final int YEAR = 7;
	public static final int EPISODE_NUM = 8;
	public static final int EPISODE_TITLE = 9;
	public static final int EPISODE_RATING = 10;
	public static final int DATE_ADDED = 11;
	public static final int MPAA_RATING = 12;
	public static final int DURATION = 13;
	public static final int PLAY_COUNT = 14;
	
	public static final int DONT_SORT = -1;
	
	/**
	 * @param sortMethod - value returned from JSON-RPC: {"jsonrpc":"2.0","method":"XBMC.GetInfoLabels","params": {"labels":["Container.SortMethod"]}, "id":1}
	 * @return
	 */
	public static int parseSortMethod( String sortMethod ) {
		if( "Rating".equals(sortMethod) ) {
			return RATING;
		} else if( "MPAA rating".equals(sortMethod) ) {
			return MPAA_RATING;
		} else if( "Duration".equals(sortMethod) ) {
			return DURATION;
		} else if( "Date added".equals(sortMethod) ) {
			return DATE_ADDED;
		} else if( "Play count".equals(sortMethod) ) {
			return PLAY_COUNT;
		} else if( "Title".equals(sortMethod) ) {
			return TITLE;
		} else if( "Year".equals(sortMethod) ) {
			return YEAR;
		} else return DONT_SORT;
	}
}