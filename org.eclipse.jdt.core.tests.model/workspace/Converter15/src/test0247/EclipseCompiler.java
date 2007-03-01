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
package org.eclipse.jdt.internal.compiler.tool;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.Processor;
import javax.lang.model.SourceVersion;
import javax.tools.DiagnosticListener;
import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.StandardLocation;

import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.ClassFile;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.IErrorHandlingPolicy;
import org.eclipse.jdt.internal.compiler.IProblemFactory;
import org.eclipse.jdt.internal.compiler.batch.CompilationUnit;
import org.eclipse.jdt.internal.compiler.batch.FileSystem;
import org.eclipse.jdt.internal.compiler.batch.Main;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilationUnit;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemSeverities;
import org.eclipse.jdt.internal.compiler.util.Messages;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;

/**
 * Implementation of a batch compiler that supports the jsr199
 */
public class EclipseCompiler extends Main implements JavaCompiler {

	private HashMap<CompilationUnit, JavaFileObject> javaFileObjectMap;
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
	public DiagnosticListener<? super JavaFileObject> diagnosticListener;
	public JavaFileManager fileManager;
	protected Processor[] processors;

	public EclipseCompiler(PrintWriter out, PrintWriter err, boolean systemExitWhenFinished) {
		super(out, err, systemExitWhenFinished);
	}

