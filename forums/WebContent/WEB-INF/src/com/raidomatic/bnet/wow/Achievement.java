package com.raidomatic.bnet.wow;

import java.util.List;
import lombok.Data;

public @Data class Achievement implements BnetObject {
	private static final long serialVersionUID = -7823828293062233330L;

	private API api;
	private List<AchievementCriterion> criterion;
	private String description;
	private String icon;
	private int id;
	private int points;
	private List<Object> rewardItems;
	private String title;

}
