/*******************************************************************************
 * Copyright (c) 2026 Hélios GILLES and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Hélios GILLES - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import static org.eclipse.jdt.internal.core.JavaModelManager.trace;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.eclipse.jdt.core.IModuleDescription;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.CompilationResult;
import org.eclipse.jdt.internal.compiler.DefaultErrorHandlingPolicies;
import org.eclipse.jdt.internal.compiler.ast.ASTNode;
import org.eclipse.jdt.internal.compiler.ast.AbstractMethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.AccessRestriction;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.ICompilationUnit;
import org.eclipse.jdt.internal.compiler.env.ISourceType;
import org.eclipse.jdt.internal.compiler.env.NameEnvironmentAnswer;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.impl.ITypeRequestor;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.ModuleBinding;
import org.eclipse.jdt.internal.compiler.lookup.PackageBinding;
import org.eclipse.jdt.internal.compiler.parser.Parser;
import org.eclipse.jdt.internal.compiler.problem.DefaultProblemFactory;
import org.eclipse.jdt.internal.compiler.problem.ProblemReporter;
import org.eclipse.jdt.internal.core.DefaultWorkingCopyOwner;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.JavaProject;
import org.eclipse.jdt.internal.core.SourceTypeElementInfo;
import org.eclipse.jdt.internal.core.jdom.CompilationUnit;
import org.eclipse.jdt.internal.core.search.matching.JavaSearchNameEnvironment;
import org.eclipse.jdt.internal.core.search.processing.JobManager;
import org.eclipse.jdt.internal.core.util.ResourceCompilationUnit;

/**
 * What {@link SourceIndexer} needs to resolve the documents of a project: its name environment and a lookup
 * environment.
 * <p>
 * Both are costly to create and to fill. The index manager therefore keeps the last one, to resolve the next
 * documents of the same project as a compiler would resolve the units of a batch. It is used by one indexer at a
 * time: an indexer {@link #acquire acquires} it to resolve a document, and {@link #release releases} it when the
 * document is indexed.
 * </p>
 * <p>
 * A document is to be resolved as it would be with a new name environment and a new lookup environment:
 * </p>
 * <ul>
 * <li>the name environment is used as long as it is {@link #isUpToDate up to date};</li>
 * <li>the lookup environment is replaced by an empty one, see {@link #forgetTypes()}, when the types it knows
 * may not be the ones that a new one would find.</li>
 * </ul>
 */
public final class SourceIndexerEnvironment implements ITypeRequestor {

	/**
	 * The number of units after which the lookup environment is replaced, to limit what it retains. With 1, each
	 * document is resolved in its own lookup environment. Not final for testing purposes.
	 */
	public static int MAX_UNITS = 2000;

	/**
	 * Whether the index manager keeps an environment for the next document. If not, each document is resolved with
	 * a new name environment and a new lookup environment, as it was before environments were kept. Not final for
	 * testing purposes.
	 */
	public static boolean KEEP = true;

	private final JavaProject project;
	private final Map<String, String> projectOptions;
	private final org.eclipse.jdt.core.ICompilationUnit[] workingCopies;
	private final int changeCount;
	/** The name of the module of the project, or <code>null</code> if it is not a module */
	private final char[] moduleName;

	private final CompilerOptions options;
	private final ProblemReporter problemReporter;
	/** The parser of the unit being resolved: a parser keeps a state after a syntax error */
	private Parser parser;
	private final JavaSearchNameEnvironment nameEnvironment;
	private LookupEnvironment lookupEnvironment;
	/** The units that were parsed, without their method bodies, to resolve other units, by file name. */
	private final Map<String, CompilationUnitDeclaration> acceptedUnits = new HashMap<>();
	/** The file names of the units that were resolved */
	private final Set<String> resolvedUnits = new HashSet<>();
	/** Whether the types that the lookup environment knows may differ from the ones a new one would find */
	private boolean typesMayDiffer;

