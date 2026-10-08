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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import junit.framework.AssertionFailedError;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.junit.runners.model.MultipleFailureException;

public class JobFamilyWaitTests extends TestCase {
	private static final long TIMEOUT_SECONDS = 10;
	private static final long DELAY_MILLIS = TimeUnit.MINUTES.toMillis(5);
	private final List<Job> jobs = new ArrayList<>();
	private ExecutorService executor;

	public JobFamilyWaitTests(String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(JobFamilyWaitTests.class);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		this.executor = Executors.newSingleThreadExecutor(runnable -> {
			Thread thread = new Thread(runnable, "job-family-wait-test");
			thread.setDaemon(true);
			return thread;
		});
	}

	@Override
	protected void tearDown() throws Exception {
		List<Throwable> failures = new ArrayList<>();
		for (Job job : this.jobs) {
			collectCleanupFailure(failures, () -> job.cancel());
		}
		collectCleanupFailure(failures, () -> this.executor.shutdownNow());
		collectCleanupFailure(failures, this::awaitExecutorTermination);
		for (Job job : this.jobs) {
			collectCleanupFailure(failures, () -> awaitJobTermination(job));
		}
		collectCleanupFailure(failures, () -> super.tearDown());
		for (Throwable failure : failures) {
			if (failure instanceof InterruptedException) {
				Thread.currentThread().interrupt();
				break;
			}
		}
		MultipleFailureException.assertEmpty(failures);
	}

	private static void collectCleanupFailure(List<Throwable> failures, CleanupStep step) {
		try {
			step.run();
		} catch (Throwable failure) {
			// Report after attempting the remaining cleanup, including assertion errors.
			failures.add(failure);
		}
	}

	@FunctionalInterface
	private interface CleanupStep {
		void run() throws Exception;
	}

	public void testEmptyFamily() throws Exception {
		await(waitFor(Job.getJobManager(), new Object()));
	}

	public void testSleepingJobRunsWithoutBeingCancelled() throws Exception {
		Object family = new Object();
		AtomicBoolean ran = new AtomicBoolean();
		AtomicBoolean cancelled = new AtomicBoolean();
		Job job = job(family, monitor -> {
			ran.set(true);
			cancelled.set(monitor.isCanceled());
		});
		job.schedule(DELAY_MILLIS);
		await(waitFor(Job.getJobManager(), family));
		assertTrue("The sleeping job must execute", ran.get());
		assertFalse("Do not cancel the job to make the wait finish", cancelled.get());
	}

	public void testJobScheduledAfterWakeUpDoesNotWaitForItsDelay() throws Exception {
		Object family = new Object();
		AtomicBoolean ran = new AtomicBoolean();
		Job job = job(family, monitor -> ran.set(true));
		AtomicBoolean scheduled = new AtomicBoolean();
		IJobManager manager = beforeJoin(() -> {
			if (scheduled.compareAndSet(false, true)) {
				// This is the exact gap between the initial wakeUp and family join.
				// The scheduler and the wait below are real, not simulated jobs.
				job.schedule(DELAY_MILLIS);
				assertEquals(Job.SLEEPING, job.getState());
			}
		});
		await(waitFor(manager, family));
		assertTrue("The late sleeping job must execute before returning", ran.get());
	}

