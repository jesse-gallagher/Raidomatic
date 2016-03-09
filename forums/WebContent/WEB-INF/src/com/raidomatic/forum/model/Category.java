package com.raidomatic.forum.model;

import java.util.List;
import com.raidomatic.model.*;
import com.raidomatic.JSFUtil;
import lotus.domino.*;
import lombok.*;

public class Category extends AbstractModel {
	@SuppressWarnings("unchecked")
	protected static AbstractModel createObjectFromEntry(DominoEntryWrapper entry) throws NotesException {
		List<Object> columnValues = (List<Object>)entry.getColumnValues();

		Category object = new Category();
		object.setUniversalId(entry.getUniversalID());
		object.setDocExists(true);

		object.setIndex(JSFUtil.toInteger(columnValues.get(0)));
		object.setTitle((String)columnValues.get(1));
		object.setTitle((String)columnValues.get(2));
		object.setReaders(JSFUtil.toStringList(columnValues.get(3)));
		object.setAuthors(JSFUtil.toStringList(columnValues.get(4)));

		return object;
	}

	private @Getter @Setter String title;
	private @Getter @Setter String id;
	private @Getter @Setter int index;

	public List<Forum> getForums() {
		Forums forumList = new Forums();
		return forumList.getForumsForCategoryId(this.getId());
	}

	private static final long serialVersionUID = -2838275692552570276L;
}