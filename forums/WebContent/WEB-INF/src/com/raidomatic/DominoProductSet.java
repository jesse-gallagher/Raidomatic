package com.raidomatic;

import lotus.domino.*;
import java.util.concurrent.*;

public class DominoProductSet extends CopyOnWriteArraySet<Base> {
	public void recycle() {
		//		for(Base domObj : this) {
		//			if(domObj != null) {
		//				try { domObj.recycle(); } catch(NotesException ne) { System.out.println(ne); }
		//				this.remove(domObj);
		//			}
		//		}
	}
	@Override
      public boolean add(Base obj) {
		if(obj != null) {
			return super.add(obj);
		}
		return false;
	}

	private static final long serialVersionUID = -1153707029389155393L;
}
