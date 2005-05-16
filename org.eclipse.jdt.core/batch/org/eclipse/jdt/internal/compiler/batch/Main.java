/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.compiler.batch;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.Compiler;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.compiler.util.HashtableOfObject;
import org.eclipse.jdt.internal.compiler.util.Util;

public class Main implements ProblemSeverities, SuffixConstants {

	public static class Logger {
		private static final String CLASS = "class"; //$NON-NLS-1$
		private static final String CLASS_FILE = "classfile"; //$NON-NLS-1$
		private static final String CLASSPATH = "classpath"; //$NON-NLS-1$
		private static final String CLASSPATH_ID = "id"; //$NON-NLS-1$
		private static final String CLASSPATH_FILE = "FILE"; //$NON-NLS-1$
		private static final String CLASSPATH_FOLDER = "FOLDER"; //$NON-NLS-1$
		private static final String CLASSPATH_JAR = "JAR"; //$NON-NLS-1$
		private static final String CLASSPATHS = "classpaths"; //$NON-NLS-1$
		private static final String COMMAND_LINE_ARGUMENT = "argument"; //$NON-NLS-1$
		private static final String COMMAND_LINE_ARGUMENTS = "command_line"; //$NON-NLS-1$
		private static final String COMPILER = "compiler"; //$NON-NLS-1$
		private static final String COMPILER_COPYRIGHT = "copyright"; //$NON-NLS-1$
		private static final String COMPILER_VERSION = "version"; //$NON-NLS-1$
		private static final String COMPILER_NAME = "name"; //$NON-NLS-1$
		private static final String EXCEPTION = "exception"; //$NON-NLS-1$
		private static final String ERROR = "ERROR"; //$NON-NLS-1$
		private static final String ERROR_TAG = "error"; //$NON-NLS-1$
		private static final String KEY = "key"; //$NON-NLS-1$
		private static final String MESSAGE = "message"; //$NON-NLS-1$
		private static final String NUMBER_OF_CLASSFILES = "number_of_classfiles"; //$NON-NLS-1$
		private static final String NUMBER_OF_ERRORS = "errors"; //$NON-NLS-1$
		private static final String NUMBER_OF_LINES = "number_of_lines"; //$NON-NLS-1$
		private static final String NUMBER_OF_PROBLEMS = "problems"; //$NON-NLS-1$
		private static final String NUMBER_OF_TASKS = "tasks"; //$NON-NLS-1$
		private static final String NUMBER_OF_WARNINGS = "warnings"; //$NON-NLS-1$
		private static final String OPTION = "option"; //$NON-NLS-1$
		private static final String OPTIONS = "options"; //$NON-NLS-1$
		private static final String PATH = "path"; //$NON-NLS-1$
		private static final String PROBLEM_ARGUMENT = "argument"; //$NON-NLS-1$
		private static final String PROBLEM_ARGUMENT_VALUE = "value"; //$NON-NLS-1$
		private static final String PROBLEM_ARGUMENTS = "arguments"; //$NON-NLS-1$
		private static final String PROBLEM_ID = "id"; //$NON-NLS-1$
		private static final String PROBLEM_LINE = "line"; //$NON-NLS-1$
		private static final String PROBLEM_MESSAGE = "message"; //$NON-NLS-1$
		private static final String PROBLEM_SEVERITY = "severity"; //$NON-NLS-1$
		private static final String PROBLEM_SOURCE_START = "charStart"; //$NON-NLS-1$
		private static final String PROBLEM_SOURCE_END = "charEnd"; //$NON-NLS-1$
		private static final String PROBLEM_SUMMARY = "problem_summary"; //$NON-NLS-1$
		private static final String PROBLEM_TAG = "problem"; //$NON-NLS-1$
		private static final String PROBLEMS = "problems"; //$NON-NLS-1$
		private static final String SOURCE = "source"; //$NON-NLS-1$
		private static final String SOURCE_CONTEXT = "source_context"; //$NON-NLS-1$
		private static final String SOURCE_END = "sourceEnd"; //$NON-NLS-1$
		private static final String SOURCE_START = "sourceStart"; //$NON-NLS-1$
		private static final String SOURCES = "sources"; //$NON-NLS-1$
		private static final String STATS = "stats"; //$NON-NLS-1$
		private static final String TASK = "task"; //$NON-NLS-1$
		private static final String TASKS = "tasks"; //$NON-NLS-1$
		private static final String TIME = "time"; //$NON-NLS-1$
		private static final String VALUE = "value"; //$NON-NLS-1$
		private static final String WARNING = "WARNING"; //$NON-NLS-1$
		private static final String XML_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"; //$NON-NLS-1$
		private static final String XML_DTD_DECLARATION = "<!DOCTYPE compiler SYSTEM \"compiler.dtd\">"; //$NON-NLS-1$

		private static final HashMap FIELD_TABLE = new HashMap();
		static {
			try {
				Class c = IProblem.class;
				Field[] fields = c.getFields();
				for (int i = 0, max = fields.length; i < max; i++) {
					Field field = fields[i];
					FIELD_TABLE.put(field.get(null), field.getName());
				}
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			}
		}
		private static void appendEscapedChar(StringBuffer buffer, char c) {
			String replacement= getReplacement(c);
			if (replacement != null) {
				buffer.append('&');
				buffer.append(replacement);
				buffer.append(';');
			} else {
				buffer.append(c);
			}
		}
		private static String getEscaped(String s) {
			StringBuffer result= new StringBuffer(s.length() + 10);
			for (int i= 0; i < s.length(); ++i)
				appendEscapedChar(result, s.charAt(i));
			return result.toString();
		}
		private static String getReplacement(char c) {
			// Encode special XML characters into the equivalent character references.
			// These five are defined by default for all XML documents.
			switch (c) {
				case '<' :
					return "lt"; //$NON-NLS-1$
				case '>' :
					return "gt"; //$NON-NLS-1$
				case '"' :
					return "quot"; //$NON-NLS-1$
				case '\'' :
					return "apos"; //$NON-NLS-1$
				case '&' :
					return "amp"; //$NON-NLS-1$
			}
			return null;
		}
		private PrintWriter err;
		boolean isXml;
		private PrintWriter log;
		private PrintWriter out;
		private int tab;
		private HashMap parameters;

		public Logger(PrintWriter out, PrintWriter err) {
			this.out = out;
			this.err = err;
			this.isXml = false;
			this.parameters = new HashMap();
		}

		public String buildFileName(
			String outputPath,
			String relativeFileName) {
			char fileSeparatorChar = File.separatorChar;
			String fileSeparator = File.separator;
			
			outputPath = outputPath.replace('/', fileSeparatorChar);
			// To be able to pass the mkdirs() method we need to remove the extra file separator at the end of the outDir name
			StringBuffer outDir = new StringBuffer(outputPath);
			if (!outputPath.endsWith(fileSeparator)) {
				outDir.append(fileSeparator);
			}
			StringTokenizer tokenizer =
				new StringTokenizer(relativeFileName, fileSeparator);
			String token = tokenizer.nextToken();
			while (tokenizer.hasMoreTokens()) {
				outDir.append(token).append(fileSeparator);
				token = tokenizer.nextToken();
			}
			// token contains the last one
			return outDir.append(token).toString();
		}
		
		public void close() {
			if (this.log != null) {
				if (this.isXml) {
					this.endTag(COMPILER);
					this.flush();
				}
				this.log.close();
			}
		}

		/**
		 * 
		 */
		public void compiling() {
			this.printlnOut(Main.bind("progress.compiling")); //$NON-NLS-1$
		}
		
		/**
		 * Used to stop logging problems.
		 * Only use in xml mode.
		 */
		private void endLoggingProblems() {
			this.endTag(PROBLEMS);
		}
		public void endLoggingSource() {
			if (this.isXml) {
				this.endTag(SOURCE);
			}
		}
		public void endLoggingSources() {
			if (this.isXml) {
				this.endTag(SOURCES);
			}
		}
		public void endLoggingTasks() {
			if (this.isXml) {
				this.endTag(TASKS);
			}
		}
		public void endTag(String name) {
			tab--;
			this.printTag('/' + name, null, true, false);
			this.tab--;
		}
		
		private void extractContext(IProblem problem, char[] unitSource) {
			//sanity .....
			int startPosition = problem.getSourceStart();
			int endPosition = problem.getSourceEnd();
			if ((startPosition > endPosition)
				|| ((startPosition < 0) && (endPosition < 0))) {
				this.parameters.put(VALUE, Messages.problem_noSourceInformation); 
				this.parameters.put(SOURCE_START, "-1"); //$NON-NLS-1$
				this.parameters.put(SOURCE_END, "-1"); //$NON-NLS-1$
				return;
			}

			char c;
			//the next code tries to underline the token.....
			//it assumes (for a good display) that token source does not
			//contain any \r \n. This is false on statements ! 
			//(the code still works but the display is not optimal !)

			// expand to line limits
			int length = unitSource.length, begin, end;
			for (begin = startPosition >= length ? length - 1 : startPosition; begin > 0; begin--) {
				if ((c = unitSource[begin - 1]) == '\n' || c == '\r') break;
			}
			for (end = endPosition >= length ? length - 1 : endPosition ; end+1 < length; end++) {
				if ((c = unitSource[end + 1]) == '\r' || c == '\n') break;
			}

			// trim left and right spaces/tabs
			while ((c = unitSource[begin]) == ' ' || c == '\t') begin++;
			while ((c = unitSource[end]) == ' ' || c == '\t') end--;
			
			// copy source
			StringBuffer buffer = new StringBuffer();
			buffer.append(unitSource, begin, end - begin + 1);
			
			this.parameters.put(VALUE, String.valueOf(buffer)); //$NON-NLS-1$
			this.parameters.put(SOURCE_START, Integer.toString(startPosition - begin)); //$NON-NLS-1$
			this.parameters.put(SOURCE_END, Integer.toString(endPosition - begin)); //$NON-NLS-1$
		}

		private String getFieldName(int id) {
			return (String) FIELD_TABLE.get(new Integer(id));
		}

