/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.concurrent.TimeUnit;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.IJobManager;

/** Waits for a job family without retaining delays introduced after wakeUp(). */
final class JobFamilyWait {
	private static final long WAKE_UP_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);

	private JobFamilyWait() {
		// Not instantiable.
	}

	static void join(IJobManager manager, Object family) throws InterruptedException {
		while (true) {
			manager.wakeUp(family);
			RetryMonitor monitor = new RetryMonitor();
			try {
				manager.join(family, monitor);
				return;
			} catch (OperationCanceledException e) {
				if (!monitor.retryRequested) {
					throw e;
				}
				// Only this wait was cancelled, not the jobs. Wake any jobs scheduled
				// after the previous wakeUp, then keep waiting for the whole family.
			}
		}
	}

	private static final class RetryMonitor extends NullProgressMonitor {
		private final long start = System.nanoTime();
		private boolean retryRequested;

		@Override
		public boolean isCanceled() {
			this.retryRequested |= System.nanoTime() - this.start >= WAKE_UP_INTERVAL_NANOS;
			return this.retryRequested;
		}
	}
}
