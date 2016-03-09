package com.raidomatic.forum.model;

import java.util.*;
import lotus.domino.*;
import lombok.*;
import com.raidomatic.JSFUtil;
//import com.raidomatic.ModelCache;
import com.raidomatic.ReadMarkManager;
import com.raidomatic.model.*;

public class Post extends AbstractModel {

	@SuppressWarnings("unchecked")
	protected static AbstractModel createObjectFromEntry(DominoEntryWrapper entry) throws NotesException {
		List<Object> columnValues = (List<Object>)entry.getColumnValues();

		Post object = new Post();
		System.out.println("getUniversalID in Post");
		object.setUniversalId(entry.getUniversalID());
		object.setDocExists(true);

		object.setId((String)columnValues.get(0));
		object.setTopicId((String)columnValues.get(1));
		object.setCreatedBy((String)columnValues.get(2));
		object.setDateTime(JSFUtil.toDate(columnValues.get(3)));
		object.setForumId((String)columnValues.get(4));

		// Non-view data
		String bodyString;
		Document postDoc = entry.getDocument();
		JSFUtil.registerProductObject(postDoc);
		MIMEEntity body = postDoc.getMIMEEntity();
		if(body != null) {
			JSFUtil.registerProductObject(body);
			bodyString = body.getContentAsText();
		} else {
			bodyString = "";
		}

		object.setBody(bodyString);
		object.setReplyPostId(postDoc.getItemValueString("ReplyPostID"));
		object.setScreenshotUrl(postDoc.getItemValueString("ScreenshotURL"));
		object.setTitle(postDoc.getItemValueString("Title"));
		object.setTags((List<String>)postDoc.getItemValue("Tags"));
		object.setAuthors((List<String>)postDoc.getItemValue("Authors"));
		object.setReaders((List<String>)postDoc.getItemValue("Readers"));

		return object;
	}

	private @Getter @Setter String id;
	private @Getter @Setter String topicId;
	private @Getter @Setter String forumId;
	private @Getter @Setter String createdBy;
	private @Getter @Setter Date dateTime;
	private @Getter @Setter String title;
	private @Getter @Setter String body;
	private @Getter @Setter String replyPostId;
	private @Setter List<String> tags;
	private @Getter @Setter String screenshotUrl;

	public List<String> getTags() {
		if(this.tags == null) { this.tags = new Vector<String>(); }
		return tags;
	}

	public Topic getTopic() throws Exception {
		Topics topics = new Topics();
		return topics.getById(this.topicId);
	}
	public Forum getForum() throws Exception {
		Forums forums = new Forums();
		return forums.getById(this.forumId);
	}
	public Player getPlayer() {
		Players players = new Players();
		return players.getByName(this.getCreatedBy());
	}
	public List<PostVersion> getVersions() {
		PostVersions postVersions = new PostVersions();
		return postVersions.getPostVersionsByPostId(this.getId());
	}

	public boolean delete(boolean updateTopic) {
		try {
			if(this.getDocExists()) {
				Document doc = this.getDocument();
				doc.replaceItemValue("Form", "DeletedPost");
				doc.save();

				this.clearCache();

				if(updateTopic) {
					Topic topic = this.getTopic();
					topic.updateLatestPost();
					topic.save();
				}
			}
		} catch(Exception e) { return false; }
		return true;
	}
	public boolean delete() {
		return this.delete(true);
	}

