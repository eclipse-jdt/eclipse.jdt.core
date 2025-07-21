/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others
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

import com.sun.mirror.apt.Filer;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.PackageDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.declaration.EclipseMirrorObject;
import org.eclipse.jdt.apt.core.internal.declaration.TypeDeclarationImpl;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.Factory;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotatedNodeVisitor;
import org.eclipse.jdt.apt.core.internal.util.Visitors.AnnotationVisitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.BuildContext;
import org.eclipse.jdt.core.compiler.CategorizedProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;

public class BuildEnv extends AbstractCompilationEnv
{
	private boolean _hasRaisedErrors = false;

	private final BuildFilerImpl _filer;

	/**
	 * Set of strings that indicate new type dependencies introduced on the file
	 * each string is a fully-qualified type name.
	 */
	private final Set<String> _typeDependencies = new HashSet<>();

	/**
	 * Indicates whether we are in batch mode or not. This gets flipped only
	 * during build and could be flipped back and forth.
	 */
	private boolean _batchMode = false; // off by default.

	/**
	 * Holds all the files that contains annotation that are to be processed during build.
	 * If we are not in batch mode, <code>super._file</code> holds the file
	 * being processed at the time.
	 */
	private BuildContext[] _filesWithAnnotation = null;

	/**
	 * These are files that are part of a build but does not have annotations on it.
	 * During batch mode processing, these files still also need to be included.
	 */
	private BuildContext[] _additionFiles = null;
	/**
	 * This is intialized when <code>_batchMode</code> is set to be <code>true</code> or
	 * when batch processing is expected. <p>
	 * It is also set in build mode for perf reason rather than parsing and resolving
	 * each file individually.
	 * @see #getAllAnnotationTypes(Map)
	 */
	private CompilationUnit[] _astRoots = null;
	private List<MarkerInfo> _markerInfos = null;

