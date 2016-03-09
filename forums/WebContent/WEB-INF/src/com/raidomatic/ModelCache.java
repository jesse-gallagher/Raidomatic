package com.raidomatic;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import java.util.concurrent.*;
import com.raidomatic.model.AbstractDominoList;
import java.util.regex.*;
import lotus.domino.*;

@SuppressWarnings("unchecked")
public class ModelCache extends ConcurrentHashMap<String, AbstractDominoList> {
	transient static ModelCache instance = null;

	private ModelCache() {
		super();
	}
	public synchronized static ModelCache getInstance() {
		if(instance == null) {
			instance = new ModelCache();
		}
		return instance;
	}

	@Override
	public AbstractDominoList put(String key, AbstractDominoList value) {
		//System.out.println("Adding to cache: " + key);
		return super.put(key, value);
	}

	@Override
	public AbstractDominoList get(Object key) {
		boolean contains = super.containsKey(key);
		if(contains) {
			AbstractDominoList value = super.get(key);
			if(value != null) {
				Date dbModified = null;
				try {
					DateTime dt = value.getDatabase().getLastModified();
					dbModified = dt.toJavaDate();
					dt.recycle();
				}
				catch(NotesException ne) { return null; }
				catch(NullPointerException npe) { npe.printStackTrace(); throw npe; }
				if(dbModified != null && dbModified.after(value.getObjectCreated())) {
					value.recycle();
					this.clear();

					// Also clear the DB cache in that case
					JSFUtil.getMiscDBCache().clear();

					return null;
				}
			}
		}
		return super.get(key);
	}
	//	public boolean containsKey(Object key) {
	// Check to see if the database has been changed since the element was fetched and,
	//	if so, clear out the element
	//		boolean contains = super.containsKey(key);
	//		if(contains) {
	//			AbstractDominoList value = super.get(key);
	//			if(value != null) {
	//				Date dbModified = null;
	//				try {
	//					dbModified = value.getDatabase().getLastModified().toJavaDate();
	//				} catch(NotesException ne) { }
	//				if(dbModified != null && dbModified.after(value.getObjectCreated())) {
	//					value.recycle();
	//					super.remove(key);
	//					contains = false;
	//				}
	//			}
	//		}
	//		return super.containsKey(key);
	//	}

	//	public void clear() {
	//		Set<String> keys = this.keySet();
	//		for(String key : keys) {
	//			this.remove(key);
	//		}
	//		
	//		//super.clear();
	//	}

	public void clearMatches(String regex) {
		Pattern pattern = Pattern.compile(regex);
		Set<String> keys = this.keySet();
		for(String key : keys) {
			if(pattern.matcher(key).matches()) {
				this.remove(key);
			}
		}
	}

	//	public AbstractDominoList remove(Object key) {
	//		AbstractDominoList obj = super.get(key);
	//		if(obj != null) {
	//			obj.recycle();
	//		}
	//		super.remove(key);
	//		return obj;
	//	}

	private static final long serialVersionUID = 8734559037732722221L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException { aInputStream.defaultReadObject(); }
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}
