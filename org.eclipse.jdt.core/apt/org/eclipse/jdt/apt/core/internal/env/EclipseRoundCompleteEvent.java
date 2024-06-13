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
 *    tyeung@bea.com - initial implementation.
 *******************************************************************************/
package org.eclipse.jdt.apt.core.internal.env;


import com.sun.mirror.apt.RoundCompleteEvent;
import com.sun.mirror.apt.RoundState;

public class EclipseRoundCompleteEvent extends RoundCompleteEvent
{
	static final long serialVersionUID = 0;

	public EclipseRoundCompleteEvent(final BuildEnv env)
	{
		super( env, new State(env) );
	}

	private static class State implements RoundState
	{
		private final BuildEnv _env;
		State(BuildEnv env){ _env = env; }
		@Override
		public boolean classFilesCreated() { return _env.hasGeneratedClassFiles(); }
		@Override
		public boolean errorRaised() {  return _env.hasRaisedErrors(); }
		@Override
		public boolean sourceFilesCreated() {  return _env.hasGeneratedSourceFiles(); }
		@Override
		public boolean finalRound() {
			// apt terminates when there are no new generated source files
			return !_env.hasGeneratedSourceFiles();
		}
	}
}
