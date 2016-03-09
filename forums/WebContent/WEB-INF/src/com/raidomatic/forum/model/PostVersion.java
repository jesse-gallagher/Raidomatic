package com.raidomatic.forum.model;

import java.util.Date;
import java.util.List;
import lotus.domino.Document;
import lotus.domino.MIMEEntity;
import lotus.domino.NotesException;
import lombok.*;

import com.raidomatic.JSFUtil;
import com.raidomatic.model.AbstractModel;
import com.raidomatic.model.DominoEntryWrapper;
import com.raidomatic.model.Player;
import com.raidomatic.model.Players;

public class PostVersion extends Post {

	@SuppressWarnings("unchecked")
	protected static AbstractModel createObjectFromEntry(DominoEntryWrapper entry) throws NotesException {
		List<Object> columnValues = (List<Object>)entry.getColumnValues();

		PostVersion object = new PostVersion();
		System.out.println("getUniversalID in PostVersion");
		object.setUniversalId(entry.getUniversalID());
		object.setDocExists(true);

		object.setId((String)columnValues.get(0));
		object.setTopicId((String)columnValues.get(1));
		object.setCreatedBy((String)columnValues.get(2));
		object.setDateTime(JSFUtil.toDate(columnValues.get(3)));
		object.setTitle((String)columnValues.get(4));
		object.setForumId((String)columnValues.get(5));
		object.setReaders(JSFUtil.toStringList(columnValues.get(6)));
		object.setReplyPostId((String)columnValues.get(7));
		object.setAuthors(JSFUtil.toStringList(columnValues.get(8)));
		object.setTags(JSFUtil.toStringList(columnValues.get(9)));
		// Ignore index 10
		object.setPostId((String)columnValues.get(11));
		object.setNextVersionBy((String)columnValues.get(12));
		object.setNextVersionAt(JSFUtil.toDate(columnValues.get(13)));

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

		return object;
	}

	private @Getter @Setter String postId;
	private @Getter @Setter String nextVersionBy;
	private @Getter @Setter Date nextVersionAt;

	public Player getNextVersionByPlayer() {
		Players players = new Players();
		return players.getByName(this.getNextVersionBy());
	}
	public Post getPost() throws Exception {
		Posts posts = new Posts();
		return posts.getById(this.getPostId());
	}

	private static final long serialVersionUID = 8211751421067461769L;
}
