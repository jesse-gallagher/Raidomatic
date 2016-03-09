package com.raidomatic.forum.model;

import java.util.*;

import lombok.*;
import lotus.domino.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;
import com.raidomatic.ReadMarkManager;
import com.raidomatic.model.*;

public class Topic extends AbstractModel {
	private @Getter @Setter String id;
	private @Getter @Setter String forumId;
	private @Getter @Setter String title;
	private @Getter @Setter String createdBy;
	private @Getter @Setter boolean sticky;
	private @Getter @Setter boolean locked;
	private @Getter @Setter String latestPostId;
	private @Getter @Setter Date latestPostAt = null;
	private @Getter @Setter String latestPostBy = null;
	private @Getter @Setter Date firstPostAt = null;
	private @Getter @Setter String firstPostBy = null;

	private @Getter @Setter Date eventDate = null;
	private @Getter @Setter String eventType;
	private @Getter @Setter String eventTitle;
	private @Getter @Setter String eventInstance;
	private @Getter @Setter String eventRaidSize;
	private @Getter @Setter int eventGroups = 1;
	private @Getter List<String> eventLeader;
	private @Getter @Setter String eventLootTier;
	private @Getter @Setter String eventLog;
	private @Getter @Setter boolean eventHeroic = false;
	private @Getter @Setter List<String> eventBosses;
	private @Getter @Setter boolean eventAttendanceMarked = false;
	private @Getter @Setter boolean eventLineupSet = false;
	private @Getter @Setter Date enrolledUser = null;

	Map<String, String> applicationResponses;
	String applicationStatus;

	protected Date getDate(Object value) throws NotesException {
		if(value.toString().equals("")) {
			return null;
		}
		return ((DateTime)value).toJavaDate();
	}

	public String getStickyString() { return this.isSticky() + ""; }
	public void setStickyString(String stickyString) { this.setSticky(Boolean.parseBoolean(stickyString)); }

	public boolean isApplication() {
		return this.getApplicationResponses().size() > 0;
	}
	public boolean getIsApplication() { return this.isApplication(); }
	public void setApplicationResponse(String field, String value) {
		this.getApplicationResponses().put(field, value);
	}
	public String getApplicationResponse(String field) {
		return this.getApplicationResponses().get(field);
	}
	public Map<String, String> getApplicationResponses() {
		if(this.applicationResponses == null) {
			this.applicationResponses = new HashMap<String, String>();
		}
		return this.applicationResponses;
	}
	public void setApplicationResponses(Map<String, String> applicationResponses) { this.applicationResponses = applicationResponses; }
	public String getApplicationStatus() {
		return applicationStatus;
	}
	public void setApplicationStatus(String applicationStatus) {
		this.applicationStatus = applicationStatus;
	}

	public Forum getForum() throws Exception {
		Forums forums = new Forums();
		return forums.getById(this.forumId);
	}
	public List<Post> getPosts() {
		Posts postList = new Posts();
		return postList.getPostsForTopicId(this.getId());
	}
	public List<Signup> getSignups() throws Exception {
		Signups signupList = new Signups();
		return signupList.getSignupsForTopicId(this.getId());
	}
	public Collection<Signup> getSignupsSorted() throws Exception {
		Collection<Signup> result = new TreeSet<Signup>(new Signup.CharacterNameComparator());
		result.addAll(this.getSignups());
		return result;
	}
	public List<Loot> getLoots() {
		Loots lootList = new Loots();
		return lootList.getLootsForTopicId(this.getId());
	}

	public List<RaidReport> getRaidReports() {
		RaidReports raidReportList = new RaidReports();
		return raidReportList.getRaidReportsByTopicId(this.getId());
	}