	@SuppressWarnings("unchecked")
	public boolean save(boolean asSigner, boolean noVersions) {
		try {
			lotus.domino.Item tags = null;
			if(this.getDocExists()) {

				boolean changed = false;
				Document doc = this.getDocument();
				Session session = JSFUtil.getSession();

				Document postVersion = JSFUtil.getDatabase().createDocument();
				JSFUtil.registerProductObject(postVersion);
				doc.copyAllItems(postVersion, true);
				postVersion.replaceItemValue("Form", "PostVersion");
				postVersion.makeResponse(doc);
				postVersion.replaceItemValue("PostVersionID", postVersion.getUniversalID());
				postVersion.replaceItemValue("NextVersionBy", JSFUtil.getUserName());
				postVersion.replaceItemValue("NextVersionAt", session.evaluate("@Now"));

				if(!doc.getItemValueString("PostID").equals(this.getId())) {
					doc.replaceItemValue("PostID", this.getId());
					changed = true;
				}
				if(!doc.getItemValueString("TopicID").equals(this.getTopicId())) {
					doc.replaceItemValue("TopicID", this.getTopicId());
					doc.makeResponse(this.getTopic().getDocument());
					changed = true;
				}
				if(!doc.getItemValueString("ForumID").equals(this.getForumId())) {
					doc.replaceItemValue("ForumID", this.getForumId());
					changed = true;
				}
				if(!doc.getItemValueString("CreatedBy").equals(this.getCreatedBy())) {
					doc.replaceItemValue("CreatedBy", this.getCreatedBy());
					changed = true;
				}
				DateTime origDateTime = (DateTime)doc.getItemValueDateTimeArray("DateTime").get(0);
				JSFUtil.registerProductObject(origDateTime);
				if(origDateTime.timeDifference(this.createDateTime(this.getDateTime())) != 0) {
					doc.replaceItemValue("DateTime", this.createDateTime(this.getDateTime()));
					changed = true;
				}
				if(!doc.getItemValueString("Title").equals(this.getTitle())) {
					doc.replaceItemValue("Title", this.getTitle());
					changed = true;
				}
				if(!doc.getItemValueString("ReplyPostID").equals(this.getReplyPostId())) {
					doc.replaceItemValue("ReplyPostID", this.getReplyPostId());
					changed = true;
				}

				// An empty tag set in Java will be a null vector, but in the document it will be an empty string,
				//	so that has to be accounted for when comparing and storing
				if(!doc.getItemValue("Tags").equals(this.getTags())
						&& !(doc.getItemValueString("Tags").equals("") && this.getTags().size() == 0)) {
					doc.replaceItemValue("Tags", "");
					tags = doc.getFirstItem("Tags");
					for(String tag : this.getTags()) {
						tags.appendToTextList(tag);
					}
					changed = true;
				}
				if(!doc.getItemValueString("ScreenshotURL").equals(this.getScreenshotUrl())) {
					doc.replaceItemValue("ScreenshotURL", this.getScreenshotUrl());
					changed = true;
				}


				session.setConvertMime(false);
				MIMEEntity bodyItem;
				if(doc.hasItem("Body")) {
					bodyItem = doc.getMIMEEntity();
				} else {
					bodyItem = doc.createMIMEEntity();
				}
				JSFUtil.registerProductObject(bodyItem);
				Stream stream = session.createStream();
				JSFUtil.registerProductObject(stream);
				stream.writeText(this.getBody());
				bodyItem.setContentFromText(stream, "text/html;charset=UTF-8", MIMEEntity.ENC_IDENTITY_8BIT);

				changed = true;

				if(changed) {
					doc.computeWithForm(false, false);

					// Assign SanitizedCreatedBy as necessary
					if(doc.getItemValue("Readers").size() == 0) {
						doc.replaceItemValue("SanitizedCreatedBy", doc.getItemValueString("CreatedBy"));
					} else {
						Vector userRoles = JSFUtil.getDatabase().queryAccessRoles(doc.getItemValueString("CreatedBy"));
						boolean goodToGo = false;
						for(Object reader : doc.getItemValue("Readers")) {
							if(reader.toString().toLowerCase().equals(doc.getItemValueString("CreatedBy").toLowerCase())) {
								goodToGo = true;
								break;
							}

							if(userRoles.contains(reader)) {
								goodToGo = true;
								break;
							}
						}

						if(goodToGo) {
							doc.replaceItemValue("SanitizedCreatedBy", doc.getItemValue("CreatedBy"));
						} else {
							doc.replaceItemValue("SanitizedCreatedBy", "");
						}
					}

					doc.computeWithForm(false, false);

					doc.save();

					if(!noVersions) { postVersion.save(); }

					this.clearCache();
					JSFUtil.updateFTIndex();
				}
				//				try {
				//					doc.recycle();
				//					bodyItem.recycle();
				//					postVersion.recycle();
				//					if(tags != null) { tags.recycle(); }
				//				} catch(NotesException ne) { }
			} else {
				Session session = JSFUtil.getSession();
				Database database = JSFUtil.getDatabase();
				database = JSFUtil.getSessionAsSigner().getDatabase(database.getServer(), database.getFilePath());
				JSFUtil.registerProductObject(database);
				com.raidomatic.Configuration config = JSFUtil.getConfiguration();

				Document doc = database.createDocument();
				JSFUtil.registerProductObject(doc);
				doc.replaceItemValue("Form", "Post");
				//doc.makeResponse(this.getTopic().getDocument());


				this.setDateTime(Calendar.getInstance().getTime());
				String userName = JSFUtil.getUserName();
				this.setCreatedBy(userName.equalsIgnoreCase("Anonymous") ? (String)config.getDocumentItemValue("Forum Options", "ForumPostNamelessPostsAs").get(0) : userName);
				this.setUniversalId(doc.getUniversalID());
				this.setId(this.getUniversalId());

				doc.replaceItemValue("PostID", this.getId());
				doc.replaceItemValue("TopicID", this.getTopicId());
				doc.replaceItemValue("ForumID", this.getForumId());
				doc.replaceItemValue("CreatedBy", this.getCreatedBy());
				doc.replaceItemValue("DateTime", this.createDateTime(this.getDateTime()));
				doc.replaceItemValue("Title", this.getTitle());
				doc.replaceItemValue("ReplyPostID", this.getReplyPostId());
				//doc.replaceItemValue("Tags", this.getTags());
				doc.replaceItemValue("Tags", "");
				tags = doc.getFirstItem("Tags");
				for(String tag : this.getTags()) {
					tags.appendToTextList(tag);
				}
				doc.replaceItemValue("ScreenshotURL", this.getScreenshotUrl());

				// If this is a guild app, we're Anonymous but want to post in a restricted forum, so break the rules a lot
				Document topicDoc = null;
				View topics = database.getView("Topics");
				JSFUtil.registerProductObject(topics);
				topics.resortView();
				topicDoc = topics.getDocumentByKey(this.getTopicId());
				JSFUtil.registerProductObject(topicDoc);
				topicDoc.getFirstItem("Readers").copyItemToDocument(doc);
				topicDoc.getFirstItem("ReadersFailsafe").copyItemToDocument(doc);
				doc.makeResponse(topicDoc);


				MIMEEntity bodyItem = doc.createMIMEEntity();
				JSFUtil.registerProductObject(bodyItem);
				Stream stream = session.createStream();
				JSFUtil.registerProductObject(stream);
				stream.writeText(this.getBody());
				bodyItem.setContentFromText(stream, "text/html;charset=UTF-8", MIMEEntity.ENC_IDENTITY_8BIT);

				doc.computeWithForm(false, false);

				// Assign SanitizedCreatedBy as necessary
				if(doc.getItemValue("Readers").size() == 0) {
					doc.replaceItemValue("SanitizedCreatedBy", doc.getItemValueString("CreatedBy"));
				} else {
					Vector userRoles = JSFUtil.getDatabase().queryAccessRoles(doc.getItemValueString("CreatedBy"));
					boolean goodToGo = false;
					for(Object reader : doc.getItemValue("Readers")) {
						if(reader.toString().toLowerCase().equals(doc.getItemValueString("CreatedBy").toLowerCase())) {
							goodToGo = true;
							break;
						}

						if(userRoles.contains(reader)) {
							goodToGo = true;
							break;
						}
					}

					if(goodToGo) {
						doc.replaceItemValue("SanitizedCreatedBy", doc.getItemValue("CreatedBy"));
					} else {
						doc.replaceItemValue("SanitizedCreatedBy", "");
					}
				}

				doc.computeWithForm(false, false);

				doc.save();

				View postsView = database.getView("Posts");
				JSFUtil.registerProductObject(postsView);
				postsView.refresh();
				topics.refresh();

				this.setId(doc.getUniversalID());
				this.setUniversalId(doc.getUniversalID());

				//				Topic topic = this.getTopic();
				//				topic.setLatestPostAt(this.getDateTime());
				//				topic.setLatestPostBy(this.getCreatedBy());
				//				topic.save(true);
				topicDoc.replaceItemValue("LatestPostAt", this.createDateTime(this.getDateTime()));
				topicDoc.replaceItemValue("LatestPostBy", this.getCreatedBy());
				topicDoc.replaceItemValue("LatestPostID", this.getId());
				topicDoc.save();

				this.clearCache();
				JSFUtil.updateFTIndex();

				//				try {
				//					doc.recycle();
				//					bodyItem.recycle();
				//					topicDoc.recycle();
				//					postsView.recycle();
				//					topics.recycle();
				//					if(tags != null) { tags.recycle(); }
				//				} catch(NotesException ne) { }
			}
		} catch(Exception ne) {
			ne.printStackTrace();
			return false;
		}
		return true;
	}
	@Override
	public boolean save() { return this.save(false, false); }

