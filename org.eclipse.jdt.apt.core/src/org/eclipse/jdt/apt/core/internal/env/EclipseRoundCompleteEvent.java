/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
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
		public boolean classFilesCreated() { return _env.hasGeneratedClassFiles(); }		
		public boolean errorRaised() {  return _env.hasRaisedErrors(); }			
		public boolean sourceFilesCreated() {  return _env.hasGeneratedSourceFiles(); }			
		public boolean finalRound() {
			// apt terminates when there are no new generated source files 
			return !_env.hasGeneratedSourceFiles(); 
		}
	}
}
