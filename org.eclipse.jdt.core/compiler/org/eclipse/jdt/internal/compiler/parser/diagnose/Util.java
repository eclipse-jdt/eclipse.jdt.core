/*******************************************************************************
 * Copyright (c) 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.parser.diagnose;

import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.lookup.CompilerModifiers;

public class Util {
	private static final int INITIAL_SIZE = 10;
	
	// flags
	public static final int NO_FLAG = 0;
	public static final int LBRACE_MISSING = 1;
	public static final int IGNORE = 2;
	
	private static int pos;
	private static int[] intervalStarts;
	private static int[] intervalEnds;
	private static int[] intervalFlags;
	
	private static void addInterval(int start, int end){
		addInterval(start, end, NO_FLAG);
	}
	
	private static void addInterval(int start, int end, int flags){
		if(pos >= intervalStarts.length) {
			System.arraycopy(intervalStarts, 0, intervalStarts = new int[pos * 2], 0, pos);
			System.arraycopy(intervalEnds, 0, intervalEnds = new int[pos * 2], 0, pos);
			System.arraycopy(intervalFlags, 0, intervalFlags = new int[pos * 2], 0, pos);
		}
		intervalStarts[pos] = start;
		intervalEnds[pos] = end;
		intervalFlags[pos] = flags;
		pos++;
	}
	
	private static int compare(int i1, int i2) {
		return i1 - i2;
	}
	
	public static boolean containsErrorInSignature(AbstractMethodDeclaration method){
		return method.sourceEnd + 1 == method.bodyStart	|| method.bodyEnd == method.declarationSourceEnd;
	}
	
	private static void quickSort(int[] list, int[] list2, int[] list3, int left, int right) {
		int original_left= left;
		int original_right= right;
		int mid= list[(left + right) / 2];
		do {
			while (compare(list[left], mid) < 0) {
				left++;
			}
			while (compare(mid, list[right]) < 0) {
				right--;
			}
			if (left <= right) {
				int tmp= list[left];
				list[left]= list[right];
				list[right]= tmp;
				
				tmp = list2[left];
				list2[left]= list2[right];
				list2[right]= tmp;
				
				tmp = list3[left];
				list3[left]= list3[right];
				list3[right]= tmp;
				
				left++;
				right--;
			}
		} while (left <= right);
		
		if (original_left < right) {
			quickSort(list, list2, list3, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, list2, list3, left, original_right);
		}
	}
	public static int[][] computeDietRange(TypeDeclaration[] types) {
		if(types == null || types.length == 0) {
			return new int[3][0];
		} else {
			pos = 0;
			intervalStarts = new int[INITIAL_SIZE];
			intervalEnds = new int[INITIAL_SIZE];
			intervalFlags = new int[INITIAL_SIZE];
			computeDietRange0(types);
			
			System.arraycopy(intervalStarts, 0, intervalStarts = new int[pos], 0, pos);
			System.arraycopy(intervalEnds, 0, intervalEnds = new int[pos], 0, pos);
			System.arraycopy(intervalFlags, 0, intervalFlags = new int[pos], 0, pos);

			if (intervalStarts.length > 1) {
				quickSort(intervalStarts, intervalEnds, intervalFlags, 0, intervalStarts.length - 1);
			}
			int[][] res = new int[][]{intervalStarts, intervalEnds, intervalFlags};
			intervalStarts = null;
			intervalEnds = null;
			intervalFlags = null;
			return res;
		}
	}
	
	private static void computeDietRange0(TypeDeclaration[] types) {
		for (int j = 0; j < types.length; j++) {
			//members
			MemberTypeDeclaration[] memberTypeDeclarations = types[j].memberTypes;
			if(memberTypeDeclarations != null && memberTypeDeclarations.length > 0) {
				computeDietRange0(types[j].memberTypes);
			}
			//methods
			AbstractMethodDeclaration[] methods = types[j].methods;
			if (methods != null) {
				int length = methods.length;
				for (int i = 0; i < length; i++) {
					AbstractMethodDeclaration method = methods[i];
					if(containsIgnoredBody(method)) {
						if(containsErrorInSignature(method)) {
							method.errorInSignature = true;
							addInterval(method.declarationSourceStart, method.declarationSourceEnd, IGNORE);
						} else {
							int flags = method.sourceEnd + 1 == method.bodyStart ? LBRACE_MISSING : NO_FLAG;
							addInterval(method.bodyStart, method.bodyEnd, flags);
						}
					}
				}
			}
	
			//initializers
			FieldDeclaration[] fields = types[j].fields;
			if (fields != null) {
				int length = fields.length;
				for (int i = 0; i < length; i++) {
					if (fields[i] instanceof Initializer) {
						Initializer initializer = (Initializer)fields[i];
						if(initializer.declarationSourceEnd == initializer.bodyEnd){
							initializer.errorInSignature = true;
							addInterval(initializer.declarationSourceStart, initializer.declarationSourceEnd, IGNORE);
						} else {
							addInterval(initializer.bodyStart, initializer.bodyEnd);
						}
					}
				}
			}
		}
	}
	
	public static boolean isInInterval(int start, int end, int[] intervalStart, int[] intervalEnd) {
		int length = intervalStart.length;
		for (int i = 0; i < length; i++) {
			if(intervalStart[i] <= start && intervalEnd[i] >= end) {
				return true;
			} else if(intervalStart[i] > end) {
				return false;
			}
		}
		return false;
	}
	
	public static int getPreviousInterval(int start, int end, int[] intervalStart, int[] intervalEnd) {
		int length = intervalStart.length;
		for (int i = 0; i < length; i++) {
			if(intervalStart[i] > end) {
				return i - 1;
			}
		}
		return length - 1;
	}
	
	public static boolean containsIgnoredBody(AbstractMethodDeclaration method){
		return !method.isDefaultConstructor()
			&& !method.isClinit()
			&& (method.modifiers & CompilerModifiers.AccSemicolonBody) == 0;
	}
}