	public EclipseCompiler() {
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
			}
			try {
				if (this.fileManager != null) {
					this.fileManager.flush();
				}
			} catch (IOException e) {
				// ignore
			}
		} catch (InvalidInputException e) {
			this.logger.logException(e);
			if (this.systemExitWhenFinished) {
				this.logger.flush();
				this.logger.close();
				System.exit(-1);
			}
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
			CompilationUnit compilationUnit = new CompilationUnit(null,
				name,
				null) {

				public char[] getContents() {
					try {
						return javaFileObject.getCharContent(true).toString().toCharArray();
					} catch(IOException e) {
						e.printStackTrace();
						throw new AbortCompilationUnit(null, e, null);
					}
				}
			};
			units.add(compilationUnit);
			this.javaFileObjectMap.put(compilationUnit, javaFileObject);
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

				DiagnosticListener<? super JavaFileObject> diagnosticListener = EclipseCompiler.this.diagnosticListener;
				if (diagnosticListener != null) {
					diagnosticListener.report(new Diagnostic<JavaFileObject>() {
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
								if (EclipseCompiler.this.fileManager.hasLocation(StandardLocation.SOURCE_PATH)) {
									return EclipseCompiler.this.fileManager.getJavaFileForInput(
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
	public StandardJavaFileManager getStandardFileManager(DiagnosticListener<? super JavaFileObject> diagnosticListener,
			Locale locale,
			Charset charset) {
		this.diagnosticListener = diagnosticListener;
		return new EclipseFileManager(this, locale, charset);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see javax.tools.JavaCompiler#getTask(java.io.Writer,
	 *      javax.tools.JavaFileManager, javax.tools.DiagnosticListener,
	 *      java.lang.Iterable, java.lang.Iterable, java.lang.Iterable)
	 */
	@SuppressWarnings("unchecked")
	public CompilationTask getTask(Writer out,
			JavaFileManager fileManager,
			DiagnosticListener<? super JavaFileObject> diagnosticListener,
			Iterable<String> options,
			Iterable<String> classes,
			Iterable<? extends JavaFileObject> compilationUnits) {

		for ()
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
		if (fileManager != null) {
			this.fileManager = fileManager;
		} else {
			this.fileManager = this.getStandardFileManager(diagnosticListener, null, null);
		}

		initialize(writerOut, writerErr, false);
		this.options.put(CompilerOptions.OPTION_Compliance, CompilerOptions.VERSION_1_6);
		this.options.put(CompilerOptions.OPTION_Source, CompilerOptions.VERSION_1_6);
		this.options.put(CompilerOptions.OPTION_TargetPlatform, CompilerOptions.VERSION_1_6);

		// TODO FIXME (olivier) REMOVE BEFORE 3.3 once the APT1.6 IS WORKING FINE
		for (String option : options ) {
			if ("-processorpath".equals(option) //$NON-NLS-1$
					|| ("-processor".equals(option))) { //$NON-NLS-1$
				this.options.put(CompilerOptions.OPTION_Process_Annotations, CompilerOptions.ENABLED);
			}
		}

		ArrayList<String> allOptions = new ArrayList<String>();
		if (options != null) {
			for (Iterator<String> iterator = options.iterator(); iterator.hasNext(); ) {
				this.fileManager.handleOption(iterator.next(), iterator);
			}
			for (String option : options) {
				allOptions.add(option);
			}
		}
		if (compilationUnits != null) {
			for (JavaFileObject javaFileObject : compilationUnits) {
				allOptions.add(new File(javaFileObject.toUri()).getAbsolutePath());
			}
		}

		final String[] optionsToProcess = new String[allOptions.size()];
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
				this.setDestinationPath(location.iterator().next().getAbsolutePath());
			}
		}

		return new CompilationTask() {
			private boolean hasRun = false;
			public Boolean call() {
				// set up compiler with passed options
				if (this.hasRun) {
					throw new IllegalStateException("This task has already been run"); //$NON-NLS-1$
				}
				Boolean value = EclipseCompiler.this.call() ? Boolean.TRUE : Boolean.FALSE;
				this.hasRun = true;
				return value;
			}
			public void setLocale(Locale locale) {
				EclipseCompiler.this.setLocale(locale);
			}
			public void setProcessors(Iterable<? extends Processor> processors) {
				ArrayList<Processor> temp = new ArrayList<Processor>();
				for (Processor processor : processors) {
					temp.add(processor);
				}
				Processor[] processors2 = new Processor[temp.size()];
				temp.toArray(processors2);
				EclipseCompiler.this.processors = processors2;
			}
		};
	}

	@Override
	@SuppressWarnings("unchecked")
	protected void initialize(PrintWriter outWriter, PrintWriter errWriter, boolean systemExit, Map customDefaultOptions) {
		super.initialize(outWriter, errWriter, systemExit, customDefaultOptions);
		this.javaFileObjectMap = new HashMap<CompilationUnit, JavaFileObject>();
	}

	@Override
	protected void initializeAnnotationProcessorManager() {
		super.initializeAnnotationProcessorManager();
		if (this.batchCompiler.annotationProcessorManager != null &&
				this.processors != null) {
			this.batchCompiler.annotationProcessorManager.setProcessors(this.processors);
		} else if (this.processors != null) {
			throw new UnsupportedOperationException("Cannot handle annotation processing"); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see javax.tools.OptionChecker#isSupportedOption(java.lang.String)
	 */
	public int isSupportedOption(String option) {
		return Options.processOptions(option);
	}

	// Dump classfiles onto disk for all compilation units that where successful
	// and do not carry a -d none spec, either directly or inherited from Main.
	public void outputClassFiles(CompilationResult unitResult) {
		if (!((unitResult == null) || (unitResult.hasErrors() && !this.proceedOnError))) {
			ClassFile[] classFiles = unitResult.getClassFiles();
			boolean generateClasspathStructure = this.fileManager.hasLocation(StandardLocation.CLASS_OUTPUT);
			String currentDestinationPath = this.destinationPath;
			File outputLocation = null;
			if (currentDestinationPath != null) {
				outputLocation = new File(currentDestinationPath);
				outputLocation.mkdirs();
			}
			for (int i = 0, fileCount = classFiles.length; i < fileCount; i++) {
				// retrieve the key and the corresponding classfile
				ClassFile classFile = classFiles[i];
				char[] filename = classFile.fileName();
				int length = filename.length;
				char[] relativeName = new char[length + 6];
				System.arraycopy(filename, 0, relativeName, 0, length);
				System.arraycopy(SuffixConstants.SUFFIX_class, 0, relativeName, length, 6);
				CharOperation.replace(relativeName, '/', File.separatorChar);
				String relativeStringName = new String(relativeName);
				if (this.compilerOptions.verbose) {
					EclipseCompiler.this.out.println(
						Messages.bind(
							Messages.compilation_write,
							new String[] {
								String.valueOf(this.exportedClassFilesCounter+1),
								relativeStringName
							}));
				}
				try {
					JavaFileObject javaFileForOutput =
					this.fileManager.getJavaFileForOutput(
							StandardLocation.CLASS_OUTPUT,
							new String(filename),
							JavaFileObject.Kind.CLASS,
							this.javaFileObjectMap.get(unitResult.compilationUnit));

					if (generateClasspathStructure) {
						if (currentDestinationPath != null) {
							int index = CharOperation.lastIndexOf(File.separatorChar, relativeName);
							if (index != -1) {
								File currentFolder = new File(currentDestinationPath, relativeStringName.substring(0, index));
								currentFolder.mkdirs();
							}
						} else {
							// create the subfolfers is necessary
							// need a way to retrieve the folders to create
							String path = javaFileForOutput.toUri().getPath();
							int index = path.lastIndexOf('/');
							if (index != -1) {
								File file = new File(path.substring(0, index));
								file.mkdirs();
							}
						}
					}

					OutputStream openOutputStream = javaFileForOutput.openOutputStream();
					BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(openOutputStream);
					bufferedOutputStream.write(classFile.header, 0, classFile.headerOffset);
					bufferedOutputStream.write(classFile.contents, 0, classFile.contentsOffset);
					bufferedOutputStream.flush();
					bufferedOutputStream.close();
				} catch (IOException e) {
					this.logger.logNoClassFileCreated(currentDestinationPath, relativeStringName, e);
				}
				LookupEnvironment env = EclipseCompiler.this.batchCompiler.lookupEnvironment;
				if (classFile.isShared) env.classFilePool.release(classFile);
				this.logger.logClassFile(
					generateClasspathStructure,
					currentDestinationPath,
					relativeStringName);
				this.exportedClassFilesCounter++;
			}
		}
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
	@SuppressWarnings("unchecked")
	protected void setPaths(ArrayList bootclasspaths,
			String sourcepathClasspathArg,
			ArrayList sourcepathClasspaths,
			ArrayList classpaths,
			ArrayList extdirsClasspaths,
			ArrayList endorsedDirClasspaths,
			String customEncoding) throws InvalidInputException {

		ArrayList<FileSystem.Classpath> fileSystemClasspaths = new ArrayList<FileSystem.Classpath>();
		EclipseFileManager javaFileManager = null;
		StandardJavaFileManager standardJavaFileManager = null;
		if (this.fileManager instanceof EclipseFileManager) {
			javaFileManager = (EclipseFileManager) this.fileManager;
		}
		if (this.fileManager instanceof StandardJavaFileManager) {
			standardJavaFileManager = (StandardJavaFileManager) this.fileManager;
		}

		if (javaFileManager != null) {
			if ((javaFileManager.flags & EclipseFileManager.HAS_ENDORSED_DIRS) == 0
					&& (javaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleEndorseddirs(null));
			}
		}
		Iterable<? extends File> location = null;
		if (standardJavaFileManager != null) {
			location = standardJavaFileManager.getLocation(StandardLocation.PLATFORM_CLASS_PATH);
		}
		if (location != null) {
			for (File file : location) {
				fileSystemClasspaths.add(FileSystem.getClasspath(
					file.getAbsolutePath(),
					null,
					null));
			}
		}
		if (javaFileManager != null) {
			if ((javaFileManager.flags & EclipseFileManager.HAS_EXT_DIRS) == 0
					&& (javaFileManager.flags & EclipseFileManager.HAS_BOOTCLASSPATH) != 0) {
				fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleExtdirs(null));
			}
		}
		if (standardJavaFileManager != null) {
			location = standardJavaFileManager.getLocation(StandardLocation.SOURCE_PATH);
		} else {
			location = null;
		}
		if (location != null) {
			for (File file : location) {
				fileSystemClasspaths.add(FileSystem.getClasspath(
					file.getAbsolutePath(),
					null,
					null));
			}
		}
		if (standardJavaFileManager != null) {
			location = standardJavaFileManager.getLocation(StandardLocation.CLASS_PATH);
		} else {
			location = null;
		}
		if (location != null) {
			for (File file : location) {
				fileSystemClasspaths.add(FileSystem.getClasspath(
					file.getAbsolutePath(),
					null,
					null));
			}
		}
		if (this.checkedClasspaths == null) {
			fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleBootclasspath(null, null));
			fileSystemClasspaths.addAll((ArrayList<? extends FileSystem.Classpath>) this.handleClasspath(null, null));
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
