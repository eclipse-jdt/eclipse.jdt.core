/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaCompilerTool;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;

/**
 * Implementation of a batch compiler that supports the jsr199.
 * Temporary support that handles old API (remove after 1.6b91)
 */
public class EclipseCompilerTool extends Main implements JavaCompilerTool {

	private static Set<SourceVersion> SupportedSourceVersions;
	static {
		// Eclipse compiler supports all possible versions from version 0 to
		// version 6
		// we don't care about the order
		EnumSet<SourceVersion> enumSet = EnumSet.range(SourceVersion.RELEASE_0, SourceVersion.RELEASE_6);
		// we don't want anybody to modify this list
		SupportedSourceVersions = Collections.unmodifiableSet(enumSet);
	}

	Iterable<? extends JavaFileObject> compilationUnits;
	DiagnosticListener<? super JavaFileObject> diagnosticListener;
	JavaFileManager fileManager;

	public EclipseCompilerTool(PrintWriter out, PrintWriter err, boolean systemExitWhenFinished) {
		super(out, err, systemExitWhenFinished);
	}
	
	public EclipseCompilerTool() {
		super(null, null, false);
	}

	public boolean call() {
		try {
			if (this.proceed) {
				this.globalProblemsCount = 0;
				this.globalErrorsCount = 0;
				this.globalWarningsCount = 0;
				this.globalTasksCount = 0;
				this.lineCount = 0;
				this.exportedClassFilesCounter = 0;
				// request compilation
				performCompilation();
			} else {
				return false;
			}
		} catch (InvalidInputException e) {
			this.logger.logException(e);
			return false;
		} catch(IllegalArgumentException e) {
			throw e;
		} catch (RuntimeException e) { // internal compiler failure
			this.logger.logException(e);
			return false;
		} finally {
			this.logger.flush();
			this.logger.close();
		}
		if (this.globalErrorsCount == 0)
			return true;
		return false;
	}

	public CompilationUnit[] getCompilationUnits() {
		ArrayList<CompilationUnit> units = new ArrayList<CompilationUnit>();
		for (final JavaFileObject javaFileObject : this.compilationUnits) {
			if (javaFileObject.getKind() != JavaFileObject.Kind.SOURCE) {
				throw new IllegalArgumentException();
			}
			String name = javaFileObject.getName();
			name = name.replace('\\', '/');
			int index = name.lastIndexOf('/');
			units.add(new CompilationUnit(null,
				index == -1 ? name : name.substring(index),
				null) {
				
				public char[] getContents() {
					try {
						return javaFileObject.getCharContent(true).toString().toCharArray();
					} catch(IOException e) {
						throw new AbortCompilationUnit(null, e, null);
					}
				}				
			});
		}
		CompilationUnit[] result = new CompilationUnit[units.size()];
		units.toArray(result);
		return result;
	}
	/*
	 *  Low-level API performing the actual compilation
	 */
	public IErrorHandlingPolicy getHandlingPolicy() {
		// passes the initial set of files to the batch oracle (to avoid finding more than once the same units when case insensitive match)	
		return new IErrorHandlingPolicy() {
			public boolean proceedOnErrors() {
				return false; // stop if there are some errors 
			}
			public boolean stopOnFirstError() {
				return false;
			}
		};
	}

