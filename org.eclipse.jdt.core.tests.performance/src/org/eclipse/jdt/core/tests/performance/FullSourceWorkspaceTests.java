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
import java.text.NumberFormat;
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

	// Final static variables
	final static boolean DEBUG = "true".equals(System.getProperty("debug"));
	
	// Garbage collect constants
	final static int MAX_GC = 10; // Max gc iterations
	final static int TIME_GC = 500; // Sleep to wait gc to run (in ms)
	final static int DELTA_GC = 1000; // Threshold to remaining free memory

	// Workspace variables
	protected static TestingEnvironment ENV = null;
	protected static IJavaProject[] ALL_PROJECTS;
	
	// Index variables
	protected static IndexManager INDEX_MANAGER = JavaModelManager.getJavaModelManager().getIndexManager();
	
	// Tests infos
	protected static int ALL_TESTS_COUNT = 0;
	protected static int TEST_POSITION = 0;
	protected static List TESTS_NAME_LIST;

	// Tests counters
	protected final static int MEASURES_COUNT = 10;

	// Scenario information
	String scenarioReadableName, scenarioShortName;
	StringBuffer scenarioComment;
	static Map SCENARII_COMMENT = new HashMap();
	
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
	private final static File INVALID_DIR = new File("Invalid");
	protected static File LOG_DIR;
	// Types of statistic which can be stored.
	protected final static String[] LOG_TYPES = { "cpu", "elapsed" };
	// Main version which is logged
	protected final static String LOG_VERSION;
	static {
		String version = Main.bind("compiler.version");
		LOG_VERSION = "v_"+version.substring(version.indexOf('.')+1, version.indexOf(','));
	};
	// Patch version currently applied: may be null!
	protected final static String PATCH_ID = System.getProperty("patch");
	protected static String RUN_ID;

	/**
	 * Initialize log directory.
	 * 
	 * Directory where log files must be put is specified by System property <code>logDir</code>.
	 * For example, if user want to store log files in d:/usr/OTI/tests/perfs/stats,
	 * then he has to specify: -DlogDir=d:/usr/OTI/tests/perfs/stats in VM Arguments of his
	 * performance test launch configuration.
	 * 
	 * CAUTION: Parent directory at least <b>must</b> exist before running test otherwise
	 * it won't be created and times won't be logged.
	 * This was intentional to avoid unexpected log files creation (especially during nightly/integration builds).
	 */
	protected static void initLogDir() {
		String logDir = System.getProperty("logDir");
		File dir = null;
		if (logDir != null) {
			// Verify that parent log dir is valid if exist
			dir = new File(logDir);
			if (dir.exists()) {
				if (!dir.isDirectory()) {
					System.err.println(logDir+" is not a valid directory. Log files will NOT be written!");
					dir = INVALID_DIR;
				}
			} else {
				// Create parent dir if necessary
				int n=0;
				boolean created = false;
				while (!created && n<3) {
					created = dir.mkdir();
					if (!created) {
						dir = dir.getParentFile();
					}
					n++;
				}
				if (!created) {
					System.err.println("Cannot create "+logDir+". Log files will NOT be written!");
					dir = INVALID_DIR;
				}
			}
			
			// Create Log dir
			String[] subdirs = new String[] {
				(PATCH_ID == null) ? LOG_VERSION : PATCH_ID,
				RUN_ID
			};
			for (int i=0; i<subdirs.length; i++) {
				dir = new File(dir, subdirs[i]);
				if (dir.exists()) {
					if (!dir.isDirectory()) {
						System.err.println(dir.getPath()+" is not a valid directory. Log files will NOT be written!");
						dir= INVALID_DIR;
						break;
					}
				} else if (!dir.mkdir()) {
					System.err.println("Cannot create "+logDir+". Log files will NOT be written!");
					dir = INVALID_DIR;
					break;
				}
			}
		}
		LOG_DIR = dir;
	}

	// Standard deviation threshold. Statistic should not be take into account when it's reached
	protected final static double STDDEV_THRESHOLD = 0.1; // default is 10%
	
	// JavaCore options management
	protected boolean resetOptions = false;

	/**
	 * @param name
	 */
	public FullSourceWorkspaceTests(String name) {
		super(name);
	}

	protected static String suiteTypeShortName(Class testClass) {
		String className = testClass.getName();
		int startIndex = className.indexOf("FullSourceWorkspace");
		int endIndex = className.lastIndexOf("Test");
		if (startIndex < 0) return null;
		startIndex += "FullSourceWorkspace".length();
		return className.substring(startIndex, endIndex);
	}

	/**
	 * Create test suite for a given TestCase class.
	 * 
	 * Use this method for all JDT/Core performance test using full source workspace.
	 * All test count is computed to know when tests are about to be finished.
	 *
	 * @param testClass TestCase test class
	 * @return test suite
	 */
	static Test buildSuite(Class testClass) {

		// Create tests
		String className = testClass.getName();
		TestSuite suite = new TestSuite(className);
		List tests = buildTestsList(testClass);
		int size = tests.size();
		TESTS_NAME_LIST = new ArrayList(size);
		for (int i=0; i<size; i++) {
			FullSourceWorkspaceTests test = (FullSourceWorkspaceTests)tests.get(i);
			suite.addTest(test);
			TESTS_NAME_LIST.add(test.getName());
		}
		ALL_TESTS_COUNT += suite.testCount();
		
		// Init log dir if necessary
		if (LOG_DIR == null) {
			if (RUN_ID == null) {
				RUN_ID = suiteTypeShortName(testClass);
			}
			initLogDir();
		}
		
		// Return created tests
		return suite;
	}

	/**
	 * Create print streams (one for each type of statistic).
	 * Log file names have all same prefix based on test class name,
	 * include type of statistic stored in it and always have extension ".log".
	 * 
	 * If log file does not exist, then add column headers at the beginning of the file.
	 * 
	 * This method does nothing if log files directory has not been initialized
	 * (which should be the case most of times and especially while running nightly/integration build performance tests).
	 */
	static void createPrintStream(Class testClass, PrintStream[] logStreams, int count, String prefix) {
		if (LOG_DIR != null) {
			for (int i=0, ln=LOG_TYPES.length; i<ln; i++) {
				String suiteTypeName = suiteTypeShortName(testClass);
				File logFile = new File(LOG_DIR, suiteTypeName+'_'+LOG_TYPES[i]+".log");
				try {
					boolean fileExist = logFile.exists();
					logStreams[i] = new PrintStream(new FileOutputStream(logFile, true));
					if (!fileExist && logStreams[i] != null) {
						logStreams[i].print("Date  \tTime  \t");
						for (int j=0; j<count; j++) {
							String testName = ((String) TESTS_NAME_LIST.get(j)).substring(4+(prefix==null?0:prefix.length())); // 4="test".length()
							logStreams[i].print(testName+'\t');
						}
						logStreams[i].println("Comment");
						
					}
					// Log date and time
					Date date = new Date(System.currentTimeMillis());
					logStreams[i].print(DateFormat.getDateInstance(3).format(date)+'\t');
					logStreams[i].print(DateFormat.getTimeInstance(3).format(date)+'\t');
					System.out.println("Log file "+logFile.getPath()+" opened.");
				} catch (FileNotFoundException e) {
					// no log available for this statistic
				}
			}
		}
	}

	/**
	 * Perform gc several times to be sure that it won't take time while executing current test.
	 */
	protected void runGc() {
		int iterations = 0;
		long delta=0, free=0;
		for (int i=0; i<MAX_GC; i++) {
			free = Runtime.getRuntime().freeMemory();
			System.gc();
			delta = Runtime.getRuntime().freeMemory() - free;
			if (DEBUG) System.out.println("Loop gc "+ ++iterations + " (free="+free+", delta="+delta+")");
			try {
				Thread.sleep(TIME_GC);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
		if (iterations == MAX_GC && delta > DELTA_GC) {
			// perhaps gc was not well executed
			System.out.println("WARNING: "+this.scenarioShortName+" still get "+delta+" unfreeable memory (free="+free+",total="+Runtime.getRuntime().totalMemory()+") after "+MAX_GC+" gc...");
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	/**
	 * Log test performance result and close stream if it was last one.
	 */
	protected void logPerfResult(PrintStream[] logStreams, int count) {

		// Perfs comment buffers
		String[] comments = new String[2];

		// Log perf result
		boolean haveTimes  = JdtCorePerformanceMeter.CPU_TIMES != null && JdtCorePerformanceMeter.ELAPSED_TIMES != null;
		if (haveTimes) {
			NumberFormat pFormat = NumberFormat.getPercentInstance();
			pFormat.setMaximumFractionDigits(1);
			NumberFormat dFormat = NumberFormat.getNumberInstance();
			dFormat.setMaximumFractionDigits(0);
			String stddevThresholdStr = dFormat.format(STDDEV_THRESHOLD*100);
			NumberFormat dFormat2 = NumberFormat.getNumberInstance();
			dFormat2.setMaximumFractionDigits(2);
			try {
				// Store CPU Time
				JdtCorePerformanceMeter.Statistics cpuStats = (JdtCorePerformanceMeter.Statistics) JdtCorePerformanceMeter.CPU_TIMES.get(this.scenarioReadableName);
				if (cpuStats != null) {
					double percent = cpuStats.stddev/cpuStats.average;
					if (percent > STDDEV_THRESHOLD) {
						//if (logStreams[0] != null) logStreams[0].print("'"); // disable over threshold result for xls table
						System.out.println("	WARNING: CPU time standard deviation is over "+stddevThresholdStr+"%: "+dFormat2.format(cpuStats.stddev)+"/"+cpuStats.average+"="+ pFormat.format(percent));
						comments[0] = "stddev=" + pFormat.format(percent);
					}
					if (logStreams[0] != null) {
						logStreams[0].print(""+cpuStats.sum+"\t");
					}
				} else {
					Thread.sleep(1000);
					System.err.println(this.scenarioShortName+": we should have stored CPU time!");
					Thread.sleep(1000);
				}
				// Store Elapsed time
				JdtCorePerformanceMeter.Statistics elapsedStats = (JdtCorePerformanceMeter.Statistics) JdtCorePerformanceMeter.ELAPSED_TIMES.get(this.scenarioReadableName);
				if (elapsedStats != null) {
					double percent = elapsedStats.stddev/elapsedStats.average;
					if (percent > STDDEV_THRESHOLD) {
						//if (logStreams[1] != null) logStreams[1].print("'"); // disable over threshold result for xls table
						System.out.println("	WARNING: Elapsed time standard deviation is over "+stddevThresholdStr+"%: "+dFormat.format(elapsedStats.stddev)+"/"+elapsedStats.average+"="+ pFormat.format(percent));
						comments[1] = "stddev=" + pFormat.format(percent);
					}
					if (logStreams[1] != null) {
						logStreams[1].print(""+elapsedStats.sum+"\t");
					}
				} else {
					Thread.sleep(1000);
					System.err.println(this.scenarioShortName+": we should have stored Elapsed time");
					Thread.sleep(1000);
				}
			} catch (InterruptedException e) {
				// do nothing
			}
		}

		// Update comment buffers
		StringBuffer[] scenarioComments = (StringBuffer[]) SCENARII_COMMENT.get(getClass());
		if (scenarioComments == null) {
			scenarioComments = new StringBuffer[LOG_TYPES.length];
			SCENARII_COMMENT.put(getClass(), scenarioComments);
		}
		for (int i=0, ln=LOG_TYPES.length; i<ln; i++) {
			if (this.scenarioComment != null || comments[i] != null) {
				if (scenarioComments[i] == null) {
					scenarioComments[i] = new StringBuffer();
				} else {
					scenarioComments[i].append(' ');
				}
				if (this.scenarioComment == null) {
					scenarioComments[i].append("["+TEST_POSITION+"]");
				} else {
					scenarioComments[i].append(this.scenarioComment);
				}
				if (comments[i] != null) {
					if (this.scenarioComment != null) scenarioComments[i].append(',');
					scenarioComments[i].append(comments[i]);
				}
			}
		}

		// Close log
		if (count == 0) {
			for (int i=0, ln=logStreams.length; i<ln; i++) {
				if (logStreams[i] != null) {
					if (haveTimes) {
						if (scenarioComments[i] != null) {
							logStreams[i].print(scenarioComments[i].toString());
						}	
						logStreams[i].println();
					}
					logStreams[i].close();
				}
			}
			TEST_POSITION = 0;
		}
	}

	/**
	 * Override super implementation to:
	 * <ul>
	 *		<li>store scenario names and comment (one scenario per test)</li>
	 *		<li>init workspace if first test run</li>
	 *		<li>increment test position</li>
	 *	</ul>
	 * @see org.eclipse.test.performance.PerformanceTestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();

		// Store scenario readable name
		String scenario = Performance.getDefault().getDefaultScenarioId(this);
		this.scenarioReadableName = scenario.substring(scenario.lastIndexOf('.')+1, scenario.length()-2);
		this.scenarioShortName = this.scenarioReadableName.substring(this.scenarioReadableName.lastIndexOf('#')+5/*1+"test".length()*/, this.scenarioReadableName.length());
		this.scenarioComment = null;

		// Set testing environment if null
		if (ENV == null) {
			ENV = new TestingEnvironment();
			ENV.openEmptyWorkspace();
			setUpFullSourceWorkspace();
		}
		
		// Increment test position
		TEST_POSITION++;
		
		// Options will not be reset by default
		this.resetOptions = false;
	}
	/**
	 * @deprecated Use {@link #tagAsGlobalSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsGlobalSummary(String shortName, Dimension dimension) {
		tagAsGlobalSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	protected void tagAsGlobalSummary(String shortName, boolean fingerprint) {
		tagAsGlobalSummary(shortName, Dimension.ELAPSED_PROCESS, fingerprint);
	}
	protected void tagAsGlobalSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsGlobalSummary(shortName, dimension);
	}
	/**
	 * @deprecated We do not use this method...
	 */
	public void tagAsGlobalSummary(String shortName, Dimension[] dimensions) {
		System.out.println("ERROR: tagAsGlobalSummary(String, Dimension[]) is not implemented!!!");
	}
	/**
	 * @deprecated Use {@link #tagAsSummary(String,Dimension,boolean)} instead
	 */
	public void tagAsSummary(String shortName, Dimension dimension) {
		tagAsSummary(shortName, dimension, false); // do NOT put in fingerprint
	}
	protected void tagAsSummary(String shortName, boolean fingerprint) {
		tagAsSummary(shortName, Dimension.ELAPSED_PROCESS, fingerprint);
	}
	public void tagAsSummary(String shortName, Dimension dimension, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimension);
	}
	/**
	 * @deprecated We do not use this method...
	 */
	public void tagAsSummary(String shortName, Dimension[] dimensions) {
		System.out.println("ERROR: tagAsGlobalSummary(String, Dimension[]) is not implemented!!!");
	}
	public void tagAsSummary(String shortName, Dimension[] dimensions, boolean fingerprint) {
		if (DEBUG) System.out.println(shortName);
		if (fingerprint) super.tagAsSummary(shortName, dimensions);
	}
	/**
	 * Override super implementation to:
	 *	<ul>
	 *		<li>decrement all test count</li>
	 *		<li>reset workspace and back to initial options if last test run</li>
	 *</ul>
	 * @see org.eclipse.test.performance.PerformanceTestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		ALL_TESTS_COUNT--;
		if (ALL_TESTS_COUNT == 0) {
			ENV.resetWorkspace();
		}
		if (this.resetOptions) {
			JavaCore.setOptions(JavaCore.getDefaultOptions());
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
	protected void buildUsingBatchCompiler(String options) throws IOException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		final IWorkspaceRoot workspaceRoot = workspace.getRoot();
		final String targetWorkspacePath = workspaceRoot.getProject(JavaCore.PLUGIN_ID).getLocation().toFile().getCanonicalPath();
		final String sources = targetWorkspacePath + File.separator + "compiler";
		final String bins = targetWorkspacePath + File.separator + "bin"; //$NON-NLS-1$
		final String logs = targetWorkspacePath + File.separator + "log.txt"; //$NON-NLS-1$

		// Warm up
		String cmdLine = sources + " -1.4 -g -preserveAllLocals "+(options==null?"":options)+" -d " + bins + " -log " + logs; //$NON-NLS-1$ //$NON-NLS-2$
		int errorsCount = 0;
		for (int i=0; i<2; i++) {
			StringWriter errStrWriter = new StringWriter();
			PrintWriter err = new PrintWriter(errStrWriter);
			PrintWriter out = new PrintWriter(new StringWriter());
			Main main = new Main(out, err, false);
			main.compile(Main.tokenize(cmdLine));
			if (main.globalErrorsCount > 0 && main.globalErrorsCount != errorsCount) {
				System.out.println(this.scenarioShortName+": "+errorsCount+" Unexpected compile ERROR!");
				if (DEBUG) {
					System.out.println(errStrWriter.toString());
					System.out.println("--------------------");
				}
				errorsCount = main.globalErrorsCount;
			}
		}

		// Clear memory
		runGc();

		// Measures
		int max = MEASURES_COUNT * 2;
		int warnings = 0;
		for (int i = 0; i < max; i++) {
			StringWriter errStrWriter = new StringWriter();
			PrintWriter err = new PrintWriter(errStrWriter);
			PrintWriter out = new PrintWriter(new StringWriter());
			startMeasuring();
			Main main = new Main(out, err, false);
			main.compile(Main.tokenize(cmdLine));
			stopMeasuring();
			if (main.globalErrorsCount > 0 && main.globalErrorsCount != errorsCount) {
				System.out.println(this.scenarioShortName+": "+errorsCount+" Unexpected compile ERROR!");
				if (DEBUG) {
					System.out.println(errStrWriter.toString());
					System.out.println("--------------------");
				}
				errorsCount = main.globalErrorsCount;
			}
			cleanupDirectory(new File(bins));
			warnings = main.globalWarningsCount;
		}
		
		// Commit measures
		commitMeasurements();
		assertPerformance();

		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing batch compilation.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
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
	protected void startBuild(Hashtable options, boolean noWarning) throws IOException, CoreException {
		if (DEBUG) System.out.print("\tstart build...");
		JavaCore.setOptions(options);
		
		// Clean memory
		runGc();
		
		// Measure
		startMeasuring();
		ENV.fullBuild();
		stopMeasuring();
		
		// Verify markers
		IMarker[] markers = ResourcesPlugin.getWorkspace().getRoot().findMarkers(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		List resources = new ArrayList();
		List messages = new ArrayList();
		int warnings = 0;
		for (int i = 0, length = markers.length; i < length; i++) {
			IMarker marker = markers[i];
			switch (((Integer) marker.getAttribute(IMarker.SEVERITY)).intValue()) {
				case IMarker.SEVERITY_ERROR:
					resources.add(marker.getResource().getName());
					messages.add(marker.getAttribute(IMarker.MESSAGE));
					break;
				case IMarker.SEVERITY_WARNING:
					warnings++;
					if (noWarning) {
						resources.add(marker.getResource().getName());
						messages.add(marker.getAttribute(IMarker.MESSAGE));
					}
					break;
			}
		}
		
		// Assert result
		int size = messages.size();
		if (size > 0) {
			StringBuffer debugBuffer = new StringBuffer();
			for (int i=0; i<size; i++) {
				debugBuffer.append(resources.get(i));
				debugBuffer.append(":\n\t");
				debugBuffer.append(messages.get(i));
				debugBuffer.append('\n');
			}
			System.out.println(this.scenarioShortName+": Unexpected ERROR marker(s):\n" + debugBuffer.toString());
			System.out.println("--------------------");
		}
		if (DEBUG) System.out.println("done");
		
		// Commit measure
		commitMeasurements();
		assertPerformance();

		// Store warning
		if (warnings>0) {
			System.out.println("\t- "+warnings+" warnings found while performing build.");
		}
		if (this.scenarioComment == null) {
			this.scenarioComment = new StringBuffer("["+TEST_POSITION+"]");
		} else {
			this.scenarioComment.append(' ');
		}
		this.scenarioComment.append("warn=");
		this.scenarioComment.append(warnings);
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
	 * Possible kind:
	 * 	-1: no options
	 *  0: default options
	 *  1: all options
	 */
	protected Hashtable warningOptions(int kind) {

		// Values
		Hashtable optionsMap = null;
		switch (kind) {
			case 0:
				optionsMap = JavaCore.getDefaultOptions();
				break;
			default:
				optionsMap = new Hashtable(350);
				break;
		}
		if (kind == 0) {
			// Default set since 3.1
			optionsMap.put(CompilerOptions.OPTION_ReportUnusedImport, CompilerOptions.IGNORE); 
		} else {
			boolean all = kind == 1;
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
		}
		
		// Ignore 3.1 options
		optionsMap.put(CompilerOptions.OPTION_ReportMissingSerialVersion, CompilerOptions.IGNORE); 
		optionsMap.put(CompilerOptions.OPTION_ReportEnumIdentifier, CompilerOptions.IGNORE); 

		// Options should be reset while tear down test
		this.resetOptions = true;

		// Return created options map
		return optionsMap;
	}
}
