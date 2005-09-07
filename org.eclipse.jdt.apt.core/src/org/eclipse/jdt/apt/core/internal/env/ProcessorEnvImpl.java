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

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
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
import org.eclipse.jdt.core.*;
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

import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class ProcessorEnvImpl extends BaseProcessorEnv implements EclipseAnnotationProcessorEnvironment
{
	public static final String BUILD_MARKER = "org.eclipse.jdt.apt.core.marker"; //$NON-NLS-1$
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];
	/** delimiter of path variables in -A values, e.g., %ROOT%/foo */
	private static final char PATHVAR_DELIM = '%';
	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
    
    private final ICompilationUnit _compilationUnit;       
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
	
	private boolean _batchMode = false; // off by default. 
	private char[] _curSource = null;
	/** 
	 * This is only non-null when <code>#_batchMode</code> is <code>true</code>
	 * If we are not in batch mode (reconcile time or file-based dispatch during build),
	 * <code>super._file</code> holds the file being processed at the time. 
	 */ 
	private IFile[] _files = null;
	/** 
	 * This is only non-null when <code>#_batchMode</code> is <code>true</code> *
	 * If we are not in batch mode, <code>super._astRoot</code> holds the current ast 
	 * being processed at the time.*/
	private CompilationUnit[] _astUnits = null;
	
	/** 
	 * The source to all of the compilation units in <code>_astUnits</code>
	 * This is only non-null when <code>#_batchMode</code> is <code>true</code> 
	 */
	private char[][] _sources = null;
	private List<MarkerInfo> _markerInfos = null;

	public static ProcessorEnvImpl newProcessorEnvironmentForReconcile(ICompilationUnit compilationUnit, IJavaProject javaProj)
    {	
    	String unitName =  compilationUnit.getResource().getProjectRelativePath().toString();
		ASTNode node = createDietAST( unitName, javaProj, compilationUnit, null );
       	return new ProcessorEnvImpl( 
       			(org.eclipse.jdt.core.dom.CompilationUnit)node, 
       			compilationUnit, null /*source*/, 
       			(IFile)compilationUnit.getResource(), 
       			javaProj, Phase.RECONCILE );
    }   
    
    public static ProcessorEnvImpl newProcessorEnvironmentForBuild(IFile[] files, IJavaProject javaProj )
    {
    	assert files != null : "missing files"; //$NON-NLS-1$    	
    
		// note, we are not reading any files.
		return new ProcessorEnvImpl(files, javaProj, Phase.BUILD);
    }
    
    private ProcessorEnvImpl(
    		final CompilationUnit astCompilationUnit,
    		final ICompilationUnit compilationUnit,
    		final char[] source,
    		final IFile file,
    		final IJavaProject javaProj,
    		final Phase phase)
    {
    	super(  astCompilationUnit, file, javaProj, phase );
    	
    	// if we are in reconcile, compilationUnit will be valid
		// if we are in build, file will not be null & compilationUnit will be
		// null
    	assert( (phase == Phase.RECONCILE && compilationUnit != null) || 
    			(phase == Phase.BUILD && compilationUnit == null && file != null ) ) :
    			"Unexpected phase value " + phase ; //$NON-NLS-1$
    	
    	assert (source == null && compilationUnit != null) ||
			   (source != null && compilationUnit == null) : 
	           "Unexpected values for _compilationUnit and _source!"; //$NON-NLS-1$
			   
	   _compilationUnit = compilationUnit;
	   _curSource = source;
	   _filer = new FilerImpl(this);
	   _allProblems = new HashMap<IFile, List<IProblem>>();
	   _markerInfos = new ArrayList<MarkerInfo>();
	   initOptions(javaProj);
    }
    
    private ProcessorEnvImpl(
			final IFile[] files, 
			final IJavaProject javaProj, 
			final Phase phase) {
    	
    	super(null, null, javaProj, phase);
    	assert( phase == Phase.BUILD && files != null  ) :
    		"Unexpected phase value " + phase; //$NON-NLS-1$
		
		_compilationUnit = null;
		_filer = new FilerImpl(this);
		_files = files;
		_allProblems = new HashMap<IFile, List<IProblem>>();
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
		ITypeBinding typeBinding = _astUnits[0].getAST().resolveWellKnownType(name);
		String typeKey = BindingKey.createTypeBindingKey(name);
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

    public ICompilationUnit getCompilationUnit(){ return _compilationUnit; }
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
		
		// TODO: also include markers
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
    	_files = null;
    	_sources = null;
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
    		
    	if( resource == null ){
    		assert _batchMode : "not in batch mode but don't know about current resource"; //$NON-NLS-1$
    		addMarker(start, end, severity, msg, line, arguments);
    	}
    	else
    		addProblem(resource, start, end, severity, msg, line, arguments);
    	
    }
    
    void addProblem(
    		IFile resource, 
		    int start, 
			int end,
            Severity severity, 
            String msg, 
            int line,
            String[] arguments)
    {
    	 
    	final APTProblem newProblem = 
        	new APTProblem(msg, severity, resource, start, end, line, arguments);
    	List<IProblem> problems = _allProblems.get(resource);
    	if( problems == null ){
    		problems = new ArrayList<IProblem>(4);
    		_allProblems.put(resource, problems);    		
    	}
    	problems.add(newProblem);
    }
    
    void addMarker(
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
    	return Collections.unmodifiableMap(_allProblems);
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
	 * associated with this environment.
	 * @param file2Annotations populated by this method to map files to the annotation types
	 *        if contains. May be null.
	 * @return the map containing all annotation types found within this environment.
	 */
    public Map<String, AnnotationTypeDeclaration> getAllAnnotationTypes(
    		final Map<IFile, Set<AnnotationTypeDeclaration>> file2Annotations) {
    	
    	checkValid();
    	if( _files == null )  
    		return getAnnotationTypesInFile();
    	readFiles();
    	
		final List<Annotation> instances = new ArrayList<Annotation>();
		final Map<String, AnnotationTypeDeclaration> decls = 
			new HashMap<String, AnnotationTypeDeclaration>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		for( int astIndex=0, len=_astUnits.length; astIndex<len; astIndex++ ){
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
				file2Annotations.put(_files[astIndex], perFileAnnos);
			visitor.reset();
		}
		
		return decls;
	}
    
    /**
     * Determine the ending offset of any problems on the current resource that doesn't have one by
     * traversing the ast for the. We will only walk the ast once to determine the ending 
     * offsets of all the marker infos that do not have the information set.
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
    		for( IProblem problem : entry.getValue() ){
    			if( problem.getSourceEnd() == -1 ){
    				count ++;
    			}
    		}
    		
    		if( count > 0 ){
    			final CompilationUnit astUnit = getAstCompilationUnit(file);
    			if( astUnit != null ){
    			
    				final int[] startingOffsets = new int[count];
    		    	int index = 0;
	    			for( IProblem problem : entry.getValue() ){
	    				if( problem.getSourceEnd() == -1 )
	    					startingOffsets[index++] = problem.getSourceStart();
	    			}
	    			
	    			final EndingOffsetFinder lfinder = new EndingOffsetFinder(startingOffsets);
	    			
	    			astUnit.accept( lfinder );
	    	    	
	    	    	for(IProblem problem : entry.getValue() ){
	    				if( problem.getSourceEnd() == -1 ){
	    					int startingOffset = problem.getSourceStart();
	    					int endingOffset = lfinder.getEndingOffset(startingOffset);
	    	    			if( endingOffset == 0 )
	    	    				endingOffset = startingOffset;
	    	    			problem.setSourceEnd(endingOffset);	    	    			
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
		if( _phase == Phase.RECONCILE )
			throw new IllegalStateException("No batch processing during reconcile."); //$NON-NLS-1$
		
		checkValid();
		readFiles();
		
		_batchMode = true;
		_file = null;
		_astRoot = null;
	}
	
	private void readFiles()
	{
		if( _astUnits != null || _files == null ) return;
		final int numFiles = _files.length;
		_astUnits = new CompilationUnit[numFiles]; 
		_sources = new char[numFiles][];
		for( int i=0; i<numFiles; i++){	
			try{
				_sources[i] = ProcessorEnvImpl.getFileContents( _files[i] );
				_astUnits[i] = (CompilationUnit)createDietAST(_files[i].toString(), _javaProject, null, _sources[i] );
			}
			catch( Exception e ){
				// TODO:  propagate these exceptions out of APTDispatch
				e.printStackTrace();
			}
		}
	}
	
	public void setFileProcessing(IFile file){		
		if( file == null )
			throw new IllegalStateException("missing file"); //$NON-NLS-1$
		// already in per-file mode.
		if( !_batchMode ){
			// this is a no-op
			if(  file.equals(_file) )
				return;
			
			_astRoot = null;
			_file = null;
			_curSource = null;
			
			// need to match up the file with the ast.
			if( _files != null ){
				for( int i=0, len=_files.length; i<len; i++ ){
					if( file.equals(_files[i]) ){
						_file = file;
						if( _astUnits != null ){
							_astRoot = _astUnits[i];		
							_curSource = _sources[i];
						}
						else{
							try{
								_curSource = ProcessorEnvImpl.getFileContents( _files[i] );
							}
							catch( Exception e ){
								// TODO:  propagate these exceptions out of APTDispatch
								e.printStackTrace();
							}
							_astRoot = (CompilationUnit)createDietAST(_files[i].toString(), _javaProject, null, _curSource );
						}
					}
				}
			}
 
			if( _file == null )
				throw new IllegalStateException(
						"file " +  //$NON-NLS-1$
						file.getName() + 
						" is not in the list to be processed."); //$NON-NLS-1$
		}
		else{
			_batchMode = false;
			if( _files != null ){
				for( int i=0, len=_files.length; i<len; i++ ){
					if( _files[i] == file ){
						try{
							_curSource = ProcessorEnvImpl.getFileContents( _files[i] );
						}
						catch( Exception e ){
							// TODO:  propagate these exceptions out of APTDispatch
							e.printStackTrace();
						}	
						_astRoot = (CompilationUnit)createDietAST(_files[i].toString(), _javaProject, null, _curSource );
						_file = file;
					}
				}
			}
			if( _astRoot == null )
				throw new IllegalStateException(
						"file " +  //$NON-NLS-1$
						file.getName() + 
						" is not in the list to be processed."); //$NON-NLS-1$
		}
	}
	
	// Implementation for EclipseAnnotationProcessorEnvironment
	public CompilationUnit getAST()
	{
		if( _batchMode ) return null;
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
			p.setSource( _curSource );
			p.setResolveBindings( false );
			p.setProject( _javaProject );
			p.setUnitName( _files[0].getProjectRelativePath().toString() );
			p.setKind( ASTParser.K_COMPILATION_UNIT );
			ASTNode node = p.createAST( null );
			return (CompilationUnit)node;
		}
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
	
	protected List<AbstractTypeDeclaration> searchLocallyForTypeDeclarations()
    {
		if( !_batchMode )
			return super.searchLocallyForTypeDeclarations();
		final List<AbstractTypeDeclaration> typeDecls = new ArrayList<AbstractTypeDeclaration>();
		for( int i=0, len=_astUnits.length; i<len; i++ )
        	typeDecls.addAll( _astUnits[i].types() );	
		return typeDecls;
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
				return _files[i];
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
				return _files[i];
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
    	else if( _batchMode ){
    		for( int i=0, len=_files.length; i<len; i++ ){
        		if( file.equals(_files[i]) )
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
	        IWorkspace ws;
	        if (_file != null) {
	        	ws = _file.getWorkspace();
	        }
	        else {
	        	ws = _files[0].getWorkspace(); 
	        }
			ws.run(runnable, null);
		}
		catch(CoreException e){
			AptPlugin.log(e, "Failed to post markers"); //$NON-NLS-1$
		}
		finally{
			_markerInfos.clear();
		}
    }
}