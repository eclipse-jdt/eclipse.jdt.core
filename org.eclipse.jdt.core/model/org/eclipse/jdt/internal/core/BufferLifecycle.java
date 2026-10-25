/*******************************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Contributors to the Eclipse Foundation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IProblemRequestor;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.core.JavaModelManager.PerWorkingCopyInfo;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * Coordinates compilation-unit buffer publication and working-copy transitions.
 * State decisions use the model-manager monitor. Source reads, provider creation,
 * candidate initialization and explicit buffer close calls occur outside these
 * transactions. Existing callers can themselves hold the model-manager monitor.
 */
final class BufferLifecycle {

	private final JavaModelManager manager;
	// Only in-flight attempts retain handles. Equality is the Java model's handle equality.
	private final Map<CompilationUnit, OpenState> opening = new HashMap<>();
	// Providers may return the same physical buffer to several concurrent openers.
	private final Map<IBuffer, CandidateState> candidates = new IdentityHashMap<>();

	BufferLifecycle(JavaModelManager manager) {
		this.manager = manager;
	}

	private static final class OpenState {
		Object generation = new Object();
		int attempts;
	}

	private record OpenAttempt(BufferLifecycle lifecycle, CompilationUnit unit, OpenState state,
			Object generation, PerWorkingCopyInfo workingCopyInfo) implements AutoCloseable {
		boolean isWorkingCopy() {
			return !this.unit.isPrimary() || this.workingCopyInfo != null;
		}
		@Override
		public void close() {
			synchronized (this.lifecycle.manager) {
				if (--this.state.attempts == 0) {
					this.lifecycle.opening.remove(this.unit);
				}
			}
		}
	}

	private OpenAttempt beginOpen(CompilationUnit unit) throws JavaModelException {
		synchronized (this.manager) {
			PerWorkingCopyInfo workingCopyInfo = unit.getPerWorkingCopyInfo();
			if (!unit.isPrimary() && workingCopyInfo == null) {
				throw unit.newNotPresentException();
			}
			OpenState state = this.opening.computeIfAbsent(unit, key -> new OpenState());
			state.attempts++;
			return new OpenAttempt(this, unit, state, state.generation, workingCopyInfo);
		}
	}

	private boolean isCurrent(OpenAttempt attempt) {
		return attempt.generation == attempt.state.generation
				&& attempt.workingCopyInfo == attempt.unit.getPerWorkingCopyInfo();
	}

	private void invalidate(CompilationUnit unit) {
		OpenState state = this.opening.get(unit);
		if (state != null) {
			state.generation = new Object();
		}
	}

	private static final class CandidateState {
		final Object initializationLock = new Object();
		int claims;
		volatile boolean retiring;
		boolean closeStarted;
		boolean closeFinished;
	}

	private record Candidate(BufferLifecycle lifecycle, CompilationUnit unit, IBuffer buffer,
			CandidateState state) implements AutoCloseable {
		void initialize(boolean workingCopy) throws JavaModelException {
			if (!this.state.retiring && this.buffer.getCharacters() == null) {
				char[] contents = readContents(this.unit, workingCopy);
				// Serialize only initialization of this physical candidate, not unrelated providers.
				// Some providers report null, uninitialized contents as closed; validate after initialization.
				synchronized (this.state.initializationLock) {
					if (!this.state.retiring && this.buffer.getCharacters() == null) {
						this.buffer.setContents(contents);
					}
				}
			}
		}
		@Override
		public void close() {
			this.lifecycle.releaseCandidate(this);
		}
	}

	private Candidate claim(CompilationUnit unit, IBuffer buffer) {
		synchronized (this.manager) {
			CandidateState state = this.candidates.computeIfAbsent(buffer, key -> new CandidateState());
			state.claims++;
			return new Candidate(this, unit, buffer, state);
		}
	}

	private void releaseCandidate(Candidate candidate) {
		synchronized (this.manager) {
			CandidateState state = candidate.state;
			if (--state.claims != 0) {
				return;
			}
			if (!state.retiring && candidate.unit.getBufferManager().getBuffer(candidate.buffer.getOwner()) == candidate.buffer) {
				this.candidates.remove(candidate.buffer, state);
				return;
			}
			state.retiring = true;
		}
		closeRetired(new RetiredBuffer(candidate.unit, candidate.buffer, candidate.state));
	}

	private sealed interface Publication {
		record Published(IBuffer buffer) implements Publication {}
		record Existing(IBuffer buffer) implements Publication {}
		record Retry() implements Publication {}
	}

