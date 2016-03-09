package com.raidomatic.model;

import java.util.*;

public class LootTiers extends AbstractCollectionManager<LootTier> {

	public List<LootTier> getLootTiers() throws Exception {
		//return this.getAll();
		return this.getCollection(LootTierList.BY_ID, null, null, -1);
	}

	public LootTier getByName(String name) {
		return this.getFirstOfCollection(LootTierList.BY_NAME, name);
	}

	public LootTier getByItemLevel(int itemLevel) throws Exception {
		return this.getFirstOfCollection(LootTierList.BY_ITEM_LEVELS, itemLevel);
	}

	public List<LootTier> getLootTiersByName() {
		return this.getCollection(LootTierList.BY_NAME);
	}
	//	public Map<String, LootTier> getLootTiersById() { return this.getIdMap(); }
	@Override
	protected AbstractDominoList<LootTier> createCollection() { return new LootTierList(); }

	private static final long serialVersionUID = -4724999224598820168L;


	public class LootTierList extends AbstractDominoList<LootTier> {
		public static final String BY_ID = "TierID";
		public static final String BY_NAME = "Name";
		public static final String BY_ITEM_LEVELS = "ItemLevels";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "name", "itemLevels" }; }
		@Override
		protected String getDefaultSort() { return LootTierList.BY_ID; }
		@Override
		protected String getViewName() { return "Loot Tiers"; }

		private static final long serialVersionUID = -8293875184916741179L;
	}
}