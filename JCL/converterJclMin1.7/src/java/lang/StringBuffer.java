/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package java.lang;

public class StringBuffer {
	public StringBuffer() {
	}
	public StringBuffer(String s) {
	}
	public StringBuffer(int i) {
	}
	public synchronized StringBuffer insert(int offset, String str) {
		return this;
	}
	public synchronized StringBuffer append(String s) {
		return this;
	}
	public synchronized StringBuffer append(StringBuffer buffer) {
		return this;
	}
	public synchronized StringBuffer append(boolean b) {
		return this;
	}
	public synchronized StringBuffer append(int i) {
		return this;
	}
	public synchronized StringBuffer append(long l) {
		return this;
	}
	public synchronized StringBuffer append(float f) {
		return this;
	}
	public synchronized StringBuffer append(double d) {
		return this;
	}
	public synchronized StringBuffer append(char[] o) {
		return this;
	}
	public synchronized StringBuffer append(char[] o, int i, int j) {
		return this;
	}
	public synchronized StringBuffer append(Object o) {
		return this;
	}
	public int length() {
		return 0;
	}
	public char[] getChars(int i, int j, char[] tab, int k) {
		return null;
	}
}
