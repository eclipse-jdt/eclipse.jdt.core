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

	private static final String APP_CLASSNAME = "org.eclipse.core.launcher.Main";
    private static final String APP_PLUGIN = "org.eclipse.jdt.apt.core.aptBuild";
    
    private File workspace;
    private File startupJar;
    
    public void setWorkspace(File file) {
        if(!file.exists()) {
            throw new BuildException("Workspace does not exist: " + file);
        }
        workspace = file;
    }

    public void setEclipseHome(File file) {
        if(!file.exists()) {
            throw new BuildException("Eclipse not found in eclipse home: " + file);
        }
        startupJar = new File(file, "startup.jar");
        if(!startupJar.exists()) {
            throw new BuildException("Could not find startup.jar in the eclipse directory: " + file);
        }
    }
    
    public void execute() throws BuildException {
	    if(workspace == null) {
	        throw new BuildException("Must set a workspace");
	    }
	    if(startupJar == null) {
	        throw new BuildException("Must set eclipse home");
	    }
	    
        setFork(true);
        setLogError(true);
        setClasspath(new Path(null, startupJar.getAbsolutePath()));
        setClassname(APP_CLASSNAME);
        createArg().setValue("-noupdate");
        createArg().setValue("-application");
        createArg().setValue(APP_PLUGIN);
        createArg().setValue("-data");
        createArg().setValue(workspace.getAbsolutePath());
        super.execute();
	}


}
