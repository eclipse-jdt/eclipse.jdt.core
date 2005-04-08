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

package org.eclipse.jdt.core.tests.performance;

import java.io.*;
import java.net.URL;
import java.text.DateFormat;
import java.util.*;

import junit.framework.*;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.tests.builder.TestingEnvironment;
import org.eclipse.jdt.core.tests.junit.extension.TestCase;
import org.eclipse.jdt.core.tests.performance.util.JdtCorePerformanceMeter;
import org.eclipse.jdt.core.tests.util.Util;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.search.processing.IJob;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.Performance;



public abstract class FullSourceWorkspaceTests extends TestCase {

	final static boolean DEBUG = "true".equals(System.getProperty("debug"));
	final static boolean INCLUDE_ALL = "true".equals(System.getProperty("includeAll"));
	final static Hashtable INITIAL_OPTIONS = JavaCore.getOptions();

	// Workspace variables
	protected static TestingEnvironment ENV = null;
	protected static IJavaProject[] ALL_PROJECTS;
	
	// Index variables
	protected static IndexManager INDEX_MANAGER = JavaModelManager.getJavaModelManager().getIndexManager();
	
	// Tests infos
	protected static int ALL_TESTS_COUNT = 0;
	protected static List TESTS_NAME_LIST;

	// Tests counters
	protected final static int MEASURES_COUNT = 10;
	
	/**
	 * Variable used for log files.
	 * Log files are used in conjonction with {@link JdtCorePerformanceMeter} class.
	 * These are file where CPU times of each test of subclasses are stored.
	 * This specific way to run performance tests is activated by specifying
	 * following options:
	 *		-DPerformanceMeterFactory=org.eclipse.jdt.core.tests.performance:org.eclipse.jdt.core.tests.performance.util.JdtCorePerformanceMeterFactory
	 *		-DlogDir=directory where you want to write log files (for example d:/usr/OTI/tests/perfs/stats)
	 * 
	 */
	// Store directory where to put files
	protected static File LOG_DIR;
	// Types of statistic whcih can be stored.
	protected final static String[] LOG_TYPES = { "count", "average", "sum", "stddev" };
	// Main version which is logged
	// TODO (frederic) see whether this could be computed automatically
	private final static String LOG_VERSION = "_v31_";
	String scenario;

	/**
	 * @param name
	 */
	public FullSourceWorkspaceTests(String name) {
		super(name);
	}

//	static {
//		TESTS_NAMES = new String[] { "testPerfFullBuildNoDocCommentSupport" };
//	}
	public static Test suite() {
		return buildSuite(FullSourceWorkspaceTests.class);
	}

	protected static Test buildSuite(Class testClass) {

		// Create tests
		TestSuite suite = new TestSuite(testClass.getName());
		List tests = buildTestsList(testClass);
		int size = tests.size();
		TESTS_NAME_LIST = new ArrayList(size);
		for (int i=0; i<size; i++) {
			FullSourceWorkspaceTests test = (FullSourceWorkspaceTests)tests.get(i);
			suite.addTest(test);
			TESTS_NAME_LIST.add(test.getName());
		}
		ALL_TESTS_COUNT += suite.testCount();

		// Init log dir
		initLogDir();
		
		// Return created tests
		return suite;
	}

	/**
	 * Initialize log directory.
	 * 
	 * Directory where log files must be put is specified by System property <code>logDir</code>.
	 * For example, if user want to store log files in d:/usr/OTI/tests/perfs/stats,
	 * then he has to specify: -DlogDir=d:/usr/OTI/tests/perfs/stats in VM Arguments of his
	 * performance test launch configuration.
	 * 
	 * CAUTION: Directory *must* exist before running test otherwise it won't be created
	 * and CPU times won't be logged. This was intentional to avoid unexpected log files creation
	 * (especially during bightly/integration builds).
	 */
	protected static void initLogDir() {
		String logDir = System.getProperty("logDir");
		if (logDir != null) {
			File dir = new File(logDir);
			if (dir.exists() && dir.isDirectory()) {
				LOG_DIR = dir;
			} else {
				System.err.println(logDir+" is not a valid directory or does not exist. Log files will NOT be written!");
			}
		}
	}

