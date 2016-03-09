package com.raidomatic.bnet.wow;

import lombok.Data;

public @Data class BonusStat implements BnetObject {
	private static final long serialVersionUID = 8852641822798937976L;

	private API api;
	private int amount;
	private boolean reforged;
	private int statId;

}
