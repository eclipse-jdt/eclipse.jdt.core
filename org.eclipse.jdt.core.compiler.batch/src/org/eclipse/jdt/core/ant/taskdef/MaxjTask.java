package org.eclipse.jdt.core.ant.taskdef;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Map;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.Util;

/**
 *
 * @author Milos Djuric
 *
 */
public class MaxjTask extends Task{

		private static final String FAIL_MSG
		= "Compile failed; see the compiler error output for details.";  //$NON-NLS-1$

		private final static String EMPTY_STRING = ""; //$NON-NLS-1$
		private final static String JAVA_VERSION = "21"; //$NON-NLS-1$

		private boolean debug;
		private String debugLevel;
		private boolean nowarn;
		private boolean deprecation;
		private String destdir;
		private boolean verbose;
		private boolean failonerror;
		private String targetPlatform;
		private String source;
		private String encoding;
		private boolean listFiles;

		private Path classpathPath;
		private Path modulePath;
		private Path srcPath;
		private Path srcdirPath;
		private Path srcpathPath;
		private Path bootclassPath;
		private Path extdirsPath;

		private final ArrayList<CompileargTask> compilersArg;
		private final ArrayList<String> filesAndFolders;

		protected Map<String, String> customDefaultOptions;
		protected ArrayList<String> arguments;

		private int fileListIndex;

		public MaxjTask(){
			this.debug = false;
			this.debugLevel = EMPTY_STRING;
			this.deprecation = false;
			this.destdir = EMPTY_STRING;
			this.verbose = false;
			this.failonerror = true;
			this.targetPlatform = JAVA_VERSION;
			this.source = JAVA_VERSION;
			this.nowarn = false;
			this.encoding = EMPTY_STRING;
			this.listFiles = false;

			this.classpathPath = null;
			this.modulePath = null;
			this.srcPath = null;
			this.srcdirPath = null;
			this.srcpathPath = null;
			this.bootclassPath = null;
			this.extdirsPath = null;

			this.compilersArg = new ArrayList<>();
			this.filesAndFolders = new ArrayList<>();

			this.customDefaultOptions = new CompilerOptions().getMap();
			this.arguments = new ArrayList<>();

			this.fileListIndex = -1;
		}

		protected void logFolder(File folder){
			if(folder.isFile())
				log(folder.getAbsolutePath(),Project.MSG_INFO);
			if(folder.isDirectory()){
				File[] subFolders = folder.listFiles();
				for(int i = 0; i < subFolders.length; i++){
					logFolder(subFolders[i]);
				}
			}
		}

		@Override
		public void execute() throws BuildException {
			try {
				createArgumnetsLine();

				if(this.listFiles){
					log("",Project.MSG_INFO);//$NON-NLS-1$
					log("File list:",Project.MSG_INFO);//$NON-NLS-1$
					for(int i = 0; i < this.filesAndFolders.size();i++){
						logFolder(new File(this.filesAndFolders.get(i)));
					}
					log("",Project.MSG_INFO);//$NON-NLS-1$
				}

				if(this.fileListIndex >= 0){
					log("",Project.MSG_VERBOSE);//$NON-NLS-1$
					log("Compiling files and folders:",Project.MSG_VERBOSE);//$NON-NLS-1$
				}
				String[] argStringArray = new String[this.arguments.size()];
				for(int i = 0; i < this.arguments.size(); i++){
					argStringArray[i] = this.arguments.get(i);
					log(argStringArray[i],Project.MSG_VERBOSE);
					if(i == this.fileListIndex){
						log("",Project.MSG_VERBOSE);//$NON-NLS-1$
						log("Compiler options:",Project.MSG_VERBOSE);//$NON-NLS-1$
					}
				}
				Main compiler = new Main(new PrintWriter(System.out), new PrintWriter(System.err),true, this.customDefaultOptions, null);
				boolean success = compiler.compile(argStringArray);
				if(!success){
					if(this.failonerror){
						throw new BuildException(FAIL_MSG, getLocation());
                	} else {
                		log(FAIL_MSG, Project.MSG_ERR);
                	}
				}
			} catch (Exception ex) {
				throw new BuildException(ex);
			}

		}

