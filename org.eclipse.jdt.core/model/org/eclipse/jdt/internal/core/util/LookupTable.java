package org.eclipse.jdt.internal.core.util;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

/**
 * A lookup table whose keys and values are Objects.
 * A lookup table is like a Hashtable, but uses linear probing to
 * resolve collisions rather than a linked list of hash table entries.
 */
public class LookupTable extends Dictionary implements Cloneable {
	protected int count;
	protected float loadFactor;
	protected int threshold;
	protected Object[] keys;
	protected Object[] values;
	protected int modCount;
	
	// Types of Enumeration
	protected static final int KEYS = 0;
	protected static final int VALUES = 1;

	/**
	 * A lookup table enumerator class. 
	 */
	protected class Enumerator implements Enumeration {

		protected int type;
		protected Object[] table;
		protected int index;
		protected Object entry;
		
		/**
		 * The modCount value that the enumeration believes that the backing
		 * lookup table should have.  If this expectation is violated, the enumeration 
		 * has detected concurrent modification.
		 */
		protected int expectedModCount = modCount;
		
	Enumerator(int type) {
	    this.type = type;
	    table = (type == KEYS ? keys : values);
	    index = table.length;
	}
		
	public boolean hasMoreElements() {
	    while (entry == null && index > 0)
			entry = table[--index];
	    return entry != null;
	}

