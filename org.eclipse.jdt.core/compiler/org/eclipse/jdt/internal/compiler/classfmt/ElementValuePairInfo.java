/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;

public class ElementValuePairInfo implements IBinaryElementValuePair 
{	
	static final ElementValuePairInfo[] NoMember = new ElementValuePairInfo[0];
	static final Object[] ZeroLengthArray = new Object[0];
	private final char[] _membername;
	private final Object _value;
	
	ElementValuePairInfo(char[] membername, Object value)
	{
		_membername = membername;
		_value = value;
	}
		
	public char[] getMemberName() { return _membername; }

	public Object getMemberValue() { return _value; }	
	
	public String toString()
	{
		StringBuffer buffer = new StringBuffer();
		buffer.append(_membername);
		buffer.append('=');
		if( _value instanceof Object[] ){
			final Object[] values = (Object[])_value;
			buffer.append('{');
			for( int i=0, len=values.length; i<len; i++ ){
				if( i > 0 )
					buffer.append(", "); //$NON-NLS-1$
				buffer.append(values[i]);
			}	
			buffer.append('}');
		}
		else
			buffer.append(_value);		
		return buffer.toString();
	}


	
}
