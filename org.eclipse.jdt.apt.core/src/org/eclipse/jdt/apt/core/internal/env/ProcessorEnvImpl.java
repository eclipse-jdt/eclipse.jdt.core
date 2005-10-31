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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.EclipseMirrorImpl;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotatedNodeVisitor;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.BindingKey;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.SimpleName;

import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class ProcessorEnvImpl extends BaseProcessorEnv implements EclipseAnnotationProcessorEnvironment
{
	private static final boolean ENABLE_GENERATED_FILE_LISTENER = false;
	public static final String BUILD_MARKER = "org.eclipse.jdt.apt.core.marker"; //$NON-NLS-1$
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];
	/** delimiter of path variables in -A values, e.g., %ROOT%/foo */
	private static final char PATHVAR_DELIM = '%';
	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
    
	/**
	 * The compilation unit of the file that is being processed in reconcile 
	 * or in file-based mode of build.  
	 */
    private ICompilationUnit _unit;       
    private Map<IFile, List<IProblem>> _allProblems;
    
	// Stores the generated files and whether or not they were modified. In this case,
	// new files will be considered "modified".
    private Map<IFile, Boolean> _generatedFiles = new HashMap<IFile, Boolean>();
	private Set<AnnotationProcessorListener> _listeners = null;
	private final FilerImpl _filer;
	private boolean _isClosed = false;

	/** true indicates that the source path for the project was modified during this APT dispatch */
	private boolean _sourcePathChanged;
	
	/**
	 * Set of strings that indicate new type dependencies introduced on the file
	 * each string is a fully-qualified type name.
	 */
	private Map<IFile, Set<String>> _typeDependencies = new HashMap<IFile, Set<String>>();
	
	/**
	 * Processor options, including -A options.
	 * Set in ctor and then not changed.
	 */
	private Map<String, String> _options;
	
	/**
	 * Indicates whether we are in batch mode or not. This gets flipped only 
	 * during build and could be flipped back and forth. 
	 */
	private boolean _batchMode = false; // off by default.	

	/** 
	 * Holds all the files that contains annotation that are to be processed during build.
	 * If we are not in batch mode (reconcile time or file-based dispatch during build),
	 * <code>super._file</code> holds the file being processed at the time. 
	 */ 
	private IFile[] _filesWithAnnotation = null;
	
	/**
	 * These are files that are part of a build but does not have annotations on it.
	 * During batch mode processing, these files still also need to be included. 
	 */
	private IFile[] _additionFiles = null;
	/** 
	 * This is intialized when <code>_batchMode</code> is set to be <code>true</code> or
	 * when batch processing is expected. @see #getAllAnnotationTypes(Map)
	 */
	private CompilationUnit[] _astUnits = null;
	/**
	 * <code>ICompilationUnit</code> parallel to the <code>CompilationUnit</code>s in 
	 * <code>_astUnits</code>
	 */
	private ICompilationUnit[] _units = null;
	private List<MarkerInfo> _markerInfos = null;

	public static ProcessorEnvImpl newReconcileEnv(ICompilationUnit compilationUnit, IJavaProject javaProj)
    {
		CompilationUnit domUnit = createDietAST( javaProj, compilationUnit );
       	return new ProcessorEnvImpl( domUnit, compilationUnit, javaProj);
    }
	
	/**
	 * @param filesWithAnnotation files that have annotation.
	 * @param units compilation unit associated with <code>filesWithAnnotation</code>
	 * @param javaProj
	 * @return a new processor environment.
	 */
	public static ProcessorEnvImpl newBuildEnvInternalRounding(
			IFile[] filesWithAnnotation,			
			ICompilationUnit[] units, 
			IJavaProject javaProj)
	{
		assert filesWithAnnotation != null : "missing files"; //$NON-NLS-1$
		return new ProcessorEnvImpl(filesWithAnnotation, null, units, javaProj, Phase.BUILD);
	}
    
    public static ProcessorEnvImpl newBuildEnv(
    		IFile[] filesWithAnnotation,
    		IFile[] additionalFiles,
    		IJavaProject javaProj )
    {
    	assert filesWithAnnotation != null : "missing files"; //$NON-NLS-1$    	
    
		// note, we are not reading any files.
		return new ProcessorEnvImpl(filesWithAnnotation, additionalFiles, null, javaProj, Phase.BUILD);
    }
    
    /** 
     * Constructor for creating a processor environment used during reconcile
     * @param astCompilationUnit
     * @param compilationUnit
     * @param file
     * @param javaProj
     * @param phase
     */
    private ProcessorEnvImpl(
    		final CompilationUnit astCompilationUnit,
    		final ICompilationUnit compilationUnit,
    		final IJavaProject javaProj )
    {
    	super(  astCompilationUnit, (IFile)compilationUnit.getResource(), javaProj, Phase.RECONCILE );
   
	   _unit = compilationUnit;	
	   _filer = new FilerImpl(this);
	   _allProblems = new HashMap<IFile, List<IProblem>>();	   
	   initOptions(javaProj);
    }
    
    /**
     * Constructor for creating a processor environment used during build.
     * @param filesWithAnnotations
     * @param additionalFiles
     * @param units
     * @param javaProj
     * @param phase
     */
    private ProcessorEnvImpl(
			final IFile[] filesWithAnnotations,
			final IFile[] additionalFiles, 
			final ICompilationUnit[] units,
			final IJavaProject javaProj,
			final Phase phase) {
    	
    	super(null, null, javaProj, phase);
    
		_unit = null;
		_units = units;
		_filer = new FilerImpl(this);
		_filesWithAnnotation = filesWithAnnotations;
		_additionFiles = additionalFiles;
		_allProblems = new HashMap<IFile, List<IProblem>>();
		_markerInfos = new ArrayList<MarkerInfo>();
		initOptions(javaProj);
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
	private void initOptions(IJavaProject jproj) {
		Map<String, String> procOptions = AptConfig.getProcessorOptions(jproj);
		_options = new HashMap<String, String>(procOptions.size() * 2);
		
		// Add configured options
		for (Map.Entry<String, String> entry : procOptions.entrySet()) {
			String value = resolveVarPath(entry.getValue());
			String key = entry.getKey();
			_options.put(key, value);
			if (!AptConfig.isAutomaticProcessorOption(key)) {
				String sunStyle;
				if (value != null) {
					sunStyle = "-A" + entry.getKey() + "=" + value; //$NON-NLS-1$ //$NON-NLS-2$
				}
				else {
					sunStyle = "-A" + entry.getKey(); //$NON-NLS-1$
				}
				_options.put(sunStyle, ""); //$NON-NLS-1$
			}
		}
	}

	/**
	 * If the value starts with a path variable such as %ROOT%, replace it with
	 * the absolute path.
	 * @param value the value of a -Akey=value command option
	 */
	private String resolveVarPath(String value) {
		if (value == null) {
			return null;
		}
		// is there a token to substitute?
		if (!Pattern.matches(PATHVAR_TOKEN, value)) {
			return value;
		}
		IPath path = new Path(value);
		String firstToken = path.segment(0);
		// If it matches %ROOT%/project, it is a project-relative path.
		if (PATHVAR_ROOT.equals(firstToken)) {
			IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
			IResource proj = root.findMember(path.segment(1));
			if (proj == null) {
				return value;
			}
			// all is well; do the substitution
			IPath relativePath = path.removeFirstSegments(2);
			IPath absoluteProjPath = proj.getLocation();
			IPath absoluteResPath = absoluteProjPath.append(relativePath);
			return absoluteResPath.toOSString();
		}
		
		// otherwise it's a classpath-var-based path.
		String cpvName = firstToken.substring(1, firstToken.length() - 1);
		IPath cpvPath = JavaCore.getClasspathVariable(cpvName);
		if (cpvPath != null) {
			IPath resolved = cpvPath.append(path.removeFirstSegments(1));
			return resolved.toOSString();
		}
		else {
			return value;
		}
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
        final HashMap<String, String> options = new HashMap<String, String>(_options);
		options.put("phase", getPhase().toString()); //$NON-NLS-1$
		return options;
    }

    public PackageDeclaration getPackage(String name)
    {
		checkValid();
		return super.getPackage(name);
    }

    public TypeDeclaration getTypeDeclaration(String name)
    {
		checkValid();		
		TypeDeclaration decl = null;
		if( !_batchMode ){
			// we are not keeping dependencies unless we are processing on a
			// per file basis.
			decl = super.getTypeDeclaration(name);			
			addTypeDependency( name );
		}
		else
			decl = getTypeDeclarationInBatch(name);
			
		return decl;
    }

    private TypeDeclaration getTypeDeclarationInBatch(String name)
    {	
    	if( name == null || _astUnits == null ) return null;
		// get rid of the generics parts.
		final int index = name.indexOf('<');
		if( index != -1 )
			name = name.substring(0, index);
		
		// first see if it is one of the well known types.
		// any AST is as good as the other.
		ITypeBinding typeBinding = null;
		String typeKey = BindingKey.createTypeBindingKey(name);
		if( _astUnits.length > 0 ){
			_astUnits[0].getAST().resolveWellKnownType(name);
			
			if(typeBinding == null){
				// then look into the current compilation units			
				ASTNode node = null;
				for( int i=0, len=_astUnits.length; i<len; i++ )
					node = _astUnits[i].findDeclaringNode(typeKey);			
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
		}

		// finally go search for it in the universe.
		typeBinding = getTypeBinding(typeKey);
		if( typeBinding != null ){			
			return Factory.createReferenceType(typeBinding, this);
		}

		return null;
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
		if( _generatedFiles.containsKey(f) ){
			// Could have generated the same file twice during one build.
			// The first time, contentsChanged = true, second time, if the file
			// is identical, contentsChanged will be false. 
			// Overall during this process, the file has been changed.
			boolean curValue = _generatedFiles.get(f);
			contentsChanged |= curValue;
		}
		
		_generatedFiles.put( f, contentsChanged );
	}

    public ICompilationUnit getCompilationUnit(){ return _unit; }
    public Map<IFile, Boolean> getGeneratedFiles(){ return _generatedFiles; }

	/**
	 * @return true iff source files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedSourceFiles(){ return !_generatedFiles.isEmpty();  }

	/**
	 * @return true iff class files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedClassFiles(){ return _filer.hasGeneratedClassFile(); }

	/**
	 * @return true iff errors (markers with serverity == APTProblem.Severity.Error) has been posted
	 *         Always return false when this environment is closed.
	 */
	public boolean hasRaisedErrors()
	{
		checkValid();
		for(List<IProblem> problems : _allProblems.values() )
		{
			for(IProblem problem : problems ){
				if( problem.isError() ) 
					return true;
			}		
		}
		
		if( _markerInfos != null ){
			for(MarkerInfo markerInfo : _markerInfos){
				if( markerInfo.isError() )
					return true;
			}
		}
		return false;
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
		Reader reader = null;
		CharArrayWriter w = null;

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
    	if( _isClosed ) 
    		return;
    	postMarkers();
    	_markerInfos = null;
    	_astRoot = null;
    	_file = null;
    	_astUnits = null;
    	_filesWithAnnotation = null;
    	_units = null;
    	_allProblems = null;
        _modelCompUnit2astCompUnit.clear();		
		_generatedFiles = null;
		if(_listeners != null)
			_listeners.clear();
		_isClosed = true;
    }

	/* package */ void checkValid()
	{
		if( _isClosed )
			throw new IllegalStateException("Environment has expired"); //$NON-NLS-1$
	}	
       
    
    /**
     * 
     * @param resource null to indicate current resource
     * @param start the starting offset of the marker
     * @param end -1 to indicate unknow ending offset.
     * @param severity the severity of the marker
     * @param msg the message on the marker
     * @param line the line number of where the marker should be
     */
    void addMessage(IFile resource, 
       		        int start, 
    				int end,
                    Severity severity, 
                    String msg, 
                    int line,
                    String[] arguments)
    {
    	checkValid();
    	
    	if( resource == null )
    		resource = getFile();
    	
    	// not going to post any markers to resource outside of the one we are currently 
    	// processing during reconcile phase.
    	if( _phase == Phase.RECONCILE && resource != null && !resource.equals( getFile() ) )
    		return;
    	
    	// Eclipse doesn't support INFO-level IProblems, so we send them to the log instead.
    	if ( _phase != Phase.RECONCILE && severity == Severity.INFO) {
    		StringBuilder sb = new StringBuilder();
    		sb.append("Informational message reported by annotation processor:\n"); //$NON-NLS-1$
    		sb.append(msg);
    		sb.append("\n"); //$NON-NLS-1$
    		if (resource != null) {
    			sb.append("Resource="); //$NON-NLS-1$
    			sb.append(resource.getName());
    			sb.append("; "); //$NON-NLS-1$
    		}
    		sb.append("starting offset="); //$NON-NLS-1$
    		sb.append(start);
    		sb.append("; ending offset="); //$NON-NLS-1$
    		sb.append(end);
    		sb.append("; line="); //$NON-NLS-1$
    		sb.append(line);
    		if (arguments != null) {
    			sb.append("; arguments:"); //$NON-NLS-1$
    			for (String s : arguments) {
    				sb.append("\n"); //$NON-NLS-1$
    				sb.append(s);
    			}
    		}
    		else {
    			sb.append("\n"); //$NON-NLS-1$
    		}
    		IStatus status = AptPlugin.createInfoStatus(null, sb.toString());
    		AptPlugin.log(status);
    		return;
    	}
    	
    	if( resource == null ){
    		assert _batchMode : "not in batch mode but don't know about current resource"; //$NON-NLS-1$
    		addMarker(start, end, severity, msg, line, arguments);
    	}
    	else    	
    		addProblem(resource, start, end, severity, msg, line, arguments);
    	
    }
    
    private void addProblem(
    		IFile resource, 
		    int start, 
			int end,
            Severity severity, 
            String msg, 
            int line,
            String[] arguments)
    {	 
    	// end-1 since IProblem ending offsets are inclusive but DOM layer
    	// ending offsets are exclusive.
    	final APTProblem newProblem = 
        	new APTProblem(msg, severity, resource, start, end-1, line, arguments);
    	List<IProblem> problems = _allProblems.get(resource);
    	if( problems == null ){
    		problems = new ArrayList<IProblem>(4);
    		_allProblems.put(resource, problems);    		
    	}
    	problems.add(newProblem);
    }
    
    private void addMarker(
    		int start, 
			int end,
            Severity severity, 
            String msg, 
            int line,
            String[] arguments)
    {    	
    	// Note that the arguments are ignored -- no quick-fix for markers.
    	_markerInfos.add(new MarkerInfo(start, end, severity, msg, line));
    }
    
    public Map<IFile, List<IProblem>> getProblems()
    {
    	checkValid();
    	
    	updateProblemLength();
    	return _allProblems;
    }
    
    public Map<String, AnnotationTypeDeclaration> getAnnotationTypesInFile()
    {
    	checkValid();
    	assert _astRoot != null && _file != null && !_batchMode : 
    		"operation not available under batch mode."; //$NON-NLS-1$
    	final List<Annotation> instances = new ArrayList<Annotation>();
		final Map<String, AnnotationTypeDeclaration> decls = 
			new HashMap<String, AnnotationTypeDeclaration>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		_astRoot.accept(visitor);
			
		for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
			final Annotation instance = instances.get(instanceIndex);
			final ITypeBinding annoType = instance.resolveTypeBinding();
			if (annoType == null)
				continue;
			final TypeDeclarationImpl decl = 
				Factory.createReferenceType(annoType, this);
			if (decl.kind() == EclipseMirrorImpl.MirrorKind.TYPE_ANNOTATION){
				final AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)decl;
				decls.put(annoDecl.getQualifiedName(), annoDecl);
			}
		}
		
		return decls;
    }
    
    /**
	 * Return all annotations at declaration level within all compilation unit(s)
	 * associated with this environment. All the files associated with this environment will 
	 * be parsed and resolved for all declaration level elements at the return of this call.
	 * 
	 * @param file2Annotations populated by this method to map files to the annotation types
	 *        if contains. May be null.
	 * @return the map containing all annotation types found within this environment.
	 */
    public Map<String, AnnotationTypeDeclaration> getAllAnnotationTypes(
    		final Map<IFile, Set<AnnotationTypeDeclaration>> file2Annotations) {
    	
    	checkValid();
    	if( _filesWithAnnotation == null )  
    		return getAnnotationTypesInFile();
    	createDomASTs();
    	
		final List<Annotation> instances = new ArrayList<Annotation>();
		final Map<String, AnnotationTypeDeclaration> decls = 
			new HashMap<String, AnnotationTypeDeclaration>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		for( int astIndex=0, len=_astUnits.length; astIndex<len; astIndex++ ){
			if( _astUnits == null || _astUnits[astIndex] == null  )
				System.err.println();
			_astUnits[astIndex].accept(visitor);
			final Set<AnnotationTypeDeclaration> perFileAnnos = new HashSet<AnnotationTypeDeclaration>(); 
			
			for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
				final Annotation instance = instances.get(instanceIndex);
				final ITypeBinding annoType = instance.resolveTypeBinding();
				if (annoType == null)
					continue;
				final TypeDeclarationImpl decl = 
					Factory.createReferenceType(annoType, this);
				if (decl.kind() == EclipseMirrorImpl.MirrorKind.TYPE_ANNOTATION){
					final AnnotationTypeDeclaration annoDecl = (AnnotationTypeDeclaration)decl;
					decls.put(annoDecl.getQualifiedName(), annoDecl);
					perFileAnnos.add(annoDecl);
				}
			}
			if( file2Annotations != null && !perFileAnnos.isEmpty() )
				file2Annotations.put(_filesWithAnnotation[astIndex], perFileAnnos);
			visitor.reset();
		}
		
		return decls;
	}
    
    /**
     * @param file
     * @return length 3 int array with the following information.
     * at index 0: contains the starting offset, always >= 0
     * at index 1: contains the ending offset, may be a negative number.
     * at index 2: the line number
     * 
     */
    private int[] getClassNameRange(final IFile file){
    	final CompilationUnit astUnit = getAstCompilationUnit(file);
    	int[] startAndEnd = null;
    	if( astUnit != null){
    		@SuppressWarnings({"unchecked", "nls"})
    		final List<AbstractTypeDeclaration> topTypes = astUnit.types();
    		if( topTypes != null && topTypes.size() > 0 ){
    			final AbstractTypeDeclaration topType = topTypes.get(0);
    			startAndEnd = new int[3];
    			final SimpleName typename = topType.getName();
    			if( typename != null ){
    				startAndEnd[0] = typename.getStartPosition();
    				// ending offsets need to be exclusive.
    				startAndEnd[1] = startAndEnd[0] + typename.getLength() - 1;
    				startAndEnd[2] = astUnit.lineNumber(typename.getStartPosition());
    			}
    			else{
    				startAndEnd[0] = topType.getStartPosition();
    				// let case 2 in updateProblemLength() kicks in. 
    				startAndEnd[1] = -2;
    				startAndEnd[2] = astUnit.lineNumber(topType.getStartPosition());
    			}
    		}
    	}
    	if( startAndEnd == null )
    		// let case 2 in updateProblemLength() kicks in.
    		return new int[]{0, -2, 1};
    
    	return startAndEnd;
    }
    
    /**
     * Handling the following 2 cases
     * 1) For IProblems that does not have a starting and ending offset, 
     * place the problem at the class name. 
     * 
     * 2) For IProblems that does not have an ending offset, place the ending
     * offset at the end of the tightest ast node. 
     * We will only walk the ast once to determine the ending 
     * offsets of all the problems that do not have the information set. 
     */
    private void updateProblemLength()
    {	
    	// for those problems that doesn't have an ending offset, figure it out by
    	// traversing the ast.
    	// we do it once just before we post the marker so we only have to walk the ast 
    	// once.
    	for( Map.Entry<IFile, List<IProblem>> entry : _allProblems.entrySet() ){
    		int count = 0;
    		final IFile file = entry.getKey();
    		int[] classNameRange = null;
    		for( IProblem problem : entry.getValue() ){
    			if( problem.getSourceStart() < 0 ){
    				if( classNameRange == null )
    					classNameRange = getClassNameRange(file);
    				problem.setSourceStart(classNameRange[0]);
    				problem.setSourceEnd(classNameRange[1]);
    				problem.setSourceLineNumber(classNameRange[2]);
    			}
    			if( problem.getSourceEnd() < 0 ){
    				count ++;
    			}
    		}
    		
    		if( count > 0 ){
    			final CompilationUnit astUnit = getAstCompilationUnit(file);
    			if( astUnit != null ){
    			
    				final int[] startingOffsets = new int[count];
    		    	int index = 0;
	    			for( IProblem problem : entry.getValue() ){
	    				if( problem.getSourceEnd() < 0 )
	    					startingOffsets[index++] = problem.getSourceStart();
	    			}
	    			
	    			final EndingOffsetFinder lfinder = new EndingOffsetFinder(startingOffsets);
	    			
	    			astUnit.accept( lfinder );
	    	    	
	    	    	for(IProblem problem : entry.getValue() ){
	    				if( problem.getSourceEnd() < 0 ){
	    					int startingOffset = problem.getSourceStart();
	    					int endingOffset = lfinder.getEndingOffset(startingOffset);
	    	    			if( endingOffset == 0 )
	    	    				endingOffset = startingOffset;
	    	    			problem.setSourceEnd(endingOffset-1);
	    				}
	    			}
    			}
    			else{
        			for(IProblem problem : entry.getValue() ){
        				// set the -1 source end to be the same as the source start.
        				if( problem.getSourceEnd() < problem.getSourceStart() )
        					problem.setSourceEnd(problem.getSourceStart());
        			}
        		}
    		}
    		
    	}
    }
    
    /**
     * Responsible for finding the ending offset of the ast node that has the tightest match 
     * for a given offset. This ast visitor can operator on an array of offsets in one pass.   
     * @author tyeung     
     */
    private static class EndingOffsetFinder extends ASTVisitor 
    {
    	private final int[] _sortedStartingOffset;
    	/** 
    	 * parallel to <code>_sortedOffsets</code> and contains 
    	 * the ending offset of the ast node that has the tightest match for the 
    	 * corresponding starting offset.
    	 */
    	private final int[] _endingOffsets;
    	
    	/**
    	 * @param offsets the array of offsets which will be sorted.
    	 * @throws IllegalArgumentException if <code>offsets</code> is <code>null</code>.
    	 */
    	private EndingOffsetFinder(int[] offsets)
    	{
    		if(offsets == null)
    			throw new IllegalArgumentException("argument cannot be null."); //$NON-NLS-1$
    		// sort the array first
    		Arrays.sort(offsets);
    	
    		// look for duplicates.		
    		int count = 0;	
    		for( int i=0, len=offsets.length; i<len; i++){
    			if( i == 0 ) ; // do nothing				
    			else if( offsets[i-1] == offsets[i] )
    				continue;			
    			count ++;
    		}	
    	
    		if( count != offsets.length ){
    			_sortedStartingOffset = new int[count];
    	
    			int index = 0;
    			for( int i=0, len=offsets.length; i<len; i++){
    				if( i != 0 && offsets[i-1] == offsets[i] )
    					continue;
    				_sortedStartingOffset[index++] = offsets[i];
    			}		
    		}
    		else{
    			_sortedStartingOffset = offsets;
    		}
    		
    		_endingOffsets = new int[count];
    		for( int i=0; i<count; i++ )
    			_endingOffsets[i] = 0;
    	}
    	
    	public void preVisit(ASTNode node) 
    	{
    		final int startingOffset = node.getStartPosition();
    		final int endingOffset = startingOffset + node.getLength();
    		// starting offset is inclusive
    		int startIndex = Arrays.binarySearch(_sortedStartingOffset, startingOffset);
    		// ending offset is exclusive
    		int endIndex = Arrays.binarySearch(_sortedStartingOffset, endingOffset);
    		if( startIndex < 0 )
    			startIndex = - startIndex - 1;		
    		if( endIndex < 0 )
    			endIndex = - endIndex - 1;
    		else 
    			// endIndex needs to be exclusive and we want to 
    			// include the 'endIndex'th entry in our computation.
    			endIndex ++; 
    		if( startIndex >= _sortedStartingOffset.length )
    			return;
    		
    		for( int i=startIndex; i<endIndex; i++ ){    			
    			if( _endingOffsets[i] == 0 )
    				_endingOffsets[i] = endingOffset;
    			else if( endingOffset < _endingOffsets[i] )
    				_endingOffsets[i] = endingOffset;
    		}
    	}
    	
    	
    	public int getEndingOffset(final int startingOffset)
    	{
    		int index = Arrays.binarySearch(_sortedStartingOffset, startingOffset);
    		if( index == -1 ) return 0;
    		return _endingOffsets[index];
    	}
    }

	/**
	 * @return - the extra type dependencies for the files under compilation
	 */
	public Map<IFile, Set<String>> getTypeDependencies()  { return _typeDependencies; }
	
	/** true value indicates that the source path for the project changed during this APT dispatch */
	public boolean getSourcePathChanged() { return _sourcePathChanged; }

	/** true value indicates that the source path for the project changed during this APT dispatch */
	public void setSourcePathChanged( boolean b ) { _sourcePathChanged = b; }
	
	/**
	 * Switch to batch processing mode. 
	 * Note: Call to this method will cause all files associated with this environment to be 
	 * read and parsed.
	 */
	public void setBatchProcessing(){		
		if( _phase != Phase.BUILD )
			throw new IllegalStateException("No batch processing outside build."); //$NON-NLS-1$
		
		if( _batchMode ) return;
		checkValid();
		createDomASTs();
		
		_batchMode = true;
		_file = null;
		_astRoot = null;
	}
	
	private void createDomASTs()
	{
		if( _astUnits != null || _filesWithAnnotation == null) return;
		createICompilationUnits();		
		_astUnits = createDietASTs(_javaProject, _units);
	}
	
	public void setFileProcessing(IFile file){		
		if( file == null )
			throw new IllegalStateException("missing file"); //$NON-NLS-1$
		_batchMode = false;
		if( file.equals(_file) ) // this is a no-op
			return;
		
		_astRoot = null;
		_file = null;
		_unit = null;
		
		// need to match up the file with the ast.
		if( _filesWithAnnotation != null ){
			for( int i=0, len=_filesWithAnnotation.length; i<len; i++ ){
				if( file.equals(_filesWithAnnotation[i]) ){
					_file = file;
					if( _astUnits != null ){
						_astRoot = _astUnits[i];		
						_unit = _units[i];
					}
					else{
						_unit = JavaCore.createCompilationUnitFrom(_filesWithAnnotation[i]);
						_astRoot = createDietAST(_javaProject, _unit);
					}
				}
			}
		}
		
		if( _file == null || _astRoot == null)
			throw new IllegalStateException(
					"file " +  //$NON-NLS-1$
					file.getName() + 
					" is not in the list to be processed."); //$NON-NLS-1$
	}
	
	// Implementation for EclipseAnnotationProcessorEnvironment
	public CompilationUnit getAST()
	{
		if( _batchMode ) return null;
		final ASTParser parser =  ASTParser.newParser(AST.JLS3);
        parser.setResolveBindings(false);
        parser.setSource(_unit);
        CompilationUnit resultUnit = (CompilationUnit)parser.createAST(null);
        return resultUnit;
	}

	public void addTypeDependency(final String fullyQualifiedTypeName )
	{
		if(!_batchMode){
			Set<String> deps = _typeDependencies.get(_file);
			if( deps == null ){
				deps = new HashSet<String>(4);
				_typeDependencies.put(_file, deps);
			}
			deps.add( fullyQualifiedTypeName );
		}
	}
	// End of implementation for EclipseAnnotationProcessorEnvironment
	
	/**
	 * Include all the types from all files, files with and without annotations on it
	 * if we are in batch mode. Otherwise, just the types from the file that's currently
	 * being processed.
	 */
	protected List<AbstractTypeDeclaration> searchLocallyForTypeDeclarations()
    {
		if( !_batchMode )
			return super.searchLocallyForTypeDeclarations();
		final List<AbstractTypeDeclaration> typeDecls = new ArrayList<AbstractTypeDeclaration>();
		for( int i=0, len=_astUnits.length; i<len; i++ )
        	typeDecls.addAll( _astUnits[i].types() );
		
		getTypeDeclarationsFromAdditionFiles(typeDecls);
		
		return typeDecls;
    }
	
	private void getTypeDeclarationsFromAdditionFiles(List<AbstractTypeDeclaration> typeDecls){
		if( _additionFiles == null || _additionFiles.length == 0 ) return;
	
		ICompilationUnit[] units = createICUsFrom(_additionFiles);
		final int actualLen = units.length;
		final int numFiles = _additionFiles.length;
		if( actualLen == 0 )
			return;
		
		// We are simply silently dropping files that doesn't have a compilation unit.
		// This most like means the file has been deleted.
		if( numFiles != actualLen ){
			final ICompilationUnit[] newUnits = new ICompilationUnit[actualLen];
			int newIndex = 0;
			for( ICompilationUnit unit : units ){
				if( unit != null )
					newUnits[newIndex ++] = unit;
			}
			units = newUnits;
		}
		final CompilationUnit[] domUnits = createDietASTs(_javaProject, units);
		for( CompilationUnit domUnit : domUnits ){
			if( domUnit != null ){
				typeDecls.addAll( domUnit.types() );
			}
		}
	}
	
	protected Map<ASTNode, List<Annotation>> getASTNodesWithAnnotations()
    {
		if( !_batchMode )
			return super.getASTNodesWithAnnotations();
    	final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap<ASTNode, List<Annotation>>();
        final AnnotatedNodeVisitor visitor = new AnnotatedNodeVisitor(astNode2Anno);        
        for( int i=0, len=_astUnits.length; i<len; i++ )
        	_astUnits[i].accept( visitor );
        return astNode2Anno;
    }
	
	protected IFile getFileForNode(final ASTNode node)
	{
		if( !_batchMode )
			return super.getFileForNode(node);
		final CompilationUnit curRoot = (CompilationUnit)node.getRoot();
		for( int i=0, len=_astUnits.length; i<len; i++ ){
			if( _astUnits[i] == curRoot )
				return _filesWithAnnotation[i];
		}
		throw new IllegalStateException();
	}
	
	/**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @param binding 
	 * @return the compilation unit that defines the given binding or null if no 
	 * match is found.
	 */
	protected CompilationUnit searchLocallyForBinding(final IBinding binding)
	{
		if( !_batchMode )
			return super.searchLocallyForBinding(binding);
		
		for( int i=0, len=_astUnits.length; i<len; i++ ){
			ASTNode node = _astUnits[i].findDeclaringNode(binding);
			if( node != null)
				return _astUnits[i];
		}
		return null;
	}
	
	/**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @param binding 
	 * @return the compilation unit that defines the given binding or null if no 
	 * match is found.
	 */
	protected IFile searchLocallyForIFile(final IBinding binding)
	{
		if( !_batchMode )
			return super.searchLocallyForIFile(binding);
		
		for( int i=0, len=_astUnits.length; i<len; i++ ){
			ASTNode node = _astUnits[i].findDeclaringNode(binding);
			if( node != null)
				return _filesWithAnnotation[i];
		}
		return null;
	}
	
	public ICompilationUnit getICompilationUnitForFile(final IFile file){
		if( file == null ) 
    		return null;
    	else if( file.equals(_file) )
    		return _unit;
    	else if( _units != null ){
    		for( int i=0, len=_filesWithAnnotation.length; i<len; i++ ){
        		if( file.equals(_filesWithAnnotation[i]) )
        			return _units[i];
        	}
    	}
    	return null;
	}
	
	/**
     * @param file
     * @return the compilation unit associated with the given file.
     * If the file is not one of those that this environment is currently processing,
     * return null;
     */
	public CompilationUnit getAstCompilationUnit(final IFile file)
	{
		if( file == null ) 
    		return null;
    	else if( file.equals(_file) )
    		return _astRoot;
    	else if( _astUnits != null ){
    		for( int i=0, len=_filesWithAnnotation.length; i<len; i++ ){
        		if( file.equals(_filesWithAnnotation[i]) )
        			return _astUnits[i];
        	}
    	}
    	return null;
	}
	
	/**
	 * @return the current ast being processed if in per-file mode.
	 * If in batch mode, one of the asts being processed (no guarantee which
	 * one will be returned.  
	 */
	protected AST getCurrentDietAST(){
		
		if( _astRoot != null )
			return _astRoot.getAST();
		else{
			if( _astUnits == null )
				throw new IllegalStateException("no AST is available"); //$NON-NLS-1$
			return _astUnits[0].getAST();
		}
	}
	
	void postMarkers()
    {
		if( _markerInfos == null || _markerInfos.size() == 0 )
			return;
		// Posting all the markers to the workspace. Doing this in a batch process
		// to minimize the amount of notification.
		try{
	        final IWorkspaceRunnable runnable = new IWorkspaceRunnable(){
	            public void run(IProgressMonitor monitor)
	            {		
	                for( MarkerInfo markerInfo : _markerInfos ){	                  
						try{
		                    final IMarker marker = _javaProject.getProject().createMarker(BUILD_MARKER);
							//final IMarker marker = _javaProject.getProject().createMarker(IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER);
		                    markerInfo.copyIntoMarker(marker);
						}
						catch(CoreException e){
							AptPlugin.log(e, "Failure posting markers"); //$NON-NLS-1$
						}
	                }
	            };
	        };
	        IWorkspace ws = _javaProject.getProject().getWorkspace();
			ws.run(runnable, null);
		}
		catch(CoreException e){
			AptPlugin.log(e, "Failed to post markers"); //$NON-NLS-1$
		}
		finally{
			_markerInfos.clear();
		}
    }
	
	public IFile[] getFiles()
	{
		if(_filesWithAnnotation != null)
			return _filesWithAnnotation;
		else
			return new IFile[]{_file};
	}
	
	private static ICompilationUnit[] createICUsFrom(final IFile[] files){
		final int len = files.length;
		if( len == 0 )
			return NO_UNIT;
		final ICompilationUnit[] units = new ICompilationUnit[len];
		for( int i=0; i<len; i++ ){
			units[i] = JavaCore.createCompilationUnitFrom(files[i]);
		}
		return units;
	}
	
	private int getNumberNonNulls(final ICompilationUnit[] units){
		int counter = 0;
		for(ICompilationUnit unit : units ){
			if( unit != null )
				counter ++;
		}
		return counter;
	}
	
	
	/**
	 * Build <code>ICompilationUnit</code> from the files with annotations in this environment.
	 * If a compilation unit cannot be created from a file, the file will be 
	 * dropped from the file list.
	 */
	private void createICompilationUnits(){
		if(_units != null) 
			return;
		_units = createICUsFrom(_filesWithAnnotation);
		
		// drop files that doesn't have an ICompilationUnit from the list 
		// and shrink the list of ICompilationUnits.
		final int actualLen = getNumberNonNulls(_units);
		if(actualLen != _filesWithAnnotation.length ){
			final IFile[] newFiles = new IFile[actualLen];
			final ICompilationUnit[] newUnits = new ICompilationUnit[actualLen];
			int newIndex = 0;
			for( int i=0; i<_filesWithAnnotation.length ; i++ ){
				if( _units[i] != null ){
					newFiles[newIndex] = _filesWithAnnotation[i];
					newUnits[newIndex] = _units[i];
					newIndex ++;
				}
			}
			_filesWithAnnotation = newFiles;
			_units = newUnits;
		}
	}
}