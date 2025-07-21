/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others.
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
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.apt.core.env.Phase;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.env.MessagerImpl.Severity;
import org.eclipse.jdt.apt.core.internal.util.TestCodeUtil;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.compiler.ReconcileContext;
import org.eclipse.jdt.core.dom.ASTRequestor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;

public class ReconcileEnv extends AbstractCompilationEnv{

	/** The compilation unit being reconciled */
	private final ICompilationUnit _workingCopy;

	private final ReconcileContext _context;

	/**
	 * Create a reconcile environment from the given context.
	 * @return the reconcile environment or null if creation failed.
	 */
	static ReconcileEnv newEnv(ReconcileContext context)
    {
		final ICompilationUnit workingCopy = context.getWorkingCopy();
		IJavaProject javaProject = workingCopy.getJavaProject();
		final IFile file = (IFile)workingCopy.getResource();
       	return new ReconcileEnv(context, workingCopy, file, javaProject, TestCodeUtil.isTestCode(workingCopy));
    }

	private ReconcileEnv(
			ReconcileContext context,
			ICompilationUnit workingCopy,
		    IFile file,
		    IJavaProject javaProj, boolean isTestCode)
	{
		// See bug 133744: calling ReconcileContext.getAST3() here would result in
		// a typesystem whose types are not comparable with the types we get after
		// openPipeline().  Instead, we start the env with an EMPTY_AST_UNIT, and
		// replace it with the real thing inside the openPipeline() ASTRequestor's
		// acceptAST() callback.
		super(EMPTY_AST_UNIT, file, javaProj, Phase.RECONCILE, isTestCode);
		_context = context;
		_workingCopy = workingCopy;
		if (AptPlugin.DEBUG_COMPILATION_ENV) AptPlugin.trace(
				"constructed " + this + " for " + _workingCopy.getElementName()); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
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

	@Override
	public CompilationUnit getASTFrom(final IFile file){
		if( _file.equals(file) )
			return _astRoot;
		else
			return null;
	}

	@Override
	public void addTypeDependency(String fullyQualifiedTypeName) {
		// do not store type dependency during reconcile.
		return;
	}

	@Override
	public Filer getFiler(){
    	return new ReconcileFilerImpl(this);
    }

	void openPipeline() {
		_requestor = new CallbackRequestor();
		createASTs(_javaProject, new ICompilationUnit[]{_workingCopy}, _requestor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.core.internal.env.AbstractCompilationEnv#close()
	 */
	@Override
	public void close() {
		// Notify the compiler that the working copy was modified, so that the editor
		// and any downstream compilationParticipants will get a recomputed AST,
		// taking into account any changes to generated types.
		//TODO: don't call unless generated types were changed - WSH 10/06
		_context.resetAST();
		super.close();
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

	/* package scope */
	ICompilationUnit getCompilationUnit() {
		return _workingCopy;
	}

}
