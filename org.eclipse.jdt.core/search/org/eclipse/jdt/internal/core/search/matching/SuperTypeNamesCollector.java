package org.eclipse.jdt.internal.core.search.matching;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;

import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.search.IJavaSearchConstants;
import org.eclipse.jdt.core.search.IJavaSearchScope;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.compiler.AbstractSyntaxTreeVisitorAdapter;
import org.eclipse.jdt.internal.compiler.ast.CompilationUnitDeclaration;
import org.eclipse.jdt.internal.compiler.ast.ConstructorDeclaration;
import org.eclipse.jdt.internal.compiler.ast.FieldDeclaration;
import org.eclipse.jdt.internal.compiler.ast.Initializer;
import org.eclipse.jdt.internal.compiler.ast.MemberTypeDeclaration;
import org.eclipse.jdt.internal.compiler.ast.MethodDeclaration;
import org.eclipse.jdt.internal.compiler.ast.TypeDeclaration;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding;
import org.eclipse.jdt.internal.compiler.lookup.BlockScope;
import org.eclipse.jdt.internal.compiler.lookup.ClassScope;
import org.eclipse.jdt.internal.compiler.lookup.CompilationUnitScope;
import org.eclipse.jdt.internal.compiler.lookup.MethodScope;
import org.eclipse.jdt.internal.compiler.lookup.ProblemReasons;
import org.eclipse.jdt.internal.compiler.lookup.ReferenceBinding;
import org.eclipse.jdt.internal.compiler.problem.AbortCompilation;
import org.eclipse.jdt.internal.compiler.util.CharOperation;
import org.eclipse.jdt.internal.core.BinaryType;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.search.IIndexSearchRequestor;
import org.eclipse.jdt.internal.core.search.IInfoConstants;
import org.eclipse.jdt.internal.core.search.IndexSearchAdapter;
import org.eclipse.jdt.internal.core.search.PathCollector;
import org.eclipse.jdt.internal.core.search.PatternSearchJob;
import org.eclipse.jdt.internal.core.search.indexing.IIndexConstants;
import org.eclipse.jdt.internal.core.search.indexing.IndexManager;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;

/**
 * Collects the super type names of a given declaring type.
 * Returns NOT_FOUND_DECLARING_TYPE if the declaring type was not found.
 * Returns null if the declaring type pattern doesn't require an exact match.
 */