		public void flush() {
			this.out.flush();
			this.err.flush();
			if (this.log != null) {
				this.log.flush();
			}
		}
		public void logAverage(long[] times, long lineCount) {
			Arrays.sort(times);
			final int length = times.length;
			long sum = 0;
			for (int i = 1, max = length - 1; i < max; i++) {
				sum += times[i];
			}
			long time = sum / (length - 2);
			this.printlnOut(Main.bind(
				"compile.averageTime", //$NON-NLS-1$
				new String[] {
					String.valueOf(lineCount),
					String.valueOf(time),
					String.valueOf(((int) (lineCount * 10000.0 / time)) / 10.0) }));
		}
		public void logClasspath(String[] classpaths) {
			if (classpaths == null) return;
			if (this.isXml) {
				final int length = classpaths.length;
				if (length != 0) {
					// generate xml output
					this.printTag(CLASSPATHS, null, true, false);
					for (int i = 0; i < length; i++) {
						this.parameters.clear();
						String classpath = classpaths[i];
						parameters.put(PATH, classpath);
						File f = new File(classpath);
						String id = null;
						if (f.isFile()) {
							if (Util.isArchiveFileName(classpath)) {
								id = CLASSPATH_JAR;
							} else {
								id = CLASSPATH_FILE;
							}
						} else if (f.isDirectory()) {
							id = CLASSPATH_FOLDER;
						}
						if (id != null) {
							this.parameters.put(CLASSPATH_ID, id);
							this.printTag(CLASSPATH, parameters, true, true);
						}
					}
					this.endTag(CLASSPATHS);
				}
			}
			
		}

		public void logClassFile(boolean generatePackagesStructure, String outputPath, String relativeFileName) {
			if (this.isXml) {
				String fileName = null;
				if (generatePackagesStructure) {
					fileName = buildFileName(outputPath, relativeFileName);
				} else {
					char fileSeparatorChar = File.separatorChar;
					String fileSeparator = File.separator;
					// First we ensure that the outputPath exists
					outputPath = outputPath.replace('/', fileSeparatorChar);
					// To be able to pass the mkdirs() method we need to remove the extra file separator at the end of the outDir name
					int indexOfPackageSeparator = relativeFileName.lastIndexOf(fileSeparatorChar);
					if (indexOfPackageSeparator == -1) {
						if (outputPath.endsWith(fileSeparator)) {
							fileName = outputPath + relativeFileName;
						} else {
							fileName = outputPath + fileSeparator + relativeFileName;
						}
					} else {
						int length = relativeFileName.length();
						if (outputPath.endsWith(fileSeparator)) {
							fileName = outputPath + relativeFileName.substring(indexOfPackageSeparator + 1, length);
						} else {
							fileName = outputPath + fileSeparator + relativeFileName.substring(indexOfPackageSeparator + 1, length);
						}
					}
				}
				File f = new File(fileName);
				try {
					this.parameters.clear();
					this.parameters.put(PATH, f.getCanonicalPath());
					this.printTag(CLASS_FILE, this.parameters, true, true);
				} catch (IOException e) {
					this.logNoClassFileCreated(fileName);
				}
			}	
		}
		
		public void logCommandLineArguments(String[] commandLineArguments) {
			if (commandLineArguments == null) return;
			if (this.isXml) {
				final int length = commandLineArguments.length;
				if (length != 0) {
					// generate xml output
					this.printTag(COMMAND_LINE_ARGUMENTS, null, true, false);
					parameters.clear();
					for (int i = 0; i < length; i++) {
						parameters.put(VALUE, commandLineArguments[i]);
						this.printTag(COMMAND_LINE_ARGUMENT, parameters, true, true);
					}
					this.endTag(COMMAND_LINE_ARGUMENTS);
				}
			}
		}

		/**
		 * @param e the given exception to log
		 */
		public void logException(Exception e) {
			final String message = e.getMessage();
			if (isXml) {
				parameters.clear();
				parameters.put(MESSAGE, message);
				parameters.put(CLASS, e.getClass());
				this.printTag(EXCEPTION, parameters, true, true);
			}
			this.printlnErr(message);
		}

		/**
		 * @param wrongClasspath
		 *            the given wrong classpath entry
		 */
		public void logIncorrectClasspath(String wrongClasspath) {
			if (isXml) {
				this.parameters.clear();
				this.parameters.put(MESSAGE, Main.bind("configure.incorrectClasspath", wrongClasspath)); //$NON-NLS-1$
				this.printTag(ERROR_TAG, this.parameters, true, true);
			}
			this.printlnErr(Main.bind(
				"configure.incorrectClasspath", wrongClasspath)); //$NON-NLS-1$
		}

		/**
		 * 
		 */
		public void logNoClassFileCreated(String fileName) {
			if (isXml) {
				this.parameters.clear();
				this.parameters.put(MESSAGE, Main.bind("output.noClassFileCreated", fileName)); //$NON-NLS-1$
				this.printTag(ERROR_TAG, this.parameters, true, true);
			}
			this.printlnErr(Main.bind("output.noClassFileCreated", fileName)); //$NON-NLS-1$
		}

		public void logNoClasspath() {
			if (isXml) {
				this.parameters.clear();
				this.parameters.put(MESSAGE, Main.bind("configure.noClasspath")); //$NON-NLS-1$//$NON-NLS-2$
				this.printTag(ERROR_TAG, this.parameters, true, true);
			}
			this.printlnErr(Main.bind("configure.noClasspath")); //$NON-NLS-1$
		}

		/**
		 * @param exportedClassFilesCounter
		 */
		public void logNumberOfClassFilesGenerated(int exportedClassFilesCounter) {
			if (isXml) {
				this.parameters.clear();
				this.parameters.put(VALUE, new Integer(exportedClassFilesCounter)); //$NON-NLS-1$
				this.printTag(NUMBER_OF_CLASSFILES, this.parameters, true, true);
			}
			if (exportedClassFilesCounter == 1) {
				this.printlnOut(Main.bind("compile.oneClassFileGenerated")); //$NON-NLS-1$
			} else {
				this.printlnOut(Main.bind("compile.severalClassFilesGenerated", //$NON-NLS-1$
					String.valueOf(exportedClassFilesCounter)));
			}
		}

		/**
		 * @param options the given compiler options
		 */
		public void logOptions(Map options) {
			if (this.isXml) {
				this.printTag(OPTIONS, null, true, false);
				final Set keySet = options.keySet();
				Object[] keys = keySet.toArray();
				Arrays.sort(keys);
				for (int i = 0, max = keys.length; i < max; i++) {
					this.parameters.clear();
					Object key = keys[i];
					this.parameters.put(KEY, key);
					this.parameters.put(VALUE, options.get(key));
					this.printTag(OPTION, this.parameters, true, true);
				}
				this.endTag(OPTIONS);
			}
		}

		private void logProblem(IProblem problem, int localErrorCount,
			int globalErrorCount, char[] unitSource) {
			if (localErrorCount == 0) {
				this.printlnErr("----------"); //$NON-NLS-1$
			}
			this.printlnErr(problem.isError() ?
				Main.bind(
					"requestor.error", //$NON-NLS-1$
					Integer.toString(globalErrorCount),
					new String(problem.getOriginatingFileName()))
				: Main.bind(
					"requestor.warning", //$NON-NLS-1$
					Integer.toString(globalErrorCount),
					new String(problem.getOriginatingFileName())));
			try {
				this.printlnErr(((DefaultProblem) problem).errorReportSource(unitSource));
				this.printlnErr(problem.getMessage());
			} catch (Exception e) {
				this.printlnErr(Main.bind(
					"requestor.notRetrieveErrorMessage", problem.toString())); //$NON-NLS-1$
			}
			this.printlnErr("----------"); //$NON-NLS-1$
		}
		
		/**
		 * @param globalProblemsCount
		 * @param globalErrorsCount
		 * @param globalWarningsCount
		 */
		public void logProblemsSummary(int globalProblemsCount,
			int globalErrorsCount, int globalWarningsCount, int globalTasksCount) {
			if (this.isXml) {
				// generate xml
				parameters.clear();
				parameters.put(NUMBER_OF_PROBLEMS, new Integer(globalProblemsCount));
				parameters.put(NUMBER_OF_ERRORS, new Integer(globalErrorsCount));
				parameters.put(NUMBER_OF_WARNINGS, new Integer(globalWarningsCount));
				parameters.put(NUMBER_OF_TASKS, new Integer(globalTasksCount));
				this.printTag(PROBLEM_SUMMARY, parameters, true, true);
			}
			if (globalProblemsCount == 1) {
				String message = null;
				if (globalErrorsCount == 1) {
					message = Main.bind("compile.oneError"); //$NON-NLS-1$
				} else {
					message = Main.bind("compile.oneWarning"); //$NON-NLS-1$
				}
				this.printErr(Main.bind("compile.oneProblem", message)); //$NON-NLS-1$
			} else {
				String errorMessage = null;
				String warningMessage = null;
				if (globalErrorsCount > 0) {
					if (globalErrorsCount == 1) {
						errorMessage = Main.bind("compile.oneError"); //$NON-NLS-1$
					} else {
						errorMessage = Main.bind("compile.severalErrors", String.valueOf(globalErrorsCount)); //$NON-NLS-1$
					}
				}
				int warningsNumber = globalWarningsCount + globalTasksCount;
				if (warningsNumber > 0) {
					if (warningsNumber == 1) {
						warningMessage = Main.bind("compile.oneWarning"); //$NON-NLS-1$
					} else {
						warningMessage = Main.bind("compile.severalWarnings", String.valueOf(warningsNumber)); //$NON-NLS-1$
					}
				}
				if (errorMessage == null || warningMessage == null) {
					if (errorMessage == null) {
						this.printErr(Main.bind(
							"compile.severalProblemsErrorsOrWarnings", //$NON-NLS-1$
							String.valueOf(globalProblemsCount),
							warningMessage));
					} else {
						this.printErr(Main.bind(
							"compile.severalProblemsErrorsOrWarnings", //$NON-NLS-1$
							String.valueOf(globalProblemsCount),
							errorMessage));
					}
				} else {
					this.printErr(Main.bind(
						"compile.severalProblemsErrorsAndWarnings", //$NON-NLS-1$
						new String[] {
							String.valueOf(globalProblemsCount),
							errorMessage,
							warningMessage
						}));
				}
			}
		}

