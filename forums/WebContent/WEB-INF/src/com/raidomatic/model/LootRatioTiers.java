package com.raidomatic.model;

import java.util.List;

public class LootRatioTiers extends AbstractCollectionManager<LootRatioTier> {
	private static final long serialVersionUID = 1L;

	public LootRatioTier getLootRatioTierForPercent(int percent) {
		List<LootRatioTier> lootRatioTiers = new LootRatioTierList();
		for(LootRatioTier tier : lootRatioTiers) {
			if(tier.getMax() != null && percent <= tier.getMax()) {
				return tier;
			}
		}
		return lootRatioTiers.get(lootRatioTiers.size()-1);
	}

	@Override
	protected AbstractDominoList<LootRatioTier> createCollection() { return new LootRatioTierList(); }

	public class LootRatioTierList extends AbstractDominoList<LootRatioTier> {
		private static final long serialVersionUID = 1L;

		public static final String BY_ID = "$LootRatioTierID";
		public static final String BY_MIN = "MinPct";

		@Override
		protected String[] getColumnFields() {
			return new String[] { "min", "max", "name", "id" };
		}

		@Override
		protected String getDefaultSort() { return LootRatioTierList.BY_ID; }
		@Override
		protected String getViewName() { return "Loot Ratio Tiers"; }
	}
}
