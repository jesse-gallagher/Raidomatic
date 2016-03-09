package com.raidomatic.bnet.wow;

import java.io.IOException;
import java.util.*;
import org.json.JSONException;
import lombok.*;


public @Data class Character implements BnetObject {
	private static final long serialVersionUID = 1082307290254153868L;

	private API api;
	private Date lastModified;
	private String name;
	private String realmName;
	private int classId;
	private int raceId;
	private int genderId;
	private int level;
	private long achievementPoints;
	private String thumbnail;
	private Guild guild = null;
	private TalentSpec primaryTalentSpec = null;
	private TalentSpec secondaryTalentSpec = null;

	public Realm getRealm() throws IOException, JSONException {
		return this.api.getRealm(this.getRealmName());
	}
}