		public int logProblems(IProblem[] problems, char[] unitSource, Main currentMain) {
			final int count = problems.length;
			int localErrorCount = 0;
			if (count != 0) {
				if (this.isXml) {
					int errors = 0;
					int warnings = 0;
					int tasks = 0;
					for (int i = 0; i < count; i++) {
						IProblem problem = problems[i];
						if (problem != null) {
							currentMain.globalProblemsCount++;
							this.logProblem(problem, localErrorCount, currentMain.globalProblemsCount, unitSource);
							if (problem.isError()) {
								errors++;
								currentMain.globalErrorsCount++;
								localErrorCount++;
							} else if (problem.getID() == IProblem.Task) {
								currentMain.globalTasksCount++;
								tasks++;
							} else {
								currentMain.globalWarningsCount++;
								warnings++;
							}
						}
					}
					if ((errors + warnings) != 0) {
						this.startLoggingProblems(errors, warnings);
						for (int i = 0; i < count; i++) {
							IProblem problem = problems[i];
							if (problem!= null) {
								if (problem.getID() != IProblem.Task) {
									this.logXmlProblem(problem, unitSource);
								}
							}
						}
						this.endLoggingProblems();
					}
					if (tasks != 0) {
						this.startLoggingTasks(tasks);
						for (int i = 0; i < count; i++) {
							IProblem problem = problems[i];
							if (problem!= null) {
								if (problem.getID() == IProblem.Task) {
									this.logXmlTask(problem, unitSource);
								}
							}
						}
						this.endLoggingTasks();
					}
				} else {
					for (int i = 0; i < count; i++) {
						if (problems[i] != null) {
							currentMain.globalProblemsCount++;
							this.logProblem(problems[i], localErrorCount, currentMain.globalProblemsCount, unitSource);
							if (problems[i].isError()) {
								currentMain.globalErrorsCount++;
								localErrorCount++;
							} else {
								currentMain.globalWarningsCount++;
							}
						}
					}
				}
			}
			return localErrorCount;
		}
		
		/**
		 * 
		 */
		public void logProgress() {
			this.printOut('.');
		}

		/**
		 * @param i
		 *            the current repetition number
		 * @param repetitions
		 *            the given number of repetitions
		 */
		public void logRepetition(int i, int repetitions) {
			this.printlnOut(Main.bind("compile.repetition", //$NON-NLS-1$
				String.valueOf(i + 1), String.valueOf(repetitions)));
		}

		public void printStats(Main main) {
			final boolean isTimed = main.timing;
			if (isXml) {
				this.printTag(STATS, null, true, false);
			}
			if (isTimed) {
				long time = System.currentTimeMillis() - main.startTime;
				this.logTiming(time, main.lineCount);
				if (main.times != null) {
					main.times[main.timesCounter++] = time;
				}
			}
			if (main.globalProblemsCount > 0) {
				this.logProblemsSummary(main.globalProblemsCount, main.globalErrorsCount, main.globalWarningsCount, main.globalTasksCount);
			}
			if (main.exportedClassFilesCounter != 0
					&& (main.showProgress || isTimed || main.verbose)) {
				this.logNumberOfClassFilesGenerated(main.exportedClassFilesCounter);
			}
			if (isXml) {
				this.endTag(STATS);
			}
		}
		/**
		 * @param time
		 * @param lineCount
		 */
		public void logTiming(long time, long lineCount) {
			if (isXml) {
				this.parameters.clear();
				this.parameters.put(VALUE, new Long(time));
				this.printTag(TIME, this.parameters, true, true);
				this.parameters.clear();
				this.parameters.put(VALUE, new Long(lineCount));
				this.printTag(NUMBER_OF_LINES, this.parameters, true, true);
			}
			if (lineCount != 0) {
				this.printlnOut(Main.bind(
					"compile.instantTime", //$NON-NLS-1$
					new String[] {
						String.valueOf(lineCount),
						String.valueOf(time),
						String.valueOf(((int) (lineCount * 10000.0 / time)) / 10.0) }));
			} else {
				this.printlnOut(Main.bind("compile.totalTime", String.valueOf(time))); //$NON-NLS-1$
			}
		}

		/**
		 * @param usage
		 */
		public void logUsage(String usage) {
			this.printlnOut(usage); //$NON-NLS-1$//$NON-NLS-2$
		}

		/**
		 * 
		 */
		public void logVersion() {
			this.printlnOut(Main.bind("misc.version", //$NON-NLS-1$
				new String[] {
					Main.bind("compiler.name"), //$NON-NLS-1$
					Main.bind("compiler.version"), //$NON-NLS-1$
					Main.bind("compiler.copyright") //$NON-NLS-1$
				}
			));
		}

		/**
		 * 
		 */
		public void logWrongJDK() {
			if (isXml) {
				parameters.clear();
				parameters.put(MESSAGE, Main.bind("configure.requiresJDK1.2orAbove")); //$NON-NLS-1$//$NON-NLS-2$
				this.printTag(ERROR, parameters, true, true);				
			}
			this.printlnErr(Main.bind("configure.requiresJDK1.2orAbove")); //$NON-NLS-1$
		}

		/**
		 * @param problem
		 *            the given problem to log
		 * @param unitSource
		 *            the given unit source
		 */
		private void logXmlProblem(IProblem problem, char[] unitSource) {
			final int sourceStart = problem.getSourceStart();
			final int sourceEnd = problem.getSourceEnd();
			this.parameters.clear();
			this.parameters.put(PROBLEM_ID, getFieldName(problem.getID()));
			this.parameters.put(PROBLEM_SEVERITY, problem.isError() ? ERROR : WARNING);
			this.parameters.put(PROBLEM_LINE, new Integer(problem.getSourceLineNumber()));
			this.parameters.put(PROBLEM_SOURCE_START, new Integer(sourceStart));
			this.parameters.put(PROBLEM_SOURCE_END, new Integer(sourceEnd));
			this.printTag(PROBLEM_TAG, parameters, true, false);
			this.parameters.clear();
			this.parameters.put(VALUE, problem.getMessage());
			this.printTag(PROBLEM_MESSAGE, parameters, true, true);
			this.parameters.clear();
			extractContext(problem, unitSource);
			this.printTag(SOURCE_CONTEXT, this.parameters, true, true);
			String[] arguments = problem.getArguments();
			final int length = arguments.length;
			if (length != 0) {
				this.printTag(PROBLEM_ARGUMENTS, null, true, false);
				this.parameters.clear();
				for (int i = 0; i < length; i++) {
					this.parameters.put(PROBLEM_ARGUMENT_VALUE, arguments[i]);
					this.printTag(PROBLEM_ARGUMENT, this.parameters, true, true);
				}
				this.endTag(PROBLEM_ARGUMENTS);
			}
			this.endTag(PROBLEM_TAG);
		}
		/**
		 * @param problem
		 *            the given problem to log
		 * @param unitSource
		 *            the given unit source
		 */
		private void logXmlTask(IProblem problem, char[] unitSource) {
			this.parameters.clear();
			this.parameters.put(PROBLEM_LINE, new Integer(problem.getSourceLineNumber()));
			this.parameters.put(PROBLEM_SOURCE_START, new Integer(problem.getSourceStart()));
			this.parameters.put(PROBLEM_SOURCE_END, new Integer(problem.getSourceEnd()));
			this.printTag(TASK, this.parameters, true, false);
			this.parameters.clear();
			this.parameters.put(VALUE, problem.getMessage());
			this.printTag(PROBLEM_MESSAGE, this.parameters, true, true);
			this.parameters.clear();
			extractContext(problem, unitSource);
			this.printTag(SOURCE_CONTEXT, this.parameters, true, true);
			this.endTag(TASK);
		}
		private void printErr(String s) {
			this.err.print(s);
			if (!this.isXml) {
				if (this.log != null) {
					this.log.print(s);
				}
			}
		}

		private void printlnErr(String s) {
			this.err.println(s);
			if (!this.isXml) {
				if (this.log != null) {
					this.log.println(s);
				}
			}
		}

		private void printlnOut(String s) {
			this.out.println(s);
			if (!this.isXml) {
				if (this.log != null) {
					this.log.println(s);
				}
			}
		}

		/**
		 * 
		 */
		public void printNewLine() {
			this.out.println();
		}

		private void printOut(char c) {
			this.out.print(c);
		}
		public void printTag(String name, HashMap params, boolean insertNewLine, boolean closeTag) {
			for (int i= this.tab; i > 0; i--) this.log.print('\t');
			StringBuffer buffer= new StringBuffer();
			buffer.append("<"); //$NON-NLS-1$
			buffer.append(name);
			if (params != null) {
				for (Enumeration enumeration = Collections.enumeration(params.keySet()); enumeration.hasMoreElements();) {
					buffer.append(" "); //$NON-NLS-1$
					String key= (String) enumeration.nextElement();
					buffer.append(key);
					buffer.append("=\""); //$NON-NLS-1$
					buffer.append(getEscaped(String.valueOf(params.get(key))));
					buffer.append("\""); //$NON-NLS-1$
				}
			}
			if (closeTag) {
				buffer.append("/>"); //$NON-NLS-1$
			} else {
				buffer.append(">"); //$NON-NLS-1$
				this.tab++;
			}
			if (insertNewLine) {
				this.log.println(String.valueOf(buffer));
			} else {
				this.log.print(String.valueOf(buffer));
			}
		}

		public void setLog(String logFileName) throws InvalidInputException {
			final Date date = new Date();
			final SimpleDateFormat dateFormat = new SimpleDateFormat("d MMM yyyy HH:mm:ss", Locale.getDefault());//$NON-NLS-1$
			try {
				this.log = new PrintWriter(new FileOutputStream(logFileName, false));
				int index = logFileName.lastIndexOf('.');
				if (index != -1) {
					if (logFileName.substring(index).toLowerCase().equals(".xml")) { //$NON-NLS-1$
						this.isXml = true;
						this.log.println(XML_HEADER);
						// insert time stamp as comment
						this.log.println("<!-- " + dateFormat.format(date) + " -->");//$NON-NLS-1$//$NON-NLS-2$
						this.log.println(XML_DTD_DECLARATION);
						this.tab = 0;
						parameters.clear();
						parameters.put(COMPILER_NAME, Main.bind("compiler.name")); //$NON-NLS-1$//$NON-NLS-2$
						parameters.put(COMPILER_VERSION, Main.bind("compiler.version")); //$NON-NLS-1$//$NON-NLS-2$
						parameters.put(COMPILER_COPYRIGHT, Main.bind("compiler.copyright")); //$NON-NLS-1$//$NON-NLS-2$
						this.printTag(COMPILER, parameters, true, false);
					} else {
						this.log.println("# " + dateFormat.format(date));//$NON-NLS-1$//$NON-NLS-2$
					}
				} else {
					this.log.println("# " + dateFormat.format(date));//$NON-NLS-1$//$NON-NLS-2$
				}
			} catch (FileNotFoundException e) {
				throw new InvalidInputException(Main.bind("configure.cannotOpenLog")); //$NON-NLS-1$
			}
		}

