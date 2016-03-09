package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class Socket implements BnetObject {
	private static final long serialVersionUID = -60550243478318040L;

	private API api;
	private String type;

}
