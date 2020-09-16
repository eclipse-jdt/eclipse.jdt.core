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


public class Float extends Number implements Comparable<Float> {
	private static final long serialVersionUID = -872370025937303716L;
	public static final float NaN = 0.0f / 0.0f;
	public Float(float f) {
	}
    public static native float intBitsToFloat(int bits);
    public static Float valueOf(String s) throws NumberFormatException {
    	return null;
    }
    public static final float POSITIVE_INFINITY = 1.0f / 0.0f;
    public static boolean isNaN(float v) {
    	return false;
    }
    public float floatValue() {
    	return 0.0f;
    }
    public static final float MAX_VALUE = 3.4028235e+38f;
    public static final float MIN_VALUE = 1.4e-45f;
    public static native int floatToIntBits(float value);
    public static String toString(float f) {
    	return null;
    }
	/* (non-Javadoc)
	 * @see java.lang.Number#doubleValue()
	 */
	public double doubleValue() {
		return 0;
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#intValue()
	 */
	public int intValue() {
		return 0;
	}
	/* (non-Javadoc)
	 * @see java.lang.Number#longValue()
	 */
	public long longValue() {
		return 0;
	}
	public int compareTo(Float f) {
		return 0;
	}
}
