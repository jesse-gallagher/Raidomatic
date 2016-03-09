package com.raidomatic;

//import java.util.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.concurrent.*;
import com.raidomatic.model.*;

public class ModelIDCache extends ConcurrentHashMap<String, AbstractModel> {
	transient static ModelIDCache instance = null;
	
//	private ModelIDCache() {
//		super();
//	}
//	public synchronized static ModelIDCache getInstance() {
//		if(instance == null) {
//			instance = new ModelIDCache();
//		}
//		return instance;
//	}

	private static final long serialVersionUID = -3248344714074060223L;
	private void readObject(ObjectInputStream aInputStream) throws ClassNotFoundException, IOException { aInputStream.defaultReadObject(); }
	private void writeObject(ObjectOutputStream aOutputStream) throws IOException { aOutputStream.defaultWriteObject(); }
}