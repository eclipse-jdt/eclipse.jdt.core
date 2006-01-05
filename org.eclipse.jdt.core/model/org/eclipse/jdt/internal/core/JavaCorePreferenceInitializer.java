/*******************************************************************************
 * Copyright (c) 2000, 2005 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.util.*;

import org.eclipse.core.runtime.preferences.*;
import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;

/**
 * JavaCore eclipse preferences initializer.
 * Initially done in JavaCore.initializeDefaultPreferences which was deprecated
 * with new eclipse preferences mechanism.
 */
public class JavaCorePreferenceInitializer extends AbstractPreferenceInitializer {

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	public void initializeDefaultPreferences() {

		// Get options names set
		HashSet optionNames = JavaModelManager.getJavaModelManager().optionNames;
		
		// Compiler settings
		Map defaultOptionsMap = new CompilerOptions().getMap(); // compiler defaults
		
		// Override some compiler defaults
		defaultOptionsMap.put(JavaCore.COMPILER_LOCAL_VARIABLE_ATTR, JavaCore.GENERATE);
		defaultOptionsMap.put(JavaCore.COMPILER_CODEGEN_UNUSED_LOCAL, JavaCore.PRESERVE);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_TAGS, JavaCore.DEFAULT_TASK_TAGS);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_PRIORITIES, JavaCore.DEFAULT_TASK_PRIORITIES);
		defaultOptionsMap.put(JavaCore.COMPILER_TASK_CASE_SENSITIVE, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.COMPILER_DOC_COMMENT_SUPPORT, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.COMPILER_PB_FORBIDDEN_REFERENCE, JavaCore.ERROR);
	
		// Builder settings
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_INVALID_CLASSPATH, JavaCore.ABORT); 
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_DUPLICATE_RESOURCE, JavaCore.WARNING); 
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, JavaCore.CLEAN); 

		// JavaCore settings
		defaultOptionsMap.put(JavaCore.CORE_JAVA_BUILD_ORDER, JavaCore.IGNORE); 
		defaultOptionsMap.put(JavaCore.CORE_INCOMPLETE_CLASSPATH, JavaCore.ERROR); 
		defaultOptionsMap.put(JavaCore.CORE_CIRCULAR_CLASSPATH, JavaCore.ERROR); 
		defaultOptionsMap.put(JavaCore.CORE_INCOMPATIBLE_JDK_LEVEL, JavaCore.IGNORE); 
		defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, JavaCore.ENABLED); 
		defaultOptionsMap.put(JavaCore.CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, JavaCore.ENABLED); 

		// encoding setting comes from resource plug-in
		optionNames.add(JavaCore.CORE_ENCODING);

		// Formatter settings
		Map codeFormatterOptionsMap = DefaultCodeFormatterConstants.getEclipseDefaultSettings(); // code formatter defaults
		for (Iterator iter = codeFormatterOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			defaultOptionsMap.put(optionName, entry.getValue());
			optionNames.add(optionName);
		}
		
		// ImportRewrite settings
		defaultOptionsMap.put(JavaCore.IMPORTREWRITE_IMPORT_ORDER, "java;javax;org;com"); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.IMPORTREWRITE_ONDEMAND_THRESHOLD, String.valueOf(99));

		// CodeAssist settings
		defaultOptionsMap.put(JavaCore.CODEASSIST_VISIBILITY_CHECK, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_IMPLICIT_QUALIFICATION, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
		defaultOptionsMap.put(JavaCore.CODEASSIST_FORBIDDEN_REFERENCE_CHECK, JavaCore.ENABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_DISCOURAGED_REFERENCE_CHECK, JavaCore.DISABLED);
		defaultOptionsMap.put(JavaCore.CODEASSIST_CAMEL_CASE_MATCH, JavaCore.ENABLED);
		
		// Time out for parameter names
		defaultOptionsMap.put(JavaCore.TIMEOUT_FOR_PARAMETER_NAME_FROM_ATTACHED_JAVADOC, "50"); //$NON-NLS-1$
		// TODO (olivier) To remove after M4
		// deprecated constant
		defaultOptionsMap.put("org.eclipse.jdt.core.codeAssist.timeoutForParameterNameFromAttachedJavadoc", "50"); //$NON-NLS-1$//$NON-NLS-2$
		
		// Store default values to default preferences
	 	IEclipsePreferences defaultPreferences = new DefaultScope().getNode(JavaCore.PLUGIN_ID);
		for (Iterator iter = defaultOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			defaultPreferences.put(optionName, (String)entry.getValue());
			optionNames.add(optionName);
		}
	}
}
