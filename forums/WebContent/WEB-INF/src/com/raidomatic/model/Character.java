package com.raidomatic.model;

import java.util.List;
import java.util.Vector;
import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.NotesException;
import lombok.*;

import com.raidomatic.JSFUtil;
import com.raidomatic.bnet.wow.*;

public class Character extends AbstractModel {
	private @Getter @Setter String id;

	private @Getter @Setter String name;
	private @Getter @Setter List<String> alias;
	private @Getter @Setter String playerName;
	private @Getter @Setter String className;
	private @Getter @Setter int classId;
	private @Getter @Setter String focus;
	private @Getter @Setter int raceId;
	private @Getter @Setter int genderId;
	private @Getter @Setter String preferredSpec;
	private @Getter @Setter int level;

	private @Getter @Setter String primarySpec;
	private @Getter @Setter String primaryRole;
	private @Getter @Setter String primaryGroup;
	private @Getter @Setter boolean primaryProgression;
	private @Getter @Setter List<String> primaryProvides;
	private @Getter @Setter List<String> primarySometimesProvides;

	private @Getter @Setter String secondarySpec;
	private @Getter @Setter String secondaryRole;
	private @Getter @Setter String secondaryGroup;
	private @Getter @Setter boolean secondaryProgression;
	private @Getter @Setter List<String> secondaryProvides;
	private @Getter @Setter List<String> secondarySometimesProvides;

	private @Getter @Setter String avatarPath;

	public List<String> getAvailableRoles() {
		List<String> roles = new Vector<String>();
		roles.add(this.getPrimaryRole());
		if(!this.getSecondaryRole().equals("") && !roles.contains(this.getSecondaryRole())) {
			roles.add(this.getSecondaryRole());
		}
		return roles;
	}

	public Player getPlayer() {
		Players players = new Players();
		return players.getByName(this.getPlayerName());
	}
	public List<Signup> getSignups() {
		Signups signups = new Signups();
		return signups.getSignupsForCharacterName(this.getName());
	}
	public List<Loot> getLoots() {
		Loots loots = new Loots();
		return loots.getLootsForCharacterName(this.getName());
	}

	public Spec getPrimarySpecObject() {
		if(this.getPrimarySpec().equals("")) { return null; }
		Specs specs = new Specs();
		return specs.getBySignature(this.getClassName() + " - " + this.getPrimarySpec());
	}
	public Spec getSecondarySpecObject() {
		if(this.getSecondarySpec().equals("")) { return null; }
		Specs specs = new Specs();
		return specs.getBySignature(this.getClassName() + " - " + this.getSecondarySpec());
	}

	public boolean updateFromArmory() {
		try {
			API api = new API();
			com.raidomatic.bnet.wow.Character apiChar = api.getCharacter("Thorium Brotherhood", this.getName());

			this.setClassId(apiChar.getClassId());
			this.setRaceId(apiChar.getRaceId());
			this.setGenderId(apiChar.getGenderId());
			this.setLevel(apiChar.getLevel());
			this.setClassName(this.getClassNameForClassId(this.getClassId()));

			TalentSpec primary = apiChar.getPrimaryTalentSpec();
			if(primary != null) {
				this.setPrimarySpec(primary.getName());
				//this.setPrimaryTalents(primary.getBuild());
				this.setPrimaryRole(primary.getRole());
			}
			TalentSpec secondary = apiChar.getSecondaryTalentSpec();
			if(secondary != null) {
				this.setSecondarySpec(secondary.getName());
				//this.setSecondaryTalents(secondary.getBuild());
				this.setSecondaryRole(secondary.getRole());
			}
			return true;
		} catch(Exception e) { System.out.println(e.toString()); }
		return false;
	}

	public boolean save(boolean asSigner) {
		try {
			Database database = this.getDatabase();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Character");
				this.setId(doc.getUniversalID());
			}

			doc.replaceItemValue("Name", this.getName());
			doc.replaceItemValue("RealmName", "Thorium Brotherhood");
			doc.replaceItemValue("Alias", this.getAlias());
			doc.replaceItemValue("Player", this.getPlayer().getName());
			doc.replaceItemValue("PrimaryGroup", this.getPrimaryGroup());
			doc.replaceItemValue("PrimarySpec", this.getPrimarySpec());
			doc.replaceItemValue("SecondaryGroup", this.getSecondaryGroup());
			doc.replaceItemValue("SecondarySpec", this.getSecondarySpec());
			doc.replaceItemValue("Level", this.getLevel());
			doc.replaceItemValue("ClassID", this.getClassId());
			if(doc.hasItem("Class")) { doc.removeItem("Class"); } // This will be re-computed
			doc.replaceItemValue("RaceID", this.getRaceId());
			doc.replaceItemValue("GenderID", this.getGenderId());

			doc.replaceItemValue("Authors", "[Admin]");
			lotus.domino.Item authors = doc.getFirstItem("Authors");
			JSFUtil.registerProductObject(authors);
			authors.setAuthors(true);

			doc.computeWithForm(false, false);
			doc.save();
			this.setUniversalId(doc.getUniversalID());
			this.clearCache();

		} catch(Exception ne) {
			ne.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean save() { return this.save(false); }

	@Override
	public Database getDatabase() throws NotesException {
		Database database = JSFUtil.getSession().getDatabase("", "wow/common.nsf");
		JSFUtil.registerProductObject(database);
		return database;
	}

	private String getClassNameForClassId(int classId) {
		switch(classId) {
		case 1: return "Warrior";
		case 2: return "Paladin";
		case 3: return "Hunter";
		case 4: return "Rogue";
		case 5: return "Priest";
		case 6: return "Death Knight";
		case 7: return "Shaman";
		case 8: return "Mage";
		case 9: return "Warlock";
		case 10: return "Monk";
		case 11: return "Druid";
		}
		return "";
	}

	private static final long serialVersionUID = -1537097403220544265L;
}