	private SourceIndexerEnvironment(JavaProject project, Map<String, String> projectOptions,
			org.eclipse.jdt.core.ICompilationUnit[] workingCopies, int changeCount) throws JavaModelException {
		this.project = project;
		this.projectOptions = projectOptions;
		this.workingCopies = workingCopies;
		this.changeCount = changeCount;
		IModuleDescription module = project.getModuleDescription();
		this.moduleName = module == null ? null : module.getElementName().toCharArray();
		this.options = new CompilerOptions(projectOptions);
		this.problemReporter = new ProblemReporter(
				DefaultErrorHandlingPolicies.proceedWithAllProblems(),
				this.options,
				new DefaultProblemFactory());
		// Use a non model name environment to avoid locks, monitors and such.
		this.nameEnvironment = new JavaSearchNameEnvironment(project, workingCopies);
		forgetTypes();
	}

	/**
	 * Replaces the lookup environment by an empty one, as a new environment has.
	 */
	private void forgetTypes() {
		this.lookupEnvironment = new LookupEnvironment(this, this.options, this.problemReporter, this.nameEnvironment);
		this.acceptedUnits.clear();
		this.resolvedUnits.clear();
		this.typesMayDiffer = false;
	}

	/**
	 * Returns the environment to resolve a document of the given project: the one that the index manager keeps if
	 * it is up to date, else a new one. The caller should {@link #release} it once done with the document.
	 */
	static SourceIndexerEnvironment acquire(IndexManager manager, JavaProject project) throws JavaModelException {
		SourceIndexerEnvironment environment = manager.sourceIndexerEnvironment.getAndSet(null);
		Map<String, String> projectOptions = project.getOptions(true);
		org.eclipse.jdt.core.ICompilationUnit[] workingCopies = JavaModelManager.getJavaModelManager()
				.getWorkingCopies(DefaultWorkingCopyOwner.PRIMARY, true/* add primary WCs */);
		int changeCount = manager.changeCount.get();
		if (environment != null && environment.isUpToDate(project, projectOptions, workingCopies, changeCount)) {
			return environment;
		}
		return new SourceIndexerEnvironment(project, projectOptions, workingCopies, changeCount);
	}

	/**
	 * Lets the index manager keep this environment for the next document, see {@link #KEEP}.
	 */
	void release(IndexManager manager) {
		if (!KEEP) {
			return;
		}
		if (this.typesMayDiffer) {
			forgetTypes();
		}
		manager.sourceIndexerEnvironment.set(this);
	}

	/**
	 * Returns whether the given index manager keeps an environment for the next document. For testing purposes.
	 */
	public static boolean isKept(IndexManager manager) {
		return manager.sourceIndexerEnvironment.get() != null;
	}

	/**
	 * A new environment answers the state of the project when it resolves a document. This one only does if
	 * nothing changed since it was created: the index manager counts the changes to the resources, to the
	 * classpaths and to the contents of the working copies.
	 */
	private boolean isUpToDate(JavaProject javaProject, Map<String, String> currentOptions,
			org.eclipse.jdt.core.ICompilationUnit[] currentWorkingCopies, int currentChangeCount) {
		return this.changeCount == currentChangeCount
				&& this.project.equals(javaProject)
				&& this.projectOptions.equals(currentOptions)
				&& Arrays.equals(this.workingCopies, currentWorkingCopies);
	}

	/**
	 * Parses and resolves the given unit, of which only the methods with functional expressions keep their
	 * statements. The unit is partly resolved only if its resolution fails.
	 */
	CompilationUnitDeclaration resolve(ICompilationUnit unit) {
		if (this.acceptedUnits.size() + this.resolvedUnits.size() >= MAX_UNITS) {
			forgetTypes();
		}
		boolean done = false;
		try {
			CompilationUnitDeclaration resolved = resolveWithKnownTypes(unit);
			if (resolved == null) {
				forgetTypes();
				resolved = resolveWithKnownTypes(unit);
			}
			if (this.lookupEnvironment.hasMissingTypes()) {
				// Where a type is missing, the resolution of a unit fails, or not, whether the type is already
				// known to be missing: do not keep the lookup environment, which knows it now.
				this.typesMayDiffer = true;
			}
			done = true;
			return resolved;
		} finally {
			if (!done) {
				// an unexpected failure
				this.typesMayDiffer = true;
			}
		}
	}

