package org.eclipse.jdt.core.ant;

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
			throw new BuildException(Util.bind("jdtcom.source"/*nonNLS*/));
		if(dest == null)
			throw new BuildException(Util.bind("jdtcom.destination"/*nonNLS*/));
		
		arguments.append(" -d "/*nonNLS*/);
		arguments.append(dest.getAbsolutePath());
		
		if(classpath != null){
			arguments.append(" -classpath "/*nonNLS*/);
			String[] classpathList = classpath.list();
			for(int i = 0 ; i < classpathList.length ; i++){
				File pathElement = project.resolveFile(classpathList[i]);
				if(!pathElement.exists())
					throw new BuildException(Util.bind("jdtcom.classpath"/*nonNLS*/,pathElement.getAbsolutePath()));
				if(i != 0)
					arguments.append(";"/*nonNLS*/);
				arguments.append(pathElement);
			}
		}
				
		String[] srcList = src.list();
		for(int i = 0 ; i < srcList.length ; i++){
			File file = project.resolveFile(srcList[i]);
			if(!file.exists())
				throw new BuildException(Util.bind("jdtcom.sourcepath"/*nonNLS*/,file.getAbsolutePath()));
			if(!file.isDirectory())
				throw new BuildException(Util.bind("jdtcom.sourcedir"/*nonNLS*/,file.getAbsolutePath()));
			DirectoryScanner ds = getDirectoryScanner(file);
			String[] files = ds.getIncludedFiles();
			for(int j =  0; j < files.length ; j++){
				if(files[j].endsWith(".java"/*nonNLS*/)){
					arguments.append(" "/*nonNLS*/);
					arguments.append(new File(file,files[j]).getAbsolutePath());
				}
			}
		}

		try {
			Main.compile(arguments.toString(),new AntPrintWriter(this));
		}
		catch(Exception e){
			throw new BuildException("Jdtcom"/*nonNLS*/,e);
		}
		log("FINISH"/*nonNLS*/);
	}
	
	public void setProceedonerror(boolean proceed){
		if(proceed)
			arguments.append(" -proceedOnError"/*nonNLS*/);
	}
	
	public void setTime(boolean time){
		if(time)
			arguments.append(" -time"/*nonNLS*/);
	}
	
	public void setVersion(boolean version){
		if(version)
			arguments.append(" -version"/*nonNLS*/);
	}
	
	public void setNoimporterror(boolean noimporterror){
		if(noimporterror)
			arguments.append(" -noImportError"/*nonNLS*/);
	}
	
	public void setVerbose(boolean verbose){
		if(verbose)
			arguments.append(" -verbose"/*nonNLS*/);
	}
	
	public void setReferenceinfo(boolean referenceinfo){
		if(referenceinfo)
			arguments.append(" -referenceInfo"/*nonNLS*/);
	}

	public void setPreservealllocals(boolean preservealllocals){
		if(preservealllocals)
			arguments.append(" -preserveAllLocals"/*nonNLS*/);
	}
	
	public void setTarget(String target){
		if (!target.equals("1.1"/*nonNLS*/) && !target.equals("1.2"/*nonNLS*/))
			throw new BuildException(Util.bind("jdtcom.target"/*nonNLS*/));
		arguments.append(" -target "/*nonNLS*/);
		arguments.append(target);
	}
	
	public void setLog(File log){
		try {
			new PrintWriter(new FileOutputStream(log.getAbsolutePath(), false));
		} catch(IOException e){
			throw new BuildException(Util.bind("jdtcom.log"/*nonNLS*/,log.getAbsolutePath()));
		}
		arguments.append(" -log "/*nonNLS*/);
		arguments.append(log.getAbsolutePath());
	}
	
	public void setRepeat(int repeat){
		if(repeat < 0)
			throw new BuildException(Util.bind("jdtcom.repeat"/*nonNLS*/));
		arguments.append(" -repeat "/*nonNLS*/);
		arguments.append(String.valueOf(repeat));
	}
	
	public void setWarning(String warning){
		if(warning.equals("no"/*nonNLS*/)){
			arguments.append(" -nowarn"/*nonNLS*/);
		}
		else{
			StringTokenizer tokenizer = new StringTokenizer(warning, ","/*nonNLS*/);			
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!token.equals("constructorName"/*nonNLS*/) &&
					!token.equals("packageDefaultMethod"/*nonNLS*/) &&
					!token.equals("maskedCatchBlocks"/*nonNLS*/) &&
					!token.equals("deprecation"/*nonNLS*/) &&
					!token.equals("unusedLocals"/*nonNLS*/) &&
					!token.equals("unusedArguments"/*nonNLS*/) &&
					!token.equals("syntheticAccess"/*nonNLS*/) &&
					!token.equals("nls"/*nonNLS*/))
					throw new BuildException(Util.bind("jdtcom.warning"/*nonNLS*/));
			}
			arguments.append(" -warn:"/*nonNLS*/+warning);
		}
	}
	
	public void setDebug(String debug){
		if(debug.equals("no"/*nonNLS*/)){
			arguments.append(" -g:none"/*nonNLS*/);
		}
		else if (debug.equals("all"/*nonNLS*/)){
			arguments.append(" -g"/*nonNLS*/);
		}
		else{
			StringTokenizer tokenizer = new StringTokenizer(debug, ","/*nonNLS*/);
			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				if (!token.equals("vars"/*nonNLS*/) && !token.equals("lines"/*nonNLS*/) && !token.equals("source"/*nonNLS*/))
					throw new BuildException(Util.bind("jdtcom.debug"/*nonNLS*/));
			}
			arguments.append(" -g:"/*nonNLS*/+debug);
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

