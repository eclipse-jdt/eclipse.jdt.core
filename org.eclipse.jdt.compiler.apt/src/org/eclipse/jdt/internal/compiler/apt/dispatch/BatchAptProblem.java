package org.eclipse.jdt.internal.compiler.apt.dispatch;

import org.eclipse.jdt.internal.compiler.problem.DefaultProblem;

public class BatchAptProblem extends DefaultProblem {
	private static final String MARKER_ID = "org.eclipse.jdt.compiler.apt.compiler.problem"; //$NON-NLS-1$
	public BatchAptProblem(
			char[] originatingFileName,
			String message,
			int id,
			String[] stringArguments,
			int severity,
			int startPosition,
			int endPosition,
			int line,
			int column) {
		super(originatingFileName,
			message,
			id,
			stringArguments,
			severity,
			startPosition,
			endPosition,
			line,
			column);
	}
	@Override
	public int getCategoryID() {
		return CAT_UNSPECIFIED;
	}

	@Override
	public String getMarkerType() {
		return MARKER_ID;
	}
}
