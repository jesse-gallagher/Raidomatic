package com.raidomatic.model;

import java.util.List;
import lombok.*;

public class LootTier extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String name;
	private @Getter @Setter List<Integer> itemLevels;

	private static final long serialVersionUID = -4715135287833283127L;
}