	/**
	 * Create print streams (one for each type of statistic).
	 * Log file names have all same prefix (see {@link getLogFilePrefix(String)}),
	 * include type of statistic stored in it and always have extension ".log".
	 * 
	 * If log file does not exist, then add column headers at the beginning of the file.
	 * 
	 * This method does nothing if log files directory has not been initialized
	 * (which should be the case most of times and especially while running nightly/integration build performance tests).
	 */
	protected static void createPrintStream(String className, PrintStream[] logStreams, int count, String prefix) {
		if (LOG_DIR != null) {
			String testTypeName = className.substring(className.indexOf("FullSourceWorkspace")+"FullSourceWorkspace".length(), className.lastIndexOf("Test"));
			for (int i=0, ln=LOG_TYPES.length; i<ln; i++) {
				File logFile = new File(LOG_DIR, "Perfs"+testTypeName+LOG_VERSION+LOG_TYPES[i]+".log");
				try {
					boolean fileExist = logFile.exists();
					PrintStream logStream = null;
					// Open stream
					if (LOG_TYPES[i].equals("count")) {
						logStream = logStreams[0] = new PrintStream(new FileOutputStream(logFile, true));
					} else if (LOG_TYPES[i].equals("sum")) {
						logStream = logStreams[1] = new PrintStream(new FileOutputStream(logFile, true));
					} else if (LOG_TYPES[i].equals("average")) {
						logStream = logStreams[2] = new PrintStream(new FileOutputStream(logFile, true));
					} else if (LOG_TYPES[i].equals("stddev")) {
						logStream = logStreams[3] = new PrintStream(new FileOutputStream(logFile, true));
					}
					if (!fileExist && logStream != null) {
						logStream.print("Date  \tTime  \t");
						for (int j=0; j<count; j++) {
							String testName = ((String) TESTS_NAME_LIST.get(j)).substring(8+(prefix==null?0:prefix.length())); // 8="testPerf".length()
							logStream.print(testName+'\t');
						}
						logStream.println("Comment");
						
					}
					// Log date and time
					Date date = new Date(System.currentTimeMillis());
					logStream.print(DateFormat.getDateInstance(3).format(date)+'\t');
					logStream.print(DateFormat.getTimeInstance(3).format(date)+'\t');
					System.out.println("Log file "+logFile.getPath()+" opened.");
				} catch (FileNotFoundException e) {
					// no log available for this statistic
				}
			}
		}
	}

