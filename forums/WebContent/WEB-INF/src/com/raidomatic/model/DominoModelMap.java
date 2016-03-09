/*
 * The views in the Domino database are meant to be read-only - any add/remove operations
 * will be handled by the *List models. Thus, many of the normal List interface operations
 * don't apply.
 */

package com.raidomatic.model;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import com.raidomatic.JSFUtil;

public class DominoModelMap<E extends AbstractModel> implements Map<String, E> {
	
	public void clear() throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
	public boolean containsValue(Object arg0) throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public Set<java.util.Map.Entry<String, E>> entrySet() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public boolean isEmpty() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public Set<String> keySet() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public E put(String arg0, E arg1) throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public void putAll(Map<? extends String, ? extends E> arg0) throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public E remove(Object arg0) throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public int size() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public Collection<E> values() throws UnsupportedOperationException {throw new UnsupportedOperationException();}
	public boolean containsKey(Object arg0) throws UnsupportedOperationException { throw new UnsupportedOperationException(); }
	
	@SuppressWarnings("unchecked")
	public E get(Object id) {
		try {
			AbstractCollectionManager collectionManager = (AbstractCollectionManager)Class.forName(this.getPackageName() + "." + this.getCollectionManagerSimpleName()).newInstance();
			return (E)collectionManager.getById(id.toString());
		} catch(Exception e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	protected String getPackageName() {
		String className = this.getClass().getName();
		String simpleName = this.getClass().getSimpleName();
		return className.substring(0, className.length()-simpleName.length()-1);
	}
	protected String getCollectionManagerSimpleName() {
		String simpleName = this.getClass().getSimpleName();
		String singular = simpleName.substring(0, simpleName.length() - 10);
		return JSFUtil.pluralize(singular);
	}
}
