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

public class Error extends Throwable {
	private static final long serialVersionUID = 9179125830904709243L;

	public Error(String s) {
		super(s);
	}
	
	public Error() {
	}	
}
