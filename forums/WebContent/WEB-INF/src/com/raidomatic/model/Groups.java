package com.raidomatic.model;

import java.util.List;
import lotus.domino.Document;

public class Groups extends AbstractCollectionManager<Group> {
	@Override
	protected AbstractDominoList<Group> createCollection() { return new GroupList(); }

	private static final long serialVersionUID = 4061232179348505403L;


	public class GroupList extends AbstractDominoList<Group> {
		public static final String BY_ID = "$GroupID";
		public static final String BY_NAME = "$ListName";
		public static final String BY_CATEGORY = "ListCategory";

		public static final String DEFAULT_SORT = GroupList.BY_ID;

		@Override
		protected String[] getColumnFields() {
			return new String[] { "id", "name", "category", "description" };
		}
		@Override
		protected String getViewName() { return "Player Groups"; }
		@Override
		protected String getDatabasePath() { return "wownames.nsf"; }

		// Read in the list of group members, which is not in the view
		@Override
		@SuppressWarnings("unchecked")
		protected Group createObjectFromViewEntry(DominoEntryWrapper entry) throws Exception {
			Group group = super.createObjectFromViewEntry(entry);
			Document groupDoc = entry.getDocument();
			group.setMemberNames((List<String>)groupDoc.getItemValue("Members"));

			return group;
		}

		private static final long serialVersionUID = -4421668686573328890L;
	}
}