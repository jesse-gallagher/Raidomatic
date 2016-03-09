package com.raidomatic.bnet.wow;

import java.io.IOException;
import org.json.JSONException;
import java.util.List;
import lombok.Data;

public @Data class Guild implements BnetObject {
	private static final long serialVersionUID = 7838787010269528052L;

	private API api;
	private String name;
	private String realmName;
	private int level;
	private int members;
	private long achievementPoints;
	private GuildEmblem emblem;

	public Realm getRealm() throws IOException, JSONException { return this.api.getRealm(this.getRealmName()); }

	public List<News> getNews() throws Exception {
		return api.getNews(realmName, name);
	}
}