	private Publication publish(OpenAttempt attempt, Candidate candidate) throws JavaModelException {
		synchronized (this.manager) {
			if (!isCurrent(attempt)) {
				return new Publication.Retry();
			}
			BufferManager buffers = attempt.unit.getBufferManager();
			IBuffer existing = buffers.getBuffer(attempt.unit);
			if (existing != null) {
				return new Publication.Existing(existing);
			}
			if (candidate.state.retiring || candidate.buffer.isClosed()) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
			}
			buffers.cacheBuffer(candidate.buffer);
			return new Publication.Published(candidate.buffer);
		}
	}

	IBuffer openBuffer(CompilationUnit unit) throws JavaModelException {
		while (true) {
			try (OpenAttempt attempt = beginOpen(unit)) {
				IBuffer buffer = attempt.isWorkingCopy() ? unit.owner.createBuffer(unit) : BufferManager.createBuffer(unit);
				if (buffer == null) {
					return null;
				}
				try (Candidate candidate = claim(unit, buffer)) {
					synchronized (this.manager) {
						if (!isCurrent(attempt)) {
							continue;
						}
						IBuffer existing = unit.getBufferManager().getBuffer(unit);
						if (existing != null) {
							return existing;
						}
					}
					candidate.initialize(attempt.isWorkingCopy());
					buffer.addBufferChangedListener(unit);
					Publication publication;
					try {
						publication = publish(attempt, candidate);
					} finally {
						unit.getBufferManager().closeBuffers();
					}
					switch (publication) {
						case Publication.Published(var published) -> { return published; }
						case Publication.Existing(var existing) -> { return existing; }
						case Publication.Retry() -> { /* retry with the new generation's provider */ }
					}
				}
			}
		}
	}

	private static char[] readContents(CompilationUnit unit, boolean workingCopy) throws JavaModelException {
		if (workingCopy && !unit.isPrimary()) {
			CompilationUnit original = new CompilationUnit((PackageFragment) unit.getParent(), unit.getElementName(), DefaultWorkingCopyOwner.PRIMARY);
			if (original.isOpen()) {
				String contents = original.getSource();
				return contents == null ? null : contents.toCharArray();
			}
		}
		IFile file = (IFile) unit.getResource();
		if (file == null || !file.exists()) {
			if (!workingCopy) {
				throw unit.newNotPresentException();
			}
			return CharOperation.NO_CHAR;
		}
		return Util.getResourceContentsAsCharArray(file);
	}

	boolean retainWorkingCopy(CompilationUnit unit) {
		synchronized (this.manager) {
			return this.manager.getPerWorkingCopyInfo(unit, false, true, null) != null;
		}
	}

	void becomeWorkingCopy(CompilationUnit unit, IProblemRequestor requestor) throws JavaModelException {
		RetiredBuffer retired = null;
		try {
			synchronized (this.manager) {
				if (unit.getPerWorkingCopyInfo() == null) {
					invalidate(unit);
					retired = detach(unit);
					// The buffer is already detached, so model cleanup cannot call its provider.
					this.manager.removeInfoAndChildren(unit);
				}
				this.manager.getPerWorkingCopyInfo(unit, true, true, requestor);
			}
		} finally {
			closeRetired(retired);
		}
	}

	sealed interface Release extends AutoCloseable {
		int useCount();
		@Override
		default void close() {}
	}

	private record Absent() implements Release {
		@Override
		public int useCount() { return -1; }
	}

	private record Retained(int useCount) implements Release {}

	private record Retired(BufferLifecycle lifecycle, RetiredBuffer buffer) implements Release {
		@Override
		public int useCount() { return 0; }
		@Override
		public void close() { this.lifecycle.closeRetired(this.buffer); }
	}

	Release releaseWorkingCopy(CompilationUnit unit) throws JavaModelException {
		// Do not acquire model information while holding the per-working-copy map lock.
		JavaElementDeltaBuilder deltaBuilder = unit.isPrimary() && unit.hasUnsavedChanges() ? new JavaElementDeltaBuilder(unit) : null;
		Retired retired = null;
		try {
			synchronized (this.manager) {
				int useCount = this.manager.decrementWorkingCopyUseCount(unit);
				if (useCount < 0) {
					return new Absent();
				}
				if (useCount > 0) {
					return new Retained(useCount);
				}
				invalidate(unit);
				retired = new Retired(this, detach(unit));
				this.manager.removeInfoAndChildren(unit);
			}
			if (deltaBuilder != null) {
				deltaBuilder.buildDeltas();
				if (deltaBuilder.delta != null) {
					this.manager.getDeltaProcessor().registerJavaModelDelta(deltaBuilder.delta);
				}
			}
			return retired;
		} catch (JavaModelException | RuntimeException | Error e) {
			if (retired != null) {
				try {
					retired.close();
				} catch (RuntimeException | Error closeFailure) {
					if (closeFailure != e) {
						e.addSuppressed(closeFailure);
					}
				}
			}
			throw e;
		}
	}

	private record RetiredBuffer(CompilationUnit unit, IBuffer buffer, CandidateState state) {}

	private RetiredBuffer detach(CompilationUnit unit) {
		IBuffer buffer = unit.getBufferManager().detachBuffer(unit);
		if (buffer == null) {
			return null;
		}
		CandidateState state = this.candidates.computeIfAbsent(buffer, key -> new CandidateState());
		state.retiring = true;
		return new RetiredBuffer(unit, buffer, state);
	}

	private void closeRetired(RetiredBuffer retired) {
		if (retired == null) {
			return;
		}
		boolean close;
		synchronized (this.manager) {
			close = !retired.state.closeStarted;
			retired.state.closeStarted = true;
		}
		try {
			try {
				retired.buffer.removeBufferChangedListener(retired.unit);
			} catch (RuntimeException | Error failure) {
				if (close) {
					try {
						retired.buffer.close();
					} catch (RuntimeException | Error closeFailure) {
						if (closeFailure != failure) {
							failure.addSuppressed(closeFailure);
						}
					}
				}
				throw failure;
			}
			if (close) {
				retired.buffer.close();
			}
		} finally {
			synchronized (this.manager) {
				if (close) {
					retired.state.closeFinished = true;
				}
				if (retired.state.closeFinished && retired.state.claims == 0) {
					this.candidates.remove(retired.buffer, retired.state);
				}
			}
		}
	}
}
