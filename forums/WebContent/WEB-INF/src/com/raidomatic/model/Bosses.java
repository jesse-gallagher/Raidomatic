package com.raidomatic.model;

import java.util.*;

public class Bosses extends AbstractCollectionManager<Boss> {

	public List<Boss> getBosses() throws Exception {
		return this.getAll();
	}

	public Boss getByName(String name) throws Exception {
		return this.getFirstOfCollection(BossList.BY_NAME, name);
	}


	@Override
	protected AbstractDominoList<Boss> createCollection() { return new BossList(); }

	private static final long serialVersionUID = -1728862847913599525L;


	public class BossList extends AbstractDominoList<Boss> {
		public static final String BY_ID = "BossID";
		public static final String BY_NAME = "Name";
		public static final String BY_NPC_ID = "NPCID";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "name", "npcId" }; }
		@Override
		protected String getDefaultSort() { return BossList.BY_ID; }
		@Override
		protected String getViewName() { return "Bosses"; }
		@Override
		protected String getDatabasePath() { return "wow/common.nsf"; }

		private static final long serialVersionUID = -3676508324348239523L;
	}
}