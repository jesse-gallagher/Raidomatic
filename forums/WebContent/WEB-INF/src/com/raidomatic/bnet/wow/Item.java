package com.raidomatic.bnet.wow;

import java.util.*;
import lombok.*;

public @Data class Item implements BnetObject {
	private static final long serialVersionUID = -4298893492461566643L;

	private API api;
	private List<Integer> allowableClasses;
	private int armor;
	private int baseArmor;
	private List<BonusStat> bonusStats;
	private long buyPrice;
	private int containerSlots;
	private String description;
	private int disenchantingSkillRank;
	private int displayInfoId;
	private boolean equippable;
	private boolean hasSockets;
	private String icon;
	private int id;
	private int inventoryType;
	private boolean isAuctionable;
	private int itemBind;
	private int itemClass;
	private int itemLevel;
	private ItemSource itemSource;
	private List<Object> itemSpells;
	private int itemSubClass;
	private int maxCount;
	private int maxDurability;
	private int minFactionId;
	private int minReputation;
	private String name;
	private int quality;
	private int requiredLevel;
	private int requiredSkill;
	private int requiredSkillRank;
	private long sellPrice;
	private SocketInfo socketInfo;
	private int stackable;

	public String getQualityName() {
		switch(quality) {
		case 1: return "Common";
		case 2: return "Uncommon";
		case 3: return "Rare";
		case 4: return "Epic";
		case 5: return "Legendary";
		default: return "";
		}
	}
}
