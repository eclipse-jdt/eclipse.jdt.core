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

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

public class JarFactoryContainer extends FactoryContainer
{
	public JarFactoryContainer( File jarFile )
	{
		_jarFile = jarFile.getAbsoluteFile();
	}
	
	public void loadFactoryNames() { 
		// TODO 
	}
	
	public boolean isPlugin() { return false; }
	public URL getJarFileURL() throws MalformedURLException { return _jarFile.toURL(); }
		
	private File _jarFile;
}

