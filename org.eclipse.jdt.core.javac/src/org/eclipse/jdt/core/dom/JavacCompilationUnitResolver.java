/*******************************************************************************
 * Copyright (c) 2023, Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.dom;

import java.io.File;
import java.io.IOException;
import java.nio.CharBuffer;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticListener;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.WorkingCopyOwner;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.jdt.internal.compiler.batch.FileSystem.Classpath;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.INameEnvironment;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.util.Util;
import org.eclipse.jdt.internal.core.CancelableNameEnvironment;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.dom.ICompilationUnitResolver;
import org.eclipse.jdt.internal.core.util.BindingKeyParser;
import org.eclipse.jdt.internal.javac.JavacProblemConverter;
import org.eclipse.jdt.internal.javac.JavacUtils;
import org.eclipse.jdt.internal.javac.UnusedProblemFactory;
import org.eclipse.jdt.internal.javac.UnusedTreeScanner;

import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.JavacTool;
import com.sun.tools.javac.api.MultiTaskListener;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.file.JavacFileManager;
import com.sun.tools.javac.main.Option;
import com.sun.tools.javac.parser.JavadocTokenizer;
import com.sun.tools.javac.parser.Scanner;
import com.sun.tools.javac.parser.ScannerFactory;
import com.sun.tools.javac.parser.Tokens.Comment.CommentStyle;
import com.sun.tools.javac.parser.Tokens.TokenKind;
import com.sun.tools.javac.tree.JCTree.JCClassDecl;
import com.sun.tools.javac.tree.JCTree.JCCompilationUnit;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.Options;

/**
 * Allows to create and resolve DOM ASTs using Javac
 * @implNote Cannot move to another package because parent class is package visible only
 */
public class JavacCompilationUnitResolver implements ICompilationUnitResolver {
	public JavacCompilationUnitResolver() {
		// 0-arg constructor
	}
	private interface GenericRequestor {
		public void acceptBinding(String bindingKey, IBinding binding);
	}
	private List<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> createSourceUnitList(String[] sourceFilePaths, String[] encodings) {
		// make list of source unit
		int length = sourceFilePaths.length;
		List<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> sourceUnitList = new ArrayList<>(length);
		for (int i = 0; i < length; i++) {
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit obj = createSourceUnit(sourceFilePaths[i], encodings[i]);
			if( obj != null )
				sourceUnitList.add(obj);
		}
		return sourceUnitList;
	}

	private org.eclipse.jdt.internal.compiler.env.ICompilationUnit createSourceUnit(String sourceFilePath, String encoding) {
		char[] contents = null;
		try {
			contents = Util.getFileCharContent(new File(sourceFilePath), encoding);
		} catch(IOException e) {
			return null;
		}
		if (contents == null) {
			return null;
		}
		return new org.eclipse.jdt.internal.compiler.batch.CompilationUnit(contents, sourceFilePath, encoding);
	}


	@Override
	public void resolve(String[] sourceFilePaths, String[] encodings, String[] bindingKeys, FileASTRequestor requestor,
			int apiLevel, Map<String, String> compilerOptions, List<Classpath> classpaths, int flags,
			IProgressMonitor monitor) {
		List<org.eclipse.jdt.internal.compiler.env.ICompilationUnit> sourceUnitList = createSourceUnitList(sourceFilePaths, encodings);
		JavacBindingResolver bindingResolver = null;

		// parse source units
		Map<org.eclipse.jdt.internal.compiler.env.ICompilationUnit, CompilationUnit> res =
				parse(sourceUnitList.toArray(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[]::new), apiLevel, compilerOptions, true, flags, (IJavaProject)null, null, monitor);

		for (var entry : res.entrySet()) {
			CompilationUnit cu = entry.getValue();
			requestor.acceptAST(new String(entry.getKey().getFileName()), cu);
			if (bindingResolver == null && (JavacBindingResolver)cu.ast.getBindingResolver() != null) {
				bindingResolver = (JavacBindingResolver)cu.ast.getBindingResolver();
			}
		}

		resolveRequestedBindingKeys(bindingResolver, bindingKeys,
				(a,b) -> requestor.acceptBinding(a,b),
				classpaths.stream().toArray(Classpath[]::new),
				new CompilerOptions(compilerOptions),
				res.values(), null, new HashMap<>(), monitor);
	}