public class SuperTypeNamesCollector {
	MethodReferencePattern pattern;
	MatchLocator locator;
	IJavaProject project; 
	IProgressMonitor progressMonitor;
	char[][][] result;
	int resultIndex;
	
	
/**
 * An ast visitor that visits type declarations and member type declarations
 * collecting their super type names.
 */
public class TypeDeclarationVisitor extends AbstractSyntaxTreeVisitorAdapter {
	public boolean visit(TypeDeclaration typeDeclaration, BlockScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(TypeDeclaration typeDeclaration, ClassScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(TypeDeclaration typeDeclaration, 	CompilationUnitScope scope) {
		ReferenceBinding type = typeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(MemberTypeDeclaration memberTypeDeclaration, 	ClassScope scope) {
		ReferenceBinding type = memberTypeDeclaration.binding;
		if (SuperTypeNamesCollector.this.matches(type)) {
			SuperTypeNamesCollector.this.collectSuperTypeNames(type);
		}
		return true;
	}
	public boolean visit(FieldDeclaration fieldDeclaration, MethodScope scope) {
		return false; // don't visit field declarations
	}
	public boolean visit(Initializer initializer, MethodScope scope) {
		return false; // don't visit initializers
	}
	public boolean visit(ConstructorDeclaration constructorDeclaration, ClassScope scope) {
		return false; // don't visit constructor declarations
	}
	public boolean visit(MethodDeclaration methodDeclaration, ClassScope scope) {
		return false; // don't visit method declarations
	}
}
	
public SuperTypeNamesCollector(
	MethodReferencePattern pattern,
	MatchLocator locator,
	IJavaProject project, 
	IProgressMonitor progressMonitor) {
		
	this.pattern = pattern;
	this.locator = locator;
	this.project = project;
	this.progressMonitor = progressMonitor;
}

protected char[][][] collect() {
		
	// Collect the paths of the cus that declare a type which matches declaringQualification + declaringSimpleName
	String[] paths = this.getPathsOfDeclaringType();
	
	// Create bindings from source types and binary types
	// and collect super type names of the type declaration 
	// that match the given declaring type
	if (paths != null) {
		this.result = new char[1][][];
		this.resultIndex = 0;
		for (int i = 0, length = paths.length; i < length; i++) {
			try {
				Openable openable = locator.handleFactory.createOpenable(paths[i]);
				if (openable == null)
					continue; // outside classpath
				if (openable instanceof ICompilationUnit) {
					ICompilationUnit unit = (ICompilationUnit)openable;
					CompilationUnitDeclaration parsedUnit = locator.buildBindings(unit);
					if (parsedUnit != null) {
						parsedUnit.traverse(new TypeDeclarationVisitor(), parsedUnit.scope);
					}
				} else if (openable instanceof IClassFile) {
					IClassFile classFile = (IClassFile)openable;
					IBinaryType binaryType = (IBinaryType)((BinaryType)classFile.getType()).getRawInfo();
					BinaryTypeBinding binding = locator.lookupEnvironment.cacheBinaryType(binaryType);
					if (this.matches(binding)) {
						this.collectSuperTypeNames(binding);
					}
				}
			} catch (JavaModelException e) {
				// ignore: continue with next element
			}
		}
		System.arraycopy(this.result, 0, this.result = new char[this.resultIndex][][], 0, this.resultIndex);
		return this.result;
	} else {
		return null;
	}
}
protected boolean matches(ReferenceBinding type) {
	if (type == null || type.compoundName == null) return false;
	return this.matches(type.compoundName);
}
protected boolean matches(char[][] compoundName) {
	int length = compoundName.length;
	if (length == 0) return false;
	char[] simpleName = compoundName[length-1];
	char[] declaringSimpleName = this.pattern.declaringSimpleName;
	char[] declaringQualification = this.pattern.declaringQualification;
	int last = length - 1;
	if (declaringSimpleName != null) {
		// most frequent case: simple name equals last segment of compoundName
		if (this.pattern.matchesName(simpleName, declaringSimpleName)) {
			char[][] qualification = new char[last][];
			System.arraycopy(compoundName, 0, qualification, 0, last);
			return 
				this.pattern.matchesName(
					declaringQualification, 
					CharOperation.concatWith(qualification, '.'));
		} else if (!CharOperation.endsWith(simpleName, declaringSimpleName)) {
			return false;
		} else {
			// member type -> transform A.B.C$D into A.B.C.D
			System.arraycopy(compoundName, 0, compoundName = new char[length+1][], 0, last);
			int dollar = CharOperation.indexOf('$', simpleName);
			if (dollar == -1) return false;
			compoundName[last] = CharOperation.subarray(simpleName, 0, dollar);
			compoundName[length] = CharOperation.subarray(simpleName, dollar+1, simpleName.length); 
			return this.matches(compoundName);
		}
	} else {
		char[][] qualification = new char[last][];
		System.arraycopy(compoundName, 0, qualification, 0, last);
		return 
			this.pattern.matchesName(
				declaringQualification, 
				CharOperation.concatWith(qualification, '.'));
	}
}
private void addToResult(char[][] compoundName) {
	if (this.result.length == this.resultIndex) {
		System.arraycopy(this.result, 0, this.result = new char[this.resultIndex*2][][], 0, this.resultIndex);
	}
	this.result[this.resultIndex++] = compoundName;
}
/**
 * Collects the names of all the supertypes of the given type.
 */
protected void collectSuperTypeNames(ReferenceBinding type) {

	// superclass
	ReferenceBinding superclass = type.superclass();
	if (superclass != null) {
		this.addToResult(superclass.compoundName);
		this.collectSuperTypeNames(superclass);
	}

	// interfaces
	ReferenceBinding[] interfaces = type.superInterfaces();
	if (interfaces != null) {
		for (int i = 0; i < interfaces.length; i++) {
			ReferenceBinding interfase = interfaces[i];
			this.addToResult(interfase.compoundName);
			this.collectSuperTypeNames(interfase);
		}
	}
}
private String[] getPathsOfDeclaringType() {
	char[] declaringQualification = this.pattern.declaringQualification;
	char[] declaringSimpleName = this.pattern.declaringSimpleName;
	if (declaringQualification != null || declaringSimpleName != null) {
		final PathCollector pathCollector = new PathCollector();
		IJavaSearchScope scope = SearchEngine.createJavaSearchScope(new IJavaElement[] {project});
	
		IndexManager indexManager = ((JavaModelManager)JavaModelManager.getJavaModelManager())
										.getIndexManager();
		int detailLevel = IInfoConstants.PathInfo;
		SearchPattern searchPattern = new TypeDeclarationPattern(
			declaringSimpleName != null ? null : declaringQualification, // use the qualification only if no simple name
			null, // do find member types
			declaringSimpleName,
			IIndexConstants.TYPE_SUFFIX,
			this.pattern.matchMode, 
			true);
		IIndexSearchRequestor searchRequestor = new IndexSearchAdapter(){
			public void acceptClassDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
				if (enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR) { // filter out local and anonymous classes
					pathCollector.acceptClassDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
				}
			}		
			public void acceptInterfaceDeclaration(String resourcePath, char[] simpleTypeName, char[][] enclosingTypeNames, char[] packageName) {
				if (enclosingTypeNames != IIndexConstants.ONE_ZERO_CHAR) { // filter out local and anonymous classes
					pathCollector.acceptInterfaceDeclaration(resourcePath, simpleTypeName, enclosingTypeNames, packageName);
				}
			}		
		};		
		if (indexManager != null) {
			indexManager.performConcurrentJob(
				new PatternSearchJob(
					searchPattern, 
					scope, 
					detailLevel, 
					searchRequestor, 
					indexManager),
				IJavaSearchConstants.WAIT_UNTIL_READY_TO_SEARCH,
				progressMonitor);
			return pathCollector.getPaths();
		}
	}
	return null;
}
}

