package com.raidomatic.model;

import lombok.*;

public class LootRatioTier extends AbstractModel {
	private static final long serialVersionUID = 1L;

	private @Getter @Setter String id;
	private @Getter @Setter Integer min;
	private @Getter @Setter Integer max;
	private @Getter @Setter String name;
}
