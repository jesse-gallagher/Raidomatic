package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class AchievementCriterion implements BnetObject {
	private static final long serialVersionUID = -1464872382003034617L;

	private API api;
	private String description;
	private int id;
}
