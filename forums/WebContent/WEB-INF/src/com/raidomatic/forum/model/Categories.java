package com.raidomatic.forum.model;

import java.util.*;
import com.raidomatic.model.*;

public class Categories extends AbstractCollectionManager<Category> {

	public List<Category> getCategories() throws Exception {
		return this.getAll();
	}

	public List<Category> getCategoriesByIndex() {
		return this.createCollection(CategoryList.BY_INDEX, null, null, -1);
	}

	@Override
	protected AbstractDominoList<Category> createCollection() { return new CategoryList(); }

	private static final long serialVersionUID = 4018162633364069680L;



	public static class CategoryList extends AbstractDominoList<Category> {
		public static final String BY_ID = "CategoryID";
		public static final String BY_INDEX = "Index";
		public static final String BY_TITLE = "Title";

		@Override
		protected String[] getColumnFields() { return new String[] { "index", "title", "id", "readers", "authors" }; }
		@Override
		protected String getDefaultSort() { return CategoryList.BY_ID; }
		@Override
		protected String getViewName() { return "Categories"; }

		private static final long serialVersionUID = 8427427503135420864L;
	}
}