	/**
	 * @return the unit, or <code>null</code> if it cannot be resolved with the types that the lookup environment
	 *         knows as it would be with an empty one, which always resolves it
	 */
	private CompilationUnitDeclaration resolveWithKnownTypes(ICompilationUnit unit) {
		boolean isNew = this.acceptedUnits.isEmpty() && this.resolvedUnits.isEmpty();
		String fileName = new String(unit.getFileName());
		if (!this.resolvedUnits.add(fileName)) {
			return null;
		}
		// A normal parser: the indexing parser swallows several nodes
		this.parser = new Parser(this.problemReporter, false);
		this.parser.reportOnlyOneSyntaxError = true;
		this.parser.scanner.taskTags = null;
		CompilationUnitDeclaration parsedUnit = this.acceptedUnits.remove(fileName);
		boolean isAccepted = parsedUnit != null;
		if (isAccepted) {
			// The unit is known already, as another one refers to it: complete it, as a compiler does.
			if (!(parsedUnit.compilationResult.compilationUnit instanceof ResourceCompilationUnit)) {
				// a working copy, the contents of which may not be the indexed ones
				return null;
			}
			if (!hasAllTypeBindings(parsedUnit)) {
				// a type of the same name was known already, from another unit
				return null;
			}
		} else {
			ICompilationUnit unitInModule = inModule(unit);
			parsedUnit = this.parser.parse(unitInModule, new CompilationResult(unitInModule, 0, 0, this.options.maxProblemsPerUnit));
			if (!isAnswerOfNameEnvironment(parsedUnit, true)) {
				// do not keep the lookup environment, which is about to know the types of this unit
				this.typesMayDiffer = true;
			}
		}
		try {
			if (isAccepted) {
				this.parser.getMethodBodies(parsedUnit);
				reduceParseTree(parsedUnit);
			} else {
				reduceParseTree(parsedUnit);
				this.lookupEnvironment.buildTypeBindings(parsedUnit, null);
				if (!isNew && !hasAllTypeBindings(parsedUnit)) {
					// a type of the same name is known already, from another unit
					return null;
				}
				this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
			}
			if (parsedUnit.scope != null) {
				parsedUnit.scope.faultInTypes();
				parsedUnit.resolve();
			}
		} catch (RuntimeException e) {
			// e.g. a type that is not on the classpath
			this.typesMayDiffer = true;
			if (!isNew) {
				// it may fail later with an empty lookup environment, which does not know the missing types yet
				return null;
			}
			// what is resolved of the unit is indexed
			if (JobManager.VERBOSE) {
				trace("", e); //$NON-NLS-1$
			}
		}
		return parsedUnit;
	}

	/**
	 * The units that a document refers to are found in the module of their project. The document is to be resolved
	 * in its module too: as a unit of the unnamed module, it would not see the types of the module.
	 */
	private ICompilationUnit inModule(ICompilationUnit unit) {
		char[] name = this.moduleName;
		if (name == null) {
			return unit;
		}
		return new CompilationUnit(unit.getContents(), unit.getFileName()) {
			@Override
			public char[] getModuleName() {
				return name;
			}
		};
	}

