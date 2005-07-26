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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.type.PrimitiveTypeImpl;
import org.eclipse.jdt.apt.core.internal.type.VoidTypeImpl;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.EclipseMessager;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.apt.AnnotationProcessorListener;
import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;

public class ProcessorEnvImpl extends BaseProcessorEnv implements EclipseAnnotationProcessorEnvironment
{	
	public static final ICompilationUnit[] NO_UNIT = new ICompilationUnit[0];

	/** delimiter of path variables in -A values, e.g., %ROOT%/foo */
	private static final char PATHVAR_DELIM = '%';
	/** regex to identify substituted token in path variables */
	private static final String PATHVAR_TOKEN = "^%[^%/\\\\ ]+%.*"; //$NON-NLS-1$
	/** path variable meaning "workspace root" */
	private static final String PATHVAR_ROOT = "%ROOT%"; //$NON-NLS-1$
    
    private final ICompilationUnit _compilationUnit;       
    private Map<IFile, List<IProblem>> _allProblems;
    
	/**
	 * The source code in <code>_file</code>.
	 * This is the exact same source code that created the dom compilation unit
	 */
	private final char[] _source;	
	// Stores the generated files and whether or not they were modified. In this case,
	// new files will be considered "modified".
    private final Map<IFile, Boolean> _generatedFiles = new HashMap<IFile, Boolean>();
	private Set<AnnotationProcessorListener> _listeners = null;
	private final FilerImpl _filer;
	private boolean _isClosed = false;

	/** true indicates that the source path for the project was modified during this APT dispatch */
	private boolean _sourcePathChanged;
	
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
	 * Processor options, including -A options.
	 * Set in ctor and then not changed.
	 */
	private Map<String, String> _options;

	public static ProcessorEnvImpl newProcessorEnvironmentForReconcile(ICompilationUnit compilationUnit, IJavaProject javaProj)
    {	
    	String unitName =  compilationUnit.getResource().getProjectRelativePath().toString();
		ASTNode node = createDietAST( unitName, javaProj, compilationUnit, null );
       	return new ProcessorEnvImpl( (org.eclipse.jdt.core.dom.CompilationUnit)node,
       								  compilationUnit, null /*source*/,        								  
       								  (IFile)compilationUnit.getResource(), 
       								  javaProj, Phase.RECONCILE );
    }

    public static ProcessorEnvImpl newProcessorEnvironmentForBuild( IFile file, IJavaProject javaProj )
    {
    	char[] source = null;
		try{
			source = getFileContents( file );
		}
		catch( Exception e ){
			// TODO:  propagate these exceptions out of APTDispatch
			e.printStackTrace();
		}		
		String unitName = file.getProjectRelativePath().toString();
		ASTNode node = createDietAST( unitName, javaProj, null, source );
    	return new ProcessorEnvImpl((org.eclipse.jdt.core.dom.CompilationUnit)node, 
    			  					null /*ICompilationUnit*/, 
    							    source, file, javaProj, Phase.BUILD );
    }
    