	public void testRunningJobIsStillJoined() throws Exception {
		Object family = new Object();
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch joining = new CountDownLatch(1);
		Job job = job(family, monitor -> {
			running.countDown();
			assertTrue("Test did not release the running job",
					release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		});
		try {
			job.schedule();
			assertTrue(running.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			Future<?> wait = waitFor(beforeJoin(joining::countDown), family);
			assertTrue(joining.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			assertFalse("A running job must not be treated as finished", wait.isDone());
			release.countDown();
			await(wait);
			assertEquals(Status.OK_STATUS, job.getResult());
		} finally {
			release.countDown();
		}
	}

	public void testUnrelatedFamilyIsNotWoken() throws Exception {
		Object family = new Object();
		AtomicBoolean unrelatedRan = new AtomicBoolean();
		Job unrelated = job(new Object(), monitor -> unrelatedRan.set(true));
		unrelated.schedule(DELAY_MILLIS);
		Job selected = job(family, monitor -> { });
		selected.schedule(DELAY_MILLIS);
		await(waitFor(Job.getJobManager(), family));
		assertFalse(unrelatedRan.get());
		assertEquals(Job.SLEEPING, unrelated.getState());
	}

	public void testInterruptedJoinIsPropagated() throws Exception {
		InterruptedException expected = new InterruptedException("test interruption");
		try {
			JobFamilyWait.join(beforeJoin(() -> {
				throw expected;
			}), new Object());
			fail("Expected interruption");
		} catch (InterruptedException actual) {
			assertSame(expected, actual);
		}
	}

	public void testCancellationNotCausedByRetryIsPropagated() throws Exception {
		OperationCanceledException expected = new OperationCanceledException("test cancellation");
		try {
			JobFamilyWait.join(beforeJoin(() -> {
				throw expected;
			}), new Object());
			fail("Expected cancellation");
		} catch (OperationCanceledException actual) {
			assertSame(expected, actual);
		}
	}

	public void testRunningJobRemainsJoinedAcrossWakeUpRetry() throws Exception {
		Object family = new Object();
		CountDownLatch running = new CountDownLatch(1);
		CountDownLatch release = new CountDownLatch(1);
		CountDownLatch retry = new CountDownLatch(1);
		AtomicInteger joins = new AtomicInteger();
		Job job = job(family, monitor -> {
			running.countDown();
			assertTrue("Test did not release the running job",
					release.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
		});
		try {
			job.schedule();
			assertTrue(running.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			Future<?> wait = waitFor(beforeJoin(() -> {
				if (joins.incrementAndGet() == 2) {
					retry.countDown();
				}
			}), family);
			assertTrue("Expected another wait after the wake-up interval",
					retry.await(TIMEOUT_SECONDS, TimeUnit.SECONDS));
			assertFalse("A retry must not be treated as successful completion", wait.isDone());
			release.countDown();
			await(wait);
			assertEquals(Status.OK_STATUS, job.getResult());
		} finally {
			release.countDown();
		}
	}


	public void testCleanupContinuesAfterExecutorFailure() throws Exception {
		assertCleanupContinues(true, false);
	}

	public void testCleanupContinuesAfterJobFailure() throws Exception {
		assertCleanupContinues(false, true);
	}

	public void testCleanupRetainsMultipleFailures() throws Exception {
		assertCleanupContinues(true, true);
	}

	private void assertCleanupContinues(boolean executorFails, boolean jobFails) throws Exception {
		AssertionFailedError executorFailure = new AssertionFailedError("executor cleanup failure");
		AssertionFailedError jobFailure = new AssertionFailedError("job cleanup failure");
		List<Job> joined = new ArrayList<>();
		JobFamilyWaitTests fixture = new JobFamilyWaitTests("cleanup fixture") {
			@Override
			void awaitExecutorTermination() throws InterruptedException {
				super.awaitExecutorTermination();
				if (executorFails) {
					throw executorFailure;
				}
			}

			@Override
			void awaitJobTermination(Job job) throws InterruptedException {
				joined.add(job);
				super.awaitJobTermination(job);
				if (jobFails && joined.size() == 1) {
					throw jobFailure;
				}
			}
		};
		fixture.setUp();
		fixture.job(new Object(), monitor -> { });
		fixture.job(new Object(), monitor -> { });
		Throwable observed = null;
		try {
			fixture.tearDown();
		} catch (Throwable failure) {
			observed = failure;
		}
		assertEquals("Every owned job must be joined even after a cleanup failure", fixture.jobs, joined);
		if (executorFails && jobFails) {
			assertTrue("Both cleanup failures must be retained", observed instanceof MultipleFailureException);
			MultipleFailureException multiple = (MultipleFailureException) observed;
			assertEquals(List.of(executorFailure, jobFailure), multiple.getFailures());
		} else {
			assertSame("Preserve the original cleanup failure", executorFails ? executorFailure : jobFailure, observed);
		}
	}

	void awaitExecutorTermination() throws InterruptedException {
		assertTrue("Waiter thread did not terminate",
				this.executor.awaitTermination(TIMEOUT_SECONDS, TimeUnit.SECONDS));
	}

	void awaitJobTermination(Job job) throws InterruptedException {
		assertTrue("Test job did not terminate: " + job,
				job.join(TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS), null));
	}

	private Future<?> waitFor(IJobManager manager, Object family) {
		return this.executor.submit(() -> {
			JobFamilyWait.join(manager, family);
			return null;
		});
	}

	private void await(Future<?> wait) throws Exception {
		try {
			wait.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			fail("Family wait retained the delayed schedule instead of waking the job");
		}
	}

	private Job job(Object family, JobBody body) {
		Job job = new Job("JobFamilyWaitTests") {
			@Override
			public boolean belongsTo(Object candidate) {
				return candidate == family;
			}

			@Override
			protected IStatus run(IProgressMonitor monitor) {
				try {
					body.run(monitor);
					return Status.OK_STATUS;
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
					return Status.CANCEL_STATUS;
				}
			}
		};
		this.jobs.add(job);
		return job;
	}

	private IJobManager beforeJoin(BeforeJoin action) {
		IJobManager delegate = Job.getJobManager();
		return (IJobManager) Proxy.newProxyInstance(IJobManager.class.getClassLoader(),
				new Class<?>[] { IJobManager.class }, (proxy, method, arguments) -> {
					if ("join".equals(method.getName())) {
						action.run();
					}
					try {
						return method.invoke(delegate, arguments);
					} catch (InvocationTargetException e) {
						throw e.getCause();
					}
				});
	}

	@FunctionalInterface
	private interface BeforeJoin {
		void run() throws InterruptedException;
	}

	@FunctionalInterface
	private interface JobBody {
		void run(IProgressMonitor monitor) throws InterruptedException;
	}
}
