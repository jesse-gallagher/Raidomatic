package com.raidomatic.forum.model;

import com.raidomatic.model.*;
import java.util.*;

public class Forums extends AbstractCollectionManager<Forum> {

	public List<Forum> getForums() throws Exception {
		return this.getAll();
	}

	public List<Forum> getForumsForCategoryId(String categoryId) {
		return this.getCollection(ForumList.BY_CATEGORY_ID, categoryId);
	}

	@Override
	protected AbstractDominoList<Forum> createCollection() { return new ForumList(); }

	private static final long serialVersionUID = -8748198360876359061L;


	public class ForumList extends AbstractDominoList<Forum> {
		public static final String BY_ID = "ForumID";
		public static final String BY_TITLE = "Title";
		public static final String BY_CATEGORY_ID = "CategoryID";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "title", "categoryId", "subtitle", "readers", "authors" }; }
		@Override
		protected String getDefaultSort() { return ForumList.BY_ID; }
		@Override
		protected String getViewName() { return "Forums"; }

		private static final long serialVersionUID = -3249992079082466956L;
	}
}