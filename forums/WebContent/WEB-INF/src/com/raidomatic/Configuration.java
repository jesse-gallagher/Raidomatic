package com.raidomatic;

import java.util.*;
import lotus.domino.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

@SuppressWarnings("unchecked")
public class Configuration implements Serializable {
	transient Map<String, Vector> itemValueCache;
	transient Map<String, Vector> documentItemValueCache;
	private transient View configView;

	public Configuration() {
		this.itemValueCache = new HashMap<String, Vector>();
		this.documentItemValueCache = new HashMap<String, Vector>();
	}

	public Vector getDocumentItemValue(String docName, String fieldName) throws Exception {
		if(!this.documentItemValueCache.containsKey(fieldName.toLowerCase() + docName.toLowerCase())) {
			Vector result;
			View configView = this.getConfigView();
			Document configDoc = configView.getDocumentByKey(docName, true);
			if(configDoc != null) {
				JSFUtil.registerProductObject(configDoc);
				this.documentItemValueCache.put(fieldName.toLowerCase() + docName.toLowerCase(), configDoc.getItemValue(fieldName));
			} else {
				result = new Vector();
				result.add("");
				this.documentItemValueCache.put(fieldName.toLowerCase() + docName.toLowerCase(), result);
			}
		}
		return this.documentItemValueCache.get(fieldName.toLowerCase() + docName.toLowerCase());
	}
	public void setDocumentItemValue(String docName, String fieldName, Object newValue) throws Exception {
		View configView = this.getConfigView();
		Document configDoc = configView.getDocumentByKey(docName, true);
		if(configDoc != null) {
			JSFUtil.registerProductObject(configDoc);
			configDoc.replaceItemValue(fieldName, newValue);
			configDoc.save();
			this.documentItemValueCache.put(fieldName.toLowerCase() + docName.toLowerCase(), configDoc.getItemValue(fieldName));
		}
	}

	public Vector getItemValue(String fieldName) throws Exception {
		if(!this.itemValueCache.containsKey(fieldName.toLowerCase())) {
			Vector result = null;

			View configView = this.getConfigView();
			Document configDoc = configView.getFirstDocument();
			while(configDoc != null) {
				JSFUtil.registerProductObject(configDoc);
				if(configDoc.hasItem(fieldName)) {
					result = configDoc.getItemValue(fieldName);
					break;
				}

				configDoc = configView.getNextDocument(configDoc);
			}
			if(result == null) {
				result = new Vector();
				result.add("");
			}
			this.itemValueCache.put(fieldName.toLowerCase(), result);
		}
		return this.itemValueCache.get(fieldName.toLowerCase());
	}

	public void clearCache() {
		this.itemValueCache.clear();
		this.documentItemValueCache.clear();
	}

	private View getConfigView() throws Exception {
		if(this.configView == null) {
			Session session = JSFUtil.getSessionAsSigner();
			Database common = session.getDatabase("", "wow/common.nsf");
			this.configView = common.getView("Configuration");
			this.configView.setAutoUpdate(false);

			JSFUtil.registerProductObject(common);
			JSFUtil.registerProductObject(configView);
		}

		return this.configView;
	}


	private static final long serialVersionUID = -7961078785403769410L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
		this.itemValueCache = new HashMap<String, Vector>();
		this.documentItemValueCache = new HashMap<String, Vector>();
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}
