package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class Realm implements BnetObject {
	private static final long serialVersionUID = 6848145671089559449L;

	private API api;
	private String type;
	private boolean queue;
	private boolean status;
	private String population;
	private String name;
	private String slug;

}
