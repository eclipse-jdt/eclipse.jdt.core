/*******************************************************************************
 * Copyright (c) 2016 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * This is an implementation of an early-draft specification developed under the Java
 * Community Process (JCP) and is made available for testing and evaluation purposes
 * only. The code is not compatible with any specification of the JCP.
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.env;

import java.util.function.BiFunction;

/**
 * Functional interface for looking up types
 * 
 */
public interface ITypeLookup {

	ITypeLookup Dummy = (typeName, qualifiedPackageName, qualifiedBinaryFileName,binaryOnly) -> null;

	/**
	 * Find the class named typeName with binary file name qualifiedBinaryFileName in the package whose full name is qualifiedPackageName
	 * 
	 * @param typeName
	 * @param qualifiedPackageName
	 * @param qualifiedBinaryFileName
	 * 
	 * @return NameEnvironmentAnswer if found, otherwise null
	 */
	default public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName) {
		return findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, false);
	}

	/**
	 * Find the class named typeName with binary file name qualifiedBinaryFileName in the package whose full name is qualifiedPackageName
	 * 
	 * @param typeName
	 * @param qualifiedPackageName
	 * @param qualifiedBinaryFileName
	 * @param asBinaryOnly Look for only binary files
	 * 
	 * @return NameEnvironmentAnswer if found, otherwise null
	 */
	public NameEnvironmentAnswer findClass(char[] typeName, String qualifiedPackageName, String qualifiedBinaryFileName, boolean asBinaryOnly);

	BiFunction<NameEnvironmentAnswer, ITypeLookup, ITypeLookup> orBetter = (suggested, other) -> {
		return (typeName, qualifiedPackageName, qualifiedBinaryFileName,binaryOnly) -> {
			NameEnvironmentAnswer answer = other.findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, binaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggested))
						return answer;
				} else if (answer.isBetter(suggested))
					// remember suggestion and keep looking
					return answer;
			}
			return suggested;
		};
	};

	/**
	 * Returns a composed TypeLookup that represents a short circuiting TypeLookup which returns
	 * the better answer between this TypeLookup and other
	 *   
	 * @param other
	 * @return Chained TypeLookup
	 */
	default ITypeLookup chain(ITypeLookup other) {
		NameEnvironmentAnswer suggestedAnswer = null;
		return (typeName, qualifiedPackageName, qualifiedBinaryFileName, binaryOnly) -> {
			NameEnvironmentAnswer answer = findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, binaryOnly);
			if (answer != null) {
				if (!answer.ignoreIfBetter()) {
					if (answer.isBetter(suggestedAnswer))
						return answer;
				} else if (answer.isBetter(suggestedAnswer))
					// remember suggestion and keep looking
					return orBetter.apply(answer, other).findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName, binaryOnly);
			}
			return orBetter.apply(suggestedAnswer, other).findClass(typeName, qualifiedPackageName, qualifiedBinaryFileName);
		};
	}
}