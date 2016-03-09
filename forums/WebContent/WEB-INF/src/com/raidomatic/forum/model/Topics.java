package com.raidomatic.forum.model;

import com.raidomatic.JSFUtil;
import com.raidomatic.model.*;
import java.util.*;
import lotus.domino.*;
import java.io.Serializable;

public class Topics extends AbstractCollectionManager<Topic> {

	public List<Topic> getTopics() throws Exception {
		return this.getAll();
	}

	public List<Topic> getTopicsForForumId(String forumId) {
		return this.getCollection(TopicList.BY_FORUM_ID, forumId);
	}

	public List<Topic> getTopicsByLatestPostAtDesc() {
		return this.getCollection(TopicList.BY_LATEST_POST_AT_DESC, null, null, 15);
	}
	public List<Topic> getTopicsByLatestPostAtDescNew() {
		return new TopicsByLatestPostAtDesc();
	}

	public List<Topic> getTopicsForEventDay(Date day) throws NotesException {
		return this.getCollection(TopicList.BY_EVENT_DAY, JSFUtil.getSession().createDateTime(day));
	}
	public List<Topic> getEvents() throws NotesException {
		return this.getCollection("[EventDate] is present", TopicList.BY_EVENT_DATE);
	}

	public List<Topic> getApplications() throws NotesException {
		return this.getCollection(TopicList.BY_IS_APPLICATION, 1);
	}

	@Override
	protected AbstractDominoList<Topic> createCollection() { return new TopicList(); }

	private static final long serialVersionUID = -905477665030064095L;


	public class TopicList extends AbstractDominoList<Topic> {
		public static final String BY_ID = "TopicID";
		public static final String BY_TITLE = "Title";
		public static final String BY_FORUM_ID = "ForumID";
		public static final String BY_LATEST_POST_AT_DESC = "LatestPostAt-desc";
		public static final String BY_EVENT_DATE = "EventDate";
		public static final String BY_EVENT_DAY = "$EventDay";
		public static final String BY_IS_APPLICATION = "$IsApplication";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "sticky", "latestPostAt", "forumId", "title", "eventDate", "", "", "", ""}; }
		@Override
		protected String getDefaultSort() { return TopicList.BY_ID; }
		@Override
		protected String getViewName() { return "Topics"; }

		@Override
		@SuppressWarnings("unchecked")
		protected Topic createObjectFromViewEntry(DominoEntryWrapper entry) throws Exception {
			Topic topic = super.createObjectFromViewEntry(entry);
			Vector values = entry.getColumnValues();

			List<String> appFields = JSFUtil.toStringList(values.get(8));
			Document topicDoc = entry.getDocument();
			for(String appField : appFields) {
				if(!appField.equals("")) {
					topic.setApplicationResponse(appField, topicDoc.getItemValueString("App_" + appField));
				}
			}

			topicDoc.setPreferJavaDates(true);

			topic.setCreatedBy(topicDoc.getItemValueString("CreatedBy"));
			topic.setLatestPostBy(topicDoc.getItemValueString("LatestPostBy"));
			topic.setLatestPostId(topicDoc.getItemValueString("LatestPostID"));
			topic.setReaders(JSFUtil.toStringList(topicDoc.getItemValue("Readers")));
			topic.setAuthors(JSFUtil.toStringList(topicDoc.getItemValue("Authors")));
			topic.setEventType(topicDoc.getItemValueString("EventType"));
			topic.setEventTitle(topicDoc.getItemValueString("EventTitle"));
			topic.setEventInstance(topicDoc.getItemValueString("EventInstance"));
			topic.setEventLeader(topicDoc.getItemValue("EventLeader"));
			topic.setEventGroups((new Double(topicDoc.getItemValueDouble("EventGroups"))).intValue());
			topic.setEventLootTier(topicDoc.getItemValueString("EventLootTier"));
			topic.setEventHeroic(topicDoc.getItemValueDouble("EventHeroic") == 1);
			topic.setEventRaidSize(topicDoc.getItemValueString("EventRaidSize"));
			topic.setLocked(topicDoc.getItemValueDouble("Locked") == 1);
			topic.setEventBosses(JSFUtil.toStringList(topicDoc.getItemValue("EventBosses")));
			topic.setEventLineupSet(topicDoc.getItemValueDouble("EventLineupSet") == 1);
			topic.setEventAttendanceMarked(topicDoc.getItemValueDouble("EventAttendanceMarked") == 1);
			topic.setApplicationStatus(topicDoc.getItemValueString("ApplicationStatus"));

			topic.setFirstPostAt(((List<DateTime>)topicDoc.getItemValue("FirstPostAt")).get(0).toJavaDate());
			topic.setFirstPostBy(topicDoc.getItemValueString("FirstPostBy"));

			try {
				List<DateTime> dateTimes = topicDoc.getItemValue("EnrolledUser");
				if(dateTimes.size() > 0) {
					topic.setEnrolledUser(dateTimes.get(0).toJavaDate());
				}
			} catch(NotesException ne) {
				// This should probably be "Item value is not a date time"
				// Regardless, if we have a problem here, just don't bother filling in the enrolled date
			}


			return topic;
		}
		protected Date getDate(Object value) throws NotesException {
			if(value.toString().equals("")) {
				return null;
			}
			return ((DateTime)value).toJavaDate();
		}

		private static final long serialVersionUID = 4833653738174950549L;
	}
}