	/**
	 * Returns whether the name environment answers the given unit for each of its types. It may answer another one
	 * if several units of the classpath declare a type of the same name, and none for a secondary type. Other units
	 * are then to refer to what the name environment answers, not to the types of this unit, which this environment
	 * knows once it has built them.
	 *
	 * @param isDocument whether the unit is a document, which is read from its resource: the name environment then
	 *                   has to answer the resource too, and not a working copy
	 */
	private boolean isAnswerOfNameEnvironment(CompilationUnitDeclaration unit, boolean isDocument) {
		if (unit.types != null) {
			ICompilationUnit compilationUnit = unit.compilationResult.compilationUnit;
			char[][] packageName = unit.currentPackage == null ? CharOperation.NO_CHAR_CHAR : unit.currentPackage.tokens;
			char[] module = compilationUnit.getModuleName() == null ? ModuleBinding.ANY : compilationUnit.getModuleName();
			for (TypeDeclaration type : unit.types) {
				NameEnvironmentAnswer answer = this.nameEnvironment.findType(type.name, packageName, module);
				ICompilationUnit answered = answer == null ? null : answer.getCompilationUnit();
				if (answered == null || !CharOperation.equals(answered.getFileName(), unit.getFileName())) {
					return false;
				}
				if (isDocument && !(answered instanceof ResourceCompilationUnit)) {
					return false;
				}
			}
		}
		return true;
	}

	private static boolean hasAllTypeBindings(CompilationUnitDeclaration unit) {
		if (unit.types != null) {
			for (TypeDeclaration type : unit.types) {
				if (type.binding == null) {
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * Called prior to the unit being resolved. Reduce the parse tree where possible.
	 */
	private static void reduceParseTree(CompilationUnitDeclaration unit) {
		// remove statements from methods that have no functional interface types.
		TypeDeclaration[] types = unit.types;
		for (int i = 0, l = types == null ? 0 : types.length; i < l; i++)
			purgeMethodStatements(types[i]);
	}

	private static void purgeMethodStatements(TypeDeclaration type) {
		AbstractMethodDeclaration[] methods = type.methods;
		for (int j = 0, length = methods == null ? 0 : methods.length; j < length; j++) {
			AbstractMethodDeclaration method = methods[j];
			/*
			 * In case the method defines a local type, skip purging the method body.
			 * We don't know if the local type defines a method that uses a method reference or a lambda.
			 * See:
			 *   https://github.com/eclipse-jdt/eclipse.jdt.core/issues/432
			 *   https://bugs.eclipse.org/bugs/show_bug.cgi?id=566435
			 */
			if (method != null && (method.bits & (ASTNode.HasFunctionalInterfaceTypes | ASTNode.HasLocalType)) == 0) {
				method.statements = null;
				method.javadoc = null;
			}
		}

		TypeDeclaration[] memberTypes = type.memberTypes;
		if (memberTypes != null)
			for (TypeDeclaration memberType : memberTypes)
				purgeMethodStatements(memberType);
	}

	@Override
	public void accept(IBinaryType binaryType, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		this.lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding, accessRestriction);
	}

	@Override
	public void accept(ICompilationUnit unit, AccessRestriction accessRestriction) {
		CompilationResult unitResult = new CompilationResult(unit, 1, 1, this.options.maxProblemsPerUnit);
		CompilationUnitDeclaration parsedUnit = this.parser.dietParse(unit, unitResult);
		if (parsedUnit.types != null && parsedUnit.types.length > 1 && !isAnswerOfNameEnvironment(parsedUnit, false)) {
			// The unit is the answer for one of its types, and not for another one, e.g. a secondary type.
			// Do not keep the lookup environment, which is about to know them all.
			this.typesMayDiffer = true;
		}
		this.lookupEnvironment.buildTypeBindings(parsedUnit, accessRestriction);
		this.lookupEnvironment.completeTypeBindings(parsedUnit, true);
		this.acceptedUnits.put(new String(unit.getFileName()), parsedUnit);
	}

	@Override
	public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding, AccessRestriction accessRestriction) {
		ISourceType sourceType = sourceTypes[0];
		while (sourceType.getEnclosingType() != null)
			sourceType = sourceType.getEnclosingType();
		SourceTypeElementInfo elementInfo = (SourceTypeElementInfo) sourceType;
		IType type = elementInfo.getHandle();
		ICompilationUnit sourceUnit = (ICompilationUnit) type.getCompilationUnit();
		accept(sourceUnit, accessRestriction);
	}
}
