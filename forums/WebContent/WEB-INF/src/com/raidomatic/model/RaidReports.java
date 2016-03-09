package com.raidomatic.model;

import java.util.*;


public class RaidReports extends AbstractCollectionManager<RaidReport> {
	public List<RaidReport> getRaidReports() throws Exception {
		return this.getAll();
	}
	public List<RaidReport> getRaidReportsByBossId(String bossId) {
		return this.getCollection(RaidReportList.BY_BOSS_ID, bossId);
	}
	public List<RaidReport> getRaidReportsByTopicId(String topicId) {
		return this.getCollection(RaidReportList.BY_TOPIC_ID, topicId);
	}

	@Override
	protected AbstractDominoList<RaidReport> createCollection() { return new RaidReportList(); }

	private static final long serialVersionUID = -2834169598679788343L;


	public class RaidReportList extends AbstractDominoList<RaidReport> {
		public static final String BY_ID = "RaidReportID";
		public static final String BY_BOSS_ID = "BossIDs";
		public static final String BY_TOPIC_ID = "TopicID";

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "url", "bossIds", "topicId", "eventLeader", "notes", "group", "createdBy", "authors" };
		}
		@Override
		protected String getDefaultSort() { return RaidReportList.BY_ID; }
		@Override
		protected String getViewName() { return "Raid Reports"; }

		private static final long serialVersionUID = 3082792870044949936L;
	}
}