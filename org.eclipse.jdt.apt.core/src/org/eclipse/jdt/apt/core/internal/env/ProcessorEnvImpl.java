/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    tyeung@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.core.internal.env;

import java.io.BufferedInputStream;
import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.internal.declaration.PackageDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.declaration.PackageDeclarationImplNoBinding;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.type.PrimitiveTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.VoidTypeImpl;
import org.eclipse.jdt.apt.core.internal.util.DeclarationsUtil;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.PackageUtil;
import org.eclipse.jdt.apt.core.internal.util.TypesUtil;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
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
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BodyDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.Declaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.util.Declarations;
import com.sun.mirror.util.Types;

public class ProcessorEnvImpl implements AnnotationProcessorEnvironment,
										 EclipseAnnotationProcessorEnvironment
{
	public static final String RECONCILE_MARKER = "org.eclipse.jdt.apt.core.reconcile_marker";
	public static final String BUILD_MARKER		= "org.eclipse.jdt.apt.core.build_marker";
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];

    public enum Phase { RECONCILE, BUILD };

    private final CompilationUnit _astCompilationUnit;
    private final ICompilationUnit _compilationUnit;
    private final List<MarkerInfo> _markerInfos;
    private final Phase _phase;
    private final IFile _file;
	/**
	 * The source code in <code>_file</code>.
	 * This is the exact same source code that created the dom compil
	 */
	private final char[] _source;

	private final IJavaProject _javaProject;
	// Stores the generated files and whether or not they were modified. In this case,
	// new files will be considered "modified".
    private final Map<IFile, Boolean> _generatedFiles = new HashMap<IFile, Boolean>();
	private Set<AnnotationProcessorListener> _listeners = null;
	private final FilerImpl _filer;
	private boolean _isClosed = false;

	/**
	 * Set of strings that indicate new type dependencies introduced on the file
	 * each string is a fully-qualified type name.
	 */
	private Set<String> _typeDependencies = new HashSet<String>();

	// void type and the primitive types will be null if the '_file'
	// is outside of the workspace.
	private VoidTypeImpl _voidType;
	private PrimitiveTypeImpl[] _primitives;

    /**
     * Mapping model compilation unit to dom compilation unit.
     * The assumption here is that once the client examine some binding from some file, it will continue
     * to examine other bindings from came from that same file.
     */
    private final Map<ICompilationUnit, CompilationUnit> _modelCompUnit2astCompUnit;
	/**
	 * Mapping (source) top-level type binding to the compilation unit that defines it.
	 */
	private final Map<ITypeBinding, ICompilationUnit> _typeBinding2ModelCompUnit;

    public static ProcessorEnvImpl newProcessorEnvironmentForReconcile(ICompilationUnit compilationUnit, IJavaProject javaProj)
    {
       	return new ProcessorEnvImpl( compilationUnit, javaProj, Phase.RECONCILE );
    }

    public static ProcessorEnvImpl newProcessorEnvironmentForBuild( IFile file, IJavaProject javaProj )
    {
    	return new ProcessorEnvImpl( file, javaProj, Phase.BUILD );
    }

    private ProcessorEnvImpl( IFile file, IJavaProject javaProj, Phase phase )
    {
        assert phase == Phase.BUILD : "Unexpected phase value.  Use Phase.BUILD instead of " + phase;

    	_compilationUnit = null;

		char[] source = null;

		try
		{
			source = getFileContents( file );
		}
		catch( Exception e )
		{
			// TODO:  propagate these exceptions out of APTDispatch
			e.printStackTrace();
		}

		_source = source;
		assert _source != null : "missing source";

		String unitName = file.getProjectRelativePath().toString();
		ASTNode node = createDietAST( unitName, javaProj, null, _source );
		_astCompilationUnit = (org.eclipse.jdt.core.dom.CompilationUnit) node;
        _phase = phase;
        _file = file;
		_javaProject = javaProj;
		_modelCompUnit2astCompUnit = new HashMap<ICompilationUnit, CompilationUnit>();
		_typeBinding2ModelCompUnit = new HashMap<ITypeBinding, ICompilationUnit>();
        _markerInfos = new ArrayList<MarkerInfo>(4);
		_filer = new FilerImpl(this);
		initPrimitives(_javaProject);
    }

    private ProcessorEnvImpl(ICompilationUnit compilationUnit, IJavaProject javaProj, Phase phase)
    {
        assert phase == Phase.RECONCILE : "Unexpected phase value.  Use Phase.RECONCILE instead of " + phase;

		_source = null;
		String unitName =  compilationUnit.getResource().getProjectRelativePath().toString();
		ASTNode node = createDietAST( unitName, javaProj, compilationUnit, null );

		_astCompilationUnit = (org.eclipse.jdt.core.dom.CompilationUnit) node;

        _compilationUnit = compilationUnit;
        _phase = phase;
        _file = (IFile)compilationUnit.getResource();
		_javaProject = javaProj;
        _modelCompUnit2astCompUnit = new HashMap<ICompilationUnit, CompilationUnit>();
		_typeBinding2ModelCompUnit = new HashMap<ITypeBinding, ICompilationUnit>();
        _markerInfos = new ArrayList<MarkerInfo>(4);
		_filer = new FilerImpl(this);
		initPrimitives(_javaProject);
    }


	/**
	 *  This should create an AST without imports or method-body statements
	 */
	private static ASTNode createDietAST( String unitName, IJavaProject javaProject, ICompilationUnit compilationUnit, char[] source )
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


    public Collection<Declaration> getDeclarationsAnnotatedWith(AnnotationTypeDeclaration a)
    {
        final ITypeBinding annotationType = TypesUtil.getTypeBinding(a);
        if( annotationType == null  || !annotationType.isAnnotation()) return Collections.emptyList();
        final List<IBinding> annotatedDecls = getBindingsAnnotatedWith(annotationType);
        if( annotatedDecls.isEmpty() ) return Collections.emptyList();
        final Collection<Declaration> results = new ArrayList<Declaration>(annotatedDecls.size());
        for( IBinding annotatedDecl : annotatedDecls ){
            Declaration mirrorDecl = Factory.createDeclaration(annotatedDecl, this);
            if( mirrorDecl != null )
                results.add(mirrorDecl);
       }
          return results;
    }

    private List<IBinding> getBindingsAnnotatedWith(final ITypeBinding annotationType)
    {
        final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap<ASTNode, List<Annotation>>();
		_astCompilationUnit.accept( new AnnotatedNodeVisitor(astNode2Anno) );
		if( astNode2Anno.isEmpty() )
			return Collections.emptyList();
		final List<IBinding> annotatedBindings = new ArrayList<IBinding>();
		for(Map.Entry<ASTNode, List<Annotation>> entry : astNode2Anno.entrySet() ){
			final ASTNode node = entry.getKey();
			for( Annotation anno : entry.getValue() ){
				final IBinding resolvedTypeBinding = anno.resolveTypeBinding();
				if( annotationType.isEqualTo(resolvedTypeBinding) )
                    getBinding(node, annotatedBindings);
			}
		}
        return annotatedBindings;

    }

	/**
	 * @param node the ast node in question
	 * @param bindings the list to be populated.
	 *        adding the binding(s) corresponding to the ast node to this list.
	 */
	private void getBinding(ASTNode node, List<IBinding> bindings)
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
				if( fieldBinding != null )
					bindings.add(fieldBinding);
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
            throw new UnsupportedOperationException("unknown node type: " + node.getNodeType());
        }

        if(binding != null)
            bindings.add(binding);
        return;
	}

    private Map<ASTNode, List<Annotation>> findASTNodesWithAnnotataion()
    {
        throw new UnsupportedOperationException("e-mail tyeung@bea.com if you need this now.");
    }

    private static final class AnnotatedNodeVisitor extends ASTVisitor
    {
        private final Map<ASTNode, List<Annotation>> _result;
        private AnnotatedNodeVisitor(Map<ASTNode, List<Annotation>> map)
        {
            _result = map;
        }

		/**
		 * visit package declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.PackageDeclaration node)
        {
			final List<Annotation> annotations = node.annotations();
			if( !annotations.isEmpty() )
				_result.put(node, annotations);

            return false;
        }

		/**
		 * visit class and interface declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.TypeDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit annotation type declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.AnnotationTypeDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit enum type declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.EnumDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit field declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.FieldDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit enum constant declaration
		 */
        public boolean visit(org.eclipse.jdt.core.dom.EnumConstantDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit method declaration
		 */
        public boolean visit(MethodDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

		/**
		 * visit annotation type member
		 */
        public boolean visit(AnnotationTypeMemberDeclaration node)
        {
            visitBodyDeclaration(node);
            return true;
        }

        private void visitBodyDeclaration(final BodyDeclaration node)
        {
            final List<IExtendedModifier> extMods = node.modifiers();
			List<Annotation> annos = null;
            for( IExtendedModifier extMod : extMods ){
                if( extMod.isAnnotation() ){
					if( annos == null ){
                        annos = new ArrayList<Annotation>(2);
                        _result.put(node, annos);
					}
                    annos.add((Annotation)extMod);
                }
            }
        }

		/**
		 * visiting formal parameter declaration.
		 */
		public boolean visit(SingleVariableDeclaration node)
		{
			final List<IExtendedModifier> extMods = node.modifiers();
			List<Annotation> annos = null;
            for( IExtendedModifier extMod : extMods ){
                if( extMod.isAnnotation() ){
					if( annos == null ){
                        annos = new ArrayList<Annotation>(2);
                        _result.put(node, annos);
					}
                    annos.add((Annotation)extMod);
                }
            }
			return false;
		}

		/**
		 * @return false so we skip everything beyond declaration level.
		 */
        public boolean visit(Block node)
        {   // so we don't look into anything beyond declaration level.
            return false;
        }
        public boolean visit(MarkerAnnotation node){ return false; }
        public boolean visit(NormalAnnotation node){ return false; }
        public boolean visit(SingleMemberAnnotation node){ return false; }

    }

    /**
     * Traverse the ast looking for annotations at the declaration level.
     */
    public static final class AnnotationVisitor extends ASTVisitor
    {
        final List<Annotation> _annotations;
        public AnnotationVisitor(final List<Annotation> annotations)
        { _annotations = annotations; }

        public boolean visit(MarkerAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }

        public boolean visit(SingleMemberAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }

        public boolean visit(NormalAnnotation annotation)
        {
            _annotations.add(annotation);
            return false;
        }


        // make sure we don't hit Arguments other than formal parameters.
        public boolean visit(Block blk){ return false; }
        public boolean visit(DoStatement doStatement){ return false; }
        public boolean visit(ForStatement forStatement){ return false; }
        public boolean visit(IfStatement ifStatement){ return false; }
        public boolean visit(TryStatement tryStatement){ return false; }
    }


    public Declarations getDeclarationUtils()
    {
        return new DeclarationsUtil();
    }

    public Filer getFiler()
    {
		checkValid();
        return _filer;
    }

    public EclipseMessager getMessager()
    {
		checkValid();
		return new MessagerImpl(this);
	}

    public Map<String, String> getOptions()
    {
        final HashMap<String, String> options = new HashMap<String, String>(2);
		options.put("phase", getPhase().toString());
		return options;
    }

    public PackageDeclaration getPackage(String name)
    {
		if (name == null)
			throw new IllegalArgumentException("name cannot be null");

		checkValid();
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
						if (tempClassFile.getElementName().indexOf("$") < 0) {
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
			return new PackageDeclarationImpl(binding.getPackage(), typeDecl, this, true);
		}

		// No classes or source files found
		return new PackageDeclarationImplNoBinding(pkgFrags, this);
    }

    public Collection<TypeDeclaration> getSpecifiedTypeDeclarations()
    {
        return getTypeDeclarations();
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

    public TypeDeclaration getTypeDeclaration(String name)
    {
		checkValid();
		if( name == null ) return null;
		// get rid of the generics parts.
		final int index = name.indexOf('<');
		if( index != -1 )
			name = name.substring(0, index);

		// first look into the current compilation unit
		final String typeKey = BindingKey.createTypeBindingKey(name);
		final ASTNode node = _astCompilationUnit.findDeclaringNode(typeKey);
		ITypeBinding typeBinding = null;
		if( node != null ){
			final int nodeType = node.getNodeType();
			if( nodeType == ASTNode.TYPE_DECLARATION ||
				nodeType == ASTNode.ANNOTATION_TYPE_DECLARATION ||
				nodeType == ASTNode.ENUM_DECLARATION )
			typeBinding = ((AbstractTypeDeclaration)node).resolveBinding();
		}
		if( typeBinding != null )
			return Factory.createReferenceType(typeBinding, this);

		// then go search for it else where.
		typeBinding = getTypeBinding(typeKey);
		if( typeBinding != null ){
			addTypeDependency( name );
			return Factory.createReferenceType(typeBinding, this);
		}

		return null;
    }

	public TypeDeclaration getTypeDeclaration(final IType type) {
		if (type == null) return null;
		String name = type.getFullyQualifiedName();
		return getTypeDeclaration(name);
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

	private List<ITypeBinding> getTypeBindings()
	{
		final List<AbstractTypeDeclaration> declTypes = _astCompilationUnit.types();
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
	private void getTypeBindings(final ITypeBinding type, final List<ITypeBinding> typeBindings)
	{
		if( type == null ) return;
		typeBindings.add(type);
		final ITypeBinding[] nestedTypes = type.getDeclaredTypes();
		for( ITypeBinding nestedType : type.getDeclaredTypes() ) {
			typeBindings.add(nestedType);
			getTypeBindings(nestedType, typeBindings);
		}
	}

    public Types getTypeUtils()
    {
		return new TypesUtil(this);
    }

    public void addListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if(_listeners == null )
			_listeners = new HashSet<AnnotationProcessorListener>();
		_listeners.add(listener);
    }

    public void removeListener(AnnotationProcessorListener listener)
    {
		checkValid();
        if( _listeners == null ) return;
		_listeners.remove(listener);
    }

	public Set<AnnotationProcessorListener> getProcessorListeners()
	{
		if( _listeners == null )
			return Collections.emptySet();
		return Collections.unmodifiableSet(_listeners);
	}

	public void addGeneratedFile( IFile f, boolean contentsChanged ) {
		_generatedFiles.put( f, contentsChanged );
	}

    public CompilationUnit  getAstCompilationUnit()    { return _astCompilationUnit; }
    public ICompilationUnit getCompilationUnit()       { return _compilationUnit; }
    public Phase            getPhase()                 { return _phase; }

    public IFile            getFile()                  { return _file; }
    public IProject         getProject()               { return _javaProject.getProject(); }
	public IJavaProject		getJavaProject()		   { return _javaProject; }
    public Map<IFile, Boolean>       getGeneratedFiles()        { return _generatedFiles; }

	/**
	 * @return true iff source files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedSourceFiles()		   { return !_generatedFiles.isEmpty();  }

	/**
	 * @return true iff class files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedClassFiles()			   { return _filer.hasGeneratedClassFile(); }

	/**
	 * @return true iff errors (markers with serverity == IMarker.SEVERITY_ERRROR) has been posted
	 *         Always return false when this environment is closed.
	 */
	public boolean hasRaisedErrors()
	{
		checkValid();
		for(MarkerInfo info : _markerInfos )
		{
			final Object val = info._markerAttrs.get(IMarker.SEVERITY);
			if( val != null && ((Integer)val).intValue() == IMarker.SEVERITY_ERROR )
				return true;
		}
		return false;
	}

    /**
     * @param binding must be correspond to a type, method or field declaration.
     * @return the ast node the corresponds to the declaration of the given binding.
     *         Return null if none is found.
     */
    public ASTNode getASTNodeForBinding(final IBinding binding)
    {
		final CompilationUnit astUnit = getCompilationUnitForBinding(binding);
		if( astUnit == null ) return null;
		return astUnit.findDeclaringNode(binding.getKey());
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
        ASTNode node = getAstCompilationUnit().findDeclaringNode(binding);
        if( node != null ) return getAstCompilationUnit();
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
		ASTNode node = getAstCompilationUnit().findDeclaringNode(binding);
		if( node != null ) return _file;
		else{
			final IMember member = (IMember)binding.getJavaElement();
			if( member != null ){
				final ICompilationUnit unit = member.getCompilationUnit();
				return (IFile)unit.getResource();
			}
			else{
				final ITypeBinding type = getDeclaringClass(binding);
				assert type.isTopLevel() : "type must be top-level type";
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
	}

	/**
	 * @param bindin a type, method or field binding.
	 * @return the top-level type binding that declares <code>binding</code>
	 * 	       or itself if it is already one.
	 */
	private ITypeBinding getDeclaringClass(final IBinding binding)
	{
		assert binding != null : "binding cannot be null";
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
			throw new IllegalStateException("unrecognized binding type " +  binding.getKind());
		}
		if(aTypeBinding == null ) return null;
		while( !aTypeBinding.isTopLevel() ){
			aTypeBinding = aTypeBinding.getDeclaringClass();
		}
		return aTypeBinding;
	}



	/**
	 *
	 * reads a given file's contents and returns them as a char array.
	 *
	 * @param file
	 * @return
	 * @throws CoreException
	 */
	public static char[] getFileContents( IFile file )
		throws CoreException, IOException
	{
		Reader   reader   = null;
		CharArrayWriter      w    = null;

		try
		{
			reader = getFileReader( file );
			w = new CharArrayWriter( 4096 );
			int c = -1;
			while ( ( c = reader.read() ) > -1 )
				w.write( c );
			return w.toCharArray();
		}
		finally
		{
			try { if ( reader != null ) reader.close(); } catch ( IOException ioe ) {};
			if ( w != null ) w.close();
		}
	}

	public static InputStreamReader getFileReader( final IFile file ) throws IOException, CoreException {
		return new InputStreamReader(getInputStream(file), file.getCharset());
	}

	public static InputStream getInputStream( final IFile file ) throws IOException, CoreException {
		return new BufferedInputStream(file.getContents());
	}

	/* (non-Javadoc)
	 *  Once the environment is closed the following is not allowed
	 *  1) posting messge
	 *  2) generating file
	 *  3) retrieving type or package by name
	 *  4) add or remove listeners
	 */
    public void close(){
        // post all of the messages
        postMarkers();

        _modelCompUnit2astCompUnit.clear();
		_markerInfos.clear();
		_generatedFiles.clear();
		if(_listeners != null)
			_listeners.clear();
		_isClosed = true;
    }

	private void checkValid()
	{
		if( _isClosed )
			throw new IllegalStateException("Environment has expired");
	}

	/**
	 * Add a marker to the environment.
	 * @param resource null to indicate the resource of the current compilation unit in this environment.
	 * @param markerAttrs the attributes to the marker
	 */
    void addMarker(final IResource resource, Map<String, Object> markerAttrs)
    {
		_markerInfos.add( new MarkerInfo(resource, markerAttrs));
    }

    void postMarkers()
    {
		// Posting all the markers to the workspace. Doing this in a batch process
		// to minimize the amount of notification.
		try{
			// the resource of the compilation unit in the environment.
			final IResource currentResource = _file;
	        final IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
	            public void run(IProgressMonitor monitor)
	            {
					final String markerType = _phase == Phase.RECONCILE ?
											  RECONCILE_MARKER : BUILD_MARKER;
	                for( MarkerInfo markerInfo : _markerInfos ){
	                    IResource resource = markerInfo._resource;
						if( resource == null )
							resource = currentResource;
						try{
		                    final IMarker marker = resource.createMarker(markerType);
		                    marker.setAttributes(markerInfo._markerAttrs);
						}
						catch(CoreException e){
							throw new IllegalStateException(e);
						}
	                }
	            };
	        };
			currentResource.getWorkspace().run(runnable, currentResource, IWorkspace.AVOID_UPDATE, null );

		}
		catch(CoreException e){
			throw new IllegalStateException(e);
		}
		finally{
			_markerInfos.clear();
		}
    }

	/**
	 * @return - the extra type dependencies for the file under compilation
	 */
	public Set<String> getTypeDependencies()  { return _typeDependencies; }

    private static class MarkerInfo
    {
        private final IResource _resource;
        private final Map<String, Object> _markerAttrs;

        private MarkerInfo(final IResource resource, final Map<String, Object> markerAttrs )
        {
            _resource = resource;
            _markerAttrs = markerAttrs;
        }
    }

	// Implementation for EclipseAnnotationProcessorEnvironment
	public CompilationUnit getAST()
	{
		if( _compilationUnit != null )
		{
			final ASTParser parser =  ASTParser.newParser(AST.JLS3);
            parser.setResolveBindings(false);
            parser.setSource(_compilationUnit);
            CompilationUnit resultUnit = (CompilationUnit)parser.createAST(null);
            return resultUnit;
		}
		else{
			// this is a fully-flushed out DOM/AST unlike the one that's current in the environment.
			// also this copy will not contain any binding information nor pointers to java element.
			ASTParser p = ASTParser.newParser( AST.JLS3 );
			p.setSource( _source );
			p.setResolveBindings( false );
			p.setProject( _javaProject );
			p.setUnitName( _file.getProjectRelativePath().toString() );
			p.setKind( ASTParser.K_COMPILATION_UNIT );
			ASTNode node = p.createAST( null );
			return (CompilationUnit)node;
		}
	}

	public void addTypeDependency(final String fullyQualifiedTypeName )
	{
		_typeDependencies.add( fullyQualifiedTypeName );
	}

	private void initPrimitives(final IJavaProject project)
	{
		if(_primitives != null ) return;
		_primitives = new PrimitiveTypeImpl[8];
		class PrimitiveBindingRequestor extends ASTRequestor
		{
			public void acceptBinding(String bindingKey, IBinding binding)
			{
				if( binding.getKind() == IBinding.TYPE ){
					if( "boolean".equals(binding.getName()) )
						_primitives[0] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "byte".equals(binding.getName()) )
						_primitives[1] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "char".equals(binding.getName()) )
						_primitives[2] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "double".equals(binding.getName()) )
						_primitives[3] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "float".equals(binding.getName()) )
						_primitives[4] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "int".equals(binding.getName()) )
						_primitives[5] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "long".equals(binding.getName()) )
						_primitives[6] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "short".equals(binding.getName()) )
						_primitives[7] = new PrimitiveTypeImpl( (ITypeBinding)binding );
					else if( "void".equals(binding.getName()) )
						_voidType = new VoidTypeImpl( (ITypeBinding)binding );
					else
						System.err.println("got unexpected type " + binding.getName());
				}
				else
					System.err.println("got unexpected binding " + binding.getClass().getName() + binding );
			}
		}

		final String[] keys = { BindingKey.createTypeBindingKey("boolean"),
				BindingKey.createTypeBindingKey("byte"),
				BindingKey.createTypeBindingKey("char"),
				BindingKey.createTypeBindingKey("double"),
				BindingKey.createTypeBindingKey("float"),
				BindingKey.createTypeBindingKey("int"),
				BindingKey.createTypeBindingKey("long"),
				BindingKey.createTypeBindingKey("short"),
				BindingKey.createTypeBindingKey("void")};

		final PrimitiveBindingRequestor requestor = new PrimitiveBindingRequestor();
		final ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setProject(project);
		parser.setResolveBindings(true);
		parser.createASTs(ProcessorEnvImpl.NO_UNIT, keys, requestor, null);
	}

	public PrimitiveTypeImpl getBooleanType(){ return _primitives[0]; }
	public PrimitiveTypeImpl getByteType(){ return _primitives[1]; }
	public PrimitiveTypeImpl getCharType(){ return _primitives[2]; }
	public PrimitiveTypeImpl getDoubleType(){ return _primitives[3]; }
	public PrimitiveTypeImpl getFloatType(){ return _primitives[4]; }
	public PrimitiveTypeImpl getIntType(){ return _primitives[5]; }
	public PrimitiveTypeImpl getLongType(){ return _primitives[6]; }
	public PrimitiveTypeImpl getShortType(){ return _primitives[7]; }
	public VoidTypeImpl getVoidType(){ return _voidType; }

	// End of implementation for EclipseAnnotationProcessorEnvironment
}
