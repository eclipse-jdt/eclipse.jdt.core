/*******************************************************************************
 * Copyright (c) 2000, 2015 IBM Corporation and others.
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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.test.internal.performance.OSPerformanceMeter;
import org.eclipse.test.internal.performance.data.DataPoint;
import org.eclipse.test.internal.performance.data.Sample;

@SuppressWarnings({"rawtypes", "unchecked"})
public class JdtCorePerformanceMeter extends OSPerformanceMeter {

public static final Map STATISTICS = new HashMap();

public JdtCorePerformanceMeter(String scenarioId) {
	super(scenarioId);
}

/*
 * @see org.eclipse.test.performance.PerformanceMeter#commit()
 */
public void commit() {
	Sample sample = getSample();
	if (sample != null) {
		storeDataPoints(sample);
	}
}

private void storeDataPoints(Sample sample) {
	DataPoint[] dataPoints = sample.getDataPoints();
	int length = dataPoints.length;
	if (length > 0) {
		System.out.println("	Store " + length + " data points...");
		STATISTICS.put(getReadableName(), dataPoints);
	}
}

public String getReadableName() {
	String name = getScenarioName();
	return name.substring(name.lastIndexOf('.') + 1, name.length() - 2);
}

public String getShortName() {
	String name = getReadableName();
	return name.substring(name.lastIndexOf('#') + 5/* 1+"test".length() */, name.length());
}

}
