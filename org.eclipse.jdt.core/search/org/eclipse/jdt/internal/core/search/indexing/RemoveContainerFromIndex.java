/*******************************************************************************
 * Copyright (c) 2020, Andrey Loskutov <loskutov@gmx.de> and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov <loskutov@gmx.de> - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core.search.indexing;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;

public class RemoveContainerFromIndex extends IndexRequest {

	public RemoveContainerFromIndex(IPath containerPath, IndexManager manager) {
		super(containerPath, manager);
	}

	@Override
	public boolean execute(IProgressMonitor progressMonitor) {
		if (this.isCancelled || progressMonitor != null && progressMonitor.isCanceled()) {
			return true;
		}
		this.manager.removeIndex(this.containerPath);
		return true;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof RemoveContainerFromIndex) {
			return this.containerPath.equals(((RemoveContainerFromIndex) o).containerPath);
		}
		return false;
	}

	@Override
	public int hashCode() {
		return this.containerPath.hashCode();
	}

	@Override
	public boolean canDiscardWaitingJobs() {
		return true;
	}

	@Override
	public String toString() {
		return "removing " + this.containerPath + " from index "; //$NON-NLS-1$ //$NON-NLS-2$
	}
}
