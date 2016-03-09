package com.raidomatic.model;

import lombok.*;
import com.raidomatic.JSFUtil;
import lotus.domino.*;

public class Boss extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String name;
	private @Getter @Setter String npcId;

	@Override
	public Database getDatabase() throws NotesException { return JSFUtil.getSession().getDatabase("", "wow/common.nsf"); }

	private static final long serialVersionUID = 4903072861687902165L;
}