class TopicsForForumIdd extends AbstractList<Topic> implements Serializable {
	String forumId;
	transient List<String> cache;
	transient Set<String> usedIds;
	transient Topics topics;

	public TopicsForForumIdd(String forumId) {
		this.forumId = forumId;

		this.initCache();
	}

	@Override
	public Topic get(int arg0) {
		try {
			return topics.getById(this.cache.get(arg0));
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public int size() {
		return this.cache.size();
	}

	@SuppressWarnings("unchecked")
	private void initCache() {
		this.cache = new Vector<String>();
		this.usedIds = new HashSet<String>();
		this.topics = new Topics();

		//int stickyCount = 0;

		try {
			Database database = JSFUtil.getDatabase();

			View topicsView = database.getView("Topics");
			JSFUtil.registerProductObject(topicsView);
			topicsView.setAutoUpdate(false);
			topicsView.resortView("ForumID", true);


			ViewEntryCollection topicsNav = topicsView.getAllEntriesByKey(this.forumId, true);
			JSFUtil.registerProductObject(topicsNav);
			ViewEntry topicEntry = topicsNav.getFirstEntry();
			while(topicEntry != null) {
				JSFUtil.registerProductObject(topicEntry);
				Vector values = topicEntry.getColumnValues();
				if((Double)values.get(1) != 1) {
					//topicEntry.recycle();
					break;
				}

				//System.out.println("Adding sticky topic " + (String)values.get(0));

				this.cache.add((String)values.get(0));
				this.usedIds.add((String)values.get(0));
				//stickyCount++;

				//ViewEntry tempEntry = topicEntry;
				topicEntry = topicsNav.getNextEntry(topicEntry);
				//tempEntry.recycle();
			}
			//topicsNav.recycle();
			//topicsView.recycle();


			View postsView = database.getView("Posts");
			JSFUtil.registerProductObject(postsView);
			postsView.setAutoUpdate(false);
			postsView.resortView("ForumID", true);

			ViewNavigator postsNav = postsView.createViewNavFromCategory(this.forumId);
			JSFUtil.registerProductObject(postsNav);
			//System.out.println("Found " + postsNav.getCount() + " posts for forum ID " + this.forumId);
			ViewEntry postEntry = postsNav.getFirst();
			while(postEntry != null) {
				JSFUtil.registerProductObject(postEntry);
				Vector values = postEntry.getColumnValues();

				if(!((String)values.get(5)).equals(this.forumId)) {
					postEntry.recycle();
					break;
				}

				String postTopicId = (String)values.get(1);

				if(!this.usedIds.contains(postTopicId)) {
					//System.out.println("Adding topic " + postTopicId + " from post " + values.get(0));

					this.cache.add(postTopicId);
					this.usedIds.add(postTopicId);
				}

				//ViewEntry tempEntry = postEntry;
				postEntry = postsNav.getNext(postEntry);
				//tempEntry.recycle();
			}
			//postsNav.recycle();
			//postsView.recycle();

		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private static final long serialVersionUID = 7503711353565531962L;
}

class TopicsByLatestPostAtDesc extends AbstractList<Topic> implements Serializable {
	private List<Topic> cache;
	private Set<String> usedIds;
	//private int lastPostIndex = 0;

	@SuppressWarnings("unchecked")
	@Override
	public Topic get(int arg0) {
		//System.out.println("Requesting index " + arg0);
		//System.out.println("Current cache size: " + this.getCache().size());
		if(arg0 < this.getCache().size()) {
			return this.getCache().get(arg0);
		} else if(arg0 < this.size()) {
			// Oh god, you won't tell anyone I'm cheating, will you?
			try {
				View postsView = JSFUtil.getDatabase().getView("Posts");
				JSFUtil.registerProductObject(postsView);
				postsView.setAutoUpdate(false);
				postsView.resortView("DateTime", false);

				Topics topics = new Topics();
				Topic result = null;
				Vector topicIds = postsView.getColumnValues(1);
				for(Object topicIdObj : topicIds) {
					String topicId = (String)topicIdObj;
					//System.out.println("Checking topic id " + topicId);
					if(!this.getUsedIds().contains(topicId)) {
						Topic topic = topics.getById(topicId);
						this.getCache().add(topic);
						this.getUsedIds().add(topicId);
						if(this.getCache().size() == arg0+1) {
							result = topic;
							break;
						}
					}
				}

				//				ViewNavigator entries = postsView.createViewNav();
				//				ViewEntry entry = entries.getFirst();
				//				
				//				while(entry != null) {
				//					String topicId = (String)entry.getColumnValues().get(1);
				//					if(!this.getUsedIds().contains(topicId)) {
				//						Topic topic = (new Topics()).getById(topicId);
				//						this.getCache().add(topic);
				//						this.getUsedIds().add(topicId);
				//						if(this.getCache().size() == arg0+1) {
				//							result = topic;
				//							break;
				//						}
				//					}
				//					
				//					entry = entries.getNext(entry);
				//				}
				//postsView.recycle();
				return result;
			} catch(Exception e) { }


			//			Posts posts = new Posts();
			//			List<Post> postList = posts.getPostsByDateTimeDesc();
			//			for(; this.lastPostIndex < postList.size(); this.lastPostIndex++) {
			//				Post post = postList.get(this.lastPostIndex);
			//				if(!this.getUsedIds().contains(post.getTopicId())) {
			//					Topic topic = post.getTopic();
			//					this.getCache().add(topic);
			//					this.getUsedIds().add(post.getTopicId());
			//					if(this.getCache().size() == arg0+1) {
			//						return topic;
			//					}
			//				}
			//			}
		}
		return null;
	}

	@Override
	public int size() {
		try {
			Topics topics = new Topics();
			return topics.getAll().size();
		} catch(Exception e) { }
		return 0;
	}

	private List<Topic> getCache() {
		if(this.cache == null) {
			this.cache = new Vector<Topic>();
		}
		return this.cache;
	}
	private Set<String> getUsedIds() {
		if(this.usedIds == null) {
			this.usedIds = new HashSet<String>();
		}
		return this.usedIds;
	}

	private static final long serialVersionUID = 1497286689207875056L;
}