	/**
	 * Log test performance result and close stream if it was last one.
	 */
	protected void logPerfResult(PrintStream[] logStreams, int count) {

		// Log perf result
		boolean haveTimes  = JdtCorePerformanceMeter.CPU_TIMES != null;
		if (haveTimes) {
			JdtCorePerformanceMeter.Statistics stats = (JdtCorePerformanceMeter.Statistics) JdtCorePerformanceMeter.CPU_TIMES.get(this.scenario);
			if (stats != null) {
				if (logStreams[0] != null) {
					logStreams[0].print(""+stats.count+"\t");
					if (DEBUG) System.out.println("	- count stored in log file.\n");
				}
				if (logStreams[1] != null) {
					logStreams[1].print(""+stats.sum+"\t");
					if (DEBUG) System.out.println("	- sum stored in log file.\n");
				}
				if (logStreams[2] != null) {
					logStreams[2].print(""+stats.average+"\t");
					if (DEBUG) System.out.println("	- average stored in log file.\n");
				}
				if (logStreams[3] != null) {
					logStreams[3].print(""+stats.stddev+"\t");
					if (DEBUG) System.out.println("	- stddev stored in log file.\n");
				}
			} else {
				System.err.println("We should have stored Cpu Time for "+this.scenario);
			}
		}

		// Close log
		if (count == 0) {
			for (int i=0, ln=logStreams.length; i<ln; i++) {
				if (logStreams[i] != null) {
					if (haveTimes) logStreams[i].println();
					logStreams[i].close();
				}
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		this.scenario = Performance.getDefault().getDefaultScenarioId(this);
		if (ENV == null) {
			ENV = new TestingEnvironment();
			ENV.openEmptyWorkspace();
			setUpFullSourceWorkspace();
		}
		// Perform gc several times to be sure that it won't take time while executing current test
		int iterations = 0;
		long delta;
		do {
			long free = Runtime.getRuntime().freeMemory();
			System.gc();
			delta = Runtime.getRuntime().freeMemory() -free;
			if (DEBUG) System.out.println("Loop gc "+ ++iterations + " (free="+free+", delta="+delta+")");
			Thread.sleep(500);
		} while (iterations<10 && delta>100);
	}
	/**
	 * @deprecated Use {@link #tagAsGlobalSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		tagAsGlobalSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	public void tagAsGlobalSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsGlobalSummary(shortName, dimension);
	}
	/**
	 * @deprecated Use {@link #tagAsGlobalSummary(String,Dimension[],boolean)} instead
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		tagAsGlobalSummary(shortName, dimensions, false); // do NOT put in fingerprint
	}
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsGlobalSummary(shortName, dimensions);
	}
	/**
	 * @deprecated Use {@link #tagAsSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		tagAsSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	public void tagAsSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimension);
	}
	/**
	 * @deprecated Use {@link #tagAsSummary(String,Dimension[],boolean)} instead
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		tagAsSummary(shortName, dimensions, false); // do NOT put in fingerprint
	}
	public void tagAsSummary(String shortName, Dimension[] dimensions, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimensions);
	}
	/* (non-Javadoc)
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ALL_TESTS_COUNT--;
		if (ALL_TESTS_COUNT == 0) {
			ENV.resetWorkspace();
			JavaCore.setOptions(INITIAL_OPTIONS);
		}
		super.tearDown();
	}

	/*
	 * Returns the OS path to the directory that contains this plugin.
	 */
	private static String getPluginDirectoryPath() {
		try {
			URL platformURL = Platform.getBundle("org.eclipse.jdt.core.tests.performance").getEntry("/");
			return new File(Platform.asLocalURL(platformURL).getFile()).getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/*
	 * Set up full source workpsace from zip file.
	 */
	private static void setUpFullSourceWorkspace() throws IOException, CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		if (workspaceRoot.getProjects().length == 0) {
			String fullSourceZipPath = getPluginDirectoryPath() + File.separator + "full-source-R3_0.zip";
			final String targetWorkspacePath = workspaceRoot.getLocation().toFile().getCanonicalPath();

			if (DEBUG) System.out.print("Unzipping "+fullSourceZipPath+"...");
			Util.unzip(fullSourceZipPath, targetWorkspacePath);
		
			workspace.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor monitor) throws CoreException {
					File targetWorkspaceDir = new File(targetWorkspacePath);
					String[] projectNames = targetWorkspaceDir.list();
					for (int i = 0, length = projectNames.length; i < length; i++) {
						String projectName = projectNames[i];
						if (".metadata".equals(projectName)) continue;
						IProject project = workspaceRoot.getProject(projectName);
						project.create(monitor);
						project.open(monitor);
					}
				}
			}, null);
			if (DEBUG) System.out.println("done!");
		}
		String jdkLib = Util.getJavaClassLibs()[0];
		JavaCore.setClasspathVariable("JRE_LIB", new Path(jdkLib), null);
		
		// workaround bug 73253 Project references not set on project open 
		if (DEBUG) System.out.print("Set projects classpaths...");
		ALL_PROJECTS = JavaCore.create(workspaceRoot).getJavaProjects();
		int length = ALL_PROJECTS.length;
		for (int i = 0; i < length; i++) {
			ALL_PROJECTS[i].setRawClasspath(ALL_PROJECTS[i].getRawClasspath(), null);
		}
		if (DEBUG) System.out.println("done!");
	}

