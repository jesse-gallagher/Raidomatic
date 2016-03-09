package com.raidomatic.forum.model;

import java.util.*;
import com.raidomatic.model.*;

public class PostVersions extends AbstractCollectionManager<PostVersion> {

	public List<PostVersion> getPostVersions() throws Exception {
		return this.getAll();
	}
	public List<PostVersion> getPostVersionsByPostId(String postId) {
		return this.getCollection(PostVersionList.BY_POST_ID, postId);
	}

	@Override
	protected AbstractDominoList<PostVersion> createCollection() { return new PostVersionList(); }

	private static final long serialVersionUID = 8748698434653522631L;


	public class PostVersionList extends AbstractDominoList<PostVersion> {
		public static final String BY_ID = "PostVersionID";
		public static final String BY_POST_ID = "PostID";

		@Override
		protected String[] getColumnFields() { return new String[] { "id", "topicId", "createdBy", "dateTime", "title", "forumId", "readers", "replyPostId", "authors", "tags", "", "postId", "nextVersionBy", "nextVersionAt" }; }
		@Override
		protected String getDefaultSort() { return PostVersionList.BY_ID; }
		@Override
		protected String getViewName() { return "PostVersions"; }

		private static final long serialVersionUID = 7714837638779123477L;
	}
}