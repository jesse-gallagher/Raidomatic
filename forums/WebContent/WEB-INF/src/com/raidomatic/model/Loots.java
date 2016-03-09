package com.raidomatic.model;

import java.util.*;

public class Loots extends AbstractCollectionManager<Loot> {
	public List<Loot> getLoots() throws Exception {
		return this.getAll();
	}
	public List<Loot> getLootsByLevel() throws Exception {
		return this.getCollection(LootList.BY_ITEM_LEVEL);
	}
	public List<Loot> getLootsByResult() throws Exception {
		return this.getCollection(LootList.BY_RESULT);
	}
	public List<Loot> getLootsByEventDate() throws Exception {
		return this.getCollection(LootList.BY_EVENT_DATE_DESC);
	}
	public List<Loot> getLootsByItemName() throws Exception {
		return this.getCollection(LootList.BY_ITEM_NAME);
	}
	public List<Loot> getBankedLoots() throws Exception {
		return this.getCollection(LootList.BY_ITEM_NAME, null, "[Result]=Bank");
	}

	public List<Loot> getLootsForTopicId(String topicId) {
		//System.out.println("Creating loot list for topic id " + topicId);
		return this.getCollection(LootList.BY_TOPIC_ID, topicId, null, -1);
	}
	public List<Loot> getLootsForCharacterName(String name) {
		//System.out.println("Creating loot list for character name " + name);
		return this.getCollection(LootList.BY_CHARACTER_NAME, name);
	}
	public List<Loot> getLootsForItemId(String itemId) {
		//System.out.println("Creating loot list for item id " + itemId);
		return this.getCollection(LootList.BY_ITEM_ID, itemId, null, -1);
	}

	public List<Loot> getLootsForPlayerName(String playerName) throws Exception {
		//System.out.println("Creating loot list for player name " + playerName);
		return this.getCollection(LootList.BY_PLAYER_NAME, playerName);
	}
	public List<Loot> getLootsForPlayerNameAndResult(String playerName, String result) throws Exception {
		return this.search("[PlayerName]=" + playerName + " and [Result]=" + result);
	}
	public List<Loot> getLootsForPlayerNameResultAndLootTierId(String playerName, String result, String lootTierId) {
		//System.out.println("Creating loot list for player name, result, and loot tier id " + playerName + ", " + result + ", " + lootTierId);
		return this.getCollection(LootList.BY_PLAYER_NAME_RESULT_AND_LOOT_TIER_ID, playerName + result + lootTierId);
	}

	@Override
	protected AbstractDominoList<Loot> createCollection() { return new LootList(); }

	private static final long serialVersionUID = -1017415250449155473L;



	public static class LootList extends AbstractDominoList<Loot> {
		public static final String BY_ID = "LootID";
		public static final String BY_TOPIC_ID = "TopicID";
		public static final String BY_ITEM_ID = "ItemID";
		public static final String BY_CHARACTER_NAME = "CharacterName";
		public static final String BY_PLAYER_NAME = "PlayerName";
		public static final String BY_PLAYER_NAME_RESULT_AND_LOOT_TIER_ID = "$PlayerNameResultLootTierID";
		public static final String BY_ITEM_LEVEL = "ItemLevel";
		public static final String BY_RESULT = "Result";
		public static final String BY_EVENT_DATE_DESC = "EventDate-desc";
		public static final String BY_ITEM_NAME = "ItemName";

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "topicId", "itemId", "result", "characterName", "outOfGuildCharacter",
					"lootTierId", "dateTime", "playerName", "authors", "bankRemoval", "bankRemovalBy", "eventLeader",
					"eventLootTier", "eventDate"
			};
		}
		@Override
		protected String getDefaultSort() { return LootList.BY_ID; }
		@Override
		protected String getViewName() { return "Loots"; }


		private static final long serialVersionUID = 8154579029450245572L;
	}
}