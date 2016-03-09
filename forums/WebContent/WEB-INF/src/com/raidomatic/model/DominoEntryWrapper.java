package com.raidomatic.model;

import java.util.Vector;

import com.raidomatic.JSFUtil;

import lotus.domino.Document;
import lotus.domino.NotesException;
import lotus.domino.ViewEntry;

@SuppressWarnings("unchecked")
public class DominoEntryWrapper {
	ViewEntry entry = null;
	Document doc = null;
	String universalId = null;

	public DominoEntryWrapper(ViewEntry entry) throws NotesException {
		//System.out.println("DominoEntryWrapper creating with " + entry);
		//entry.getUniversalID();
		this.entry = entry;
		entry.setPreferJavaDates(true);
	}
	public DominoEntryWrapper(Document doc) throws NotesException {
		this.doc = doc;
	}

	public String getUniversalID() throws NotesException {
		if(this.universalId == null) {
			this.universalId = this.entry == null ? doc.getUniversalID() : entry.getUniversalID();
		}
		return this.universalId;
	}

	public Vector getColumnValues() throws NotesException {
		if(this.entry == null) {
			//System.out.println("Doc column values: " + this.doc.getColumnValues());
		}
		return this.entry == null ? this.doc.getColumnValues() : this.entry.getColumnValues();
	}

	public Document getDocument() throws NotesException {
		Document doc = this.entry == null ? this.doc : this.entry.getDocument();
		JSFUtil.registerProductObject(doc);
		return doc;
	}

	public boolean isNull() { return this.entry == null && this.doc == null; }

	public void recycle() {
		//		try {
		//			if(this.entry != null) { this.entry.recycle(); }
		//			if(this.doc != null) { this.doc.recycle(); }
		//		} catch(Exception e) { }
		this.entry = null;
		this.doc = null;
	}
}