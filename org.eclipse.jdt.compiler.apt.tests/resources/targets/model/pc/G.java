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

import targets.model.pa.IA;

@AnnoX("on G")
public abstract class G extends F<String> implements IA, IF {
	public String _fieldString;
	
	int fieldInt; // hides definition in F
	
	@Override
	@AnnoY("on G.method_T1")
	String method_T1(String param1) 
	{
		return null;
	}
	
	String method2_String()
	{
		return null;
	}
	
	// hides F.staticMethod()
	public static void staticMethod()
	{
	}

	// Method declared in an interface but not implemented:
	//public String methodIAString(int int1)
	//{
	//	return null;
	//}
}