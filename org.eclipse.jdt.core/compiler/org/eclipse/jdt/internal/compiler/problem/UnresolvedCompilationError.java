package org.eclipse.jdt.internal.compiler.problem;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import java.io.*;
import java.util.*;

/*
 * Special unchecked exception type used 
 * to report a problem at runtime.
 * Used to create the problem method.
 *
 */
public class UnresolvedCompilationError extends Error {
	/**
	 * Insert method's description here.
	 * @param s java.lang.String
	 */
	public UnresolvedCompilationError(String s) {
		super(s);
	}

}
