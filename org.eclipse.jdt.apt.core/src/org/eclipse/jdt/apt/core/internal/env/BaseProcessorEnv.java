/*******************************************************************************
 * Copyright (c) 2005 BEA Systems Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;

import java.io.File;
import java.util.ArrayList;
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
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IPackageFragment;
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
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.sun.mirror.apt.AnnotationProcessor;
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
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];

	private static final int BOOLEAN_INDEX = 0;
	private static final int BYTE_INDEX = 1;
	private static final int CHAR_INDEX = 2;
	private static final int DOUBLE_INDEX = 3;
	private static final int FLOAT_INDEX = 4;
	private static final int INT_INDEX = 5;
	private static final int LONG_INDEX = 6;
	private static final int SHORT_INDEX = 7;
	private static final int VOID_INDEX = 8;
	
	protected CompilationUnit _astRoot;
	protected final Phase _phase;
	protected IFile _file;
	protected final IJavaProject _javaProject;
	
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
    private AnnotationProcessor _latestProcessor;
	
	public BaseProcessorEnv(CompilationUnit astCompilationUnit,
						    IFile file,
						    IJavaProject javaProj,
							Phase phase )
	{
		_astRoot = astCompilationUnit;
		_file = file;
		_javaProject = javaProj;
		_phase = phase;
		
		_modelCompUnit2astCompUnit = new HashMap<ICompilationUnit, CompilationUnit>();
		_typeBinding2ModelCompUnit = new HashMap<ITypeBinding, ICompilationUnit>();
	}
    
    public AnnotationProcessor getLatestProcessor()
    {
        return _latestProcessor;
    }
    
    public void setLatestProcessor(AnnotationProcessor latestProcessor)
    {
        _latestProcessor = latestProcessor;
    }
	
	public Types getTypeUtils()
    {
		return new TypesUtil(this);
    }
	
	public Declarations getDeclarationUtils()
    {
        return new DeclarationsUtil();
    }
	
	public void addListener(AnnotationProcessorListener listener) {
		throw new UnsupportedOperationException("Not supported!"); //$NON-NLS-1$
	}
	
	public void removeListener(AnnotationProcessorListener listener) {
		throw new UnsupportedOperationException("Not supported!"); //$NON-NLS-1$
	}
	
	/**
     * @return the list of all named type declarations in compilation unit associated with
     *         this environment.
     * This implementation is different from the API specification that it does not return
     * all included types in the universe.
     */
    public Collection<TypeDeclaration> getTypeDeclarations()
    {
    	final List<ITypeBinding> bindings = getTypeBindings();
		if( bindings.isEmpty() )
			return Collections.emptyList();
		final List<TypeDeclaration> mirrorDecls = new ArrayList<TypeDeclaration>(bindings.size());

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
		final List<ITypeBinding> typeBindings = new ArrayList<ITypeBinding>(declTypes.size());

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
			typeBindings.add(nestedType);
			getTypeBindings(nestedType, typeBindings);
		}
	}
    
    public Collection<TypeDeclaration> getSpecifiedTypeDeclarations()
    {
        return getTypeDeclarations();
    }
    
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
    	final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap<ASTNode, List<Annotation>>();
        final AnnotatedNodeVisitor visitor = new AnnotatedNodeVisitor(astNode2Anno);
        _astRoot.accept(visitor);
        return astNode2Anno;
    }

    private List<Declaration> getDeclarationsAnnotatedWith(final ITypeBinding annotationType)
    {
        final Map<ASTNode, List<Annotation>> astNode2Anno = getASTNodesWithAnnotations();       
		if( astNode2Anno.isEmpty() )
			return Collections.emptyList();
		final List<Declaration> decls = new ArrayList<Declaration>();
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
            binding = ((SingleVariableDeclaration)node).resolveBinding();
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
    
    public Map<String, String> getOptions(){ return Collections.emptyMap(); };
    
    // does not generated dependencies
    public TypeDeclaration getTypeDeclaration(String name)
    {	
    	if( name == null ) return null;
		// get rid of the generics parts.
		final int index = name.indexOf('<');
		if( index != -1 )
			name = name.substring(0, index);
		
		// first see if it is one of the well known types.
		// any AST is as good as the other.		
		ITypeBinding typeBinding = null;
		if( _astRoot != null )
			typeBinding = _astRoot.getAST().resolveWellKnownType(name);
		String typeKey = BindingKey.createTypeBindingKey(name);
		if(typeBinding == null){
			// then look into the current compilation units			
			ASTNode node = null;
			if( _astRoot != null )
				node = _astRoot.findDeclaringNode(typeKey);
						
			if( node != null ){
				final int nodeType = node.getNodeType();
				if( nodeType == ASTNode.TYPE_DECLARATION ||
					nodeType == ASTNode.ANNOTATION_TYPE_DECLARATION ||
					nodeType == ASTNode.ENUM_DECLARATION )
				typeBinding = ((AbstractTypeDeclaration)node).resolveBinding();
			}
		}
		if( typeBinding != null )
			return Factory.createReferenceType(typeBinding, this);

		// finally go search for it in the universe.
		typeBinding = getTypeBinding(typeKey);
		if( typeBinding != null ){			
			return Factory.createReferenceType(typeBinding, this);
		}

		return null;
    }
    
    /**
	 * @param key the key to a type binding, could be reference type, array or primitive.
	 * @return the binding corresponding to the given key or null if none is found.
	 */
	public ITypeBinding getTypeBinding(final String key)
	{
		class BindingRequestor extends ASTRequestor
		{
			private ITypeBinding _result = null;
			public void acceptBinding(String bindingKey, IBinding binding)
			{
				if( binding != null && binding.getKind() == IBinding.TYPE )
					_result = (ITypeBinding)binding;
			}
		}

		final BindingRequestor requestor = new BindingRequestor();
		final ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setResolveBindings(true);
		parser.setProject(_javaProject);
		parser.createASTs(NO_UNIT, new String[]{key}, requestor, null);
		return requestor._result;
	}
	
	public TypeDeclaration getTypeDeclaration(final IType type) {
		if (type == null) return null;
		String name = type.getFullyQualifiedName();
		return getTypeDeclaration(name);
	}
    
	public PackageDeclaration getPackage(String name)
    {
		if (name == null)
			throw new IllegalArgumentException("name cannot be null"); //$NON-NLS-1$		
        IPackageFragment[] pkgFrags = PackageUtil.getPackageFragments(name, this);

		// No packages found, null expected
		if (pkgFrags.length == 0)
			return null;

		// If there are no source or class files, we'll need to return
		// a special implementation of the package decl that expects
		// no declarations inside it
		boolean containsNoJavaResources = true;
		for (IPackageFragment pkg : pkgFrags) {
			try {
				if (pkg.containsJavaResources()) {
					containsNoJavaResources = false;
					break;
				}
			}
			catch (JavaModelException e) {}
		}
		if (containsNoJavaResources)
			return new PackageDeclarationImplNoBinding(pkgFrags, this);

		// We should be able to create a class or
		// source file from one of the packages.
		ICompilationUnit compUnit = null;
		IClassFile classFile = null;

		OUTER:
		for (IPackageFragment pkg : pkgFrags) {
			try {
				ICompilationUnit[] compUnits = pkg.getCompilationUnits();
				if (compUnits.length > 0) {
					compUnit = compUnits[0];
					break;
				}
				IClassFile[] classFiles = pkg.getClassFiles();
				if (classFiles.length > 0) {
					// Need to grab the first one that's not an inner class,
					// as eclipse has trouble parsing inner class files
					for (IClassFile tempClassFile : classFiles) {
						if (tempClassFile.getElementName().indexOf("$") < 0) { //$NON-NLS-1$
							classFile = tempClassFile;
							break OUTER;
						}
					}
				}
			}
			catch (JavaModelException e) {}
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
		else if (classFile != null) {
			try {
				type = classFile.getType();
			}
			catch (JavaModelException e) {}
		}

		// Given a type, we can construct a package declaration impl from it,
		// but we must hide the fact that it came from a real declaration,
		// as the client requested it without that context
		if (type != null) {
			TypeDeclarationImpl typeDecl = (TypeDeclarationImpl)getTypeDeclaration(type);
			ITypeBinding binding = typeDecl.getDeclarationBinding();
			return new PackageDeclarationImpl(binding.getPackage(), typeDecl, this, true, pkgFrags);
		}

		// No classes or source files found
		return new PackageDeclarationImplNoBinding(pkgFrags, this);
    }
	
	protected CompilationUnit searchLocallyForBinding(final IBinding binding)
	{
		final ASTNode node = _astRoot.findDeclaringNode(binding);
		if( node != null )
			return _astRoot;
		return null;
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
				if( _typeBinding2ModelCompUnit.get(typeBinding) != null )
					unit = _typeBinding2ModelCompUnit.get(typeBinding);
				else{
					final String qname = typeBinding.getQualifiedName();
					final String pathname = qname.replace('.', File.separatorChar);
					final IPath path = Path.fromOSString(pathname);
					try{
						unit = (ICompilationUnit)_javaProject.findElement(path);
						_typeBinding2ModelCompUnit.put(typeBinding, unit);
					}
					catch(JavaModelException e){
						throw new IllegalStateException(e);
					}
				}
			}
			if( unit == null ) return null;

            final CompilationUnit astUnit = _modelCompUnit2astCompUnit.get(unit);
            if( astUnit != null ) return astUnit;
            else{
                // Note: very expensive operation. we are re-compiling a file with binding information.
                final ASTParser parser =  ASTParser.newParser(AST.JLS3);
                parser.setResolveBindings(true);
                parser.setSource(unit);
				parser.setFocalPosition(0);
                CompilationUnit resultUnit = (CompilationUnit)parser.createAST(null);
                _modelCompUnit2astCompUnit.put(unit, resultUnit);
                return resultUnit;
            }
        }
    }
    
    public Filer getFiler(){ 
    	throw new UnsupportedOperationException("Not supported: the EnvironmentFactory API is for type system navigation only"); //$NON-NLS-1$
    }    

    public Messager getMessager(){ 
    	throw new UnsupportedOperationException("Not supported: the EnvironmentFactory API is for type system navigation only"); //$NON-NLS-1$
    }
    
    /**
	 * @param bindin a type, method or field binding.
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
	
	protected IFile searchLocallyForIFile(final IBinding binding)
	{
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
			final String qname = type.getQualifiedName();
			final String pathname = qname.replace('.', File.separatorChar);
			final IPath path = Path.fromOSString(pathname);
			try{
				// the element would be a compilation unit.
				final IJavaElement element = _javaProject.findElement(path);
				if( element == null ) return null;
				return (IFile)element.getResource();
			}
			catch(JavaModelException e){
				throw new IllegalStateException(e);
			}
		}
	}
	
	/**
	 *  This should create an AST without imports or method-body statements
	 */
	public static ASTNode createDietAST( String unitName, IJavaProject javaProject, ICompilationUnit compilationUnit, char[] source )
	{
		ASTParser p = ASTParser.newParser( AST.JLS3 );
		if ( compilationUnit != null )
			p.setSource( compilationUnit );
		else
			p.setSource( source );
		p.setResolveBindings( true );
		p.setProject( javaProject );
		p.setUnitName( unitName );
		p.setFocalPosition( 0 );
		p.setKind( ASTParser.K_COMPILATION_UNIT );
		ASTNode node = p.createAST( null );
		return node;
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
}
