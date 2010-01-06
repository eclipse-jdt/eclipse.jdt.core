/*******************************************************************************
 * Copyright (c) 2000, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.impl;

public class CompilerStats implements Comparable {

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (int) (this.endTime ^ (this.endTime >>> 32));
		result = prime * result + (int) (this.startTime ^ (this.startTime >>> 32));
		return result;
	}
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		CompilerStats other = (CompilerStats) obj;
		long time1 = elapsedTime();
		long time2 = other.elapsedTime();
		return time1 == time2;
	}
	// overall
	public long startTime;
	public long endTime;
	public long lineCount;

	// compile phases
	public long parseTime;
	public long resolveTime;
	public long analyzeTime;
	public long generateTime;

/**
 * Returns the total elapsed time (between start and end)
 * @return the time spent between start and end
 */
public long elapsedTime() {
	return this.endTime - this.startTime;
}
/**
 * @see java.lang.Comparable#compareTo(java.lang.Object)
 */
public int compareTo(Object o) {
	CompilerStats otherStats = (CompilerStats) o;
	long time1 = elapsedTime();
	long time2 = otherStats.elapsedTime();
	return time1 < time2 ? -1 : (time1 == time2 ? 0 : 1);
}
}
