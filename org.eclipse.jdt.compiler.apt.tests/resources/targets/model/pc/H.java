/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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