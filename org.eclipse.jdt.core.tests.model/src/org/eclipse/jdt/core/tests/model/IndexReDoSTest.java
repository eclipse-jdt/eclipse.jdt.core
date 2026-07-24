/*******************************************************************************
 * Copyright (c) 2026 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.eclipse.jdt.core.search.SearchPattern;
import org.eclipse.jdt.internal.core.index.Index;

/**
 * Tests for the ReDoS (Regular Expression Denial of Service) protection in
 * {@link Index#isMatch(char[], char[], int)} and related regex matching code.
 * <p>
 * The JDT search index uses {@link java.util.regex.Pattern} to compile and match
 * user-supplied regex patterns against index entries when {@link SearchPattern#R_REGEXP_MATCH}
 * is used. Since Java's regex engine uses backtracking (NFA-based), pathological
 * patterns like {@code (a+)+$} can cause catastrophic backtracking, consuming
 * 100% CPU and effectively creating a Denial of Service condition.
 * </p>
 * <p>
 * {@code Index.isMatch} guards against this by wrapping the input in a deadline-checking
 * {@code CharSequence} that aborts a runaway match after a bounded time budget
 * (configurable via the {@code jdt.core.index.regexpMatchTimeoutMillis} system property),
 * treating a timeout as a non-match. Invalid patterns are likewise treated as a non-match
 * instead of propagating a {@link java.util.regex.PatternSyntaxException}. These tests verify
 * that behaviour.
 * </p>
 *
 * @see Index#isMatch(char[], char[], int)
 */
public class IndexReDoSTest extends TestCase {

	private static final int TIMEOUT_SECONDS = 5;

	public static Test suite() {
		return new TestSuite(IndexReDoSTest.class);
	}

	public IndexReDoSTest(String name) {
		super(name);
	}

	/**
	 * Demonstrates that a normal regex match completes quickly.
	 * This is the baseline — no ReDoS, normal behavior.
	 */
	public void testNormalRegexMatchCompletes() {
		char[] pattern = "module\\..*".toCharArray();
		char[] word = "module.java.base".toCharArray();

		boolean result = Index.isMatch(pattern, word, SearchPattern.R_REGEXP_MATCH);
		assertTrue("Normal regex should match", result);
	}

	/**
	 * Demonstrates that a non-matching normal regex also completes quickly.
	 */
	public void testNormalRegexNonMatchCompletes() {
		char[] pattern = "module\\.foo.*".toCharArray();
		char[] word = "module.java.base".toCharArray();

		boolean result = Index.isMatch(pattern, word, SearchPattern.R_REGEXP_MATCH);
		assertFalse("Non-matching regex should return false", result);
	}

	/**
	 * Verifies that the pathological regex {@code (a+)+$} matched against a string of
	 * 'a' characters followed by a non-matching character does NOT cause catastrophic
	 * backtracking. Without the ReDoS guard this would take minutes/hours (O(2^n)); with
	 * the guard it aborts within the configured time budget and returns {@code false}.
	 *
	 * @see <a href="https://owasp.org/www-community/attacks/Regular_expression_Denial_of_Service_-_ReDoS">OWASP ReDoS</a>
	 */
	public void testReDoSWithCatastrophicBacktracking() {
		// Pathological regex pattern: nested quantifiers cause exponential backtracking
		char[] pattern = "(a+)+$".toCharArray();

		// Input: 40 'a' characters followed by '!' (a non-matching tail).
		// Without protection the engine would try 2^40 combinations before failing.
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 40; i++) {
			sb.append('a');
		}
		sb.append('!');
		char[] word = sb.toString().toCharArray();

		// Run the match in a separate thread with a timeout
		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<Boolean> future = executor.submit(() ->
				Index.isMatch(pattern, word, SearchPattern.R_REGEXP_MATCH)
			);

			try {
				Boolean result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				// The guard aborts the runaway match and reports a non-match.
				assertFalse("Pathological pattern must report a non-match", result.booleanValue());
			} catch (TimeoutException e) {
				future.cancel(true);
				fail("ReDoS vulnerability detected: Index.isMatch with pathological regex " +
					"'(a+)+$' did not complete within " + TIMEOUT_SECONDS + " seconds. " +
					"A caller can cause Denial of Service by supplying a pathological regex pattern " +
					"via SearchPattern.R_REGEXP_MATCH. The regex engine's backtracking causes " +
					"exponential time complexity (O(2^n)) on non-matching inputs.");
			} catch (ExecutionException e) {
				// Unexpected exception during matching
				fail("Unexpected exception during regex match: " + e.getCause().getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Another ReDoS variant using a different pathological pattern.
	 * The pattern {@code (.*a){20}} causes catastrophic backtracking when
	 * the input has many 'a' characters but doesn't fully match. The guard
	 * must abort within the time budget and return {@code false}.
	 */
	public void testReDoSWithNestedWildcard() {
		// Pathological regex: repeated greedy wildcard
		char[] pattern = "(.*a){20}".toCharArray();

		// Input that partially matches: enough 'a' characters to be ambiguous
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 20; i++) {
			sb.append("aXa");
		}
		sb.append('!');
		char[] word = sb.toString().toCharArray();

		ExecutorService executor = Executors.newSingleThreadExecutor();
		try {
			Future<Boolean> future = executor.submit(() ->
				Index.isMatch(pattern, word, SearchPattern.R_REGEXP_MATCH)
			);

			try {
				Boolean result = future.get(TIMEOUT_SECONDS, TimeUnit.SECONDS);
				assertFalse("Pathological pattern must report a non-match", result.booleanValue());
			} catch (TimeoutException e) {
				future.cancel(true);
				fail("ReDoS vulnerability detected: Index.isMatch with pathological regex " +
					"'(.*a){20}' did not complete within " + TIMEOUT_SECONDS + " seconds.");
			} catch (ExecutionException e) {
				fail("Unexpected exception during regex match: " + e.getCause().getMessage());
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("Test interrupted");
			}
		} finally {
			executor.shutdownNow();
		}
	}

	/**
	 * Verifies that an invalid regex pattern (e.g. an unclosed group) is handled
	 * gracefully: {@code Index.isMatch} returns {@code false} instead of propagating
	 * an unchecked {@link java.util.regex.PatternSyntaxException} that could abort the
	 * enclosing search operation.
	 */
	public void testInvalidRegexIsHandledGracefully() {
		char[] pattern = "(unclosed[group".toCharArray();
		char[] word = "anything".toCharArray();

		try {
			boolean result = Index.isMatch(pattern, word, SearchPattern.R_REGEXP_MATCH);
			assertFalse("Invalid regex must be treated as a non-match", result);
		} catch (java.util.regex.PatternSyntaxException e) {
			fail("Invalid regex should be handled gracefully, but PatternSyntaxException propagated: "
					+ e.getMessage());
		}
	}
}
