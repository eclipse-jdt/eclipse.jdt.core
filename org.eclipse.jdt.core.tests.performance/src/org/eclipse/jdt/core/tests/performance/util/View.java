/*******************************************************************************
 * Copyright (c) 2004, 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.performance.util;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

import org.eclipse.test.internal.performance.PerformanceTestPlugin;
import org.eclipse.test.internal.performance.data.Dim;
import org.eclipse.test.internal.performance.db.DB;
import org.eclipse.test.internal.performance.db.Report;
import org.eclipse.test.internal.performance.db.Scenario;
import org.eclipse.test.internal.performance.db.TimeSeries;
import org.eclipse.test.internal.performance.db.Variations;

/**
 * Dumps performance data to stdout.
 */
public class View {

public static void main(String[] args) {

	Variations variations = PerformanceTestPlugin.getVariations();
	variations.put("config", "eclipseperflnx1_R3.3"); //$NON-NLS-1$//$NON-NLS-2$
	variations.put("build", "I2007%"); //$NON-NLS-1$//$NON-NLS-2$
	variations.put("jvm", "sun"); //$NON-NLS-1$//$NON-NLS-2$

	String scenarioPattern = "%testFullBuildProject%"; //$NON-NLS-1$

	String seriesKey = PerformanceTestPlugin.BUILD;

	String outFile = null;
	if (args != null && args.length > 0) {
		outFile = args[0];
	}
	// outFile= "/tmp/dbdump"; //$NON-NLS-1$
	PrintStream ps = null;
	if (outFile != null) {
		try {
			ps = new PrintStream(new BufferedOutputStream(new FileOutputStream(outFile)));
		} catch (FileNotFoundException e) {
			System.err.println("can't create output file"); //$NON-NLS-1$
		}
	}
	if (ps == null)
		ps = System.out;

	Scenario[] scenarios = DB.queryScenarios(variations, scenarioPattern, seriesKey, null);
	System.out.println(scenarios.length + " Scenarios"); //$NON-NLS-1$
	System.out.println();

	for (int s = 0; s < scenarios.length; s++)
		dump(ps, scenarios[s]);

	if (ps != System.out)
		ps.close();
}

private static void dump(PrintStream ps, Scenario scenario) {
	// scenario.dump(ps, PerformanceTestPlugin.BUILD);
	System.out.print("Scenario: " + scenario.getScenarioName()+"..."); //$NON-NLS-1$
	ps.println("Scenario: " + scenario.getScenarioName()); //$NON-NLS-1$
	Report r = new Report(2);

	String[] timeSeriesLabels = scenario.getTimeSeriesLabels();
	r.addCell(PerformanceTestPlugin.BUILD + ":"); //$NON-NLS-1$
	for (int j = 0; j < timeSeriesLabels.length; j++)
		r.addCellRight(timeSeriesLabels[j]);
	r.nextRow();

	Dim[] dimensions = scenario.getDimensions();
	for (int i = 0; i < dimensions.length; i++) {
		Dim dim = dimensions[i];
		String dimName = dim.getName();
		if (!dimName.equals("CPU Time") && !dimName.equals("Elapsed Process") && !dimName.equals("Used Java Heap")) {
			continue;
		}
		r.addCell(dimName + ':');

		TimeSeries ts = scenario.getTimeSeries(dim);
		int n = ts.getLength();
		for (int j = 0; j < n; j++) {
			String stddev = ""; //$NON-NLS-1$
			double stddev2 = ts.getStddev(j);
			if (stddev2 != 0.0)
				stddev = " [" + dim.getDisplayValue(stddev2) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			r.addCellRight(dim.getDisplayValue(ts.getValue(j)) + stddev);
		}
		r.nextRow();
	}
	r.print(ps);
	ps.println();
	System.out.println("done");
}
}