    /**
     * Constructor for creating a processor environment used during build.
     */
    BuildEnv(
			final BuildContext[] filesWithAnnotations,
			final BuildContext[] additionalFiles,
			final IJavaProject javaProj, boolean isTestCode) {

    	super(null, null, javaProj, Phase.BUILD, isTestCode);
		_filer = new BuildFilerImpl(this);
		_filesWithAnnotation = filesWithAnnotations;
		_additionFiles = additionalFiles;
		_problems = new ArrayList<>();
		_markerInfos = new ArrayList<>();

		if (AptPlugin.DEBUG_COMPILATION_ENV) AptPlugin.trace(
				"constructed " + this + " for " + _filesWithAnnotation.length + " files"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	}

    @Override
	public Filer getFiler()
    {
		checkValid();
        return _filer;
    }
    @Override
	public PackageDeclaration getPackage(String name)
    {
		checkValid();
		return super.getPackage(name);
    }

    @Override
	public TypeDeclaration getTypeDeclaration(String name)
    {
		checkValid();
		TypeDeclaration decl = super.getTypeDeclaration(name);

		if (!_batchMode)
			addTypeDependency(name);

		return decl;
    }

	/**
	 * @return true iff errors (MessagerImpl.Severity.Error) has been posted
	 *         Always return false when this environment is closed.
	 */
	public boolean hasRaisedErrors(){
		return _hasRaisedErrors;
	}

	public static InputStreamReader getFileReader( final IFile file ) throws IOException, CoreException {
		return new InputStreamReader(getInputStream(file), file.getCharset());
	}

	public static InputStream getInputStream( final IFile file ) throws IOException, CoreException {
		return new BufferedInputStream(file.getContents());
	}

	/**
	 * @return true iff class files has been generated.
	 *         Always return false when this environment is closed.
	 */
	public boolean hasGeneratedClassFiles(){ return _filer.hasGeneratedClassFile(); }

	/* (non-Javadoc)
	 *  Once the environment is closed the following is not allowed
	 *  1) posting messge
	 *  2) generating file
	 *  3) retrieving type or package by name
	 *  4) add or remove listeners
	 */
    @Override
	public void close(){
    	if( isClosed() )
    		return;
    	_markerInfos = null;
    	_astRoot = null;
    	_file = null;
    	_astRoots = null;
    	_filesWithAnnotation = null;
    	_problems = null;
        _modelCompUnit2astCompUnit.clear();
		_hasRaisedErrors = false;
		super.close();
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
    @Override
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

    	_hasRaisedErrors |= severity == MessagerImpl.Severity.ERROR;

    	// Eclipse doesn't support INFO-level IProblems, so we send them to the log instead.
    	if ( severity == Severity.INFO) {
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

    	APTProblem problem = createProblem(resource, start, end, severity, msg, line, arguments);
    	_problems.add(problem);
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

    @Override
	public Map<String, AnnotationTypeDeclaration> getAnnotationTypes()
    {
    	checkValid();
    	assert _astRoot != null && _file != null && !_batchMode :
    		"operation not available under batch mode."; //$NON-NLS-1$
    	return super.getAnnotationTypes();
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
    		final Map<BuildContext, Set<AnnotationTypeDeclaration>> file2Annotations) {

    	checkValid();
    	if( _filesWithAnnotation == null )
    		return getAnnotationTypes();

		final List<Annotation> instances = new ArrayList<>();
		final Map<String, AnnotationTypeDeclaration> decls =
			new HashMap<>();
		final AnnotationVisitor visitor = new AnnotationVisitor(instances);
		for( int astIndex=0, len=_astRoots.length; astIndex<len; astIndex++ ){
			if( _astRoots == null || _astRoots[astIndex] == null  )
				System.err.println();
			_astRoots[astIndex].accept(visitor);
			final Set<AnnotationTypeDeclaration> perFileAnnos = new HashSet<>();

			for (int instanceIndex=0, size = instances.size(); instanceIndex < size; instanceIndex++) {
				final Annotation instance = instances.get(instanceIndex);
				final ITypeBinding annoType = instance.resolveTypeBinding();
				if (annoType == null)
					continue;
				final TypeDeclarationImpl decl =
					Factory.createReferenceType(annoType, this);
				if (decl != null && decl.kind() == EclipseMirrorObject.MirrorKind.TYPE_ANNOTATION){
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
	 * @return - the extra type dependencies for the files under compilation
	 */
	public Set<String> getTypeDependencies()  { return _typeDependencies; }

	/**
	 * Switch to batch processing mode.
	 * Note: Call to this method will cause all files associated with this environment to be
	 * read and parsed.
	 */
	public void beginBatchProcessing(){
		if( _phase != Phase.BUILD )
			throw new IllegalStateException("No batch processing outside build."); //$NON-NLS-1$

		if( _batchMode ) return;
		checkValid();

		_batchMode = true;
		_file = null;
		_astRoot = null;
	}

	public void completedBatchProcessing(){
		postMarkers();
		completedProcessing();
	}

	void createASTs(BuildContext[] cpResults){
		final int len = cpResults.length;
		final ICompilationUnit[] units = new ICompilationUnit[len];
		for( int i=0; i<len; i++ ){
			// may return null if creation failed. this may occur if
			// the file does not exists.
			units[i] = JavaCore.createCompilationUnitFrom(cpResults[i].getFile());
		}
		createASTs(_javaProject, units, _requestor = new CallbackRequestor(units));
	}

	public void beginFileProcessing(BuildContext result){
		if( result == null )
			throw new IllegalStateException("missing compilation result"); //$NON-NLS-1$
		_batchMode = false;
		final IFile file = result.getFile();
		if( file.equals(_file) ) // this is a no-op
			return;

		_astRoot = null;
		_file = null;

		// need to match up the file with the ast.
		if( _filesWithAnnotation != null ){
			for( int i=0, len=_filesWithAnnotation.length; i<len; i++ ){
				if( file.equals(_filesWithAnnotation[i].getFile()) ){
					_file = file;
					_astRoot = _astRoots[i];
				}
			}
		}

		if( _file == null || _astRoot == null)
			throw new IllegalStateException(
					"file " +  //$NON-NLS-1$
					file.getName() +
					" is not in the list to be processed."); //$NON-NLS-1$
	}

	public void completedFileProcessing(){
		completedProcessing();
	}

	@Override
	protected void completedProcessing(){
		_problems.clear();
		_typeDependencies.clear();
		super.completedProcessing();
	}

	@Override
	public List<? extends CategorizedProblem> getProblems(){
		if( !_problems.isEmpty() )
			EnvUtil.updateProblemLength(_problems, getAstCompilationUnit());
		return _problems;
	}

	// Implementation for EclipseAnnotationProcessorEnvironment
	@Override
	public CompilationUnit getAST()
	{
		if( _batchMode )
			return null;
		return _astRoot;
	}

	@Override
	public void addTypeDependency(final String fullyQualifiedTypeName )
	{
		if(!_batchMode){
			_typeDependencies.add( fullyQualifiedTypeName );
		}
	}
	// End of implementation for EclipseAnnotationProcessorEnvironment

	/**
	 * Include all the types from all files, files with and without annotations on it
	 * if we are in batch mode. Otherwise, just the types from the file that's currently
	 * being processed.
	 */
	@Override
	protected List<AbstractTypeDeclaration> searchLocallyForTypeDeclarations()
    {
		if( !_batchMode )
			return super.searchLocallyForTypeDeclarations();
		final List<AbstractTypeDeclaration> typeDecls = new ArrayList<>();
		for( int i=0, len=_astRoots.length; i<len; i++ )
        	typeDecls.addAll( _astRoots[i].types() );

		getTypeDeclarationsFromAdditionFiles(typeDecls);

		return typeDecls;
    }

	private void getTypeDeclarationsFromAdditionFiles(List<AbstractTypeDeclaration> typeDecls){
		if( _additionFiles == null || _additionFiles.length == 0 ) return;

		final int len = _additionFiles.length;
		final ICompilationUnit[] units = new ICompilationUnit[len];
		for( int i=0; i<len; i++ ){
			// may return null if creation failed. this may occur if
			// the file does not exists.
			units[i] = JavaCore.createCompilationUnitFrom(_additionFiles[i].getFile());
		}
		BaseRequestor r = new BaseRequestor(units);
		createASTs(_javaProject, units, r);

		CompilationUnit[] asts = r.asts;
		for( CompilationUnit ast : asts ){
			if( ast != null ){
				typeDecls.addAll( ast.types() );
			}
		}
	}

	@Override
	protected Map<ASTNode, List<Annotation>> getASTNodesWithAnnotations()
    {
		if( !_batchMode )
			return super.getASTNodesWithAnnotations();
    	final Map<ASTNode, List<Annotation>> astNode2Anno = new HashMap<>();
        final AnnotatedNodeVisitor visitor = new AnnotatedNodeVisitor(astNode2Anno);
        for( int i=0, len=_astRoots.length; i<len; i++ )
        	_astRoots[i].accept( visitor );
        return astNode2Anno;
    }

	@Override
	protected IFile getFileForNode(final ASTNode node)
	{
		if( !_batchMode )
			return super.getFileForNode(node);
		final CompilationUnit curAST = (CompilationUnit)node.getRoot();
		for( int i=0, len=_astRoots.length; i<len; i++ ){
			if( _astRoots[i] == curAST )
				return _filesWithAnnotation[i].getFile();
		}
		throw new IllegalStateException();
	}

	/**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @return the compilation unit that defines the given binding or null if no
	 * match is found.
	 */
	@Override
	protected CompilationUnit searchLocallyForBinding(final IBinding binding)
	{
		if( !_batchMode )
			return super.searchLocallyForBinding(binding);

		for( int i=0, len=_astRoots.length; i<len; i++ ){
			ASTNode node = _astRoots[i].findDeclaringNode(binding);
			if( node != null)
				return _astRoots[i];
		}
		return null;
	}

	/**
	 * Go through the list of compilation unit in this environment and looking for
	 * the declaration node of the given binding.
	 * @return the compilation unit that defines the given binding or null if no
	 * match is found.
	 */
	@Override
	protected IFile searchLocallyForIFile(final IBinding binding)
	{
		if( !_batchMode )
			return super.searchLocallyForIFile(binding);

		for( int i=0, len=_astRoots.length; i<len; i++ ){
			ASTNode node = _astRoots[i].findDeclaringNode(binding);
			if( node != null)
				return _filesWithAnnotation[i].getFile();
		}
		return null;
	}

	/**
     * @return the compilation unit associated with the given file.
     * If the file is not one of those that this environment is currently processing,
     * return null;
     */
	@Override
	public CompilationUnit getASTFrom(final IFile file)
	{
		if( file == null )
    		return null;
    	else if( file.equals(_file) )
    		return _astRoot;
    	else if( _astRoots != null ){
    		for( int i=0, len=_filesWithAnnotation.length; i<len; i++ ){
        		if( file.equals(_filesWithAnnotation[i].getFile()) )
        			return _astRoots[i];
        	}
    	}
    	return null;
	}

	/**
	 * @return the current ast being processed if in per-file mode.
	 * If in batch mode, one of the asts being processed (no guarantee which
	 * one will be returned.
	 */
	@Override
	protected AST getCurrentDietAST(){

		if( _astRoot != null )
			return _astRoot.getAST();
		else{
			if( _astRoots == null )
				throw new IllegalStateException("no AST is available"); //$NON-NLS-1$
			return _astRoots[0].getAST();
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
	            @Override
				public void run(IProgressMonitor monitor)
	            {
	                for( MarkerInfo markerInfo : _markerInfos ){
						try{
		                    final IMarker marker = _javaProject.getProject().createMarker(AptPlugin.APT_BATCH_PROCESSOR_PROBLEM_MARKER);
		                    markerInfo.copyIntoMarker(marker);
						}
						catch(CoreException e){
							AptPlugin.log(e, "Failure posting markers"); //$NON-NLS-1$
						}
	                }
	            }
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

	public BuildContext[] getFilesWithAnnotation()
	{
		return _filesWithAnnotation;
	}

	public BuildContext[] getFilesWithoutAnnotation()
	{
		return _additionFiles;
	}

	private class CallbackRequestor extends BaseRequestor {
		CallbackRequestor(ICompilationUnit[] parseUnits) {
			super(parseUnits);
		}
		@Override
		public void acceptBinding(String bindingKey, IBinding binding) {
			// If we have recieved the last ast we have requested,
			// then assign the asts, then begin dispatch
			_astRoots = asts;
			_callback.run(BuildEnv.this);
		}
	}

}
