/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package targets.model8;

public class LambdaTest {
	String foo() {
		return null;
	}
}     

interface DefaultInterface {
	public default String defaultMethod () {
		return null;
	}
	default int anotherDefault() {
		return 0;
	}
	public static String staticMethod () {
		return null;
	}
}     

interface FunctionalInterface {
	public abstract String abstractMethod ();
}