	public IProblemFactory getProblemFactory() {
		return new DefaultProblemFactory() {
			@Override
			public CategorizedProblem createProblem(
					final char[] originatingFileName,
					final int problemId,
					final String[] problemArguments,
					final String[] messageArguments,
					final int severity,
					final int startPosition,
					final int endPosition,
					final int lineNumber,
					final int columnNumber) {

				if (EclipseCompilerTool.this.diagnosticListener != null) {
    				EclipseCompilerTool.this.diagnosticListener.report(new Diagnostic<JavaFileObject>() {
    					public String getCode() {
    						return Integer.toString(problemId);
    					}
    					public long getColumnNumber() {
    						return columnNumber;
    					}
    					public long getEndPosition() {
    						return endPosition;
    					}
    					public Kind getKind() {
    						if ((severity & ProblemSeverities.Error) != 0) {
    							return Diagnostic.Kind.ERROR;
    						}
    						if ((severity & ProblemSeverities.Optional) != 0) {
    							return Diagnostic.Kind.WARNING;
    						}
    						if ((severity & ProblemSeverities.Warning) != 0) {
    							return Diagnostic.Kind.MANDATORY_WARNING;
    						}
    						return Diagnostic.Kind.OTHER;
    					}
    					public long getLineNumber() {
                        	return lineNumber;
                        }
                        public String getMessage(Locale locale) {
                        	setLocale(locale);
                        	return getLocalizedMessage(problemId, problemArguments);
                        }
                        public long getPosition() {
                        	return startPosition;
                        }
                        public JavaFileObject getSource() {
    						try {
    							if (EclipseCompilerTool.this.fileManager.hasLocation(StandardLocation.SOURCE_PATH)) {
        							return EclipseCompilerTool.this.fileManager.getJavaFileForInput(
        									StandardLocation.SOURCE_PATH,
        									new String(originatingFileName),
        									JavaFileObject.Kind.SOURCE);
    							}
    						} catch (IOException e) {
    							// ignore
    						}
    						return null;
                        }
                        public long getStartPosition() {
    						return startPosition;
    					}
    				});
				}
   				return super.createProblem(originatingFileName, problemId, problemArguments, messageArguments, severity, startPosition, endPosition, lineNumber, columnNumber);
			}
		};
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.tools.Tool#getSourceVersions()
	 */
	public Set<SourceVersion> getSourceVersions() {
		return SupportedSourceVersions;
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.tools.JavaCompiler#getStandardFileManager(javax.tools.DiagnosticListener,
	 *      java.util.Locale, java.nio.charset.Charset)
	 */
	public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener) {
		this.diagnosticListener = diagnosticListener;
		return new OldEclipseFileManager(this, null, null);
	}
	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.tools.JavaCompiler#getTask(java.io.Writer,
	 *      javax.tools.JavaFileManager, javax.tools.DiagnosticListener,
	 *      java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	public CompilationTask getTask(Writer out,
			JavaFileManager fileManager,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			Iterable<String> options,
			Iterable<String> classes,
			Iterable<? extends JavaFileObject> compilationUnits) {
		
		PrintWriter writerOut = null;
		PrintWriter writerErr = null;
		if (out == null) {
			writerOut = new PrintWriter(System.out);
			writerErr = new PrintWriter(System.err);
		} else {
			writerOut = new PrintWriter(out);
			writerErr = new PrintWriter(out);
		}
		this.compilationUnits = compilationUnits;
		this.diagnosticListener = diagnosticListener;
		this.fileManager = fileManager;

		this.initialize(writerOut, writerErr, false);
		
		for (Iterator<String> iterator = options.iterator(); iterator.hasNext(); ) {
			fileManager.handleOption(iterator.next(), iterator);
		}
		
		ArrayList<String> allOptions = new ArrayList<String>();
		for (String option : options) {
			allOptions.add(option);
		}

		if (compilationUnits != null) {
    		for (JavaFileObject javaFileObject : compilationUnits) {
    			allOptions.add(new File(javaFileObject.toUri()).getAbsolutePath());
    		}
		}
		
		String[] optionsToProcess = new String[allOptions.size()];
		allOptions.toArray(optionsToProcess);
		try {
			this.configure(optionsToProcess);
		} catch (InvalidInputException e) {
			throw new RuntimeException(e);
		}
		
		if (this.fileManager instanceof StandardJavaFileManager) {
			StandardJavaFileManager javaFileManager = (StandardJavaFileManager) this.fileManager;

			Iterable<? extends File> location = javaFileManager.getLocation(StandardLocation.CLASS_OUTPUT);
			if (location != null) {
				this.destinationPath = location.iterator().next().getAbsolutePath();
			}
		}

		return new CompilationTask() {
			private boolean hasRun = false;
			private boolean result;
    		public boolean getResult() {
    			// set up compiler with passed options
    			if (!this.hasRun) {
    				run();
    			}
    			return this.result;
    		}
    		public void run() {
    			if (this.hasRun) return;
    			this.result = EclipseCompilerTool.this.call();
    			this.hasRun = true;
    		}
    		public void setProcessors(Iterable<? extends Processor> processors) {
    			throw new UnsupportedOperationException();
    		}
		};
	}	

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.tools.OptionChecker#isSupportedOption(java.lang.String)
	 */
	public int isSupportedOption(String option) {
		return Options.processOptions(option);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see javax.tools.Tool#run(java.io.InputStream, java.io.OutputStream,
	 *      java.io.OutputStream, java.lang.String[])
	 */
	public int run(InputStream in, OutputStream out, OutputStream err, String... arguments) {
		boolean succeed = new Main(new PrintWriter(new OutputStreamWriter(out)), new PrintWriter(new OutputStreamWriter(err)), true).compile(arguments);
		return succeed ? 0 : -1;
	}
	
	@Override
	protected void setPaths(ArrayList bootclasspaths,
			String sourcepathClasspathArg,
			ArrayList sourcepathClasspaths,
			ArrayList classpaths,
			ArrayList extdirsClasspaths,
			ArrayList endorsedDirClasspaths,
			String customEncoding) throws InvalidInputException {

		ArrayList<FileSystem.Classpath> fileSystemClasspaths = new ArrayList<FileSystem.Classpath>();
		if (this.fileManager instanceof EclipseFileManager) {
			EclipseFileManager javaFileManager = (EclipseFileManager) this.fileManager;

			if ((javaFileManager.flags & EclipseFileManager.HAS_ENDORSED_DIRS) == 0
					&& (javaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleEndorseddirs(null));
			}
			Iterable<? extends File> location = javaFileManager.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
			if (location != null) {
				for (File file : location) {
					fileSystemClasspaths.add(FileSystem.getClasspath(
	    				file.getAbsolutePath(),
	    				null,
	    				null));
				}
			}
			if ((javaFileManager.flags & EclipseFileManager.HAS_EXT_DIRS) == 0
					&& (javaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleExtdirs(null));
			}
			location = javaFileManager.getLocation(StandardLocation.SOURCE_PATH);
			if (location != null) {
				for (File file : location) {
					fileSystemClasspaths.add(FileSystem.getClasspath(
	    				file.getAbsolutePath(),
	    				null,
	    				null));
				}
			}
			location = javaFileManager.getLocation(StandardLocation.CLASS_PATH);
			if (location != null) {
				for (File file : location) {
					fileSystemClasspaths.add(FileSystem.getClasspath(
	    				file.getAbsolutePath(),
	    				null,
	    				null));
				}
			}
		}
		final int size = fileSystemClasspaths.size();
		if (size != 0) {
    		this.checkedClasspaths = new FileSystem.Classpath[size];
    		int i = 0;
    		for (FileSystem.Classpath classpath : fileSystemClasspaths) {
    			this.checkedClasspaths[i++] = classpath;
    		}
		}
	}
}
