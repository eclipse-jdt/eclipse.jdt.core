/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.internal.tools.unicode;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;

public class CodePointsBuilder {

	public static Integer[] build(String[] codePointTable, Environment environment) {
		ArrayList<Integer> values = new ArrayList<>();
		for (String codePointTableEntry : codePointTable) {
			if (codePointTableEntry.length() != 0) {
				int indexOfDots = codePointTableEntry.indexOf(".."); //$NON-NLS-1$
				if (indexOfDots == -1) {
					// single value on the line
					try {
						values.add(Integer.parseInt(codePointTableEntry, 16));
					} catch (NumberFormatException e) {
						System.err.println("NumberFormatException processing : " + codePointTableEntry); //$NON-NLS-1$
						return null;
					}
				} else {
					// range of values
					try {
						int firstValue = Integer.parseInt(codePointTableEntry.substring(0, indexOfDots), 16);
						int secondValue = Integer.parseInt(codePointTableEntry.substring(indexOfDots + 2), 16);
						for (int i = firstValue; i <= secondValue; i++) {
							values.add(i);
						}
					} catch (NumberFormatException e) {
						System.err.println("NumberFormatException processing : " + codePointTableEntry); //$NON-NLS-1$
						return null;
					}
				}
			}
		}
		Collections.sort(values);
		printDistribution(values, 0x10000);
		return values.toArray(new Integer[values.size()]);
	}

	private static void printDistribution(ArrayList<Integer> array, int increment) {
		int bound = increment;
		int counter = 0;
		int totalCounter = 0;
		int length = array.size();
		int max = array.get(length - 1).intValue();
		int numberOfFiguresForRange = (int) (Math.log(max) / Math.log(10));
		if ((max % increment) == 0) {
			numberOfFiguresForRange = (int) (Math.log(max + 1) / Math.log(10));
		}
		int numberOfFiguresForCounter = (int) (Math.log(length) / Math.log(10));
		if ((length % increment) == 0) {
			numberOfFiguresForCounter = (int) (Math.log(length + 1) / Math.log(10));
		}
		for (int i = 0; i < length; i++) {
			if (array.get(i).intValue() < bound) {
				counter++;
			} else {
				i--;
				totalCounter += counter;
				printRange(counter, bound, increment, totalCounter, length, numberOfFiguresForRange,
						numberOfFiguresForCounter);
				counter = 0;
				bound += increment;
			}
		}
		totalCounter += counter;
		printRange(counter, bound, increment, totalCounter, length, numberOfFiguresForRange, numberOfFiguresForCounter);
	}

	private static void printRange(int counter, int bound, int increment, int totalCounter, int length,
			int numberOfFiguresForRange, int numberOfFiguresForCounters) {
		if (counter != 0) {
			StringBuilder buffer = new StringBuilder();
			int low = bound - increment;
			if (low != 0) {
				low++;
			}
			DecimalFormat format = new DecimalFormat("###.##"); //$NON-NLS-1$
			buffer.append(display(low, numberOfFiguresForRange, 16)).append(" - ") //$NON-NLS-1$
					.append(display(bound, numberOfFiguresForRange, 16)).append(" : ") //$NON-NLS-1$
					.append(display(counter, numberOfFiguresForCounters, 10)).append("\t") //$NON-NLS-1$
					.append((low & 0x1F0000) >> 16).append("\t\t") //$NON-NLS-1$
					.append(format.format(100.0 * ((double) totalCounter / length)));
			System.out.println(String.valueOf(buffer));
		}
	}

	private static String display(int value, int numberOfFiguresForRange, int radix) {
		int numberOfFigures = value == 0 ? 1 : (int) (Math.log(value) / Math.log(10));
		if ((value % 10) == 0) {
			numberOfFigures = (int) (Math.log(value + 1) / Math.log(10));
		}
		StringBuilder buffer = new StringBuilder();
		switch (radix) {
		case 10:
			while (numberOfFigures < numberOfFiguresForRange) {
				buffer.append(" "); //$NON-NLS-1$
				numberOfFigures++;
			}
			buffer.append(value);
			break;
		case 16:
			buffer.append("0x" + Integer.toHexString(value)); //$NON-NLS-1$
		}
		return String.valueOf(buffer);
	}
}
