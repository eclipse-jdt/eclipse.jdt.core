package org.eclipse.jdt.core.ant;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */

import java.util.*;
import java.io.*; 

import org.apache.tools.ant.*;
import org.apache.tools.ant.taskdefs.*;
import org.apache.tools.ant.types.*;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.core.ant.*;

public class Jdtcom extends MatchingTask {
	private Path src;
	private Path classpath;
	private File dest;
	
	private StringBuffer arguments;

	public Jdtcom(){
		arguments = new StringBuffer();
	}

	public void execute() throws BuildException {
		if(src == null)
			throw new BuildException(Util.bind("jdtcom.source")); //$NON-NLS-1$
		if(dest == null)
			throw new BuildException(Util.bind("jdtcom.destination")); //$NON-NLS-1$
		
		arguments.append(" -d "); //$NON-NLS-1$
		arguments.append(dest.getAbsolutePath());
		
		if(classpath != null){
			arguments.append(" -classpath "); //$NON-NLS-1$
			String[] classpathList = classpath.list();
			for(int i = 0 ; i < classpathList.length ; i++){
				File pathElement = project.resolveFile(classpathList[i]);
				if(!pathElement.exists())
					throw new BuildException(Util.bind("jdtcom.classpath",pathElement.getAbsolutePath())); //$NON-NLS-1$
				if(i != 0)
					arguments.append(";"); //$NON-NLS-1$
				arguments.append(pathElement);
			}
		}
				
		String[] srcList = src.list();
		for(int i = 0 ; i < srcList.length ; i++){
			File file = project.resolveFile(srcList[i]);
			if(!file.exists())
				throw new BuildException(Util.bind("jdtcom.sourcepath",file.getAbsolutePath())); //$NON-NLS-1$
			if(!file.isDirectory())
				throw new BuildException(Util.bind("jdtcom.sourcedir",file.getAbsolutePath())); //$NON-NLS-1$
			DirectoryScanner ds = getDirectoryScanner(file);
			String[] files = ds.getIncludedFiles();
			for(int j =  0; j < files.length ; j++){
				if(files[j].endsWith(".java")){ //$NON-NLS-1$
					arguments.append(" "); //$NON-NLS-1$
					arguments.append(new File(file,files[j]).getAbsolutePath());
				}
			}
		}

		try {
			Main.compile(arguments.toString(),new AntPrintWriter(this));
		}
		catch(Exception e){
			throw new BuildException("Jdtcom",e); //$NON-NLS-1$
		}
		log("FINISH"); //$NON-NLS-1$
	}
	
	public void setProceedonerror(boolean proceed){
		if(proceed)
			arguments.append(" -proceedOnError"); //$NON-NLS-1$
	}
	
	public void setTime(boolean time){
		if(time)
			arguments.append(" -time"); //$NON-NLS-1$
	}
	
	public void setVersion(boolean version){
		if(version)
			arguments.append(" -version"); //$NON-NLS-1$
	}
	
	public void setNoimporterror(boolean noimporterror){
		if(noimporterror)
			arguments.append(" -noImportError"); //$NON-NLS-1$
	}
	
	public void setVerbose(boolean verbose){
		if(verbose)
			arguments.append(" -verbose"); //$NON-NLS-1$
	}
	
	public void setReferenceinfo(boolean referenceinfo){
		if(referenceinfo)
			arguments.append(" -referenceInfo"); //$NON-NLS-1$
	}

	public void setPreservealllocals(boolean preservealllocals){
		if(preservealllocals)
			arguments.append(" -preserveAllLocals"); //$NON-NLS-1$
	}
	
	public void setTarget(String target){
		if (!target.equals("1.1") && !target.equals("1.2")) //$NON-NLS-2$ //$NON-NLS-1$
			throw new BuildException(Util.bind("jdtcom.target")); //$NON-NLS-1$
		arguments.append(" -target "); //$NON-NLS-1$
		arguments.append(target);
	}
	
	public void setLog(File log){
		try {
			new PrintWriter(new FileOutputStream(log.getAbsolutePath(), false));
		} catch(IOException e){
			throw new BuildException(Util.bind("jdtcom.log",log.getAbsolutePath())); //$NON-NLS-1$
		}
		arguments.append(" -log "); //$NON-NLS-1$
		arguments.append(log.getAbsolutePath());
	}
	
	public void setRepeat(int repeat){
		if(repeat < 0)
			throw new BuildException(Util.bind("jdtcom.repeat")); //$NON-NLS-1$
		arguments.append(" -repeat "); //$NON-NLS-1$
		arguments.append(String.valueOf(repeat));
	}
	
	public void setWarning(String warning){
		if(warning.equals("no")){ //$NON-NLS-1$
			arguments.append(" -nowarn"); //$NON-NLS-1$
		}
		else{
			StringTokenizer tokenizer = new StringTokenizer(warning, ",");			 //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!token.equals("constructorName") && //$NON-NLS-1$
					!token.equals("packageDefaultMethod") && //$NON-NLS-1$
					!token.equals("maskedCatchBlocks") && //$NON-NLS-1$
					!token.equals("deprecation") && //$NON-NLS-1$
					!token.equals("unusedLocals") && //$NON-NLS-1$
					!token.equals("unusedArguments") && //$NON-NLS-1$
					!token.equals("syntheticAccess") && //$NON-NLS-1$
					!token.equals("nls")) //$NON-NLS-1$
					throw new BuildException(Util.bind("jdtcom.warning")); //$NON-NLS-1$
			}
			arguments.append(" -warn:"+warning); //$NON-NLS-1$
		}
	}
	
	public void setDebug(String debug){
		if(debug.equals("no")){ //$NON-NLS-1$
			arguments.append(" -g:none"); //$NON-NLS-1$
		}
		else if (debug.equals("all")){ //$NON-NLS-1$
			arguments.append(" -g"); //$NON-NLS-1$
		}
		else{
			StringTokenizer tokenizer = new StringTokenizer(debug, ","); //$NON-NLS-1$
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!token.equals("vars") && !token.equals("lines") && !token.equals("source")) //$NON-NLS-1$ //$NON-NLS-3$ //$NON-NLS-2$
					throw new BuildException(Util.bind("jdtcom.debug")); //$NON-NLS-1$
			}
			arguments.append(" -g:"+debug); //$NON-NLS-1$
		}
	}
	
	public void setDestdir(File dest){
		this.dest = dest;
	}
	
	public void setClasspath(Path path){
		if (classpath == null) {
            classpath = path;
        }
		classpath.append(path);
	}
	
	public Path createClasspath() {
        if (classpath == null) {
            classpath = new Path(project);
        }
        return classpath.createPath();
    }
	
	public void setSrcdir(Path path){
		if (src == null) {
            src = path;
        }
		src.append(path);
	}
	
	public Path createSrc() {
        if (src == null) {
            src = new Path(project);
        }
        return src.createPath();
    }
}