		/**
		 * Used to start logging problems.
		 * Only use in xml mode.
		 */
		private void startLoggingProblems(int errors, int warnings) {
			parameters.clear();
			parameters.put(NUMBER_OF_PROBLEMS, new Integer(errors + warnings));
			parameters.put(NUMBER_OF_ERRORS, new Integer(errors));
			parameters.put(NUMBER_OF_WARNINGS, new Integer(warnings));
			this.printTag(PROBLEMS, this.parameters, true, false);
		}
		public void startLoggingSource(CompilationResult compilationResult) {
			if (this.isXml) {
				ICompilationUnit compilationUnit = compilationResult.compilationUnit;
				char[] fileName = compilationUnit.getFileName();
				File f = new File(new String(fileName));
				if (fileName != null) {
					this.parameters.clear();
					if (compilationUnit != null) {
						this.parameters.put(PATH, f.getAbsolutePath());
					}
				}
				this.printTag(SOURCE, this.parameters, true, false);
			}
		}
		public void startLoggingSources() {
			if (this.isXml) {
				this.printTag(SOURCES, null, true, false);
			}
		}
		public void startLoggingTasks(int tasks) {
			if (this.isXml) {
				parameters.clear();
				parameters.put(NUMBER_OF_TASKS, new Integer(tasks));
				this.printTag(TASKS, this.parameters, true, false);
			}
		}
	}
	static {
		relocalize();
	}

	/* Bundle containing messages */
	public static ResourceBundle bundle;
	public final static String bundleName =
		"org.eclipse.jdt.internal.compiler.batch.messages"; 	//$NON-NLS-1$

	public String[] classpaths;
	public String destinationPath;
	public String[] encodings;
	public Logger logger;
	public int exportedClassFilesCounter;
	public String[] filenames;
	public boolean generatePackagesStructure;
	public int globalErrorsCount;
	public int globalTasksCount;
	public int globalProblemsCount;
	public int globalWarningsCount;
	public long lineCount;
	public String log;

	public boolean noWarn = false;

	public Map options; 
	public CompilerOptions compilerOptions; // read-only

	public boolean proceed = true;
	public boolean proceedOnError = false;
	public boolean produceRefInfo = false;
	public int repetitions;
	public int maxProblems;
	public boolean showProgress = false;
	public boolean systemExitWhenFinished = true;
	public long startTime;
	public boolean timing = false;
	public long[] times;
	public int timesCounter;
	public boolean verbose = false;

	public Main(PrintWriter outWriter, PrintWriter errWriter, boolean systemExitWhenFinished) {
		this(outWriter, errWriter, systemExitWhenFinished, null);
	}
	
	public Main(PrintWriter outWriter, PrintWriter errWriter, boolean systemExitWhenFinished, Map customDefaultOptions) {
		this.logger = new Logger(outWriter, errWriter);
		this.systemExitWhenFinished = systemExitWhenFinished;
		this.options = new CompilerOptions().getMap();
		if (customDefaultOptions != null) {
			for (Iterator iter = customDefaultOptions.keySet().iterator(); iter.hasNext();) {
				Object key = iter.next();
				this.options.put(key, customDefaultOptions.get(key));
			}
		}
	}
	
	/*
	 * Lookup the message with the given ID in this catalog 
	 */
	public static String bind(String id) {
		return bind(id, (String[]) null);
	}

	/*
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string.
	 */
	public static String bind(String id, String binding) {
		return bind(id, new String[] { binding });
	}

	/*
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given strings.
	 */
	public static String bind(String id, String binding1, String binding2) {
		return bind(id, new String[] { binding1, binding2 });
	}

	/*
	 * Lookup the message with the given ID in this catalog and bind its
	 * substitution locations with the given string values.
	 */
	public static String bind(String id, String[] arguments) {
		if (id == null)
			return "No message available"; //$NON-NLS-1$
		String message = null;
		try {
			message = bundle.getString(id);
		} catch (MissingResourceException e) {
			// If we got an exception looking for the message, fail gracefully by just returning
			// the id we were looking for.  In most cases this is semi-informative so is not too bad.
			return "Missing message: " + id + " in: " + bundleName; //$NON-NLS-2$ //$NON-NLS-1$
		}
		return MessageFormat.format(message, arguments);
	}

	/*
	 * Internal IDE API
	 */
	public static boolean compile(String commandLine) {

		return compile(commandLine, new PrintWriter(System.out), new PrintWriter(System.err));
	}

	/*
	 * Internal IDE API for test harness purpose
	 */
	public static boolean compile(String commandLine, PrintWriter outWriter, PrintWriter errWriter) {

		return new Main(outWriter, errWriter, false).compile(tokenize(commandLine));
	}
	
	/*
	 * External API
	 */

	public static void main(String[] argv) {
		new Main(new PrintWriter(System.out), new PrintWriter(System.err), true).compile(argv);
	}

	/**
	 * Creates a NLS catalog for the given locale.
	 */
	public static void relocalize() {
		try {
			bundle = ResourceBundle.getBundle(bundleName, Locale.getDefault());
		} catch(MissingResourceException e) {
			System.out.println("Missing resource : " + bundleName.replace('.', '/') + ".properties for locale " + Locale.getDefault()); //$NON-NLS-1$//$NON-NLS-2$
			throw e;
		}
	}

	public static String[] tokenize(String commandLine) {

		int count = 0;
		String[] arguments = new String[10];
		StringTokenizer tokenizer = new StringTokenizer(commandLine, " \"", true); //$NON-NLS-1$
		String token = ""; //$NON-NLS-1$
		boolean insideQuotes = false;
		boolean startNewToken = true;

		// take care to quotes on the command line
		// 'xxx "aaa bbb";ccc yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx "aaa bbb;ccc" yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx "aaa bbb";"ccc" yyy' --->  {"xxx", "aaa bbb;ccc", "yyy" }
		// 'xxx/"aaa bbb";"ccc" yyy' --->  {"xxx/aaa bbb;ccc", "yyy" }
		while (tokenizer.hasMoreTokens()) {
			token = tokenizer.nextToken();

			if (token.equals(" ")) { //$NON-NLS-1$
				if (insideQuotes) {
					arguments[count - 1] += token;
					startNewToken = false;
				} else {
					startNewToken = true;
				}
			} else if (token.equals("\"")) { //$NON-NLS-1$
				if (!insideQuotes && startNewToken) { //$NON-NLS-1$
					if (count == arguments.length)
						System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
					arguments[count++] = ""; //$NON-NLS-1$
				}
				insideQuotes = !insideQuotes;
				startNewToken = false;
			} else {
				if (insideQuotes) {
					arguments[count - 1] += token;
				} else {
					if (token.length() > 0 && !startNewToken) {
						arguments[count - 1] += token;
					} else {
						if (count == arguments.length)
							System.arraycopy(arguments, 0, (arguments = new String[count * 2]), 0, count);
						String trimmedToken = token.trim();
						if (trimmedToken.length() != 0) {
							arguments[count++] = trimmedToken;
						}
					}
				}
				startNewToken = false;
			}
		}
		System.arraycopy(arguments, 0, arguments = new String[count], 0, count);
		return arguments;
	}

	/*
	 *  Low-level API performing the actual compilation
	 */
	public boolean compile(String[] argv) {

		// decode command line arguments
		try {
			configure(argv);
			if (this.proceed) {
//				if (this.verbose) {
//					System.out.println(new CompilerOptions(this.options));
//				}
				if (this.showProgress) this.logger.compiling(); //$NON-NLS-1$
				for (int i = 0; i < this.repetitions; i++) {
					this.globalProblemsCount = 0;
					this.globalErrorsCount = 0;
					this.globalWarningsCount = 0;
					this.globalTasksCount = 0;
					this.lineCount = 0;
					this.exportedClassFilesCounter = 0;

					if (this.repetitions > 1) {
						this.logger.flush();
						this.logger.logRepetition(i, this.repetitions);
					} 
					// request compilation
					performCompilation();
				}
				if (this.times != null) {
					this.logger.logAverage(this.times, this.lineCount);
				}
				if (this.showProgress) this.logger.printNewLine();
			}
			if (this.systemExitWhenFinished) {
				this.logger.flush();
    			this.logger.close();
				System.exit(this.globalErrorsCount > 0 ? -1 : 0);
			}
		} catch (InvalidInputException e) {
			this.logger.logException(e);
			if (this.systemExitWhenFinished) {
    			this.logger.flush();
    			this.logger.close();
				System.exit(-1);
			}
			return false;
		} catch (RuntimeException e) { // internal compiler failure
			if (this.systemExitWhenFinished) {
				this.logger.flush();
				this.logger.close();
				System.exit(-1);
			}
			return false;
		} finally {
			this.logger.flush();
			this.logger.close();
		}
		if (this.globalErrorsCount == 0)
			return true;
		return false;
	}

