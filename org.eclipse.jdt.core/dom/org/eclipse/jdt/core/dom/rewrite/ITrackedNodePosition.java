/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.dom.rewrite;

/**
 * A tracked node positions is retrurned when a rewrite change is requested to be tracked
 * 
 */
public interface ITrackedNodePosition {
	
	/**
	 * Returns the original or modified start position of the tracked node depending if called before
	 * or after the rewrite is applied. <code>-1</code> is returned for removed nodes.
	 * 
	 * @return Returns the original or modified start position of the tracked node
	 */
	public int getStartPosition();
	
	/**
	 * Returns the original or modified length of the tracked node depending if called before
	 * or after the rewrite is applied. <code>-1</code> is returned for removed nodes.
	 * 
	 * @return Returns the original or modified length of the tracked node
	 */
	public int getLength();
	
	
}
