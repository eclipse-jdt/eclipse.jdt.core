/**********************************************************************
Copyright (c) 2002 IBM Corp. and others.
All rights reserved.   This program and the accompanying materials
are made available under the terms of the Common Public License v0.5
which accompanies this distribution, and is available at
http://www.eclipse.org/legal/cpl-v05.html
 
Contributors:
     IBM Corporation - initial API and implementation
**********************************************************************/

package org.eclipse.jdt.core.util;

/**
 * Specify flags value for decoding class file reader.
 * METHOD_INFOS, FIELD_INFOS, SUPER_INTERFACES, CLASSFILE_ATTRIBUTES include CONSTANT_POOL, because
 * CONSTANT_POOL is a prerequisite of all others. But the CONSTANT_POOL could be sufficient if you
 * simply want to decode the constant pool and nothing else.
 * 
 * @since 2.0
 */
public interface DecodingFlag {

	int ALL 					= 0xFFFF;
	int CONSTANT_POOL 			= 0x0001;
	int METHOD_INFOS 			= 0x0003;
	int FIELD_INFOS 			= 0x0005;
	int SUPER_INTERFACES 		= 0x0009;
	int CLASSFILE_ATTRIBUTES 	= 0x0011;
}