	public Object nextElement() {
	    if (modCount != expectedModCount)
			throw new IllegalStateException();
	    while (entry == null && index > 0)
			entry = table[--index];
	    if (entry != null) {
	    	Object next = entry;
	    	entry = null;
	    	return next;
		}
	    throw new NoSuchElementException("LookupTable Enumerator");
	}
	}      	
	/**
	 * Constructs a new, empty LookupTable with a default capacity (11) and load
	 * factor (0.5). 
	 */
	public LookupTable() {
	this(11, 0.5f);
	}
	/**
	 * Constructs a new, empty LookupTable with the specified initial capacity
	 * and default load factor (0.5).
	 *
	 * @param     initialCapacity   the initial capacity of the lookup table.
	 * @exception IllegalArgumentException if the initial capacity is less
	 *              than zero.
	 */
	public LookupTable(int initialCapacity) {
	this(initialCapacity, 0.5f);
	}
	/**
	 * Constructs a new, empty LookupTable with the specified initial 
	 * capacity and the specified load factor.
	 *
	 * @param      initialCapacity   the initial capacity of the lookup table.
	 * @param      loadFactor        the load factor of the lookup table.
	 * @exception  IllegalArgumentException  if the initial capacity is less
	 *             than zero, or if the load factor is nonpositive.
	 */
	public LookupTable(int initialCapacity, float loadFactor) {
	if (initialCapacity < 0)
	    throw new IllegalArgumentException("Illegal Capacity: "+
											   initialCapacity);
		if (loadFactor <= 0)
			throw new IllegalArgumentException("Illegal Load: "+loadFactor);

		if (initialCapacity==0)
			initialCapacity = 1;
	this.loadFactor = loadFactor;
	keys = new Object[initialCapacity];
	values = new Object[initialCapacity];
	threshold = (int)(initialCapacity * loadFactor);
	}
	/**
	 * Clears this lookup table so that it contains no keys. 
	 */
	public synchronized void clear() {
		Object tab1[] = keys;
		Object tab2[] = values;
		modCount++;
		for (int index = keys.length; --index >= 0; ) {
		    tab1[index] = null;
		    tab2[index] = null;
	    }
		count = 0;
	}
	/**
	 * Creates a shallow copy of this lookup table. All the structure of the 
	 * lookup table itself is copied, but the keys and values are not cloned. 
	 * This is a relatively expensive operation.
	 *
	 * @return  a clone of the lookup table.
	 */
	public synchronized Object clone() {
		try { 
			LookupTable t = (LookupTable) super.clone();
			int size = keys.length;
			t.keys = new Object[size];
			t.values = new Object[size];
			System.arraycopy(keys, 0, t.keys, 0, size);
			System.arraycopy(values, 0, t.values, 0, size);
			t.modCount = 0;
			return t;
	    } catch (CloneNotSupportedException e) { 
			// this shouldn't happen, since we are Cloneable
			throw new InternalError();
		}
	}
	/**
	 * Tests if some key maps into the specified value in this lookup table.
	 * This operation is more expensive than the <code>containsKey</code>
	 * method.<p>
	 *
	 * Note that this method is identical in functionality to containsValue,
	 * (which is part of the Map interface in the collections framework).
	 * 
	 * @param      value   a value to search for.
	 * @return     <code>true</code> if and only if some key maps to the
	 *             <code>value</code> argument in this lookup table as 
	 *             determined by the <tt>equals</tt> method;
	 *             <code>false</code> otherwise.
	 * @exception  NullPointerException  if the value is <code>null</code>.
	 * @see        #containsKey(Object)
	 * @see        #containsValue(Object)
	 * @see	   Map
	 */
	public synchronized boolean contains(Object value) {
	if (value == null) {
	    throw new NullPointerException();
	}

	Object tab[] = values;
	for (int i = tab.length ; i-- > 0 ;) {
		if (tab[i].equals(value)) {
			return true;
		}
	}
	return false;
	}
	/**
	 * Tests if the specified object is a key in this lookup table.
	 * 
	 * @param   key   possible key.
	 * @return  <code>true</code> if and only if the specified object 
	 *          is a key in this lookup table, as determined by the 
	 *          <tt>equals</tt> method; <code>false</code> otherwise.
	 * @see     #contains(Object)
	 */
	public synchronized boolean containsKey(Object key) {
		Object[] tab = keys;
		int size = tab.length;
		int index = (key.hashCode() & 0x7FFFFFFF) % size;
		for (int i = index; i < size; ++i) {
			Object testKey = tab[i];
			if (testKey == null) return false;
			if (testKey.equals(key)) return true;
		}
		for (int i = 0; i < index; ++i) {
			Object testKey = tab[i];
			if (testKey == null) return false;
			if (testKey.equals(key)) return true;
		}
		return false;
	}
	/**
	 * Returns an enumeration of the values in this lookup table.
	 * Use the Enumeration methods on the returned object to fetch the elements
	 * sequentially.
	 *
	 * @return  an enumeration of the values in this lookup table.
	 * @see     java.util.Enumeration
	 * @see     #keys()
	 * @see	#values()
	 * @see	Map
	 */
	public synchronized Enumeration elements() {
		return new Enumerator(VALUES);
	}
	/**
	 * Increases the capacity of and internally reorganizes this 
	 * lookup table, in order to accommodate and access its entries more 
	 * efficiently.  This method is called automatically when the 
	 * number of keys in the lookup table exceeds this lookup table's capacity 
	 * and load factor. 
	 */
	protected void expand() {
		rehash(keys.length * 2 + 1);
	}
	/**
	 * Returns the value to which the specified key is mapped in this lookup table.
	 *
	 * @param   key   a key in the lookup table.
	 * @return  the value to which the key is mapped in this lookup table;
	 *          <code>null</code> if the key is not mapped to any value in
	 *          this lookup table.
	 * @see     #put(Object, Object)
	 */
	public synchronized Object get(Object key) {
	Object[] tab = keys;
	int size = tab.length;
	int index = (key.hashCode() & 0x7FFFFFFF) % size;
	for (int i = index; i < size; ++i) {
		Object testKey = tab[i];
		if (testKey == null) return null;
		if (testKey.equals(key)) return values[i];
	}
	for (int i = 0; i < index; ++i) {
		Object testKey = tab[i];
		if (testKey == null) return null;
		if (testKey.equals(key)) return values[i];
	}
	return null;
	}
	/**
	 * Tests if this lookup table maps no keys to values.
	 *
	 * @return  <code>true</code> if this lookup table maps no keys to values;
	 *          <code>false</code> otherwise.
	 */
	public boolean isEmpty() {
	return count == 0;
	}
	/**
	 * Returns an enumeration of the keys in this lookup table.
	 *
	 * @return  an enumeration of the keys in this lookup table.
	 * @see     Enumeration
	 * @see     #elements()
	 * @see	#keySet()
	 * @see	Map
	 */
	public synchronized Enumeration keys() {
		return new Enumerator(KEYS);
	}
	/**
	 * Maps the specified <code>key</code> to the specified 
	 * <code>value</code> in this lookup table. Neither the key nor the 
	 * value can be <code>null</code>. <p>
	 *
	 * The value can be retrieved by calling the <code>get</code> method 
	 * with a key that is equal to the original key. 
	 *
	 * @param      key     the lookup table key.
	 * @param      value   the value.
	 * @return     the previous value of the specified key in this lookup table,
	 *             or <code>null</code> if it did not have one.
	 * @exception  NullPointerException  if the key or value is
	 *               <code>null</code>.
	 * @see     Object#equals(Object)
	 * @see     #get(Object)
	 */
	public synchronized Object put(Object key, Object value) {
		// Make sure the value is not null
		if (value == null) {
		    throw new NullPointerException();
		}
		Object[] tab = keys;
		int size = tab.length;
		int hashIndex = (key.hashCode() & 0x7FFFFFFF) % size;
		for (int i = hashIndex; i < size; ++i) {
			Object element = tab[i];
			if (element == null) {
				keys[i] = key;
				values[i] = value;
				if (++count > threshold) {
					expand();
				}
				++modCount;
				return null;
			}
			if (element.equals(key)) {
				Object oldValue = values[i];
				values[i] = value;
				return oldValue;
			}
		}
		for (int i = 0; i < hashIndex; ++i) {
			Object element = tab[i];
			if (element == null) {
				keys[i] = key;
				values[i] = value;
				if (++count > threshold) {
					expand();
				}
				++modCount;
				return null;
			}
			if (element.equals(key)) {
				Object oldValue = values[i];
				values[i] = value;
				return oldValue;
			}
		}
		expand();
		return put(key, value);
	}
	protected void rehash(int newSize) {
/*	
		System.out.println("LookupTable.rehash()");
		System.out.println("  oldSize= " + keys.length);
		System.out.println("  count= " + count);
		System.out.println("  threshold= " + threshold);
		System.out.println("  newSize= " + newSize);
*/		
		Object[] newKeys = new Object[newSize];
		Object[] newValues = new Object[newSize];
		if (count > 0) {
			Object[] oldKeys = keys;
			Object[] oldValues = values;
			for (int i = keys.length; i-- > 0; ) {
				Object key = oldKeys[i];
				if (key != null) {
					int hashIndex = (key.hashCode() & 0x7FFFFFFF) % newSize;
					while (newKeys[hashIndex] != null) {
						if (++hashIndex >= newSize)
							hashIndex = 0;
					}
					newKeys[hashIndex] = key;
					newValues[hashIndex] = oldValues[i];
				}
			}
		}
		keys = newKeys;
		values = newValues;
		threshold = (int) (newSize * loadFactor);
		
//		sanityCheck("rehash");
	}
	/**
	 * The element at target has been removed. Move the elements
	 * to keep the receiver properly hashed.
	 */
	protected void rehashTo(int index) {
		int target = index;
		Object[] tab = keys;
		int size = tab.length;
		for (;;) {
			if (++index >= size) index = 0;
			Object element = tab[index];
			if (element == null) break;
			int hashIndex = (element.hashCode() & 0x7FFFFFFF) % size;
			if (index < target
				? hashIndex <= target && hashIndex > index
				: hashIndex <= target || hashIndex > index) {
				keys[target] = element;
				values[target] = values[index];
				target = index;
			}
		}
		keys[target] = null;
		values[target] = null;
	}
	/**
	 * Removes the key (and its corresponding value) from this 
	 * lookup table. This method does nothing if the key is not in the lookup table.
	 *
	 * @param   key   the key that needs to be removed.
	 * @return  the value to which the key had been mapped in this lookup table,
	 *          or <code>null</code> if the key did not have a mapping.
	 */
	public synchronized Object remove(Object key) {
		Object[] tab = keys;
		int size = tab.length;
		int hashIndex = (key.hashCode() & 0x7FFFFFFF) % size;
		for (int i = hashIndex; i < size; ++i) {
			Object element = tab[i];
			if (element == null) return null;
			if (element.equals(key)) {
				Object oldValue = values[i];
				rehashTo(i);
				--count;
//				sanityCheck("remove");
				++modCount;
				return oldValue;
			}
		}
		for (int i = 0; i < hashIndex; ++i) {
			Object element = tab[i];
			if (element == null) return null;
			if (element.equals(key)) {
				Object oldValue = values[i];
				rehashTo(i);
				--count;
//				sanityCheck("remove"); 
				++modCount;
				return oldValue;
			}
		}
		return null;
	}
	protected void sanityCheck(String where) {
		int n = 0;
		for (int i = 0; i < keys.length; ++i) {
			if (keys[i] == null) {
				if (values[i] != null) {
					System.err.println("LookupTable sanity check in " + where + ": key is null, but value isn't at index " + i);
					throw new Error();
				}
			}
			else {
				if (values[i] == null) {
					System.err.println("LookupTable sanity check in " + where + ": value is null, but key isn't at index " + i);
					throw new Error();
				}
				else {
					++n;
					Object value = get(keys[i]);
					if (value == null || value != values[i]) {
						System.err.println("LookupTable sanity check in " + where + ": key doesn't hash to proper value: " + keys[i]);
						throw new Error();
					}
				}
			}
		}
		if (n != count) {
			System.err.println("LookupTable sanity check in " + where + ": count is " + count + " but there are " + n + " entries");
			throw new Error();
		}
	}
	/**
	 * Returns the number of keys in this LookupTable.
	 *
	 * @return  the number of keys in this lookup table.
	 */
	public int size() {
	return count;
	}
	/**
	 * Returns a string representation of this <tt>LookupTable</tt> object 
	 * in the form of a set of entries, enclosed in braces and separated 
	 * by the ASCII characters "<tt>,&nbsp;</tt>" (comma and space). Each 
	 * entry is rendered as the key, an equals sign <tt>=</tt>, and the 
	 * associated element, where the <tt>toString</tt> method is used to 
	 * convert the key and element to strings. <p>Overrides to 
	 * <tt>toString</tt> method of <tt>Object</tt>.
	 *
	 * @return  a string representation of this lookup table.
	 */
	public synchronized String toString() {
	StringBuffer buf = new StringBuffer();
	buf.append("{");
	boolean first = true;
	for (int i = 0, max = keys.length; i < max; i++) {
		if (keys[i] != null) {
			if (first) {
				first = false;
			}
			else {
				buf.append(", ");
			}
			if (buf.length() > 1000) {
				buf.append("...");
				break;
			}
			else {
			    buf.append(keys[i]).append("=").append(values[i]);
		    }
	    }
	}
	buf.append("}");
	return buf.toString();
	}
}
