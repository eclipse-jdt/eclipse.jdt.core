/*******************************************************************************
 * Copyright (c) 2006 BEA Systems, Inc. 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *    
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.dispatch;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Manages context during a single round of annotation processing.
 */
public class RoundDispatcher {
	
	private final Set<TypeElement> _unclaimedAnnotations;
	private final RoundEnvironment _roundEnv;
	private final IProcessorProvider _provider;
	private boolean _searchForStar = false;
	
	/**
	 * Processors discovered so far.  This list may grow during the
	 * course of a round, as additional processors are discovered.
	 */
	private final List<ProcessorInfo> _processors;
	
	/**
	 * @param rootAnnotations a possibly empty but non-null set of annotations on the
	 * root compilation units of this round.  A local copy of the set will be made, to
	 * avoid modifying the set passed in.
	 */
	public RoundDispatcher(
			IProcessorProvider provider, RoundEnvironment env, Set<TypeElement> rootAnnotations)
	{
		_provider = provider;
		_processors = provider.getDiscoveredProcessors();
		_roundEnv = env;
		_unclaimedAnnotations = new HashSet<TypeElement>(rootAnnotations);
	}
	
	/**
	 * Handle a complete round, dispatching to all appropriate processors. 
	 */
	public void round()
	{
		// If there are no root annotations, try to find a processor that claims "*"
		_searchForStar = _unclaimedAnnotations.isEmpty();
		
		// Iterate over all the already-found processors, giving each one a chance at the unclaimed
		// annotations. If a processor is called at all, it is called on every subsequent round 
		// including the final round, but it may be called with an empty set of annotations.
		for (ProcessorInfo pi : _processors) {
			handleProcessor(pi);
		}
		
		// If there are any unclaimed annotations, or if there were no root annotations and
		// we have not yet run into a processor that claimed "*", continue discovery.
		while (_searchForStar || !_unclaimedAnnotations.isEmpty()) {
			ProcessorInfo pi = _provider.discoverNextProcessor();
			if (null == pi) {
				// There are no more processors to be discovered.
				break;
			}
			handleProcessor(pi);
		}
		
		// TODO: If !unclaimedAnnos.isEmpty(), issue a warning.
	}
	
	/**
	 * Evaluate a single processor.  Depending on the unclaimed annotations,
	 * the annotations this processor supports, and whether it has already been
	 * called in a previous round, possibly call its process() method.
	 */
	private void handleProcessor(ProcessorInfo pi)
	{
		try {
			Set<TypeElement> annotationsToProcess = new HashSet<TypeElement>();
			boolean shouldCall = pi.computeSupportedAnnotations(
					_unclaimedAnnotations, annotationsToProcess);
			if (shouldCall) {
				if (pi._processor.process(annotationsToProcess, _roundEnv)) {
					// The processor claimed its annotations.
					_unclaimedAnnotations.removeAll(annotationsToProcess);
					if (pi.supportsStar()) {
						_searchForStar = false;
					}
				}
			}
		} catch (Exception e) {
			// If a processor throws an exception (as opposed to reporting an error),
			// report it and abort compilation by throwing AbortCompilation.
			_provider.reportProcessorException(pi._processor, e);
		}
	}
	
}
