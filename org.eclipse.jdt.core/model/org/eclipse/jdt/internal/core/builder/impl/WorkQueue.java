package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.util.*;

public class WorkQueue {
	/**
	 * Table which maps from PackageElement to Entry.
	 */
	protected Hashtable entries = new Hashtable(11);

	/**
	 * Vector which keeps the elements marked NEEDS_COMPILE,
	 * in the order in which they were added to the queue.
	 */
	protected Vector needsCompileList = new Vector();
	
	protected static class Entry {
		protected PackageElement element;
		protected int status;

		public Entry(PackageElement e) {
			element = e;
			status = NEEDS_COMPILE;
		}
	}

	/**
	 * Status flag.  The element is unaffected by the latest changes.
	 */
	private static final int UNAFFECTED = 0;
	
	/**
	 * Status flag.  The element has been convicted and must be compiled before any other
	 * type can use it.
	 */
	private static final int NEEDS_COMPILE = 2;
	
	/**
	 * Status flag.  The element was affected by the changes, and was compiled as a result.
	 */
	private static final int COMPILED = 3;
	
/**
 * Creates a new WorkQueue.
 */
public WorkQueue() {
}
/**
 * Adds an element to the queue.  The element is marked as NEEDS_COMPILE.
 */
public void add(PackageElement element) {
	Entry entry = (Entry) entries.get(element);
	if (entry == null) {
		entry = new Entry(element);
		entries.put(element, entry);
		needsCompileList.addElement(element);
	} else {
		if (entry.status != NEEDS_COMPILE) {
			if (entry.status == COMPILED) {
				System.out.println("Warning: image builder wants to recompile already compiled element: " + element);
			}
			entry.status = NEEDS_COMPILE;
			needsCompileList.addElement(element);
		}
	}
}
/**
 * Marks the given element as COMPILED.
 */
public void compiled(PackageElement element) {
	Entry entry = (Entry) entries.get(element);
	if (entry == null){
		//System.out.println("Warning: Java builder compiled unexpected element: " + element);
		entry = new Entry(element);
		entries.put(element, entry);		
	} else {
		if (entry.status != NEEDS_COMPILE) {
			System.out.println("Warning: Java builder compiled the same element twice: " + element);
		}
		needsCompileList.removeElement(element);
	}
	entry.status = COMPILED;

}
/**
 * Returns true if the given element is in the queue, false otherwise.
 */
public boolean contains(PackageElement element) {
	return entries.containsKey(element);
}
/**
 * Returns the elements which are marked NEEDS_COMPILE,
 * in the order in which they should be compiled.
 */
public Vector getElementsToCompile() {
	// Important to clone because needsCompileList may
	// be modified while the result is being examined.
	// Also, the caller may modify the result. 
	return (Vector) needsCompileList.clone();
}
/**
 * Returns true if the given element is marked as COMPILED.
 */
public boolean hasBeenCompiled(PackageElement element) {
	Entry entry = (Entry) entries.get(element);
	return entry != null && entry.status == COMPILED;
}
/**
 * Returns true if the given element is marked as NEEDS_COMPILE.
 */
public boolean needsCompile(PackageElement element) {
	Entry entry = (Entry) entries.get(element);
	return entry != null && entry.status == NEEDS_COMPILE;
}
public String toString() {
	return "WorkQueue: " + needsCompileList;
}
}
