package com.raidomatic;

import javax.faces.context.FacesContext;
import com.ibm.xsp.designer.context.ServletXSPContext;
import lotus.domino.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;

public class ReadMarkManager implements Serializable {
	transient Map<String, Boolean> cache;
	
	public void markRead(String unid) throws Exception {
		if(this.anonymous()) { return; }

		String username = this.username();
		if(!this.getCache().containsKey(username + "-" + unid) || !this.getCache().get(username + "-" + unid)) {
			try {
				
				ViewEntry entry = this.getMarksByKey().getEntryByKey(username + "-" + unid);
				if(entry == null) {
					Document readDoc = this.getMarkDB().createDocument();
					JSFUtil.registerProductObject(readDoc);
					readDoc.replaceItemValue("Form", "Read Mark");
					readDoc.replaceItemValue("Username", username);
					readDoc.replaceItemValue("UNID", unid);
					readDoc.save();
					
					this.getCache().put(username + "-" + unid, true);
					
					//readDoc.recycle();
				} else {
					JSFUtil.registerProductObject(entry);
					//entry.recycle();
				}
				
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}
	public boolean isRead(String unid) throws Exception {
		if(this.anonymous()) { return true; }
		
		String username = this.username();
		if(this.getCache().containsKey(username + "-" + unid)) {
			return this.getCache().get(username + "-" + unid);
		} else {
			ViewEntry entry = this.getMarksByKey().getEntryByKey(username + "-" + unid, true);
			if(entry != null) {
				JSFUtil.registerProductObject(entry);
				this.getCache().put(username + "-" + unid, true);
				//entry.recycle();
				return true;
			}
		}
		this.getCache().put(username + "-" + unid, false);
		return false;
	}
	
	public String username() {
		ServletXSPContext context = (ServletXSPContext)FacesContext.getCurrentInstance().getApplication().getVariableResolver().resolveVariable(FacesContext.getCurrentInstance(), "context");
		return context.getUser().getName();
	}
	public boolean anonymous() {
		return this.username().toLowerCase().equals("anonymous");
	}
	
	public void clearMarks(String unid) throws Exception {
		ViewEntryCollection entries = this.getMarksByUNID().getAllEntriesByKey(unid, true);
		JSFUtil.registerProductObject(entries);
		entries.removeAll(true);
		//entries.recycle();
		
		for(String key : this.getCache().keySet()) {
			if(key.endsWith("-" + unid)) {
				this.getCache().remove(key);
			}
		}
	}
	
	protected Map<String, Boolean> getCache() {
		if(this.cache == null) {
			this.cache = new ConcurrentHashMap<String, Boolean>();
		}
		return this.cache;
	}
	
	protected Database getMarkDB() throws Exception {
		Session session = JSFUtil.getSessionAsSigner();
		Database database = session.getDatabase("", "wow/forum-marks.nsf");
		JSFUtil.registerProductObject(database);
		return database;
	}
	protected View getMarksByKey() throws Exception {
		View view = this.getMarkDB().getView("Marks by Key");
		JSFUtil.registerProductObject(view);
		return view;
	}
	protected View getMarksByUNID() throws Exception {
		View view = this.getMarkDB().getView("Marks by UNID");
		JSFUtil.registerProductObject(view);
		return view;
	}

	private static final long serialVersionUID = 1377082132485250295L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException { aInputStream.defaultReadObject(); }
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}