	@Override
	public void clearCache() throws Exception {
		//		if(true) { return; }
		//		Forum forum = this.getForum();
		//		String categoryId = forum.getCategoryId();
		//		
		//		ModelCache cache = JSFUtil.getModelCache();
		//		cache.clearMatches(".*-ForumList-BY_ID-" + this.getForumId());
		//		cache.clearMatches(".*-PostList-BY_TOPIC_ID-" + this.getTopicId());
		//		cache.clearMatches(".*-CategoryList-BY_ID-" + categoryId);
		//		cache.clearMatches(".*-PostList-BY_ID-" + this.getId());
		//		cache.clearMatches(".*-TopicList-BY_ID-" + this.getTopicId());
		//		cache.clearMatches(".*-PostVersionList-BY_POST_ID-" + this.getId());
		//		
		//		// Clear out any aggregate collections that may contain this object
		//		cache.clearMatches(".*-PostList-.*-Search-.*");
		//		cache.clearMatches(".*-PostList-[^\\-]*");
		//		cache.clearMatches(".*-PostList-BY_TAG-.*");
		//		
		//		// Clear everything for now
		//		cache.clear();
		//		
		JSFUtil.getModelIDCache().clear();

		ReadMarkManager manager = JSFUtil.getReadMarkManager();
		manager.clearMarks(this.getId());
		manager.clearMarks(this.getTopicId());
		manager.clearMarks(this.getForumId());

		manager.markRead(this.getId());
		manager.markRead(this.getTopicId());
		manager.markRead(this.getForumId());
	}

	private static final long serialVersionUID = -3863922534920393635L;
}
