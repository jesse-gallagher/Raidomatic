/*
 * The views in the Domino database are meant to be read-only - any add/remove operations
 * will be handled by the *List models. Thus, many of the normal List interface operations
 * don't apply.
 */

package com.raidomatic.model;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.*;
import com.raidomatic.JSFUtil;
import java.lang.reflect.*;
import lotus.domino.*;
import com.ibm.xsp.model.TabularDataModel;

//extends AbstractList<E>
public abstract class AbstractDominoList<E extends AbstractModel> extends TabularDataModel implements Serializable, List<E> {
	protected int cachedEntryCount = -1;
	transient protected DominoEntryCollectionWrapper cachedEntryCollection = null;
	protected String sortBy = null;
	protected Object restrictTo = null;
	protected String searchQuery = null;
	protected Map<Integer, E> cache;
	protected Date objectCreated;
	protected boolean singleEntry = false;

	public AbstractDominoList() {
		this.objectCreated = new Date();
	}
	public void preCache(int count) {
		// cache is how many objects should be fetched immediately, rather than waiting for each call of .get()
		// -1 means "all"
		if(count == -1 || count > 0) {
			try {
				DominoEntryCollectionWrapper collection = this.getEntries();
				int upTo = count == -1 ? collection.getCount() : count > collection.getCount() ? collection.getCount() : count;
				//System.out.println(this.getClass().getName() + ": pre-fetching " + upTo + " objects");
				for(int i = 0; i < upTo; i++) {
					DominoEntryWrapper entry = collection.getNthEntry(i+1);
					//					System.out.println("Creating " + this.getClass().getName() + " " + count++);
					E obj = this.createObjectFromViewEntry(entry);
					if(obj != null) {
						this.getCache().put(i, obj);
					}
					//entry.recycle();
				}
				//collection.recycle();
			} catch(Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Date getObjectCreated() { return this.objectCreated; }

	abstract protected String[] getColumnFields();
	protected void setSortBy(String sortBy) { this.sortBy = sortBy; }
	protected void setRestrictTo(Object restrictTo) { this.restrictTo = restrictTo; }
	protected void setSearchQuery(String searchQuery) { this.searchQuery = searchQuery; }
	protected void setSingleEntry(boolean singleEntry) { this.singleEntry = singleEntry; }

	@SuppressWarnings("unchecked")
	protected synchronized E createObjectFromViewEntry(DominoEntryWrapper entry) throws Exception {
		Class currentClass = this.getClass();
		Class modelClass = Class.forName(JSFUtil.strLeftBack(currentClass.getName(), ".") + "." + JSFUtil.strLeftBack(currentClass.getSimpleName(), "List"));

		// Create an instance of our model object
		E object = (E)modelClass.newInstance();
		object.setUniversalId(entry.getUniversalID());
		object.setDocExists(true);

		// Loop through the declared columns and extract the values from the Entry
		Method[] methods = modelClass.getMethods();
		String[] columns = this.getColumnFields();

		Vector values = entry.getColumnValues();
		for(Method method : methods) {
			for(int i = 0; i < columns.length; i++) {
				if(values != null && i < values.size() && columns[i].length() > 0 && method.getName().equals("set" + columns[i].substring(0, 1).toUpperCase() + columns[i].substring(1))) {
					String fieldType = method.getGenericParameterTypes()[0].toString();
					if(fieldType.equals("int")) {
						if(values.get(i) instanceof Double) {
							method.invoke(object, ((Double)values.get(i)).intValue());
						}
					} else if(fieldType.equals("class java.lang.String")) {
						method.invoke(object, values.get(i).toString());
					} else if(fieldType.equals("java.util.List<java.lang.String>")) {
						method.invoke(object, JSFUtil.toStringList(values.get(i)));
					} else if(fieldType.equals("class java.util.Date")) {
						if(values.get(i) instanceof DateTime) {
							method.invoke(object, ((DateTime)values.get(i)).toJavaDate());
						} else if(values.get(i) instanceof Date) {
							method.invoke(object, (Date)values.get(i));
						}
					} else if(fieldType.equals("boolean")) {
						if(values.get(i) instanceof Double) {
							method.invoke(object, ((Double)values.get(i)).intValue() == 1);
						} else {
							method.invoke(object, values.get(i).toString().equals("Yes"));
						}
					} else if(fieldType.equals("double")) {
						if(values.get(i) instanceof Double) { method.invoke(object, (Double)values.get(i)); }
					} else if(fieldType.equals("java.util.List<java.lang.Integer>")) {
						method.invoke(object, JSFUtil.toIntegerList(values.get(i)));
					} else if(fieldType.equals("class java.lang.Integer")) {
						if(values.get(i) instanceof Double) { method.invoke(object, ((Double)values.get(i)).intValue()); }
					} else {
						System.out.println("!! unknown type " + fieldType);
					}
				}
			}
		}

		JSFUtil.getModelIDCache().put(JSFUtil.getUserName() + "-" + object.getClass().getSimpleName() + "-" + object.getId(), object);

		return object;
	}

	@Override
	public int size() {
		try {
			if(this.cachedEntryCount < 0) {
				this.cachedEntryCount = this.getEntries().getCount();
			}
			return this.cachedEntryCount;
		} catch(NotesException ne) { }
		return 0;
	}

	@Override
	public E get(int index) {
		E result = null;
		Map<Integer, E> cache = this.getCache();
		if(!cache.containsKey(index)) {
			//System.out.println(this.getClass().getName() + ": Didn't find index " + index + " in the cache");
			try {
				// Fetch the entry
				//DominoEntryWrapper entry = this.getNthViewEntry(index);
				DominoEntryCollectionWrapper collection = this.getEntries();
				DominoEntryWrapper entry = collection.getNthEntry(index+1);

				// The wrapper shouldn't be null, but the entry inside of it may be
				if(entry != null) {
					E obj = this.createObjectFromViewEntry(entry);
					if(obj != null) {
						cache.put(index, obj);
					}
					entry.recycle();
					//collection.recycle();
				} else {
					//					System.out.println("Requested index " + index + " in " + this.getClass().getName() + " was null.");
					//					System.out.println("Sort by: '" + sortBy + "'");
					//					System.out.println("Restrict to: '" + restrictTo + "'");
					//					System.out.println("Search query: " + searchQuery);
					//this.cache.put(index, null);
					//collection.recycle();
					return null;
				}
			} catch(Exception ne) {
				ne.printStackTrace();
				return null;
			}
		} else {
			//System.out.println(this.getClass().getName() + ": reading " + index + " from cache");
		}
		result = cache.get(index);
		return result;
	}

	public Vector<E> toVector() {
		Vector<E> result = new Vector<E>();
		for(int i = 0; i < this.size(); i++) {
			result.add(this.get(i));
		}
		return result;
	}
	public List<E> toList() {
		List<E> result = new ArrayList<E>(this.size());
		//result.addAll(this);
		for(E e : this) {
			result.add(e);
		}
		return result;
	}
	@Override
	public Object[] toArray() { return this.toList().toArray(); }
	@Override public <T> T[] toArray(T[] a) { return (T[])this.toList().toArray(a); }

	/* Grab bag of remaining List methods */
	@Override public Iterator<E> iterator() { return new BasicDominoIterator<E>(this); }
	@Override public ListIterator<E> listIterator() { return new BasicDominoIterator<E>(this); }
	@Override
	public ListIterator<E> listIterator(int index) {
		if(index < 0 || index >= this.size()) { throw new IndexOutOfBoundsException(); }
		return new BasicDominoIterator<E>(this, index);
	}
	@Override public boolean removeAll(Collection<?> c) { throw new UnsupportedOperationException(); }
	@Override public boolean retainAll(Collection<?> c) { throw new UnsupportedOperationException(); }
	@Override public E remove(int i) { throw new UnsupportedOperationException(); }
	@Override public boolean remove(Object object) { throw new UnsupportedOperationException(); }
	@Override
	public boolean containsAll(Collection<?> collection) {
		for(Object obj : collection) {
			if(!this.contains(obj)) { return false; }
		}
		return true;
	}
	@Override public E set(int index, E element) { throw new UnsupportedOperationException(); }
	@Override public boolean add(E e) { throw new UnsupportedOperationException(); }
	@Override public void add(int index, E element) { throw new UnsupportedOperationException(); }
	@Override public boolean isEmpty() { return this.size() == 0; }
	@Override public int indexOf(Object obj) {
		if(obj == null) { return -1; }
		for(int i = 0; i < this.size(); i++) {
			if(this.get(i).equals(obj)) { return i; }
		}
		return -1;
	}
	@Override public void clear() { throw new UnsupportedOperationException(); }
	@Override public boolean contains(Object obj) {
		for(E element : this) {
			if(element.equals(obj)) { return true; }
		}
		return false;
	}
	@Override public List<E> subList(int fromIndex, int toIndex) {
		if(fromIndex < 0 || toIndex > this.size() || fromIndex > toIndex) { throw new IndexOutOfBoundsException(); }
		List<E> result = new ArrayList<E>(toIndex - fromIndex);
		for(int i = fromIndex; i < toIndex; i++) {
			result.add(this.get(i));
		}
		return result;
	}
	@Override public int lastIndexOf(Object obj) {
		if(obj == null) { return -1; }
		for(int i = this.size()-1; i >= 0; i--) {
			if(this.get(i).equals(obj)) { return i; }
		}
		return -1;
	}
	@Override public boolean addAll(Collection<? extends E> c) { throw new UnsupportedOperationException(); }
	@Override public boolean addAll(int index, Collection<? extends E> c) { throw new UnsupportedOperationException(); }

	/* TabularDataModel methods */
	@Override public int getRowCount() { return this.size(); }
	@Override public Object getRowData() { return this.get(this.getRowIndex()); }

	public void recycle() {
		// Try to recycle the cache, but I really don't care if it fails
		try {
			if(this.cachedEntryCollection != null) {
				this.cachedEntryCollection.recycle();
			}
		} catch(Exception e) { }
		this.cachedEntryCollection = null;
	}

	protected boolean isInView(E obj) throws NotesException { return false; }

	protected String getDefaultSort() { return ""; }
	protected String getViewName() { return ""; }
	protected String getDatabaseServer() throws NotesException { return ""; }
	protected String getDatabasePath() throws NotesException { return ""; }
	protected Session getSession() { return this.isSessionAsSigner() ? JSFUtil.getSessionAsSigner() : JSFUtil.getSession(); }
	protected boolean isSessionAsSigner() { return false; }

	public Database getDatabase() throws NotesException {
		Database database;
		if(this.getDatabaseServer().length() == 0 && this.getDatabasePath().length() == 0) {
			database = JSFUtil.getDatabase();
		} else if(this.getDatabaseServer().length() == 0) {
			String server = JSFUtil.getDatabase().getServer();
			String path = this.getDatabasePath();
			Session session = this.getSession();
			database = session.getDatabase(server, path);

			JSFUtil.registerProductObject(database);
		} else {
			database = this.getSession().getDatabase(this.getDatabaseServer(), this.getDatabasePath());
			JSFUtil.registerProductObject(database);
		}
		return database;
	}
	protected DominoEntryCollectionWrapper getEntries() throws NotesException {
		if(cachedEntryCollection == null) {
			cachedEntryCollection = this.fetchNewEntryCollection();
		} else {
			// The one we have may be recycled, to try a method and see if it complains
			if(!cachedEntryCollection.isStillGood()) {
				cachedEntryCollection = this.fetchNewEntryCollection();
			}
		}
		return cachedEntryCollection;
	}
	protected DominoEntryCollectionWrapper fetchNewEntryCollection() throws NotesException {
		DominoEntryCollectionWrapper result = null;

		// Fecth the view
		Database database = this.getDatabase();
		View view = database.getView(this.getViewName());
		//View view = JSFUtil.getView(this.getDatabaseServer(), this.getDatabasePath(), this.getViewName());
		JSFUtil.registerProductObject(view);
		//view.setAutoUpdate(false);
		//view.refresh();
		String sortColumn = this.sortBy == null ? null :
			this.sortBy.toLowerCase().endsWith("-desc") ? this.sortBy.substring(0, this.sortBy.length()-5) :
				this.sortBy;
			boolean sortAscending = this.sortBy == null || !this.sortBy.toLowerCase().endsWith("-desc");
			if(this.searchQuery != null) {
				// Sorting isn't relevant when searching
				// More importantly, FTSearch'ing a resortView'd view leads to bad behavior 
				//view.resortView();
				//System.out.println("Searching for " + this.searchQuery);
				view.FTSearchSorted(this.searchQuery, 0, sortColumn, sortAscending, false, false, false);
				//view.FTSearchSorted(this.searchQuery, 0, "DateTime", false, false, false, false);
			} else if(this.sortBy != null && !this.sortBy.equals(this.getDefaultSort())) {
				view.resortView(sortColumn, sortAscending);
				//			if(this.sortBy.toLowerCase().endsWith("-desc")) {
				//				view.resortView(this.sortBy.substring(0, this.sortBy.length()-5), false);
				//			} else {
				//				view.resortView(this.sortBy, true);
				//			}
			} else {
				// Restore to default sorting, which apparently matters - I'd have thought a fresh .getView() would come with the default sort
				view.resortView();
			}


			// ViewNavigator is significantly faster than ViewEntryCollection, but doesn't obey .FTSearch() and can't be used to get a non-categorized subset
			if(this.restrictTo == null && this.searchQuery == null) {
				//result = new DominoEntryCollectionWrapper(view.createViewNav());
				ViewEntryCollection collection = view.getAllEntries();
				JSFUtil.registerProductObject(collection);
				result = new DominoEntryCollectionWrapper(collection, view);
			} else if(this.restrictTo == null) {
				ViewEntryCollection collection = view.getAllEntries();
				JSFUtil.registerProductObject(collection);
				result = new DominoEntryCollectionWrapper(collection, view);
			} else {
				if(singleEntry) {
					//System.out.println("Getting single entry for " + this.restrictTo);
					ViewEntry entry = view.getEntryByKey(this.restrictTo, true);
					JSFUtil.registerProductObject(entry);
					result = new DominoEntryCollectionWrapper(entry, view);
				} else {
					ViewEntryCollection collection = view.getAllEntriesByKey(this.restrictTo, true);
					JSFUtil.registerProductObject(collection);
					result = new DominoEntryCollectionWrapper(collection, view);
				}

				// getAllDocumentsByKey may or may not be faster (http://www.ibm.com/developerworks/lotus/library/notes7-application-performance1/);
				//	however, its documents don't have a ColumnValues property and may be out of order, so this isn't terribly useful
				// 8.5.3 should allow sorting of .FTSearch()'d views, so that will be worth looking into
				//result = new DominoEntryCollectionWrapper(view.getAllDocumentsByKey(this.restrictTo, true));
			}
			return result;
	}
	protected DominoEntryWrapper getNthViewEntry(int index) throws NotesException {
		DominoEntryWrapper entry = null;
		DominoEntryCollectionWrapper collection = this.getEntries();
		entry = collection.getNthEntry(index+1);
		return entry;
	}

	protected Map<Integer, E> getCache() {
		if(this.cache == null) {
			this.cache = new ConcurrentHashMap<Integer, E>();
		}
		return this.cache;
	}

	private static final long serialVersionUID = 5598396399214624956L;

	@Override
	public String toString() {
		return "[" + this.getClass().getName() + "]";
	}

	@SuppressWarnings("hiding")
	public class BasicDominoIterator<E extends AbstractModel> implements Iterator<E>, ListIterator<E> {
		private AbstractDominoList<E> parent = null;
		private int index = 0;

		public BasicDominoIterator(AbstractDominoList<E> parent) { this.parent = parent; }
		public BasicDominoIterator(AbstractDominoList<E> parent, int index) {
			this.parent = parent;
			this.index = index;
		}

		@Override public boolean hasNext() { return this.index < parent.size(); }
		@Override public E next() { if(this.index == this.parent.size()) { throw new NoSuchElementException(); } return this.parent.get(this.index++); }
		@Override public void remove() { throw new UnsupportedOperationException(); }
		@Override public void add(E arg0) { throw new UnsupportedOperationException(); }
		@Override public boolean hasPrevious() { return this.index > -1; }
		@Override public int nextIndex() { return this.index == this.parent.size() ? this.index : this.index + 1; }

		@Override
		public E previous() {
			// TODO Auto-generated method stub
			return null;
		}

		@Override
		public int previousIndex() {
			// TODO Auto-generated method stub
			return 0;
		}

		@Override
		public void set(E arg0) {
			// TODO Auto-generated method stub

		}
	}
}

class DominoIterator<E extends AbstractModel> implements Iterator<E>, Serializable, ListIterator<E> {
	int currentIndex = -1;
	AbstractDominoList<E> parent = null;

	public DominoIterator(AbstractDominoList<E> parent) {
		this.parent = parent;
	}
	public DominoIterator(AbstractDominoList<E> parent, int index) throws IndexOutOfBoundsException {
		this.parent = parent;
		if(index < 0 || index >= parent.size()) {
			throw new IndexOutOfBoundsException();
		}
		this.currentIndex = index;
	}

	public boolean hasNext() {
		return currentIndex < parent.size() - 1;
	}
	public E next() throws NoSuchElementException {
		if(!this.hasNext()) { throw new NoSuchElementException(); }
		return parent.get(++currentIndex);
	}
	public int nextIndex() {
		return currentIndex >= parent.size() ? parent.size() : currentIndex+1;
	}

	public boolean hasPrevious() {
		return currentIndex >= 0;
	}
	public E previous() throws NoSuchElementException {
		if(!this.hasPrevious()) { throw new NoSuchElementException(); }
		return parent.get(--currentIndex);
	}
	public int previousIndex() {
		return currentIndex <= 0 ? -1 : currentIndex-1;
	}

	public void remove() { }

	private static final long serialVersionUID = 8585746048585649596L;

	public void add(E arg0) throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
	public void set(E arg0) throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
}

class DominoEntryCollectionWrapper {
	ViewEntryCollection collection = null;
	ViewNavigator navigator = null;
	DocumentCollection docCollection = null;
	DominoEntryWrapper entry = null;
	View view = null;
	int count = 0;

	public DominoEntryCollectionWrapper(ViewEntryCollection collection, View view) throws NotesException {
		this.collection = collection;
		this.view = view;
		this.count = collection.getCount();
	}
	public DominoEntryCollectionWrapper(ViewNavigator navigator, View view) throws NotesException {
		this.navigator = navigator;
		this.view = view;
		this.count = navigator.getCount();
	}
	public DominoEntryCollectionWrapper(DocumentCollection docCollection, View view) throws NotesException {
		this.docCollection = docCollection;
		this.view = view;
		this.count = docCollection.getCount();
	}
	public DominoEntryCollectionWrapper(ViewEntry entry, View view) throws NotesException {
		this.entry = new DominoEntryWrapper(entry);
		this.view = view;
		this.count = 1;
	}

	public DominoEntryWrapper getFirstEntry() throws NotesException {
		if(this.entry != null) {
			return entry;
		} else if(this.collection != null) {
			ViewEntry entry = this.collection.getFirstEntry();
			entry.setPreferJavaDates(true);
			JSFUtil.registerProductObject(entry);
			return new DominoEntryWrapper(entry);
		} else if(this.navigator != null) {
			ViewEntry entry = this.navigator.getFirst();
			entry.setPreferJavaDates(true);
			JSFUtil.registerProductObject(entry);
			return new DominoEntryWrapper(entry);
		} else if(this.docCollection != null) {
			Document doc = this.docCollection.getFirstDocument();
			doc.setPreferJavaDates(true);
			JSFUtil.registerProductObject(doc);
			return new DominoEntryWrapper(doc);
		}
		return null;
	}
	public DominoEntryWrapper getNextEntry(DominoEntryWrapper entry) throws NotesException {
		if(this.entry != null) {
			return null;
		} else if(this.collection != null) {
			ViewEntry entryObj = this.collection.getNextEntry();
			entryObj.setPreferJavaDates(true);
			JSFUtil.registerProductObject(entryObj);
			return new DominoEntryWrapper(entryObj);
		} else if(this.navigator != null) {
			ViewEntry entryObj = this.navigator.getNext();
			entryObj.setPreferJavaDates(true);
			JSFUtil.registerProductObject(entryObj);
			return new DominoEntryWrapper(entryObj);
		} else if(this.docCollection != null) {
			Document doc = this.docCollection.getNextDocument();
			doc.setPreferJavaDates(true);
			JSFUtil.registerProductObject(doc);
			return new DominoEntryWrapper(doc);
		}
		return null;
	}
	public DominoEntryWrapper getNthEntry(int n) throws NotesException {
		String context = "";
		try {
			if(this.entry != null) {
				context = "single";
				return n == 1 ? entry : null;
			} else if(this.collection != null) {
				context = "collection";
				ViewEntry entry = this.collection.getNthEntry(n);
				entry.setPreferJavaDates(true);
				JSFUtil.registerProductObject(entry);
				return new DominoEntryWrapper(entry);
			} else if(this.navigator != null) {
				context = "navigator";
				ViewEntry entry = this.navigator.getNth(n);
				entry.setPreferJavaDates(true);
				JSFUtil.registerProductObject(entry);
				return new DominoEntryWrapper(entry);
			} else if(this.docCollection != null) {
				context = "docCollection";
				Document doc = this.docCollection.getNthDocument(n);
				doc.setPreferJavaDates(true);
				JSFUtil.registerProductObject(doc);
				return new DominoEntryWrapper(doc);
			}
		} catch(NullPointerException e) {
			System.out.println("Exception in DominoEntryWrapper in " + context);
			throw e;
		}
		return null;
	}
	public int getCount() throws NotesException {
		return this.count;
	}

	public boolean isStillGood() {
		try {
			if(this.collection != null) {
				Base obj = this.collection.getParent();
				JSFUtil.registerProductObject(obj);
				return true;
			} else if(this.navigator != null) {
				Base obj = this.navigator.getParentView();
				JSFUtil.registerProductObject(obj);
				return true;
			} else if(this.docCollection != null) {
				Base obj = this.docCollection.getParent();
				JSFUtil.registerProductObject(obj);
				return true;
			}
		} catch(NotesException ne) { }
		return false;
	}

	public void recycle() {
		//		try {
		//			if(this.view != null) {
		//				this.view.recycle();
		//			}
		//		} catch(NotesException e) { }
		//		try {
		//			
		//			
		//			if(this.collection != null) {
		//				this.collection.recycle();
		//			} else if(this.navigator != null) {
		//				this.navigator.recycle();
		//			} else if(this.docCollection != null) {
		//				this.docCollection.recycle();
		//			}
		//		} catch(NotesException e) { }
		this.collection = null;
		this.navigator = null;
		this.docCollection = null;
	}
}