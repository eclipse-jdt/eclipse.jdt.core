package org.eclipse.jdt.internal.compiler;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * This is the public entry point to resolve type hierarchies.
 *
 * When requesting additional types from the name environment, the resolver
 * accepts all forms (binary, source & compilation unit) for additional types.
 *
 * Side notes: Binary types already know their resolved supertypes so this
 * only makes sense for source types. Even though the compiler finds all binary
 * types to complete the hierarchy of a given source type, is there any reason
 * why the requestor should be informed that binary type X subclasses Y &
 * implements I & J?
 */

import org.eclipse.jdt.internal.compiler.env.*;
import org.eclipse.jdt.internal.compiler.impl.*;
import org.eclipse.jdt.internal.compiler.ast.*;
import org.eclipse.jdt.internal.compiler.lookup.*;
import org.eclipse.jdt.internal.compiler.parser.*;
import org.eclipse.jdt.internal.compiler.problem.*;
import org.eclipse.jdt.internal.compiler.util.*;

import java.util.Locale;

public class HierarchyResolver implements ITypeRequestor {
	IHierarchyRequestor requestor;
	LookupEnvironment lookupEnvironment;

	private int typeIndex;
	private IGenericType[] typeModels;
	private ReferenceBinding[] typeBindings;
public HierarchyResolver(
	INameEnvironment nameEnvironment,
	IErrorHandlingPolicy policy,
	ConfigurableOption[] settings,
	IHierarchyRequestor requestor,
	IProblemFactory problemFactory) {

	// create a problem handler given a handling policy
	CompilerOptions options = settings == null ? new CompilerOptions() : new CompilerOptions(settings);
	ProblemReporter problemReporter = new ProblemReporter(policy, options, problemFactory);
	this.lookupEnvironment = new LookupEnvironment(this, options, problemReporter, nameEnvironment);
	this.requestor = requestor;

	this.typeIndex = -1;
	this.typeModels = new IGenericType[5];
	this.typeBindings = new ReferenceBinding[5];
}
public HierarchyResolver(INameEnvironment nameEnvironment, IHierarchyRequestor requestor, IProblemFactory problemFactory) {
	this(
		nameEnvironment,
		DefaultErrorHandlingPolicies.exitAfterAllProblems(),
		null,
		requestor,
		problemFactory);
}
/**
 * Add an additional binary type
 */

public void accept(IBinaryType binaryType, PackageBinding packageBinding) {
	BinaryTypeBinding typeBinding = lookupEnvironment.createBinaryTypeFrom(binaryType, packageBinding);
	remember(binaryType, typeBinding);
}
/**
 * Add an additional compilation unit.
 */

public void accept(ICompilationUnit sourceUnit) {
	//System.out.println("Cannot accept compilation units inside the HierarchyResolver.");
	lookupEnvironment.problemReporter.abortDueToInternalError(
		new StringBuffer(Util.bind("accept.cannot"/*nonNLS*/))
			.append(sourceUnit.getFileName())
			.toString());
}
/**
 * Add additional source types
 */

public void accept(ISourceType[] sourceTypes, PackageBinding packageBinding) {
	CompilationResult result = new CompilationResult(sourceTypes[0].getFileName(), 1, 1);
	CompilationUnitDeclaration unit =
		SourceTypeConverter.buildCompilationUnit(sourceTypes, false, true, lookupEnvironment.problemReporter, result);

	if (unit != null) {
		lookupEnvironment.buildTypeBindings(unit);
		rememberWithMemberTypes(sourceTypes[0], unit.types[0].binding);

		lookupEnvironment.completeTypeBindings(unit, false);
	}
}
private void remember(IGenericType suppliedType, ReferenceBinding typeBinding) {
	if (typeBinding == null) return;

	if (++typeIndex == typeModels.length) {
		System.arraycopy(typeModels, 0, typeModels = new IGenericType[typeIndex * 2], 0, typeIndex);
		System.arraycopy(typeBindings, 0, typeBindings = new ReferenceBinding[typeIndex * 2], 0, typeIndex);
	}
	typeModels[typeIndex] = suppliedType;
	typeBindings[typeIndex] = typeBinding;
}
private void rememberWithMemberTypes(TypeDeclaration typeDeclaration, HierarchyType enclosingType, ICompilationUnit unit) {

	if (typeDeclaration.binding == null) return;

	HierarchyType hierarchyType = new HierarchyType(
		enclosingType, 
		!typeDeclaration.isInterface(),
		typeDeclaration.name,
		typeDeclaration.binding.modifiers,
		unit);
	remember(hierarchyType, typeDeclaration.binding);

	// propagate into member types
	if (typeDeclaration.memberTypes == null) return;
	MemberTypeDeclaration[] memberTypes = typeDeclaration.memberTypes;
	for (int i = 0, max = memberTypes.length; i < max; i++){
		rememberWithMemberTypes(memberTypes[i], hierarchyType, unit);
	}
}
private void rememberWithMemberTypes(ISourceType suppliedType, ReferenceBinding typeBinding) {
	if (typeBinding == null) return;

	remember(suppliedType, typeBinding);

	ISourceType[] memberTypes = suppliedType.getMemberTypes();
	if (memberTypes == null) return;
	for (int m = memberTypes.length; --m >= 0;) {
		ISourceType memberType = memberTypes[m];
		rememberWithMemberTypes(memberType, typeBinding.getMemberType(memberType.getName()));
	}
}
private void reportHierarchy() {
	// ensure each binary type knows its supertypes before reporting the hierarchy
	int problemLength = typeIndex+1;
	boolean[] typesWithProblem = new boolean[problemLength];
	for (int current = 0; current <= typeIndex; current++) { // typeIndex may continue to grow
		ReferenceBinding typeBinding = typeBindings[current];
		if (typeBinding.isBinaryBinding()) {
			// fault in its hierarchy...
			try {
				typeBinding.superclass();
				typeBinding.superInterfaces();
			} catch (AbortCompilation e) {
				if (current >= problemLength) {
					System.arraycopy(typesWithProblem, 0, typesWithProblem = new boolean[current+1], 0, problemLength);
					problemLength = current+1;
				}
				typesWithProblem[current] = true;
			}
		}
	}

	for (int current = typeIndex; current >= 0; current--) {
		IGenericType suppliedType = typeModels[current];
		ReferenceBinding typeBinding = typeBindings[current];
		if (current < problemLength && typesWithProblem[current]) continue;

		ReferenceBinding superBinding = typeBinding.superclass();
		IGenericType superclass = null;
		if (superBinding != null) {
			for (int t = typeIndex; t >= 0; t--) {
				if (typeBindings[t] == superBinding) {
					superclass = typeModels[t];
					break;
				}
			}
		}

		ReferenceBinding[] interfaceBindings = typeBinding.superInterfaces();
		int length = interfaceBindings.length;
		IGenericType[] superinterfaces = new IGenericType[length];
		next : for (int i = 0; i < length; i++) {
			ReferenceBinding interfaceBinding = interfaceBindings[i];
			for (int t = typeIndex; t >= 0; t--) {
				if (typeBindings[t] == interfaceBinding) {
					superinterfaces[i] = typeModels[t];
					continue next;
				}
			}
		}
		if (typeBinding.isInterface()){ // do not connect interfaces to Object
			superclass = null;
		}
		requestor.connect(suppliedType, superclass, superinterfaces);
	}
}
private void reset(){
	lookupEnvironment.reset();

	this.typeIndex = -1;
	this.typeModels = new IGenericType[5];
	this.typeBindings = new ReferenceBinding[5];
}
/**
 * Resolve the supertypes for the supplied source types.
 * Inform the requestor of the resolved supertypes for each
 * supplied source type using:
 *    connect(ISourceType suppliedType, IGenericType superclass, IGenericType[] superinterfaces)
 *
 * Also inform the requestor of the supertypes of each
 * additional requested super type which is also a source type
 * instead of a binary type.
 */

public void resolve(IGenericType[] suppliedTypes) {
	resolve(suppliedTypes, null);
}
/**
 * Resolve the supertypes for the supplied source types.
 * Inform the requestor of the resolved supertypes for each
 * supplied source type using:
 *    connect(ISourceType suppliedType, IGenericType superclass, IGenericType[] superinterfaces)
 *
 * Also inform the requestor of the supertypes of each
 * additional requested super type which is also a source type
 * instead of a binary type.
 */

public void resolve(IGenericType[] suppliedTypes, ICompilationUnit[] sourceUnits) {
	try {
		int suppliedLength = suppliedTypes == null ? 0 : suppliedTypes.length;
		int sourceLength = sourceUnits == null ? 0 : sourceUnits.length;
		CompilationUnitDeclaration[] units = new CompilationUnitDeclaration[suppliedLength + sourceLength];
		int count = -1;
		for (int i = 0; i < suppliedLength; i++) {
			if (suppliedTypes[i].isBinaryType()) {
				IBinaryType binaryType = (IBinaryType) suppliedTypes[i];
				suppliedTypes[i] = null; // no longer needed pass this point
				try {
					remember(binaryType, lookupEnvironment.cacheBinaryType(binaryType, false));
				} catch (AbortCompilation e) {
					// classpath problem for this type: ignore
				}
			} else {
				// must start with the top level type
				ISourceType topLevelType = (ISourceType) suppliedTypes[i];
				suppliedTypes[i] = null; // no longer needed pass this point				
				while (topLevelType.getEnclosingType() != null)
					topLevelType = topLevelType.getEnclosingType();
				CompilationResult result = new CompilationResult(topLevelType.getFileName(), i, suppliedLength);
				units[++count] = SourceTypeConverter.buildCompilationUnit(new ISourceType[]{topLevelType}, false, true, lookupEnvironment.problemReporter, result);

				if (units[count] == null) {
					count--;
				} else {
					try {
						lookupEnvironment.buildTypeBindings(units[count]);
						rememberWithMemberTypes(topLevelType, units[count].types[0].binding);
					} catch (AbortCompilation e) {
						// classpath problem for this type: ignore
					}
				}
			}
		}
		for (int i = 0; i < sourceLength; i++){
			ICompilationUnit sourceUnit = sourceUnits[i];
			sourceUnits[i] = null; // no longer needed pass this point
			CompilationResult unitResult = new CompilationResult(sourceUnit, suppliedLength+i, suppliedLength+sourceLength); 
			CompilerOptions options = new CompilerOptions(Compiler.getDefaultOptions(Locale.getDefault()));
			Parser parser = new Parser(lookupEnvironment.problemReporter, false, options.getAssertMode());
			CompilationUnitDeclaration parsedUnit = parser.dietParse(sourceUnit, unitResult);
			if (parsedUnit != null) {
				units[++count] = parsedUnit;
				lookupEnvironment.buildTypeBindings(units[count]);
				int typeCount = parsedUnit.types == null ? 0 : parsedUnit.types.length;
				for (int j = 0; j < typeCount; j++){
					rememberWithMemberTypes(parsedUnit.types[j], null, sourceUnit);
				}
			}
		}
		for (int i = 0; i <= count; i++)
			lookupEnvironment.completeTypeBindings(units[i], false);

		reportHierarchy();
		
	} catch (ClassCastException e){ // work-around for 1GF5W1S - can happen in case duplicates are fed to the hierarchy with binaries hiding sources
	} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
	} finally {
		reset();
	}
}
/**
 * Resolve the supertypes for the supplied source type.
 * Inform the requestor of the resolved supertypes using:
 *    connect(ISourceType suppliedType, IGenericType superclass, IGenericType[] superinterfaces)
 */

public void resolve(IGenericType suppliedType) {
	try {
		if (suppliedType.isBinaryType()) {
			remember(suppliedType, lookupEnvironment.cacheBinaryType((IBinaryType) suppliedType));
		} else {
			// must start with the top level type
			ISourceType topLevelType = (ISourceType) suppliedType;
			while (topLevelType.getEnclosingType() != null)
				topLevelType = topLevelType.getEnclosingType();
			CompilationResult result = new CompilationResult(topLevelType.getFileName(), 1, 1);
			CompilationUnitDeclaration unit =
				SourceTypeConverter.buildCompilationUnit(new ISourceType[]{topLevelType}, false, true, lookupEnvironment.problemReporter, result);

			if (unit != null) {
				lookupEnvironment.buildTypeBindings(unit);
				rememberWithMemberTypes(topLevelType, unit.types[0].binding);

				lookupEnvironment.completeTypeBindings(unit, false);
			}
		}
		reportHierarchy();
	} catch (AbortCompilation e) { // ignore this exception for now since it typically means we cannot find java.lang.Object
	} finally {
		reset();
	}
}
}
