package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class ItemSource implements BnetObject {
	private static final long serialVersionUID = 906999190231903426L;

	private API api;
	private int sourceId;
	private String sourceType;

}