	@Override
	public void resolve(ICompilationUnit[] compilationUnits, String[] bindingKeys, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, IJavaProject project, WorkingCopyOwner workingCopyOwner, int flags,
			IProgressMonitor monitor) {
		Map<ICompilationUnit, CompilationUnit> units = parse(compilationUnits, apiLevel, compilerOptions, true, flags, workingCopyOwner, monitor);
		if (requestor != null) {
			final JavacBindingResolver[] bindingResolver = new JavacBindingResolver[1];
			bindingResolver[0] = null;

			final Map<String, IBinding> bindingMap = new HashMap<>();
			{
				INameEnvironment environment = null;
				if (project instanceof JavaProject javaProject) {
					try {
						environment = new CancelableNameEnvironment(javaProject, workingCopyOwner, monitor);
					} catch (JavaModelException e) {
						// fall through
					}
				}
				if (environment == null) {
					environment = new NameEnvironmentWithProgress(new Classpath[0], null, monitor);
				}
				LookupEnvironment lu = new LookupEnvironment(new ITypeRequestor() {

					@Override
					public void accept(IBinaryType binaryType, PackageBinding packageBinding,
							AccessRestriction accessRestriction) {
						// do nothing
					}

					@Override
					public void accept(org.eclipse.jdt.internal.compiler.env.ICompilationUnit unit,
							AccessRestriction accessRestriction) {
						// do nothing
					}

					@Override
					public void accept(ISourceType[] sourceType, PackageBinding packageBinding,
							AccessRestriction accessRestriction) {
						// do nothing
					}

				}, new CompilerOptions(compilerOptions), null, environment);
				requestor.additionalBindingResolver = javacAdditionalBindingCreator(bindingMap, environment, lu, bindingResolver);
			}

			units.forEach((a,b) -> {
				if (bindingResolver[0] == null && (JavacBindingResolver)b.ast.getBindingResolver() != null) {
					bindingResolver[0] = (JavacBindingResolver)b.ast.getBindingResolver();
				}
				requestor.acceptAST(a,b);
				resolveBindings(b, bindingMap, apiLevel);
			});

			resolveRequestedBindingKeys(bindingResolver[0], bindingKeys,
					(a,b) -> requestor.acceptBinding(a,b),
					new Classpath[0], // TODO need some classpaths
					new CompilerOptions(compilerOptions),
					units.values(), project, bindingMap, monitor);
		} else {
			Iterator<CompilationUnit> it = units.values().iterator();
			while(it.hasNext()) {
				resolveBindings(it.next(), apiLevel);
			}
		}
	}

	private void resolveRequestedBindingKeys(JavacBindingResolver bindingResolver, String[] bindingKeys, GenericRequestor requestor,
			Classpath[] cp,CompilerOptions opts,
			Collection<CompilationUnit> units,
			IJavaProject project,
			Map<String, IBinding> bindingMap,
			IProgressMonitor monitor) {
		if (bindingResolver == null) {
			var compiler = ToolProvider.getSystemJavaCompiler();
			var context = new Context();
			JavacTask task = (JavacTask) compiler.getTask(null, null, null, List.of(), List.of(), List.of());
			bindingResolver = new JavacBindingResolver(null, task, context, new JavacConverter(null, null, context, null, true), null);
		}

		for (CompilationUnit cu : units) {
			cu.accept(new BindingBuilder(bindingMap));
		}

		INameEnvironment environment = null;
		if (project instanceof JavaProject javaProject) {
			try {
				environment = new CancelableNameEnvironment(javaProject, null, monitor);
			} catch (JavaModelException e) {
				// do nothing
			}
		}
		if (environment == null) {
			environment = new NameEnvironmentWithProgress(cp, null, monitor);
		}

		LookupEnvironment lu = new LookupEnvironment(new ITypeRequestor() {

			@Override
			public void accept(IBinaryType binaryType, PackageBinding packageBinding,
					AccessRestriction accessRestriction) {
				// do nothing
			}

			@Override
			public void accept(org.eclipse.jdt.internal.compiler.env.ICompilationUnit unit,
					AccessRestriction accessRestriction) {
				// do nothing
			}

			@Override
			public void accept(ISourceType[] sourceType, PackageBinding packageBinding,
					AccessRestriction accessRestriction) {
				// do nothing
			}

		}, opts, null, environment);

		// resolve the requested bindings
		for (String bindingKey : bindingKeys) {

			int arrayCount = Signature.getArrayCount(bindingKey);
			IBinding bindingFromMap = bindingMap.get(bindingKey);
			if (bindingFromMap != null) {
				// from parsed files
				requestor.acceptBinding(bindingKey, bindingFromMap);
			} else {

				if (arrayCount > 0) {
					String elementKey = Signature.getElementType(bindingKey);
					IBinding elementBinding = bindingMap.get(elementKey);
					if (elementBinding instanceof ITypeBinding elementTypeBinding) {
						requestor.acceptBinding(bindingKey, elementTypeBinding.createArrayType(arrayCount));
						continue;
					}
				}

				CustomBindingKeyParser bkp = new CustomBindingKeyParser(bindingKey);
				bkp.parse(true);
				char[][] name = bkp.compoundName;

//				// from ECJ
//				char[] charArrayFQN = Signature.toCharArray(bindingKey.toCharArray());
//				char[][] twoDimensionalCharArrayFQN = Stream.of(new String(charArrayFQN).split("/")) //
//						.map(myString -> myString.toCharArray()) //
//						.toArray(char[][]::new);
//				char[][] twoDimensionalCharArrayFQN = new char[][] {};
				NameEnvironmentAnswer answer = environment.findType(name);
				if( answer != null ) {
					IBinaryType binaryType = answer.getBinaryType();
					if (binaryType != null) {
						BinaryTypeBinding binding = lu.cacheBinaryType(binaryType, null);
						requestor.acceptBinding(bindingKey, new TypeBinding(bindingResolver, binding));
					}
				}
			}

		}

	}

