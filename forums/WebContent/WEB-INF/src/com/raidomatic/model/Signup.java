package com.raidomatic.model;

import java.util.*;

import lotus.domino.Database;
import lotus.domino.Document;
import lotus.domino.Item;
import lotus.domino.NotesException;
import lombok.*;

import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;
import com.raidomatic.forum.model.Topic;
import com.raidomatic.forum.model.Topics;

public class Signup extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String topicId;
	private @Getter @Setter String playerName;
	private @Getter @Setter String createdBy;

	private @Getter @Setter String preferredCharacter;
	private @Getter @Setter String preferredRole;
	private @Getter @Setter String status;

	private @Getter @Setter Date dateTime;
	private @Getter boolean canceled = false;

	private @Getter @Setter boolean selected = false;
	private String assignedCharacter;
	private String assignedRole;
	private int group = 1;
	private int attended = 0;
	private @Getter @Setter String signupNotes;

	private @Getter @Setter List<String> eventLeader;
	private @Getter @Setter String eventLootTier;
	private @Getter @Setter Date eventDate;

	public LootTier getLootTier() throws Exception {
		LootTiers tiers = new LootTiers();
		return tiers.getById(this.eventLootTier);
	}

	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
		if(canceled) { this.setSelected(false); }
	}


	public String getSelectedText() { return selected + ""; }
	public void setSelectedText(String selectedText) {
		this.selected = Boolean.parseBoolean(selectedText);
	}

	public String getAssignedCharacter() {
		return assignedCharacter == null || assignedCharacter.length() < 1 ? this.getPreferredCharacter() : assignedCharacter;
	}
	public void setAssignedCharacter(String assignedCharacter) throws Exception {
		if(!assignedCharacter.equals(this.assignedCharacter)) {
			this.assignedCharacter = assignedCharacter;
			// Make sure that the signup ends up with a valid role/character combination
			if(!this.getCharacter().getAvailableRoles().contains(assignedRole)) {
				// Set the field directly, since .setAssignedRole() will have a similar sanity check
				this.assignedRole = this.getCharacter().getPrimaryRole();
			}
		}
	}
	public String getAssignedRole() {
		return assignedRole == null || assignedRole.length() < 1 ? this.getPreferredRole() : assignedRole;
	}
	public void setAssignedRole(String assignedRole) throws Exception {
		if(!assignedRole.equals(this.assignedRole)) {
			this.assignedRole = assignedRole;
			// Make sure that the signup ends up with a valid role/character combination
			if(!this.getCharacter().getAvailableRoles().contains(assignedRole)) {
				for(Character character : this.getPlayer().getCharacters()) {
					if(character.getAvailableRoles().contains(assignedRole)) {
						// Set the field directly, since .setAssignedCharacter() will have a similar sanity check
						this.assignedCharacter = character.getName();
						break;
					}
				}
			}
		}
	}
	public int getGroup() { return group; }
	public void setGroup(int group) { this.group = group; }
	public String getGroupText() { return group + ""; }
	public void setGroupText(String groupText) { this.group = Integer.parseInt(groupText); }

	public int getAttended() { return attended; }
	public void setAttended(int attended) { this.attended = attended; }
	public String getAttendedText() {
		return attended == 1 ? "true" :
			attended == -1 ? "no-show" :
				"false";
	}
	public void setAttendedText(String attendedText) {
		this.attended = attendedText.equals("true") ? 1 : attendedText.equals("no-show") ? -1 : 0;
	}

	public Topic getTopic() throws Exception {
		Topics topics = new Topics();
		return topics.getById(this.topicId);
	}
	public Player getPlayer() {
		Players players = new Players();
		return players.getByName(this.getPlayerName());
	}
	public Character getCharacter() throws Exception {
		Characters characters = new Characters();
		if(this.assignedCharacter == null || this.assignedCharacter.equals("")) {
			return characters.getByName(this.getPreferredCharacter());
		} else {
			return characters.getByName(this.getAssignedCharacter());
		}
	}
	public Map<String, String> getSpec() throws Exception {
		Map<String, String> spec = new HashMap<String, String>();
		Character character = this.getCharacter();
		if(character.getPrimaryRole().equals(this.getAssignedRole())) {
			spec.put("name", character.getPrimarySpec());
			spec.put("group", character.getPrimaryGroup());
		} else {
			spec.put("name", character.getSecondarySpec());
			spec.put("group", character.getSecondaryGroup());
		}
		return spec;
	}
	public Spec getSpecObject() throws Exception {
		Character character = this.getCharacter();
		if(character.getPrimaryRole().equals(this.getAssignedRole())) {
			return character.getPrimarySpecObject();
		} else {
			return character.getSecondarySpecObject();
		}
	}
	public List<String> getAvailableRoles() throws Exception {
		List<String> roles = new Vector<String>();
		for(Character character : this.getPlayer().getCharacters()) {
			if(!roles.contains(character.getPrimaryRole())) {
				roles.add(character.getPrimaryRole());
			}
			if(!character.getSecondaryRole().equals("") && !roles.contains(character.getSecondaryRole())) {
				roles.add(character.getSecondaryRole());
			}
		}
		return roles;
	}
	public String getRaidGroup() throws Exception {
		Character character = this.getCharacter();
		if(character.getPrimaryRole().equals(this.getAssignedRole())) {
			return character.getPrimaryGroup();
		} else {
			return character.getSecondaryGroup();
		}
	}
	public List<String> getProvidedBuffs() throws Exception {
		Character character = this.getCharacter();
		if(character.getPrimaryRole().equals(this.getAssignedRole())) {
			return character.getPrimaryProvides();
		} else {
			return character.getSecondaryProvides();
		}
	}
	public List<String> getSometimesProvidedBuffs() throws Exception {
		Character character = this.getCharacter();
		if(character.getPrimaryRole().equals(this.getAssignedRole())) {
			return character.getPrimarySometimesProvides();
		} else {
			return character.getSecondarySometimesProvides();
		}
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

	synchronized public boolean save(boolean asSigner) {
		try {
			//Session session = JSFUtil.getSession();
			Database database = JSFUtil.getDatabase();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Signup");
				doc.makeResponse(this.getTopic().getDocument());
				this.setDateTime(Calendar.getInstance().getTime());
				this.setCreatedBy(JSFUtil.getUserName());
				this.setUniversalId(doc.getUniversalID());
				this.setId(this.getUniversalId());
			}


			doc.replaceItemValue("SignupID", this.getId());
			doc.replaceItemValue("TopicID", this.getTopicId());
			doc.replaceItemValue("CreatedBy", this.getCreatedBy());
			doc.replaceItemValue("DateTime", this.createDateTime(this.getDateTime()));
			doc.replaceItemValue("Canceled", this.isCanceled() ? 1 : 0);
			doc.replaceItemValue("Username", this.getPlayerName());
			doc.replaceItemValue("PreferredCharacter", this.getPreferredCharacter());
			doc.replaceItemValue("PreferredRole", this.getPreferredRole());
			doc.replaceItemValue("Status", this.getStatus());
			doc.replaceItemValue("Selected", this.isSelected() ? 1 : 0);
			doc.replaceItemValue("AssignedCharacter", this.getAssignedCharacter());
			doc.replaceItemValue("AssignedRole", this.getAssignedRole());
			doc.replaceItemValue("Group", this.getGroup());
			doc.replaceItemValue("Attended", this.getAttended());
			doc.replaceItemValue("SignupNotes", this.getSignupNotes());
			if(this.getEventDate() != null) {
				doc.replaceItemValue("EventDate", this.createDateTime(this.getEventDate()));
			} else {
				doc.replaceItemValue("EventDate", "");
			}


			this.setEventLootTier(this.getTopic().getEventLootTier());
			this.setEventLeader(this.getTopic().getEventLeader());
			doc.replaceItemValue("EventLootTier", this.getEventLootTier());

			doc.replaceItemValue("EventLeader", this.getEventLeader());
			Item eventLeader = doc.getFirstItem("EventLeader");
			JSFUtil.registerProductObject(eventLeader);
			eventLeader.setAuthors(true);

			Item createdBy = doc.getFirstItem("CreatedBy");
			JSFUtil.registerProductObject(createdBy);
			createdBy.setAuthors(true);
			Item userName = doc.getFirstItem("Username");
			JSFUtil.registerProductObject(userName);
			userName.setAuthors(true);

			doc.computeWithForm(false, false);

			doc.save(true, false);

			//this.setId(doc.getUniversalID());
			//this.setUniversalId(doc.getUniversalID());
			this.clearCache();

			//			try {
			//				userName.recycle();
			//				createdBy.recycle();
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

	public boolean remove() {
		try {
			if(this.getDocExists()) {
				Document doc = this.getDocument();
				doc.replaceItemValue("Form", "DeletedSignup");
				doc.save();
				this.clearCache();
			}
		} catch(Exception e) { return false; }
		return true;
	}

	@Override
	public void clearCache() throws Exception {
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-SignupList-.*");
		//		cache.clearMatches(".*-TopicList-.*");
		//		
		//		// Clear everything for now
		//		cache.clear();

		JSFUtil.getModelIDCache().clear();

		//JSFUtil.getDatabase().getView("Signups").refresh();
	}

	private static final long serialVersionUID = -2240807359840346251L;

	public static class CharacterNameComparator implements Comparator<Signup> {

		@Override
		@SneakyThrows
		public int compare(Signup arg0, Signup arg1) {
			return arg0.getCharacter().getName().toLowerCase().compareTo(arg1.getCharacter().getName().toLowerCase());
		}

	}
}