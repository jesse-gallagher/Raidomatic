package com.raidomatic.model;

import lombok.Getter;
import lombok.Setter;
import lotus.domino.Database;
import lotus.domino.NotesException;

import com.raidomatic.JSFUtil;

public class Spec extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String className;
	private @Getter @Setter String spec;
	private @Getter @Setter String mainItemFormula;
	private @Getter @Setter String offItemFormula;

	@Override
      public Database getDatabase() throws NotesException { return JSFUtil.getSession().getDatabase("", "wow/common.nsf"); }

	private static final long serialVersionUID = 2701090450318398313L;
}
