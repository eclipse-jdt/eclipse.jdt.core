package org.eclipse.jdt.core.ant.taskdef;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.StringTokenizer;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.Commandline;
import org.apache.tools.ant.types.Path;
import org.apache.tools.ant.types.Reference;
import org.eclipse.jdt.core.ant.taskdef.DocletTask.DocletParam;

/**
 *
 * @author Milos Djuric
 *
 */
public class MaxjDocTask extends Task{

	private static final String FAIL_MSG
	= "Javadoc failed; see the tools error output for details.";  //$NON-NLS-1$


	private final static String EMPTY_STRING = ""; //$NON-NLS-1$
	private final static String NAME_LIST_SEPARATOR = ","; //$NON-NLS-1$
	private final static String OPTION_PREFIX = "-"; //$NON-NLS-1$
	private final static String PATH_SEPARATOR = ":"; //$NON-NLS-1$
	private final static String EXECUTABLE_NAME = "maxjdoc"; //$NON-NLS-1$

	private Path sourcepath;

	private String sourcefiles;

	private String destdir;
	private final static String DESTDIR_OPTION = "d"; //$NON-NLS-1$

	private String packagenames;

	private Path classpath;
	private final static String CLASSPATH_OPTION = "classpath"; //$NON-NLS-1$

	private String extdirs;
	private final static String EXTDIRS_OPTION = "extdirs"; //$NON-NLS-1$

	private String overview;
	private final static String OVERVIEW_OPTION = "overview"; //$NON-NLS-1$

	private String access;
	private boolean public_att;
	private boolean protected_att;
	private boolean package_att;
	private boolean private_att;
	private final static String PUBLIC_ACCESS_OPTION = "public"; //$NON-NLS-1$
	private final static String PROTECTED_ACCESS_OPTION = "protected"; //$NON-NLS-1$
	private final static String PACKAGE_ACCESS_OPTION = "package"; //$NON-NLS-1$
	private final static String PRIVATE_ACCESS_OPTION = "private"; //$NON-NLS-1$

	private boolean verbose;
	private final static String VERBOSE_OPTION = "verbose"; //$NON-NLS-1$

	private boolean version;
	private final static String VERSION_OPTION = "version"; //$NON-NLS-1$

	private boolean use;
	private final static String USE_OPTION = "use"; //$NON-NLS-1$

	private boolean author;
	private final static String AUTHOR_OPTION = "author"; //$NON-NLS-1$

	private boolean splitindex;
	private final static String SPLITINDEX_OPTION = "splitindex"; //$NON-NLS-1$

	private String windowtitle;
	private final static String WINDOWTITLE_OPTION = "windowtitle"; //$NON-NLS-1$

	private String doctitle;
	private final static String DOCTITLE_OPTION = "doctitle"; //$NON-NLS-1$

	private String header;
	private final static String HEADER_OPTION = "header"; //$NON-NLS-1$

	private String footer;
	private final static String FOOTER_OPTION = "footer"; //$NON-NLS-1$

	private String bottom;
	private final static String BOTTOM_OPTION = "bottom"; //$NON-NLS-1$

	private boolean nodeprecated;
	private final static String NODEPRECATED_OPTION = "nodeprecated"; //$NON-NLS-1$

	private boolean nodeprecatedlist;
	private final static String NODEPRACATEDLIST_OPTION = "nodeprecatedlist"; //$NON-NLS-1$

	private boolean notree;
	private final static String NOTREE_OPTION = "notree"; //$NON-NLS-1$

	private boolean noindex;
	private final static String NOINDEX_OPTION = "noindex"; //$NON-NLS-1$

	private boolean nohelp;
	private final static String NOHELP_OPTION = "nohelp"; //$NON-NLS-1$

	private boolean nonavbar;
	private final static String NONAVBAR_OPTION = "nonavbar"; //$NON-NLS-1$

	private boolean serialwarn;
	private final static String SERIALWARN_OPTION = "serialwarn"; //$NON-NLS-1$

	private String source;
	private final static String SOURCE_OPTION = "source"; //$NON-NLS-1$

	private ArrayList<String> packages;
	private ArrayList<String> srcFiles;

	private FileFilter dirFilter;
	private FilenameFilter sourceFileFilter;

	private ArrayList<LinkArgument> links;
	private final static String LINK_OPTION = "link"; //$NON-NLS-1$

