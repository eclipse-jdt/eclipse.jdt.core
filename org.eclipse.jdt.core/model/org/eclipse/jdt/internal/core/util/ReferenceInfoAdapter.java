package org.eclipse.jdt.internal.core.util;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.core.*;

/**
 * An adapter which implements the methods for handling
 * reference information from the parser.
 */
public abstract class ReferenceInfoAdapter {
/**
 * Does nothing.
 */
public void acceptConstructorReference(char[] typeName, int argCount, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptFieldReference(char[] fieldName, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptMethodReference(char[] methodName, int argCount, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptTypeReference(char[][] typeName, int sourceStart, int sourceEnd) {}
/**
 * Does nothing.
 */
public void acceptTypeReference(char[] typeName, int sourcePosition) {}
/**
 * Does nothing.
 */
public void acceptUnknownReference(char[][] name, int sourceStart, int sourceEnd) {}
/**
 * Does nothing.
 */
public void acceptUnknownReference(char[] name, int sourcePosition) {}
}
