package java.lang.invoke;
/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.invoke.MethodType;

public abstract class MethodHandle {
	@Target(METHOD)
	@Retention(RUNTIME)
	@interface PolymorphicSignature {
	}

	@PolymorphicSignature
	public final native Object invoke(Object... args) throws Throwable;

	@PolymorphicSignature
	public final native Object invokeExact(Object... args) throws Throwable;

	public native Object invokeWithArguments(Object... arguments)
			throws Throwable;

	public native boolean isVarargsCollector();

	public native MethodHandle asType(MethodType newType);
}