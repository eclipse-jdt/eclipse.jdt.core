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

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.env.EclipseAnnotationProcessorEnvironment;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

import com.sun.mirror.apt.Filer;

public class ReconcileEnv extends AbstractCompilationEnv implements EclipseAnnotationProcessorEnvironment{
	
	private final ICompilationUnit _workingCopy;
	
	/**
	 * Create a reconcile environment from the given context. 
	 * @param reconcileContext
	 * @return the reconcile environment or null if creation failed.
	 */
	static ReconcileEnv newEnv(ReconcileContext reconcileContext)
    {	
		final ICompilationUnit workingCopy = reconcileContext.getWorkingCopy();
		IJavaProject javaProject = workingCopy.getJavaProject();
		final IFile file = (IFile)workingCopy.getResource();
       	return new ReconcileEnv(workingCopy, file, javaProject);
    }
	
	private ReconcileEnv(
			ICompilationUnit workingCopy,
		    IFile file,
		    IJavaProject javaProj)
	{
		// See bug 133744: calling ReconcileContext.getAST3() here would result in 
		// a typesystem whose types are not comparable with the types we get after 
		// openPipeline().  Instead, we start the env with an EMPTY_AST_UNIT, and 
		// replace it with the real thing inside the openPipeline() ASTRequestor's 
		// acceptAST() callback.
		super(EMPTY_AST_UNIT, file, javaProj, Phase.RECONCILE);
		_workingCopy = workingCopy;
	}
	
	void addMessage(
			IFile resource, 
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
		
		assert resource != null : "don't know about the current resource"; //$NON-NLS-1$
		
		// not going to post any markers to resource outside of the one we are currently 
		// processing during reconcile phase.
		if( resource != null && !resource.equals( getFile() ) )
			return;
		
		_problems.add(createProblem(resource, start, end, severity, msg, line, arguments));
	}
	
	public CompilationUnit getASTFrom(final IFile file){
		if( _file.equals(file) )
			return _astRoot;
		else 
			return null;
	}
	
	public void addTypeDependency(String fullyQualifiedTypeName) {
		// do not store type dependency during reconcile.
		return;
	}
	
	public Filer getFiler(){ 
    	return new NoOpFiler();
    }
	
	private static final class NoOpFiler implements Filer{
		
		private static final OutputStream NO_OP_STREAM = new OutputStream(){
			public void write(int b) throws IOException {
				return;
			}
		};
		
		private static final PrintWriter NO_OP_WRITER = new PrintWriter(new NoOpWriter());
		
		public OutputStream createBinaryFile(Filer.Location loc, String pkg, File relPath)
			throws IOException {
			return NO_OP_STREAM;
		}
		public OutputStream createClassFile(String name) throws IOException {
			return NO_OP_STREAM;
		}
		public PrintWriter createSourceFile(String typeName) throws IOException  {
			return NO_OP_WRITER;
		}
		public PrintWriter createTextFile(Filer.Location loc, String pkg, File relPath, String charsetName) 
			throws IOException {
			return NO_OP_WRITER;
		}
	}
	
	private static final class NoOpWriter extends Writer{
		public void write(char[] cbuf, int off, int len) 
			throws IOException {
			return;
		}
		public void flush() throws IOException {
			return;
		}		
		public void close() throws IOException {
			return;
		}
	}

	void openPipeline() {
		_requestor = new CallbackRequestor();
		createASTs(_javaProject, new ICompilationUnit[]{_workingCopy}, _requestor);
	}
	
	class CallbackRequestor extends ASTRequestor {
		@Override
		public void acceptAST(ICompilationUnit source, CompilationUnit ast) {
			// Use the AST from the pipeline's parser, not the one from ReconcileContext.getAST3().
			_astRoot = ast;
		}

		@Override
		public void acceptBinding(String bindingKey, IBinding binding) {
			// This is called when the only binding has been passed, hence it is time
			// to dispatch
			_callback.run(ReconcileEnv.this);

		}
	}
}