	private static class CustomBindingKeyParser extends BindingKeyParser {

		private char[] secondarySimpleName;
		private char[][] compoundName;

		public CustomBindingKeyParser(String key) {
			super(key);
		}

		@Override
		public void consumeSecondaryType(char[] simpleTypeName) {
			this.secondarySimpleName = simpleTypeName;
		}

		@Override
		public void consumeFullyQualifiedName(char[] fullyQualifiedName) {
			this.compoundName = CharOperation.splitOn('/', fullyQualifiedName);
		}
	}

	@Override
	public void parse(ICompilationUnit[] compilationUnits, ASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {
		WorkingCopyOwner workingCopyOwner = Arrays.stream(compilationUnits)
					.filter(ICompilationUnit.class::isInstance)
					.map(ICompilationUnit.class::cast)
					.map(ICompilationUnit::getOwner)
					.filter(Objects::nonNull)
					.findFirst()
					.orElse(null);
		Map<ICompilationUnit, CompilationUnit>  units = parse(compilationUnits, apiLevel, compilerOptions, false, flags, workingCopyOwner, monitor);
		if (requestor != null) {
			units.forEach(requestor::acceptAST);
		}
	}

	private Map<ICompilationUnit, CompilationUnit> parse(ICompilationUnit[] compilationUnits, int apiLevel,
			Map<String, String> compilerOptions, boolean resolveBindings, int flags, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) {
		// TODO ECJCompilationUnitResolver has support for dietParse and ignore method body
		// is this something we need?
		if (compilationUnits.length > 0
			&& Arrays.stream(compilationUnits).map(ICompilationUnit::getJavaProject).distinct().count() == 1
			&& Arrays.stream(compilationUnits).allMatch(org.eclipse.jdt.internal.compiler.env.ICompilationUnit.class::isInstance)) {
			// all in same project, build together
			Map<ICompilationUnit, CompilationUnit> res =
				parse(Arrays.stream(compilationUnits)
						.map(org.eclipse.jdt.internal.compiler.env.ICompilationUnit.class::cast)
						.toArray(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[]::new),
					apiLevel, compilerOptions, resolveBindings, flags, compilationUnits[0].getJavaProject(), workingCopyOwner, monitor)
				.entrySet().stream().collect(Collectors.toMap(entry -> (ICompilationUnit)entry.getKey(), entry -> entry.getValue()));
			for (ICompilationUnit in : compilationUnits) {
				res.get(in).setTypeRoot(in);
			}
			return res;
		}
		// build individually
		Map<ICompilationUnit, CompilationUnit> res = new HashMap<>(compilationUnits.length, 1.f);
		for (ICompilationUnit in : compilationUnits) {
			if (in instanceof org.eclipse.jdt.internal.compiler.env.ICompilationUnit compilerUnit) {
				res.put(in, parse(new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] { compilerUnit },
						apiLevel, compilerOptions, resolveBindings, flags, in.getJavaProject(), workingCopyOwner, monitor).get(compilerUnit));
				res.get(in).setTypeRoot(in);
			}
		}
		return res;
	}