	/*
	 * Full Build using batch compiler
	 */
	protected File buildUsingBatchCompiler(String options) throws IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID).getLocation().toFile().getCanonicalPath();
		final String sources = targetWorkspacePath + File.separator + "compiler";
		final String bins = targetWorkspacePath + File.separator + "bin"; //$NON-NLS-1$
		final String logs = targetWorkspacePath + File.separator + "log.txt"; //$NON-NLS-1$

		// Warm up
		String cmdLine = sources + " -1.4 -g -preserveAllLocals "+(options==null?"":options)+" -d " + bins + " -log " + logs; //$NON-NLS-1$ //$NON-NLS-2$
		for (int i=0; i<2; i++) {
			Main.compile(cmdLine, new PrintWriter(new StringWriter()), new PrintWriter(new StringWriter()));
		}

		// Measures
		int max = MEASURES_COUNT * 2;
		for (int i = 0; i < max; i++) {
			startMeasuring();
			Main.compile(cmdLine, new PrintWriter(new StringWriter()), new PrintWriter(new StringWriter()));
			stopMeasuring();
			cleanupDirectory(new File(bins));
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		return new File(logs);
	}

	/**
	 * Delete a directory from file system.
	 * @param directory
	 */
	protected void cleanupDirectory(File directory) {
		if (!directory.isDirectory() || !directory.exists()) {
			return;
		}
		String[] fileNames = directory.list();
		for (int i = 0; i < fileNames.length; i++) {
			File file = new File(directory, fileNames[i]);
			if (file.isDirectory()) {
				cleanupDirectory(file);
			} else {
				if (!file.delete())
					System.out.println("Could not delete file " + file.getPath()); //$NON-NLS-1$
			}
		}
		if (!directory.delete())
			System.out.println("Could not delete directory " + directory.getPath()); //$NON-NLS-1$
	}

	private void collectAllFiles(File root, ArrayList collector, FileFilter fileFilter) {
		File[] files = root.listFiles(fileFilter);
		for (int i = 0; i < files.length; i++) {
			final File currentFile = files[i];
			if (currentFile.isDirectory()) {
				collectAllFiles(currentFile, collector, fileFilter);
			} else {
				collector.add(currentFile);
			}
		}
	}

	protected File[] getAllFiles(File root, FileFilter fileFilter) {
		ArrayList files = new ArrayList();
		if (root.isDirectory()) {
			collectAllFiles(root, files, fileFilter);
			File[] result = new File[files.size()];
			files.toArray(result);
			return result;
		} else {
			return null;
		}
	}

	/**
	 * Returns project correspoding to given name or null if none is found.
	 * @param projectName
	 * @return IJavaProject
	 */
	protected IJavaProject getProject(String projectName) {
		for (int i=0, length = ALL_PROJECTS.length; i<length; i++) {
			if (ALL_PROJECTS[i].getElementName().equals(projectName))
				return ALL_PROJECTS[i];
		}
		return null;
	}

	/**
	 * Returns compilation unit with given name in given project and package.
	 * @param projectName
	 * @param packageName
	 * @param unitName
	 * @return org.eclipse.jdt.core.ICompilationUnit
	 */
	protected ICompilationUnit getCompilationUnit(String projectName, String packageName, String unitName) throws JavaModelException {
		IJavaProject javaProject = getProject(projectName);
		if (javaProject == null) return null;
		IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
		int length = fragmentRoots.length;
		for (int i=0; i<length; i++) {
			if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
			IJavaElement[] packages= fragmentRoots[i].getChildren();
			for (int k= 0; k < packages.length; k++) {
				IPackageFragment pack = (IPackageFragment) packages[k];
				if (pack.getElementName().equals(packageName)) {
					ICompilationUnit[] units = pack.getCompilationUnits();
					for (int u=0; u<units.length; u++) {
						if (units[u].getElementName().equals(unitName))
							return units[u];
					}
				}
			}
		}
		return null;
	}

	/**
	 * Returns all compilation units of a given project.
	 * @param javaProject Project to collect units
	 * @return List of org.eclipse.jdt.core.ICompilationUnit
	 */
	protected List getProjectCompilationUnits(IJavaProject javaProject) throws JavaModelException {
		IPackageFragmentRoot[] fragmentRoots = javaProject.getPackageFragmentRoots();
		int length = fragmentRoots.length;
		List allUnits = new ArrayList();
		for (int i=0; i<length; i++) {
			if (fragmentRoots[i] instanceof JarPackageFragmentRoot) continue;
			IJavaElement[] packages= fragmentRoots[i].getChildren();
			for (int k= 0; k < packages.length; k++) {
				IPackageFragment pack = (IPackageFragment) packages[k];
				ICompilationUnit[] units = pack.getCompilationUnits();
				for (int u=0; u<units.length; u++) {
					allUnits.add(units[u]);
				}
			}
		}
		return allUnits;
	}

	/**
	 * Split a list of compilation units in several arrays.
	 * @param units List of org.eclipse.jdt.core.ICompilationUnit
	 * @param splitSize Size of the arrays
	 * @return List of ICompilationUnit[]
	 */
	protected List splitListInSmallArrays(List units, int splitSize) throws JavaModelException {
		int size = units.size();
		if (size == 0) return Collections.EMPTY_LIST;
		int length = size / splitSize;
		int remind = size%splitSize;
		List splitted = new ArrayList(remind==0?length:length+1);
		if (length == 0) {
			ICompilationUnit[] sublist = new ICompilationUnit[size];
			units.toArray(sublist);
			splitted.add(sublist);
			return splitted;
		}
		int ptr = 0;
		for (int i= 0; i<length; i++){
			ICompilationUnit[] sublist = new ICompilationUnit[splitSize];
			units.subList(ptr, ptr+splitSize).toArray(sublist);
			splitted.add(sublist);
			ptr += splitSize;
		}
		if (remind > 0) {
			if (remind< 10) {
				ICompilationUnit[] lastList = (ICompilationUnit[]) splitted.remove(length-1);
				System.arraycopy(lastList, 0, lastList = new ICompilationUnit[splitSize+remind], 0, splitSize);
				for (int i=ptr, j=splitSize; i<size; i++, j++) {
					lastList[j] = (ICompilationUnit) units.get(i);
				}
				splitted.add(lastList);
			} else {
				ICompilationUnit[] sublist = new ICompilationUnit[remind];
				units.subList(ptr, size).toArray(sublist);
				splitted.add(sublist);
			}
		}
		return splitted;
	}

	/**
	 * Start a build on workspace using given options.
	 * @param options
	 * @throws IOException
	 * @throws CoreException
	 */
	protected int startBuild(Hashtable options) throws IOException, CoreException {
		if (DEBUG) System.out.print("\tstart build...");
		JavaCore.setOptions(options);
		startMeasuring();
		ENV.fullBuild();
		stopMeasuring();
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		int warnings = 0;
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			switch (((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue()) {
				case IMarker.SEVERITY_ERROR:
					assertTrue("Unexpected marker: " + marker.getAttribute(IMarker.MESSAGE), false);
					break;
				case IMarker.SEVERITY_WARNING:
					warnings++;
					break;
			}
		}
		if (DEBUG) System.out.println("done");
		{
			commitMeasurements();
			assertPerformance();
		}
		return warnings;
	}

	// Wait for indexing end
	protected void waitUntilIndexesReady() {
		/**
		 * Simple Job which does nothing
		 */
		class	 DoNothing implements IJob {
			/**
			 * Answer true if the job belongs to a given family (tag)
			 */
			public boolean belongsTo(String jobFamily) {
				return true;
			}
			/**
			 * Asks this job to cancel its execution. The cancellation
			 * can take an undertermined amount of time.
			 */
			public void cancel() {
				// nothing to cancel
			}
			/**
			 * Ensures that this job is ready to run.
			 */
			public void ensureReadyToRun() {
				// always ready to do nothing
			}
			/**
			 * Execute the current job, answer whether it was successful.
			 */
			public boolean execute(IProgressMonitor progress) {
				// always succeed to do nothing
				return true;
			}
		}
		
		// Run simple job which does nothing but wait for indexing end
		INDEX_MANAGER.performConcurrentJob(new DoNothing(), IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH, null);
		assertEquals("Index manager should not have remaining jobs!", 0, INDEX_MANAGER.awaitingJobsCount()); //$NON-NLS-1$
	}

	/*
	 * Create hashtable of none or all warning options.
	 */
	protected Hashtable warningOptions(boolean all) {

		// Values
		Hashtable optionsMap = new Hashtable(30);
		String generate = all ? CompilerOptions.GENERATE : CompilerOptions.DO_NOT_GENERATE;
		String warning = all ? CompilerOptions.WARNING : CompilerOptions.IGNORE;
		String enabled = all ? CompilerOptions.ENABLED : CompilerOptions.DISABLED;
		String preserve = all ? CompilerOptions.OPTIMIZE_OUT : CompilerOptions.PRESERVE;
		
		// Set options values
		optionsMap.put(CompilerOptions.OPTION_LocalVariableAttribute, generate); 
		optionsMap.put(CompilerOptions.OPTION_LineNumberAttribute, generate);
		optionsMap.put(CompilerOptions.OPTION_SourceFileAttribute, generate);
		optionsMap.put(CompilerOptions.OPTION_PreserveUnusedLocal, preserve);
		optionsMap.put(CompilerOptions.OPTION_DocCommentSupport, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportMethodWithConstructorName, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportDeprecation, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportHiddenCatchBlock, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedLocal, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameter, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportSyntheticAccessEmulation, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportNoEffectAssignment, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportNonExternalizedStringLiteral, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportNoImplicitStringConversion, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportNonStaticAccessToStatic, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportIndirectStaticAccess, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportIncompatibleNonInheritedInterfaceMethod, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedPrivateMember, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportLocalVariableHiding, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportFieldHiding, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportEmptyStatement, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportAssertIdentifier, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUndocumentedEmptyBlock, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryTypeCheck, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnnecessaryElse, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadoc, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportInvalidJavadocTags, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocTags, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportMissingJavadocComments, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedDeclaredThrownException, warning); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnqualifiedFieldAccess, warning); 
		optionsMap.put(CompilerOptions.OPTION_TaskTags, all ? JavaCore.DEFAULT_TASK_TAGS : "");
		optionsMap.put(CompilerOptions.OPTION_TaskPriorities, all ? JavaCore.DEFAULT_TASK_PRIORITIES : "");
		optionsMap.put(CompilerOptions.OPTION_TaskCaseSensitive, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenImplementingAbstract, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportUnusedParameterWhenOverridingConcrete, enabled); 
		optionsMap.put(CompilerOptions.OPTION_ReportSpecialParameterHidingField, enabled); 
		optionsMap.put(CompilerOptions.OPTION_InlineJsr, enabled); 
		
		// Return created options map
		return optionsMap;
	}
}
