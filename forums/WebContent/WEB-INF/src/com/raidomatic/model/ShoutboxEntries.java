package com.raidomatic.model;

import java.util.List;

public class ShoutboxEntries extends AbstractCollectionManager<ShoutboxEntry> {

	public List<ShoutboxEntry> getEntriesByDateTimeDesc() {
		return this.createCollection(ShoutboxEntryList.BY_DATETIME_DESC);
	}

	@Override
	protected AbstractDominoList<ShoutboxEntry> createCollection() { return new ShoutboxEntryList(); }

	private static final long serialVersionUID = -6504257918994099002L;


	public class ShoutboxEntryList extends AbstractDominoList<ShoutboxEntry> {
		public static final String BY_ID = "ShoutboxEntryID";
		public static final String BY_DATETIME_DESC = "DateTime-desc";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "dateTime", "createdBy", "body" }; }
		@Override
		protected String getDefaultSort() { return ShoutboxEntryList.BY_ID; }
		@Override
		protected String getViewName() { return "Shoutbox Entries"; }


		private static final long serialVersionUID = 551073504492210831L;
	}
}