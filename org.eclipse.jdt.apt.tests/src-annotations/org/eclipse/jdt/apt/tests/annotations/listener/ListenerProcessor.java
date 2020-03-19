/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.tests.annotations.listener;

import org.eclipse.jdt.apt.tests.annotations.BaseProcessor;

import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundCompleteListener;

/**
 * Ensure that the RoundCompleteListener does get called, and that it is possible
 * to modify the listener list from within the callback (see bug 180595).
 */
public class ListenerProcessor extends BaseProcessor
{
	private static int _calls = 0;

	private class Listener implements RoundCompleteListener {
		public void roundComplete(RoundCompleteEvent event) {
			if (event.getRoundState().finalRound()) {
				++_calls;
				_env.removeListener(this);
				// Only report success if we make it this far on both listeners
				if (2 == _calls) {
					ListenerProcessor.this.reportSuccess(ListenerProcessor.class);
				}
			}
		}
	}

	public ListenerProcessor(AnnotationProcessorEnvironment env) {
		super(env);
		env.addListener(new Listener());
		env.addListener(new Listener());
	}

	/* (non-Javadoc)
	 * @see com.sun.mirror.apt.AnnotationProcessor#process()
	 */
	public void process()
	{
		// do nothing
	}

}
