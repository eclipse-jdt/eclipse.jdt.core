/*******************************************************************************
 * Copyright (c) 2000, 2003 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.search;

/**
 * generic result. hierarchy can be extended
 * TODO add spec
 * @since 3.0
 */
public class SearchMatch {
	
	/* match accuracy */
	public static final int A_ACCURATE = 1;
	public static final int A_INACCURATE = 2;
	
	/* generic information */
	private final int accuracy;
	private final  String descriptiveLocation;
	private final String documentPath;
	private final String name;
	private final SearchParticipant participant;	
	private final int sourceEnd;
	private final int sourceLineNumber;
	private final int sourceStart;

	public SearchMatch(
			String name, 
			String documentPath, 
			int accuracy,  
			SearchParticipant participant, 
			int sourceStart, 
			int sourceEnd, 
			int sourceLineNumber, 
			String descriptiveLocation) {

		this.name = name;
		this.documentPath = documentPath;
		this.accuracy = accuracy;
		this.participant = participant;
		this.sourceStart = sourceStart;
		this.sourceEnd = sourceEnd;
		this.sourceLineNumber = sourceLineNumber;
		this.descriptiveLocation = descriptiveLocation;
	}
	
	public int getAccuracy() {
		return this.accuracy;
	}

	public String getDescriptiveLocation() {
		return this.descriptiveLocation;
	}

	public String getDocumentPath() {
		return this.documentPath;
	}

	public String getName() {
		return this.name;
	}

	/**
	 * Returns the participant which issued this match
	 */
	public SearchParticipant getParticipant() {
		return this.participant;
	}
	
	public int getSourceEnd() {
		return this.sourceEnd;
	}

	public int getSourceLineNumber() {
		return this.sourceLineNumber;
	}

	public int getSourceStart() {
		return this.sourceStart;
	}
}
