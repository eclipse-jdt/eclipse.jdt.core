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
package targets.model.pb;

import java.util.*;
import targets.model.pa.IA;

@SuppressWarnings("unchecked") // contains some raw types
public class AC<T1 extends String & Iterator, T2> implements IC, IA {
	
	protected class ACInner<T3> {
		
	}
	
	private List<String> _fieldListString = new ArrayList<String>();
	
	public Map<String, Number> _fieldMapStringNumber = null; 
	
	public List _fieldRawList = null;
	
	public String methodIAString(int int1) {
		return _fieldListString.iterator().next();
	}
	
	public T1 methodGetT1(T2 paramT2) {
		return null;
	}
	
	public List<T1> methodGetListT1() {
		return null;
	}
	
	public Map<T1, List<T2>> methodGetMapT1ListT2( Iterator<T2> paramIterT2 ) {
		return null;
	}
	
	public Map<String, Number> methodGetMapStringNumber() {
		return _fieldMapStringNumber;
	}
	
	public List<? extends T1> methodGetQExtendsT1() {
		return null;
	}
	
	public <T3 extends List<T2>> void methodT3Void(T3 paramT3) {
	}
}
