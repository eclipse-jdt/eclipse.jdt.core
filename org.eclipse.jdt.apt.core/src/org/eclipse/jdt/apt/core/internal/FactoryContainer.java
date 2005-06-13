/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    mkaufman@bea.com - initial API and implementation
 *******************************************************************************/


package org.eclipse.jdt.apt.core.internal;

import java.util.ArrayList;
import java.util.List;

public abstract class FactoryContainer
{
	public abstract boolean isPlugin();
	public abstract void loadFactoryNames();
	public  List<String> getFactoryNames() { return _factoryNames; }
	protected List<String> _factoryNames = new ArrayList<String>();
}

