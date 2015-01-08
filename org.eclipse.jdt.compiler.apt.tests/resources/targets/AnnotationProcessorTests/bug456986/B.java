/*******************************************************************************
 * Copyright (c) 2015 Google, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     het@google.com - initial API and implementation
 *******************************************************************************/
package targets.AnnotationProcessorTests.Bug456986;

import gen.anno.Annos.GenAnno;

@Bug456986
public class B {
	public void foo(@GenAnno int x) {
		return;
	}
}
