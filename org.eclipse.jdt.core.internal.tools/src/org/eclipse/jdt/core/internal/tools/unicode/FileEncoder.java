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

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

public class FileEncoder {
	private static final String RESOURCE_FILE_EXTENSION = ".rsc"; //$NON-NLS-1$
	private final static int Bit1 = 0x1;
	private final static int Bit2 = 0x2;
	private final static int Bit3 = 0x4;
	private final static int Bit4 = 0x8;
	private final static int Bit5 = 0x10;
	private final static int Bit6 = 0x20;
	private final static int Bit7 = 0x40;
	private final static int Bit8 = 0x80;
	private final static int Bit9 = 0x100;
	private final static int Bit10 = 0x200;
	private final static int Bit11 = 0x400;
	private final static int Bit12 = 0x800;
	private final static int Bit13 = 0x1000;
	private final static int Bit14 = 0x2000;
	private final static int Bit15 = 0x4000;
	private final static int Bit16 = 0x8000;
	private final static int Bit17 = 0x10000;
	private final static int Bit18 = 0x20000;
	private final static int Bit19 = 0x40000;
	private final static int Bit20 = 0x80000;
	private final static int Bit21 = 0x100000;
	private final static int Bit22 = 0x200000;
	private final static int Bit23 = 0x400000;
	private final static int Bit24 = 0x800000;
	private final static int Bit25 = 0x1000000;
	private final static int Bit26 = 0x2000000;
	private final static int Bit27 = 0x4000000;
	private final static int Bit28 = 0x8000000;
	private final static int Bit29 = 0x10000000;
	private final static int Bit30 = 0x20000000;
	private final static int Bit31 = 0x40000000;
	private final static long Bit32 = 0x80000000L;
	private final static long Bit33 = 0x100000000L;
	private final static long Bit34 = 0x200000000L;
	private final static long Bit35 = 0x400000000L;
	private final static long Bit36 = 0x800000000L;
	private final static long Bit37 = 0x1000000000L;
	private final static long Bit38 = 0x2000000000L;
	private final static long Bit39 = 0x4000000000L;
	private final static long Bit40 = 0x8000000000L;
	private final static long Bit41 = 0x10000000000L;
	private final static long Bit42 = 0x20000000000L;
	private final static long Bit43 = 0x40000000000L;
	private final static long Bit44 = 0x80000000000L;
	private final static long Bit45 = 0x100000000000L;
	private final static long Bit46 = 0x200000000000L;
	private final static long Bit47 = 0x400000000000L;
	private final static long Bit48 = 0x800000000000L;
	private final static long Bit49 = 0x1000000000000L;
	private final static long Bit50 = 0x2000000000000L;
	private final static long Bit51 = 0x4000000000000L;
	private final static long Bit52 = 0x8000000000000L;
	private final static long Bit53 = 0x10000000000000L;
	private final static long Bit54 = 0x20000000000000L;
	private final static long Bit55 = 0x40000000000000L;
	private final static long Bit56 = 0x80000000000000L;
	private final static long Bit57 = 0x100000000000000L;
	private final static long Bit58 = 0x200000000000000L;
	private final static long Bit59 = 0x400000000000000L;
	private final static long Bit60 = 0x800000000000000L;
	private final static long Bit61 = 0x1000000000000000L;
	private final static long Bit62 = 0x2000000000000000L;
	private final static long Bit63 = 0x4000000000000000L;
	private final static long Bit64 = 0x8000000000000000L;
	private final static long[] Bits = { Bit1, Bit2, Bit3, Bit4, Bit5, Bit6, Bit7, Bit8, Bit9, Bit10, Bit11, Bit12,
			Bit13, Bit14, Bit15, Bit16, Bit17, Bit18, Bit19, Bit20, Bit21, Bit22, Bit23, Bit24, Bit25, Bit26, Bit27,
			Bit28, Bit29, Bit30, Bit31, Bit32, Bit33, Bit34, Bit35, Bit36, Bit37, Bit38, Bit39, Bit40, Bit41, Bit42,
			Bit43, Bit44, Bit45, Bit46, Bit47, Bit48, Bit49, Bit50, Bit51, Bit52, Bit53, Bit54, Bit55, Bit56, Bit57,
			Bit58, Bit59, Bit60, Bit61, Bit62, Bit63, Bit64, };

	private static final int BOUND = 64;

	private static final int BLOCK_SIZE = 65536;

	private static final int BLOCK_LENGTH = 1024;

	private static int getBitIndex(int i) {
		return i % 64;
	}

	public static void encodeResourceFiles(Integer[] codePoints, Environment environment, String exportDirectory) {
		File parentFile = new File(exportDirectory);
		if (!parentFile.exists()) {
			if (!parentFile.mkdirs()) {
				System.err.println("Could not create the export directory: " + exportDirectory); //$NON-NLS-1$
				return;
			}
		}
		long encoding = 0;
		int length = codePoints.length;
		int counter = 0;
		long[] computedValues = new long[BLOCK_LENGTH];
		int limit = BOUND;
		int blockLimit = BLOCK_SIZE;
		int blockNumber = 0;
		boolean hasMeaningfulValue = false;
		for (int i = 0; i < length; i++) {
			if (codePoints[i] < blockLimit) {
				int valueToEncode = codePoints[i] & 0xFFFF;
				if (valueToEncode < limit) {
					encoding |= Bits[getBitIndex(valueToEncode)];
				} else {
					i--;
					computedValues[counter++] = encoding;
					hasMeaningfulValue |= encoding != 0 ? true : false;
					encoding = 0;
					limit += BOUND;
				}
			} else {
				computedValues[counter++] = encoding;
				try {
					File f = new File(exportDirectory,
							environment.getResourceFileName() + blockNumber + RESOURCE_FILE_EXTENSION);
					if (hasMeaningfulValue) {
						try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(f))) {
							for (int j = 0, max = computedValues.length; j < max; j++) {
								long value = computedValues[j];
								outputStream.writeLong(value);
							}
							outputStream.flush();
						}
					}
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				counter = 0;
				hasMeaningfulValue = false;
				Arrays.fill(computedValues, 0);
				i--;
				limit = BOUND;
				blockLimit += BLOCK_SIZE;
				blockNumber++;
			}
		}
		computedValues[counter++] = encoding;
		try {
			File f = new File(exportDirectory,
					environment.getResourceFileName() + blockNumber + RESOURCE_FILE_EXTENSION);
			try (DataOutputStream outputStream = new DataOutputStream(new FileOutputStream(f))) {
				for (int j = 0, max = computedValues.length; j < max; j++) {
					long value = computedValues[j];
					outputStream.writeLong(value);
				}
				outputStream.flush();
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
