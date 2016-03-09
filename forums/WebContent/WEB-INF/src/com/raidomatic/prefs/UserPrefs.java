package com.raidomatic.prefs;

import java.util.*;
import java.util.concurrent.*;

import com.raidomatic.JSFUtil;
import com.raidomatic.model.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;


public class UserPrefs implements Serializable {

	private transient Map<String, PrefObject> map;

	public UserPrefs() {
		this.map = new ConcurrentHashMap<String, PrefObject>();
	}

	public PrefObject get(Object arg0) {
		if(!this.map.containsKey(arg0)) {
			this.map.put(arg0.toString(), new PrefObject(arg0.toString()));
		}
		return this.map.get(arg0.toString());
	}
	public PrefObject getForUser(String user) {
		if(!map.containsKey(user)) {
			try {
				Players players = new Players();
				Player player = players.getByName(user);
				if(player == null) {
					map.put(user, this.get("anonymous"));
				}
				map.put(user, this.get(player.getShortName()));
			} catch(Exception e) {
			}
		}
		return map.get(user);
	}

	public PrefObject getCurrent() {
		//		Players players = new Players();
		//		Player player = players.getCurrent();
		//		if(player == null) {
		//			return this.get("anonymous");
		//		}
		//		return this.get(player.getShortName());
		return this.getForUser(JSFUtil.getUserName());
	}

	private static final long serialVersionUID = 8189435956358448904L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException {
		aInputStream.defaultReadObject();
		this.map = new ConcurrentHashMap<String, PrefObject>();
	}
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}