		protected void createArgumnetsLine() {

			if(this.srcPath != null){
				//this.arguments.add("-sourcepath"); //$NON-NLS-1$
				String[] srcPaths = this.srcPath.list();
				for(int i = 0; i < srcPaths.length; i++){
					this.arguments.add(srcPaths[i]);
					this.filesAndFolders.add(srcPaths[i]);
				}
			}
			if(this.srcdirPath != null){
				//this.arguments.add("-sourcepath"); //$NON-NLS-1$
				String[] srcPaths = this.srcdirPath.list();
				for(int i = 0; i < srcPaths.length; i++){
					this.arguments.add(srcPaths[i]);
					this.filesAndFolders.add(srcPaths[i]);
				}
			}
			if(this.srcpathPath != null){
				//this.arguments.add("-sourcepath"); //$NON-NLS-1$
				String[] srcPaths = this.srcpathPath.list();
				for(int i = 0; i < srcPaths.length; i++){
					this.arguments.add(srcPaths[i]);
					this.filesAndFolders.add(srcPaths[i]);
				}
			}

			this.fileListIndex = this.arguments.size() - 1;

			/*
			 * This option is used to never exit at the end of the ant task.
			 */
			this.arguments.add("-noExit"); //$NON-NLS-1$
			if(this.bootclassPath != null){
				this.arguments.add("-bootclasspath"); //$NON-NLS-1$
				String[] classPaths = this.bootclassPath.list();
				StringBuffer classPath= new StringBuffer();
				for(int i = 0; i < classPaths.length; i++){
					classPath.append(classPaths[i]);
					if(i != classPaths.length - 1){
						classPath.append(":");//$NON-NLS-1$
					}
				}
				this.arguments.add(classPath.toString());
			}

			if(this.extdirsPath != null){
				this.arguments.add("-extdirs"); //$NON-NLS-1$
				String[] classPaths = this.extdirsPath.list();
				StringBuffer classPath= new StringBuffer();
				for(int i = 0; i < classPaths.length; i++){
					classPath.append(classPaths[i]);
					if(i != classPaths.length - 1){
						classPath.append(":");//$NON-NLS-1$
					}
				}
				this.arguments.add(classPath.toString());
			}
			if(this.classpathPath != null){
				this.arguments.add("-classpath"); //$NON-NLS-1$
				String[] classPaths = this.classpathPath.list();
				StringBuffer classPath= new StringBuffer();
				for(int i = 0; i < classPaths.length; i++){
					classPath.append(classPaths[i]);
					if(i != classPaths.length - 1){
						classPath.append(":");//$NON-NLS-1$
					}
				}
				this.arguments.add(classPath.toString());
			}
			if(this.modulePath != null){
				this.arguments.add("--module-path"); //$NON-NLS-1$
				String[] classPaths = this.modulePath.list();
				StringBuffer classPath= new StringBuffer();
				for(int i = 0; i < classPaths.length; i++){
					classPath.append(classPaths[i]);
					if(i != classPaths.length - 1){
						classPath.append(":");//$NON-NLS-1$
					}
				}
				this.arguments.add(classPath.toString());
			}

			this.customDefaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.DO_NOT_GENERATE);
			this.customDefaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.DO_NOT_GENERATE);
			this.customDefaultOptions.put(CompilerOptions.OPTION_SourceFileAttribute , CompilerOptions.DO_NOT_GENERATE);
			if(this.debug){
				if(!this.debugLevel.equals(EMPTY_STRING)){
					if(this.debugLevel.indexOf("lines") != -1){ //$NON-NLS-1$
						this.customDefaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
					}
					if(this.debugLevel.indexOf("vars") != -1){ //$NON-NLS-1$
						this.customDefaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
					}
					if(this.debugLevel.indexOf("source") != -1){ //$NON-NLS-1$
						this.customDefaultOptions.put(CompilerOptions.OPTION_SourceFileAttribute, CompilerOptions.GENERATE);
					}
				}else{
					this.customDefaultOptions.put(CompilerOptions.OPTION_LocalVariableAttribute, CompilerOptions.GENERATE);
					this.customDefaultOptions.put(CompilerOptions.OPTION_LineNumberAttribute, CompilerOptions.GENERATE);
					this.customDefaultOptions.put(CompilerOptions.OPTION_SourceFileAttribute , CompilerOptions.GENERATE);
				}
			}

			if (this.nowarn) {
				// disable all warnings
				for (Map.Entry<String, String> entry : this.customDefaultOptions.entrySet()) {
					if (entry.getValue().equals(CompilerOptions.WARNING)) {
						this.customDefaultOptions.put(entry.getKey(), CompilerOptions.IGNORE);
					}
				}
				this.customDefaultOptions.put(CompilerOptions.OPTION_TaskTags, Util.EMPTY_STRING);
				if (this.deprecation) {
					this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
					this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
					this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.ENABLED);
				}
			} else if (this.deprecation) {
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.ENABLED);
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.ENABLED);
			} else {
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.IGNORE);
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, CompilerOptions.DISABLED);
				this.customDefaultOptions.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, CompilerOptions.DISABLED);
			}
			if(!this.destdir.equals(EMPTY_STRING)){
				this.arguments.add("-d"); //$NON-NLS-1$
				this.arguments.add(this.destdir);
				log("",Project.MSG_INFO);//$NON-NLS-1$
				log("",Project.MSG_INFO);//$NON-NLS-1$
				log("Compiling to folder " + this.destdir ,Project.MSG_INFO);//$NON-NLS-1$
			}
			else{
				log("",Project.MSG_INFO);//$NON-NLS-1$
				log("",Project.MSG_INFO);//$NON-NLS-1$
				log("Compiling to source folders " + this.destdir ,Project.MSG_INFO);//$NON-NLS-1$
			}
			if (this.verbose) {
				this.arguments.add("-verbose"); //$NON-NLS-1$
			}
			if (!this.failonerror) {
				this.arguments.add("-proceedOnError"); //$NON-NLS-1$
			}

			for(int i = 0; i < this.compilersArg.size(); i++){
				this.arguments.add(this.compilersArg.get(i).getValue());
			}
			if (this.targetPlatform != null) {
				this.customDefaultOptions.put(CompilerOptions.OPTION_TargetPlatform, this.targetPlatform);
			}
			if (this.source != null) {
				this.customDefaultOptions.put(CompilerOptions.OPTION_Source, this.source);
			}

			/*
			 * encoding option. javac task encoding property must be the last encoding on the command
			 * line as compiler arg might also specify an encoding.
			 */
			if (!this.encoding.equals(EMPTY_STRING)) {
				this.arguments.add("-encoding"); //$NON-NLS-1$
				this.arguments.add(this.encoding);
			}

		}

		public Path createClasspath(){
			if(this.classpathPath == null)
				this.classpathPath = new Path(getProject());
			return this.classpathPath.createPath();
		}
	    public void setClasspath(Path classpath) {
		    if(this.classpathPath == null)
				this.classpathPath = new Path(getProject());
		    classpath.append(classpath);
	    }

		public Path createModulepath(){
			if(this.modulePath == null)
				this.modulePath = new Path(getProject());
			return this.modulePath.createPath();
		}
	    public void setModulepath(Path classpath) {
		    if(this.modulePath == null)
				this.modulePath = new Path(getProject());
		    classpath.append(classpath);
	    }

	    //src argument / sub task
	    public void setSrc(Path srcDir) {
			if(this.srcPath == null){
				this.srcPath = new Path(getProject());
			}
			this.srcPath.append(srcDir);
	    }
	    public Path createSrc() {
			if(this.srcdirPath == null){
				this.srcdirPath = new Path(getProject());
			}
			return this.srcdirPath.createPath();
	    }
	    //srcdir argument / sub task
	    public void setSrcdir(Path srcDir) {
			if(this.srcdirPath == null){
				this.srcdirPath = new Path(getProject());
			}
			this.srcdirPath.append(srcDir);
	    }
	    public Path createSrcdir() {
			if(this.srcdirPath == null){
				this.srcdirPath = new Path(getProject());
			}
			return this.srcdirPath.createPath();
	    }

	    //srcpath argument / sub task
	    public void setSourcepath(Path srcDir) {
			if(this.srcpathPath == null){
				this.srcpathPath = new Path(getProject());
			}
			this.srcpathPath.append(srcDir);
	    }
	    public Path createSourcepath() {
			if(this.srcpathPath == null){
				this.srcpathPath = new Path(getProject());
			}
			return this.srcpathPath.createPath();
	    }


	    //Bootclass argument / sub task
	    public void setBootclasspath(Path srcDir) {
			if(this.bootclassPath == null){
				this.bootclassPath = new Path(getProject());
			}
			this.bootclassPath.append(srcDir);
	    }
	    public Path createBootclasspath() {
			if(this.bootclassPath == null){
				this.bootclassPath = new Path(getProject());
			}
			return this.bootclassPath.createPath();
	    }

	    //extdirs argument / sub task
	    public void setExtdirs(Path srcDir) {
			if(this.extdirsPath == null){
				this.extdirsPath = new Path(getProject());
			}
			this.extdirsPath.append(srcDir);
	    }
	    public Path createExtdirs() {
			if(this.extdirsPath == null){
				this.extdirsPath = new Path(getProject());
			}
			return this.extdirsPath.createPath();
	    }

	    public void setDebugLevel(String  v) {
	    	this.debugLevel = v;
	    }
	    public void setSource(String  v) {
	    	this.source = v;
	    }
	    public void setDestdir(File destDir) {
		    this.destdir = destDir.getAbsolutePath();
	    }
	    public void setFailonerror(boolean fail) {
	    	this.failonerror = fail;
	    }
	    public void setDeprecation(boolean deprecation) {
		    this.deprecation = deprecation;
	    }
	    public void setDebug(boolean debug) {
		    this.debug = debug;
	    }
	    public void setVerbose(boolean verbose) {
		    this.verbose = verbose;
	    }
	    public void setTarget(String target) {
		    this.targetPlatform = target;
	    }
	    public void setNowarn(boolean flag) {
		    this.nowarn = flag;
	    }

	    public void setSourcepathref(Reference r) {
    		this.createSrcdir().setRefid(r);
	    }

	    public void setClasspathref(Reference r) {
	    	this.createClasspath().setRefid(r);
	    }

	    public void setBootclasspathref(Reference r) {
	    	this.createBootclasspath().setRefid(r);
	    }

	    public CompileargTask createCompilerarg(){
	    	CompileargTask arg = new CompileargTask();
	    	this.compilersArg.add(arg);
	    	return arg;
	    }

	    public void setDepend(boolean depend) {
			/*
			 * Jdt compiler doesn't support this attribute
			 */
	    }
	    public void setOptimize(boolean optimize) {
			/*
			 * Jdt compiler doesn't support this attribute
			 */
	    }
	    public void setListfiles(boolean listFiles) {
			this.listFiles = listFiles;
	    }
	    public void setEncoding(String encoding) {
			this.encoding = encoding;
	    }
}
