package org.eclipse.jdt.internal.compiler.env;
/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.jdt.internal.compiler.*;

public interface IGenericMethod {
/**
 * Answer an int whose bits are set according the access constants
 * defined by the VM spec.
 */
// We have added AccDeprecated & AccSynthetic.
int getModifiers();

/**
 * Answer the name of the method.
 *
 * For a constructor, answer <init> & <clinit> for a clinit method.
 */
char[] getSelector();

boolean isConstructor();

/**
 * Answer the names of the argument
 * or null if the argument names are not available.
 */

char[][] getArgumentNames();
}