	public Signup getSignup() {
		return this.getSignupForUser(JSFUtil.getUserName());
	}
	public Signup getSignupForUser(String userName) {
		Signups signupList = new Signups();
		List<Signup> potentials = signupList.getSignupsForTopicIdAndPlayerName(this.getId(), userName);
		if(potentials.size() == 0) {
			return null;
		}
		return potentials.get(0);
	}
	public boolean getIsSignedUp() throws NotesException {
		return this.getIsPlayerSignedUp(JSFUtil.getUserName());
	}
	public boolean getIsPlayerSignedUp(String userName) throws NotesException {
		// WARN: this is ugly
		//		Signups signupList = new Signups();
		//		return signupList.getSignupsForTopicIdAndPlayerName(this.getId(), userName).size() != 0;
		Database database = ExtLibUtil.getCurrentDatabase();
		View signups = database.getView("Signups");
		signups.setAutoUpdate(false);
		signups.resortView("$TopicIDAndUsername");
		ViewEntry entry = signups.getEntryByKey(this.getId() + "-" + userName, true);
		boolean isSignedUp = entry != null;
		signups.recycle();
		return isSignedUp;
	}
	public boolean getIsPlayerSignupSelected(String userName) {
		Signups signupList = new Signups();
		List<Signup> signups = signupList.getSignupsForTopicIdAndPlayerName(this.getId(), userName);
		if(signups.size() < 1) {
			return false;
		}
		return signups.get(0).isSelected();
	}
	public boolean getIsSignupSelected() {
		return this.getIsPlayerSignupSelected(JSFUtil.getUserName());
	}
	public boolean getIsEvent() {
		return this.getEventDate() != null && this.getEventType() != "";
	}

	public int getPostCount() throws Exception {
		Posts posts = (Posts)JSFUtil.getVariableValue("Posts");
		return posts.getPostCountForTopicId(this.getId());
	}

	public boolean getCanPlayerSignUp(String userName) throws NotesException {
		return !userName.equalsIgnoreCase("Anonymous")
		&& !this.getIsPlayerSignedUp(userName)
		&& this.getEventDate().after(new Date());
	}
	public boolean getCanSignUp() throws NotesException {
		return this.getCanPlayerSignUp(JSFUtil.getUserName());
	}

