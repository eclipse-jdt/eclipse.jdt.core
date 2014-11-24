package org.eclipse.jdt.apt.core.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IExecutableExtensionFactory;

/**
 * ExtensionPoint-Factory to access the singleton instance of
 * the {@link AptCompilationParticipant} during ExtensionPoint processing.
 */
public class AptCompilationParticipantExtensionFactory implements
		IExecutableExtensionFactory {

	public Object create() throws CoreException {
		return AptCompilationParticipant.getInstance();
	}

}