	@Override
	public void parse(String[] sourceFilePaths, String[] encodings, FileASTRequestor requestor, int apiLevel,
			Map<String, String> compilerOptions, int flags, IProgressMonitor monitor) {

		for( int i = 0; i < sourceFilePaths.length; i++ ) {
			org.eclipse.jdt.internal.compiler.env.ICompilationUnit ast = createSourceUnit(sourceFilePaths[i], encodings[i]);
			Map<org.eclipse.jdt.internal.compiler.env.ICompilationUnit, CompilationUnit> res =
					parse(new org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] {ast}, apiLevel, compilerOptions, false, flags, (IJavaProject)null, null, monitor);
			CompilationUnit result = res.get(ast);
			requestor.acceptAST(sourceFilePaths[i], result);
		}
	}


	private void resolveBindings(CompilationUnit unit, int apiLevel) {
		resolveBindings(unit, new HashMap<>(), apiLevel);
	}

	private void resolveBindings(CompilationUnit unit, Map<String, IBinding> bindingMap, int apiLevel) {
		try {
			if (unit.getPackage() != null) {
				IPackageBinding pb = unit.getPackage().resolveBinding();
				if (pb != null) {
					bindingMap.put(pb.getKey(), pb);
				}
			}
			if (!unit.types().isEmpty()) {
				List<AbstractTypeDeclaration> types = unit.types();
				for( int i = 0; i < types.size(); i++ ) {
					ITypeBinding tb = types.get(i).resolveBinding();
					if (tb != null) {
						bindingMap.put(tb.getKey(), tb);
					}
				}
			}
			if( apiLevel >= AST.JLS9_INTERNAL) {
				if (unit.getModule() != null) {
					IModuleBinding mb = unit.getModule().resolveBinding();
					if (mb != null) {
						bindingMap.put(mb.getKey(), mb);
					}
				}
			}
		} catch (Exception e) {
			ILog.get().warn("Failed to resolve binding", e);
		}
	}

	@Override
	public CompilationUnit toCompilationUnit(org.eclipse.jdt.internal.compiler.env.ICompilationUnit sourceUnit,
			boolean resolveBindings, IJavaProject project, List<Classpath> classpaths,
			int focalPoint, int apiLevel, Map<String, String> compilerOptions,
			WorkingCopyOwner workingCopyOwner, WorkingCopyOwner typeRootWorkingCopyOwner, int flags, IProgressMonitor monitor) {

		// collect working copies
		var workingCopies = JavaModelManager.getJavaModelManager().getWorkingCopies(workingCopyOwner, true);
		if (workingCopies == null) {
			workingCopies = new ICompilationUnit[0];
		}
		var pathToUnit = new HashMap<String, org.eclipse.jdt.internal.compiler.env.ICompilationUnit>();
		Arrays.stream(workingCopies) //
				.map(org.eclipse.jdt.internal.compiler.env.ICompilationUnit.class::cast) //
				.forEach(inMemoryCu -> {
					pathToUnit.put(new String(inMemoryCu.getFileName()), inMemoryCu);
				});

		// note that this intentionally overwrites an existing working copy entry for the same file
		pathToUnit.put(new String(sourceUnit.getFileName()), sourceUnit);

		// TODO currently only parse
		CompilationUnit res = parse(pathToUnit.values().toArray(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[]::new),
				apiLevel, compilerOptions, resolveBindings, flags, project, workingCopyOwner, monitor).get(sourceUnit);
		if (resolveBindings) {
			resolveBindings(res, apiLevel);
		}
		// For comparison
//		CompilationUnit res2  = CompilationUnitResolver.FACADE.toCompilationUnit(sourceUnit, initialNeedsToResolveBinding, project, classpaths, nodeSearcher, apiLevel, compilerOptions, typeRootWorkingCopyOwner, typeRootWorkingCopyOwner, flags, monitor);
//		//res.typeAndFlags=res2.typeAndFlags;
//		String res1a = res.toString();
//		String res2a = res2.toString();
//
//		AnnotationTypeDeclaration l1 = (AnnotationTypeDeclaration)res.types().get(0);
//		AnnotationTypeDeclaration l2 = (AnnotationTypeDeclaration)res2.types().get(0);
//		Object o1 = l1.bodyDeclarations().get(0);
//		Object o2 = l2.bodyDeclarations().get(0);
		return res;
	}

	private Map<org.eclipse.jdt.internal.compiler.env.ICompilationUnit, CompilationUnit> parse(org.eclipse.jdt.internal.compiler.env.ICompilationUnit[] sourceUnits, int apiLevel, Map<String, String> compilerOptions,
			boolean resolveBindings, int flags, IJavaProject javaProject, WorkingCopyOwner workingCopyOwner, IProgressMonitor monitor) {
		if (sourceUnits.length == 0) {
			return Collections.emptyMap();
		}
		var compiler = ToolProvider.getSystemJavaCompiler();
		Context context = new Context();
		Map<org.eclipse.jdt.internal.compiler.env.ICompilationUnit, CompilationUnit> result = new HashMap<>(sourceUnits.length, 1.f);
		Map<JavaFileObject, CompilationUnit> filesToUnits = new HashMap<>();
		final UnusedProblemFactory unusedProblemFactory = new UnusedProblemFactory(new DefaultProblemFactory(), compilerOptions);
		var problemConverter = new JavacProblemConverter(compilerOptions, context);
		DiagnosticListener<JavaFileObject> diagnosticListener = diagnostic -> {
			findTargetDOM(filesToUnits, diagnostic).ifPresent(dom -> {
				var newProblem = problemConverter.createJavacProblem(diagnostic);
				if (newProblem != null) {
					IProblem[] previous = dom.getProblems();
					IProblem[] newProblems = Arrays.copyOf(previous, previous.length + 1);
					newProblems[newProblems.length - 1] = newProblem;
					dom.setProblems(newProblems);
				}
			});
		};
		MultiTaskListener.instance(context).add(new TaskListener() {
			@Override
			public void finished(TaskEvent e) {
				if (e.getCompilationUnit() instanceof JCCompilationUnit u) {
					problemConverter.registerUnit(e.getSourceFile(), u);
				}

				if (e.getKind() == TaskEvent.Kind.ANALYZE) {
					final JavaFileObject file = e.getSourceFile();
					final CompilationUnit dom = filesToUnits.get(file);
					if (dom == null) {
						return;
					}

					final TypeElement currentTopLevelType = e.getTypeElement();
					UnusedTreeScanner<Void, Void> scanner = new UnusedTreeScanner<>() {
						@Override
						public Void visitClass(ClassTree node, Void p) {
							if (node instanceof JCClassDecl classDecl) {
								/**
								 * If a Java file contains multiple top-level types, it will
								 * trigger multiple ANALYZE taskEvents for the same compilation
								 * unit. Each ANALYZE taskEvent corresponds to the completion
								 * of analysis for a single top-level type. Therefore, in the
								 * ANALYZE task event listener, we only visit the class and nested
								 * classes that belong to the currently analyzed top-level type.
								 */
								if (Objects.equals(currentTopLevelType, classDecl.sym)
									|| !(classDecl.sym.owner instanceof PackageSymbol)) {
									return super.visitClass(node, p);
								} else {
									return null; // Skip if it does not belong to the currently analyzed top-level type.
								}
							}

							return super.visitClass(node, p);
						}
					};
					final CompilationUnitTree unit = e.getCompilationUnit();
					try {
						scanner.scan(unit, null);
					} catch (Exception ex) {
						ILog.get().error("Internal error when visiting the AST Tree. " + ex.getMessage(), ex);
					}

					List<CategorizedProblem> unusedProblems = scanner.getUnusedPrivateMembers(unusedProblemFactory);
					if (!unusedProblems.isEmpty()) {
						addProblemsToDOM(dom, unusedProblems);
					}

					List<CategorizedProblem> unusedImports = scanner.getUnusedImports(unusedProblemFactory);
					List<? extends Tree> topTypes = unit.getTypeDecls();
					int typeCount = topTypes.size();
					// Once all top level types of this Java file have been resolved,
					// we can report the unused import to the DOM.
					if (typeCount <= 1) {
						addProblemsToDOM(dom, unusedImports);
					} else if (typeCount > 1 && topTypes.get(typeCount - 1) instanceof JCClassDecl lastType) {
						if (Objects.equals(currentTopLevelType, lastType.sym)) {
							addProblemsToDOM(dom, unusedImports);
						}
					}
				}
			}
		});
		// must be 1st thing added to context
		context.put(DiagnosticListener.class, diagnosticListener);
		boolean docEnabled = JavaCore.ENABLED.equals(compilerOptions.get(JavaCore.COMPILER_DOC_COMMENT_SUPPORT));
		JavacUtils.configureJavacContext(context, compilerOptions, javaProject, JavacUtils.isTest(javaProject, sourceUnits));
		Options.instance(context).put(Option.PROC, "only");
		var fileManager = (JavacFileManager)context.get(JavaFileManager.class);
		List<JavaFileObject> fileObjects = new ArrayList<>(); // we need an ordered list of them
		for (var sourceUnit : sourceUnits) {
			File unitFile;
			if (javaProject != null && javaProject.getResource() != null) {
				// path is relative to the workspace, make it absolute
				IResource asResource = javaProject.getProject().getParent().findMember(new String(sourceUnit.getFileName()));
				if (asResource != null) {
					unitFile = asResource.getLocation().toFile();
				} else {
					unitFile = new File(new String(sourceUnit.getFileName()));
				}
			} else {
				unitFile = new File(new String(sourceUnit.getFileName()));
			}
			Path sourceUnitPath;
			if (!unitFile.getName().endsWith(".java") || sourceUnit.getFileName() == null || sourceUnit.getFileName().length == 0) {
				sourceUnitPath = Path.of(new File("whatever.java").toURI());
			} else {
				sourceUnitPath = Path.of(unitFile.toURI());
			}
			var fileObject = fileManager.getJavaFileObject(sourceUnitPath);
			fileManager.cache(fileObject, CharBuffer.wrap(sourceUnit.getContents()));
			AST ast = createAST(compilerOptions, apiLevel, context, flags);
			CompilationUnit res = ast.newCompilationUnit();
			result.put(sourceUnit, res);
			filesToUnits.put(fileObject, res);
			fileObjects.add(fileObject);
		}


		JCCompilationUnit javacCompilationUnit = null;
		Iterable<String> options = configureAPIfNecessary(fileManager) ? null : Arrays.asList("-proc:none");
		JavacTask task = ((JavacTool)compiler).getTask(null, fileManager, null /* already added to context */, options, List.of() /* already set */, fileObjects, context);
		{
			// don't know yet a better way to ensure those necessary flags get configured
			var javac = com.sun.tools.javac.main.JavaCompiler.instance(context);
			javac.keepComments = true;
			javac.genEndPos = true;
			javac.lineDebugInfo = true;
		}

		try {
			var elements = task.parse().iterator();
			var aptPath = fileManager.getLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH);
			if ((flags & ICompilationUnit.FORCE_PROBLEM_DETECTION) != 0
				|| resolveBindings
				|| (aptPath != null && aptPath.iterator().hasNext())) {
				task.analyze();
			}

			Throwable cachedThrown = null;

			for (int i = 0 ; i < sourceUnits.length; i++) {
				if (elements.hasNext() && elements.next() instanceof JCCompilationUnit u) {
					javacCompilationUnit = u;
				} else {
					return Map.of();
				}
				try {
					String rawText = null;
					try {
						rawText = fileObjects.get(i).getCharContent(true).toString();
					} catch( IOException ioe) {
						// ignore
					}
					CompilationUnit res = result.get(sourceUnits[i]);
					AST ast = res.ast;
					JavacConverter converter = new JavacConverter(ast, javacCompilationUnit, context, rawText, docEnabled);
					converter.populateCompilationUnit(res, javacCompilationUnit);
					// javadoc problems explicitly set as they're not sent to DiagnosticListener (maybe find a flag to do it?)
					var javadocProblems = converter.javadocDiagnostics.stream()
							.map(problemConverter::createJavacProblem)
							.filter(Objects::nonNull)
							.toArray(IProblem[]::new);
					if (javadocProblems.length > 0) {
						int initialSize = res.getProblems().length;
						var newProblems = Arrays.copyOf(res.getProblems(), initialSize + javadocProblems.length);
						System.arraycopy(javadocProblems, 0, newProblems, initialSize, javadocProblems.length);
						res.setProblems(newProblems);
					}
					List<org.eclipse.jdt.core.dom.Comment> javadocComments = new ArrayList<>();
					res.accept(new ASTVisitor(true) {
						@Override
						public void postVisit(ASTNode node) { // fix some positions
							if( node.getParent() != null ) {
								if( node.getStartPosition() < node.getParent().getStartPosition()) {
									int parentEnd = node.getParent().getStartPosition() + node.getParent().getLength();
									if( node.getStartPosition() >= 0 ) {
										node.getParent().setSourceRange(node.getStartPosition(), parentEnd - node.getStartPosition());
									}
								}
							}
						}
						@Override
						public boolean visit(Javadoc javadoc) {
							javadocComments.add(javadoc);
							return true;
						}
					});
					addCommentsToUnit(javadocComments, res);
					addCommentsToUnit(converter.notAttachedComments, res);
					attachMissingComments(res, context, rawText, converter, compilerOptions);
					if ((flags & ICompilationUnit.ENABLE_STATEMENTS_RECOVERY) == 0) {
						// remove all possible RECOVERED node
						res.accept(new ASTVisitor(false) {
							private boolean reject(ASTNode node) {
								return (node.getFlags() & ASTNode.RECOVERED) != 0
									|| (node instanceof FieldDeclaration field && field.fragments().isEmpty())
									|| (node instanceof VariableDeclarationStatement decl && decl.fragments().isEmpty());
							}

							@Override
							public boolean preVisit2(ASTNode node) {
								if (reject(node)) {
									StructuralPropertyDescriptor prop = node.getLocationInParent();
									if ((prop instanceof SimplePropertyDescriptor simple && !simple.isMandatory())
										|| (prop instanceof ChildPropertyDescriptor child && !child.isMandatory())
										|| (prop instanceof ChildListPropertyDescriptor)) {
										node.delete();
									} else if (node.getParent() != null) {
										node.getParent().setFlags(node.getParent().getFlags() | ASTNode.RECOVERED);
									}
									return false; // branch will be cut, no need to inspect deeper
								}
								return true;
							}

							@Override
							public void postVisit(ASTNode node) {
								// repeat on postVisit so trimming applies bottom-up
								preVisit2(node);
							}
						});
					}
					if (resolveBindings) {
						JavacBindingResolver resolver = new JavacBindingResolver(javaProject, task, context, converter, workingCopyOwner);
						resolver.isRecoveringBindings = (flags & ICompilationUnit.ENABLE_BINDINGS_RECOVERY) != 0;
						ast.setBindingResolver(resolver);
					}
					//
					ast.setOriginalModificationCount(ast.modificationCount()); // "un-dirty" AST so Rewrite can process it
					ast.setDefaultNodeFlag(ast.getDefaultNodeFlag() & ~ASTNode.ORIGINAL);
				} catch (Throwable thrown) {
					if (cachedThrown == null) {
						cachedThrown = thrown;
					}
					ILog.get().error("Internal failure while parsing or converting AST for unit " + new String(sourceUnits[i].getFileName()));
					ILog.get().error(thrown.getMessage(), thrown);
				}
			}
			if (cachedThrown != null) {
				throw new RuntimeException(cachedThrown);
			}
			if (resolveBindings) {
				result.values().forEach(cu -> cu.getAST().resolveWellKnownType(Object.class.getName()));
			}
		} catch (IOException ex) {
			ILog.get().error(ex.getMessage(), ex);
		}

		return result;
	}

	private void addProblemsToDOM(CompilationUnit dom, Collection<CategorizedProblem> problems) {
		if (problems == null) {
			return;
		}
		IProblem[] previous = dom.getProblems();
		IProblem[] newProblems = Arrays.copyOf(previous, previous.length + problems.size());
		int start = previous.length;
		for (CategorizedProblem problem : problems) {
			newProblems[start] = problem;
			start++;
		}
		dom.setProblems(newProblems);
	}

	private Optional<CompilationUnit> findTargetDOM(Map<JavaFileObject, CompilationUnit> filesToUnits, Object obj) {
		if (obj == null) {
			return Optional.empty();
		}
		if (obj instanceof JavaFileObject o) {
			return Optional.ofNullable(filesToUnits.get(o));
		}
		if (obj instanceof DiagnosticSource source) {
			return findTargetDOM(filesToUnits, source.getFile());
		}
		if (obj instanceof Diagnostic diag) {
			return findTargetDOM(filesToUnits, diag.getSource());
		}
		return Optional.empty();
	}

	private AST createAST(Map<String, String> options, int level, Context context, int flags) {
		AST ast = AST.newAST(level, JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES)));
		ast.setFlag(flags);
		ast.setDefaultNodeFlag(ASTNode.ORIGINAL);
		String sourceModeSetting = options.get(JavaCore.COMPILER_SOURCE);
		long sourceLevel = CompilerOptions.versionToJdkLevel(sourceModeSetting);
		if (sourceLevel == 0) {
			// unknown sourceModeSetting
			sourceLevel = ClassFileConstants.getLatestJDKLevel();
		}
		ast.scanner.sourceLevel = sourceLevel;
		String compliance = options.get(JavaCore.COMPILER_COMPLIANCE);
		long complianceLevel = CompilerOptions.versionToJdkLevel(compliance);
		if (complianceLevel == 0) {
			// unknown sourceModeSetting
			complianceLevel = sourceLevel;
		}
		ast.scanner.complianceLevel = complianceLevel;
		ast.scanner.previewEnabled = JavaCore.ENABLED.equals(options.get(JavaCore.COMPILER_PB_ENABLE_PREVIEW_FEATURES));
		return ast;
	}

