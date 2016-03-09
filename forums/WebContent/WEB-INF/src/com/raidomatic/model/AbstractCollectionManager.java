package com.raidomatic.model;

import java.io.Serializable;
import java.util.*;
import com.raidomatic.JSFUtil;

public abstract class AbstractCollectionManager<E extends AbstractModel> implements Serializable {

	public List<E> getAll() throws Exception {
		return this.getCollection();
	}
	public List<E> search(String searchQuery) throws Exception {
		return this.getCollection(null, null, searchQuery);
	}
	public List<E> search(String searchQuery, int preCache) throws Exception {
		return this.getCollection(null, null, searchQuery, preCache);
	}
	public List<E> search(String searchQuery, String sortBy) throws Exception {
		return this.getCollection(sortBy, null, searchQuery);
	}
	public List<E> search(String searchQuery, String sortBy, int preCache) throws Exception {
		return this.getCollection(sortBy, null, searchQuery, preCache);
	}

	@SuppressWarnings("unchecked")
	public E getById(String id) throws Exception {
		if(JSFUtil.getModelIDCache().containsKey(JSFUtil.getUserName() + "-" + getObjectSimpleName() + "-" + id)) {
			return (E)JSFUtil.getModelIDCache().get(JSFUtil.getUserName() + "-" + getObjectSimpleName() + "-" + id);
		}
		return this.getFirstOfCollection(null, id);
	}

	protected E getFirstOfCollection() { return this.getFirstOfCollection(null, null, null); }
	protected E getFirstOfCollection(String sortBy) { return this.getFirstOfCollection(sortBy, null, null); }
	protected E getFirstOfCollection(String sortBy, Object restrictTo) { return this.getFirstOfCollection(sortBy, restrictTo, null); }
	protected E getFirstOfCollection(String sortBy, Object restrictTo, String searchQuery) {
		List<E> result = this.getCollection(sortBy, restrictTo, searchQuery, 1);
		return result.size() > 0 ? result.get(0) : null;
	}

	protected String getPackageName() {
		String className = this.getClass().getName();
		String simpleName = this.getClass().getSimpleName();
		return className.substring(0, className.length()-simpleName.length()-1);
	}
	protected String getCollectionName() {
		return this.getPackageName() + "." + this.getCollectionSimpleName();
	}
	protected String getCollectionSimpleName() {
		String singular = JSFUtil.singularize(this.getClass().getSimpleName());
		return singular + "List";
	}
	protected String getObjectSimpleName() {
		return JSFUtil.singularize(this.getClass().getSimpleName());
	}

	abstract protected AbstractDominoList<E> createCollection();
	protected AbstractDominoList<E> createCollection(String sortBy) { return this.createCollection(sortBy, null, null, 0, false); }
	protected AbstractDominoList<E> createCollection(String sortBy, Object restrictTo) { return this.createCollection(sortBy, restrictTo, null, 0, false); }
	protected AbstractDominoList<E> createCollection(String sortBy, Object restrictTo, String searchQuery) { return this.createCollection(sortBy, restrictTo, searchQuery, 0, false); }
	protected AbstractDominoList<E> createCollection(String sortBy, Object restrictTo, String searchQuery, int preCache) { return this.createCollection(sortBy, restrictTo, searchQuery, preCache, false); }
	protected AbstractDominoList<E> createCollection(String sortBy, Object restrictTo, String searchQuery, int preCache, boolean singleEntry) {
		AbstractDominoList<E> collection = this.createCollection();
		collection.setSortBy(sortBy);
		collection.setRestrictTo(restrictTo);
		collection.setSearchQuery(searchQuery);
		collection.setSingleEntry(singleEntry);
		collection.preCache(preCache);
		return collection;
	}

	protected AbstractDominoList<E> getCollection() { return this.getCollection(null, null, null, 0, false); }
	protected AbstractDominoList<E> getCollection(String sortBy) { return this.getCollection(sortBy, null, null, 0, false); }
	protected AbstractDominoList<E> getCollection(String sortBy, Object restrictTo) { return this.getCollection(sortBy, restrictTo, null, 0, false); }
	protected AbstractDominoList<E> getCollection(String sortBy, Object restrictTo, String searchQuery) { return this.getCollection(sortBy, restrictTo, searchQuery, 0, false); }
	protected AbstractDominoList<E> getCollection(String sortBy, Object restrictTo, String searchQuery, int preCache) { return this.getCollection(sortBy, restrictTo, searchQuery, preCache, false); }
	@SuppressWarnings("unchecked")
	protected AbstractDominoList<E> getCollection(String sortBy, Object restrictTo, String searchQuery, int preCache, boolean singleEntry) {
		Map<String, AbstractDominoList> cache = JSFUtil.getModelCache();
		String userName = JSFUtil.getUserName();
		String cacheKey = userName + "-" +
		this.getCollectionSimpleName() + "-" +
		(sortBy == null ? "DEFAULT" : sortBy) +
		(restrictTo == null ? "" : "-" + restrictTo) +
		(searchQuery == null ? "" : "-" + searchQuery) + "-" +
		singleEntry;

		AbstractDominoList<E> cached = (AbstractDominoList<E>)cache.get(cacheKey);
		if(cached == null) {
			cached = this.createCollection(sortBy, restrictTo, searchQuery, preCache, singleEntry);
			cache.put(cacheKey, cached);
		}
		return cached;
	}

	private static final long serialVersionUID = -6264594496068552303L;
}
