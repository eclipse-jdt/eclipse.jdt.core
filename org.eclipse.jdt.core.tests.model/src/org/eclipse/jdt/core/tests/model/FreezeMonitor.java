/*******************************************************************************
 * Copyright (c) 2016 Google, Inc and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Stefan Xenos (Google) - Initial implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;

public class FreezeMonitor {

	private static /* @Nullable */ Job monitorJob;

	public static void expectCompletionIn(final long millis) {
		done();
		monitorJob = Job.create("", new ICoreRunnable() {
			@Override
			public void run(IProgressMonitor monitor) throws CoreException {
				if (monitor.isCanceled()) {
					throw new OperationCanceledException();
				}
				StringBuilder result = new StringBuilder();
				result.append("Possible frozen test case\n");
				ThreadMXBean threadStuff = ManagementFactory.getThreadMXBean();
				ThreadInfo[] allThreads = threadStuff.getThreadInfo(threadStuff.getAllThreadIds(), 200);
				for (ThreadInfo threadInfo : allThreads) {
					result.append("\"");
					result.append(threadInfo.getThreadName());
					result.append("\": ");
					result.append(threadInfo.getThreadState());
					result.append("\n");
					final StackTraceElement[] elements = threadInfo.getStackTrace();
					for (StackTraceElement element : elements) {
						result.append("    ");
						result.append(element);
						result.append("\n");
					}
					result.append("\n");
				}
				System.out.println(result.toString());
			}
		});
		monitorJob.schedule(millis);
	}

	public static void done() {
		if (monitorJob != null) {
			monitorJob.cancel();
			monitorJob = null;
		}
	}
}