//
	/**
	 * Currently re-scans the doc to build the list of comments and then
	 * attach them to the already built AST.
	 * @param res
	 * @param context
	 * @param fileObject
	 * @param converter
	 * @param compilerOptions
	 */
	private void attachMissingComments(CompilationUnit unit, Context context, String rawText, JavacConverter converter, Map<String, String> compilerOptions) {
		ScannerFactory scannerFactory = ScannerFactory.instance(context);
		List<Comment> missingComments = new ArrayList<>();
		JavadocTokenizer commentTokenizer = new JavadocTokenizer(scannerFactory, rawText.toCharArray(), rawText.length()) {
			@Override
			protected com.sun.tools.javac.parser.Tokens.Comment processComment(int pos, int endPos, CommentStyle style) {
				// workaround Java bug 9077218
				if (style == CommentStyle.JAVADOC_BLOCK && endPos - pos <= 4) {
					style = CommentStyle.BLOCK;
				}
				var res = super.processComment(pos, endPos, style);
				if (noCommentAt(unit, pos)) { // not already processed
					var comment = converter.convert(res, pos, endPos);
					missingComments.add(comment);
				}
				return res;
			}
		};
		Scanner javacScanner = new Scanner(scannerFactory, commentTokenizer) {
			// subclass just to access constructor
			// TODO DefaultCommentMapper.this.scanner.linePtr == -1?
		};
		do { // consume all tokens to populate comments
			javacScanner.nextToken();
		} while (javacScanner.token() != null && javacScanner.token().kind != TokenKind.EOF);
		org.eclipse.jdt.internal.compiler.parser.Scanner ecjScanner = new ASTConverter(compilerOptions, false, null).scanner;
		ecjScanner.recordLineSeparator = true;
		ecjScanner.skipComments = false;
		try {
			ecjScanner.setSource(rawText.toCharArray());
			do {
				ecjScanner.getNextToken();
			} while (!ecjScanner.atEnd());
		} catch (InvalidInputException ex) {
			JavaCore.getPlugin().getLog().log(org.eclipse.core.runtime.Status.error(ex.getMessage(), ex));
		}

		// need to scan with ecjScanner first to populate some line indexes used by the CommentMapper
		// on longer-term, implementing an alternative comment mapper based on javac scanner might be best
		addCommentsToUnit(missingComments, unit);
		unit.initCommentMapper(ecjScanner);
	}

	static void addCommentsToUnit(Collection<Comment> comments, CompilationUnit res) {
		List<Comment> before = res.getCommentList() == null ? new ArrayList<>() : new ArrayList<>(res.getCommentList());
		comments.stream().filter(comment -> comment.getStartPosition() >= 0 && JavacCompilationUnitResolver.noCommentAt(res, comment.getStartPosition()))
			.forEach(before::add);
		before.sort(Comparator.comparingInt(Comment::getStartPosition));
		res.setCommentTable(before.toArray(Comment[]::new));
	}

	private static boolean noCommentAt(CompilationUnit unit, int pos) {
		if (unit.getCommentList() == null) {
			return true;
		}
		return ((List<Comment>)unit.getCommentList()).stream()
				.allMatch(other -> pos < other.getStartPosition() || pos >= other.getStartPosition() + other.getLength());
	}

	private static class BindingBuilder extends ASTVisitor {
		public Map<String, IBinding> bindingMap = new HashMap<>();

		public BindingBuilder(Map<String, IBinding> bindingMap) {
			this.bindingMap = bindingMap;
		}

		@Override
		public boolean visit(TypeDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(MethodDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(EnumDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(RecordDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(SingleVariableDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(VariableDeclarationFragment node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}

		@Override
		public boolean visit(AnnotationTypeDeclaration node) {
			IBinding binding = node.resolveBinding();
			if (binding != null) {
				bindingMap.putIfAbsent(binding.getKey(), binding);
			}
			return true;
		}
	}

	private static Function<String, IBinding> javacAdditionalBindingCreator(Map<String, IBinding> bindingMap, INameEnvironment environment, LookupEnvironment lu, BindingResolver[] bindingResolverPointer) {

		return key -> {

			{
				// check parsed files
				IBinding binding = bindingMap.get(key);
				if (binding != null) {
					return binding;
				}
			}

			// if the requested type is an array type,
			// check the parsed files for element type and create the array variant
			int arrayCount = Signature.getArrayCount(key);
			if (arrayCount > 0) {
				String elementKey = Signature.getElementType(key);
				IBinding elementBinding = bindingMap.get(elementKey);
				if (elementBinding instanceof ITypeBinding elementTypeBinding) {
					return elementTypeBinding.createArrayType(arrayCount);
				}
			}

			// check name environment
			CustomBindingKeyParser bkp = new CustomBindingKeyParser(key);
			bkp.parse(true);
			char[][] name = bkp.compoundName;
			NameEnvironmentAnswer answer = environment.findType(name);
			if (answer != null) {
				IBinaryType binaryType = answer.getBinaryType();
				if (binaryType != null) {
					BinaryTypeBinding binding = lu.cacheBinaryType(binaryType, null);
					return new TypeBinding(bindingResolverPointer[0], binding);
				}
			}

			return null;
		};
	}

	private boolean configureAPIfNecessary(JavacFileManager fileManager) {
		Iterable<? extends File> apPaths = fileManager.getLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH);
		if (apPaths != null) {
			return true;
		}

		Iterable<? extends File> apModulePaths = fileManager.getLocation(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH);
		if (apModulePaths != null) {
			return true;
		}

		Iterable<? extends File> classPaths = fileManager.getLocation(StandardLocation.CLASS_PATH);
		if (classPaths != null) {
			for(File cp : classPaths) {
				String fileName = cp.getName();
				if (fileName != null && fileName.startsWith("lombok") && fileName.endsWith(".jar")) {
					try {
						fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_PATH, List.of(cp));
						return true;
					} catch (IOException ex) {
						ILog.get().error(ex.getMessage(), ex);
					}
				}
			}
		}

		Iterable<? extends File> modulePaths = fileManager.getLocation(StandardLocation.MODULE_PATH);
		if (modulePaths != null) {
			for(File mp : modulePaths) {
				String fileName = mp.getName();
				if (fileName != null && fileName.startsWith("lombok") && fileName.endsWith(".jar")) {
					try {
						fileManager.setLocation(StandardLocation.ANNOTATION_PROCESSOR_MODULE_PATH, List.of(mp));
						return true;
					} catch (IOException ex) {
						ILog.get().error(ex.getMessage(), ex);
					}
				}
			}
		}

		return false;
	}
}
