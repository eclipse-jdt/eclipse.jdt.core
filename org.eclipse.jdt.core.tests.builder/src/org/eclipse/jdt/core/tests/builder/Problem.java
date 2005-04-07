/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.builder;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.IPath;

public class Problem implements Comparable {
	private String location;
	private String message;
	private IPath resourcePath;
	private int start = -1, end = -1;
	
	public Problem(String location, String message, IPath resourcePath){
		this(location, message, resourcePath, -1, -1);
	}
	public Problem(String location, String message, IPath resourcePath, int start, int end){
		this.location = location;
		this.message = message;
		this.resourcePath = resourcePath;
		this.start = start;
		this.end = end;
	}
	
	public Problem(IMarker marker) {
		this(marker, false);
	}
	
	public Problem(IMarker marker, boolean storeRange){
		this.location = marker.getAttribute(IMarker.LOCATION, ""); //$NON-NLS-1$
		this.message = marker.getAttribute(IMarker.MESSAGE, ""); //$NON-NLS-1$
		this.resourcePath = marker.getResource().getFullPath();
		if (storeRange) {
			this.start = marker.getAttribute(IMarker.CHAR_START, -1);
			this.end = marker.getAttribute(IMarker.CHAR_END, -1);
		}
	}
	/**
	 * Gets the location.
	 * @return Returns a String
	 */
	public String getLocation() {
		return location;
	}
	/**
	 * Gets the message.
	 * @return Returns a String
	 */
	public String getMessage() {
		return message;
	}
	/**
	 * Gets the resourcePath.
	 * @return Returns a IPath
	 */
	public IPath getResourcePath() {
		return resourcePath;
	}
	
	public int getStart() {
		return this.start;
	}
	
	public int getEnd() {
		return this.end;
	}
	
	public String toString(){
// ignore locations since the builder no longer finds exact Java elements
//		return "Problem : " + message + " [ resource : <" + resourcePath + "> location <"+ location + "> ]"; 
		return 
			"Problem : " 
			+ message 
			+ " [ resource : <" 
			+ resourcePath 
			+ ">" 
			+ ((this.start != -1 && this.end != -1) ? (" range : <" + this.start + "," + this.end + ">") : "")
			+ "]";
	}
	
	public boolean equals(Object o){
		if(o instanceof Problem){
			return this.toString().equals(o.toString());
		}
		return false;
	}
	
	public int compareTo(Object o) {
		Problem problem = (Problem) o;
		if (!(this.getLocation().equals(problem.getLocation()))) {
			return this.getLocation().compareTo(problem.getLocation());
		}
		if (this.getStart() < problem.getStart()) {
			return -1;
		}
		if (this.getEnd() < problem.getEnd()) {
			return -1;
		}
		return this.getMessage().compareTo(problem.getMessage());
	}
}

