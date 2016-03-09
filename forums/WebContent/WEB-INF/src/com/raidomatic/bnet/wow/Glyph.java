package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class Glyph implements BnetObject {
	private static final long serialVersionUID = -3265125148381187010L;

	private API api;
	private int glyphId;
	private int itemId;
	private String name;
	private String icon;
}