	public boolean delete() {
		try {
			if(this.getDocExists()) {
				Document doc = this.getDocument();
				doc.replaceItemValue("Form", "DeletedPost");
				doc.save();

				for(Object post : this.getPosts().toArray()) {
					((Post)post).delete(false);
				}
				for(Object signup : this.getSignups().toArray()) {
					((Signup)signup).remove();
				}
				for(Object loot : this.getLoots().toArray()) {
					((Loot)loot).remove();
				}
				for(Object report : this.getRaidReports().toArray()) {
					((RaidReport)report).remove();
				}

				this.clearCache();
			}
		} catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	// JavaScript is apparently not a fan of using "delete" as a method name
	public boolean remove() { return this.delete(); }

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

	public synchronized boolean save(boolean asSigner) {
		try {
			Session session = JSFUtil.getSession();
			Database database = JSFUtil.getDatabase();
			database = JSFUtil.getSessionAsSigner().getDatabase(database.getServer(), database.getFilePath());
			JSFUtil.registerProductObject(database);
			com.raidomatic.Configuration config = JSFUtil.getConfiguration();

			Document doc;
			if(this.getDocExists()) {
				doc = asSigner ? this.getDocumentAsSigner() : this.getDocument();
			} else {
				doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				System.out.println("getUniversalID in Topic.save");
				this.setUniversalId(doc.getUniversalID());
				this.setId(doc.getUniversalID());
				String userName = JSFUtil.getUserName();
				this.setCreatedBy(userName.equalsIgnoreCase("Anonymous") ? (String)config.getDocumentItemValue("Forum Options", "ForumPostNamelessPostsAs").get(0) : userName);
			}
			doc.replaceItemValue("TopicID", this.getId());
			doc.replaceItemValue("Form", "Topic");
			doc.replaceItemValue("ForumID", this.getForumId());
			doc.replaceItemValue("Sticky", this.isSticky() ? 1 : 0);
			doc.replaceItemValue("Locked", this.isLocked() ? 1 : 0);
			doc.replaceItemValue("CreatedBy", this.getCreatedBy());
			doc.replaceItemValue("Title", this.getTitle());
			if(this.getEventDate() != null) {
				doc.replaceItemValue("EventDate", database.getParent().createDateTime(this.getEventDate()));
			} else {
				doc.replaceItemValue("EventDate", "");
			}
			doc.replaceItemValue("EventType", this.getEventType());
			doc.replaceItemValue("EventTitle", this.getEventTitle());
			doc.replaceItemValue("EventInstance", this.getEventInstance());
			doc.replaceItemValue("EventGroups", this.getEventGroups());
			doc.replaceItemValue("EventLog", this.getEventLog());
			doc.replaceItemValue("EventHeroic", this.isEventHeroic() ? 1 : 0);
			doc.replaceItemValue("EventRaidSize", this.getEventRaidSize());

			doc.replaceItemValue("EventLineupSet", this.isEventLineupSet() ? 1 : 0);
			doc.replaceItemValue("EventAttendanceMarked", this.isEventAttendanceMarked() ? 1 : 0);

			boolean updateChildren = false;
			// These two fields are copied down to signups, so check to see if they really changed and, if so, re-save the signups
			if(this.getDocExists() && (
					!doc.getItemValue("EventLeader").equals(this.getEventLeader()) ||
					!doc.getItemValueString("EventLootTier").equals(this.getEventLootTier())
			)) {
				doc.replaceItemValue("EventLeader", this.getEventLeader());
				lotus.domino.Item eventLeaderItem = doc.getFirstItem("EventLeader");
				JSFUtil.registerProductObject(eventLeaderItem);
				eventLeaderItem.setAuthors(true);
				//eventLeaderItem.recycle();
				doc.replaceItemValue("EventLootTier", this.getEventLootTier());
				updateChildren = true;
			} else if(!this.getDocExists()) {
				doc.replaceItemValue("EventLeader", this.getEventLeader());
				doc.replaceItemValue("EventLootTier", this.getEventLootTier());
			}

			doc.replaceItemValue("FirstPostBy", this.getFirstPostBy());
			DateTime firstPostDateTime;
			if(this.getFirstPostAt() != null) {
				firstPostDateTime = this.createDateTime(this.getFirstPostAt());
			} else {
				List<Post> posts = this.getPosts();
				try {
					firstPostDateTime = this.createDateTime(posts.get(0).getDateTime());
				} catch(Exception e) {
					firstPostDateTime = this.createDateTime(new Date());
				}
			}
			doc.replaceItemValue("FirstPostAt", firstPostDateTime);

			doc.replaceItemValue("LatestPostBy", this.getLatestPostBy());
			doc.replaceItemValue("LatestPostID", this.getLatestPostId());
			if(this.getLatestPostAt() != null) {
				DateTime latestPostDateTime = this.createDateTime(this.getLatestPostAt());
				JSFUtil.registerProductObject(latestPostDateTime);
				doc.replaceItemValue("LatestPostAt", latestPostDateTime);
			}


			for(String appField : this.getApplicationResponses().keySet()) {

				if(appField.equals("DesiredPassword")) {
					if(!(doc.getItemValueString("App_DesiredPassword").startsWith("(") && doc.getItemValueString("App_DesiredPassword").endsWith(")"))) {
						doc.replaceItemValue("App_DesiredPassword", session.evaluate(" @Password(App_DesiredPassword) ", doc));
					}
				} else {
					doc.replaceItemValue("App_" + appField, this.getApplicationResponse(appField));
				}
			}
			doc.replaceItemValue("ApplicationStatus", this.getApplicationStatus());

			doc.replaceItemValue("Authors", this.generateAuthorList());
			doc.getFirstItem("Authors").setAuthors(true);


			// If this is a guild app, we're Anonymous but want to post in a restricted forum, so break the rules a lot
			Document forumDoc = null;
			if(this.getApplicationResponses().size() > 0) {
				View forums = database.getView("Forums");
				JSFUtil.registerProductObject(forums);
				forums.resortView();
				forumDoc = forums.getDocumentByKey(this.getForumId());
				JSFUtil.registerProductObject(forumDoc);
			} else {
				forumDoc = this.getForum().getDocument();
			}
			forumDoc.getFirstItem("Readers").copyItemToDocument(doc);
			forumDoc.getFirstItem("ReadersFailsafe").copyItemToDocument(doc);

			doc.computeWithForm(false, false);

			doc.makeResponse(forumDoc);

			doc.save(true);

			this.clearCache();

			if(updateChildren) {
				for(Signup signup : this.getSignups()) {
					signup.setEventLeader(this.getEventLeader());
					signup.setEventLootTier(this.getEventLootTier());
					signup.save(asSigner);
				}
				for(Loot loot : this.getLoots()) {
					loot.setEventLeader(this.getEventLeader());
					loot.setEventLootTier(this.getEventLootTier());
					loot.setEventDate(this.getEventDate());
					loot.save(asSigner);
				}
				for(RaidReport report : this.getRaidReports()) {
					report.setEventLeader(this.getEventLeader());
					report.save(asSigner);
				}
			}
		} catch(Exception ne) {
			ne.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean save() { return this.save(false); }

	public boolean moveToForum(Forum newForum) throws Exception {
		if(!newForum.getId().equals(this.getForumId())) {
			this.setForumId(newForum.getId());
			this.save();
			for(Post post : this.getPosts()) {
				post.setForumId(newForum.getId());

				// Save the post as the current user but without creating versions
				post.save(false, true);
			}
			return true;
		}
		return false;
	}

	@Override
	public void clearCache() throws Exception {
		//		Forum forum = this.getForum();
		//		String categoryId = forum.getCategoryId();
		//		
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-ForumList-BY_ID-" + this.getForumId());
		//		cache.clearMatches(".*-PostList-BY_TOPIC_ID-" + this.getId());
		//		cache.clearMatches(".*-CategoryList-BY_ID-" + categoryId);
		//		cache.clearMatches(".*-TopicList-BY_ID-" + this.getId());
		//		cache.clearMatches(".*-TopicList-BY_LATEST_POST_AT_DESC");
		//		cache.clearMatches(".*-TopicList-BY_FORUM_ID-" + this.getForumId());
		//		cache.clearMatches(".*-TopicList-BY_EVENT_DAY-.*");
		//		
		//		// Clear out any aggregate collections that may contain this object
		//		cache.clearMatches(".*-TopicList-.*-Search-.*");
		//		cache.clearMatches(".*-TopicList-[^\\-]*");
		//		
		//		// Clear everything for now
		//		cache.clear();
		//		
		JSFUtil.getModelIDCache().clear();

		ReadMarkManager manager = JSFUtil.getReadMarkManager();
		manager.clearMarks(this.getId());
		manager.clearMarks(this.getForumId());

		manager.markRead(this.getId());
		manager.markRead(this.getForumId());
	}

	public String getEventGroupsText() { return eventGroups + ""; }
	public void setEventGroupsText(String eventGroupsText) {
		this.eventGroups = Integer.parseInt(eventGroupsText);
	}

	public void setEventLeader(List<String> eventLeader) {
		this.eventLeader = eventLeader;
	}
	public void setEventLeader(String eventLeader) {
		List<String> result = new Vector<String>();
		if(eventLeader.length() > 0) result.add(eventLeader);
		this.setEventLeader(result);
	}
	public void addEventLeader(String eventLeader) {
		if(this.eventLeader == null) {
			this.eventLeader = new Vector<String>();
		}
		if(!this.eventLeader.contains(eventLeader)) {
			this.eventLeader.add(eventLeader);
		}
	}
	public void removeEventLeader(String eventLeader) {
		if(this.eventLeader != null) {
			this.eventLeader.remove(eventLeader);
		}
	}

	public String getEventHeroicText() { return eventHeroic + ""; }
	public void setEventHeroicText(String eventHeroicText) {
		this.eventHeroic = Boolean.parseBoolean(eventHeroicText);
	}

	public String getEventAttendanceMarkedText() { return eventAttendanceMarked + ""; }
	public void setEventAttendanceMarkedText(String eventAttendanceMarkedText) {
		this.eventAttendanceMarked = Boolean.parseBoolean(eventAttendanceMarkedText);
	}
	public String getEventLineupSetText() { return eventLineupSet + ""; }
	public void setEventLineupSetText(String eventLineupSetText) {
		this.eventLineupSet = Boolean.parseBoolean(eventLineupSetText);
	}

	private List<String> generateAuthorList() {
		List<String> result = new Vector<String>();

		result.add(this.getCreatedBy());
		result.add("[GeneralEditor]");
		result.add("[ForumEditor]");
		result.add("[Admin]");
		if(this.getEventType().equals("Raid")) {
			result.add("[RaidLeader]");
		}

		return result;
	}

	public void updateLatestPost() {
		List<Post> posts = this.getPosts();
		Post post = posts.get(posts.size()-1);
		this.setLatestPostAt(post.getDateTime());
		this.setLatestPostBy(post.getCreatedBy());
		this.setLatestPostId(post.getId());
	}
	public List<Boss> getBosses() throws Exception {
		List<Boss> bosses = new Vector<Boss>();
		Bosses bossList = new Bosses();
		for(String bossId : this.getEventBosses()) {
			bosses.add(bossList.getById(bossId));
		}
		return bosses;
	}

	public Post getLatestPost() {
		//return this.getPosts().get(this.getPosts().size()-1);
		Posts posts = new Posts();
		return posts.getLatestPostForTopicId(this.getId());
	}
	public Post getCatchupPost() throws Exception {
		List<Post> posts = this.getPosts();
		for(int i = 0; i < posts.size(); i++) {
			if(!posts.get(i).isRead()) {
				return posts.get(i);
			}
		}
		return this.getLatestPost();
	}


	public boolean enrollUser() throws Exception {
		if(this.getEnrolledUser() != null) {
			System.out.println("User is already enrolled.");
			return false;
		}

		// Open up the names database
		Session session = JSFUtil.getSessionAsSigner();
		Database names = session.getDatabase("", "wownames.nsf");
		JSFUtil.registerProductObject(names);

		String desiredUsername = this.getApplicationResponse("DesiredUsername");
		//System.out.println("Using username: " + desiredUsername);

		// First, check to make sure the user doesn't already exist
		View nameLookup = names.getView("$NamesFieldLookup");
		JSFUtil.registerProductObject(nameLookup);
		nameLookup.setAutoUpdate(false);
		ViewEntry existingEntry = nameLookup.getEntryByKey(desiredUsername, true);
		if(existingEntry != null) {
			JSFUtil.registerProductObject(existingEntry);
			System.out.println("Username already in use.");
			return false;
		}

		// Ok, so it's a legitimate new user, so it's time to create the user document
		Document user = names.createDocument();
		JSFUtil.registerProductObject(user);
		user.replaceItemValue("Form", "Person");
		user.replaceItemValue("Type", "Person");
		user.replaceItemValue("LastName", desiredUsername);
		user.replaceItemValue("ShortName", desiredUsername);
		user.replaceItemValue("FullName", desiredUsername);
		user.replaceItemValue("DisplayName", desiredUsername);
		user.replaceItemValue("HTTPPassword", this.getApplicationResponse("DesiredPassword"));
		user.computeWithForm(false, false);
		user.save();

		// Try saving a second time to re-compute the UIDNumber and GIDNumber fields, which are based on Note ID
		user.computeWithForm(false, false);
		user.save();

		// If we're still here, that means we have a user properly created, so now it's time to add them to the Risen group
		Document userGroup = nameLookup.getDocumentByKey("Risen", true);
		if(userGroup != null) {
			JSFUtil.registerProductObject(userGroup);

			lotus.domino.Item groupUsers = userGroup.getFirstItem("Members");
			JSFUtil.registerProductObject(groupUsers);
			groupUsers.appendToTextList(desiredUsername);

			userGroup.save();
		}

		// This should theoretically tell the server to look for the new user, but who knows if it actually works?
		// No one, that's who.
		session.sendConsoleCommand(names.getServer(), "show nlcache reset");


		// Let's try making a mail file
		String mailDBName = "wowmail/" + desiredUsername.toLowerCase() + ".nsf";
		Database mailDB = session.getDatabase("", mailDBName);
		JSFUtil.registerProductObject(mailDB);
		if(!mailDB.isOpen()) {
			Database template = session.getDatabase("", "mail85.ntf");
			JSFUtil.registerProductObject(template);
			mailDB = template.createFromTemplate(template.getServer(), mailDBName, true);
			JSFUtil.registerProductObject(mailDB);
			mailDB.setTitle(desiredUsername);

			// Add the user to the ACL
			ACL acl = mailDB.getACL();
			JSFUtil.registerProductObject(acl);
			ACLEntry entry = acl.createACLEntry(desiredUsername, ACL.LEVEL_EDITOR);
			JSFUtil.registerProductObject(entry);
			entry.setUserType(ACLEntry.TYPE_PERSON);
			entry.setCanCreatePersonalFolder(true);
			entry.setCanCreateSharedFolder(true);
			entry.setCanDeleteDocuments(true);
			acl.save();


			// If it all worked, update the user
			user.replaceItemValue("MailServer", mailDB.getServer());
			user.replaceItemValue("MailFile", mailDB.getFilePath());
			user.save();
		}

		// Update the topic document with the fact that the user was enrolled
		this.setEnrolledUser(new Date());
		this.save();

		//System.out.println("Enrolled user");

		return true;
	}

	private static final long serialVersionUID = 5123445164006352398L;
}