    private ProcessorEnvImpl(final CompilationUnit astCompilationUnit,
			final ICompilationUnit compilationUnit, final char[] source,
			final IFile file, final IJavaProject javaProj, final Phase phase) {
		super(astCompilationUnit, file, javaProj, phase);
		// if we are in reconcile, compilationUnit will be valid
		// if we are in build, file will not be null & compilationUnit will be
		// null
		assert (phase == Phase.RECONCILE && compilationUnit != null) ||
				(phase == Phase.BUILD && compilationUnit == null && file != null) : 
				"Unexpected phase value " //$NON-NLS-1$
				+ phase;
		assert (source == null && compilationUnit != null) ||
				(source != null && compilationUnit == null) : 
				"Unexpected values for _compilationUnit and _source!"; //$NON-NLS-1$
		_source = source;
		_compilationUnit = compilationUnit;
		_allProblems = new HashMap<IFile, List<IProblem>>(4);
		_filer = new FilerImpl(this);
		initOptions(_javaProject);
	}
    
    
    /**
     * Set the _options map based on the current project/workspace settings.
     * There is a bug in Sun's apt implementation: it parses the command line 
     * incorrectly, such that -Akey=value gets added to the options map as 
     * key "-Akey=value" and value "".  In order to support processors written 
     * to run on Sun's apt as well as processors written without this bug
     * in mind, we populate the map with two copies of every option, one the
     * expected way ("key" / "value") and the other the Sun way 
     * ("-Akey=value" / ""). 
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
			_options.put(entry.getKey(), value);
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
		TypeDeclaration decl = super.getTypeDeclaration(name);
		if( decl != null)
			addTypeDependency( name );
		return decl;
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

    public ICompilationUnit getCompilationUnit()       { return _compilationUnit; }
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
    	_allProblems = null;
        _modelCompUnit2astCompUnit.clear();		
		_generatedFiles.clear();
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
    void addProblem(IFile resource, 
       		        int start, 
    				int end,
                    APTProblem.Severity severity, 
                    String msg, 
                    int line,
                    String[] arguments)
    {
    	checkValid();
    	// not going to post any markers to resource outside of the one we are currently 
    	// processing during reconcile phase.
    	if( _phase == Phase.RECONCILE && resource != null && !resource.equals(_file) )
    		return;
    	if(resource == null)
    		resource = _file;
    	final APTProblem newProblem = 
        	new APTProblem(msg, severity, resource, start, end, line, arguments);
    	List<IProblem> problems = _allProblems.get(resource);
    	if( problems == null ){
    		problems = new ArrayList<IProblem>(4);
    		_allProblems.put(resource, problems);    		
    	}
    	problems.add(newProblem);
    }
    
    public Map<IFile, List<IProblem>> getProblems()
    {
    	checkValid();
    	
    	updateProblemLength();
    	return Collections.unmodifiableMap(_allProblems);
    }   
    
    /**
     * Determine the ending offset of any problems on the current resource that doesn't have one by
     * traversing the ast for the. We will only walk the ast once to determine the ending 
     * offsets of all the marker infos that do not have the information set.
     */
    private void updateProblemLength()
    {
    	// for those markers that doesn't have an ending offset, figure it out by
    	// traversing the ast.
    	// we do it once just before we post the marker so we only have to walk the ast 
    	// once.
    	int count = 0;
    	for( Map.Entry<IFile, List<IProblem>> entry : _allProblems.entrySet() ){  
    		if( _file.equals(entry.getKey()) ){
    			for(IProblem problem : entry.getValue() ){
    				if( problem.getSourceEnd() == -1 )
    					count ++;
    			}    				
    		}
    		else{
    			for(IProblem problem : entry.getValue() ){
    				if( problem.getSourceEnd() < problem.getSourceStart() )
    					problem.setSourceEnd(problem.getSourceStart());
    			}
    		}
    	}
    	if( count > 0 ){
	    	final int[] startingOffsets = new int[count];
	    	int index = 0;
	    	
	    	for( Map.Entry<IFile, List<IProblem>> entry : _allProblems.entrySet() ){  
	    		if( entry.getKey() == _file ){
	    			for(IProblem problem : entry.getValue() ){
	    				if( problem.getSourceEnd() == -1 )
	    					startingOffsets[index++] = problem.getSourceStart();
	    			}    				
	    		}
	    	}
	    	
	    	final EndingOffsetFinder lfinder = new EndingOffsetFinder(startingOffsets);
	    	_astCompilationUnit.accept( lfinder );
	    	
	    	for( Map.Entry<IFile, List<IProblem>> entry : _allProblems.entrySet() ){  
	    		if( _file.equals(entry.getKey()) ){
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
	 * @return - the extra type dependencies for the file under compilation
	 */
	public Set<String> getTypeDependencies()  { return _typeDependencies; }
    
	
	/** true value indicates that the source path for the project changed during this APT dispatch */
	public boolean getSourcePathChanged() { return _sourcePathChanged; }

	/** true value indicates that the source path for the project changed during this APT dispatch */
	public void setSourcePathChanged( boolean b ) { _sourcePathChanged = b; }
	
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
	// End of implementation for EclipseAnnotationProcessorEnvironment
}