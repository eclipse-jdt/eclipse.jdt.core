/*******************************************************************************
 * Copyright (c) 2005, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler;

import java.util.Arrays;

import org.eclipse.jdt.internal.compiler.lookup.SourceTypeBinding;

public class ClassFilePool {
	public static final int POOL_SIZE = 7;
	ClassFile[] classFiles; 
	
private ClassFilePool() {
	// prevent instantiation
	this.classFiles = new ClassFile[POOL_SIZE];		
}

public static ClassFilePool newInstance() {
	return new ClassFilePool();
}

public void release(ClassFile classFile) {
	for (int i = 0; i < POOL_SIZE; i++) {
		ClassFile currentClassFile = this.classFiles[i];
		if (currentClassFile == classFile) {
			classFile.isShared = false;
			return;
		}
	}
}
	
public ClassFile acquire(SourceTypeBinding typeBinding) {
	for (int i = 0; i < POOL_SIZE; i++) {
		ClassFile classFile = this.classFiles[i];
		if (classFile == null) {
			ClassFile newClassFile = new ClassFile(typeBinding);
			this.classFiles[i] = newClassFile;
			newClassFile.isShared = true;
			return newClassFile;
		}
		if (classFile.isShared) {
			continue;
		}
		classFile.reset(typeBinding);
		classFile.isShared = true;
		return classFile;
	}
	return new ClassFile(typeBinding);
}
public void reset() {
	Arrays.fill(this.classFiles, null); 	
}
//	TypeBinding mostEnclosingType(TypeBinding binding) {
//		TypeBinding currentBinding = binding;
//		while (currentBinding.enclosingType() != null) {
//			currentBinding = currentBinding.enclosingType();
//		}
//		return currentBinding;
//	}
}

