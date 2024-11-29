/*******************************************************************************
* Copyright (c) 2024 Red Hat, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.jdt.internal.javac;

import java.lang.reflect.Field;

import org.eclipse.core.runtime.ILog;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.internal.compiler.ICompilerRequestor;
import org.eclipse.jdt.internal.core.builder.AbstractImageBuilder;
import org.eclipse.jdt.internal.core.builder.BuildNotifier;

/*******************************************************************************
 * Copyright (c) 2024 Red Hat, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
public class BuildNotifierCompilationProgress extends CompilationProgress {

	private BuildNotifier buildNotifier;

	public BuildNotifierCompilationProgress(ICompilerRequestor requestor) {
		this.buildNotifier = findBuildNotifier(requestor);
	}

	private BuildNotifier findBuildNotifier(ICompilerRequestor requestor) {
		if (requestor instanceof AbstractImageBuilder) {
			try {
				Field notifierField = AbstractImageBuilder.class.getDeclaredField("notifier");
				notifierField.setAccessible(true);
				return notifierField.get(requestor) instanceof BuildNotifier notifier ? notifier : null;
			} catch (Exception ex) {
				ILog.get().warn(ex.getMessage(), ex);
			}
		}
		return null;
	}

	@Override
	public void begin(int remainingWork) {
		if (this.buildNotifier != null) {
			this.buildNotifier.checkCancelWithinCompiler();
		}
	}

	@Override
	public void done() {
		// do not forward as done() is sent via requestor
	}

	@Override
	public boolean isCanceled() {
		if (this.buildNotifier != null) {
			this.buildNotifier.checkCancel();
		}
		return false;
	}

	@Override
	public void setTaskName(String name) {
		if (this.buildNotifier != null) {
			this.buildNotifier.subTask(name);
		}
	}

	@Override
	public void worked(int workIncrement, int remainingWork) {
		// TODO Auto-generated method stub
	}

}
