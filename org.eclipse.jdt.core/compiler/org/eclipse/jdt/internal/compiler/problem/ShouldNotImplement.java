package org.eclipse.jdt.internal.compiler.problem;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

/*
 * Special unchecked exception type used 
 * to denote implementation that should never be reached.
 *
 *	(internal only)
 */
public class ShouldNotImplement extends RuntimeException {
public ShouldNotImplement(){
}
public ShouldNotImplement(String message){
	super(message);
}
}
