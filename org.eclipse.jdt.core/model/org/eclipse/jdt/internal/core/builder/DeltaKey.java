package org.eclipse.jdt.internal.core.builder;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;

/**
 * <code>DeltaKey</code> objects are used to index into hierarchical structures.
 * They consist of a sequence of local names, which can be 
 * arbitrary objects.  A node in the hierarchy is obtained by 
 * traversing down from the root of the hierarchy, using the
 * localNames to indicate the choice of child at each step along the way.
 *
 * <p><code>DeltaKey</code> objects are immutable, as they must consistently describe
 * a particular branch of a source tree.  If branches are addded or deleted
 * from a <code>DeltaKey</code> object, it is done by creating a copy and modifying
 * the copy.
 */
public class DeltaKey implements IDeltaKey, Serializable {
	
	/**
	 * Names of the branches of the key
	 */
	protected Object[] fLocalNames;
	
	/**
	 * Singleton root node instance.
	 */
	protected static DeltaKey NodeRoot = new DeltaKey(0);

	/** Version UID for serialization purposes (as of LF 143)
   */
  protected static final long serialVersionUID = 1379863541613390307L;


	/**
	 * Sets the names of the key
	 */
	public DeltaKey(Object[] names) {
		fLocalNames = names;
	}
	/**
	 * Creates a new key of the given length.
	 */
	protected DeltaKey (int length) {
		fLocalNames = new Object[length];
	}
	/**
	 * Returns a new key describing the key's child with the given local name.
	 * This replaces the "/" syntax in the Smalltalk implementation
	 */
	public DeltaKey add(Object localName) {
		

		/* Calculate length of new key */
		int length = this.size();

		DeltaKey newKey = new DeltaKey(length + 1);
		
		/* Copy names from receiver to new key */
		newKey.replaceFromToWithStartingAt(0, length - 1, this, 0);
		
		/* Add new local name at end of new key */
		newKey.fLocalNames[length] = localName;
		
		return newKey;
	}
	/**
	 * Returns the local name at the given index in the receiver
	 */
	public Object at(int index) {
		return fLocalNames[index];
	}
	/** 
	 * Returns true if the receiver has the exact same local names as anObject.
	 * Returns false otherwise.
	 * Returns false if anObject is not an instance of NodeKey
	 */
	public boolean equals (Object anObject) {
		
		DeltaKey aKey;
		int result;		
			
		/* return true if the same object */
		if (this == anObject) {
			return true;
		}
		
		/* return false if different class of object */
		if (anObject instanceof DeltaKey) {
			aKey = (DeltaKey) anObject;
		} else {
			return false;
		}
		
		/* return false if not the same number of names */
		if (this.size() != aKey.size()) {
			return false;
		}
		
		/* not equal if any local names are not equal */
		for (int i = this.size() - 1; i >= 0; i--) {
			if (!this.at(i).equals(aKey.at(i))) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Returns the receiver's local name (the name of the last child).  If the
	 * receiver is a root key, answer null
	 */
	public Object getLocalName() {
		if (this.size() == 0) {
			return null;
		} else {
			return fLocalNames[fLocalNames.length - 1];
		}
	}
	/**
	 * Returns the local names of the key
	 */
	protected Object[] getLocalNames() {
		return fLocalNames;
	}
	/**
	 * Returns the singleton root node instance.
	 */
	public static DeltaKey getRoot() {
		return NodeRoot;
	}
	/**
	 * Returns the hash value of the receiver.  Objects which are equal have
	 * the same hash value.
	 */
	public int hashCode() {

		int hash, selfSize;
		
		selfSize = this.size();
		if (selfSize == 0) {
			return 0;
		}		
		
		/* Hash the first element */
		hash = this.at(0).hashCode();
		if (selfSize == 1) {
			return hash;
		}
		
		/* Hash the last element */
		hash ^= this.at(selfSize - 1).hashCode();
		if (selfSize == 2) {
			return hash & 32767;
		}

		/* Hash the middle element for good luck */
		hash ^= this.at(selfSize / 2).hashCode();
		return hash & 32767;
	}
	/**
	 * Returns true if the receiver is a prefix of the given key, false otherwise.
	 * Keys which are equal are considered to be prefixes of each other (so
	 * true is answered in this case).
	 */
	public boolean isPrefixOf (DeltaKey key) {

		int size;

		/**
		 * Capitalize on the property that compound keys are usually paths, 
		 * where a common prefix is more likely than a common suffix. 
		 * Compare last elements before comparing the rest.
		 */

		size = this.size();
		if (size > key.size()) {
			return false;
		}
		if (size == 0) {
			return true;
		}

		/* Check the last element first. */
		size--;
		if (this.fLocalNames[size] != key.fLocalNames[size]) {
			return false;
		}
		if (--size <= 0) {
			return true;
		}
		
		/* Check the first element next */
		if (this.fLocalNames[0] != key.fLocalNames[0]) {
			return false;
		}
		if (size == 1) {
			return true;
		}
		
		/* Unroll the loop for increased performance */
		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		if (this.fLocalNames[size] != key.fLocalNames[size]) {return false;}
		if (--size == 1) {return true;}

		while (size > 1) {
			if (this.fLocalNames[size] != key.fLocalNames[size]) {
				return false;
			}
		}
		return true;
	}
	/**
	 * Returns true if the receiver is the root key, false otherwise
	 */
	public boolean isRoot() {
		return (this.size() == 0);
	}
	/**
	 * Returns the key describing the receiver's parent, or null if the receiver
	 * is the root key.
	 */
	 public DeltaKey parent() {

		int size = this.size() - 1;
		DeltaKey newKey;
		
		if (size < 0) {
			return null;
		} else {
			newKey = new DeltaKey (size);
			
			/* copy children from receiver */
			newKey.replaceFromToWithStartingAt(0, size -1, this, 0);
			return newKey;
		}
	}
	/**
	 * Returns a new key containing the first few local names in the
	 * receiver.
	 *
	 * @param count
	 *	number of local names to copy (length of prefix)
	 * @exception IllegalArgumentException
	 *	receiver contains fewer than count local names, or count is
	 *	negative."
	 */
	public DeltaKey prefix (int count) {

		DeltaKey newNode;

		if (count < 0 || this.size() < count) {
			throw new IllegalArgumentException (new Integer(count).toString());
		}
		
		newNode = new DeltaKey(count);
		
		/* copy children */
		newNode.replaceFromToWithStartingAt (0, count - 1, this, 0);
		return newNode;
	}
private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
	int l = in.readInt();
	fLocalNames = new String[l];
	for (int i = 0; i < l; ++i) {
		fLocalNames[i] = in.readObject();
	}
}
	/**
	 * Replaces the elements of the receiver between the given indices
	 * with the elements of the supplied key starting at a particular point.
	 *
	 * Fail if start is not an Integer. Fail if stop is not an
	 * Integer. Fail if repStart is not an Integer. Fail if start
	 * is < 1. Fail if start is > size of the receiver. Fail if
	 * stop is < 1. Fail if repStart < 1. Fail if repStart > size
	 * of replacementKey."
	 *
	 * @param start
	 *	index in the receiver to start copying at
	 * @param stop
	 * 	index in the receiver to stop copying at
	 * @param replacementKey
	 *	key to be copied
	 * @param repStart
	 *	index to start copying from in the replacement key
	 * @return the modified key
	 */
	private DeltaKey replaceFromToWithStartingAt (int start, int stop, 
		DeltaKey replacementKey, int repStart) {
		
	 	int repCount = repStart;
	 	
	 	for (int i = start; i <= stop; i++, repCount++) {
	 		this.fLocalNames[i] = replacementKey.fLocalNames[repCount];
	 	}
	 	return this;
	 }
	/**
	 * Sets the local names of the key
	 */
	protected void setLocalNames(String[] names) {
		fLocalNames = names;
	}
	/**
	 * Returns the branch length of the key
	 */
	public int size() {
		return fLocalNames.length;
	}
	/**
	 * Returns a unicode representation of the receiver.  This method is used
	 * for debugging purposes only (no NLS needed)
	 */
	public String toString () {
		StringBuffer buffer = new StringBuffer();
		
		buffer.append("DeltaKey("); //$NON-NLS-1$
			
		for (int i = 0; i < this.fLocalNames.length; i++) {
			buffer.append(this.fLocalNames[i]);
			if (i < this.fLocalNames.length - 1) {
				buffer.append("/"); //$NON-NLS-1$
			}
		}
		buffer.append(")"); //$NON-NLS-1$

		return buffer.toString();
	}
	/**
	 * Returns a new key containing everything but the first few local
	 * names in the receiver.
	 *
	 * @param count
	 *	number of local names in the key to leave out (length of prefix to cut)
	 * @exception IllegalArgumentException
	 *	receiver contains fewer than count local names, or count is
	 *	negative.
	 */
	public DeltaKey withoutPrefix (int count) {

		DeltaKey newNode;
		int newSize;

		if (count < 0 || this.size() < count) {
			throw new IllegalArgumentException (new Integer (count).toString());
		}
		
		newSize = this.size() - count;
		newNode = new DeltaKey (newSize);
		newNode.replaceFromToWithStartingAt (0, newSize - 1, this, count);
		return newNode;
	}
private void writeObject(ObjectOutputStream out) throws IOException {
	int l = fLocalNames.length;
	out.writeInt(l);
	for (int i = 0; i < l; ++i) {
		out.writeObject(fLocalNames[i]);
	}
}
}// end class definition
