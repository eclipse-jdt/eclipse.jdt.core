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

public class Util {
	private static int pos;
	private static int[] intervalStart;
	private static int[] intervalEnd;
	
	private static void addInterval(int start, int end){
		if(pos >= intervalStart.length) {
			System.arraycopy(intervalStart, 0, intervalStart = new int[pos * 2], 0, pos);
			System.arraycopy(intervalEnd, 0, intervalEnd = new int[pos * 2], 0, pos);
		}
		intervalStart[pos] = start;
		intervalEnd[pos] = end;
		pos++;
	}
	
	private static int compare(int i1, int i2) {
		return i1 - i2;
	}
	
	private static void quickSort(int[] list, int[] list2, int left, int right) {
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
				
				left++;
				right--;
			}
		} while (left <= right);
		
		if (original_left < right) {
			quickSort(list, list2, original_left, right);
		}
		if (left < original_right) {
			quickSort(list, list2, left, original_right);
		}
	}
	public static int[][] computeDietRange(TypeDeclaration[] types) {
		if(types == null || types.length == 0) {
			return new int[2][0];
		} else {
			pos = 0;
			intervalStart = new int[10];
			intervalEnd = new int[10];
			computeDietRange0(types);
			
			System.arraycopy(intervalStart, 0, intervalStart = new int[pos], 0, pos);
			System.arraycopy(intervalEnd, 0, intervalEnd = new int[pos], 0, pos);

			if (intervalStart.length > 1) {
				quickSort(intervalStart, intervalEnd, 0, intervalStart.length - 1);
			}
			int[][] res = new int[][]{intervalStart, intervalEnd};
			intervalStart = null;
			intervalEnd = null;
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
					if(!method.isDefaultConstructor() && !method.isClinit()) {
						addInterval(method.bodyStart, method.bodyEnd);
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
						addInterval(initializer.sourceStart, initializer.sourceEnd);
					}
				}
			}
		}
	}
	
	public static boolean isInInterval(int start, int end, int[] intervalStart, int[] intervalEnd) {
		int length = intervalStart.length;
		for (int i = 0; i < length; i++) {
			if(intervalStart[i] < start && intervalEnd[i] > end) {
				return true;
			} else if(intervalStart[i] > end) {
				return false;
			}
		}
		return false;
	}
}
