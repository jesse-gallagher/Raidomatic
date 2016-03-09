package com.raidomatic.model;

import java.util.*;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lombok.*;

import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.forum.model.Topics;

public class Loot extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String topicId;
	private @Getter @Setter String itemId;
	private @Getter @Setter String result;
	private @Getter String characterName;
	private @Getter @Setter String outOfGuildCharacter;
	private @Getter @Setter String lootTierId;
	private @Getter @Setter Date dateTime;
	private @Getter String playerName;
	private @Getter @Setter Date bankRemoval = null;
	private @Getter @Setter String bankRemovalBy;

	private @Getter @Setter List<String> eventLeader;
	private @Getter @Setter String eventLootTier;
	private @Getter @Setter Date eventDate;

	public void setCharacterName(String characterName) throws Exception {
		this.characterName = characterName == null ? "" : characterName;
		if(!this.characterName.equals("")) {
			Character character = this.getCharacter();
			if(character != null) {
				this.setPlayerName(character.getPlayerName());
			}
		} else {
			this.setPlayerName("");
		}
	}
	public void setPlayerName(String playerName) { this.playerName = playerName == null ? "" : playerName; }

	public String getItemName() throws Exception {
		if(this.getItemId() == null || this.getItemId().equals("")) { return ""; }

		return this.getItem().getName();
	}
	public void setItemName(String itemName) throws Exception {
		Items items = new Items();
		Item item = items.getByLookupName(itemName);
		if(item != null) { this.setItemId(item.getId()); }
	}

	public Topic getTopic() throws Exception {
		Topics topics = new Topics();
		return topics.getById(this.getTopicId());
	}
	public Item getItem() throws Exception {
		if(this.getItemId() == null || this.getItemId().equals("")) { return null; }

		Items items = new Items();
		return items.getById(this.getItemId());
	}
	public Character getCharacter() throws Exception {
		if(!this.getCharacterName().equals("")) {
			Characters characters = new Characters();
			return characters.getByName(this.getCharacterName());
		}
		return null;
	}
	public Player getPlayer() {
		if(!this.getPlayerName().equals("")) {
			Players players = new Players();
			return players.getByName(this.getPlayerName());
		}
		return null;
	}
	public LootTier getLootTier() throws Exception {
		LootTiers lootTiers = new LootTiers();
		return lootTiers.getById(this.lootTierId);
	}

	// The EventLeader field is also an authors field, so override the normal editable check to include that
	@Override
	@SuppressWarnings("unchecked")
	public boolean getIsEditable() {
		try {
			if(JSFUtil.getUserName().equalsIgnoreCase("Anonymous")) { return false; }

			Database database = JSFUtil.getDatabase();
			if(database.getCurrentAccessLevel() >= 4) { return true; }
			if(this.getAuthors().contains(JSFUtil.getUserName())) { return true; }
			if(this.getEventLeader().contains(JSFUtil.getUserName())) { return true; }
			List<String> roles = (List<String>)database.queryAccessRoles(JSFUtil.getUserName());
			for(int i = 0; i < roles.size(); i++) {
				if(this.getAuthors().contains(roles.get(i))) { return true; }
			}
		} catch(NotesException ne) { }
		return false;
	}

	public boolean save(boolean asSigner) {
		try {
			//Session session = JSFUtil.getSession();
			Database database = JSFUtil.getDatabase();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Loot");
				doc.makeResponse(this.getTopic().getDocument());
				this.setId(doc.getUniversalID());
				this.setDateTime(Calendar.getInstance().getTime());
			}

			doc.replaceItemValue("LootID", this.getId());
			doc.replaceItemValue("TopicID", this.getTopicId());
			doc.replaceItemValue("ItemID", this.getItemId());
			doc.replaceItemValue("Result", this.getResult());
			doc.replaceItemValue("CharacterName", this.getCharacterName());
			doc.replaceItemValue("OutOfGuildCharacter", this.getOutOfGuildCharacter());
			doc.replaceItemValue("LootTierID", this.getLootTierId());
			doc.replaceItemValue("DateTime", this.createDateTime(this.getDateTime()));
			doc.replaceItemValue("PlayerName", this.getPlayerName());
			if(this.getBankRemoval() == null) {
				doc.replaceItemValue("BankRemoval", "");
			} else {
				doc.replaceItemValue("BankRemoval", this.createDateTime(this.getBankRemoval()));
			}
			doc.replaceItemValue("BankRemovalBy", this.getBankRemovalBy());
			if(this.getItemId() != null && this.getItemId().length() > 0) {
				doc.replaceItemValue("ItemLevel", this.getItem().getLevel());
				doc.replaceItemValue("ItemName", this.getItem().getName());
			}

			this.setEventLootTier(this.getTopic().getEventLootTier());
			this.setEventLeader(this.getTopic().getEventLeader());
			this.setEventDate(this.getTopic().getEventDate());
			doc.replaceItemValue("EventLootTier", this.getEventLootTier());
			doc.replaceItemValue("EventDate", this.createDateTime(this.getEventDate()));

			doc.replaceItemValue("EventLeader", this.getEventLeader());
			lotus.domino.Item eventLeader = doc.getFirstItem("EventLeader");
			JSFUtil.registerProductObject(eventLeader);
			eventLeader.setAuthors(true);

			doc.replaceItemValue("Authors", JSFUtil.getUserName());
			lotus.domino.Item authors = doc.getFirstItem("Authors");
			JSFUtil.registerProductObject(authors);
			for(String leader : this.getTopic().getEventLeader()) {
				authors.appendToTextList(leader);
			}
			authors.appendToTextList("[RaidLeader]");
			authors.appendToTextList("[LootMaster]");
			authors.appendToTextList("[Admin]");
			authors.setAuthors(true);

			doc.computeWithForm(false, false);
			doc.save();
			this.setUniversalId(doc.getUniversalID());
			this.clearCache();

			//			try {
			//				authors.recycle();
			//				eventLeader.recycle();
			//				doc.recycle();
			//			} catch(NotesException ne) { }

		} catch(Exception ne) {
			ne.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean save() { return this.save(false); }

	public void remove() throws Exception {
		Document doc = this.getDocumentAsSigner();
		doc.replaceItemValue("Form", "DeletedLoot");
		doc.save(false, false);
		//		try { doc.recycle(); } catch(NotesException ne) { }
		this.clearCache();
	}

	@Override
	public void clearCache() throws Exception {
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-LootList-.*");
		//		
		//		// Clear everything for now
		//		cache.clear();
		//		
		JSFUtil.getModelIDCache().clear();
	}

	private static final long serialVersionUID = 8117323147002234360L;
}
