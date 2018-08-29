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
package targets.model.pc;

@AnnoY("on H")
public class H extends G {
	int fieldInt; // hides definition in G
	
	public String methodIAString(int int1)
	{
		return null;
	}

	// hides G.staticMethod and F.staticMethod
	public static void staticMethod()
	{
	}
	
	// different signature; does not hide G.staticMethod
	public static void staticMethod(int int1)
	{
	}

	public class FChild {} // hides definition in F
	public class IFChild {} // hides definition in IF
}