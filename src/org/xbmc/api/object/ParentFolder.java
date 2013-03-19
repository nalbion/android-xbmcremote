package org.xbmc.api.object;

import org.xbmc.api.type.MediaType;

@SuppressWarnings("serial")
public class ParentFolder implements INamedCover {

	public String getShortName() {
		return "..";
	}

	public int getId() {
		return 0;
	}

	public int getMediaType() {
		return MediaType.UNKNOWN;
	}

	public int getFallbackCrc() {
		return 0;
	}

	public long getCrc() {
		return 0;
	}

	public String getPath() {
		return null;
	}

	public String getName() {
		return "..";
	}

	public String getThumbUrl() {
		return null;
	}
}
