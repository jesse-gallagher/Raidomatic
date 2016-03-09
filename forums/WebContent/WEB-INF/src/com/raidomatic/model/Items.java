package com.raidomatic.model;

import java.util.*;

public class Items extends AbstractCollectionManager<Item> {

	public List<Item> getItems() throws Exception {
		return this.getAll();
	}
	public Item getByName(String name) throws Exception {
		return this.getFirstOfCollection(ItemList.BY_NAME, name);
	}
	public Item getByLookupName(String name) throws Exception {
		return this.getFirstOfCollection(ItemList.BY_LOOKUP_NAME, name);
	}

	@Override
	protected AbstractDominoList<Item> createCollection() { return new ItemList(); }

	private static final long serialVersionUID = 1883000135855294469L;


	public class ItemList extends AbstractDominoList<Item> {
		public static final String BY_ID = "LootID";
		public static final String BY_NAME = "Name";
		public static final String BY_LEVEL = "Level";
		public static final String BY_GEAR_SCORE = "GearScore";
		public static final String BY_CLASS_ID = "ClassID";
		public static final String BY_LOOKUP_NAME = "$LookupName";

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "name", "level", "gearScore", "qualityId", "qualityName", "classId", "className",
					"subclassId", "subclassName", "iconId", "iconName", "slotId", "slotName", "htmlTooltip", "json", "jsonEquip", "link"
			};
		}
		@Override
		protected String getDefaultSort() { return ItemList.BY_ID; }
		@Override
		protected String getViewName() { return "Items"; }
		@Override
		protected String getDatabasePath() { return "wow/common.nsf"; }


		private static final long serialVersionUID = 4224561682245929257L;
	}
}