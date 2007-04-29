/*******************************************************************************
 * Copyright (c) 2007 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package targets.model.pc;

@SuppressWarnings("deprecation")
@Deprecated
public class Deprecation {
	@Deprecated
	public class deprecatedClass {}
	
	@Deprecated
	public enum deprecatedEnum { Val1 }
	
	@Deprecated
	public interface deprecatedInterface {}
	
	@Deprecated
	public String deprecatedField;
	
	@Deprecated
	void deprecatedMethod() {}
	
	public class nonDeprecatedClass {}
	
	public enum nonDeprecatedEnum { Val1 }
	
	public interface nonDeprecatedInterface {}
	
	public String nonDeprecatedField;
	
	void nonDeprecatedMethod() {}
}