	private DocletTask doclet;
	private final static String DOCLET_OPTION = "doclet";//$NON-NLS-1$
	private final static String DOCLETPATH_OPTION = "docletpath";//$NON-NLS-1$

	private File styleSheetFile;
	private final static String STYLESHEETFILE_OPTION = "stylesheetfile"; //$NON-NLS-1$

	private String additionalParam;
	//private final static String ADDITIONALPARAM_OPTION = "additionalparam"; //$NON-NLS-1$

	private boolean failonerror;

	public MaxjDocTask(){
		this.sourcepath = null;
		this.sourcefiles = EMPTY_STRING;
		this.destdir = EMPTY_STRING;
		this.packagenames = EMPTY_STRING;
		this.classpath = null;
		this.extdirs = EMPTY_STRING;
		this.overview = EMPTY_STRING;
		this.access = EMPTY_STRING;
		this.public_att = false;
		this.protected_att = false;
		this.package_att = false;
		this.private_att = false;
		this.verbose = false;
		this.version = true;
		this.use = false;
		this.author = true;
		this.splitindex = false;
		this.windowtitle = EMPTY_STRING;
		this.doctitle = EMPTY_STRING;
		this.header = EMPTY_STRING;
		this.footer = EMPTY_STRING;
		this.bottom = EMPTY_STRING;
		this.nodeprecated = false;
		this.nodeprecatedlist = false;
		this.notree = false;
		this.noindex = false;
		this.nonavbar = false;
		this.serialwarn = false;
		this.source = EMPTY_STRING;

		this.links = new ArrayList<>();
		this.doclet = null;
		this.styleSheetFile = null;
		this.additionalParam = EMPTY_STRING;

		this.packages = new ArrayList<>();
		this.srcFiles = new ArrayList<>();

		this.failonerror = false;

		this.dirFilter = new FileFilter() {
			@Override
			public boolean accept(File file) {
				return file.isDirectory() && !file.getName().equals("..") && !file.getName().equals("."); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};


		this.sourceFileFilter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(".java") || name.endsWith(".maxj"); //$NON-NLS-1$ //$NON-NLS-2$
			}
		};
	}

	@Override
	public void execute() throws BuildException{
		File fileList = null;
		try {
			System.out.println("##########################################"); //$NON-NLS-1$
			ArrayList<String> argumentsList = new ArrayList<>();
			processArguments(argumentsList);
			String[] arguments = new String[argumentsList.size() + 1];
			for(int i = 0; i <argumentsList.size(); i++){
				arguments[i] = argumentsList.get(i);
				System.out.println(arguments[i]);
			}

			String fileListName = "fileList.txt"; //$NON-NLS-1$
			fileList = new File(fileListName);

			try (FileWriter outFile = new FileWriter(fileList);
					PrintWriter out = new PrintWriter(outFile);) {
				for(int i = 0; i < this.srcFiles.size(); i++){
					out.println(this.srcFiles.get(i));
				}
			}
			arguments[argumentsList.size()] = "@" + fileListName; //$NON-NLS-1$
			String ls_str;
			Process ls_proc = Runtime.getRuntime().exec(arguments);
			try (InputStreamReader in = new InputStreamReader(ls_proc.getInputStream());
					BufferedReader ls_in = new BufferedReader(in)) {
				while ((ls_str = ls_in.readLine()) != null) {
					System.out.println(ls_str);
			    }
			}catch (IOException e) {/**/}
			try (InputStreamReader in = new InputStreamReader(ls_proc.getErrorStream());
					BufferedReader ls_in = new BufferedReader(in);) {
				while ((ls_str = ls_in.readLine()) != null) {
					System.out.println(ls_str);
			    }
			}catch (IOException e) {/**/}
			int exitStatus = 0;
			try{
				exitStatus = ls_proc.waitFor();
			}catch (InterruptedException e) {
				throw new BuildException(e.getMessage());
			}
			if(this.failonerror && exitStatus != 0){
				throw new BuildException(FAIL_MSG);
			}
		} catch (IOException e) {
			throw new BuildException(e.getMessage());
		}finally{
			if(fileList != null)
				fileList.delete();
		}
	}

	private void processArguments(ArrayList<String> arguments) {
		String jarPath = this.getClass().getResource("/" + this.getClass().getName().replace(".", "/") + ".class").getPath(); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		jarPath = jarPath.substring(jarPath.indexOf(":") + 1, jarPath.indexOf("!")); //$NON-NLS-1$ //$NON-NLS-2$
		jarPath = jarPath.substring(0, jarPath.lastIndexOf("/") + 1); //$NON-NLS-1$
		String executable = jarPath + "maxjdoc/bin/" + EXECUTABLE_NAME;		 //$NON-NLS-1$
		arguments.add(executable);
		if(!this.access.equals(EMPTY_STRING) && (this.access.equals(PUBLIC_ACCESS_OPTION) ||
												 this.access.equals(PROTECTED_ACCESS_OPTION) ||
												 this.access.equals(PACKAGE_ACCESS_OPTION) ||
												 this.access.equals(PRIVATE_ACCESS_OPTION))){
			arguments.add(OPTION_PREFIX + this.access);
		}else if(this.public_att){
			arguments.add(OPTION_PREFIX + PUBLIC_ACCESS_OPTION);
		}else if(this.protected_att){
			arguments.add(OPTION_PREFIX + PROTECTED_ACCESS_OPTION);
		}else if(this.package_att){
			arguments.add(OPTION_PREFIX + PACKAGE_ACCESS_OPTION);
		}else if(this.private_att){
			arguments.add(OPTION_PREFIX + PRIVATE_ACCESS_OPTION);
		}

		if(this.author){
			arguments.add(OPTION_PREFIX + AUTHOR_OPTION);
		}

		if(this.use){
			arguments.add(OPTION_PREFIX + USE_OPTION);
		}

		if(this.splitindex){
			arguments.add(OPTION_PREFIX + SPLITINDEX_OPTION);
		}

		if(this.version){
			arguments.add(OPTION_PREFIX + VERSION_OPTION);
		}

		if(this.nodeprecated){
			arguments.add(OPTION_PREFIX + NODEPRECATED_OPTION);
		}

		if(this.nodeprecatedlist){
			arguments.add(OPTION_PREFIX + NODEPRACATEDLIST_OPTION);
		}

		if(this.noindex){
			arguments.add(OPTION_PREFIX + NOINDEX_OPTION);
		}

		if(this.nonavbar){
			arguments.add(OPTION_PREFIX + NONAVBAR_OPTION);
		}

		if(this.nohelp){
			arguments.add(OPTION_PREFIX + NOHELP_OPTION);
		}

		if(this.notree){
			arguments.add(OPTION_PREFIX + NOTREE_OPTION);
		}

		if(this.verbose){
			arguments.add(OPTION_PREFIX + VERBOSE_OPTION);
		}

		if(this.serialwarn){
			arguments.add(OPTION_PREFIX + SERIALWARN_OPTION);
		}

		if(!this.destdir.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + DESTDIR_OPTION);
			arguments.add(this.destdir);
		}

		if(!this.extdirs.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + EXTDIRS_OPTION);
			arguments.add(this.extdirs);
		}

		if(!this.overview.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + OVERVIEW_OPTION);
			arguments.add(this.overview);
		}

		if(!this.windowtitle.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + WINDOWTITLE_OPTION);
			arguments.add(this.windowtitle);
		}

		if(!this.doctitle.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + DOCTITLE_OPTION);
			arguments.add(this.doctitle);
		}

		if(!this.header.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + HEADER_OPTION);
			arguments.add(this.header);
		}

		if(!this.footer.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + FOOTER_OPTION);
			arguments.add(this.footer);
		}

		if(!this.bottom.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + BOTTOM_OPTION);
			arguments.add(this.bottom);
		}

		if(this.doclet != null){
			if(!this.doclet.getName().equals("")){ //$NON-NLS-1$
				arguments.add(OPTION_PREFIX + DOCLET_OPTION);
				arguments.add(this.doclet.getName());
				if(this.doclet.getPath() != null){
					arguments.add(OPTION_PREFIX + DOCLETPATH_OPTION);
					arguments.add(this.doclet.getPath().toString());
				}
				ArrayList<DocletParam> params = this.doclet.getParams();
				for(int i = 0; i < params.size(); i++){
					arguments.add(params.get(i).getName());
					arguments.add(params.get(i).getValue());
				}
			}
		}


		if(!this.additionalParam.equals(EMPTY_STRING)){
//			arguments.add(OPTION_PREFIX + ADDITIONALPARAM_OPTION);
			String[] additionalsArguments = Commandline.translateCommandline(this.additionalParam);
			for(int i = 0; i < additionalsArguments.length; i++){
				arguments.add(additionalsArguments[i]);
			}
			//arguments.add(this.additionalParam);
		}

		if(this.styleSheetFile != null){
			arguments.add(OPTION_PREFIX + STYLESHEETFILE_OPTION);
			arguments.add(this.styleSheetFile.getAbsolutePath());
		}

		if(this.links.size() > 0){
			for(int i = 0; i < this.links.size();i++){
				arguments.add(OPTION_PREFIX + LINK_OPTION);
				arguments.add(this.links.get(i).getHref());
			}
		}

		if(this.classpath != null){
			arguments.add(OPTION_PREFIX + CLASSPATH_OPTION);
			String[] classPaths = this.classpath.list();
			StringBuffer classPath= new StringBuffer();
			for(int i = 0; i < classPaths.length; i++){
				classPath.append(classPaths[i]);
				if(i != classPaths.length - 1){
					classPath.append(PATH_SEPARATOR);
				}
			}
			arguments.add(classPath.toString());
		}

		if(!this.packagenames.equals(EMPTY_STRING) ){
			StringTokenizer tok = new StringTokenizer(this.packagenames, NAME_LIST_SEPARATOR);
			while (tok.hasMoreTokens()) {
				String path = tok.nextToken();
				if(!this.packages.contains(path))
					this.packages.add(path);
			}
        }

		if(!this.sourcefiles.equals(EMPTY_STRING) ){
			StringTokenizer tok = new StringTokenizer(this.sourcefiles, NAME_LIST_SEPARATOR);
			while (tok.hasMoreTokens()) {
				String path = tok.nextToken();
				if(!this.srcFiles.contains(path))
					this.srcFiles.add(path);
			}
        }

		if(!this.source.equals(EMPTY_STRING)){
			arguments.add(OPTION_PREFIX + SOURCE_OPTION);
			arguments.add(this.source);
		}

		System.out.println(this.sourcepath != null);
		if(this.sourcepath != null){
			parsePath();
			/*arguments.add(OPTION_PREFIX + SOURCEPATH_OPTION);
			arguments.add(this.sourcepath.toString());*/
		}

		if(this.packages.size() > 0)
			packagesToSrcFiles();
	}

	private void packagesToSrcFiles() {
		//Packages are regular folders
		//Read folder and add any file with .java or .maxj extension
		String[] srcPaths = this.sourcepath.list();
		for(int i = 0; i < srcPaths.length; i++){
			if(!srcPaths[i].endsWith("/")) //$NON-NLS-1$
				srcPaths[i] += "/"; //$NON-NLS-1$
			for(int j = 0; j < this.packages.size(); j++){
				File file = new File(srcPaths[i] + this.packages.get(j));
				if(file.exists()){
					packagesToSrcFiles(file);
				}
			}
		}
	}

	private void packagesToSrcFiles(File dir){
		File[] subDirs = dir.listFiles(this.dirFilter);
		for(int i = 0; i < subDirs.length; i++){
			packagesToSrcFiles(subDirs[i]);
		}
		String[] files = dir.list(this.sourceFileFilter);
		for(int i = 0; i < files.length; i++){
			String path = (dir.getPath().endsWith("/")?dir.getPath():(dir.getPath() + "/")) + files[i];//$NON-NLS-1$ //$NON-NLS-2$
			if(!this.srcFiles.contains(path))
				this.srcFiles.add(path);
		}
	}

	private void parsePath() {
		String[] pathElements = this.sourcepath.list();
		for(int i = 0; i < pathElements.length; i++){
			File pathElement = new File(pathElements[i]);
			if(pathElement.isDirectory()){
				File[] subDirs = pathElement.listFiles(this.dirFilter);
				for(int j = 0; j < subDirs.length; j++){
					if(!this.packages.contains(subDirs[j].getName()))
						this.packages.add(subDirs[j].getName());
				}
				String[] files = pathElement.list(this.sourceFileFilter);
				for(int j = 0; j < files.length; j++){
					String path = (pathElement.getPath().endsWith("/")?pathElement.getPath():(pathElement.getPath() + "/")) + files[j];//$NON-NLS-1$ //$NON-NLS-2$
					if(!this.srcFiles.contains(path))
						this.srcFiles.add(path);
				}
			}else{
				if(!this.srcFiles.contains(pathElement.getPath()))
					this.srcFiles.add(pathElement.getPath());
			}
		}
	}

	public void setAccess(String access) {
		this.access = access;
	}

	public void setAuthor(boolean author) {
		this.author = author;
	}

	public void setClasspath(Path classpath) {
		if(this.classpath == null)
			this.classpath = new Path(getProject());
		this.classpath.append(classpath);
	}

	public void setDestdir(String destdir) {
		this.destdir = destdir;
	}

	public void setNodeprecated(boolean nodeprecated) {
		this.nodeprecated = nodeprecated;
	}

	public void setNodeprecatedlist(boolean nodeprecatedlist) {
		this.nodeprecatedlist = nodeprecatedlist;
	}

	public void setNoindex(boolean noindex) {
		this.noindex = noindex;
	}

	public void setNonavbar(boolean nonavbar) {
		this.nonavbar = nonavbar;
	}

	public void setNotree(boolean notree) {
		this.notree = notree;
	}

	public void setPackagenames(String packagenames) {
		this.packagenames = packagenames;
	}

	public void setSource(String source) {
		this.source = source;
	}

    public void setSourcepath(Path srcDir) {
		if(this.sourcepath == null){
			this.sourcepath = new Path(getProject());
		}
		this.sourcepath.append(srcDir);
    }

    public void setSourcepathref(Reference r) {
		if(this.sourcepath == null)
			this.sourcepath = new Path(getProject());
		//this.sourcepath.createPath();
		this.sourcepath.setRefid(r);
    }

    public void setClasspathref(Reference r) {
    	if(this.classpath == null){
    		this.classpath = new Path(getProject());
    	}
    	//this.classpath.createPath();
    	this.classpath.setRefid(r);
    }

    public void setSplitindex(boolean splitindex) {
		this.splitindex = splitindex;
	}

	public void setUse(boolean use) {
		this.use = use;
	}

	public void setVersion(boolean version) {
		this.version = version;
	}

	public void setExtdirs(String extdirs) {
		this.extdirs = extdirs;
	}

	public void setSourcefiles(String sourcefiles) {
		this.sourcefiles = sourcefiles;
	}

	public void setOverview(String overview) {
		this.overview = overview;
	}

	public void setPublic(boolean public_att) {
		this.public_att = public_att;
	}

	public void setProtected(boolean protected_att) {
		this.protected_att = protected_att;
	}

	public void setPackage(boolean package_att) {
		this.package_att = package_att;
	}

	public void setPrivate(boolean private_att) {
		this.private_att = private_att;
	}

	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

	public void setWindowtitle(String windowtitle) {
		this.windowtitle = windowtitle;
	}

	public void setDoctitle(String doctitle) {
		this.doctitle = doctitle;
	}

	public void setHeader(String header) {
		this.header = header;
	}

	public void setFooter(String footer) {
		this.footer = footer;
	}

	public void setBottom(String bottom) {
		this.bottom = bottom;
	}

	public void setNohelp(boolean nohelp) {
		this.nohelp = nohelp;
	}

	public void setSerialwarn(boolean serialwarn) {
		this.serialwarn = serialwarn;
	}

	public void setOld(boolean serialwarn) {
		/*doesn't have any effect*/
	}

    public void setLink(String src) {
        createLink().setHref(src);
    }

    public LinkArgument createLink() {
        LinkArgument la = new LinkArgument();
        this.links.add(la);
        return la;
    }

	public void setDoclet(String doclet){
		if(this.doclet == null)
			this.doclet = new DocletTask();
		this.doclet.setName(doclet);
	}

	public void setDocletPath(Path path){
		if(this.doclet == null)
			this.doclet = new DocletTask();
		this.doclet.setPath(path);
	}

	public DocletTask createDoclet(){
		if(this.doclet == null){
			this.doclet = new DocletTask();
		}
		return this.doclet;
	}

	public void setAdditionalParam(String param){
		this.additionalParam = param;
	}

	public void setStylesheetfile(File f) {
        this.styleSheetFile = f;
    }

    public void setFailonerror(boolean fail) {
    	this.failonerror = fail;
    }

}
