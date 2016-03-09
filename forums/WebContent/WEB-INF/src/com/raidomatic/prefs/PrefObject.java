package com.raidomatic.prefs;

import lotus.domino.*;
import com.raidomatic.JSFUtil;
import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("unchecked")
public class PrefObject implements Serializable {
	String key;
	boolean anonymous;
	String signature;
	private transient Document doc;

	@SuppressWarnings("unused")
	private PrefObject() { }
	public PrefObject(String key) {
		this.key = key;
		this.anonymous = this.key.toLowerCase().equals("anonymous");
	}

	public Document getDoc() throws Exception {
		if(doc == null) {
			Database database = JSFUtil.getDatabase();
			View prefsView = database.getView("User Prefs");
			JSFUtil.registerProductObject(prefsView);
			prefsView.setAutoUpdate(false);
			prefsView.refresh();
			doc = prefsView.getDocumentByKey(this.key, true);
			//prefsView.recycle();
			if(doc == null) {
				doc = database.createDocument();
				doc.replaceItemValue("Form", "UserPrefs");
				doc.replaceItemValue("Key", this.key);
				Item keyItem = doc.getFirstItem("Key");
				JSFUtil.registerProductObject(keyItem);
				keyItem.setNames(true);
				doc.replaceItemValue("Authors", this.key);
				Item authors = doc.getFirstItem("Authors");
				JSFUtil.registerProductObject(authors);
				authors.setAuthors(true);
				doc.computeWithForm(false, false);
				if(!this.anonymous) { doc.save(true, false); }
				prefsView.refresh();
			}
			JSFUtil.registerProductObject(doc);
		}
		return doc;
	}

	public boolean isFavorite(String unid) {
		if(this.anonymous) { return false; }

		boolean favorite = false;
		try {
			Document doc = this.getDoc();
			Vector<String> favorites = (Vector<String>)doc.getItemValue("FavoriteTopics");
			favorite = favorites.contains(unid);
		} catch(Exception e) { }
		return favorite;
	}
	public void addFavorite(String unid) {
		if(this.anonymous) { return; }

		try {
			Document doc = this.getDoc();
			Vector<String> favorites = (Vector<String>)doc.getItemValue("FavoriteTopics");
			if(!favorites.contains(unid)) {
				favorites.add(unid);
				doc.replaceItemValue("FavoriteTopics", favorites);
				doc.save();
			}
		} catch(Exception e) { }
	}
	public void removeFavorite(String unid) {
		if(this.anonymous) { return; }

		try {
			Document doc = this.getDoc();
			Vector<String> favorites = (Vector<String>)doc.getItemValue("FavoriteTopics");
			if(favorites.contains(unid)) {
				favorites.remove(unid);
				doc.replaceItemValue("FavoriteTopics", favorites);
				doc.save();
			}
		} catch(Exception e) { }
	}
	public List<String> getFavorites() {
		if(this.anonymous) { return new Vector<String>(); }

		try {
			Document doc = this.getDoc();
			return (List<String>)doc.getItemValue("FavoriteTopics");
		} catch(Exception e) { return new Vector<String>(); }
	}

	public boolean isCategoryHidden(String unid) {
		if(this.anonymous) { return false; }

		boolean hidden = false;
		try {
			Document doc = this.getDoc();
			Vector<String> hiddenCategories = (Vector<String>)doc.getItemValue("HiddenCategories");
			hidden = hiddenCategories.contains(unid);
		} catch(Exception e) { }
		return hidden;
	}
	public void hideCategory(String unid) {
		if(this.anonymous) { return; }

		try {
			Document doc = this.getDoc();
			Vector<String> favorites = (Vector<String>)doc.getItemValue("HiddenCategories");
			if(!favorites.contains(unid)) {
				favorites.add(unid);
				doc.replaceItemValue("HiddenCategories", favorites);
				doc.save();
			}
		} catch(Exception e) { }
	}
	public void showCategory(String unid) {
		if(this.anonymous) { return; }

		try {
			Document doc = this.getDoc();
			Vector<String> favorites = (Vector<String>)doc.getItemValue("HiddenCategories");
			if(favorites.contains(unid)) {
				favorites.remove(unid);
				doc.replaceItemValue("HiddenCategories", favorites);
				doc.save();
			}
		} catch(Exception e) { }
	}
	public void toggleCategory(String unid) {
		if(this.isCategoryHidden(unid)) {
			this.showCategory(unid);
		} else {
			this.hideCategory(unid);
		}
	}

	public String getForumPageMode() {
		if(this.anonymous) { return ""; }

		try {
			Document doc = this.getDoc();
			return doc.getItemValueString("ForumPageMode");
		} catch(Exception e) { }
		return "";
	}
	public void setForumPageMode(String forumPageMode) {
		if(this.anonymous) { return; }

		try {
			Document doc = this.getDoc();
			if(!doc.getItemValueString("ForumPageMode").equals(forumPageMode)) {
				doc.replaceItemValue("ForumPageMode", forumPageMode);
				doc.save();
			}
		} catch(Exception e) { e.printStackTrace(); }
	}

	public String getForumRecentMode() {
		if(this.anonymous) { return ""; }

		try {
			Document doc = this.getDoc();
			return doc.getItemValueString("RecentMode");
		} catch(Exception e) { }
		return "";
	}

	public String getAvatarMode() {
		if(this.anonymous) { return ""; }

		try {
			return this.getDoc().getItemValueString("AvatarMode");
		} catch(Exception e) {
			return "";
		}
	}
	public String getAvatarCharacter() {
		if(this.anonymous) { return ""; }

		try {
			return this.getDoc().getItemValueString("AvatarCharacter");
		} catch(Exception e) {
			return "";
		}
	}
	public String getAvatarUrl() {
		if(this.anonymous) { return ""; }

		try {
			return this.getDoc().getItemValueString("AvatarURL");
		} catch(Exception e) {
			return "";
		}
	}
	public String getAvatarUrlMimeType() {
		if(this.anonymous) { return ""; }
		try {
			return this.getDoc().getItemValueString("AvatarURLMIMEType");
		} catch(Exception e) {
			return "";
		}
	}
	public synchronized boolean isHideOtherSignatures() {
		if(this.anonymous) { return false; }
		try {
			return this.getDoc().getItemValueString("HideOtherSignatures").equals("Yes");
		} catch(Exception e) {
			return false;
		}
	}

	private static final long serialVersionUID = -5726068391631727452L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException { aInputStream.defaultReadObject(); }
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}
