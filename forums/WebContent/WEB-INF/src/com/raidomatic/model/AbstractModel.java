package com.raidomatic.model;

import java.util.List;
import java.util.Vector;
import java.io.Serializable;
import com.raidomatic.JSFUtil;
import lombok.Getter;
import lombok.Setter;
import lotus.domino.*;
import com.raidomatic.prefs.UserPrefs;

public abstract class AbstractModel implements Serializable {
	protected static AbstractModel createObjectFromEntry(DominoEntryWrapper entry) throws NotesException { return null; }

	private @Setter boolean docExists = false;
	private @Getter @Setter String universalId = "";
	private @Getter @Setter List<String> readers;
	private @Setter List<String> authors;

	public String getId() { return ""; }
	public boolean getDocExists() { return this.docExists; }
	public List<String> getAuthors() { return authors == null ? new Vector<String>() : authors; }

	public boolean getIsRead() throws Exception {
		return this.isRead();
	}
	public boolean isRead() throws Exception {
		return JSFUtil.getReadMarkManager().isRead(this.getId());
	}
	public void markRead() throws Exception {
		JSFUtil.getReadMarkManager().markRead(this.getId());
	}
	public void clearReadMarks() throws Exception {
		JSFUtil.getReadMarkManager().clearMarks(this.getId());
	}

	public boolean getIsFavorite() { return this.isFavorite(); }
	public boolean isFavorite() {
		UserPrefs prefs = JSFUtil.getUserPrefs();
		return prefs.getCurrent().isFavorite(this.getId());
	}
	public void setFavorite(boolean isFavorite) {
		UserPrefs prefs = JSFUtil.getUserPrefs();
		if(isFavorite) {
			prefs.getCurrent().addFavorite(this.getId());
		} else {
			prefs.getCurrent().removeFavorite(this.getId());
		}
	}
	public void setIsFavorite(boolean isFavorite) { this.setFavorite(isFavorite); }
	public void removeFavorite() { this.setIsFavorite(false); }
	public void addFavorite() { this.setIsFavorite(true); }

	@SuppressWarnings("unchecked")
	public boolean getIsEditable() {
		try {
			if(JSFUtil.getUserName().equalsIgnoreCase("Anonymous")) { return false; }

			Database database = JSFUtil.getDatabase();
			if(database.getCurrentAccessLevel() >= 4) { return true; }
			if(this.getAuthors().contains(JSFUtil.getUserName())) { return true; }
			List<String> roles = (List<String>)database.queryAccessRoles(JSFUtil.getUserName());
			for(int i = 0; i < roles.size(); i++) {
				if(this.getAuthors().contains(roles.get(i))) { return true; }
			}
		} catch(NotesException ne) { }
		return false;
	}
	public boolean isEditable() { return this.getIsEditable(); }

	public Database getDatabase() throws NotesException { return JSFUtil.getDatabase(); }
	public Document getDocument() throws NotesException {
		Database database = this.getDatabase();
		Document doc = database.getDocumentByUNID(this.getUniversalId());
		JSFUtil.registerProductObject(doc);
		return doc;
	}
	protected Document getDocumentAsSigner() throws NotesException {
		Session sessionAsSigner = JSFUtil.getSessionAsSigner();
		Database database = this.getDatabase();
		database = sessionAsSigner.getDatabase(database.getServer(), database.getFilePath());
		JSFUtil.registerProductObject(database);
		Document doc = database.getDocumentByUNID(this.getUniversalId());
		JSFUtil.registerProductObject(doc);
		return doc;
	}
	protected DateTime createDateTime(java.util.Date date) throws NotesException {
		Session session = JSFUtil.getSession();
		DateTime dateTime = session.createDateTime(date);
		JSFUtil.registerProductObject(dateTime);
		return dateTime;
	}

	public boolean save() { return false; }
	protected void clearCache() throws Exception { }

	@Override
	public boolean equals(Object otherModel) {
		if(otherModel instanceof AbstractModel) {
			return ((AbstractModel)otherModel).getId().equals(this.getId());
		}
		return false;
	}

	private static final long serialVersionUID = -7281570854955624668L;
}
