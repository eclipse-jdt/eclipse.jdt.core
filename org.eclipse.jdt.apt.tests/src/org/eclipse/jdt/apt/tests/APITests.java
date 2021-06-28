/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    jgarms@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.apt.tests;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptUtil;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotation;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldAnnotationProcessorFactory;
import org.eclipse.jdt.apt.tests.annotations.helloworld.HelloWorldWildcardAnnotationProcessorFactory;
import org.eclipse.jdt.apt.tests.annotations.messager.MessagerAnnotationProcessor;
import org.eclipse.jdt.apt.tests.annotations.messager.MessagerCodeExample;
import org.eclipse.jdt.core.IJavaProject;

import com.sun.mirror.apt.AnnotationProcessorFactory;

public class APITests extends APTTestBase {

	private class LogListener implements ILogListener {
		private final List<IStatus> _messages = new ArrayList<IStatus>();

		public void logging(IStatus status, String plugin) {
			_messages.add(status);
		}

		public void clear() {
			_messages.clear();
		}

		public List<IStatus> getList() {
			return _messages;
		}
	}

	private LogListener _logListener;

	public APITests(final String name) {
		super( name );
	}

	public static Test suite() {
		return new TestSuite( APITests.class );
	}

	@Override
	public void setUp() throws Exception {
		super.setUp();

		_logListener = new LogListener();
		AptPlugin.getPlugin().getLog().addLogListener(_logListener);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
		AptPlugin.getPlugin().getLog().removeLogListener(_logListener);
		_logListener = null;
	}

	public void testAptUtil() throws Exception {
		IJavaProject jproj = env.getJavaProject( getProjectName() );

		// Check getting a known annotation
		AnnotationProcessorFactory factory =
			AptUtil.getFactoryForAnnotation(HelloWorldAnnotation.class.getName(), jproj);
		assertEquals(factory.getClass(), HelloWorldAnnotationProcessorFactory.class);

		// Check getting an annotation with a partial wildcard ("org.eclipse.jdt.apt.tests.*")
		factory =
			AptUtil.getFactoryForAnnotation(HelloWorldAnnotation.class.getName() + "qwerty", jproj); //$NON-NLS-1$

		assertEquals(factory.getClass(), HelloWorldWildcardAnnotationProcessorFactory.class);

		// Check getting an annotation with a full wildcard ("*")
		// Note that these tests require that we do not cache what annotations
		// a factory claims to support. Specifically, the HelloWorldWildcard one
		// will swap out what it returns based on this static boolean.
		// If we change to cache the results, this test will need to be modified to work
		// in that scenario, probably by created a new external jar with
		// a processor that claims *.
		HelloWorldWildcardAnnotationProcessorFactory.CLAIM_ALL_ANNOTATIONS = true;
		try {
			factory =
				AptUtil.getFactoryForAnnotation("org.eclipse.Foo", jproj); //$NON-NLS-1$

			assertEquals(factory.getClass(), HelloWorldWildcardAnnotationProcessorFactory.class);
		}
		finally {
			HelloWorldWildcardAnnotationProcessorFactory.CLAIM_ALL_ANNOTATIONS = false;
		}

		// Make sure we've unset the wildcard behavior
		factory =
			AptUtil.getFactoryForAnnotation("org.eclipse.Foo", jproj); //$NON-NLS-1$

		assertNull(factory);

	}

	public void testMessagerAPI() throws Exception {
		IProject project = env.getProject( getProjectName() );
		IPath srcRoot = getSourcePath();
		IPath code = env.addClass(srcRoot, MessagerCodeExample.CODE_PACKAGE, MessagerCodeExample.CODE_CLASS_NAME, MessagerCodeExample.CODE1);
		ExpectedProblem prob1 = new ExpectedProblem("", MessagerAnnotationProcessor.PROBLEM_TEXT_WARNING, code, //$NON-NLS-1$
				MessagerCodeExample.WARNING_START,
				MessagerCodeExample.WARNING_END);
		ExpectedProblem prob2 = new ExpectedProblem("", MessagerAnnotationProcessor.PROBLEM_TEXT_ERROR, code, //$NON-NLS-1$
				MessagerCodeExample.ERROR_START,
				MessagerCodeExample.ERROR_END);
		ExpectedProblem[] problems = new ExpectedProblem[] { prob1, prob2 };

		// Code example with info, warning, and error messages
		_logListener.clear();
		fullBuild( project.getFullPath() );
		expectingOnlySpecificProblemsFor(code, problems);
		checkMessagerAnnotationLogEntry(
				MessagerAnnotationProcessor.PROBLEM_TEXT_INFO,
				MessagerCodeExample.INFO_START,
				MessagerCodeExample.INFO_END);

		// Code example with info and warning messages
		env.removeClass(code, MessagerCodeExample.CODE_CLASS_NAME);
		code = env.addClass(srcRoot, MessagerCodeExample.CODE_PACKAGE, MessagerCodeExample.CODE_CLASS_NAME, MessagerCodeExample.CODE2);
		_logListener.clear();
		fullBuild( project.getFullPath() );
		problems = new ExpectedProblem[] { prob1 };
		expectingOnlySpecificProblemsFor(code, problems);
		checkMessagerAnnotationLogEntry(
				MessagerAnnotationProcessor.PROBLEM_TEXT_INFO,
				MessagerCodeExample.INFO_START,
				MessagerCodeExample.INFO_END);

		// Code example with only a warning message
		env.removeClass(code, MessagerCodeExample.CODE_CLASS_NAME);
		code = env.addClass(srcRoot, MessagerCodeExample.CODE_PACKAGE, MessagerCodeExample.CODE_CLASS_NAME, MessagerCodeExample.CODE3);
		_logListener.clear();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		checkMessagerAnnotationLogEntry(
				MessagerAnnotationProcessor.PROBLEM_TEXT_INFO,
				MessagerCodeExample.INFO_START,
				MessagerCodeExample.INFO_END);

		// Code example with no problems
		env.removeClass(code, MessagerCodeExample.CODE_CLASS_NAME);
		code = env.addClass(srcRoot, MessagerCodeExample.CODE_PACKAGE, MessagerCodeExample.CODE_CLASS_NAME, MessagerCodeExample.CODE4);
		_logListener.clear();
		fullBuild( project.getFullPath() );
		expectingNoProblems();
		assertTrue(_logListener.getList().toString(), _logListener.getList().isEmpty());
	}

	/**
	 * Check that there are exactly [targetCount] messages in the log that contain
	 * [targetMsg] and also contain "starting offset=[start]; ending offset=[end]".
	 */
	private void checkMessagerAnnotationLogEntry(String targetMsg, int start, int end) {
		int count = 0;
		final String offsetMsg = "starting offset=" + start + "; ending offset=" + end;
		for (IStatus status : _logListener.getList()) {
			String logMessage = status.getMessage();
			if (logMessage.contains(targetMsg) && logMessage.contains(offsetMsg)) {
				++count;
			}
		}
		assertEquals(1, count);
	}


}
