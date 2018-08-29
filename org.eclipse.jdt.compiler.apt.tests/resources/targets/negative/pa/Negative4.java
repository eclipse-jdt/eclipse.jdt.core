/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package targets.negative.pa;

/**
 * This code contains missing types and generic types with incorrect parameters.
 * The desired behavior is specified in the javadoc for package 
 * javax.lang.model.element: in general, missing types should be replaced
 * by empty types with the same name.
 */
interface IFoo {}
interface IBar<T1> {}

class Negative4 {
	// Zork is unknown
	Zork zorkRaw() { return null; }
	Zork<String> zorkOfString() { return null; }
	
	// IFoo does not take a type parameter
	IFoo<String> ifooOfString() { return null; }
	
	// IBar has one type parameter
	IBar ibarRaw() { return null; }
	IBar<String, Object> ibarOfT1T2() { return null; }
}
