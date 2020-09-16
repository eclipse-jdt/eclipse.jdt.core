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

public class Double extends Number implements Comparable<Double> {
	private static final long serialVersionUID = -5839202747810229696L;
	public Double(double d) {
	}
	public static String toString(double d) {
		return null;
	}
	public static Double valueOf(String s) throws NumberFormatException {
		return null;
	}
	public static final double POSITIVE_INFINITY = 1.0 / 0.0;
	public static boolean isNaN(double v) {
		return false;
	}
	public double doubleValue() {
		return 0.0;
	}
	public static final double MAX_VALUE = 1.7976931348623157e+308;
	public static final double MIN_VALUE = 4.9e-324;
    public static final double NaN = 0.0d / 0.0;

	public static native double longBitsToDouble(long bits);
	public static native long doubleToLongBits(double value);
	/* (non-Javadoc)
	 * @see java.lang.Number#floatValue()
	 */
	public float floatValue() {
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
	
	public static double parseDouble(String s) {
		return 0.0;
	}
	public int compareTo(Double d) {
		return 0;
	}
}
