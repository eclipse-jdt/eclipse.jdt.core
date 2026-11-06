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
import org.eclipse.jdt.core.IOpenable;
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
	// Remember intervening working-copy transitions, including ordinary -> working copy -> ordinary.
	// Only in-flight attempts retain handles. Equality is the Java model's handle equality.
	private final Map<CompilationUnit, OpenState> opening = new HashMap<>();
	// A failed opener must not close a shared candidate that another opener has not published yet.
	private final Map<IBuffer, CandidateState> candidates = new IdentityHashMap<>();

	BufferLifecycle(JavaModelManager manager) {
		this.manager = manager;
	}

	private static final class OpenState {
		Object generation = new Object();
		int attempts;
	}

	/** Owns one attempt from provider creation through preparation, publication and cleanup. */
	private final class Opening implements AutoCloseable {
		private final CompilationUnit unit;
		private final OpenState state;
		private final Object generation;
		private final PerWorkingCopyInfo workingCopyInfo;
		private IBuffer buffer;
		private CandidateState candidateState;

		Opening(CompilationUnit unit, OpenState state, PerWorkingCopyInfo workingCopyInfo) {
			this.unit = unit;
			this.state = state;
			this.generation = state.generation;
			this.workingCopyInfo = workingCopyInfo;
		}

		boolean isWorkingCopy() {
			return !this.unit.isPrimary() || this.workingCopyInfo != null;
		}

		private boolean isCurrent() {
			return this.generation == this.state.generation
					&& this.workingCopyInfo == this.unit.getPerWorkingCopyInfo();
		}

		private void checkOpen() throws JavaModelException {
			if (this.buffer.isClosed()) {
				throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
			}
		}

		OpenResult prepareAndPublish() throws JavaModelException {
			this.buffer = isWorkingCopy() ? this.unit.owner.createBuffer(this.unit) : BufferManager.createBuffer(this.unit);
			if (this.buffer == null) {
				synchronized (BufferLifecycle.this.manager) {
					return isCurrent() ? new OpenResult.Complete(null) : OpenResult.Retry.INSTANCE;
				}
			}
			BufferManager buffers = this.unit.getBufferManager();
			synchronized (BufferLifecycle.this.manager) {
				IBuffer existing = buffers.getBuffer(this.unit);
				if (existing == this.buffer) {
					// A cached buffer already has a registration owned by its cache entry.
					return isCurrent() ? new OpenResult.Complete(existing) : OpenResult.Retry.INSTANCE;
				}
				this.candidateState = BufferLifecycle.this.candidates.computeIfAbsent(this.buffer, _ -> new CandidateState(this.unit));
				this.candidateState.claims++;
				if (!isCurrent()) {
					return OpenResult.Retry.INSTANCE;
				}
				if (existing != null) {
					return new OpenResult.Complete(existing);
				}
			}
			if (!this.candidateState.retiring) {
				// Null contents mean uninitialized, not closed. Never initialize a closed provider buffer.
				checkOpen();
				if (this.buffer.getCharacters() == null) {
					char[] contents = readContents(this.unit, isWorkingCopy());
					// Another opener may have initialized or edited this physical candidate meanwhile.
					synchronized (this.candidateState.initializationLock) {
						if (!this.candidateState.retiring) {
							checkOpen();
							if (this.buffer.getCharacters() == null) {
								this.buffer.setContents(contents);
							}
						}
					}
				}
				if (!this.candidateState.retiring) {
					checkOpen();
					// IBuffer makes repeated registration of the identical listener a no-op.
					// Equal handles can have different AST caches, so all contenders must
					// register the same object, regardless of the provider's equality policy.
					this.buffer.addBufferChangedListener(this.candidateState.listener);
				}
			}
			try {
				synchronized (BufferLifecycle.this.manager) {
					if (!isCurrent()) {
						return OpenResult.Retry.INSTANCE;
					}
					IBuffer existing = buffers.getBuffer(this.unit);
					if (existing != null) {
						return new OpenResult.Complete(existing);
					}
					if (this.candidateState.retiring) {
						throw new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.INVALID_CONTENTS));
					}
					IOpenable owner = this.buffer.getOwner();
					checkOpen();
					// Queries on this buffer and eviction candidates may reenter a transition.
					IBuffer published = buffers.cacheBuffer(this.candidateState.listener, owner, this.buffer,
							() -> {
								checkOpen();
								return isCurrent() && !this.candidateState.retiring;
							});
					if (published == null) {
						return OpenResult.Retry.INSTANCE;
					}
					// Provider contents can change silently; ordinary file changes are counted separately.
					if (published == this.buffer && isWorkingCopy()) {
						JavaModelManager.sourceEnvironmentChanged();
					}
					return new OpenResult.Complete(published);
				}
			} finally {
				buffers.closeBuffers();
			}
		}

		void releaseCandidate() {
			try {
				if (this.candidateState != null) {
					BufferLifecycle.this.releaseCandidate(this);
				}
			} finally {
				this.candidateState = null;
			}
		}

		@Override
		public void close() {
			try {
				releaseCandidate();
			} finally {
				synchronized (BufferLifecycle.this.manager) {
					if (--this.state.attempts == 0) {
						BufferLifecycle.this.opening.remove(this.unit);
					}
				}
			}
		}
	}

	private Opening beginOpen(CompilationUnit unit) throws JavaModelException {
		synchronized (this.manager) {
			PerWorkingCopyInfo workingCopyInfo = unit.getPerWorkingCopyInfo();
			if (!unit.isPrimary() && workingCopyInfo == null) {
				throw unit.newNotPresentException();
			}
			OpenState state = this.opening.computeIfAbsent(unit, _ -> new OpenState());
			state.attempts++;
			return new Opening(unit, state, workingCopyInfo);
		}
	}

	private void invalidate(CompilationUnit unit) {
		OpenState state = this.opening.get(unit);
		if (state != null) {
			state.generation = new Object();
		}
	}

	private static final class CandidateState {
		final Object initializationLock = new Object();
		final Openable listener;
		int claims;
		volatile boolean retiring;
		boolean closeStarted;
		boolean closeFinished;

		CandidateState(Openable listener) {
			this.listener = listener;
		}
	}

	private void releaseCandidate(Opening attempt) {
		synchronized (this.manager) {
			CandidateState state = attempt.candidateState;
			if (--state.claims != 0) {
				return;
			}
			if (!state.retiring && attempt.unit.getBufferManager().getBuffer(attempt.buffer.getOwner()) == attempt.buffer) {
				this.candidates.remove(attempt.buffer, state);
				return;
			}
			state.retiring = true;
		}
		closeRetired(new RetiredBuffer(attempt.candidateState.listener, attempt.buffer, attempt.candidateState));
	}

	private sealed interface OpenResult {
		record Complete(IBuffer buffer) implements OpenResult {}
		enum Retry implements OpenResult { INSTANCE }
	}

	IBuffer openBuffer(CompilationUnit unit) throws JavaModelException {
		while (true) {
			try (Opening attempt = beginOpen(unit)) {
				OpenResult result = attempt.prepareAndPublish();
				// Finish provider cleanup before the last check; keep tracking intervening transitions.
				attempt.releaseCandidate();
				synchronized (this.manager) {
					if (result instanceof OpenResult.Complete(var buffer) && attempt.isCurrent()) {
						return buffer;
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
		try (Retirement retirement = new Retirement()) {
			synchronized (this.manager) {
				boolean createWorkingCopy = unit.getPerWorkingCopyInfo() == null;
				if (createWorkingCopy) {
					invalidate(unit);
					retirement.buffer = detach(unit);
					// The buffer is already detached, so model cleanup cannot call its provider.
					this.manager.removeInfoAndChildren(unit);
				}
				this.manager.getPerWorkingCopyInfo(unit, true, true, requestor);
				if (createWorkingCopy) {
					// Notify after both model cleanup and working-copy registration are complete.
					JavaModelManager.sourceEnvironmentChanged();
				}
			}
		}
	}

	/** The scope never escapes: old-generation cleanup always precedes the captured buffer's close. */
	private final class Retirement implements AutoCloseable {
		private RetiredBuffer buffer;

		@Override
		public void close() {
			closeRetired(this.buffer);
		}
	}

	void discardWorkingCopy(CompilationUnit unit, Runnable afterFinalDiscard) throws JavaModelException {
		discard(unit, afterFinalDiscard);
	}

	/** Preserves the existing manager method's count-based contract at its compatibility boundary. */
	int discardPerWorkingCopyInfo(CompilationUnit unit) throws JavaModelException {
		return discard(unit, () -> {
			// Compatibility callers have no additional final-discard operation.
		});
	}

	private int discard(CompilationUnit unit, Runnable afterFinalDiscard) throws JavaModelException {
		// Do not acquire model information while holding the per-working-copy map lock.
		JavaElementDeltaBuilder deltaBuilder = unit.isPrimary() && unit.hasUnsavedChanges() ? new JavaElementDeltaBuilder(unit) : null;
		try (Retirement retirement = new Retirement()) {
			synchronized (this.manager) {
				int useCount = this.manager.decrementWorkingCopyUseCount(unit);
				if (useCount != 0) {
					return useCount;
				}
				invalidate(unit);
				retirement.buffer = detach(unit);
				this.manager.removeInfoAndChildren(unit);
				// Even a bufferless generation can have model information to retire.
				JavaModelManager.sourceEnvironmentChanged();
			}
			if (deltaBuilder != null) {
				deltaBuilder.buildDeltas();
				if (deltaBuilder.delta != null) {
					this.manager.getDeltaProcessor().registerJavaModelDelta(deltaBuilder.delta);
				}
			}
			afterFinalDiscard.run();
			return 0;
		}
	}

	private record RetiredBuffer(Openable listener, IBuffer buffer, CandidateState state) {}

	private RetiredBuffer detach(CompilationUnit unit) {
		unit.invalidateAST();
		BufferManager.DetachedBuffer detached = unit.getBufferManager().detachBuffer(unit);
		if (detached == null) {
			return null;
		}
		if (detached.listener() instanceof CompilationUnit registeredUnit && registeredUnit != unit) {
			registeredUnit.invalidateAST();
		}
		IBuffer buffer = detached.buffer();
		CandidateState state = this.candidates.computeIfAbsent(buffer, _ -> new CandidateState(detached.listener()));
		state.retiring = true;
		return new RetiredBuffer(detached.listener(), buffer, state);
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
				retired.buffer.removeBufferChangedListener(retired.listener);
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
