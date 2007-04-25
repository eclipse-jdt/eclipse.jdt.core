/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.compiler.apt.tests.annotations;

/**
 * These annotation types are visible to processor code and can thus be used
 * with {@link javax.lang.model.element.Element#getAnnotation(Class)}, which
 * returns a reflection proxy of the actual annotation class rather than just
 * an AnnotationMirror.
 */
public @interface TypedAnnos
{
	public enum Enum { A, B, C }
	
	public @interface AnnoByte {
		byte value();
	}
	public @interface AnnoBoolean {
		boolean value();
	}
	public @interface AnnoChar {
		char value();
	}
	public @interface AnnoDouble {
		double value();
	}
	public @interface AnnoFloat {
		float value();
	}
	public @interface AnnoInt {
		int value();
	}
	public @interface AnnoLong {
		long value();
	}
	public @interface AnnoShort {
		short value();
	}
	public @interface AnnoString {
		String value();
	}
	public @interface AnnoEnumConst {
		Enum value();
	}
	public @interface AnnoArrayType {
		Class<?>[] value();
	}
	public @interface AnnoType {
		Class<?> value();
	}
	public @interface AnnoAnnoChar {
		AnnoChar value();
	}

}
