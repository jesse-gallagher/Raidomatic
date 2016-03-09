package com.raidomatic.model;

import java.util.*;

public class Specs extends AbstractCollectionManager<Spec> {
	public List<Spec> getSpecs() throws Exception {
		return this.getAll();
	}
	public Spec getBySignature(String signature) {
		return this.getFirstOfCollection(SpecList.BY_SIGNATURE, signature);
	}

	@Override
	protected AbstractDominoList<Spec> createCollection() { return new SpecList(); }

	private static final long serialVersionUID = -662579864095974764L;


	public class SpecList extends AbstractDominoList<Spec> {
		public static final String BY_ID = "SpecID";
		public static final String BY_SIGNATURE = "$Signature";

		@Override
		protected String[] getColumnFields() { return new String[] { "", "id", "className", "spec", "", "mainItemFormula", "offItemFormula" }; }
		@Override
		protected String getDefaultSort() { return SpecList.BY_ID; }
		@Override
		protected String getViewName() { return "Specs"; }
		@Override
		protected String getDatabasePath() { return "wow/common.nsf"; }


		private static final long serialVersionUID = -7232668187043559709L;
	}
}