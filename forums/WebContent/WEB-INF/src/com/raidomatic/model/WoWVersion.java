package com.raidomatic.model;

import java.util.Date;
import lombok.Getter;
import lombok.Setter;
import lotus.domino.Database;
import lotus.domino.NotesException;

import com.raidomatic.JSFUtil;

public class WoWVersion extends AbstractModel {
	private @Getter @Setter Date releaseDate;
	private @Getter @Setter String versionNumber;
	private @Getter @Setter String expansionName;
	private @Getter @Setter String patchName;
	private @Getter @Setter String url;
	private @Getter @Setter String id;

	@Override
      public Database getDatabase() throws NotesException { return JSFUtil.getSession().getDatabase("", "wow/common.nsf"); }

	private static final long serialVersionUID = -5461613099158091206L;
}