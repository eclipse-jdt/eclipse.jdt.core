/*******************************************************************************
 * Copyright (c) 2005, 2020 BEA Systems Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.PackageDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.PackageDeclarationImplNoBinding;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.type.PrimitiveTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.VoidTypeImpl;
import org.eclipse.jdt.apt.core.internal.util.DeclarationsUtil;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.PackageUtil;
import org.eclipse.jdt.apt.core.internal.util.TypesUtil;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotatedNodeVisitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IOrdinaryClassFile;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.IPackageBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.apt.Messager;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.Declarations;
import com.sun.mirror.util.Types;

/**
 * Base annotation processor environment that supports type system navigation.
 * No support for problem registration as well as type generation.
 *
 * @author tyeung
 */
public class BaseProcessorEnv implements AnnotationProcessorEnvironment
{
	private static final int JLS_LATEST = AST.getJLSLatest();
	static{
		final AST ast = AST.newAST(JLS_LATEST, true);
		EMPTY_AST_UNIT = ast.newCompilationUnit();
	}
	public static final CompilationUnit EMPTY_AST_UNIT;
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];
	public static final CompilationUnit[] NO_AST_UNITs = new CompilationUnit[0];
	public static final String[] NO_KEYS = new String[0];

	private static final int BOOLEAN_INDEX = 0;
	private static final int BYTE_INDEX = 1;
	private static final int CHAR_INDEX = 2;
	private static final int DOUBLE_INDEX = 3;
	private static final int FLOAT_INDEX = 4;
	private static final int INT_INDEX = 5;
	private static final int LONG_INDEX = 6;
	private static final int SHORT_INDEX = 7;

	private static final String DOT_JAVA = ".java"; //$NON-NLS-1$

	protected CompilationUnit _astRoot;
	protected final Phase _phase;
	protected IFile _file;
	protected final IJavaProject _javaProject;
	protected final AptProject _aptProject;
	private final boolean _isTestCode;

	/**
	 * Unmodifiable map of processor options, including -A options.
	 * Set in ctor and then not changed.
	 */
	protected final Map<String, String> _options;

	/**
     * Mapping model compilation unit to dom compilation unit.
     * The assumption here is that once the client examine some binding from some file,
     * it will continue to examine other bindings from came from that same file.
     */
    protected final Map<ICompilationUnit, CompilationUnit> _modelCompUnit2astCompUnit;
	/**
	 * Mapping (source) top-level type binding to the compilation unit that defines it.
	 */
    protected final Map<ITypeBinding, ICompilationUnit> _typeBinding2ModelCompUnit;
    // void type and the primitive types will be null if the '_file'
	// is outside of the workspace.
	private VoidTypeImpl _voidType;
	private PrimitiveTypeImpl[] _primitives;

	// This type cache exists for the duration of a single round.
	// We store positive as well as negative hits. Negative hits are
	// stored with a value of null
	protected final Map<String,TypeDeclaration> _typeCache = new HashMap<>();

	protected IPackageFragmentRoot[] _packageRootsCache;

	public BaseProcessorEnv(CompilationUnit astCompilationUnit,
						    IFile file,
						    IJavaProject javaProj,
							Phase phase, boolean isTestCode )
	{
		_astRoot = astCompilationUnit;
		_file = file;
		_javaProject = javaProj;
		_phase = phase;
		_options = initOptions(javaProj);
		_modelCompUnit2astCompUnit = new HashMap<>();
		_typeBinding2ModelCompUnit = new HashMap<>();
		_aptProject = AptPlugin.getAptProject(javaProj);
		_isTestCode = isTestCode;
	}

	/**
     * Set the _options map based on the current project/workspace settings.
     * There is a bug in Sun's apt implementation: it parses the command line
     * incorrectly, such that -Akey=value gets added to the options map as
     * key "-Akey=value" and value "".  In order to support processors written
     * to run on Sun's apt as well as processors written without this bug
     * in mind, we populate the map with two copies of every option, one the
     * expected way ("key" / "value") and the other the Sun way
     * ("-Akey=value" / "").  We make exceptions for the non-dash-A options
     * that we set automatically, such as -classpath, -target, and so forth;
     * since these wouldn't have come from a -A option we don't construct a
     * -Akey=value variant.
     *
     * Called from constructor.  A new Env is constructed for each build pass,
     * so this will always be up to date with the latest settings.
	 */
	private Map<String, String> initOptions(IJavaProject jproj) {
		Map<String, String> procOptions = AptConfig.getProcessorOptions(jproj, isTestCode());
		// options is large enough to include the translated -A options
		Map<String, String> options = new HashMap<>(procOptions.size() * 2);

		// Add configured options
		for (Map.Entry<String, String> entry : procOptions.entrySet()) {
			String value = entry.getValue();
			String key = entry.getKey();
			options.put(key, value);
			if (!AptConfig.isAutomaticProcessorOption(key)) {
				String sunStyle;
				if (value != null) {
					sunStyle = "-A" + entry.getKey() + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					sunStyle = "-A" + entry.getKey(); //$NON-NLS-1$
				}
				options.put(sunStyle, ""); //$NON-NLS-1$
			}
		}
		return Collections.unmodifiableMap(options);
	}

	@Override
	public Types getTypeUtils()
    {
		return new TypesUtil(this);
    }

	@Override
	public Declarations getDeclarationUtils()
    {
        return new DeclarationsUtil();
    }

	@Override
	public void addListener(AnnotationProcessorListener listener) {
		throw new UnsupportedOperationException("Not supported!"); //$NON-NLS-1$
	}

	@Override
	public void removeListener(AnnotationProcessorListener listener) {
		throw new UnsupportedOperationException("Not supported!"); //$NON-NLS-1$
	}

	/**
     * @return the list of all named type declarations in the compilation units associated with
     *         this environment - usually just one compilation unit, except in batch mode
     *         where it will be all compilation units in the build.
     * This implementation is different from the API specification in that it does not return
     * all included source types in the universe.
     */
    @Override
	public Collection<TypeDeclaration> getTypeDeclarations()
    {
    	final List<ITypeBinding> bindings = getTypeBindings();
		if( bindings.isEmpty() )
			return Collections.emptyList();
		final List<TypeDeclaration> mirrorDecls = new ArrayList<>(bindings.size());

		for( ITypeBinding binding : bindings ){
			final TypeDeclaration mirrorDecl = Factory.createReferenceType(binding, this);
			if( mirrorDecl != null )
				mirrorDecls.add(mirrorDecl);
		}

		return mirrorDecls;
    }

	protected List<AbstractTypeDeclaration> searchLocallyForTypeDeclarations()
    {
    	return _astRoot.types();
    }

    private List<ITypeBinding> getTypeBindings()
	{
    	final List<AbstractTypeDeclaration> declTypes = searchLocallyForTypeDeclarations();
		if( declTypes == null || declTypes.isEmpty() )
			return Collections.emptyList();
		final List<ITypeBinding> typeBindings = new ArrayList<>(declTypes.size());

		for( AbstractTypeDeclaration decl : declTypes ){
			getTypeBindings(decl.resolveBinding(), typeBindings);
		}
		return typeBindings;
	}

    /**
	 * Add <code>type</code> and all its declared nested type(s) to <code>types</code>
	 * @param type the container type
	 * @param typeBindings upon return, contains all the nested types within <code>type</code>
	 *        and the type itself.
	 */
	protected void getTypeBindings(final ITypeBinding type, final List<ITypeBinding> typeBindings)
	{
		if( type == null ) return;
		typeBindings.add(type);
		for( ITypeBinding nestedType : type.getDeclaredTypes() ) {
			//typeBindings.add(nestedType);
			getTypeBindings(nestedType, typeBindings);
		}
	}

    @Override
	public Collection<TypeDeclaration> getSpecifiedTypeDeclarations()
    {
        return getTypeDeclarations();
    }

    @Override
	public Collection<Declaration> getDeclarationsAnnotatedWith(AnnotationTypeDeclaration a)
    {
    	 final ITypeBinding annotationType = TypesUtil.getTypeBinding(a);
         if( annotationType == null  || !annotationType.isAnnotation()) return Collections.emptyList();
         return getDeclarationsAnnotatedWith(annotationType);
    }

    /**
     * Go through the current compilation unit and look for ast nodes that has annotations.
     * @return the map between ast node and
     */
    protected Map<ASTNode, List<Annotation>> getASTNodesWithAnnotations()
    {
    	final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap<>();
        final AnnotatedNodeVisitor visitor = new AnnotatedNodeVisitor(astNode2Anno);
        _astRoot.accept(visitor);
        return astNode2Anno;
    }

    private List<Declaration> getDeclarationsAnnotatedWith(final ITypeBinding annotationType)
    {
        final Map<ASTNode, List<Annotation>> astNode2Anno = getASTNodesWithAnnotations();
		if( astNode2Anno.isEmpty() )
			return Collections.emptyList();
		final List<Declaration> decls = new ArrayList<>();
		for(Map.Entry<ASTNode, List<Annotation>> entry : astNode2Anno.entrySet() ){
			final ASTNode node = entry.getKey();
			for( Annotation anno : entry.getValue() ){
				final IBinding resolvedTypeBinding = anno.resolveTypeBinding();
				if( annotationType.isEqualTo(resolvedTypeBinding) )
					getDeclarations(node, decls);
			}
		}
        return decls;

    }

    protected IFile getFileForNode(final ASTNode node)
    {
    	if( node.getRoot() == _astRoot )
    		return _file;

    	throw new IllegalStateException(); // should never get here.
    }

    /**
	 * @param node the ast node in question
	 * @param decls the list to be populated.
	 *        adding the declaration(s) corresponding to the ast node to this list.
	 */
	protected void getDeclarations(ASTNode node, List<Declaration>decls)
    {
    	if( node == null ) return;
        IBinding binding = null;
		switch( node.getNodeType() )
		{
		case ASTNode.FIELD_DECLARATION:
			final List<VariableDeclarationFragment> fragments =
                ((org.eclipse.jdt.core.dom.FieldDeclaration)node).fragments();
			for( VariableDeclarationFragment frag : fragments ){
				final IBinding fieldBinding = frag.resolveBinding();
				final EclipseDeclarationImpl decl;
				if( fieldBinding != null )
					decl = Factory.createDeclaration(fieldBinding, this);
				else{
					decl = Factory.createDeclaration(frag, getFileForNode(frag), this);
				}
				if( decl != null )
					decls.add(decl);
			}
            return;

		case ASTNode.ENUM_CONSTANT_DECLARATION:
            binding = ((org.eclipse.jdt.core.dom.EnumConstantDeclaration)node).resolveVariable();
            break;
        case ASTNode.METHOD_DECLARATION:
            binding = ((org.eclipse.jdt.core.dom.MethodDeclaration)node).resolveBinding();
			break;
        case ASTNode.ANNOTATION_TYPE_MEMBER_DECLARATION:
            binding = ((AnnotationTypeMemberDeclaration)node).resolveBinding();
            break;
        case ASTNode.TYPE_DECLARATION:
        case ASTNode.ANNOTATION_TYPE_DECLARATION:
        case ASTNode.ENUM_DECLARATION:
            binding = ((AbstractTypeDeclaration)node).resolveBinding();
            break;
        case ASTNode.SINGLE_VARIABLE_DECLARATION:
        	// Need to create the declaration with the ast node, not the binding
            break;
        case ASTNode.PACKAGE_DECLARATION:
            binding = ((org.eclipse.jdt.core.dom.PackageDeclaration)node).resolveBinding();
            break;
        default:
            throw new UnsupportedOperationException("unknown node type: " + node.getNodeType()); //$NON-NLS-1$
        }

		final EclipseDeclarationImpl decl;
		if( binding != null )
			decl = Factory.createDeclaration(binding, this);
		else{
			decl = Factory.createDeclaration(node, getFileForNode(node), this);
		}
		if( decl != null )
			decls.add( decl );

        return;
    }



	/**
     * @param binding must be correspond to a type, method or field declaration.
     * @return the ast node the corresponds to the declaration of the given binding.
     *         Return null if none is found.
     */
    public ASTNode getASTNodeForBinding(final IBinding binding)
    {
    	final CompilationUnit astUnit = getCompilationUnitForBinding(binding);
		if( astUnit == null )
			return null;
		return astUnit.findDeclaringNode(binding.getKey());
    }

    @Override
	public Map<String, String> getOptions(){ return _options; }

    // does not generate dependencies
    @Override
	public TypeDeclaration getTypeDeclaration(String name)
    {
    	if( name == null || name.length() == 0 ) return null;

    	// get rid of the generics parts.
		final int index = name.indexOf('<');
		if( index != -1 )
			name = name.substring(0, index);

		ITypeBinding typeBinding = null;
		try {
			typeBinding = getTypeDefinitionBindingFromName(name);
		}
		catch (ArrayIndexOutOfBoundsException e) {
			// https://bugs.eclipse.org/bugs/show_bug.cgi?id=133947
			// if the name is invalid, JDT can throw an ArrayIndexOutOfBoundsException
			// We'll ignore this and return null to the user
			AptPlugin.log(e, "Unable to get type definition binding for: " + name); //$NON-NLS-1$
		}

    	return Factory.createReferenceType(typeBinding, this);
    }

    /**
     * @param fullyQualifiedName the fully qualified name of a type.
     * The name cannot contain type argument or array signature.
     * The name *must* also be correct wrt $ for inner-class separators.
     * e.g. java.util.Map$Entry, NOT java.util.Map.Entry
     * @return the type binding corresponding to the parameter.
     */
    protected ITypeBinding getTypeDefinitionBindingFromCorrectName(
    		final String fullyQualifiedName ){
    	final int dollarIndex = fullyQualifiedName.indexOf('$');
    	final String toplevelTypeName;
    	if( dollarIndex < 0 )
    		toplevelTypeName = fullyQualifiedName;
    	else
    		toplevelTypeName = fullyQualifiedName.substring(0, dollarIndex);

    	// locate the compilation unit for the type of interest.
    	// we need this information so that when we request the binding for 'fullyQualifiedName'
    	// we can get the dom pipeline to return back to us the ast compilation unit
    	// which we will need to correctly compute the number of methods, fields and constructors.
    	// see CR259011 -theodora
    	ICompilationUnit unit = getICompilationUnitForTopLevelType(toplevelTypeName);
       	final String key = BindingKey.createTypeBindingKey(fullyQualifiedName);
    	return (ITypeBinding)getBindingFromKey(key, unit);
    }

    private ITypeBinding getTypeDefinitionBindingFromName(String fullyQualifiedName) {
    	// We don't know for sure that the name we have represents a top-level type,
    	// so we need to loop backwards until we find one, in case we have something
    	// like "java.util.Map.Entry", converting it to "java.util.Map$Entry". --jgarms
    	ITypeBinding binding = getTypeDefinitionBindingFromCorrectName(fullyQualifiedName);
    	while (binding == null) {
    		int dotIndex = fullyQualifiedName.lastIndexOf('.');
    		if (dotIndex == -1) {
    			break;
    		}
    		fullyQualifiedName = fullyQualifiedName.substring(0, dotIndex) +
    			"$" +  //$NON-NLS-1$
    			fullyQualifiedName.substring(dotIndex + 1);
    		binding = getTypeDefinitionBindingFromCorrectName(fullyQualifiedName);
    	}
    	return binding;
    }

    /**
     * @param unit the unit that contains the definition of type whose type key is <code>key</code>
     * if <code>key</code> is a wild card, primitive, array type or parameterized type, this should be null.
     * @return return the type binding for the given key or null if none is found.
     */
    protected IBinding getBindingFromKey(final String key, final ICompilationUnit unit){

		class BindingRequestor extends ASTRequestor
		{
			private IBinding _result = null;
			private int _kind;

			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				if( source == unit ){
					_modelCompUnit2astCompUnit.put(source, ast);
				}
			}
			@Override
			public void acceptBinding(String bindingKey, IBinding binding)
			{
				if( binding != null ) {
					_result = binding;
					_kind = binding.getKind();
				}
			}
		}

		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(JLS_LATEST);
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setProject(_javaProject);
		parser.setIgnoreMethodBodies(true);
		ICompilationUnit[] units = unit == null ? NO_UNIT : new ICompilationUnit[]{unit};
		parser.createASTs(units, new String[]{key}, requestor, null);
		final IBinding result = requestor._result;
		if(result != null && unit != null){
			final CompilationUnit astUnit = _modelCompUnit2astCompUnit.get(unit);
			// make sure everything is lining up properly.  Only cache real types, not package-infos.
			if( requestor._kind == IBinding.TYPE && astUnit.findDeclaringNode(result) != null ){
				ITypeBinding declaringClass = getDeclaringClass(result);
				_typeBinding2ModelCompUnit.put(declaringClass, unit);
			}
		}
		return result;
    }

    /**
	 * @param key the key to a type binding, could be reference type, array or primitive.
	 * @return the binding corresponding to the given key or null if none is found.
	 */
	public ITypeBinding getTypeBindingFromKey(final String key)
	{
		return (ITypeBinding)getBindingFromKey(key, null);

	}

	public TypeDeclaration getTypeDeclaration(final IType type) {
		if (type == null) return null;
		String name = type.getFullyQualifiedName();
		return getTypeDeclaration(name);
	}

	@Override
	public PackageDeclaration getPackage(String name)
	{
		if (name == null)
			throw new IllegalArgumentException("name cannot be null"); //$NON-NLS-1$
		IPackageFragment[] pkgFrags = PackageUtil.getPackageFragments(name, this);

		// No packages found, null expected
		if (pkgFrags.length == 0)
			return null;

		try {
			// If there are no source or class files, we'll need to return
			// a special implementation of the package decl that expects
			// no declarations inside it
			boolean containsNoJavaResources = true;
			for (IPackageFragment pkg : pkgFrags) {
				if (pkg.containsJavaResources()) {
					containsNoJavaResources = false;
					break;
				}
			}
			if (containsNoJavaResources)
				return new PackageDeclarationImplNoBinding(pkgFrags);

			// We should be able to create a class or
			// source file from one of the packages.
			// If we find package-info, don't use it, but set
			// it aside in case it's all we can find.
			ICompilationUnit compUnit = null;
			IOrdinaryClassFile classFile = null;
			ICompilationUnit pkgInfoUnit = null;
			IOrdinaryClassFile pkgInfoClassFile = null;
			OUTER:
				for (IPackageFragment frag : pkgFrags) {
					if (frag.getKind() == IPackageFragmentRoot.K_SOURCE) {
						for (ICompilationUnit unit : frag.getCompilationUnits()) {
							if ("package-info.java".equals(unit.getElementName())) { //$NON-NLS-1$
								pkgInfoUnit = unit;
							}
							else {
								compUnit = unit;
								break OUTER;
							}
						}
					}
					else { // K_BINARY
						for (IOrdinaryClassFile file : frag.getOrdinaryClassFiles()) {
							String cfName = file.getElementName();
							if ("package-info.class".equals(cfName)) { //$NON-NLS-1$
								pkgInfoClassFile = file;
							}
							else if (file.getElementName().indexOf("$") < 0) { //$NON-NLS-1$
								classFile = file;
								break OUTER;
							}
						}
					}
				}

			IType type = null;
			if (compUnit != null) {
				try {
					IType[] types = compUnit.getAllTypes();
					if (types.length > 0) {
						type = types[0];
					}
				}
				catch (JavaModelException e) {}
			}
			if (type == null && classFile != null) {
				type = classFile.getType();
			}

			// Given a type, we can construct a package declaration impl from it,
			// but we must hide the fact that it came from a real declaration,
			// as the client requested it without that context
			if (type != null) {
				TypeDeclarationImpl typeDecl = (TypeDeclarationImpl)getTypeDeclaration(type);
				ITypeBinding binding = typeDecl.getDeclarationBinding();
				return new PackageDeclarationImpl(binding.getPackage(), typeDecl, this, true, pkgFrags);
			}

			// No classes or source files found.  Do we have a package-info we can use?
			if (pkgInfoUnit != null || pkgInfoClassFile != null) {
				String key = getPackageBindingKey(name);
				IPackageBinding packageBinding = (IPackageBinding)getBindingFromKey(key, compUnit);
				if (null != packageBinding) {
					return new PackageDeclarationImpl(packageBinding, null, this, true, pkgFrags);
				}
			}
		}
		catch (JavaModelException e) {
			// Probably bad code; treat as if no types were found
		}

		// This package is empty: no types and no package-info.
		return new PackageDeclarationImplNoBinding(pkgFrags);
	}

	// There doesn't seem to be a public inverse of
	// org.eclipse.jdt.internal.compiler.lookup.PackageBinding.computeUniqueKey().
	private String getPackageBindingKey(String packageName) {
		return packageName.replace('.', '/');
	}

	protected CompilationUnit searchLocallyForBinding(final IBinding binding)
	{
		if (_astRoot == null) {
			throw new IllegalStateException("_astRoot is null. Check that types or environments are not being cached between builds or reconciles by user code"); //$NON-NLS-1$
		}

		final ASTNode node = _astRoot.findDeclaringNode(binding);
		if( node != null )
			return _astRoot;
		return null;
	}

	/**
	 * Retrieve the <code>ICompilationUnit</code> whose top-level type has
	 * <code>topTypeQName</code> as its fully qualified name.
	 * @return the <code>ICompilationUnit</code> matching <code>topTypeQName</code> or
	 * <code>null</code> if one doesn't exist.
	 */
	private ICompilationUnit getICompilationUnitForTopLevelType(final String topTypeQName ){
		final String pathname = topTypeQName.replace('.', File.separatorChar) + DOT_JAVA;
		final IPath path = Path.fromOSString(pathname);
		try{
			final IJavaElement element = _javaProject.findElement(path);
			if( element instanceof ICompilationUnit )
				return (ICompilationUnit)element;
			else // dropping class files.
				return null;
		}
		catch(JavaModelException e){
			return null;
		}
	}

	/**
     * @param binding must be correspond to a type, method or field declaration.
     * @return the compilation unit that contains the declaration of the given binding.
     */
    public CompilationUnit getCompilationUnitForBinding(final IBinding binding)
    {
        assert binding.getKind() == IBinding.TYPE ||
               binding.getKind() == IBinding.METHOD ||
               binding.getKind() == IBinding.VARIABLE ;
        CompilationUnit domUnit = searchLocallyForBinding(binding);
        if( domUnit != null )
        	return domUnit;
        else{
			final IMember member = (IMember)binding.getJavaElement();
			final ICompilationUnit unit;
			if( member != null ){
				unit = member.getCompilationUnit();
			}
			else{
				final ITypeBinding typeBinding = getDeclaringClass(binding);
				// binary type don't have compilation unit.
				if( !typeBinding.isFromSource() )
					return null;
				if( _typeBinding2ModelCompUnit.get(typeBinding) != null )
					unit = _typeBinding2ModelCompUnit.get(typeBinding);
				else{
					final String qname = typeBinding.getQualifiedName();
					unit = getICompilationUnitForTopLevelType(qname);
				}
			}
			if( unit == null ) return null;

            final CompilationUnit astUnit = _modelCompUnit2astCompUnit.get(unit);
            if( astUnit != null ) return astUnit;
            else{
                // Note: very expensive operation. we are re-compiling a file with binding information.
                final ASTParser parser =  ASTParser.newParser(JLS_LATEST);
                parser.setResolveBindings(true);
        		parser.setBindingsRecovery(true);
                parser.setSource(unit);
				parser.setFocalPosition(0);
                parser.setIgnoreMethodBodies(true);
                CompilationUnit resultUnit = (CompilationUnit)parser.createAST(null);
                _modelCompUnit2astCompUnit.put(unit, resultUnit);
                return resultUnit;
            }
        }
    }

    @Override
	public Filer getFiler(){
    	throw new UnsupportedOperationException("Not supported: the EnvironmentFactory API is for type system navigation only"); //$NON-NLS-1$
    }

    @Override
	public Messager getMessager(){
    	throw new UnsupportedOperationException("Not supported: the EnvironmentFactory API is for type system navigation only"); //$NON-NLS-1$
    }

    /**
	 * @param binding a type, method or field binding.
	 * @return the top-level type binding that declares <code>binding</code>
	 * 	       or itself if it is already one.
	 */
	protected static ITypeBinding getDeclaringClass(final IBinding binding)
	{
		assert binding != null : "binding cannot be null"; //$NON-NLS-1$
		ITypeBinding aTypeBinding = null;
		switch( binding.getKind() )
		{
		case IBinding.TYPE:
			aTypeBinding = (ITypeBinding)binding;
			break;
		case IBinding.METHOD:
			aTypeBinding = ((IMethodBinding)binding).getDeclaringClass();
			break;
		case IBinding.VARIABLE:
			aTypeBinding = ((IVariableBinding)binding).getDeclaringClass();
			break;
		default:
			throw new IllegalStateException("unrecognized binding type " +  binding.getKind()); //$NON-NLS-1$
		}
		if(aTypeBinding == null ) return null;
		while( !aTypeBinding.isTopLevel() ){
			aTypeBinding = aTypeBinding.getDeclaringClass();
		}
		return aTypeBinding;
	}

	/**
	 * The environment caches the package fragment roots, as
	 * they are expensive to compute
	 */
	public IPackageFragmentRoot[] getAllPackageFragmentRoots() throws JavaModelException {
		if (_packageRootsCache == null) {
			_packageRootsCache = getJavaProject().getAllPackageFragmentRoots();
		}
		return _packageRootsCache;
	}

	protected IFile searchLocallyForIFile(final IBinding binding)
	{
		if (_astRoot == null) {
			return null;
		}

		ASTNode node = _astRoot.findDeclaringNode(binding);
		if( node != null )
			return _file;
		return null;
	}

	/**
	 * @param binding must be correspond to a type, method or field declaration
	 * @return the file that contains the declaration of given binding.
	 */
	public IFile getDeclaringFileForBinding(final IBinding binding)
	{
		assert binding.getKind() == IBinding.TYPE ||
		       binding.getKind() == IBinding.METHOD ||
		       binding.getKind() == IBinding.VARIABLE ;
		// check to see whether it is in the current file.
		IFile file = searchLocallyForIFile(binding);
		if( file != null )
			return file;

		final IMember member = (IMember)binding.getJavaElement();
		if( member != null ){
			final ICompilationUnit unit = member.getCompilationUnit();
			return (IFile)unit.getResource();
		}
		else{
			final ITypeBinding type = getDeclaringClass(binding);
			assert type.isTopLevel() : "type must be top-level type"; //$NON-NLS-1$
			ICompilationUnit unit = _typeBinding2ModelCompUnit.get(type);
			if( unit != null )
				return (IFile)unit.getResource();
			final String qname = type.getQualifiedName();
			unit = getICompilationUnitForTopLevelType(qname);
			if( unit == null )
				return null;
			return (IFile)unit.getResource();
		}
	}

	static class BaseRequestor extends ASTRequestor
	{
		ICompilationUnit[] parseUnits;
		CompilationUnit[] asts;
		BaseRequestor(ICompilationUnit[] parseUnits)
		{
			asts = new CompilationUnit[parseUnits.length];
			// Init all units to empty to prevent any NPEs
			Arrays.fill(asts, EMPTY_AST_UNIT);
			this.parseUnits = parseUnits;
		}

		@Override
		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			for( int i=0, len = asts.length; i<len; i++ ){
				if( source == parseUnits[i] ){
					asts[i] = ast;
					break;
				}
			}
		}

	}

	/**
	 * Parse and fully resolve all files.
	 * @param parseUnits the files to be parsed and resolved.
	 */
	static void createASTs(
			final IJavaProject javaProject,
			final ICompilationUnit[] parseUnits,
			ASTRequestor requestor)
	{
		// Construct exactly 1 binding key. When acceptBinding is called we know that
		// All ASTs have been returned. This also means that a pipeline is opened when
		// there are no asts. This is needed by the batch processors.
		String bogusKey = BindingKey.createTypeBindingKey("java.lang.Object"); //$NON-NLS-1$
		String[] keys = new String[] {bogusKey};

		ASTParser p = ASTParser.newParser( JLS_LATEST );
		p.setResolveBindings(true);
		p.setBindingsRecovery(true);
		p.setProject( javaProject );
		p.setKind( ASTParser.K_COMPILATION_UNIT );
		p.setIgnoreMethodBodies(true);
		p.createASTs( parseUnits, keys,  requestor, null);
	}

	/**
	 *  This should create an AST without imports or method-body statements
	 */
	public static CompilationUnit createAST(
			IJavaProject javaProject,
			final ICompilationUnit compilationUnit)
	{
		if(compilationUnit == null)
			return null;

		class CompilationUnitRequestor extends ASTRequestor
		{
			CompilationUnit domUnit = EMPTY_AST_UNIT;
			@Override
			public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
				if( source == compilationUnit )
					domUnit = ast;
			}
		}

		CompilationUnitRequestor requestor = new CompilationUnitRequestor();
		ASTParser p = ASTParser.newParser( JLS_LATEST );
		p.setResolveBindings(true);
		p.setBindingsRecovery(true);
		p.setProject( javaProject );
		p.setKind( ASTParser.K_COMPILATION_UNIT );
		p.setIgnoreMethodBodies(true);
		p.createASTs( new ICompilationUnit[]{compilationUnit}, NO_KEYS,  requestor, null);
		if( AptPlugin.DEBUG ){
			AptPlugin.trace("created DOM AST for " + compilationUnit.getElementName() ); //$NON-NLS-1$
		}
		return requestor.domUnit;
	}

	/**
	 * @return the ast current being processed
	 */
	protected AST getCurrentDietAST(){
		return _astRoot.getAST();
	}

	private void initPrimitives()
	{
		if(_primitives != null ) return;
		AST ast = getCurrentDietAST();

		_primitives = new PrimitiveTypeImpl[8];
		// boolean
		ITypeBinding binding = ast.resolveWellKnownType(ITypeConstants.BOOLEAN);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BOOLEAN); //$NON-NLS-1$
		_primitives[BOOLEAN_INDEX] = new PrimitiveTypeImpl(binding);
		// byte
		binding = ast.resolveWellKnownType(ITypeConstants.BYTE);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[BYTE_INDEX] = new PrimitiveTypeImpl(binding);
		// char
		binding = ast.resolveWellKnownType(ITypeConstants.CHAR);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[CHAR_INDEX] = new PrimitiveTypeImpl(binding);
		// double
		binding = ast.resolveWellKnownType(ITypeConstants.DOUBLE);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[DOUBLE_INDEX] = new PrimitiveTypeImpl(binding);
		// float
		binding = ast.resolveWellKnownType(ITypeConstants.FLOAT);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[FLOAT_INDEX] = new PrimitiveTypeImpl(binding);
		// int
		binding = ast.resolveWellKnownType(ITypeConstants.INT);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[INT_INDEX] = new PrimitiveTypeImpl(binding);
		// long
		binding = ast.resolveWellKnownType(ITypeConstants.LONG);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[LONG_INDEX] = new PrimitiveTypeImpl(binding);
		// short
		binding = ast.resolveWellKnownType(ITypeConstants.SHORT);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_primitives[SHORT_INDEX] = new PrimitiveTypeImpl(binding);
		// void
		binding = ast.resolveWellKnownType(ITypeConstants.VOID);
		if( binding == null )
			throw new IllegalStateException("fail to locate " + ITypeConstants.BYTE); //$NON-NLS-1$
		_voidType = new VoidTypeImpl(binding);
	}

	public PrimitiveTypeImpl getBooleanType(){
		initPrimitives();
		return _primitives[BOOLEAN_INDEX];
	}
	public PrimitiveTypeImpl getByteType(){
		initPrimitives();
		return _primitives[BYTE_INDEX];
	}
	public PrimitiveTypeImpl getCharType(){
		initPrimitives();
		return _primitives[CHAR_INDEX];
	}
	public PrimitiveTypeImpl getDoubleType(){
		initPrimitives();
		return _primitives[DOUBLE_INDEX];
	}
	public PrimitiveTypeImpl getFloatType(){
		initPrimitives();
		return _primitives[FLOAT_INDEX];
	}
	public PrimitiveTypeImpl getIntType(){
		initPrimitives();
		return _primitives[INT_INDEX];
	}
	public PrimitiveTypeImpl getLongType(){
		initPrimitives();
		return _primitives[LONG_INDEX];
	}
	public PrimitiveTypeImpl getShortType(){
		initPrimitives();
		return _primitives[SHORT_INDEX];
	}
	public VoidTypeImpl getVoidType(){
		initPrimitives();
		return _voidType;
	}

	public CompilationUnit  getAstCompilationUnit(){ return _astRoot; }
	public IFile            getFile() { return _file; }
	public Phase            getPhase(){ return _phase; }
    public IProject         getProject(){ return _javaProject.getProject(); }
	public IJavaProject		getJavaProject(){ return _javaProject; }
	public AptProject		getAptProject(){ return _aptProject; }
	public boolean isTestCode() { return _isTestCode; }
}
