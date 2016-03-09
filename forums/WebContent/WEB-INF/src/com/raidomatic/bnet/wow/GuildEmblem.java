package com.raidomatic.bnet.wow;

import java.awt.Color;
import lombok.Data;

public @Data class GuildEmblem implements BnetObject {
	private static final long serialVersionUID = -7992355349996612604L;

	private API api;
	private int icon;
	private Color iconColor;
	private int border;
	private Color borderColor;
	private Color backgroundColor;
}
