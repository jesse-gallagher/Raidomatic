package com.raidomatic.bnet.wow;

import java.util.Date;
import lombok.*;

public @Data class News implements BnetObject {
	private static final long serialVersionUID = -5076771045937081671L;

	private API api;
	private Guild guild;
	private String characterName;
	private int itemId;
	private long timestamp;
	private String type;
	private Achievement achievement;

	private Item item;

	public Character getCharacter() throws Exception {
		return api.getCharacter(guild.getRealmName(), characterName);
	}
	@SneakyThrows
	public Item getItem() {
		if(this.item == null) {
			item = api.getItem(itemId);
		}
		return item;
	}
	public Date getDate() { return new Date(timestamp); }
}
