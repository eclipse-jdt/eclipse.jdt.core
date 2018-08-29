/*******************************************************************************
 * Copyright (c) 2006, 2016 Wind River Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Markus Schorn - initial API and implementation
 *     Andrew Ferguson (Symbian)
 *     Sergey Prigogin (Google)
 *******************************************************************************/
package org.eclipse.jdt.core.tests.nd.util;

import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestFailure;
import junit.framework.TestResult;
import junit.framework.TestSuite;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.ILogListener;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BaseTestCase extends TestCase {
	private static final String DEFAULT_INDEXER_TIMEOUT_SEC = "10";
	private static final String INDEXER_TIMEOUT_PROPERTY = "indexer.timeout";
	/**
	 * Indexer timeout used by tests. To avoid this timeout expiring during debugging add
	 * -Dindexer.timeout=some_large_number to VM arguments of the test launch configuration. 
	 */
	protected static final int INDEXER_TIMEOUT_SEC =
			Integer.parseInt(System.getProperty(INDEXER_TIMEOUT_PROPERTY, DEFAULT_INDEXER_TIMEOUT_SEC));
	protected static final int INDEXER_TIMEOUT_MILLISEC= INDEXER_TIMEOUT_SEC * 1000;
	
	private boolean fExpectFailure;
	private int fBugNumber;
	private int fExpectedLoggedNonOK;
	private Deque<File> filesToDeleteOnTearDown= new ArrayDeque<>();

	public BaseTestCase() {
		super();
	}

	public BaseTestCase(String name) {
		super(name);
	}

	public static NullProgressMonitor npm() {
		return new NullProgressMonitor();
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		//CModelListener.sSuppressUpdateOfLastRecentlyUsed= true;
	}

	@Override
	protected void tearDown() throws Exception {
		for (File file; (file = this.filesToDeleteOnTearDown.pollLast()) != null;) {
			file.delete();
		}
		ResourceHelper.cleanUp();
		//TestScannerProvider.clear();
		super.tearDown();
	}

	protected void deleteOnTearDown(File file) {
		this.filesToDeleteOnTearDown.add(file);
	}

	protected File createTempFile(String prefix, String suffix) throws IOException {
		File file = File.createTempFile(prefix, suffix);
		this.filesToDeleteOnTearDown.add(file);
		return file;
	}

	protected File nonExistentTempFile(String prefix, String suffix) {
		File file= new File(System.getProperty("java.io.tmpdir"),
				prefix + System.currentTimeMillis() + suffix);
		this.filesToDeleteOnTearDown.add(file);
		return file;
	}

	public static TestSuite suite(Class<? extends BaseTestCase> clazz) {
		return suite(clazz, null);
	}

	protected static TestSuite suite(Class<? extends BaseTestCase> clazz, String failingTestPrefix) {
		TestSuite suite= new TestSuite(clazz);
		Test failing= getFailingTests(clazz, failingTestPrefix);
		if (failing != null) {
			suite.addTest(failing);
		}
		return suite;
	}

	private static Test getFailingTests(Class<? extends BaseTestCase> clazz, String prefix) {
		TestSuite suite= new TestSuite("Failing Tests");
		HashSet<String> names= new HashSet<>();
		Class<?> superClass= clazz;
		while (Test.class.isAssignableFrom(superClass) && !TestCase.class.equals(superClass)) {
			Method[] methods= superClass.getDeclaredMethods();
			for (Method method : methods) {
				addFailingMethod(suite, method, names, clazz, prefix);
			}
			superClass= superClass.getSuperclass();
		}
		if (suite.countTestCases() == 0) {
			return null;
		}
		return suite;
	}

	private static void addFailingMethod(TestSuite suite, Method m, Set<String> names,
			Class<? extends BaseTestCase> clazz, String prefix) {
		String name = m.getName();
		if (!names.add(name)) {
			return;
		}
		if (name.startsWith("test") || (prefix != null && !name.startsWith(prefix))) {
			return;
		}
		if (name.equals("tearDown") || name.equals("setUp") || name.equals("runBare")) {
			return;
		}
		if (Modifier.isPublic(m.getModifiers())) {
			Class<?>[] parameters = m.getParameterTypes();
			Class<?> returnType = m.getReturnType();
			if (parameters.length == 0 && returnType.equals(Void.TYPE)) {
				Test test = TestSuite.createTest(clazz, name);
				((BaseTestCase) test).setExpectFailure(0);
				suite.addTest(test);
			}
		}
	}

	@Override
	public void runBare() throws Throwable {
		final List<IStatus> statusLog= Collections.synchronizedList(new ArrayList<>());
		ILogListener logListener= new ILogListener() {
			@Override
			public void logging(IStatus status, String plugin) {
				if (!status.isOK() && status.getSeverity() != IStatus.INFO) {
					switch (status.getCode()) {
					case IResourceStatus.NOT_FOUND_LOCAL:
					case IResourceStatus.NO_LOCATION_LOCAL:
					case IResourceStatus.FAILED_READ_LOCAL:
					case IResourceStatus.RESOURCE_NOT_LOCAL:
						// Logged by the resources plugin.
						return;
					}
					statusLog.add(status);
				}
			}
		};
		final ILog log = Package.getLog();
		if (log != null) { // Iff we don't run as a JUnit Plugin Test.
			log.addLogListener(logListener);
		}

		Throwable testThrowable= null;
		try {
			try {
				super.runBare();
			} catch (Throwable e) {
				testThrowable= e;
			}

			if (statusLog.size() != this.fExpectedLoggedNonOK) {
				StringBuilder msg= new StringBuilder("Expected number (" + this.fExpectedLoggedNonOK + ") of ");
				msg.append("Non-OK status objects in log differs from actual (" + statusLog.size() + ").\n");
				Throwable cause= null;
				if (!statusLog.isEmpty()) {
					synchronized (statusLog) {
						for (IStatus status : statusLog) {
							IStatus[] ss= {status};
							ss= status instanceof MultiStatus ? ((MultiStatus) status).getChildren() : ss;
							for (IStatus s : ss) {
								msg.append("\t" + s.getMessage() + " ");

								Throwable t= s.getException();
								cause= cause != null ? cause : t;
								if (t != null) {
									msg.append(t.getMessage() != null ? t.getMessage() : t.getClass().getCanonicalName());
								}

								msg.append("\n");
							}
						}
					}
				}
				cause= cause != null ? cause : testThrowable;
				AssertionFailedError afe= new AssertionFailedError(msg.toString());
				afe.initCause(cause);
				throw afe;
			}
		} finally {
			if (log != null) {
				log.removeLogListener(logListener);
			}
		}

		if (testThrowable != null)
			throw testThrowable;
	}

    @Override
	public void run(TestResult result) {
    	if (!this.fExpectFailure || Boolean.parseBoolean(System.getProperty("SHOW_EXPECTED_FAILURES"))) {
    		super.run(result);
    		return;
    	}

        result.startTest(this);

        TestResult r = new TestResult();
        super.run(r);
        if (r.failureCount() == 1) {
        	TestFailure failure= r.failures().nextElement();
        	String msg= failure.exceptionMessage();
        	if (msg != null && msg.startsWith("Method \"" + getName() + "\"")) {
        		result.addFailure(this, new AssertionFailedError(msg));
        	}
        } else if (r.errorCount() == 0 && r.failureCount() == 0) {
            String err = "Unexpected success of " + getName();
            if (this.fBugNumber > 0) {
                err += ", bug #" + this.fBugNumber;
            }
            result.addFailure(this, new AssertionFailedError(err));
        }

        result.endTest(this);
    }

    public void setExpectFailure(int bugNumber) {
    	this.fExpectFailure= true;
    	this.fBugNumber= bugNumber;
    }

    /**
     * The last value passed to this method in the body of a testXXX method
     * will be used to determine whether or not the presence of non-OK status objects
     * in the log should fail the test. If the logged number of non-OK status objects
     * differs from the last value passed, the test is failed. If this method is not called
     * at all, the expected number defaults to zero.
     * @param count the expected number of logged error and warning messages
     */
    public void setExpectedNumberOfLoggedNonOKStatusObjects(int count) {
    	this.fExpectedLoggedNonOK= count;
    }
}
