package com.raidomatic.bnet.wow;

import java.util.List;
import lombok.Data;

public @Data class TalentSpec implements BnetObject {
	private static final long serialVersionUID = -3212494789272716575L;

	private API api;
	private boolean selected;
	private String name;
	private String icon;
	private String role;

	private List<Glyph> majorGlyphs;
	private List<Glyph> minorGlyphs;

}
