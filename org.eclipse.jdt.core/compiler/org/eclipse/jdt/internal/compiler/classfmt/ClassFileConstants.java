/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.classfmt;

import org.eclipse.jdt.internal.compiler.env.*;

public interface ClassFileConstants extends IConstants {
	
	int Utf8Tag = 1;
	int IntegerTag = 3;
	int FloatTag = 4;
	int LongTag = 5;
	int DoubleTag = 6;
	int ClassTag = 7;
	int StringTag = 8;
	int FieldRefTag = 9;
	int MethodRefTag = 10;
	int InterfaceMethodRefTag = 11;
	int NameAndTypeTag = 12;
	
	int ConstantMethodRefFixedSize = 5;
	int ConstantClassFixedSize = 3;
	int ConstantDoubleFixedSize = 9;
	int ConstantFieldRefFixedSize = 5;
	int ConstantFloatFixedSize = 5;
	int ConstantIntegerFixedSize = 5;
	int ConstantInterfaceMethodRefFixedSize = 5;
	int ConstantLongFixedSize = 9;
	int ConstantStringFixedSize = 3;
	int ConstantUtf8FixedSize = 3;
	int ConstantNameAndTypeFixedSize = 5;
	
	int MAJOR_VERSION_1_1 = 45;
	int MAJOR_VERSION_1_2 = 46;
	int MAJOR_VERSION_1_3 = 47;
	int MAJOR_VERSION_1_4 = 48;
	int MAJOR_VERSION_1_5 = 49; 
	
	int MINOR_VERSION_0 = 0;
	int MINOR_VERSION_1 = 1;
	int MINOR_VERSION_2 = 2;	
	int MINOR_VERSION_3 = 3;	
	
	// JDK 1.1 -> 1.5, comparable value allowing to check both major/minor version at once 1.4.1 > 1.4.0
	// 16 unsigned bits for major, then 16 bits for minor
	long JDK1_1 = ((long)ClassFileConstants.MAJOR_VERSION_1_1 << 16) + ClassFileConstants.MINOR_VERSION_3; // 1.1. is 45.3
	long JDK1_2 =  ((long)ClassFileConstants.MAJOR_VERSION_1_2 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_3 =  ((long)ClassFileConstants.MAJOR_VERSION_1_3 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_4 = ((long)ClassFileConstants.MAJOR_VERSION_1_4 << 16) + ClassFileConstants.MINOR_VERSION_0;
	long JDK1_5 = ((long)ClassFileConstants.MAJOR_VERSION_1_5 << 16) + ClassFileConstants.MINOR_VERSION_0;	
	
	// jdk level used to denote future releases: optional behavior is not enabled for now, but may become so. In order to enable these,
	// search for references to this constant, and change it to one of the official JDT constants above.
	long JDK_DEFERRED = Long.MAX_VALUE;
	
	int INT_ARRAY = 10;
	int BYTE_ARRAY = 8;
	int BOOLEAN_ARRAY = 4;
	int SHORT_ARRAY = 9;
	int CHAR_ARRAY = 5;
	int LONG_ARRAY = 11;
	int FLOAT_ARRAY = 6;
	int DOUBLE_ARRAY = 7;
}
