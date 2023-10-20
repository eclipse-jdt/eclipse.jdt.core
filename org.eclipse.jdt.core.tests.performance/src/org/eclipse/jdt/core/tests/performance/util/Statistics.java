/*******************************************************************************
 * Copyright (c) 2000, 2007 IBM Corporation and others.
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
package org.eclipse.jdt.core.tests.performance.util;

import java.text.NumberFormat;

import org.eclipse.test.internal.performance.InternalDimensions;
import org.eclipse.test.internal.performance.data.DataPoint;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.eval.StatisticsSession;
import org.eclipse.test.internal.performance.eval.StatisticsUtil;

public final class Statistics {
	static final NumberFormat DOUBLE_FORMAT = NumberFormat.getNumberInstance();
	static {
		DOUBLE_FORMAT.setMaximumFractionDigits(1);
	}

	StatisticsSession session;
	int count;
	long[][] measures;
	long[] min, max;
//	double[] smoothedAverages, smoothedSums;
//	long[] smoothedCounts;
	static final Dim[] DIMENSIONS = {
		InternalDimensions.CPU_TIME,
		InternalDimensions.ELAPSED_PROCESS,
		InternalDimensions.USED_JAVA_HEAP,
	};

public Statistics(DataPoint[] dataPoints) {
	this.session = new StatisticsSession(dataPoints);
	this.count = dataPoints.length / 2;
	int dimLength = DIMENSIONS.length;
	this.measures = new long[dimLength][this.count];
	this.min = new long[dimLength];
	this.max = new long[dimLength];
	for (int i = 0; i < DIMENSIONS.length; i++) {
		this.min[i] = Long.MAX_VALUE;
		this.max[i] = 0;
		for (int j = 0; j < this.count; j++) {
			long measure = dataPoints[2 * j + 1].getScalar(DIMENSIONS[i]).getMagnitude()
				- dataPoints[2 * j].getScalar(DIMENSIONS[i]).getMagnitude();
			this.measures[i][j] = measure;
			if (measure < this.min[i]) {
				this.min[i] = measure;
			}
			if (measure > this.max[i]) {
				this.max[i] = measure;
			}
		}
	}
}

@Override
public String toString() {
	StringBuilder buffer = new StringBuilder();
	int length = DIMENSIONS.length;
	for (int idx=0; idx<length; idx++) {
		dimToString(idx, buffer);
	}
	return buffer.toString();
}

public String toString(int dimIndex) {
	StringBuilder buffer = new StringBuilder();
	dimToString(dimIndex, buffer);
	return buffer.toString();
}

public String elapsedProcessToString() {
	StringBuilder buffer = new StringBuilder();
	dimToString(1, buffer);
	return buffer.toString();
}

void dimToString(int idx, StringBuilder buffer) {
	Dim dim = DIMENSIONS[idx];
	buffer.append(dim.getName());
	buffer.append(": n=");
	// long count = this.session.getCount(dim);
	buffer.append(this.count);
	buffer.append(", sum=");
	buffer.append(this.session.getSum(dim));
	buffer.append(", av=");
	buffer.append(this.session.getAverage(dim));
	buffer.append(", dev=");
	double stddev = this.session.getStddev(dim);
	buffer.append(DOUBLE_FORMAT.format(stddev));
	buffer.append(", err=");
	buffer.append(DOUBLE_FORMAT.format(stddev / Math.sqrt(this.count)));
	buffer.append(", interval=[");
	double[] interval = this.session.getConfidenceInterval(dim, StatisticsUtil.T90);
	buffer.append(DOUBLE_FORMAT.format(interval[0]));
	buffer.append('-');
	buffer.append(DOUBLE_FORMAT.format(interval[1]));
//	buffer.append("], smoothed: {n=");
//	smoothValues();
//	buffer.append(this.smoothedCounts[idx]);
//	buffer.append(", s=");
//	buffer.append(this.smoothedSums[idx]);
//	buffer.append(", a=");
//	buffer.append(this.smoothedAverages[idx]);
//	buffer.append("}, measures: {");
	buffer.append("], measures: {");
	for (int i = 0; i < this.count; i++) {
		if (i > 0)
			buffer.append(',');
		buffer.append(this.measures[idx][i]);
	}
	buffer.append("}, min=");
	buffer.append(this.min[idx]);
	buffer.append(", max=");
	buffer.append(this.max[idx]);
}

public long getSum(int dimIndex) {
	Dim dim = DIMENSIONS[dimIndex];
	return this.session.getSum(dim);
}

public double getStddev(int dimIndex) {
	Dim dim = DIMENSIONS[dimIndex];
	return this.session.getStddev(dim);
}

public double getAverage(int dimIndex) {
	Dim dim = DIMENSIONS[dimIndex];
	return this.session.getAverage(dim);
}

/*
public void smoothValues() {
	if (this.smoothedSums != null) return;
	int dimLength = DIMENSIONS.length;
	this.smoothedAverages = new double[dimLength];
	this.smoothedSums = new double[dimLength];
	this.smoothedCounts = new long[dimLength];
	for (int d=0; d<dimLength; d++) {
		Dim dim = DIMENSIONS[d];
		long c = this.session.getCount(dim);
		double[] interval = this.session.getConfidenceInterval(dim, StatisticsUtil.T90);
		long[] values  = new long[(int)c];
		int n = 0;
		for (int i=0; i<this.count; i++) {
			if (this.measures[d][i] >= interval[0] &&
				this.measures[d][i] <= interval[1]) {
				values[n++] = this.measures[d][i];
			}
		}
		if (n == c) {
			this.smoothedAverages[d] = getAverage(d);
			this.smoothedSums[d] = getSum(d);
			this.smoothedCounts[d] = getCount(d);
		} else {
			double sum = 0;
			for (int i=0; i<n; i++) {
				sum += values[i];
			}
			this.smoothedSums[d] = sum;
			this.smoothedAverages[d] = sum / n;
			this.smoothedCounts[d] = n;
		}
	}
}
*/

public long getCount(int dimIndex) {
	Dim dim = DIMENSIONS[dimIndex];
	return this.session.getCount(dim);
}
}
