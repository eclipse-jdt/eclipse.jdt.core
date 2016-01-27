/*******************************************************************************
 * Copyright (c) 2007, 2015 Symbian Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Andrew Ferguson (Symbian) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.pdom.db;

import org.eclipse.jdt.internal.core.pdom.Nd;

import java.util.HashSet;
import java.util.Set;

/**
 * DBProperties is a bare-bones implementation of a String->String mapping. It is neither
 * a Map or a Properties subclass, because of their more general applications.
 * @since 3.12
 */
public class DBProperties {
	static final int PROP_INDEX = 0;
	static final int RECORD_SIZE = 4;
	
	protected BTree index;
	protected Database db;
	protected long record;
	
	/**
	 * Allocate storage for a new DBProperties record in the specified database
	 * @param db
	 * @throws IndexException
	 */
	public DBProperties(Nd pdom) throws IndexException {
		Database db = pdom.getDB();
		this.record= db.malloc(RECORD_SIZE);
		this.index= new BTree(pdom, record + PROP_INDEX, DBProperty.getComparator());
		this.db= db;
	}
	
	/**
	 * Creates an object for accessing an existing DBProperties record at the specified location
	 * of the specified database.
	 * @param db
	 * @param record
	 * @throws IndexException
	 */
	public DBProperties(Nd pdom, long record) throws IndexException {
		Database db = pdom.getDB();
		this.record= record;
		this.index= new BTree(pdom, record + PROP_INDEX, DBProperty.getComparator());
		this.db= db;
	}
	
	/**
	 * Reads the named property from this properties storage.
	 * @param key a case-sensitive identifier for a property, or null
	 * @return the value associated with the key, or null if either no such property is set,
	 *     or the specified key was null
	 * @throws IndexException
	 */
	public String getProperty(String key) throws IndexException {
		if (key != null) {
			DBProperty existing= DBProperty.search(db, index, key);
			if (existing != null) {
				return existing.getValue().getString();
			}
		}
		return null;
	}
	
	/**
	 * Reads the named property from this properties storage, returning the default value if there
	 * is no such property.
	 * @param key a case-sensitive identifier for a property, or null
	 * @param defaultValue a value to return in case the specified key was null
	 * @return the value associated with the key, or the specified default value if either no such
	 *     property is set, or the specified key was null
	 * @throws IndexException
	 */
	public String getProperty(String key, String defaultValue) throws IndexException {
		String val= getProperty(key);
		return (val == null) ? defaultValue : val;
	}

	/**
	 * Returns the Set of property names stored in this object
	 * @return the Set of property names stored in this object
	 * @throws IndexException
	 */
	public Set<String> getKeySet() throws IndexException {
		return DBProperty.getKeySet(db, index);
	}

	/**
	 * Writes the key, value mapping to the properties. If a mapping for the
	 * same key already exists, it is overwritten.
	 * @param key a non-null property name
	 * @param value a value to associate with the key. may not be null.
	 * @throws IndexException
	 * @throws NullPointerException if key is null
	 */
	public void setProperty(String key, String value) throws IndexException {
		removeProperty(key);
		DBProperty newProperty= new DBProperty(db, key, value);
		index.insert(newProperty.getRecord());
	}

	/**
	 * Deletes a property from this DBProperties object.
	 * @param key
	 * @return whether a property with matching key existed and was removed, or false if the key
	 *     was null
	 * @throws IndexException
	 */
	public boolean removeProperty(String key) throws IndexException {
		if (key != null) {
			DBProperty existing= DBProperty.search(db, index, key);
			if (existing != null) {
				index.delete(existing.getRecord());
				existing.delete();
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Deletes all properties, does not delete the record associated with the object itself
	 * - that is it can be re-populated.
	 * @throws IndexException
	 */
	public void clear() throws IndexException {
		index.accept(new IBTreeVisitor(){
			@Override
			public int compare(long record) throws IndexException {
				return 0;
			}
			@Override
			public boolean visit(long record) throws IndexException {
				new DBProperty(db, record).delete();
				return false; // there should never be duplicates
			}
		});
	}
	
	/**
	 * Deletes all properties stored in this object and the record associated with this object
	 * itself.
	 * <br><br>
	 * <b>The behaviour of objects of this class after calling this method is undefined</b>
	 * @throws IndexException
	 */
	public void delete() throws IndexException {
		clear();
		db.free(record);
	}

	public long getRecord() {
		return record;
	}
	
	private static class DBProperty {
		static final int KEY = 0;
		static final int VALUE = 4;
		@SuppressWarnings("hiding")
		static final int RECORD_SIZE = 8;
		
		Database db;
		long record;
		
		public long getRecord() {
			return record;
		}
		
		/**
		 * Allocates and initializes a record in the specified database for a DBProperty record
		 * @param db
		 * @param key a non-null property name
		 * @param value a non-null property value
		 * @throws IndexException
		 */
		DBProperty(Database db, String key, String value) throws IndexException {
			assert key != null;
			assert value != null;
			IString dbkey= db.newString(key);
			IString dbvalue= db.newString(value);
			this.record= db.malloc(RECORD_SIZE);
			db.putRecPtr(record + KEY, dbkey.getRecord());
			db.putRecPtr(record + VALUE, dbvalue.getRecord());
			this.db= db;
		}
		
		/**
		 * Returns an object for accessing an existing DBProperty record at the specified location
		 * in the specified database.
		 * @param db
		 * @param record
		 */
		DBProperty(Database db, long record) {
			this.record= record;
			this.db= db;
		}
		
		public IString getKey() throws IndexException {
			return db.getString(db.getRecPtr(record + KEY));
		}
		
		public IString getValue() throws IndexException {
			return db.getString(db.getRecPtr(record + VALUE));
		}
		
		public static IBTreeComparator getComparator() {
			return new IBTreeComparator() {
				@Override
				public int compare(Nd pdom, long record1, long record2) throws IndexException {
					Database db = pdom.getDB();
					IString left= db.getString(db.getRecPtr(record1 + KEY));
					IString right= db.getString(db.getRecPtr(record2 + KEY));
					return left.compare(right, true);
				}
			};
		}
		
		public static DBProperty search(final Database db, final BTree index, final String key) throws IndexException {
			final DBProperty[] result= new DBProperty[1];
			index.accept(new IBTreeVisitor(){
				@Override
				public int compare(long record) throws IndexException {
					return db.getString(db.getRecPtr(record + KEY)).compare(key, true);
				}

				@Override
				public boolean visit(long record) throws IndexException {
					result[0] = new DBProperty(db, record);
					return false; // There should never be duplicates.
				}
			});
			return result[0];
		}
		
		public static Set<String> getKeySet(final Database db, final BTree index) throws IndexException {
			final Set<String> result= new HashSet<String>();
			index.accept(new IBTreeVisitor(){
				@Override
				public int compare(long record) throws IndexException {
					return 0;
				}

				@Override
				public boolean visit(long record) throws IndexException {
					result.add(new DBProperty(db, record).getKey().getString());
					return true; // There should never be duplicates.
				}
			});
			return result;
		}
		
		public void delete() throws IndexException {
			db.getString(db.getRecPtr(record + KEY)).delete();
			db.getString(db.getRecPtr(record + VALUE)).delete();
			db.free(record);
		}
	}
}
