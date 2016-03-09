package com.raidomatic.model;

import java.util.List;
import lombok.*;

public class Group extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String name;
	private @Getter @Setter String category;
	private @Getter @Setter String description;
	private @Getter @Setter List<String> memberNames;

	public List<Player> getMembers() {
		Players players = new Players();
		return players.getByNames(this.getMemberNames());
	}

	private static final long serialVersionUID = 710015844810083719L;
}
