package com.raidomatic.forum.model;

import com.ibm.xsp.extlib.util.ExtLibUtil;
import com.raidomatic.JSFUtil;
import com.raidomatic.model.*;
import java.util.*;
import lotus.domino.*;
import lombok.SneakyThrows;

public class Posts extends AbstractCollectionManager<Post> {

	public List<Post> getPosts() throws Exception {
		return this.getAll();
	}

	@SneakyThrows
	public List<Post> getPostsForTopicId(String topicId) {
		return this.getCollection(PostList.BY_TOPIC_ID, topicId, null, -1);
	}
	public Post getLatestPostForTopicId(String topicId) {
		List<Post> collection = this.getPostsForTopicId(topicId);
		Post result = collection.get(collection.size()-1);
		return result;
	}
	@SneakyThrows
	public List<Post> getPostsForForumId(String forumId) {
		return this.getCollection(PostList.BY_FORUM_ID, forumId, null, -1);
	}
	@SneakyThrows
	public Post getLatestPostForForumId(String forumId) throws Exception {
		return this.getFirstOfCollection(PostList.BY_FORUM_ID, forumId);
	}

	public List<Post> searchSortedByDateTime(String query) {
		return this.getCollection(PostList.BY_DATETIME_DESC, null, query);
	}

	public List<Post> getPostsByDateTimeDesc() {
		return this.getCollection(PostList.BY_DATETIME_DESC);
	}

	public List<Post> getPostsForTag(String tag) throws Exception {
		//return this.getCollection(PostList.BY_TAGS, tag);
		//return this.getCollection(null, null, "[Tags]=" + tag);
		return this.search("[Tags]=" + tag);
	}

	public List<Post> getPostsForUser(String user) throws Exception {
		//return this.getCollection(PostList.BY_CREATED_BY, user);
		return this.search("[CreatedBy]=" + user, PostList.BY_DATETIME_DESC);
	}
	public List<Post> getScreenshotPosts() throws Exception {
		//return this.getCollection(PostList.BY_IS_SCREENSHOT_POST, 1);
		return this.search("[ScreenshotURL] is present", PostList.BY_DATETIME_DESC);
	}
	public int getPostCountForUser(String user) throws Exception {
		Map<Object, Object> requestScope = JSFUtil.getMiscDBCache();
		String key = "$$Cache-PostsByUser-" + user + "-" + JSFUtil.getUserName();
		if(!requestScope.containsKey(key)) {
			View postsByUser = ExtLibUtil.getCurrentDatabase().getView("($$Lookup-PostsByUser)");
			postsByUser.setAutoUpdate(false);
			ViewNavigator nav = postsByUser.createViewNavFromCategory(user);
			int count = nav.getCount();
			nav.recycle();
			postsByUser.recycle();
			requestScope.put(key, count);
		}
		return (Integer)requestScope.get(key);
	}
	public int getPostCountForTopicId(String topicId) throws Exception {
		Map<Object, Object> requestScope = JSFUtil.getMiscDBCache();
		String key = "$$Cache-PostsByTopicID-" + topicId + "-" + JSFUtil.getUserName();
		if(!requestScope.containsKey(key)) {
			View postsByUser = ExtLibUtil.getCurrentDatabase().getView("($$Lookup-PostsByTopicID)");
			postsByUser.setAutoUpdate(false);
			ViewNavigator nav = postsByUser.createViewNavFromCategory(topicId);
			int count = nav.getCount();
			nav.recycle();

			postsByUser.recycle();
			requestScope.put(key, count);
		}

		return (Integer)requestScope.get(key);
	}

	@Override
	protected AbstractDominoList<Post> createCollection() { return new PostList(); }

	private static final long serialVersionUID = 3428229170619057957L;


	public class PostList extends AbstractDominoList<Post> {
		public static final String BY_ID = "PostID";
		public static final String BY_TOPIC_ID = "TopicID";
		//public static final String BY_CREATED_BY = "CreatedBy";
		public static final String BY_FORUM_ID = "ForumID";
		public static final String BY_DATETIME = "DateTime";
		public static final String BY_DATETIME_DESC = "DateTime-desc";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "topicId", "createdBy", "dateTime", "forumId", "" }; }
		@Override
		protected String getDefaultSort() { return PostList.BY_ID; }
		@Override
		protected String getViewName() { return "Posts"; }

		@Override
		@SuppressWarnings("unchecked")
		protected Post createObjectFromViewEntry(DominoEntryWrapper entry) throws Exception {
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

			Post post = super.createObjectFromViewEntry(entry);
			post.setBody(bodyString);
			post.setReplyPostId(postDoc.getItemValueString("ReplyPostID"));
			post.setScreenshotUrl(postDoc.getItemValueString("ScreenshotURL"));
			post.setTags((List<String>)postDoc.getItemValue("Tags"));
			post.setAuthors((List<String>)postDoc.getItemValue("Authors"));
			post.setReaders((List<String>)postDoc.getItemValue("Readers"));

			post.setTitle(postDoc.getItemValueString("Title"));

			return post;
		}


		private static final long serialVersionUID = 7714837638779123477L;
	}
}