/*******************************************************************************
 * Copyright (c) 2000, 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ltk.internal.ui.refactoring;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.SafeRunner;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;

import org.eclipse.core.resources.ResourcesPlugin;

import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CreateChangeOperation;
import org.eclipse.ltk.core.refactoring.PerformChangeOperation;

import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Display;

import org.eclipse.jface.wizard.IWizardContainer;

public class UIPerformChangeOperation extends PerformChangeOperation {

	private Display fDisplay;
	private IWizardContainer fWizardContainer;
	
	public UIPerformChangeOperation(Display display, Change change, IWizardContainer container) {
		super(change);
		fDisplay= display;
		fWizardContainer= container;
	}

	public UIPerformChangeOperation(Display display, CreateChangeOperation op, IWizardContainer container) {
		super(op);
		fDisplay= display;
		fWizardContainer= container;
	}
	
	protected void executeChange(final IProgressMonitor pm) throws CoreException {
		if (fDisplay != null && !fDisplay.isDisposed()) {
			final Throwable[] exception= new Throwable[1];
			final ISchedulingRule rule= ResourcesPlugin.getWorkspace().getRoot();
			final Thread callerThread= Thread.currentThread();
			final ISafeRunnable safeRunnable= new ISafeRunnable() {
				public void run() {
					try {
						Button[] cancel= new Button[1];
						fDisplay.syncExec(new Runnable)
						cancel= getCancelButton();
						boolean enabled= true;
						if (cancel != null && !cancel.isDisposed()) {
							enabled= cancel.isEnabled();
							cancel.setEnabled(false);
						}
						try {
							UIPerformChangeOperation.super.executeChange(pm);
						} finally {
							if (cancel != null && !cancel.isDisposed()) {
								cancel.setEnabled(enabled);
							}
						}
					} catch(CoreException e) {
						exception[0]= e;
					} finally {
						Job.getJobManager().transferRule(rule, callerThread);
					}
				}
				public void handleException(Throwable e) {
					exception[0]= e;
				}
			};
			Runnable r= new Runnable() {
				public void run() {
					SafeRunner.run(safeRunnable);
				}
			};
			r.run();
			if (exception[0] != null) {
				if (exception[0] instanceof CoreException) {
					IStatus status= ((CoreException)exception[0]).getStatus();
					// it is more important to get the original cause of the
					// exception. Therefore create a new status and take
					// over the exception trace from the UI thread.
					throw new CoreException(new MultiStatus(
							RefactoringUIPlugin.getPluginId(), IStatus.ERROR,
							new IStatus[] {status}, status.getMessage(), exception[0]));
				} else {
					String message= exception[0].getMessage();
					throw new CoreException(new Status(
						IStatus.ERROR, RefactoringUIPlugin.getPluginId(),IStatus.ERROR,
						message == null
							? RefactoringUIMessages.ChangeExceptionHandler_no_details
							: message,
						exception[0]));
				}
			}
		} else {
			super.executeChange(pm);
		}
	}

	private Button getCancelButton() {
		if (fWizardContainer instanceof RefactoringWizardDialog2) {
			return ((RefactoringWizardDialog2)fWizardContainer).getCancelButton();
		} else if (fWizardContainer instanceof RefactoringWizardDialog) {
			return ((RefactoringWizardDialog)fWizardContainer).getCancelButton();
		}
		return null;
	}
}
