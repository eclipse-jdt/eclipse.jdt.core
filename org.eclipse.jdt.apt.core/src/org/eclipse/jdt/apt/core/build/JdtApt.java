/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *    
 *******************************************************************************/
package org.eclipse.jdt.apt.core.build;

import java.io.File;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.Java;
import org.apache.tools.ant.types.Path;

/**
 * Ant task for invoking the commandline apt builder
 *
 * Sample build.xml:
 * 
 * <project name="test_eclipse" default="build" basedir=".">
 * 
 *    <taskdef name="apt" classname="org.eclipse.jdt.apt.core.build.JdtApt"/>
 *
 *    <target name="build">
 *        <apt workspace="C:\my_workspace" eclipseHome="C:\eclipse"/>
 *    </target>
 * </project>
 */
public class JdtApt extends Java {

	private static final String APP_CLASSNAME = "org.eclipse.core.launcher.Main"; //$NON-NLS-1$
    private static final String APP_PLUGIN = "org.eclipse.jdt.apt.core.aptBuild"; //$NON-NLS-1$
    
    private File workspace;
    private File startupJar;
    
    public void setWorkspace(File file) {
        if(!file.exists()) {
            throw new BuildException(Messages.JdtApt_noWorkspace + file);
        }
        workspace = file;
    }

    public void setEclipseHome(File file) {
        if(!file.exists()) {
            throw new BuildException(Messages.JdtApt_noEclipse + file);
        }
        startupJar = new File(file, "startup.jar"); //$NON-NLS-1$
        if(!startupJar.exists()) {
            throw new BuildException(Messages.JdtApt_noStartupJar + file);
        }
    }
    
    public void execute() throws BuildException {
	    if(workspace == null) {
	        throw new BuildException("Must set a workspace"); //$NON-NLS-1$
	    }
	    if(startupJar == null) {
	        throw new BuildException("Must set eclipse home"); //$NON-NLS-1$
	    }
	    
        setFork(true);
        setLogError(true);
        setClasspath(new Path(null, startupJar.getAbsolutePath()));
        setClassname(APP_CLASSNAME);
        createArg().setValue("-noupdate"); //$NON-NLS-1$
        createArg().setValue("-application"); //$NON-NLS-1$
        createArg().setValue(APP_PLUGIN);
        createArg().setValue("-data"); //$NON-NLS-1$
        createArg().setValue(workspace.getAbsolutePath());
        super.execute();
	}


}
