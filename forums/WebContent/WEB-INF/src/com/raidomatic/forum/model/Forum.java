package com.raidomatic.forum.model;

import java.util.*;

import lotus.domino.*;
import lombok.*;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.AbstractModel;
import com.raidomatic.model.DominoEntryWrapper;

public class Forum extends AbstractModel {

	@SuppressWarnings("unchecked")
	protected static AbstractModel createObjectFromEntry(DominoEntryWrapper entry) throws NotesException {
		List<Object> columnValues = (List<Object>)entry.getColumnValues();

		Forum object = new Forum();
		System.out.println("getUniversalID in Forum");
		object.setUniversalId(entry.getUniversalID());
		object.setDocExists(true);

		object.setId((String)columnValues.get(0));
		object.setTitle((String)columnValues.get(1));
		object.setCategoryId((String)columnValues.get(2));
		object.setSubtitle((String)columnValues.get(3));
		object.setReaders(JSFUtil.toStringList(columnValues.get(4)));
		object.setAuthors(JSFUtil.toStringList(columnValues.get(5)));

		return object;
	}

	private @Getter @Setter String id;
	private @Getter @Setter String categoryId;
	private @Getter @Setter String title;
	private @Getter @Setter String subtitle;

	public List<Topic> getTopics() {
		Topics topicList = new Topics();
		return topicList.getTopicsForForumId(this.getId());
	}
	public List<Post> getPosts() {
		Posts postList = new Posts();
		return postList.getPostsForForumId(this.getId());
	}

	public Category getCategory() throws Exception {
		Categories categories = new Categories();
		return categories.getById(this.categoryId);
	}
	public Post getLatestPost() throws Exception {
		Posts posts = new Posts();
		return posts.getLatestPostForForumId(this.getId());
	}

	public int getPostCount() throws Exception { return (Integer)fetchLatestPostInfo().get("size"); }
	public int getTopicCount() throws Exception {
		return this.getTopics().size();
	}
	public Date getLatestPostAt() throws Exception { return (Date)fetchLatestPostInfo().get("dateTime"); }
	public String getLatestPostBy() throws Exception { return (String)fetchLatestPostInfo().get("by"); }
	public String getLatestPostTopicId() throws Exception { return (String)fetchLatestPostInfo().get("topicId"); }
	public String getLatestPostId() throws Exception { return (String)fetchLatestPostInfo().get("id"); }

	@SuppressWarnings("unchecked")
	private Map<String, Object> fetchLatestPostInfo() throws NotesException {
		Map<String, Object> requestScope = ExtLibUtil.getRequestScope();
		String key = "$$Cache-PostsByForumID-" + this.getId() + "-" + JSFUtil.getUserName();
		if(!requestScope.containsKey(key)) {
			View postsByUser = ExtLibUtil.getCurrentDatabase().getView("($$Lookup-PostsByForumID)");
			postsByUser.setAutoUpdate(false);
			ViewNavigator nav = postsByUser.createViewNavFromCategory(this.getId());
			ViewEntry entry = nav.getFirst();

			entry.setPreferJavaDates(true);
			Map<String, Object> result = new HashMap<String, Object>();
			List<Object> columnValues = entry.getColumnValues();
			result.put("dateTime", columnValues.get(1));
			result.put("by", columnValues.get(2));
			result.put("topicId", columnValues.get(3));
			result.put("id", columnValues.get(4));
			result.put("size", nav.getCount());

			nav.recycle();
			postsByUser.recycle();
			requestScope.put(key, result);
		}
		return (Map<String, Object>)requestScope.get(key);
	}

	private static final long serialVersionUID = -3865619875665306067L;
}