	/*
	Decode the command line arguments 
	 */
	public void configure(String[] argv) throws InvalidInputException {
		
		if ((argv == null) || (argv.length == 0)) {
			printUsage();
			return;
		}
		final int InsideClasspath = 1;
		final int InsideDestinationPath = 2;
		final int TargetSetting = 4;
		final int InsideLog = 8;
		final int InsideRepetition = 16;
		final int InsideSource = 32;
		final int InsideDefaultEncoding = 64;
		final int InsideBootClasspath = 128;
		final int InsideMaxProblems = 256;
		final int Default = 0;
		String[] bootclasspaths = null;
		int DEFAULT_SIZE_CLASSPATH = 4;
		int pathCount = 0;
		int bootclasspathCount = 0;
		int index = -1, filesCount = 0, argCount = argv.length;
		int mode = Default;
		this.repetitions = 0;
		boolean printUsageRequired = false;
		boolean printVersionRequired = false;
		
		boolean didSpecifySource = false;
		boolean didSpecifyCompliance = false;
		boolean didSpecifyDefaultEncoding = false;
		boolean didSpecifyTarget = false;
		boolean didSpecifyDeprecation = false;
		boolean didSpecifyWarnings = false;
		boolean useEnableJavadoc = false;

		String customEncoding = null;
		String currentArg = ""; //$NON-NLS-1$

		// expand the command line if necessary
		boolean needExpansion = false;
		loop: for (int i = 0; i < argCount; i++) {
				if (argv[i].startsWith("@")) { //$NON-NLS-1$
					needExpansion = true;
					break loop;
				}
		}

		String[] newCommandLineArgs = null;
		if (needExpansion) {
			newCommandLineArgs = new String[argCount];
			index = 0;
			for (int i = 0; i < argCount; i++) {
				String[] newArgs = null;
				String arg = argv[i].trim();
				if (arg.startsWith("@")) { //$NON-NLS-1$
					try {
						LineNumberReader reader = new LineNumberReader(new StringReader(new String(Util.getFileCharContent(new File(arg.substring(1)), null))));
						StringBuffer buffer = new StringBuffer();
						String line;
						while((line = reader.readLine()) != null) {
							buffer.append(line).append(" "); //$NON-NLS-1$
						}
						newArgs = tokenize(buffer.toString());
					} catch(IOException e) {
						throw new InvalidInputException(
							Main.bind("configure.invalidexpansionargumentname", arg)); //$NON-NLS-1$
					}
				}
				if (newArgs != null) {
					int newCommandLineArgsLength = newCommandLineArgs.length;
					int newArgsLength = newArgs.length;
					System.arraycopy(newCommandLineArgs, 0, (newCommandLineArgs = new String[newCommandLineArgsLength + newArgsLength - 1]), 0, index);
					System.arraycopy(newArgs, 0, newCommandLineArgs, index, newArgsLength);
					index += newArgsLength;
				} else {
					newCommandLineArgs[index++] = arg;
				}
			}
			index = -1;
		} else {
			newCommandLineArgs = argv;
			for (int i = 0; i < argCount; i++) {
				newCommandLineArgs[i] = newCommandLineArgs[i].trim();
			}
		}
		argCount = newCommandLineArgs.length;
		while (++index < argCount) {

			if (customEncoding != null) {
				throw new InvalidInputException(
					Main.bind("configure.unexpectedCustomEncoding", currentArg, customEncoding)); //$NON-NLS-1$
			}

			currentArg = newCommandLineArgs[index];

			customEncoding = null;
			if (currentArg.endsWith("]")) { //$NON-NLS-1$ 
				// look for encoding specification
				int encodingStart = currentArg.indexOf('[') + 1;
				int encodingEnd = currentArg.length() - 1;
				if (encodingStart >= 1) {
					if (encodingStart < encodingEnd) {
						customEncoding = currentArg.substring(encodingStart, encodingEnd);
						try { // ensure encoding is supported
							new InputStreamReader(new ByteArrayInputStream(new byte[0]), customEncoding);
						} catch (UnsupportedEncodingException e) {
							throw new InvalidInputException(
								Main.bind("configure.unsupportedEncoding", customEncoding)); //$NON-NLS-1$
						}
					}
					currentArg = currentArg.substring(0, encodingStart - 1);
				}
			}

			if (currentArg.endsWith(SUFFIX_STRING_java)) {
				if (this.filenames == null) {
					this.filenames = new String[argCount - index];
					this.encodings = new String[argCount - index];
				} else if (filesCount == this.filenames.length) {
					int length = this.filenames.length;
					System.arraycopy(
						this.filenames,
						0,
						(this.filenames = new String[length + argCount - index]),
						0,
						length);
					System.arraycopy(
						this.encodings,
						0,
						(this.encodings = new String[length + argCount - index]),
						0,
						length);
				}
				this.filenames[filesCount] = currentArg;
				this.encodings[filesCount++] = customEncoding;
				customEncoding = null;
				mode = Default;
				continue;
			}
			if (currentArg.equals("-log")) { //$NON-NLS-1$
				if (this.log != null)
					throw new InvalidInputException(
						Main.bind("configure.duplicateLog", currentArg)); //$NON-NLS-1$
				mode = InsideLog;
				continue;
			}
			if (currentArg.equals("-repeat")) { //$NON-NLS-1$
				if (this.repetitions > 0)
					throw new InvalidInputException(
						Main.bind("configure.duplicateRepeat", currentArg)); //$NON-NLS-1$
				mode = InsideRepetition;
				continue;
			}
			if (currentArg.equals("-maxProblems")) { //$NON-NLS-1$
				if (this.maxProblems > 0)
					throw new InvalidInputException(
						Main.bind("configure.duplicateMaxProblems", currentArg)); //$NON-NLS-1$
				mode = InsideMaxProblems;
				continue;
			}
			if (currentArg.equals("-source")) { //$NON-NLS-1$
				mode = InsideSource;
				continue;
			}
			if (currentArg.equals("-encoding")) { //$NON-NLS-1$
				mode = InsideDefaultEncoding;
				continue;
			}
			if (currentArg.equals("-1.3")) { //$NON-NLS-1$
				if (didSpecifyCompliance) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateCompliance", currentArg));//$NON-NLS-1$
				}
				didSpecifyCompliance = true;
				this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_3);
				mode = Default;
				continue;
			}
			if (currentArg.equals("-1.4")) { //$NON-NLS-1$
				if (didSpecifyCompliance) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateCompliance", currentArg)); //$NON-NLS-1$
				}
				didSpecifyCompliance = true;
				this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
				mode = Default;
				continue;
			}
			if (currentArg.equals("-1.5") || currentArg.equals("-5") || currentArg.equals("-5.0")) { //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (didSpecifyCompliance) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateCompliance", currentArg)); //$NON-NLS-1$
				}
				didSpecifyCompliance = true;
				this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
				mode = Default;
				continue;
			}			
			if (currentArg.equals("-d")) { //$NON-NLS-1$
				if (this.destinationPath != null)
					throw new InvalidInputException(
						Main.bind("configure.duplicateOutputPath", currentArg)); //$NON-NLS-1$
				mode = InsideDestinationPath;
				this.generatePackagesStructure = true;
				continue;
			}
			if (currentArg.equals("-classpath") //$NON-NLS-1$
				|| currentArg.equals("-cp")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (pathCount == 0) {
					this.classpaths = new String[DEFAULT_SIZE_CLASSPATH];
				}
				mode = InsideClasspath;
				continue;
			}
			if (currentArg.equals("-bootclasspath")) {//$NON-NLS-1$
				if (bootclasspathCount > 0)
					throw new InvalidInputException(
						Main.bind("configure.duplicateBootClasspath", currentArg)); //$NON-NLS-1$
				bootclasspaths = new String[DEFAULT_SIZE_CLASSPATH];
				mode = InsideBootClasspath;
				continue;
			}
			if (currentArg.equals("-progress")) { //$NON-NLS-1$
				mode = Default;
				this.showProgress = true;
				continue;
			}
			if (currentArg.equals("-proceedOnError")) { //$NON-NLS-1$
				mode = Default;
				this.proceedOnError = true;
				continue;
			}
			if (currentArg.equals("-time")) { //$NON-NLS-1$
				mode = Default;
				this.timing = true;
				continue;
			}
			if (currentArg.equals("-version") //$NON-NLS-1$
					|| currentArg.equals("-v")) { //$NON-NLS-1$
				printVersion();
				this.proceed = false;
				return;
			}
			if (currentArg.equals("-showversion")) { //$NON-NLS-1$
				printVersionRequired = true;
				continue;
			}			
			if ("-deprecation".equals(currentArg)) { //$NON-NLS-1$
				didSpecifyDeprecation = true;
				this.options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
				continue;
			}
			if (currentArg.equals("-help") || currentArg.equals("-?")) { //$NON-NLS-1$ //$NON-NLS-2$
				printUsageRequired = true;
				continue;
			}
			if (currentArg.equals("-noExit")) { //$NON-NLS-1$
				mode = Default;
				this.systemExitWhenFinished = false;
				continue;
			}
			if (currentArg.equals("-verbose")) { //$NON-NLS-1$
				mode = Default;
				this.verbose = true;
				continue;
			}
			if (currentArg.equals("-referenceInfo")) { //$NON-NLS-1$
				mode = Default;
				this.produceRefInfo = true;
				continue;
			}
			if (currentArg.equals("-inlineJSR")) { //$NON-NLS-1$
			    mode = Default;
				this.options.put(
						CompilerOptions.OPTION_InlineJsr,
						CompilerOptions.ENABLED);
			    continue;
			}
			if (currentArg.startsWith("-g")) { //$NON-NLS-1$
				mode = Default;
				String debugOption = currentArg;
				int length = currentArg.length();
				if (length == 2) {
					this.options.put(
						CompilerOptions.OPTION_LocalVariableAttribute,
						CompilerOptions.GENERATE);
					this.options.put(
						CompilerOptions.OPTION_LineNumberAttribute,
						CompilerOptions.GENERATE);
					this.options.put(
						CompilerOptions.OPTION_SourceFileAttribute,
						CompilerOptions.GENERATE);
					continue;
				}
				if (length > 3) {
					this.options.put(
						CompilerOptions.OPTION_LocalVariableAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					this.options.put(
						CompilerOptions.OPTION_LineNumberAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					this.options.put(
						CompilerOptions.OPTION_SourceFileAttribute,
						CompilerOptions.DO_NOT_GENERATE);
					if (length == 7 && debugOption.equals("-g:none")) //$NON-NLS-1$
						continue;
					StringTokenizer tokenizer =
						new StringTokenizer(debugOption.substring(3, debugOption.length()), ","); //$NON-NLS-1$
					while (tokenizer.hasMoreTokens()) {
						String token = tokenizer.nextToken();
						if (token.equals("vars")) { //$NON-NLS-1$
							this.options.put(
								CompilerOptions.OPTION_LocalVariableAttribute,
								CompilerOptions.GENERATE);
						} else if (token.equals("lines")) { //$NON-NLS-1$
							this.options.put(
								CompilerOptions.OPTION_LineNumberAttribute,
								CompilerOptions.GENERATE);
						} else if (token.equals("source")) { //$NON-NLS-1$
							this.options.put(
								CompilerOptions.OPTION_SourceFileAttribute,
								CompilerOptions.GENERATE);
						} else {
							throw new InvalidInputException(
								Main.bind("configure.invalidDebugOption", debugOption)); //$NON-NLS-1$
						}
					}
					continue;
				}
				throw new InvalidInputException(
					Main.bind("configure.invalidDebugOption", debugOption)); //$NON-NLS-1$
			}
			if (currentArg.startsWith("-nowarn")) { //$NON-NLS-1$
				disableWarnings();
				mode = Default;
				continue;
			}
			if (currentArg.startsWith("-warn")) { //$NON-NLS-1$
				mode = Default;
				String warningOption = currentArg;
				int length = currentArg.length();
				if (length == 10 && warningOption.equals("-warn:none")) { //$NON-NLS-1$
					disableWarnings();
					continue;
				}
				if (length <= 6) {
					throw new InvalidInputException(
						Main.bind("configure.invalidWarningConfiguration", warningOption)); //$NON-NLS-1$
				}
				int warnTokenStart;
				boolean isEnabling;
				switch (warningOption.charAt(6)) {
					case '+' : 
						warnTokenStart = 7;
						isEnabling = true;
						break;
					case '-' :
						warnTokenStart = 7;
						isEnabling = false; // mentionned warnings are disabled
						break;
					default:
						warnTokenStart = 6;
						// clear default warning level
						// but allow multiple warning option on the command line
						if (!didSpecifyWarnings) disableWarnings();
						isEnabling = true;
				}
			
				StringTokenizer tokenizer =
					new StringTokenizer(warningOption.substring(warnTokenStart, warningOption.length()), ","); //$NON-NLS-1$
				int tokenCounter = 0;

				if (didSpecifyDeprecation) {  // deprecation could have also been set through -deprecation option
					this.options.put(CompilerOptions.OPTION_ReportDeprecation, CompilerOptions.WARNING);
				}
				
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					tokenCounter++;
					if (token.equals("constructorName")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportMethodWithConstructorName,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("pkgDefaultMethod") || token.equals("packageDefaultMethod")/*backward compatible*/ ) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportOverridingPackageDefaultMethod,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("maskedCatchBlock") || token.equals("maskedCatchBlocks")/*backward compatible*/) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportHiddenCatchBlock,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("deprecation")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportDeprecation, 
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
						this.options.put(
							CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, 
							CompilerOptions.DISABLED);
					} else if (token.equals("allDeprecation")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportDeprecation, 
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
						this.options.put(
							CompilerOptions.OPTION_ReportDeprecationInDeprecatedCode, 
							isEnabling ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
						this.options.put(
							CompilerOptions.OPTION_ReportDeprecationWhenOverridingDeprecatedMethod, 
							isEnabling ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
					} else if (token.equals("unusedLocal") || token.equals("unusedLocals")/*backward compatible*/) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportUnusedLocal, 
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unusedArgument") || token.equals("unusedArguments")/*backward compatible*/) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportUnusedParameter,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unusedImport") || token.equals("unusedImports")/*backward compatible*/) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportUnusedImport,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unusedPrivate")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportUnusedPrivateMember,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("localHiding")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportLocalVariableHiding,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("fieldHiding")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportFieldHiding,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("specialParamHiding")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportSpecialParameterHidingField,
							isEnabling ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
					} else if (token.equals("conditionAssign")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportPossibleAccidentalBooleanAssignment,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
   					} else if (token.equals("syntheticAccess")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportSyntheticAccessEmulation,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("nls")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportNonExternalizedStringLiteral,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("staticReceiver")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportNonStaticAccessToStatic,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("indirectStatic")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportIndirectStaticAccess,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("noEffectAssign")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportNoEffectAssignment,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("intfNonInherited") || token.equals("interfaceNonInherited")/*backward compatible*/) { //$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportIncompatibleNonInheritedInterfaceMethod,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("charConcat") || token.equals("noImplicitStringConversion")/*backward compatible*/) {//$NON-NLS-1$ //$NON-NLS-2$
						this.options.put(
							CompilerOptions.OPTION_ReportNoImplicitStringConversion,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("semicolon")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportEmptyStatement,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("serial")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportMissingSerialVersion,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("emptyBlock")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportUndocumentedEmptyBlock,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("uselessTypeCheck")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportUnnecessaryTypeCheck,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unchecked") || token.equals("unsafe")) {//$NON-NLS-1$ //$NON-NLS-2$ 
						this.options.put(
							CompilerOptions.OPTION_ReportUncheckedTypeOperation,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("finalBound")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportFinalParameterBound,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("suppress")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_SuppressWarnings,
							isEnabling ? CompilerOptions.ENABLED : CompilerOptions.DISABLED);
					} else if (token.equals("warningToken")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportUnhandledWarningToken,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unnecessaryElse")) {//$NON-NLS-1$ 
						this.options.put(
							CompilerOptions.OPTION_ReportUnnecessaryElse,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("javadoc")) {//$NON-NLS-1$ 
						if (!useEnableJavadoc) {
							this.options.put(
								CompilerOptions.OPTION_DocCommentSupport,
								isEnabling ? CompilerOptions.ENABLED: CompilerOptions.DISABLED);
						}
						// if disabling then it's not necessary to set other javadoc options
						if (isEnabling) {
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadoc,
								CompilerOptions.WARNING);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTags,
								CompilerOptions.ENABLED);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTagsDeprecatedRef,
								CompilerOptions.DISABLED);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTagsNotVisibleRef,
								CompilerOptions.DISABLED);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility,
								CompilerOptions.PRIVATE);
							this.options.put(
								CompilerOptions.OPTION_ReportMissingJavadocTags,
								CompilerOptions.WARNING);
							this.options.put(
								CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility,
								CompilerOptions.PRIVATE);
						}
					} else if (token.equals("allJavadoc")) { //$NON-NLS-1$
						if (!useEnableJavadoc) {
							this.options.put(
								CompilerOptions.OPTION_DocCommentSupport,
								isEnabling ? CompilerOptions.ENABLED: CompilerOptions.DISABLED);
						}
						// if disabling then it's not necessary to set other javadoc options
						if (isEnabling) {
							this.options.put(
							CompilerOptions.OPTION_ReportInvalidJavadoc,
							CompilerOptions.WARNING);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTags,
								CompilerOptions.ENABLED);
							this.options.put(
								CompilerOptions.OPTION_ReportInvalidJavadocTagsVisibility,
								CompilerOptions.PRIVATE);
							this.options.put(
								CompilerOptions.OPTION_ReportMissingJavadocTags,
								CompilerOptions.WARNING);
							this.options.put(
								CompilerOptions.OPTION_ReportMissingJavadocTagsVisibility,
								CompilerOptions.PRIVATE);
							this.options.put(
								CompilerOptions.OPTION_ReportMissingJavadocComments,
								CompilerOptions.WARNING);
						}
					} else if (token.startsWith("tasks")) { //$NON-NLS-1$
						String taskTags = ""; //$NON-NLS-1$
						int start = token.indexOf('(');
						int end = token.indexOf(')');
						if (start >= 0 && end >= 0 && start < end){
							taskTags = token.substring(start+1, end).trim();
							taskTags = taskTags.replace('|',',');
						}
						if (taskTags.length() == 0){
							throw new InvalidInputException(Main.bind("configure.invalidTaskTag", token)); //$NON-NLS-1$
						}
						this.options.put(
							CompilerOptions.OPTION_TaskTags,
							isEnabling ? taskTags : "");  //$NON-NLS-1$
					} else if (token.equals("assertIdentifier")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportAssertIdentifier,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("enumIdentifier")) { //$NON-NLS-1$
						this.options.put(
								CompilerOptions.OPTION_ReportEnumIdentifier,
								isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("finally")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportFinallyBlockNotCompletingNormally,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unusedThrown")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportUnusedDeclaredThrownException,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("unqualifiedField")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportUnqualifiedFieldAccess,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("typeHiding")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportTypeParameterHiding,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);
					} else if (token.equals("varargsCast")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportVarargsArgumentNeedCast,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("null")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportNullReference,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("boxing")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportAutoboxing,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("over-ann")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportMissingOverrideAnnotation,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("dep-ann")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportMissingDeprecatedAnnotation,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("intfAnnotation")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportAnnotationSuperInterface,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else if (token.equals("enumSwitch")) { //$NON-NLS-1$
						this.options.put(
							CompilerOptions.OPTION_ReportIncompleteEnumSwitch,
							isEnabling ? CompilerOptions.WARNING : CompilerOptions.IGNORE);						
					} else {
						throw new InvalidInputException(Main.bind("configure.invalidWarning", token)); //$NON-NLS-1$
					}
				}
				if (tokenCounter == 0)
					throw new InvalidInputException(
						Main.bind("configure.invalidWarningOption", currentArg)); //$NON-NLS-1$
				didSpecifyWarnings = true;
				continue;
			}
			if (currentArg.equals("-target")) { //$NON-NLS-1$
				mode = TargetSetting;
				continue;
			}
			if (currentArg.equals("-preserveAllLocals")) { //$NON-NLS-1$
				this.options.put(
					CompilerOptions.OPTION_PreserveUnusedLocal,
					CompilerOptions.PRESERVE);
				continue;
			}
			if (currentArg.equals("-enableJavadoc")) {//$NON-NLS-1$
				this.options.put(
					CompilerOptions.OPTION_DocCommentSupport,
					CompilerOptions.ENABLED);
				useEnableJavadoc = true;
				continue;
			}
			if (mode == TargetSetting) {
				if (didSpecifyTarget) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateTarget", currentArg));//$NON-NLS-1$
				}				
				didSpecifyTarget = true;
				if (currentArg.equals("1.1")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
				} else if (currentArg.equals("1.2")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
				} else if (currentArg.equals("1.3")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
					if (didSpecifyCompliance && CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Compliance)) < ClassFileConstants.JDK1_4) {
						throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForTarget", (String)this.options.get(CompilerOptions.OPTION_Compliance), CompilerOptions.VERSION_1_4)); //$NON-NLS-1$
					}
					this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
				} else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
					if (didSpecifyCompliance && CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Compliance)) < ClassFileConstants.JDK1_5) {
						throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForTarget", (String)this.options.get(CompilerOptions.OPTION_Compliance), CompilerOptions.VERSION_1_5)); //$NON-NLS-1$
					}
					this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
				} else {
					throw new InvalidInputException(Main.bind("configure.targetJDK", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideLog) {
				this.log = currentArg;
				mode = Default;
				continue;
			}
			if (mode == InsideRepetition) {
				try {
					this.repetitions = Integer.parseInt(currentArg);
					if (this.repetitions <= 0) {
						throw new InvalidInputException(Main.bind("configure.repetition", currentArg)); //$NON-NLS-1$
					}
				} catch (NumberFormatException e) {
					throw new InvalidInputException(Main.bind("configure.repetition", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideMaxProblems) {
				try {
					this.maxProblems = Integer.parseInt(currentArg);
					if (this.maxProblems <= 0) {
						throw new InvalidInputException(Main.bind("configure.maxProblems", currentArg)); //$NON-NLS-1$
					}
					this.options.put(CompilerOptions.OPTION_MaxProblemPerUnit, currentArg);
				} catch (NumberFormatException e) {
					throw new InvalidInputException(Main.bind("configure.maxProblems", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideSource) {
				if (didSpecifySource) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateSource", currentArg));//$NON-NLS-1$
				}				
				didSpecifySource = true;
				if (currentArg.equals("1.3")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
				} else if (currentArg.equals("1.4")) { //$NON-NLS-1$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_4);
				} else if (currentArg.equals("1.5") || currentArg.equals("5") || currentArg.equals("5.0")) { //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
					this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
				} else {
					throw new InvalidInputException(Main.bind("configure.source", currentArg)); //$NON-NLS-1$
				}
				mode = Default;
				continue;
			}
			if (mode == InsideDefaultEncoding) {
				if (didSpecifyDefaultEncoding) {
					throw new InvalidInputException(
						Main.bind("configure.duplicateDefaultEncoding", currentArg)); //$NON-NLS-1$
				}
				try { // ensure encoding is supported
					new InputStreamReader(new ByteArrayInputStream(new byte[0]), currentArg);
				} catch (UnsupportedEncodingException e) {
					throw new InvalidInputException(
						Main.bind("configure.unsupportedEncoding", currentArg)); //$NON-NLS-1$
				}
				this.options.put(CompilerOptions.OPTION_Encoding, currentArg);
				didSpecifyDefaultEncoding = true;
				mode = Default;
				continue;
			}
			if (mode == InsideDestinationPath) {
				this.destinationPath = currentArg;
				mode = Default;
				continue;
			}
			if (mode == InsideClasspath) {
				StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator);
				while (tokenizer.hasMoreTokens()) {
					int length;
					if ((length = this.classpaths.length) <= pathCount) {
						System.arraycopy(
							this.classpaths,
							0,
							(this.classpaths = new String[length * 2]),
							0,
							length);
					}
					this.classpaths[pathCount++] = tokenizer.nextToken();
				}
				mode = Default;
				continue;
			}
			if (mode == InsideBootClasspath) {
				StringTokenizer tokenizer = new StringTokenizer(currentArg, File.pathSeparator);
				while (tokenizer.hasMoreTokens()) {
					int length;
					if ((length = bootclasspaths.length) <= bootclasspathCount) {
						System.arraycopy(
							bootclasspaths,
							0,
							(bootclasspaths = new String[length * 2]),
							0,
							length);
					}
					bootclasspaths[bootclasspathCount++] = tokenizer.nextToken();
				}
				mode = Default;
				continue;
			}			
			//default is input directory
			currentArg = currentArg.replace('/', File.separatorChar);
			if (currentArg.endsWith(File.separator))
				currentArg =
					currentArg.substring(0, currentArg.length() - File.separator.length());
			File dir = new File(currentArg);
			if (!dir.isDirectory())
				throw new InvalidInputException(
					Main.bind("configure.directoryNotExist", currentArg)); //$NON-NLS-1$
			FileFinder finder = new FileFinder();
			try {
				finder.find(dir, SUFFIX_STRING_JAVA, this.verbose); //$NON-NLS-1$
			} catch (Exception e) {
				throw new InvalidInputException(Main.bind("configure.IOError", currentArg)); //$NON-NLS-1$
			}
			if (this.filenames != null) {
				// some source files were specified explicitly
				String results[] = finder.resultFiles;
				int length = results.length;
				System.arraycopy(
					this.filenames,
					0,
					(this.filenames = new String[length + filesCount]),
					0,
					filesCount);
				System.arraycopy(
					this.encodings,
					0,
					(this.encodings = new String[length + filesCount]),
					0,
					filesCount);
				System.arraycopy(results, 0, this.filenames, filesCount, length);
				for (int i = 0; i < length; i++) {
					this.encodings[filesCount + i] = customEncoding;
				}
				filesCount += length;
				customEncoding = null;
			} else {
				this.filenames = finder.resultFiles;
				filesCount = this.filenames.length;
				this.encodings = new String[filesCount];
				for (int i = 0; i < filesCount; i++) {
					this.encodings[i] = customEncoding;
				}
				customEncoding = null;
			}
			mode = Default;
			continue;
		}
		

		if (this.log != null) {
			this.logger.setLog(this.log);
		} else {
			this.showProgress = false;
		}
		
		if (printUsageRequired || filesCount == 0) {
			printUsage();
			this.proceed = false;
			return;
		}
		if (printVersionRequired) {
			printVersion();
		}

		if (filesCount != 0)
			System.arraycopy(
				this.filenames,
				0,
				(this.filenames = new String[filesCount]),
				0,
				filesCount);
		if (pathCount == 0) {
			// no user classpath specified.
			String classProp = System.getProperty("java.class.path"); //$NON-NLS-1$
			if ((classProp == null) || (classProp.length() == 0)) {
				this.logger.logNoClasspath(); //$NON-NLS-1$
				classProp = System.getProperty("user.dir"); //$NON-NLS-1$
			}
			StringTokenizer tokenizer = new StringTokenizer(classProp, File.pathSeparator);
			this.classpaths = new String[tokenizer.countTokens() + 1];
			while (tokenizer.hasMoreTokens()) {
				this.classpaths[pathCount++] = tokenizer.nextToken();
			}
			this.classpaths[pathCount++] = System.getProperty("user.dir");//$NON-NLS-1$
		}
		
		if (bootclasspathCount == 0) {
			/* no bootclasspath specified
			 * we can try to retrieve the default librairies of the VM used to run
			 * the batch compiler
			 */
			 String javaversion = System.getProperty("java.version");//$NON-NLS-1$
			 if (javaversion != null && javaversion.equalsIgnoreCase("1.1.8")) { //$NON-NLS-1$
				this.logger.logWrongJDK(); //$NON-NLS-1$
				this.proceed = false;
				return;
			 }

		 	/*
		 	 * Handle >= JDK 1.2.2 settings: retrieve rt.jar
		 	 */
		 	 String javaHome = System.getProperty("java.home");//$NON-NLS-1$
		 	 if (javaHome != null) {
		 	 	File javaHomeFile = new File(javaHome);
		 	 	if (javaHomeFile.exists()) {
					try {
						javaHomeFile = new File(javaHomeFile.getCanonicalPath());
						// add all jars in the lib subdirectory
						File[] directoriesToCheck = new File[] { new File(javaHomeFile, "lib"), new File(javaHomeFile, "lib/ext")};//$NON-NLS-1$//$NON-NLS-2$
						File[][] systemLibrariesJars = getLibrariesFiles(directoriesToCheck);
						if (systemLibrariesJars != null) {
							int length = getLength(systemLibrariesJars);
							bootclasspaths = new String[length];
							for (int i = 0, max = systemLibrariesJars.length; i < max; i++) {
								File[] current = systemLibrariesJars[i];
								if (current != null) {
									for (int j = 0, max2 = current.length; j < max2; j++) {
										bootclasspaths[bootclasspathCount++] = current[j].getAbsolutePath();
									}
								}
							}
						}
					} catch (IOException e) {
						// cannot retrieve libraries
					}
		 	 	}
		 	 }
		}

		if (this.classpaths == null) {
			this.classpaths = new String[0];
		}
		/* 
		 * We put the bootclasspath at the beginning of the classpath entries
		 */
		String[] newclasspaths = null;
		if ((pathCount + bootclasspathCount) != this.classpaths.length) {
			newclasspaths = new String[pathCount + bootclasspathCount];
		} else {
			newclasspaths = this.classpaths;
		}
		System.arraycopy(
			this.classpaths,
			0,
			newclasspaths,
			bootclasspathCount,
			pathCount);

		if (bootclasspathCount != 0) {
			System.arraycopy(
				bootclasspaths,
				0,
				newclasspaths,
				0,
				bootclasspathCount);
		}
		this.classpaths = newclasspaths;
		for (int i = 0, max = this.classpaths.length; i < max; i++) {
			File file = new File(this.classpaths[i]);
			if (!file.exists()) { // signal missing classpath entry file
				this.logger.logIncorrectClasspath(this.classpaths[i]); //$NON-NLS-1$
			}
		}
		if (this.destinationPath == null) {
			this.generatePackagesStructure = false;
		} else if ("none".equals(this.destinationPath)) { //$NON-NLS-1$
			this.destinationPath = null;
		}
		
		if (didSpecifyCompliance) {
			Object version = this.options.get(CompilerOptions.OPTION_Compliance);
			if (CompilerOptions.VERSION_1_3.equals(version)) {
				if (!didSpecifySource) this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
				if (!didSpecifyTarget) this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
			} else if (CompilerOptions.VERSION_1_4.equals(version)) {
				if (!didSpecifySource) this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_3);
				if (!didSpecifyTarget) this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
			} else if (CompilerOptions.VERSION_1_5.equals(version)) {
				if (!didSpecifySource) this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_5);
				if (!didSpecifyTarget) this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
			}
		}
		if (didSpecifySource) {
			Object version = this.options.get(CompilerOptions.OPTION_Source);
			 if (CompilerOptions.VERSION_1_4.equals(version)) {
				if (!didSpecifyCompliance) this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_4);
				if (!didSpecifyTarget) this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
			} else if (CompilerOptions.VERSION_1_5.equals(version)) {
				if (!didSpecifyCompliance) this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_5);
				if (!didSpecifyTarget) this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
			}
		}

		// check and set compliance/source/target compatibilities
		if (didSpecifyTarget) {
			// target must be 1.5 if source is 1.5
			if (CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Source)) >= ClassFileConstants.JDK1_5
					&& CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_TargetPlatform)) < ClassFileConstants.JDK1_5){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleTargetForSource", (String)this.options.get(CompilerOptions.OPTION_TargetPlatform), CompilerOptions.VERSION_1_5)); //$NON-NLS-1$
			}
	   		 // target must be 1.4 if source is 1.4
	   		if (CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Source)) >= ClassFileConstants.JDK1_4
					&& CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_TargetPlatform)) < ClassFileConstants.JDK1_4){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleTargetForSource", (String)this.options.get(CompilerOptions.OPTION_TargetPlatform), CompilerOptions.VERSION_1_4)); //$NON-NLS-1$
	   		}
			// target cannot be greater than compliance level
			if (CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Compliance)) < CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_TargetPlatform))){ 
				throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForTarget", (String)this.options.get(CompilerOptions.OPTION_Compliance), (String)this.options.get(CompilerOptions.OPTION_TargetPlatform))); //$NON-NLS-1$
			}
		}

		// compliance must be 1.5 if source is 1.5
		if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_5)
				&& CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Compliance)) < ClassFileConstants.JDK1_5) {
			throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForSource", (String)this.options.get(CompilerOptions.OPTION_Compliance), CompilerOptions.VERSION_1_5)); //$NON-NLS-1$
		} else 
			// compliance must be 1.4 if source is 1.4
			if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)
					&& CompilerOptions.versionToJdkLevel(this.options.get(CompilerOptions.OPTION_Compliance)) < ClassFileConstants.JDK1_4) { 
				throw new InvalidInputException(Main.bind("configure.incompatibleComplianceForSource", (String)this.options.get(CompilerOptions.OPTION_Compliance), CompilerOptions.VERSION_1_4)); //$NON-NLS-1$
		}
		// set default target according to compliance & sourcelevel.
		if (!didSpecifyTarget) {
			if (this.options.get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_3)) {
				this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_1);
			} else if (this.options.get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_4)) {
				if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_3)) {
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
				} else if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)) {
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
				}
			} else if (this.options.get(CompilerOptions.OPTION_Compliance).equals(CompilerOptions.VERSION_1_5)) {
				if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_3)) {
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_2);
				} else if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_4)) {
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_4);
				} else if (this.options.get(CompilerOptions.OPTION_Source).equals(CompilerOptions.VERSION_1_5)) {
					this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_5);
				}
			}
		}
		this.logger.logCommandLineArguments(newCommandLineArgs);
		this.logger.logOptions(this.options);
		this.logger.logClasspath(this.classpaths);
		if (this.repetitions == 0) {
			this.repetitions = 1;
		}
		if (this.repetitions >= 3 && this.timing) {
			this.times = new long[this.repetitions];
			this.timesCounter = 0;
		}
	}

	private void disableWarnings() {
		Object[] entries = this.options.entrySet().toArray();
		for (int i = 0, max = entries.length; i < max; i++) {
			Map.Entry entry = (Map.Entry) entries[i];
			if (!(entry.getKey() instanceof String))
				continue;
			if (!(entry.getValue() instanceof String))
				continue;
			if (((String) entry.getValue()).equals(CompilerOptions.WARNING)) {
				this.options.put(entry.getKey(), CompilerOptions.IGNORE);
			}
		}
		this.options.put(CompilerOptions.OPTION_TaskTags, ""); //$NON-NLS-1$
	}

	public String extractDestinationPathFromSourceFile(CompilationResult result) {
		ICompilationUnit compilationUnit = result.compilationUnit;
		if (compilationUnit != null) {
			char[] fileName = compilationUnit.getFileName();
			int lastIndex = CharOperation.lastIndexOf(java.io.File.separatorChar, fileName);
			if (lastIndex == -1) {
				return System.getProperty("user.dir"); //$NON-NLS-1$
			}
			return new String(fileName, 0, lastIndex);
		}
		return System.getProperty("user.dir"); //$NON-NLS-1$
	}
	/*
	 * Answer the component to which will be handed back compilation results from the compiler
	 */
	public ICompilerRequestor getBatchRequestor() {
		return new ICompilerRequestor() {
			int lineDelta = 0;
			public void acceptResult(CompilationResult compilationResult) {
				if (compilationResult.lineSeparatorPositions != null) {
					int unitLineCount = compilationResult.lineSeparatorPositions.length;
					Main.this.lineCount += unitLineCount;
					this.lineDelta += unitLineCount;
					if (Main.this.showProgress && this.lineDelta > 2000) {
						// in -log mode, dump a dot every 2000 lines compiled
						Main.this.logger.logProgress();
						this.lineDelta = 0;
					}
				}
				Main.this.logger.startLoggingSource(compilationResult);
				if (compilationResult.hasProblems() || compilationResult.hasTasks()) {
					int localErrorCount = Main.this.logger.logProblems(compilationResult.getAllProblems(), compilationResult.compilationUnit.getContents(), Main.this);
					// exit?
					if (Main.this.systemExitWhenFinished && !Main.this.proceedOnError && (localErrorCount > 0)) {
						Main.this.logger.endLoggingSource();
						Main.this.logger.printStats(Main.this);
						Main.this.logger.flush();
						Main.this.logger.close();
						System.exit(-1);
					}
				}
				outputClassFiles(compilationResult);
				Main.this.logger.endLoggingSource();
			}
		};
	}
	/*
	 *  Build the set of compilation source units
	 */
	public CompilationUnit[] getCompilationUnits()
		throws InvalidInputException {
		int fileCount = this.filenames.length;
		CompilationUnit[] units = new CompilationUnit[fileCount];
		HashtableOfObject knownFileNames = new HashtableOfObject(fileCount);

		String defaultEncoding = (String) this.options.get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$

		for (int i = 0; i < fileCount; i++) {
			char[] charName = this.filenames[i].toCharArray();
			if (knownFileNames.get(charName) != null)
				throw new InvalidInputException(Main.bind("unit.more", this.filenames[i])); //$NON-NLS-1$
			knownFileNames.put(charName, charName);
			File file = new File(this.filenames[i]);
			if (!file.exists())
				throw new InvalidInputException(Main.bind("unit.missing", this.filenames[i])); //$NON-NLS-1$
			String encoding = this.encodings[i];
			if (encoding == null)
				encoding = defaultEncoding;
			units[i] = new CompilationUnit(null, this.filenames[i], encoding);
		}
		return units;
	}
	
	private File[][] getLibrariesFiles(File[] files) {
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(File dir, String name) {
				String lowerCaseName = name.toLowerCase();
				if (lowerCaseName.endsWith(SUFFIX_STRING_jar) || lowerCaseName.endsWith(SUFFIX_STRING_zip)) {
					return true;
				}
				return false;
			}
		};
		final int filesLength = files.length;
		File[][] result = new File[filesLength][];
		for (int i = 0; i < filesLength; i++) {
			File currentFile = files[i];
			if (currentFile.exists() && currentFile.isDirectory()) {
				result[i] = currentFile.listFiles(filter);
			}
		}
		return result;
	}
	
	private int getLength(File[][] libraries) {
		int sum = 0;
		if (libraries != null) {
			for (int i = 0, max = libraries.length; i < max; i++) {
				final File[] currentFiles = libraries[i];
				if (currentFiles != null) {
					sum+= currentFiles.length;
				}
			}
		}
		return sum;
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public IErrorHandlingPolicy getHandlingPolicy() {

		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
		return new IErrorHandlingPolicy() {
			public boolean proceedOnErrors() {
				return Main.this.proceedOnError; // stop if there are some errors 
			}
			public boolean stopOnFirstError() {
				return false;
			}
		};
	}
	public FileSystem getLibraryAccess() {

		String defaultEncoding = (String) this.options.get(CompilerOptions.OPTION_Encoding);
		if ("".equals(defaultEncoding)) //$NON-NLS-1$
			defaultEncoding = null; //$NON-NLS-1$	
		return new FileSystem(this.classpaths, this.filenames, defaultEncoding);
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory(Locale.getDefault());
	}
	// Dump classfiles onto disk for all compilation units that where successfull.

	public void outputClassFiles(CompilationResult unitResult) {
		if (!((unitResult == null) || (unitResult.hasErrors() && !this.proceedOnError))) {
			ClassFile[] classFiles = unitResult.getClassFiles();
			if (!this.generatePackagesStructure) {
				this.destinationPath = extractDestinationPathFromSourceFile(unitResult);
			}
			if (this.destinationPath != null) {
				for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
					// retrieve the key and the corresponding classfile
					ClassFile classFile = classFiles[i];
					char[] filename = classFile.fileName();
					int length = filename.length;
					char[] relativeName = new char[length + 6];
					System.arraycopy(filename, 0, relativeName, 0, length);
					System.arraycopy(SUFFIX_class, 0, relativeName, length, 6);
					CharOperation.replace(relativeName, '/', File.separatorChar);
					String relativeStringName = new String(relativeName);
					try {
						if (this.compilerOptions.verbose)
							System.out.println(
								Messages.bind(
									Messages.compilation_write,
									new String[] {
										String.valueOf(this.exportedClassFilesCounter+1),
										relativeStringName
									}));
						ClassFile.writeToDisk(
							this.generatePackagesStructure,
							this.destinationPath,
							relativeStringName,
							classFile.getBytes());
						this.logger.logClassFile(
							this.generatePackagesStructure,
							this.destinationPath,
							relativeStringName);
					} catch (IOException e) {
						String fileName = this.destinationPath + relativeStringName;
						e.printStackTrace();
						this.logger.logNoClassFileCreated(fileName); //$NON-NLS-1$
					}
					this.exportedClassFilesCounter++;
				}
			}
		}
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public void performCompilation() throws InvalidInputException {

		this.startTime = System.currentTimeMillis();

		INameEnvironment environment = getLibraryAccess();
		Compiler batchCompiler =
			new Compiler(
				environment,
				getHandlingPolicy(),
				this.options,
				getBatchRequestor(),
				getProblemFactory());
		this.compilerOptions = batchCompiler.options;

		// set the non-externally configurable options.
		this.compilerOptions.verbose = this.verbose;
		this.compilerOptions.produceReferenceInfo = this.produceRefInfo;
		try {
			this.logger.startLoggingSources();
			batchCompiler.compile(getCompilationUnits());
		} finally {
			this.logger.endLoggingSources();
		}

		this.logger.printStats(this);
		
		// cleanup
		environment.cleanup();
	}
	
	public void printUsage() {
		this.logger.logUsage(Main.bind("misc.usage", //$NON-NLS-1$
			new String[] {
				System.getProperty("path.separator"), //$NON-NLS-1$
				Main.bind("compiler.name"), //$NON-NLS-1$
				Main.bind("compiler.version"), //$NON-NLS-1$
				Main.bind("compiler.copyright") //$NON-NLS-1$
			}
		));
		this.logger.flush();
	}
	public void printVersion() {
		this.logger.logVersion();  //$NON-NLS-1$
		this.logger.flush();
	}
}
