/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE
 *                                 COMPILER_PB_STATIC_ACCESS_RECEIVER
 *                                 COMPILER_TASK_TAGS
 *                                 CORE_CIRCULAR_CLASSPATH
 *                                 CORE_INCOMPLETE_CLASSPATH
 *     IBM Corporation - added run(IWorkspaceRunnable, IProgressMonitor)
 *     IBM Corporation - added exclusion patterns to source classpath entries
 *     IBM Corporation - added specific output location to source classpath entries
 *     IBM Corporation - added the following constants:
 *                                 CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER
 *                                 CLEAN
 *     IBM Corporation - added getClasspathContainerInitializer(String)
 *     IBM Corporation - added the following constants:
 *                                 CODEASSIST_ARGUMENT_PREFIXES
 *                                 CODEASSIST_ARGUMENT_SUFFIXES
 *                                 CODEASSIST_FIELD_PREFIXES
 *                                 CODEASSIST_FIELD_SUFFIXES
 *                                 CODEASSIST_LOCAL_PREFIXES
 *                                 CODEASSIST_LOCAL_SUFFIXES
 *                                 CODEASSIST_STATIC_FIELD_PREFIXES
 *                                 CODEASSIST_STATIC_FIELD_SUFFIXES
 *                                 COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_LOCAL_VARIABLE_HIDING
 *                                 COMPILER_PB_SPECIAL_PARAMETER_HIDING_FIELD
 *                                 COMPILER_PB_FIELD_HIDING
 *                                 COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT
 *                                 CORE_INCOMPATIBLE_JDK_LEVEL
 *                                 VERSION_1_5
 *                                 COMPILER_PB_SUPERFLUOUS_SEMICOLON
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_INDIRECT_STATIC_ACCESS
 *                                 COMPILER_PB_BOOLEAN_METHOD_THROWING_EXCEPTION
 *                                 COMPILER_PB_UNNECESSARY_CAST
 *     IBM Corporation - added the following constants:
 *                                 COMPILER_PB_INVALID_JAVADOC
 *                                 COMPILER_PB_INVALID_JAVADOC_TAGS
 *                                 COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY
 *                                 COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING
 *                                 COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD
 *                                 COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING
 *******************************************************************************/
package org.eclipse.jdt.core;

import java.io.File;
import java.util.*;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jdt.internal.compiler.impl.CompilerOptions;
import org.eclipse.jdt.internal.compiler.util.SuffixConstants;
import org.eclipse.jdt.internal.core.*;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * The plug-in runtime class for the Java model plug-in containing the core
 * (UI-free) support for Java projects.
 * <p>
 * Like all plug-in runtime classes (subclasses of <code>Plugin</code>), this
 * class is automatically instantiated by the platform when the plug-in gets
 * activated. Clients must not attempt to instantiate plug-in runtime classes
 * directly.
 * </p>
 * <p>
 * The single instance of this class can be accessed from any plug-in declaring
 * the Java model plug-in as a prerequisite via 
 * <code>JavaCore.getJavaCore()</code>. The Java model plug-in will be activated
 * automatically if not already active.
 * </p>
 */
public final class JavaCore extends Plugin {

	private static Plugin JAVA_CORE_PLUGIN = null; 
	/**
	 * The plug-in identifier of the Java core support
	 * (value <code>"org.eclipse.jdt.core"</code>).
	 */
	public static final String PLUGIN_ID = "org.eclipse.jdt.core" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java builder
	 * (value <code>"org.eclipse.jdt.core.javabuilder"</code>).
	 */
	public static final String BUILDER_ID = PLUGIN_ID + ".javabuilder" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java model
	 * (value <code>"org.eclipse.jdt.core.javamodel"</code>).
	 */
	public static final String MODEL_ID = PLUGIN_ID + ".javamodel" ; //$NON-NLS-1$

	/**
	 * The identifier for the Java nature
	 * (value <code>"org.eclipse.jdt.core.javanature"</code>).
	 * The presence of this nature on a project indicates that it is 
	 * Java-capable.
	 *
	 * @see org.eclipse.core.resources.IProject#hasNature(java.lang.String)
	 */
	public static final String NATURE_ID = PLUGIN_ID + ".javanature" ; //$NON-NLS-1$

	/**
	 * Name of the handle id attribute in a Java marker.
	 */
	protected static final String ATT_HANDLE_ID =
		"org.eclipse.jdt.internal.core.JavaModelManager.handleId" ; //$NON-NLS-1$

	// *************** Possible IDs for configurable options. ********************

	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LOCAL_VARIABLE_ATTR = PLUGIN_ID + ".compiler.debug.localVariable"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_LINE_NUMBER_ATTR = PLUGIN_ID + ".compiler.debug.lineNumber"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_SOURCE_FILE_ATTR = PLUGIN_ID + ".compiler.debug.sourceFile"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_CODEGEN_UNUSED_LOCAL = PLUGIN_ID + ".compiler.codegen.unusedLocal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_CODEGEN_TARGET_PLATFORM = PLUGIN_ID + ".compiler.codegen.targetPlatform"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_CODEGEN_INLINE_JSR_BYTECODE = PLUGIN_ID + ".compiler.codegen.inlineJsrBytecode"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_DOC_COMMENT_SUPPORT = PLUGIN_ID + ".compiler.doc.comment.support"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @deprecated - discontinued since turning off would violate language specs
	 */
	public static final String COMPILER_PB_UNREACHABLE_CODE = PLUGIN_ID + ".compiler.problem.unreachableCode"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @deprecated - discontinued since turning off would violate language specs
	 */
	public static final String COMPILER_PB_INVALID_IMPORT = PLUGIN_ID + ".compiler.problem.invalidImport"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_OVERRIDING_PACKAGE_DEFAULT_METHOD = PLUGIN_ID + ".compiler.problem.overridingPackageDefaultMethod"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_METHOD_WITH_CONSTRUCTOR_NAME = PLUGIN_ID + ".compiler.problem.methodWithConstructorName"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_DEPRECATION = PLUGIN_ID + ".compiler.problem.deprecation"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_DEPRECATION_IN_DEPRECATED_CODE = PLUGIN_ID + ".compiler.problem.deprecationInDeprecatedCode"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_DEPRECATION_WHEN_OVERRIDING_DEPRECATED_METHOD = "org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_HIDDEN_CATCH_BLOCK = PLUGIN_ID + ".compiler.problem.hiddenCatchBlock"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_UNUSED_LOCAL = PLUGIN_ID + ".compiler.problem.unusedLocal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER = PLUGIN_ID + ".compiler.problem.unusedParameter"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_IMPLEMENTING_ABSTRACT = PLUGIN_ID + ".compiler.problem.unusedParameterWhenImplementingAbstract"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PARAMETER_WHEN_OVERRIDING_CONCRETE = PLUGIN_ID + ".compiler.problem.unusedParameterWhenOverridingConcrete"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_PB_UNUSED_IMPORT = PLUGIN_ID + ".compiler.problem.unusedImport"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPILER_PB_SYNTHETIC_ACCESS_EMULATION = PLUGIN_ID + ".compiler.problem.syntheticAccessEmulation"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_PB_NON_NLS_STRING_LITERAL = PLUGIN_ID + ".compiler.problem.nonExternalizedStringLiteral"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_PB_ASSERT_IDENTIFIER = PLUGIN_ID + ".compiler.problem.assertIdentifier"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_STATIC_ACCESS_RECEIVER = PLUGIN_ID + ".compiler.problem.staticAccessReceiver"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_INDIRECT_STATIC_ACCESS = PLUGIN_ID + ".compiler.problem.indirectStaticAccess"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_NO_EFFECT_ASSIGNMENT = PLUGIN_ID + ".compiler.problem.noEffectAssignment"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_INCOMPATIBLE_NON_INHERITED_INTERFACE_METHOD = PLUGIN_ID + ".compiler.problem.incompatibleNonInheritedInterfaceMethod"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_UNUSED_PRIVATE_MEMBER = PLUGIN_ID + ".compiler.problem.unusedPrivateMember"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_LOCAL_VARIABLE_HIDING = PLUGIN_ID + ".compiler.problem.localVariableHiding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_SPECIAL_PARAMETER_HIDING_FIELD = PLUGIN_ID + ".compiler.problem.specialParameterHidingField"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_FIELD_HIDING = PLUGIN_ID + ".compiler.problem.fieldHiding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_POSSIBLE_ACCIDENTAL_BOOLEAN_ASSIGNMENT = PLUGIN_ID + ".compiler.problem.possibleAccidentalBooleanAssignment"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_SUPERFLUOUS_SEMICOLON = PLUGIN_ID + ".compiler.problem.superfluousSemicolon"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_BOOLEAN_METHOD_THROWING_EXCEPTION = PLUGIN_ID + ".compiler.problem.booleanMethodThrowingException"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_UNNECESSARY_TYPE_CHECK = PLUGIN_ID + ".compiler.problem.unnecessaryTypeCheck"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_UNDOCUMENTED_EMPTY_BLOCK = PLUGIN_ID + ".compiler.problem.undocumentedEmptyBlock"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_FINALLY_BLOCK_NOT_COMPLETING = PLUGIN_ID + ".compiler.problem.finallyBlockNotCompletingNormally"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION = PLUGIN_ID + ".compiler.problem.unusedDeclaredThrownException"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_UNUSED_DECLARED_THROWN_EXCEPTION_WHEN_OVERRIDING = PLUGIN_ID + ".compiler.problem.unusedDeclaredThrownExceptionWhenOverriding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_UNQUALIFIED_FIELD_ACCESS = PLUGIN_ID + ".compiler.problem.unqualifiedFieldAccess"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_INVALID_JAVADOC = PLUGIN_ID + ".compiler.problem.invalidJavadoc"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_INVALID_JAVADOC_TAGS = PLUGIN_ID + ".compiler.problem.invalidJavadocTags"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_INVALID_JAVADOC_TAGS_VISIBILITY = PLUGIN_ID + ".compiler.problem.invalidJavadocTagsVisibility"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_TAGS = PLUGIN_ID + ".compiler.problem.missingJavadocTags"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_TAGS_VISIBILITY = PLUGIN_ID + ".compiler.problem.missingJavadocTagsVisibility"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_TAGS_OVERRIDING = PLUGIN_ID + ".compiler.problem.missingJavadocTagsOverriding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS = PLUGIN_ID + ".compiler.problem.missingJavadocComments"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS_VISIBILITY = PLUGIN_ID + ".compiler.problem.missingJavadocCommentsVisibility"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_PB_MISSING_JAVADOC_COMMENTS_OVERRIDING = PLUGIN_ID + ".compiler.problem.missingJavadocCommentsOverriding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_PB_CHAR_ARRAY_IN_STRING_CONCATENATION = PLUGIN_ID + ".compiler.problem.noImplicitStringConversion"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_PB_MAX_PER_UNIT = PLUGIN_ID + ".compiler.maxProblemPerUnit"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_SOURCE = PLUGIN_ID + ".compiler.source"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPILER_COMPLIANCE = PLUGIN_ID + ".compiler.compliance"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITIES = PLUGIN_ID + ".compiler.taskPriorities"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_HIGH = "HIGH"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_LOW = "LOW"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value for COMPILER_TASK_PRIORITIES.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_PRIORITY_NORMAL = "NORMAL"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String COMPILER_TASK_TAGS = PLUGIN_ID + ".compiler.taskTags"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String COMPILER_TASK_CASE_SENSITIVE = PLUGIN_ID + ".compiler.taskCaseSensitive"; //$NON-NLS-1$	
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 */
	public static final String CORE_JAVA_BUILD_ORDER = PLUGIN_ID + ".computeJavaBuildOrder"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_RESOURCE_COPY_FILTER = PLUGIN_ID + ".builder.resourceCopyExclusionFilter"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CORE_JAVA_BUILD_DUPLICATE_RESOURCE = PLUGIN_ID + ".builder.duplicateResourceTask"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER = PLUGIN_ID + ".builder.cleanOutputFolder"; //$NON-NLS-1$	 	
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CORE_INCOMPLETE_CLASSPATH = PLUGIN_ID + ".incompleteClasspath"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CORE_CIRCULAR_CLASSPATH = PLUGIN_ID + ".circularClasspath"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String CORE_INCOMPATIBLE_JDK_LEVEL = PLUGIN_ID + ".incompatibleJDKLevel"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CORE_JAVA_BUILD_INVALID_CLASSPATH = PLUGIN_ID + ".builder.invalidClasspath"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CORE_ENCODING = PLUGIN_ID + ".encoding"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1 
	 */
	public static final String CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS = PLUGIN_ID + ".classpath.exclusionPatterns"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS = PLUGIN_ID + ".classpath.multipleOutputLocations"; //$NON-NLS-1$
	/**
	 * Default task tag
	 * @deprecated - should use #DEFAULT_TASK_TAGS instead 
	 * @since 2.1
	 */
	public static final String DEFAULT_TASK_TAG = "TODO"; //$NON-NLS-1$
	/**
	 * Default task priority
	 * @deprecated - should use #DEFAULT_TASK_PRIORITIES instead 
	 * @since 2.1
	 */
	public static final String DEFAULT_TASK_PRIORITY = "NORMAL"; //$NON-NLS-1$
	/**
	 * Default task tag
	 * @since 3.0
	 */
	public static final String DEFAULT_TASK_TAGS = "TODO,FIXME,XXX"; //$NON-NLS-1$
	/**
	 * Default task priority
	 * @since 3.0
	 */
	public static final String DEFAULT_TASK_PRIORITIES = "NORMAL,HIGH,NORMAL"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_NEWLINE_OPENING_BRACE = PLUGIN_ID + ".formatter.newline.openingBrace"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_NEWLINE_CONTROL = PLUGIN_ID + ".formatter.newline.controlStatement"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_NEWLINE_ELSE_IF = PLUGIN_ID + ".formatter.newline.elseIf"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_NEWLINE_EMPTY_BLOCK = PLUGIN_ID + ".formatter.newline.emptyBlock"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_CLEAR_BLANK_LINES = PLUGIN_ID + ".formatter.newline.clearAll"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_LINE_SPLIT = PLUGIN_ID + ".formatter.lineSplit"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated
	 */
	public static final String FORMATTER_COMPACT_ASSIGNMENT = PLUGIN_ID + ".formatter.style.assignment"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated Use DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR instead
	 */
	public static final String FORMATTER_TAB_CHAR = PLUGIN_ID + ".formatter.tabulation.char"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 * @deprecated Use DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE instead
	 */
	public static final String FORMATTER_TAB_SIZE = PLUGIN_ID + ".formatter.tabulation.size"; //$NON-NLS-1$
	/**
	 * Possible configurable option ID
	 * @see #getDefaultOptions()
	 * @since 2.1
	 * @deprecated
	 */
	public static final String FORMATTER_SPACE_CASTEXPRESSION = PLUGIN_ID + ".formatter.space.castexpression"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CODEASSIST_VISIBILITY_CHECK = PLUGIN_ID + ".codeComplete.visibilityCheck"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CODEASSIST_IMPLICIT_QUALIFICATION = PLUGIN_ID + ".codeComplete.forceImplicitQualification"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.fieldPrefixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_STATIC_FIELD_PREFIXES = PLUGIN_ID + ".codeComplete.staticFieldPrefixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_LOCAL_PREFIXES = PLUGIN_ID + ".codeComplete.localPrefixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_ARGUMENT_PREFIXES = PLUGIN_ID + ".codeComplete.argumentPrefixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.fieldSuffixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_STATIC_FIELD_SUFFIXES = PLUGIN_ID + ".codeComplete.staticFieldSuffixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_LOCAL_SUFFIXES = PLUGIN_ID + ".codeComplete.localSuffixes"; //$NON-NLS-1$
	/**
	 * Possible  configurable option ID.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CODEASSIST_ARGUMENT_SUFFIXES = PLUGIN_ID + ".codeComplete.argumentSuffixes"; //$NON-NLS-1$

	// *************** Possible values for configurable options. ********************
	
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String GENERATE = "generate"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String DO_NOT_GENERATE = "do not generate"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String PRESERVE = "preserve"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String OPTIMIZE_OUT = "optimize out"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String VERSION_1_1 = "1.1"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String VERSION_1_2 = "1.2"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String VERSION_1_3 = "1.3"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String VERSION_1_4 = "1.4"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String VERSION_1_5 = "1.5"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String ABORT = "abort"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String ERROR = "error"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String WARNING = "warning"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String IGNORE = "ignore"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 */
	public static final String COMPUTE = "compute"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String INSERT = "insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String DO_NOT_INSERT = "do not insert"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String PRESERVE_ONE = "preserve one"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String CLEAR_ALL = "clear all"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String NORMAL = "normal"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String COMPACT = "compact"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String TAB = "tab"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String SPACE = "space"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String ENABLED = "enabled"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.0
	 */
	public static final String DISABLED = "disabled"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 2.1
	 */
	public static final String CLEAN = "clean"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String PUBLIC = "public"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String PROTECTED = "protected"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String DEFAULT = "default"; //$NON-NLS-1$
	/**
	 * Possible  configurable option value.
	 * @see #getDefaultOptions()
	 * @since 3.0
	 */
	public static final String PRIVATE = "private"; //$NON-NLS-1$
	
	/**
	 * Creates the Java core plug-in.
	 * @param pluginDescriptor
	 * @since 2.1
	 */
	public JavaCore(IPluginDescriptor pluginDescriptor) {
		super(pluginDescriptor);
		JAVA_CORE_PLUGIN = this;
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 *
	 * This listener will only be notified during the POST_CHANGE resource change notification
	 * and any reconcile operation (POST_RECONCILE).
	 * For finer control of the notification, use <code>addElementChangedListener(IElementChangedListener,int)</code>,
	 * which allows to specify a different eventMask.
	 * 
	 * @param listener the listener
	 * @see ElementChangedEvent
	 */
	public static void addElementChangedListener(IElementChangedListener listener) {
		addElementChangedListener(listener, ElementChangedEvent.POST_CHANGE | ElementChangedEvent.POST_RECONCILE);
	}

	/**
	 * Adds the given listener for changes to Java elements.
	 * Has no effect if an identical listener is already registered.
	 * After completion of this method, the given listener will be registered for exactly
	 * the specified events.  If they were previously registered for other events, they
	 * will be deregistered.  
	 * <p>
	 * Once registered, a listener starts receiving notification of changes to
	 * java elements in the model. The listener continues to receive 
	 * notifications until it is replaced or removed. 
	 * </p>
	 * <p>
	 * Listeners can listen for several types of event as defined in <code>ElementChangeEvent</code>.
	 * Clients are free to register for any number of event types however if they register
	 * for more than one, it is their responsibility to ensure they correctly handle the
	 * case where the same java element change shows up in multiple notifications.  
	 * Clients are guaranteed to receive only the events for which they are registered.
	 * </p>
	 * 
	 * @param listener the listener
	 * @param eventMask the bit-wise OR of all event types of interest to the listener
	 * @see IElementChangedListener
	 * @see ElementChangedEvent
	 * @see #removeElementChangedListener(IElementChangedListener)
	 * @since 2.0
	 */
	public static void addElementChangedListener(IElementChangedListener listener, int eventMask) {
		JavaModelManager.getJavaModelManager().deltaState.addElementChangedListener(listener, eventMask);
	}

	/**
	 * Configures the given marker attribute map for the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param attributes the mutable marker attribute map (key type: <code>String</code>,
	 *   value type: <code>String</code>)
	 * @param element the Java element for which the marker needs to be configured
	 */
	public static void addJavaElementMarkerAttributes(
		Map attributes,
		IJavaElement element) {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (attributes != null && element != null)
			attributes.put(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Configures the given marker for the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param marker the marker to be configured
	 * @param element the Java element for which the marker needs to be configured
	 * @exception CoreException if the <code>IMarker.setAttribute</code> on the marker fails
	 */
	public void configureJavaElementMarker(IMarker marker, IJavaElement element)
		throws CoreException {
		if (element instanceof IMember)
			element = ((IMember) element).getClassFile();
		if (marker != null && element != null)
			marker.setAttribute(ATT_HANDLE_ID, element.getHandleIdentifier());
	}
	
	/**
	 * Returns the Java model element corresponding to the given handle identifier
	 * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
	 * <code>null</code> if unable to create the associated element.
	 * 
	 * @param handleIdentifier the given handle identifier
	 * @return the Java element corresponding to the handle identifier
	 */
	public static IJavaElement create(String handleIdentifier) {
		return create(handleIdentifier, DefaultWorkingCopyOwner.PRIMARY);
	}

	/**
	 * Returns the Java model element corresponding to the given handle identifier
	 * generated by <code>IJavaElement.getHandleIdentifier()</code>, or
	 * <code>null</code> if unable to create the associated element.
	 * If the returned Java element is an <code>ICompilationUnit</code>, its owner
	 * is the given owner if such a working copy exists, otherwise the compilation unit 
	 * is a primary compilation unit.
	 * 
	 * @param handleIdentifier the given handle identifier
	 * @param owner the owner of the returned compilation unit, ignored if the returned
	 *   element is not a compilation unit
	 * @return the Java element corresponding to the handle identifier
	 * @since 3.0
	 */
	public static IJavaElement create(String handleIdentifier, WorkingCopyOwner owner) {
		if (handleIdentifier == null) {
			return null;
		}
		String delimiters = new String(new char[] {
			JavaElement.JEM_COUNT,
			JavaElement.JEM_JAVAPROJECT,
			JavaElement.JEM_PACKAGEFRAGMENTROOT,
			JavaElement.JEM_PACKAGEFRAGMENT,
			JavaElement.JEM_FIELD,
			JavaElement.JEM_METHOD,
			JavaElement.JEM_INITIALIZER,
			JavaElement.JEM_COMPILATIONUNIT,
			JavaElement.JEM_CLASSFILE,
			JavaElement.JEM_TYPE,
			JavaElement.JEM_PACKAGEDECLARATION,
			JavaElement.JEM_IMPORTDECLARATION,
			JavaElement.JEM_LOCALVARIABLE});
		StringTokenizer memento = new StringTokenizer(handleIdentifier, delimiters, true);
		JavaModel model = JavaModelManager.getJavaModelManager().getJavaModel();
		return model.getHandleFromMemento(memento, owner);
	}
	
	/**
	 * Returns the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
	 * with a Java element.
	 *
	 * <p>The file must be one of:<ul>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param file the given file
	 * @return the Java element corresponding to the given file, or
	 * <code>null</code> if unable to associate the given file
	 * with a Java element
	 */
	public static IJavaElement create(IFile file) {
		return JavaModelManager.create(file, null/*unknown java project*/);
	}
	/**
	 * Returns the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element.
	 * <p>
	 * Note that a package fragment root is returned rather than a default package.
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param folder the given folder
	 * @return the package fragment or package fragment root corresponding to the given folder, or
	 * <code>null</code> if unable to associate the given folder with a Java element
	 */
	public static IJavaElement create(IFolder folder) {
		return JavaModelManager.create(folder, null/*unknown java project*/);
	}
	/**
	 * Returns the Java project corresponding to the given project.
	 * <p>
	 * Creating a Java Project has the side effect of creating and opening all of the
	 * project's parents if they are not yet open.
	 * <p>
	 * Note that no check is done at this time on the existence or the java nature of this project.
	 * 
	 * @param project the given project
	 * @return the Java project corresponding to the given project, null if the given project is null
	 */
	public static IJavaProject create(IProject project) {
		if (project == null) {
			return null;
		}
		JavaModel javaModel = JavaModelManager.getJavaModelManager().getJavaModel();
		return javaModel.getJavaProject(project);
	}
	/**
	 * Returns the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element.
	 * <p>
	 * The resource must be one of:<ul>
	 *	<li>a project - the element returned is the corresponding <code>IJavaProject</code></li>
	 *	<li>a <code>.java</code> file - the element returned is the corresponding <code>ICompilationUnit</code></li>
	 *	<li>a <code>.class</code> file - the element returned is the corresponding <code>IClassFile</code></li>
	 *	<li>a <code>.jar</code> file - the element returned is the corresponding <code>IPackageFragmentRoot</code></li>
	 *  <li>a folder - the element returned is the corresponding <code>IPackageFragmentRoot</code>
	 *    	or <code>IPackageFragment</code></li>
	 *  <li>the workspace root resource - the element returned is the <code>IJavaModel</code></li>
	 *	</ul>
	 * <p>
	 * Creating a Java element has the side effect of creating and opening all of the
	 * element's parents if they are not yet open.
	 * 
	 * @param resource the given resource
	 * @return the Java element corresponding to the given resource, or
	 * <code>null</code> if unable to associate the given resource
	 * with a Java element
	 */
	public static IJavaElement create(IResource resource) {
		return JavaModelManager.create(resource, null/*unknown java project*/);
	}
	/**
	 * Returns the Java model.
	 * 
	 * @param root the given root
	 * @return the Java model, or <code>null</code> if the root is null
	 */
	public static IJavaModel create(IWorkspaceRoot root) {
		if (root == null) {
			return null;
		}
		return JavaModelManager.getJavaModelManager().getJavaModel();
	}
	/**
	 * Creates and returns a class file element for
	 * the given <code>.class</code> file. Returns <code>null</code> if unable
	 * to recognize the class file.
	 * 
	 * @param file the given <code>.class</code> file
	 * @return a class file element for the given <code>.class</code> file, or <code>null</code> if unable
	 * to recognize the class file
	 */
	public static IClassFile createClassFileFrom(IFile file) {
		return JavaModelManager.createClassFileFrom(file, null);
	}
	/**
	 * Creates and returns a compilation unit element for
	 * the given <code>.java</code> file. Returns <code>null</code> if unable
	 * to recognize the compilation unit.
	 * 
	 * @param file the given <code>.java</code> file
	 * @return a compilation unit element for the given <code>.java</code> file, or <code>null</code> if unable
	 * to recognize the compilation unit
	 */
	public static ICompilationUnit createCompilationUnitFrom(IFile file) {
		return JavaModelManager.createCompilationUnitFrom(file, null/*unknown java project*/);
	}
	/**
	 * Creates and returns a handle for the given JAR file.
	 * The Java model associated with the JAR's project may be
	 * created as a side effect. 
	 * 
	 * @param file the given JAR file
	 * @return a handle for the given JAR file, or <code>null</code> if unable to create a JAR package fragment root.
	 * (for example, if the JAR file represents a non-Java resource)
	 */
	public static IPackageFragmentRoot createJarPackageFragmentRootFrom(IFile file) {
		return JavaModelManager.createJarPackageFragmentRootFrom(file, null/*unknown java project*/);
	}

	/** 
	 * Answers the project specific value for a given classpath container.
	 * In case this container path could not be resolved, then will answer <code>null</code>.
	 * Both the container path and the project context are supposed to be non-null.
	 * <p>
	 * The containerPath is a formed by a first ID segment followed with extra segments, which can be 
	 * used as additional hints for resolution. If no container was ever recorded for this container path 
	 * onto this project (using <code>setClasspathContainer</code>, then a 
	 * <code>ClasspathContainerInitializer</code> will be activated if any was registered for this container 
	 * ID onto the extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that the returned container must answer the exact same containerPath
	 * when requested <code>IClasspathContainer#getPath</code>. 
	 * Indeed, the containerPath is just an indication for resolving it to an actual container object.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but 
	 * are not preserved from a session to another. It is thus highly recommended to register a 
	 * <code>ClasspathContainerInitializer</code> for each referenced container 
	 * (through the extension point "org.eclipse.jdt.core.ClasspathContainerInitializer").
	 * <p>
	 * @param containerPath the name of the container, which needs to be resolved
	 * @param project a specific project in which the container is being resolved
	 * @return the corresponding classpath container or <code>null</code> if unable to find one.
	 * 
	 * @exception JavaModelException if an exception occurred while resolving the container, or if the resolved container
	 *   contains illegal entries (contains CPE_CONTAINER entries or null entries).	 
	 * 
	 * @see ClasspathContainerInitializer
	 * @see IClasspathContainer
	 * @see #setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
	 * @since 2.0
	 */
	public static IClasspathContainer getClasspathContainer(final IPath containerPath, final IJavaProject project) throws JavaModelException {

		IClasspathContainer container = JavaModelManager.containerGet(project, containerPath);
		if (container == JavaModelManager.ContainerInitializationInProgress) return null; // break cycle

		if (container == null){
			final ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(containerPath.segment(0));
			if (initializer != null){
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPContainer INIT - triggering initialization of: ["+project.getElementName()+"] " + containerPath + " using initializer: "+ initializer); //$NON-NLS-1$ //$NON-NLS-2$//$NON-NLS-3$
					new Exception("FAKE exception for dumping current CPContainer (["+project.getElementName()+"] "+ containerPath+ ")INIT invocation stack trace").printStackTrace(); //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
				}
				JavaModelManager.containerPut(project, containerPath, JavaModelManager.ContainerInitializationInProgress); // avoid initialization cycles
				boolean ok = false;
				try {
					// wrap initializer call with Safe runnable in case initializer would be causing some grief
					Platform.run(new ISafeRunnable() {
						public void handleException(Throwable exception) {
							Util.log(exception, "Exception occurred in classpath container initializer: "+initializer); //$NON-NLS-1$
						}
						public void run() throws Exception {
							initializer.initialize(containerPath, project);
						}
					});
					
					// retrieve value (if initialization was successful)
					container = JavaModelManager.containerGet(project, containerPath);
					if (container == JavaModelManager.ContainerInitializationInProgress) return null; // break cycle
					ok = true;
				} finally {
					if (!ok) JavaModelManager.containerPut(project, containerPath, null); // flush cache
				}
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.print("CPContainer INIT - after resolution: ["+project.getElementName()+"] " + containerPath + " --> "); //$NON-NLS-2$//$NON-NLS-1$//$NON-NLS-3$
					if (container != null){
						System.out.print("container: "+container.getDescription()+" {"); //$NON-NLS-2$//$NON-NLS-1$
						IClasspathEntry[] entries = container.getClasspathEntries();
						if (entries != null){
							for (int i = 0; i < entries.length; i++){
								if (i > 0) System.out.println(", ");//$NON-NLS-1$
								System.out.println(entries[i]);
							}
						}
						System.out.println("}");//$NON-NLS-1$
					} else {
						System.out.println("{unbound}");//$NON-NLS-1$
					}
				}
			} else {
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPContainer INIT - no initializer found for: "+project.getElementName()+"] " + containerPath); //$NON-NLS-1$ //$NON-NLS-2$
				}
			}
		}
		return container;			
	}

	/**
	 * Helper method finding the classpath container initializer registered for a given classpath container ID 
	 * or <code>null</code> if none was found while iterating over the contributions to extension point to
	 * the extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A containerID is the first segment of any container path, used to identify the registered container initializer.
	 * <p>
	 * @param containerID - a containerID identifying a registered initializer
	 * @return ClasspathContainerInitializer - the registered classpath container initializer or <code>null</code> if 
	 * none was found.
	 * @since 2.1
	 */
	public static ClasspathContainerInitializer getClasspathContainerInitializer(String containerID){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPCONTAINER_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for(int j = 0; j < configElements.length; j++){
					String initializerID = configElements[j].getAttribute("id"); //$NON-NLS-1$
					if (initializerID != null && initializerID.equals(containerID)){
						if (JavaModelManager.CP_RESOLVE_VERBOSE) {
							System.out.println("CPContainer INIT - found initializer: "+containerID +" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
						}						
						try {
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof ClasspathContainerInitializer){
								return (ClasspathContainerInitializer)execExt;
							}
						} catch(CoreException e) {
							// executable extension could not be created: ignore this initializer
							if (JavaModelManager.CP_RESOLVE_VERBOSE) {
								System.out.println("CPContainer INIT - failed to instanciate initializer: "+containerID +" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
								e.printStackTrace();
							}						
						}
					}
				}
			}	
		}
		return null;
	}

	/**
	 * Returns the path held in the given classpath variable.
	 * Returns <node>null</code> if unable to bind.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Note that classpath variables can be contributed registered initializers for,
	 * using the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * If an initializer is registered for a variable, its persisted value will be ignored:
	 * its initializer will thus get the opportunity to rebind the variable differently on
	 * each session.
	 *
	 * @param variableName the name of the classpath variable
	 * @return the path, or <code>null</code> if none 
	 * @see #setClasspathVariable(String, IPath)
	 */
	public static IPath getClasspathVariable(final String variableName) {
	
		IPath variablePath = JavaModelManager.variableGet(variableName);
		if (variablePath == JavaModelManager.VariableInitializationInProgress) return null; // break cycle
		
		if (variablePath != null) {
			return variablePath;
		}

		// even if persisted value exists, initializer is given priority, only if no initializer is found the persisted value is reused
		final ClasspathVariableInitializer initializer = JavaCore.getClasspathVariableInitializer(variableName);
		if (initializer != null){
			if (JavaModelManager.CP_RESOLVE_VERBOSE){
				System.out.println("CPVariable INIT - triggering initialization of: " + variableName+ " using initializer: "+ initializer); //$NON-NLS-1$ //$NON-NLS-2$
				new Exception("FAKE exception for dumping current CPVariable ("+variableName+ ")INIT invocation stack trace").printStackTrace(); //$NON-NLS-1$//$NON-NLS-2$
			}
			JavaModelManager.variablePut(variableName, JavaModelManager.VariableInitializationInProgress); // avoid initialization cycles
			boolean ok = false;
			try {
				// wrap initializer call with Safe runnable in case initializer would be causing some grief
				Platform.run(new ISafeRunnable() {
					public void handleException(Throwable exception) {
						Util.log(exception, "Exception occurred in classpath variable initializer: "+initializer+" while initializing variable: "+variableName); //$NON-NLS-1$ //$NON-NLS-2$
					}
					public void run() throws Exception {
						initializer.initialize(variableName);
					}
				});
				variablePath = JavaModelManager.variableGet(variableName); // initializer should have performed side-effect
				if (variablePath == JavaModelManager.VariableInitializationInProgress) return null; // break cycle (initializer did not init or reentering call)
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPVariable INIT - after initialization: " + variableName + " --> " + variablePath); //$NON-NLS-2$//$NON-NLS-1$
				}
				ok = true;
			} finally {
				if (!ok) JavaModelManager.variablePut(variableName, null); // flush cache
			}
		} else {
			if (JavaModelManager.CP_RESOLVE_VERBOSE){
				System.out.println("CPVariable INIT - no initializer found for: " + variableName); //$NON-NLS-1$
			}
		}
		return variablePath;
	}

	/**
	 * Helper method finding the classpath variable initializer registered for a given classpath variable name 
	 * or <code>null</code> if none was found while iterating over the contributions to extension point to
	 * the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * <p>
 	 * @param variable the given variable
 	 * @return ClasspathVariableInitializer - the registered classpath variable initializer or <code>null</code> if 
	 * none was found.
	 * @since 2.1
 	 */
	public static ClasspathVariableInitializer getClasspathVariableInitializer(String variable){
		
		Plugin jdtCorePlugin = JavaCore.getPlugin();
		if (jdtCorePlugin == null) return null;

		IExtensionPoint extension = jdtCorePlugin.getDescriptor().getExtensionPoint(JavaModelManager.CPVARIABLE_INITIALIZER_EXTPOINT_ID);
		if (extension != null) {
			IExtension[] extensions =  extension.getExtensions();
			for(int i = 0; i < extensions.length; i++){
				IConfigurationElement [] configElements = extensions[i].getConfigurationElements();
				for(int j = 0; j < configElements.length; j++){
					try {
						String varAttribute = configElements[j].getAttribute("variable"); //$NON-NLS-1$
						if (variable.equals(varAttribute)) {
							if (JavaModelManager.CP_RESOLVE_VERBOSE) {
								System.out.println("CPVariable INIT - found initializer: "+variable+" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
							}						
							Object execExt = configElements[j].createExecutableExtension("class"); //$NON-NLS-1$
							if (execExt instanceof ClasspathVariableInitializer){
								return (ClasspathVariableInitializer)execExt;
							}
						}
					} catch(CoreException e){
						// executable extension could not be created: ignore this initializer
						if (JavaModelManager.CP_RESOLVE_VERBOSE) {
							System.out.println("CPContainer INIT - failed to instanciate initializer: "+variable +" --> " + configElements[j].getAttribute("class"));//$NON-NLS-3$//$NON-NLS-2$//$NON-NLS-1$
							e.printStackTrace();
						}						
					}
				}
			}	
		}
		return null;
	}	
	
	/**
	 * Returns the names of all known classpath variables.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @return the list of classpath variable names
	 * @see #setClasspathVariable(String, IPath)
	 */
	public static String[] getClasspathVariableNames() {
		return JavaModelManager.variableNames();
	}

	/**
	 * Returns a table of all known configurable options with their default values.
	 * These options allow to configure the behaviour of the underlying components.
	 * The client may safely use the result as a template that they can modify and
	 * then pass to <code>setOptions</code>.
	 * 
	 * Helper constants have been defined on JavaCore for each of the option ID and 
	 * their possible constant values.
	 * 
	 * Note: more options might be added in further releases.
	 * <pre>
	 * RECOGNIZED OPTIONS:
	 * COMPILER / Generating Local Variable Debug Attribute
 	 *    When generated, this attribute will enable local variable names 
	 *    to be displayed in debugger, only in place where variables are 
	 *    definitely assigned (.class file is then bigger)
	 *     - option id:         "org.eclipse.jdt.core.compiler.debug.localVariable"
	 *     - possible values:   { "generate", "do not generate" }
	 *     - default:           "generate"
	 *
	 * COMPILER / Generating Line Number Debug Attribute 
	 *    When generated, this attribute will enable source code highlighting in debugger 
	 *    (.class file is then bigger).
	 *     - option id:         "org.eclipse.jdt.core.compiler.debug.lineNumber"
	 *     - possible values:   { "generate", "do not generate" }
	 *     - default:           "generate"
	 *    
	 * COMPILER / Generating Source Debug Attribute 
	 *    When generated, this attribute will enable the debugger to present the 
	 *    corresponding source code.
	 *     - option id:         "org.eclipse.jdt.core.compiler.debug.sourceFile"
	 *     - possible values:   { "generate", "do not generate" }
	 *     - default:           "generate"
	 *    
	 * COMPILER / Preserving Unused Local Variables
	 *    Unless requested to preserve unused local variables (that is, never read), the 
	 *    compiler will optimize them out, potentially altering debugging
	 *     - option id:         "org.eclipse.jdt.core.compiler.codegen.unusedLocal"
	 *     - possible values:   { "preserve", "optimize out" }
	 *     - default:           "preserve"
	 * 
	 * COMPILER / Defining Target Java Platform
	 *    For binary compatibility reason, .class files can be tagged to with certain VM versions and later.
	 *    Note that "1.4" target require to toggle compliance mode to "1.4" too.
	 *     - option id:         "org.eclipse.jdt.core.compiler.codegen.targetPlatform"
	 *     - possible values:   { "1.1", "1.2", "1.3", "1.4" }
	 *     - default:           "1.2"
	 *
	 * COMPILER / Inline JSR Bytecode Instruction
	 *    When enabled, the compiler will no longer generate JSR instructions, but rather inline corresponding
	 *   subroutine code sequences (mostly corresponding to try finally blocks). The generated code will thus
	 *   get bigger, but will load faster on virtual machines since the verification process is then much simpler. 
	 *  This mode is anticipating support for the Java Specification Request 202.
	 *     - option id:         "org.eclipse.jdt.core.compiler.codegen.inlineJsrBytecode"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 * 
	 * COMPILER / Javadoc Comment Support
	 *    When this support is disabled, the compiler will ignore all javadoc problems options settings
	 *    and will not report any javadoc problem. It will also not find any reference in javadoc comment and
	 *    DOM AST Javadoc node will be only a flat text instead of having structured tag elements.
	 *     - option id:         "org.eclipse.jdt.core.compiler.doc.comment.support"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 *
	 * COMPILER / Reporting Attempt to Override Package-Default Method
	 *    A package default method is not visible in a different package, and thus 
	 *    cannot be overridden. When enabling this option, the compiler will signal 
	 *    such scenarii either as an error or a warning.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.overridingPackageDefaultMethod"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Method With Constructor Name
	 *    Naming a method with a constructor name is generally considered poor 
	 *    style programming. When enabling this option, the compiler will signal such 
	 *    scenarii either as an error or a warning.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.methodWithConstructorName"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Deprecation
	 *    When enabled, the compiler will signal use of deprecated API either as an 
	 *    error or a warning.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.deprecation"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Deprecation Inside Deprecated Code
	 *    When enabled, the compiler will signal use of deprecated API inside deprecated code.
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.deprecation".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.deprecationInDeprecatedCode"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 *
	 * COMPILER / Reporting Deprecation When Overriding Deprecated Method
	 *    When enabled, the compiler will signal the declaration of a method overriding a deprecated one.
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.deprecation".
	 *     - option id:        "org.eclipse.jdt.core.compiler.problem.deprecationWhenOverridingDeprecatedMethod"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 *
	 * COMPILER / Reporting Hidden Catch Block
	 *    Locally to a try statement, some catch blocks may hide others . For example,
	 *      try {  throw new java.io.CharConversionException();
	 *      } catch (java.io.CharConversionException e) {
	 *      } catch (java.io.IOException e) {}. 
	 *    When enabling this option, the compiler will issue an error or a warning for hidden 
	 *    catch blocks corresponding to checked exceptions
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.hiddenCatchBlock"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Unused Local
	 *    When enabled, the compiler will issue an error or a warning for unused local 
	 *    variables (that is, variables never read from)
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedLocal"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Unused Parameter
	 *    When enabled, the compiler will issue an error or a warning for unused method 
	 *    parameters (that is, parameters never read from)
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedParameter"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Unused Parameter if Implementing Abstract Method
	 *    When enabled, the compiler will signal unused parameters in abstract method implementations.
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.unusedParameter".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedParameterWhenImplementingAbstract"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 *
	 * COMPILER / Reporting Unused Parameter if Overriding Concrete Method
	 *    When enabled, the compiler will signal unused parameters in methods overriding concrete ones.
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.unusedParameter".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedParameterWhenOverridingConcrete"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 *
	 * COMPILER / Reporting Unused Import
	 *    When enabled, the compiler will issue an error or a warning for unused import 
	 *    reference 
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedImport"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Unused Private Members
	 *    When enabled, the compiler will issue an error or a warning whenever a private 
	 *    method or field is declared but never used within the same unit.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedPrivateMember"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Assignment with no Effect
	 *    When enabled, the compiler will issue an error or a warning whenever an assignment
	 *    has no effect (e.g 'x = x').
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.noEffectAssignment"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 * 
	 * COMPILER / Reporting Superfluous Semicolon
	 *    When enabled, the compiler will issue an error or a warning if a superfluous semicolon is met.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.superfluousSemicolon"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Unnecessary Type Check
	 *    When enabled, the compiler will issue an error or a warning when a cast or an instanceof operation 
	 *    is unnecessary.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unnecessaryTypeCheck"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Synthetic Access Emulation
	 *    When enabled, the compiler will issue an error or a warning whenever it emulates 
	 *    access to a non-accessible member of an enclosing type. Such access can have
	 *    performance implications.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.syntheticAccessEmulation"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Non-Externalized String Literal
	 *    When enabled, the compiler will issue an error or a warning for non externalized 
	 *    String literal (that is, not tagged with //$NON-NLS-&lt;n&gt;$). 
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.nonExternalizedStringLiteral"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Usage of 'assert' Identifier
	 *    When enabled, the compiler will issue an error or a warning whenever 'assert' is 
	 *    used as an identifier (reserved keyword in 1.4)
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.assertIdentifier"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Non-Static Reference to a Static Member
	 *    When enabled, the compiler will issue an error or a warning whenever a static field
	 *    or method is accessed with an expression receiver. A reference to a static member should
	 *    be qualified with a type name.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.staticAccessReceiver"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 * 
	 * COMPILER / Reporting Indirect Reference to a Static Member
	 *    When enabled, the compiler will issue an error or a warning whenever a static field
	 *    or method is accessed in an indirect way. A reference to a static member should
	 *    preferably be qualified with its declaring type name.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.indirectStaticAccess"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Interface Method not Compatible with non-Inherited Methods
	 *    When enabled, the compiler will issue an error or a warning whenever an interface
	 *    defines a method incompatible with a non-inherited Object method. Until this conflict
	 *    is resolved, such an interface cannot be implemented, For example, 
	 *      interface I { 
	 *         int clone();
	 *      } 
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.incompatibleNonInheritedInterfaceMethod"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 * 
	 * COMPILER / Reporting Usage of char[] Expressions in String Concatenations
	 *    When enabled, the compiler will issue an error or a warning whenever a char[] expression
	 *    is used in String concatenations (for example, "hello" + new char[]{'w','o','r','l','d'}).
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.noImplicitStringConversion"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Local Variable Declaration Hiding another Variable
	 *    When enabled, the compiler will issue an error or a warning whenever a local variable
	 *    declaration is hiding some field or local variable (either locally, inherited or defined in enclosing type).
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.localVariableHiding"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Field Declaration Hiding another Variable
	 *    When enabled, the compiler will issue an error or a warning whenever a field
	 *    declaration is hiding some field or local variable (either locally, inherited or defined in enclosing type).
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.fieldHiding"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Special Parameter Hiding another Field
	 *    When enabled, the compiler will signal cases where a constructor or setter method parameter declaration 
	 *    is hiding some field (either locally, inherited or defined in enclosing type).
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.localVariableHiding".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.specialParameterHidingField"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 * 
	 * COMPILER / Reporting Possible Accidental Boolean Assignment
	 *    When enabled, the compiler will issue an error or a warning if a boolean assignment is acting as the condition
	 *    of a control statement  (where it probably was meant to be a boolean comparison).
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.possibleAccidentalBooleanAssignment"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Reporting Undocumented Empty Block
	 *    When enabled, the compiler will issue an error or a warning when an empty block is detected and it is not
	 *    documented with any comment.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.undocumentedEmptyBlock"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Finally Blocks Not Completing Normally
	 *    When enabled, the compiler will issue an error or a warning when a finally block does not complete normally.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.finallyBlockNotCompletingNormally"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "warning"
	 *
	 * COMPILER / Reporting Unused Declared Thrown Exception
	 *    When enabled, the compiler will issue an error or a warning when a method or a constructor is declaring a
	 *    thrown checked exception, but never actually raises it in its body.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Unused Declared Thrown Exception in Overridind Method
	 *    When disabled, the compiler will not include overriding methods in its diagnosis for unused declared
	 *    thrown exceptions.
	 *    <br>
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownException".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unusedDeclaredThrownExceptionWhenOverriding"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 * 
	 * COMPILER / Reporting Unqualified Access to Field
	 *    When enabled, the compiler will issue an error or a warning when a field is access without any qualification.
	 *    In order to improve code readability, it should be qualified, e.g. 'x' should rather be written 'this.x'.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.unqualifiedFieldAccess"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Reporting Invalid Javadoc Comment
	 *    This is the generic control for the severity of Javadoc problems.
	 *    When enabled, the compiler will issue an error or a warning for a problem in Javadoc.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.invalidJavadoc"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 *
	 * COMPILER / Visibility Level For Invalid Javadoc Tags
	 *    Set the minimum visibility level for Javadoc tag problems. Below this level problems will be ignored.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility"
	 *     - possible values:   { "public", "protected", "default", "private" }
	 *     - default:           "private"
	 * 
	 * COMPILER / Reporting Invalid Javadoc Tags
	 *    When enabled, the compiler will signal unbound or unexpected reference tags in Javadoc.
	 *    A 'throws' tag referencing an undeclared exception would be considered as unexpected.
	 *    <br>Note that this diagnosis can be enabled based on the visibility of the construct associated with the Javadoc;
	 *    also see the setting "org.eclipse.jdt.core.compiler.problem.invalidJavadocTagsVisibility".
	 *    <br>
	 *    The severity of the problem is controlled with option "org.eclipse.jdt.core.compiler.problem.invalidJavadoc".
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.invalidJavadocTags"
	 *     - possible values:   { "disabled", "enabled" }
	 *     - default:           "enabled"
	 * 
	 * COMPILER / Reporting Missing Javadoc Tags
	 *    This is the generic control for the severity of Javadoc missing tag problems.
	 *    When enabled, the compiler will issue an error or a warning when tags are missing in Javadoc comments.
	 *    <br>Note that this diagnosis can be enabled based on the visibility of the construct associated with the Javadoc;
	 *    also see the setting "org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility".
	 *    <br>
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocTags"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Visibility Level For Missing Javadoc Tags
	 *    Set the minimum visibility level for Javadoc missing tag problems. Below this level problems will be ignored.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocTagsVisibility"
	 *     - possible values:   { "public", "protected", "default", "private" }
	 *     - default:           "private"
	 * 
	 * COMPILER / Reporting Missing Javadoc Tags on Overriding Methods
	 *    Specify whether the compiler will verify overriding methods in order to report Javadoc missing tag problems.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocTagsOverriding"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 * 
	 * COMPILER / Reporting Missing Javadoc Comments
	 *    This is the generic control for the severity of missing Javadoc comment problems.
	 *    When enabled, the compiler will issue an error or a warning when Javadoc comments are missing.
	 *    <br>Note that this diagnosis can be enabled based on the visibility of the construct associated with the expected Javadoc;
	 *    also see the setting "org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility".
	 *    <br>
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocComments"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * COMPILER / Visibility Level For Missing Javadoc Comments
	 *    Set the minimum visibility level for missing Javadoc problems. Below this level problems will be ignored.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsVisibility"
	 *     - possible values:   { "public", "protected", "default", "private" }
	 *     - default:           "public"
	 * 
	 * COMPILER / Reporting Missing Javadoc Comments on Overriding Methods
	 *    Specify whether the compiler will verify overriding methods in order to report missing Javadoc comment problems.
	 *     - option id:         "org.eclipse.jdt.core.compiler.problem.missingJavadocCommentsOverriding"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 * 
	 * COMPILER / Setting Source Compatibility Mode
	 *    Specify whether which source level compatibility is used. From 1.4 on, 'assert' is a keyword
	 *    reserved for assertion support. Also note, than when toggling to 1.4 mode, the target VM
	 *   level should be set to "1.4" and the compliance mode should be "1.4".
	 *     - option id:         "org.eclipse.jdt.core.compiler.source"
	 *     - possible values:   { "1.3", "1.4" }
	 *     - default:           "1.3"
	 * 
	 * COMPILER / Setting Compliance Level
	 *    Select the compliance level for the compiler. In "1.3" mode, source and target settings
	 *    should not go beyond "1.3" level.
	 *     - option id:         "org.eclipse.jdt.core.compiler.compliance"
	 *     - possible values:   { "1.3", "1.4" }
	 *     - default:           "1.4"
	 * 
	 * COMPILER / Maximum number of problems reported per compilation unit
	 *    Specify the maximum number of problems reported on each compilation unit.
	 *     - option id:         "org.eclipse.jdt.core.compiler.maxProblemPerUnit"
	 *     - possible values:	"&lt;n&gt;" where &lt;n&gt; is zero or a positive integer (if zero then all problems are reported).
	 *     - default:           "100"
	 * 
	 * COMPILER / Define the Automatic Task Tags
	 *    When the tag list is not empty, the compiler will issue a task marker whenever it encounters
	 *    one of the corresponding tag inside any comment in Java source code.
	 *    Generated task messages will include the tag, and range until the next line separator or comment ending.
	 *    Note that tasks messages are trimmed. If a tag is starting with a letter or digit, then it cannot be leaded by
	 *    another letter or digit to be recognized ("fooToDo" will not be recognized as a task for tag "ToDo", but "foo#ToDo"
	 *    will be detected for either tag "ToDo" or "#ToDo"). Respectively, a tag ending with a letter or digit cannot be followed
	 *    by a letter or digit to be recognized ("ToDofoo" will not be recognized as a task for tag "ToDo", but "ToDo:foo" will
	 *    be detected either for tag "ToDo" or "ToDo:").
	 *     - option id:         "org.eclipse.jdt.core.compiler.taskTags"
	 *     - possible values:   { "&lt;tag&gt;[,&lt;tag&gt;]*" } where &lt;tag&gt; is a String without any wild-card or leading/trailing spaces 
	 *     - default:           "TODO,FIXME,XXX"
	 * 
	 * COMPILER / Define the Automatic Task Priorities
	 *    In parallel with the Automatic Task Tags, this list defines the priorities (high, normal or low)
	 *    of the task markers issued by the compiler.
	 *    If the default is specified, the priority of each task marker is "NORMAL".
	 *     - option id:         "org.eclipse.jdt.core.compiler.taskPriorities"
	 *     - possible values:   { "&lt;priority&gt;[,&lt;priority&gt;]*" } where &lt;priority&gt; is one of "HIGH", "NORMAL" or "LOW"
	 *     - default:           "NORMAL,HIGH,NORMAL"
	 * 
	 * COMPILER / Determine whether task tags are case-sensitive
	 *    When enabled, task tags are considered in a case-sensitive way.
	 *     - option id:         "org.eclipse.jdt.core.compiler.taskCaseSensitive"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 *
	 * BUILDER / Specifying Filters for Resource Copying Control
	 *    Allow to specify some filters to control the resource copy process.
	 *     - option id:         "org.eclipse.jdt.core.builder.resourceCopyExclusionFilter"
	 *     - possible values:   { "&lt;name&gt;[,&lt;name&gt;]* } where &lt;name&gt; is a file name pattern (* and ? wild-cards allowed)
	 *       or the name of a folder which ends with '/'
	 *     - default:           ""
	 * 
	 * BUILDER / Abort if Invalid Classpath
	 *    Allow to toggle the builder to abort if the classpath is invalid
	 *     - option id:         "org.eclipse.jdt.core.builder.invalidClasspath"
	 *     - possible values:   { "abort", "ignore" }
	 *     - default:           "abort"
	 * 
	 * BUILDER / Cleaning Output Folder(s)
	 *    Indicate whether the JavaBuilder is allowed to clean the output folders
	 *    when performing full build operations.
	 *     - option id:         "org.eclipse.jdt.core.builder.cleanOutputFolder"
	 *     - possible values:   { "clean", "ignore" }
	 *     - default:           "clean"
	 * 
	 * BUILDER / Reporting Duplicate Resources
	 *    Indicate the severity of the problem reported when more than one occurrence
	 *    of a resource is to be copied into the output location.
	 *     - option id:         "org.eclipse.jdt.core.builder.duplicateResourceTask"
	 *     - possible values:   { "error", "warning" }
	 *     - default:           "warning"
	 * 
	 * JAVACORE / Computing Project Build Order
	 *    Indicate whether JavaCore should enforce the project build order to be based on
	 *    the classpath prerequisite chain. When requesting to compute, this takes over
	 *    the platform default order (based on project references).
	 *     - option id:         "org.eclipse.jdt.core.computeJavaBuildOrder"
	 *     - possible values:   { "compute", "ignore" }
	 *     - default:           "ignore"	 
	 * 
	 * JAVACORE / Specify Default Source Encoding Format
	 *    Get the encoding format for compiled sources. This setting is read-only, it is equivalent
	 *    to 'ResourcesPlugin.getEncoding()'.
	 *     - option id:         "org.eclipse.jdt.core.encoding"
	 *     - possible values:   { any of the supported encoding name}.
	 *     - default:           &lt;platform default&gt;
	 * 
	 * JAVACORE / Reporting Incomplete Classpath
	 *    Indicate the severity of the problem reported when an entry on the classpath does not exist, 
	 *    is not legite or is not visible (for example, a referenced project is closed).
	 *     - option id:         "org.eclipse.jdt.core.incompleteClasspath"
	 *     - possible values:   { "error", "warning"}
	 *     - default:           "error"
	 * 
	 * JAVACORE / Reporting Classpath Cycle
	 *    Indicate the severity of the problem reported when a project is involved in a cycle.
	 *     - option id:         "org.eclipse.jdt.core.circularClasspath"
	 *     - possible values:   { "error", "warning" }
	 *     - default:           "error"
	 * 
	 * JAVACORE / Reporting Incompatible JDK Level for Required Binaries
	 *    Indicate the severity of the problem reported when a project prerequisites another project 
	 *    or library with an incompatible target JDK level (e.g. project targeting 1.1 vm, but compiled against 1.4 libraries).
	 *     - option id:         "org.eclipse.jdt.core.incompatibleJDKLevel"
	 *     - possible values:   { "error", "warning", "ignore" }
	 *     - default:           "ignore"
	 * 
	 * JAVACORE / Enabling Usage of Classpath Exclusion Patterns
	 *    When disabled, no entry on a project classpath can be associated with
	 *    an exclusion pattern.
	 *     - option id:         "org.eclipse.jdt.core.classpath.exclusionPatterns"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 * 
	 * JAVACORE / Enabling Usage of Classpath Multiple Output Locations
	 *    When disabled, no entry on a project classpath can be associated with
	 *    a specific output location, preventing thus usage of multiple output locations.
	 *     - option id:         "org.eclipse.jdt.core.classpath.multipleOutputLocations"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "enabled"
	 * 
	 *	FORMATTER / Inserting New Line Before Opening Brace
	 *    When Insert, a new line is inserted before an opening brace, otherwise nothing
	 *    is inserted
	 *     - option id:         "org.eclipse.jdt.core.formatter.newline.openingBrace"
	 *     - possible values:   { "insert", "do not insert" }
	 *     - default:           "do not insert"
	 * 
	 *	FORMATTER / Inserting New Line Inside Control Statement
	 *    When Insert, a new line is inserted between } and following else, catch, finally
	 *     - option id:         "org.eclipse.jdt.core.formatter.newline.controlStatement"
	 *     - possible values:   { "insert", "do not insert" }
	 *     - default:           "do not insert"
	 * 
	 *	FORMATTER / Clearing Blank Lines
	 *    When Clear all, all blank lines are removed. When Preserve one, only one is kept
	 *    and all others removed.
	 *     - option id:         "org.eclipse.jdt.core.formatter.newline.clearAll"
	 *     - possible values:   { "clear all", "preserve one" }
	 *     - default:           "preserve one"
	 * 
	 *	FORMATTER / Inserting New Line Between Else/If 
	 *    When Insert, a blank line is inserted between an else and an if when they are 
	 *    contiguous. When choosing to not insert, else-if will be kept on the same
	 *    line when possible.
	 *     - option id:         "org.eclipse.jdt.core.formatter.newline.elseIf"
	 *     - possible values:   { "insert", "do not insert" }
	 *     - default:           "do not insert"
	 * 
	 *	FORMATTER / Inserting New Line In Empty Block
	 *    When insert, a line break is inserted between contiguous { and }, if } is not followed
	 *    by a keyword.
	 *     - option id:         "org.eclipse.jdt.core.formatter.newline.emptyBlock"
	 *     - possible values:   { "insert", "do not insert" }
	 *     - default:           "insert"
	 * 
	 *	FORMATTER / Splitting Lines Exceeding Length
	 *    Enable splitting of long lines (exceeding the configurable length). Length of 0 will
	 *    disable line splitting
	 *     - option id:         "org.eclipse.jdt.core.formatter.lineSplit"
	 *     - possible values:	"&lt;n&gt;", where n is zero or a positive integer
	 *     - default:           "80"
	 * 
	 *	FORMATTER / Compacting Assignment
	 *    Assignments can be formatted asymmetrically, for example 'int x= 2;', when Normal, a space
	 *    is inserted before the assignment operator
	 *     - option id:         "org.eclipse.jdt.core.formatter.style.assignment"
	 *     - possible values:   { "compact", "normal" }
	 *     - default:           "normal"
	 * 
	 *	FORMATTER / Defining Indentation Character
	 *    Either choose to indent with tab characters or spaces
	 *     - option id:         "org.eclipse.jdt.core.formatter.tabulation.char"
	 *     - possible values:   { "tab", "space" }
	 *     - default:           "tab"
	 * 
	 *	FORMATTER / Defining Space Indentation Length
	 *    When using spaces, set the amount of space characters to use for each 
	 *    indentation mark.
	 *     - option id:         "org.eclipse.jdt.core.formatter.tabulation.size"
	 *     - possible values:	"&lt;n&gt;", where n is a positive integer
	 *     - default:           "4"
	 * 
	 *	FORMATTER / Inserting space in cast expression
	 *    When Insert, a space is added between the type and the expression in a cast expression.
	 *     - option id:         "org.eclipse.jdt.core.formatter.space.castexpression"
	 *     - possible values:   { "insert", "do not insert" }
	 *     - default:           "insert"
	 * 
	 *	CODEASSIST / Activate Visibility Sensitive Completion
	 *    When active, completion doesn't show that you can not see
	 *    (for example, you can not see private methods of a super class).
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.visibilityCheck"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 * 
	 *	CODEASSIST / Automatic Qualification of Implicit Members
	 *    When active, completion automatically qualifies completion on implicit
	 *    field references and message expressions.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.forceImplicitQualification"
	 *     - possible values:   { "enabled", "disabled" }
	 *     - default:           "disabled"
	 * 
	 *  CODEASSIST / Define the Prefixes for Field Name
	 *    When the prefixes is non empty, completion for field name will begin with
	 *    one of the proposed prefixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.fieldPrefixes"
	 *     - possible values:   { "&lt;prefix&gt;[,&lt;prefix&gt;]*" } where &lt;prefix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Prefixes for Static Field Name
	 *    When the prefixes is non empty, completion for static field name will begin with
	 *    one of the proposed prefixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.staticFieldPrefixes"
	 *     - possible values:   { "&lt;prefix&gt;[,&lt;prefix&gt;]*" } where &lt;prefix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Prefixes for Local Variable Name
	 *    When the prefixes is non empty, completion for local variable name will begin with
	 *    one of the proposed prefixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.localPrefixes"
	 *     - possible values:   { "&lt;prefix&gt;[,&lt;prefix&gt;]*" } where &lt;prefix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Prefixes for Argument Name
	 *    When the prefixes is non empty, completion for argument name will begin with
	 *    one of the proposed prefixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.argumentPrefixes"
	 *     - possible values:   { "&lt;prefix&gt;[,&lt;prefix&gt;]*" } where &lt;prefix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Suffixes for Field Name
	 *    When the suffixes is non empty, completion for field name will end with
	 *    one of the proposed suffixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.fieldSuffixes"
	 *     - possible values:   { "&lt;suffix&gt;[,&lt;suffix&gt;]*" } where &lt;suffix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Suffixes for Static Field Name
	 *    When the suffixes is non empty, completion for static field name will end with
	 *    one of the proposed suffixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.staticFieldSuffixes"
	 *     - possible values:   { "&lt;suffix&gt;[,&lt;suffix&gt;]*" } where &lt;suffix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Suffixes for Local Variable Name
	 *    When the suffixes is non empty, completion for local variable name will end with
	 *    one of the proposed suffixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.localSuffixes"
	 *     - possible values:   { "&lt;suffix&gt;[,&lt;suffix&gt;]*" } where &lt;suffix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * 
	 *  CODEASSIST / Define the Suffixes for Argument Name
	 *    When the suffixes is non empty, completion for argument name will end with
	 *    one of the proposed suffixes.
	 *     - option id:         "org.eclipse.jdt.core.codeComplete.argumentSuffixes"
	 *     - possible values:   { "&lt;suffix&gt;[,&lt;suffix&gt;]*" } where &lt;suffix&gt; is a String without any wild-card 
	 *     - default:           ""
	 * </pre>
	 * 
	 * @return a mutable table containing the default settings of all known options
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see #setOptions(Hashtable)
	 */
 	public static Hashtable getDefaultOptions(){
	
		Hashtable defaultOptions = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default settings
		Preferences preferences = getPlugin().getPluginPreferences();
		HashSet optionNames = JavaModelManager.OptionNames;
		
		// initialize preferences to their default
		Iterator iterator = optionNames.iterator();
		while (iterator.hasNext()) {
		    String propertyName = (String) iterator.next();
		    defaultOptions.put(propertyName, preferences.getDefaultString(propertyName));
		}
		// get encoding through resource plugin
		defaultOptions.put(CORE_ENCODING, ResourcesPlugin.getEncoding()); 
		// backward compatibility
		defaultOptions.put(COMPILER_PB_INVALID_IMPORT, ERROR);		
		defaultOptions.put(COMPILER_PB_UNREACHABLE_CODE, ERROR);
		
		return defaultOptions;
	}

	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * Equivalent to <code>(JavaCore) getPlugin()</code>.
	 * 
	 * @return the single instance of the Java core plug-in runtime class
	 */
	public static JavaCore getJavaCore() {
		return (JavaCore) getPlugin();
	}
	
	/**
	 * Helper method for returning one option value only. Equivalent to <code>(String)JavaCore.getOptions().get(optionName)</code>
	 * Note that it may answer <code>null</code> if this option does not exist.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param optionName the name of an option
	 * @return the String value of a given option
	 * @see JavaCore#getDefaultOptions()
	 * @since 2.0
	 */
	public static String getOption(String optionName) {
		
		if (CORE_ENCODING.equals(optionName)){
			return ResourcesPlugin.getEncoding();
		}
		// backward compatibility
		if (COMPILER_PB_INVALID_IMPORT.equals(optionName)
				|| COMPILER_PB_UNREACHABLE_CODE.equals(optionName)) {
			return ERROR;
		}
		String propertyName = optionName;
		if (JavaModelManager.OptionNames.contains(propertyName)){
			Preferences preferences = getPlugin().getPluginPreferences();
			return preferences.getString(propertyName).trim();
		} else if (propertyName.startsWith(JavaCore.PLUGIN_ID + ".formatter")) {//$NON-NLS-1$
			// TODO (olivier) remove after M7
			Preferences preferences = getPlugin().getPluginPreferences();
			return Util.getConvertedDeprecatedValue(preferences, propertyName);
		} else if (propertyName.equals("org.eclipse.jdt.core.align_type_members_on_columns")) { //$NON-NLS-1$
			// TODO (olivier) remove after M7
			Preferences preferences = getPlugin().getPluginPreferences();
			return preferences.getString(DefaultCodeFormatterConstants.FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS);
		}
		return null;
	}
	
	/**
	 * Returns the table of the current options. Initially, all options have their default values,
	 * and this method returns a table that includes all known options.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @return table of current settings of all options 
	 *   (key type: <code>String</code>; value type: <code>String</code>)
	 * @see JavaCore#getDefaultOptions()
	 */
	public static Hashtable getOptions() {
		
		Hashtable options = new Hashtable(10);

		// see #initializeDefaultPluginPreferences() for changing default settings
		Plugin plugin = getPlugin();
		if (plugin != null) {
			Preferences preferences = getPlugin().getPluginPreferences();
			HashSet optionNames = JavaModelManager.OptionNames;
			
			// initialize preferences to their default
			Iterator iterator = optionNames.iterator();
			while (iterator.hasNext()) {
			    String propertyName = (String) iterator.next();
			    options.put(propertyName, preferences.getDefaultString(propertyName));
			}
			// get preferences not set to their default
			String[] propertyNames = preferences.propertyNames();
			for (int i = 0; i < propertyNames.length; i++){
				String propertyName = propertyNames[i];
				String value = preferences.getString(propertyName).trim();
				if (optionNames.contains(propertyName)){
					options.put(propertyName, value);
				}
				// TODO (olivier) Remove after M7
				else if (propertyName.startsWith(JavaCore.PLUGIN_ID + ".formatter")) {//$NON-NLS-1$
					Util.convertFormatterDeprecatedOptions(propertyName, value, options);
				} else if (propertyName.equals("org.eclipse.jdt.core.align_type_members_on_columns")) { //$NON-NLS-1$
					// TODO (olivier) remove after M7
					options.put(DefaultCodeFormatterConstants.FORMATTER_ALIGN_TYPE_MEMBERS_ON_COLUMNS, value);
				}
			}
			// get encoding through resource plugin
			options.put(CORE_ENCODING, ResourcesPlugin.getEncoding());
			// backward compatibility
			options.put(COMPILER_PB_INVALID_IMPORT, ERROR);
			options.put(COMPILER_PB_UNREACHABLE_CODE, ERROR);
		}
		return options;
	}


	/**
	 * Returns the single instance of the Java core plug-in runtime class.
	 * 
	 * @return the single instance of the Java core plug-in runtime class
	 */
	public static Plugin getPlugin() {
		return JAVA_CORE_PLUGIN;
	}

	/**
	 * This is a helper method, which returns the resolved classpath entry denoted 
	 * by a given entry (if it is a variable entry). It is obtained by resolving the variable 
	 * reference in the first segment. Returns <node>null</code> if unable to resolve using 
	 * the following algorithm:
	 * <ul>
	 * <li> if variable segment cannot be resolved, returns <code>null</code></li>
	 * <li> finds a project, JAR or binary folder in the workspace at the resolved path location</li>
	 * <li> if none finds an external JAR file or folder outside the workspace at the resolved path location </li>
	 * <li> if none returns <code>null</code></li>
	 * </ul>
	 * <p>
	 * Variable source attachment path and root path are also resolved and recorded in the resulting classpath entry.
	 * <p>
	 * NOTE: This helper method does not handle classpath containers, for which should rather be used
	 * <code>JavaCore#getClasspathContainer(IPath, IJavaProject)</code>.
	 * <p>
	 * 
	 * @param entry the given variable entry
	 * @return the resolved library or project classpath entry, or <code>null</code>
	 *   if the given variable entry could not be resolved to a valid classpath entry
	 */
	public static IClasspathEntry getResolvedClasspathEntry(IClasspathEntry entry) {
	
		if (entry.getEntryKind() != IClasspathEntry.CPE_VARIABLE)
			return entry;
	
		IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
		IPath resolvedPath = JavaCore.getResolvedVariablePath(entry.getPath());
		if (resolvedPath == null)
			return null;
	
		Object target = JavaModel.getTarget(workspaceRoot, resolvedPath, false);
		if (target == null)
			return null;
	
		// inside the workspace
		if (target instanceof IResource) {
			IResource resolvedResource = (IResource) target;
			if (resolvedResource != null) {
				switch (resolvedResource.getType()) {
					
					case IResource.PROJECT :  
						// internal project
						return JavaCore.newProjectEntry(resolvedPath, entry.isExported());
						
					case IResource.FILE : 
						if (org.eclipse.jdt.internal.compiler.util.Util.isArchiveFileName(resolvedResource.getName())) {
							// internal binary archive
							return JavaCore.newLibraryEntry(
									resolvedPath,
									getResolvedVariablePath(entry.getSourceAttachmentPath()),
									getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
									entry.isExported());
						}
						break;
						
					case IResource.FOLDER : 
						// internal binary folder
						return JavaCore.newLibraryEntry(
								resolvedPath,
								getResolvedVariablePath(entry.getSourceAttachmentPath()),
								getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
								entry.isExported());
				}
			}
		}
		// outside the workspace
		if (target instanceof File) {
			File externalFile = (File) target;
			if (externalFile.isFile()) {
				String fileName = externalFile.getName().toLowerCase();
				if (fileName.endsWith(SuffixConstants.SUFFIX_STRING_jar) || fileName.endsWith(SuffixConstants.SUFFIX_STRING_zip)) { 
					// external binary archive
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			} else { // external binary folder
				if (resolvedPath.isAbsolute()){
					return JavaCore.newLibraryEntry(
							resolvedPath,
							getResolvedVariablePath(entry.getSourceAttachmentPath()),
							getResolvedVariablePath(entry.getSourceAttachmentRootPath()),
							entry.isExported());
				}
			}
		}
		return null;
	}


	/**
	 * Resolve a variable path (helper method).
	 * 
	 * @param variablePath the given variable path
	 * @return the resolved variable path or <code>null</code> if none
	 */
	public static IPath getResolvedVariablePath(IPath variablePath) {
	
		if (variablePath == null)
			return null;
		int count = variablePath.segmentCount();
		if (count == 0)
			return null;
	
		// lookup variable	
		String variableName = variablePath.segment(0);
		IPath resolvedPath = JavaCore.getClasspathVariable(variableName);
		if (resolvedPath == null)
			return null;
	
		// append path suffix
		if (count > 1) {
			resolvedPath = resolvedPath.append(variablePath.removeFirstSegments(1));
		}
		return resolvedPath; 
	}

	/**
	 * Answers the shared working copies currently registered for this buffer factory. 
	 * Working copies can be shared by several clients using the same buffer factory,see 
	 * <code>IWorkingCopy.getSharedWorkingCopy</code>.
	 * 
	 * @param factory the given buffer factory
	 * @return the list of shared working copies for a given buffer factory
	 * @see IWorkingCopy
	 * @since 2.0
	 * @deprecated - should use #getWorkingCopies(WorkingCopyOwner) instead
	 */
	public static IWorkingCopy[] getSharedWorkingCopies(IBufferFactory factory){
		
		// if factory is null, default factory must be used
		if (factory == null) factory = BufferManager.getDefaultBufferManager().getDefaultBufferFactory();

		return getWorkingCopies(BufferFactoryWrapper.create(factory));
	}
	
	/**
	 * Returns the working copies that have the given owner. 
	 * Only compilation units in working copy mode are returned.
	 * If the owner is <code>null</code>, primary working copies are returned.
	 * 
	 * @param owner the given working copy owner or <null> for primary working copy owner
	 * @return the list of working copies for a given owner
	 * @since 3.0
	 */
	public static ICompilationUnit[] getWorkingCopies(WorkingCopyOwner owner){
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		if (owner == null) owner = DefaultWorkingCopyOwner.PRIMARY;
		ICompilationUnit[] result = manager.getWorkingCopies(owner, false/*don't add primary WCs*/);
		if (result == null) return JavaModelManager.NoWorkingCopy;
		return result;
	}
		
	/**
	 * Initializes the default preferences settings for this plug-in.
	 */
	protected void initializeDefaultPluginPreferences() {
		
		Preferences preferences = getPluginPreferences();
		HashSet optionNames = JavaModelManager.OptionNames;
		
		// Compiler settings
		Map compilerOptionsMap = new CompilerOptions().getMap(); // compiler defaults
		for (Iterator iter = compilerOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			preferences.setDefault(optionName, (String)entry.getValue());
			optionNames.add(optionName);
		}
		// override some compiler defaults
		preferences.setDefault(COMPILER_LOCAL_VARIABLE_ATTR, GENERATE);
		preferences.setDefault(COMPILER_CODEGEN_UNUSED_LOCAL, PRESERVE);
		preferences.setDefault(COMPILER_TASK_TAGS, DEFAULT_TASK_TAGS);
		preferences.setDefault(COMPILER_TASK_PRIORITIES, DEFAULT_TASK_PRIORITIES);
		preferences.setDefault(COMPILER_TASK_CASE_SENSITIVE, ENABLED);
		preferences.setDefault(COMPILER_DOC_COMMENT_SUPPORT, ENABLED);

		// Builder settings
		preferences.setDefault(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER, ""); //$NON-NLS-1$
		optionNames.add(CORE_JAVA_BUILD_RESOURCE_COPY_FILTER);

		preferences.setDefault(CORE_JAVA_BUILD_INVALID_CLASSPATH, ABORT); 
		optionNames.add(CORE_JAVA_BUILD_INVALID_CLASSPATH);
	
		preferences.setDefault(CORE_JAVA_BUILD_DUPLICATE_RESOURCE, WARNING); 
		optionNames.add(CORE_JAVA_BUILD_DUPLICATE_RESOURCE);
		
		preferences.setDefault(CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER, CLEAN); 
		optionNames.add(CORE_JAVA_BUILD_CLEAN_OUTPUT_FOLDER);

		// JavaCore settings
		preferences.setDefault(CORE_JAVA_BUILD_ORDER, IGNORE); 
		optionNames.add(CORE_JAVA_BUILD_ORDER);
	
		preferences.setDefault(CORE_INCOMPLETE_CLASSPATH, ERROR); 
		optionNames.add(CORE_INCOMPLETE_CLASSPATH);
		
		preferences.setDefault(CORE_CIRCULAR_CLASSPATH, ERROR); 
		optionNames.add(CORE_CIRCULAR_CLASSPATH);
		
		preferences.setDefault(CORE_INCOMPATIBLE_JDK_LEVEL, IGNORE); 
		optionNames.add(CORE_INCOMPATIBLE_JDK_LEVEL);
		
		preferences.setDefault(CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS, ENABLED); 
		optionNames.add(CORE_ENABLE_CLASSPATH_EXCLUSION_PATTERNS);

		preferences.setDefault(CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS, ENABLED); 
		optionNames.add(CORE_ENABLE_CLASSPATH_MULTIPLE_OUTPUT_LOCATIONS);

		// encoding setting comes from resource plug-in
		optionNames.add(CORE_ENCODING);
		
		// Formatter settings
		Map codeFormatterOptionsMap = DefaultCodeFormatterConstants.getDefaultSettings(); // code formatter defaults
		for (Iterator iter = codeFormatterOptionsMap.entrySet().iterator(); iter.hasNext();) {
			Map.Entry entry = (Map.Entry) iter.next();
			String optionName = (String) entry.getKey();
			preferences.setDefault(optionName, (String)entry.getValue());
			optionNames.add(optionName);
		}		
		preferences.setDefault(FORMATTER_NEWLINE_OPENING_BRACE, DO_NOT_INSERT); 
		optionNames.add(FORMATTER_NEWLINE_OPENING_BRACE);

		preferences.setDefault(FORMATTER_NEWLINE_CONTROL, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_CONTROL);

		preferences.setDefault(FORMATTER_CLEAR_BLANK_LINES, PRESERVE_ONE); 
		optionNames.add(FORMATTER_CLEAR_BLANK_LINES);

		preferences.setDefault(FORMATTER_NEWLINE_ELSE_IF, DO_NOT_INSERT);
		optionNames.add(FORMATTER_NEWLINE_ELSE_IF);

		preferences.setDefault(FORMATTER_NEWLINE_EMPTY_BLOCK, INSERT); 
		optionNames.add(FORMATTER_NEWLINE_EMPTY_BLOCK);

		preferences.setDefault(FORMATTER_LINE_SPLIT, "80"); //$NON-NLS-1$
		optionNames.add(FORMATTER_LINE_SPLIT);

		preferences.setDefault(FORMATTER_COMPACT_ASSIGNMENT, NORMAL); 
		optionNames.add(FORMATTER_COMPACT_ASSIGNMENT);

		preferences.setDefault(FORMATTER_TAB_CHAR, TAB); 
		optionNames.add(FORMATTER_TAB_CHAR);

		preferences.setDefault(FORMATTER_TAB_SIZE, "4"); //$NON-NLS-1$ 
		optionNames.add(FORMATTER_TAB_SIZE);
		
		preferences.setDefault(FORMATTER_SPACE_CASTEXPRESSION, INSERT); //$NON-NLS-1$ 
		optionNames.add(FORMATTER_SPACE_CASTEXPRESSION);

		// CodeAssist settings
		preferences.setDefault(CODEASSIST_VISIBILITY_CHECK, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_VISIBILITY_CHECK);

		preferences.setDefault(CODEASSIST_IMPLICIT_QUALIFICATION, DISABLED); //$NON-NLS-1$
		optionNames.add(CODEASSIST_IMPLICIT_QUALIFICATION);
		
		preferences.setDefault(CODEASSIST_FIELD_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_FIELD_PREFIXES);
		
		preferences.setDefault(CODEASSIST_STATIC_FIELD_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_STATIC_FIELD_PREFIXES);
		
		preferences.setDefault(CODEASSIST_LOCAL_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_LOCAL_PREFIXES);
		
		preferences.setDefault(CODEASSIST_ARGUMENT_PREFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_ARGUMENT_PREFIXES);
		
		preferences.setDefault(CODEASSIST_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_FIELD_SUFFIXES);
		
		preferences.setDefault(CODEASSIST_STATIC_FIELD_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_STATIC_FIELD_SUFFIXES);
		
		preferences.setDefault(CODEASSIST_LOCAL_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_LOCAL_SUFFIXES);
		
		preferences.setDefault(CODEASSIST_ARGUMENT_SUFFIXES, ""); //$NON-NLS-1$
		optionNames.add(CODEASSIST_ARGUMENT_SUFFIXES);
	}
	
	/**
	 * Returns whether the given marker references the given Java element.
	 * Used for markers, which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param marker the marker
	 * @return <code>true</code> if the marker references the element, false otherwise
	 * @exception CoreException if the <code>IMarker.getAttribute</code> on the marker fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarker marker) throws CoreException {
		
		// only match units or classfiles
		if (element instanceof IMember){
			IMember member = (IMember) element;
			if (member.isBinary()){
				element = member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null) return false;			
		if (marker == null) return false;

		String markerHandleId = (String)marker.getAttribute(ATT_HANDLE_ID);
		if (markerHandleId == null) return false;
		
		IJavaElement markerElement = JavaCore.create(markerHandleId);
		while (true){
			if (element.equals(markerElement)) return true; // external elements may still be equal with different handleIDs.
			
			// cycle through enclosing types in case marker is associated with a classfile (15568)
			if (markerElement instanceof IClassFile){
				IType enclosingType = ((IClassFile)markerElement).getType().getDeclaringType();
				if (enclosingType != null){
					markerElement = enclosingType.getClassFile(); // retry with immediate enclosing classfile
					continue;
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Returns whether the given marker delta references the given Java element.
	 * Used for markers deltas, which denote a Java element rather than a resource.
	 *
	 * @param element the element
	 * @param markerDelta the marker delta
	 * @return <code>true</code> if the marker delta references the element
	 * @exception CoreException if the  <code>IMarkerDelta.getAttribute</code> on the marker delta fails 	 
	 */
	public static boolean isReferencedBy(IJavaElement element, IMarkerDelta markerDelta) throws CoreException {
		
		// only match units or classfiles
		if (element instanceof IMember){
			IMember member = (IMember) element;
			if (member.isBinary()){
				element = member.getClassFile();
			} else {
				element = member.getCompilationUnit();
			}
		}
		if (element == null) return false;			
		if (markerDelta == null) return false;

		String markerDeltarHandleId = (String)markerDelta.getAttribute(ATT_HANDLE_ID);
		if (markerDeltarHandleId == null) return false;
		
		IJavaElement markerElement = JavaCore.create(markerDeltarHandleId);
		while (true){
			if (element.equals(markerElement)) return true; // external elements may still be equal with different handleIDs.
			
			// cycle through enclosing types in case marker is associated with a classfile (15568)
			if (markerElement instanceof IClassFile){
				IType enclosingType = ((IClassFile)markerElement).getType().getDeclaringType();
				if (enclosingType != null){
					markerElement = enclosingType.getClassFile(); // retry with immediate enclosing classfile
					continue;
				}
			}
			break;
		}
		return false;
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore.getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore.classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the remaining segments will be passed onto the initializer, and can be used as additional
	 * 	hints during the initialization phase. </li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newContainerEntry(-,false)</code>.
	 * <p>
	 * @param containerPath the path identifying the container, it must be formed of two
	 * 	segments
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath) {
			
		return newContainerEntry(containerPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_CONTAINER</code>
	 * for the given path. The path of the container will be used during resolution so as to map this
	 * container entry to a set of other classpath entries the container is acting for.
	 * <p>
	 * A container entry allows to express indirect references to a set of libraries, projects and variable entries,
	 * which can be interpreted differently for each Java project where it is used.
	 * A classpath container entry can be resolved using <code>JavaCore.getResolvedClasspathContainer</code>,
	 * and updated with <code>JavaCore.classpathContainerChanged</code>
	 * <p>
	 * A container is exclusively resolved by a <code>ClasspathContainerInitializer</code> registered onto the
	 * extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * A container path must be formed of at least one segment, where: <ul>
	 * <li> the first segment is a unique ID identifying the target container, there must be a container initializer registered
	 * 	onto this ID through the extension point  "org.eclipse.jdt.core.classpathContainerInitializer". </li>
	 * <li> the remaining segments will be passed onto the initializer, and can be used as additional
	 * 	hints during the initialization phase. </li>
	 * </ul>
	 * <p>
	 * Example of an ClasspathContainerInitializer for a classpath container denoting a default JDK container:
	 * 
	 * containerEntry = JavaCore.newContainerEntry(new Path("MyProvidedJDK/default"));
	 * 
	 * <extension
	 *    point="org.eclipse.jdt.core.classpathContainerInitializer">
	 *    <containerInitializer
	 *       id="MyProvidedJDK"
	 *       class="com.example.MyInitializer"/> 
	 * <p>
	 * Note that this operation does not attempt to validate classpath containers
	 * or access the resources at the given paths.
	 * <p>
	 * @param containerPath the path identifying the container, it must be formed of at least
	 * 	one segment (ID+hints)
	 * @param isExported a boolean indicating whether this entry is contributed to dependent
	 *    projects in addition to the output location
	 * @return a new container classpath entry
	 * 
	 * @see JavaCore#getClasspathContainer(IPath, IJavaProject)
	 * @see JavaCore#setClasspathContainer(IPath, IJavaProject[], IClasspathContainer[], IProgressMonitor)
	 * @see JavaCore#newContainerEntry(IPath, boolean)
	 * @since 2.0
	 */
	public static IClasspathEntry newContainerEntry(IPath containerPath, boolean isExported) {
			
		if (containerPath == null) Assert.isTrue(false, "Container path cannot be null"); //$NON-NLS-1$
		if (containerPath.segmentCount() < 1) {
			Assert.isTrue(
				false,
				"Illegal classpath container path: \'" + containerPath.makeRelative().toString() + "\', must have at least one segment (containerID+hints)"); //$NON-NLS-1$//$NON-NLS-2$
		}
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_CONTAINER,
			containerPath,
			ClasspathEntry.INCLUDE_ALL,
			ClasspathEntry.EXCLUDE_NONE, 
			null, // source attachment
			null, // source attachment root
			null, // specific output folder
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_LIBRARY</code> for the 
	 * JAR or folder identified by the given absolute path. This specifies that all package fragments 
	 * within the root will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 * <p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newLibraryEntry(-,-,-,false)</code>.
	 * <p>
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive or folder, 
	 *    or <code>null</code> if none. Note, since 3.0, an empty path is allowed to denote no source attachment.
	 *   and will be automatically converted to <code>null</code>.
	 * @param sourceAttachmentRootPath the location of the root within the source archive or folder
	 *    or <code>null</code> if this location should be automatically detected.
	 * @return a new library classpath entry
	 * 
	 * @see #newLibraryEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath) {
			
		return newLibraryEntry(path, sourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_LIBRARY</code> for the JAR or folder
	 * identified by the given absolute path. This specifies that all package fragments within the root 
	 * will have children of type <code>IClassFile</code>.
	 * <p>
	 * A library entry is used to denote a prerequisite JAR or root folder containing binaries.
	 * The target JAR or folder can either be defined internally to the workspace (absolute path relative
	 * to the workspace root) or externally to the workspace (absolute path in the file system).
	 *	<p>
	 * e.g. Here are some examples of binary path usage<ul>
	 *	<li><code> "c:/jdk1.2.2/jre/lib/rt.jar" </code> - reference to an external JAR</li>
	 *	<li><code> "/Project/someLib.jar" </code> - reference to an internal JAR </li>
	 *	<li><code> "c:/classes/" </code> - reference to an external binary folder</li>
	 * </ul>
	 * Note that this operation does not attempt to validate or access the 
	 * resources at the given paths.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @param sourceAttachmentPath the absolute path of the corresponding source archive or folder, 
	 *    or <code>null</code> if none. Note, since 3.0, an empty path is allowed to denote no source attachment.
	 *   and will be automatically converted to <code>null</code>.
	 * @param sourceAttachmentRootPath the location of the root within the source archive or folder
	 *    or <code>null</code> if this location should be automatically detected.
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new library classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newLibraryEntry(
		IPath path,
		IPath sourceAttachmentPath,
		IPath sourceAttachmentRootPath,
		boolean isExported) {
			
		if (path == null) Assert.isTrue(false, "Library path cannot be null"); //$NON-NLS-1$
		if (!path.isAbsolute()) Assert.isTrue(false, "Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
		if (sourceAttachmentPath != null) {
			if (sourceAttachmentPath.isEmpty()) {
				sourceAttachmentPath = null; // treat empty path as none
			} else if (!sourceAttachmentPath.isAbsolute()) {
				Assert.isTrue(false, "Source attachment path for IClasspathEntry must be absolute"); //$NON-NLS-1$
			}
		}
		return new ClasspathEntry(
			IPackageFragmentRoot.K_BINARY,
			IClasspathEntry.CPE_LIBRARY,
			JavaProject.canonicalizedPath(path),
			ClasspathEntry.INCLUDE_ALL, 
			ClasspathEntry.EXCLUDE_NONE, 
			sourceAttachmentPath,
			sourceAttachmentRootPath,
			null, // specific output folder
			isExported);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newProjectEntry(_,false)</code>.
	 * <p>
	 * 
	 * @param path the absolute path of the binary archive
	 * @return a new project classpath entry
	 * 
	 * @see JavaCore#newProjectEntry(IPath, boolean)
	 */
	public static IClasspathEntry newProjectEntry(IPath path) {
		return newProjectEntry(path, false);
	}
	
	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_PROJECT</code>
	 * for the project identified by the given absolute path.
	 * <p>
	 * A project entry is used to denote a prerequisite project on a classpath.
	 * The referenced project will be contributed as a whole, either as sources (in the Java Model, it
	 * contributes all its package fragment roots) or as binaries (when building, it contributes its 
	 * whole output location).
	 * <p>
	 * A project reference allows to indirect through another project, independently from its internal layout. 
	 * <p>
	 * The prerequisite project is referred to using an absolute path relative to the workspace root.
	 * <p>
	 * 
	 * @param path the absolute path of the prerequisite project
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new project classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newProjectEntry(IPath path, boolean isExported) {
		
		if (!path.isAbsolute()) Assert.isTrue(false, "Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
		
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_PROJECT,
			path,
			ClasspathEntry.INCLUDE_ALL, 
			ClasspathEntry.EXCLUDE_NONE, 
			null, // source attachment
			null, // source attachment root
			null, // specific output folder
			isExported);
	}

	/**
	 * Returns a new empty region.
	 * 
	 * @return a new empty region
	 */
	public static IRegion newRegion() {
		return new Region();
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute 
	 * workspace-relative path. This specifies that all package fragments
	 * within the root will have children of type <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source 
	 * folders are located with that project. That is, a source classpath
	 * entry specifying the path <code>/P1/src</code> is only usable for
	 * project <code>P1</code>.
	 * </p>
	 * <p>
	 * The source classpath entry created by this method includes all source
	 * files below the given workspace-relative path. To selectively exclude
	 * some of these source files, use the factory method 
	 * <code>JavaCore.newSourceEntry(IPath,IPath[])</code> instead.
	 * </p>
	 * <p>
	 * Note that all sources/binaries inside a project are contributed as a whole through
	 * a project entry (see <code>JavaCore.newProjectEntry</code>). Particular
	 * source entries cannot be selectively exported.
	 * </p>
	 * 
	 * @param path the absolute workspace-relative path of a source folder
	 * @return a new source classpath entry with not exclusion patterns
	 * 
	 * @see #newSourceEntry(org.eclipse.core.runtime.IPath,org.eclipse.core.runtime.IPath[])
	 */
	public static IClasspathEntry newSourceEntry(IPath path) {

		return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL, ClasspathEntry.EXCLUDE_NONE, null /*output location*/);
	}
	
	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute 
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns. This specifies that all package
	 * fragments within the root will have children of type 
	 * <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source 
	 * folders are located with that project. That is, a source classpath
	 * entry specifying the path <code>/P1/src</code> is only usable for
	 * project <code>P1</code>.
	 * </p>
	 * <p>
	 * The source classpath entry created by this method includes all source
	 * files below the given workspace-relative path except for those matched
	 * by one (or more) of the given exclusion patterns. Each exclusion pattern
	 * is represented by a relative path, which is interpreted as relative to
	 * the source folder. For example, if the source folder path is 
	 * <code>/Project/src</code> and the exclusion pattern is 
	 * <code>com/xyz/tests/&#42;&#42;</code>, then source files
	 * like <code>/Project/src/com/xyz/Foo.java</code>
	 * and <code>/Project/src/com/xyz/utils/Bar.java</code> would be included,
	 * whereas <code>/Project/src/com/xyz/tests/T1.java</code>
	 * and <code>/Project/src/com/xyz/tests/quick/T2.java</code> would be
	 * excluded. Exclusion patterns can contain can contain '**', '*' or '?'
	 * wildcards; see <code>IClasspathEntry.getExclusionPatterns</code>
	 * for the full description of the syntax and semantics of exclusion
	 * patterns.
	 * </p>
	 * If the empty list of exclusion patterns is specified, the source folder
	 * will automatically include all resources located inside the source
	 * folder. In that case, the result is entirely equivalent to using the
	 * factory method <code>JavaCore.newSourceEntry(IPath)</code>. 
	 * </p>
	 * <p>
	 * Note that all sources/binaries inside a project are contributed as a whole through
	 * a project entry (see <code>JavaCore.newProjectEntry</code>). Particular
	 * source entries cannot be selectively exported.
	 * </p>
	 *
	 * @param path the absolute workspace-relative path of a source folder
	 * @param exclusionPatterns the possibly empty list of exclusion patterns
	 *    represented as relative paths
	 * @return a new source classpath entry with the given exclusion patterns
	 * @see #newSourceEntry(org.eclipse.core.runtime.IPath)
	 * @see IClasspathEntry#getInclusionPatterns()
	 * @see IClasspathEntry#getExclusionPatterns()
	 * 
	 * @since 2.1
	 */
	public static IClasspathEntry newSourceEntry(IPath path, IPath[] exclusionPatterns) {

		return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL, exclusionPatterns, null /*output location*/); 
	}

	/**
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute 
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns, and associated with a specific output location
	 * (that is, ".class" files are not going to the project default output location). 
	 * All package fragments within the root will have children of type 
	 * <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source 
	 * folders are located with that project. That is, a source classpath
	 * entry specifying the path <code>/P1/src</code> is only usable for
	 * project <code>P1</code>.
	 * </p>
	 * <p>
	 * The source classpath entry created by this method includes all source
	 * files below the given workspace-relative path except for those matched
	 * by one (or more) of the given exclusion patterns. Each exclusion pattern
	 * is represented by a relative path, which is interpreted as relative to
	 * the source folder. For example, if the source folder path is 
	 * <code>/Project/src</code> and the exclusion pattern is 
	 * <code>com/xyz/tests/&#42;&#42;</code>, then source files
	 * like <code>/Project/src/com/xyz/Foo.java</code>
	 * and <code>/Project/src/com/xyz/utils/Bar.java</code> would be included,
	 * whereas <code>/Project/src/com/xyz/tests/T1.java</code>
	 * and <code>/Project/src/com/xyz/tests/quick/T2.java</code> would be
	 * excluded. Exclusion patterns can contain can contain '**', '*' or '?'
	 * wildcards; see <code>IClasspathEntry.getExclusionPatterns</code>
	 * for the full description of the syntax and semantics of exclusion
	 * patterns.
	 * </p>
	 * If the empty list of exclusion patterns is specified, the source folder
	 * will automatically include all resources located inside the source
	 * folder. In that case, the result is entirely equivalent to using the
	 * factory method <code>JavaCore.newSourceEntry(IPath)</code>. 
	 * </p>
	 * <p>
	 * Additionally, a source entry can be associated with a specific output location. 
	 * By doing so, the Java builder will ensure that the generated ".class" files will 
	 * be issued inside this output location, as opposed to be generated into the 
	 * project default output location (when output location is <code>null</code>). 
	 * Note that multiple source entries may target the same output location.
	 * The output location is referred to using an absolute path relative to the 
	 * workspace root, e.g. <code>"/Project/bin"</code>, it must be located inside 
	 * the same project as the source folder.
	 * </p>
	 * <p>
	 * Also note that all sources/binaries inside a project are contributed as a whole through
	 * a project entry (see <code>JavaCore.newProjectEntry</code>). Particular
	 * source entries cannot be selectively exported.
	 * </p>
	 *
	 * @param path the absolute workspace-relative path of a source folder
	 * @param exclusionPatterns the possibly empty list of exclusion patterns
	 *    represented as relative paths
	 * @param specificOutputLocation the specific output location for this source entry (<code>null</code> if using project default ouput location)
	 * @return a new source classpath entry with the given exclusion patterns
	 * @see #newSourceEntry(org.eclipse.core.runtime.IPath)
	 * @see IClasspathEntry#getInclusionPatterns()
	 * @see IClasspathEntry#getExclusionPatterns()
	 * @see IClasspathEntry#getOutputLocation()
	 * 
	 * @since 2.1
	 */
	public static IClasspathEntry newSourceEntry(IPath path, IPath[] exclusionPatterns, IPath specificOutputLocation) {

	    return newSourceEntry(path, ClasspathEntry.INCLUDE_ALL, exclusionPatterns, specificOutputLocation);
	}
		
	/** TODO (philippe) fixup spec for inclusion patterns
	 * Creates and returns a new classpath entry of kind <code>CPE_SOURCE</code>
	 * for the project's source folder identified by the given absolute 
	 * workspace-relative path but excluding all source files with paths
	 * matching any of the given patterns, and associated with a specific output location
	 * (that is, ".class" files are not going to the project default output location). 
	 * All package fragments within the root will have children of type 
	 * <code>ICompilationUnit</code>.
	 * <p>
	 * The source folder is referred to using an absolute path relative to the
	 * workspace root, e.g. <code>/Project/src</code>. A project's source 
	 * folders are located with that project. That is, a source classpath
	 * entry specifying the path <code>/P1/src</code> is only usable for
	 * project <code>P1</code>.
	 * </p>
	 * <p>
	 * The source classpath entry created by this method includes all source
	 * files below the given workspace-relative path except for those matched
	 * by one (or more) of the given exclusion patterns. Each exclusion pattern
	 * is represented by a relative path, which is interpreted as relative to
	 * the source folder. For example, if the source folder path is 
	 * <code>/Project/src</code> and the exclusion pattern is 
	 * <code>com/xyz/tests/&#42;&#42;</code>, then source files
	 * like <code>/Project/src/com/xyz/Foo.java</code>
	 * and <code>/Project/src/com/xyz/utils/Bar.java</code> would be included,
	 * whereas <code>/Project/src/com/xyz/tests/T1.java</code>
	 * and <code>/Project/src/com/xyz/tests/quick/T2.java</code> would be
	 * excluded. Exclusion patterns can contain can contain '**', '*' or '?'
	 * wildcards; see <code>IClasspathEntry.getExclusionPatterns</code>
	 * for the full description of the syntax and semantics of exclusion
	 * patterns.
	 * </p>
	 * If the empty list of exclusion patterns is specified, the source folder
	 * will automatically include all resources located inside the source
	 * folder. In that case, the result is entirely equivalent to using the
	 * factory method <code>JavaCore.newSourceEntry(IPath)</code>. 
	 * </p>
	 * <p>
	 * Additionally, a source entry can be associated with a specific output location. 
	 * By doing so, the Java builder will ensure that the generated ".class" files will 
	 * be issued inside this output location, as opposed to be generated into the 
	 * project default output location (when output location is <code>null</code>). 
	 * Note that multiple source entries may target the same output location.
	 * The output location is referred to using an absolute path relative to the 
	 * workspace root, e.g. <code>"/Project/bin"</code>, it must be located inside 
	 * the same project as the source folder.
	 * </p>
	 * <p>
	 * Also note that all sources/binaries inside a project are contributed as a whole through
	 * a project entry (see <code>JavaCore.newProjectEntry</code>). Particular
	 * source entries cannot be selectively exported.
	 * </p>
	 *
	 * @param path the absolute workspace-relative path of a source folder
	 * @param inclusionPatterns the possibly empty list of inclusion patterns
	 *    represented as relative paths
	 * @param exclusionPatterns the possibly empty list of exclusion patterns
	 *    represented as relative paths
	 * @param specificOutputLocation the specific output location for this source entry (<code>null</code> if using project default ouput location)
	 * @return a new source classpath entry with the given exclusion patterns
	 * @see #newSourceEntry(org.eclipse.core.runtime.IPath)
	 * @see IClasspathEntry#getInclusionPatterns()
	 * @see IClasspathEntry#getExclusionPatterns()
	 * @see IClasspathEntry#getOutputLocation()
	 * 
	 * @since 3.0
	 */
	public static IClasspathEntry newSourceEntry(IPath path, IPath[] inclusionPatterns, IPath[] exclusionPatterns, IPath specificOutputLocation) {

		if (path == null) Assert.isTrue(false, "Source path cannot be null"); //$NON-NLS-1$
		if (!path.isAbsolute()) Assert.isTrue(false, "Path for IClasspathEntry must be absolute"); //$NON-NLS-1$
		if (exclusionPatterns == null) Assert.isTrue(false, "Exclusion pattern set cannot be null"); //$NON-NLS-1$

		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_SOURCE,
			path,
			inclusionPatterns,
			exclusionPatterns,
			null, // source attachment
			null, // source attachment root
			specificOutputLocation, // custom output location
			false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>),
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resolved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 * The resulting entry is not exported to dependent projects. This method is equivalent to
	 * <code>newVariableEntry(-,-,-,false)</code>.
	 * <p>
	 * 
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param sourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @return a new library classpath entry
	 * 
	 * @see JavaCore#newVariableEntry(IPath, IPath, IPath, boolean)
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath sourceAttachmentRootPath) {

		return newVariableEntry(variablePath, variableSourceAttachmentPath, sourceAttachmentRootPath, false);
	}

	/**
	 * Creates and returns a new non-exported classpath entry of kind <code>CPE_VARIABLE</code>
	 * for the given path. The first segment of the path is the name of a classpath variable.
	 * The trailing segments of the path will be appended to resolved variable path.
	 * <p>
	 * A variable entry allows to express indirect references on a classpath to other projects or libraries,
	 * depending on what the classpath variable is referring.
	 * <p>
	 *	It is possible to register an automatic initializer (<code>ClasspathVariableInitializer</code>),
	 * which will be invoked through the extension point "org.eclipse.jdt.core.classpathVariableInitializer".
	 * After resolution, a classpath variable entry may either correspond to a project or a library entry. </li>	 
	 * <p>
	 * e.g. Here are some examples of variable path usage<ul>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "c:/jars/jdtcore.jar". The resolved classpath entry is denoting the library "c:\jars\jdtcore.jar"</li>
	 * <li> "JDTCORE" where variable <code>JDTCORE</code> is 
	 *		bound to "/Project_JDTCORE". The resolved classpath entry is denoting the project "/Project_JDTCORE"</li>
	 * <li> "PLUGINS/com.example/example.jar" where variable <code>PLUGINS</code>
	 *      is bound to "c:/eclipse/plugins". The resolved classpath entry is denoting the library "c:/eclipse/plugins/com.example/example.jar"</li>
	 * </ul>
	 * Note that this operation does not attempt to validate classpath variables
	 * or access the resources at the given paths.
	 * <p>
	 *
	 * @param variablePath the path of the binary archive; first segment is the
	 *   name of a classpath variable
	 * @param variableSourceAttachmentPath the path of the corresponding source archive, 
	 *    or <code>null</code> if none; if present, the first segment is the
	 *    name of a classpath variable (not necessarily the same variable
	 *    as the one that begins <code>variablePath</code>)
	 * @param variableSourceAttachmentRootPath the location of the root within the source archive
	 *    or <code>null</code> if <code>archivePath</code> is also <code>null</code>
	 * @param isExported indicates whether this entry is contributed to dependent
	 * 	  projects in addition to the output location
	 * @return a new variable classpath entry
	 * @since 2.0
	 */
	public static IClasspathEntry newVariableEntry(
		IPath variablePath,
		IPath variableSourceAttachmentPath,
		IPath variableSourceAttachmentRootPath,
		boolean isExported) {

		if (variablePath == null) Assert.isTrue(false, "Variable path cannot be null"); //$NON-NLS-1$
		if (variablePath.segmentCount() < 1) {
			Assert.isTrue(
				false,
				"Illegal classpath variable path: \'" + variablePath.makeRelative().toString() + "\', must have at least one segment"); //$NON-NLS-1$//$NON-NLS-2$
		}
	
		return new ClasspathEntry(
			IPackageFragmentRoot.K_SOURCE,
			IClasspathEntry.CPE_VARIABLE,
			variablePath,
			ClasspathEntry.INCLUDE_ALL, 
			ClasspathEntry.EXCLUDE_NONE, 
			variableSourceAttachmentPath, // source attachment
			variableSourceAttachmentRootPath, // source attachment root			
			null, // specific output folder
			isExported);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @see #setClasspathVariable(String, IPath)
	 *
	 * @deprecated - use version with extra IProgressMonitor
	 */
	public static void removeClasspathVariable(String variableName) {
		removeClasspathVariable(variableName, null);
	}

	/**
	 * Removed the given classpath variable. Does nothing if no value was
	 * set for this classpath variable.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param monitor the progress monitor to report progress
	 * @see #setClasspathVariable(String, IPath)
	 */
	public static void removeClasspathVariable(
		String variableName,
		IProgressMonitor monitor) {

		try {
			updateVariableValues(new String[]{ variableName}, new IPath[]{ null }, monitor);
		} catch (JavaModelException e) {
			// cannot happen: ignore
		}
	}

	/**
	 * Removes the given element changed listener.
	 * Has no affect if an identical listener is not registered.
	 *
	 * @param listener the listener
	 */
	public static void removeElementChangedListener(IElementChangedListener listener) {
		JavaModelManager.getJavaModelManager().deltaState.removeElementChangedListener(listener);
	}
	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements,
	 * registered listeners receive after-the-fact notification of
	 * what just transpired, in the form of a element changed event.
	 * This method allows clients to call a number of
	 * methods that modify java elements and only have element
	 * changed event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such
	 * call, this method runs the action and then reports a single
	 * element changed event describing the net effect of all changes
	 * done to java elements by the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such
	 * call, this method simply runs the action.
	 * </p>
	 *
	 * @param action the action to perform
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if the operation failed.
	 * @since 2.1
	 */
	public static void run(IWorkspaceRunnable action, IProgressMonitor monitor) throws CoreException {
		run(action, ResourcesPlugin.getWorkspace().getRoot(), monitor);
	}
	/**
	 * Runs the given action as an atomic Java model operation.
	 * <p>
	 * After running a method that modifies java elements,
	 * registered listeners receive after-the-fact notification of
	 * what just transpired, in the form of a element changed event.
	 * This method allows clients to call a number of
	 * methods that modify java elements and only have element
	 * changed event notifications reported at the end of the entire
	 * batch.
	 * </p>
	 * <p>
	 * If this method is called outside the dynamic scope of another such
	 * call, this method runs the action and then reports a single
	 * element changed event describing the net effect of all changes
	 * done to java elements by the action.
	 * </p>
	 * <p>
	 * If this method is called in the dynamic scope of another such
	 * call, this method simply runs the action.
	 * </p>
	 * <p>
 	 * The supplied scheduling rule is used to determine whether this operation can be
	 * run simultaneously with workspace changes in other threads. See 
	 * <code>IWorkspace.run(...)</code> for more details.
 	 * </p>
	 *
	 * @param action the action to perform
	 * @param rule the scheduling rule to use when running this operation, or
	 * <code>null</code> if there are no scheduling restrictions for this operation.
	 * @param monitor a progress monitor, or <code>null</code> if progress
	 *    reporting and cancellation are not desired
	 * @exception CoreException if the operation failed.
	 * @since 3.0
	 */
	public static void run(IWorkspaceRunnable action, ISchedulingRule rule, IProgressMonitor monitor) throws CoreException {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		if (workspace.isTreeLocked()) {
			new BatchOperation(action).run(monitor);
		} else {
			// use IWorkspace.run(...) to ensure that a build will be done in autobuild mode
			workspace.run(new BatchOperation(action), rule, IWorkspace.AVOID_UPDATE, monitor);
		}
	}	
	/** 
	 * Bind a container reference path to some actual containers (<code>IClasspathContainer</code>).
	 * This API must be invoked whenever changes in container need to be reflected onto the JavaModel.
	 * Containers can have distinct values in different projects, therefore this API considers a
	 * set of projects with their respective containers.
	 * <p>
	 * <code>containerPath</code> is the path under which these values can be referenced through
	 * container classpath entries (<code>IClasspathEntry#CPE_CONTAINER</code>). A container path 
	 * is formed by a first ID segment followed with extra segments, which can be used as additional hints
	 * for the resolution. The container ID is used to identify a <code>ClasspathContainerInitializer</code> 
	 * registered on the extension point "org.eclipse.jdt.core.classpathContainerInitializer".
	 * <p>
	 * There is no assumption that each individual container value passed in argument 
	 * (<code>respectiveContainers</code>) must answer the exact same path when requested 
	 * <code>IClasspathContainer#getPath</code>. 
	 * Indeed, the containerPath is just an indication for resolving it to an actual container object. It can be 
	 * delegated to a <code>ClasspathContainerInitializer</code>, which can be activated through the extension
	 * point "org.eclipse.jdt.core.ClasspathContainerInitializer"). 
	 * <p>
	 * In reaction to changing container values, the JavaModel will be updated to reflect the new
	 * state of the updated container. A combined Java element delta will be notified to describe the corresponding 
	 * classpath changes resulting from the container update. This operation is batched, and automatically eliminates
	 * unnecessary updates (new container is same as old one). This operation acquires a lock on the workspace's root.
	 * <p>
	 * This functionality cannot be used while the workspace is locked, since
	 * it may create/remove some resource markers.
	 * <p>
	 * Classpath container values are persisted locally to the workspace, but 
	 * are not preserved from a session to another. It is thus highly recommended to register a 
	 * <code>ClasspathContainerInitializer</code> for each referenced container 
	 * (through the extension point "org.eclipse.jdt.core.ClasspathContainerInitializer").
	 * <p>
	 * Note: setting a container to <code>null</code> will cause it to be lazily resolved again whenever
	 * its value is required. In particular, this will cause a registered initializer to be invoked
	 * again.
	 * <p>
	 * @param containerPath - the name of the container reference, which is being updated
	 * @param affectedProjects - the set of projects for which this container is being bound
	 * @param respectiveContainers - the set of respective containers for the affected projects
	 * @param monitor a monitor to report progress
	 * @throws JavaModelException
	 * @see ClasspathContainerInitializer
	 * @see #getClasspathContainer(IPath, IJavaProject)
	 * @see IClasspathContainer
	 * @since 2.0
	 */
	public static void setClasspathContainer(final IPath containerPath, IJavaProject[] affectedProjects, IClasspathContainer[] respectiveContainers, IProgressMonitor monitor) throws JavaModelException {

		if (affectedProjects.length != respectiveContainers.length) Assert.isTrue(false, "Projects and containers collections should have the same size"); //$NON-NLS-1$
	
		if (monitor != null && monitor.isCanceled()) return;
	
		if (JavaModelManager.CP_RESOLVE_VERBOSE){
			System.out.println("CPContainer SET  - setting container: ["+containerPath+"] for projects: {" //$NON-NLS-1$ //$NON-NLS-2$
				+ (org.eclipse.jdt.internal.compiler.util.Util.toString(affectedProjects, 
						new org.eclipse.jdt.internal.compiler.util.Util.Displayable(){ 
							public String displayString(Object o) { return ((IJavaProject) o).getElementName(); }
						}))
				+ "} with values: " //$NON-NLS-1$
				+ (org.eclipse.jdt.internal.compiler.util.Util.toString(respectiveContainers, 
						new org.eclipse.jdt.internal.compiler.util.Util.Displayable(){ 
							public String displayString(Object o) { return ((IClasspathContainer) o).getDescription(); }
						}))
					);
		}

		final int projectLength = affectedProjects.length;
		final IJavaProject[] modifiedProjects;
		System.arraycopy(affectedProjects, 0, modifiedProjects = new IJavaProject[projectLength], 0, projectLength);
		final IClasspathEntry[][] oldResolvedPaths = new IClasspathEntry[projectLength][];
			
		// filter out unmodified project containers
		int remaining = 0;
		for (int i = 0; i < projectLength; i++){
	
			if (monitor != null && monitor.isCanceled()) return;
	
			IJavaProject affectedProject = affectedProjects[i];
			IClasspathContainer newContainer = respectiveContainers[i];
			if (newContainer == null) newContainer = JavaModelManager.ContainerInitializationInProgress; // 30920 - prevent infinite loop
			boolean found = false;
			if (JavaProject.hasJavaNature(affectedProject.getProject())){
				IClasspathEntry[] rawClasspath = affectedProject.getRawClasspath();
				for (int j = 0, cpLength = rawClasspath.length; j <cpLength; j++) {
					IClasspathEntry entry = rawClasspath[j];
					if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && entry.getPath().equals(containerPath)){
						found = true;
						break;
					}
				}
			}
			if (!found){
				modifiedProjects[i] = null; // filter out this project - does not reference the container path, or isnt't yet Java project
				JavaModelManager.containerPut(affectedProject, containerPath, newContainer);
				continue;
			}
			IClasspathContainer oldContainer = JavaModelManager.containerGet(affectedProject, containerPath);
			if (oldContainer == JavaModelManager.ContainerInitializationInProgress) {
				Map previousContainerValues = (Map)JavaModelManager.PreviousSessionContainers.get(affectedProject);
				if (previousContainerValues != null){
					IClasspathContainer previousContainer = (IClasspathContainer)previousContainerValues.get(containerPath);
					if (previousContainer != null) {
						if (JavaModelManager.CP_RESOLVE_VERBOSE){
							System.out.println("CPContainer INIT - reentering access to project container: ["+affectedProject.getElementName()+"] " + containerPath + " during its initialization, will see previous value: "+ previousContainer.getDescription()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
						}
						JavaModelManager.containerPut(affectedProject, containerPath, previousContainer); 
					}
					oldContainer = null; //33695 - cannot filter out restored container, must update affected project to reset cached CP
				} else {
					oldContainer = null;
				}
			}
			if (oldContainer != null && oldContainer.equals(respectiveContainers[i])){
				modifiedProjects[i] = null; // filter out this project - container did not change
				continue;
			}
			remaining++; 
			oldResolvedPaths[i] = affectedProject.getResolvedClasspath(true);
			JavaModelManager.containerPut(affectedProject, containerPath, newContainer);
		}
		
		if (remaining == 0) return;
		
		// trigger model refresh
		try {
			final boolean canChangeResources = !ResourcesPlugin.getWorkspace().isTreeLocked();
			JavaCore.run(new IWorkspaceRunnable() {
				public void run(IProgressMonitor progressMonitor) throws CoreException {
					for(int i = 0; i < projectLength; i++){
		
						if (progressMonitor != null && progressMonitor.isCanceled()) return;
		
						JavaProject affectedProject = (JavaProject)modifiedProjects[i];
						if (affectedProject == null) continue; // was filtered out
						
						if (JavaModelManager.CP_RESOLVE_VERBOSE){
							System.out.println("CPContainer SET  - updating affected project: ["+affectedProject.getElementName()+"] due to setting container: " + containerPath); //$NON-NLS-1$ //$NON-NLS-2$
						}

						// force a refresh of the affected project (will compute deltas)
						affectedProject.setRawClasspath(
								affectedProject.getRawClasspath(),
								SetClasspathOperation.ReuseOutputLocation,
								progressMonitor,
								canChangeResources,
								oldResolvedPaths[i],
								false, // updating - no need for early validation
								false); // updating - no need to save
					}
				}
			},
			null/*no need to lock anything*/,
			monitor);
		} catch(CoreException e) {
			if (JavaModelManager.CP_RESOLVE_VERBOSE){
				System.out.println("CPContainer SET  - FAILED DUE TO EXCEPTION: "+containerPath); //$NON-NLS-1$
				e.printStackTrace();
			}
			if (e instanceof JavaModelException) {
				throw (JavaModelException)e;
			} else {
				throw new JavaModelException(e);
			}
		} finally {
			for (int i = 0; i < projectLength; i++) {
				if (respectiveContainers[i] == null) {
					JavaModelManager.containerPut(affectedProjects[i], containerPath, null); // reset init in progress marker
				}
			}
		}
					
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must have at least one segment.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @throws JavaModelException
	 * @see #getClasspathVariable(String)
	 *
	 * @deprecated - use API with IProgressMonitor
	 */
	public static void setClasspathVariable(String variableName, IPath path)
		throws JavaModelException {

		setClasspathVariable(variableName, path, null);
	}

	/**
	 * Sets the value of the given classpath variable.
	 * The path must not be null.
	 * <p>
	 * This functionality cannot be used while the resource tree is locked.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 *
	 * @param variableName the name of the classpath variable
	 * @param path the path
	 * @param monitor a monitor to report progress
	 * @throws JavaModelException
	 * @see #getClasspathVariable(String)
	 */
	public static void setClasspathVariable(
		String variableName,
		IPath path,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (path == null) Assert.isTrue(false, "Variable path cannot be null"); //$NON-NLS-1$
		setClasspathVariables(new String[]{variableName}, new IPath[]{ path }, monitor);
	}

	/**
	 * Sets the values of all the given classpath variables at once.
	 * Null paths can be used to request corresponding variable removal.
	 * <p>
	 * A combined Java element delta will be notified to describe the corresponding 
	 * classpath changes resulting from the variables update. This operation is batched, 
	 * and automatically eliminates unnecessary updates (new variable is same as old one). 
	 * This operation acquires a lock on the workspace's root.
	 * <p>
	 * This functionality cannot be used while the workspace is locked, since
	 * it may create/remove some resource markers.
	 * <p>
	 * Classpath variable values are persisted locally to the workspace, and 
	 * are preserved from session to session.
	 * <p>
	 * Updating a variable with the same value has no effect.
	 * 
	 * @param variableNames an array of names for the updated classpath variables
	 * @param paths an array of path updates for the modified classpath variables (null
	 *       meaning that the corresponding value will be removed
	 * @param monitor a monitor to report progress
	 * @throws JavaModelException
	 * @see #getClasspathVariable(String)
	 * @since 2.0
	 */
	public static void setClasspathVariables(
		String[] variableNames,
		IPath[] paths,
		IProgressMonitor monitor)
		throws JavaModelException {

		if (variableNames.length != paths.length)	Assert.isTrue(false, "Variable names and paths collections should have the same size"); //$NON-NLS-1$
		updateVariableValues(variableNames, paths, monitor);
	}

	/**
	 * Sets the current table of options. All and only the options explicitly included in the given table 
	 * are remembered; all previous option settings are forgotten, including ones not explicitly
	 * mentioned.
	 * <p>
	 * For a complete description of the configurable options, see <code>getDefaultOptions</code>.
	 * </p>
	 * 
	 * @param newOptions the new options (key type: <code>String</code>; value type: <code>String</code>),
	 *   or <code>null</code> to reset all options to their default values
	 * @see JavaCore#getDefaultOptions()
	 */
	public static void setOptions(Hashtable newOptions) {
		
		// see #initializeDefaultPluginPreferences() for changing default settings
		Preferences preferences = getPlugin().getPluginPreferences();

		if (newOptions == null){
			newOptions = JavaCore.getDefaultOptions();
		}
		Enumeration keys = newOptions.keys();
		while (keys.hasMoreElements()){
			String key = (String)keys.nextElement();
			if (!JavaModelManager.OptionNames.contains(key)) continue; // unrecognized option
			if (key.equals(CORE_ENCODING)) continue; // skipped, contributed by resource prefs
			String value = (String)newOptions.get(key);
			preferences.setValue(key, value);
		}

		// persist options
		getPlugin().savePluginPreferences();
	}
	
	/**
	 * Shutdown the JavaCore plug-in.
	 * <p>
	 * De-registers the JavaModelManager as a resource changed listener and save participant.
	 * <p>
	 * @see org.eclipse.core.runtime.Plugin#shutdown()
	 */
	public void shutdown() {

		savePluginPreferences();
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.removeResourceChangeListener(JavaModelManager.getJavaModelManager().deltaState);
		workspace.removeSaveParticipant(this);

		JavaModelManager.getJavaModelManager().shutdown();
	}

	/**
	 * Initiate the background indexing process.
	 * This should be deferred after the plugin activation.
	 */
	private void startIndexing() {

		JavaModelManager.getJavaModelManager().getIndexManager().reset();
	}

	/**
	 * Startup of the JavaCore plug-in.
	 * <p>
	 * Registers the JavaModelManager as a resource changed listener and save participant.
	 * Starts the background indexing, and restore saved classpath variable values.
	 * <p>
	 * @throws CoreException
	 * @see org.eclipse.core.runtime.Plugin#startup()
	 */
	public void startup() throws CoreException {
		
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		try {
			manager.configurePluginDebugOptions();

			// request state folder creation (workaround 19885)
			JavaCore.getPlugin().getStateLocation();

			// retrieve variable values
			JavaCore.getPlugin().getPluginPreferences().addPropertyChangeListener(new JavaModelManager.PluginPreferencesListener());
			manager.loadVariablesAndContainers();

			IWorkspace workspace = ResourcesPlugin.getWorkspace();
			workspace.addResourceChangeListener(
				manager.deltaState,
				IResourceChangeEvent.PRE_AUTO_BUILD
					| IResourceChangeEvent.POST_AUTO_BUILD
					| IResourceChangeEvent.POST_CHANGE
					| IResourceChangeEvent.PRE_DELETE
					| IResourceChangeEvent.PRE_CLOSE);

			startIndexing();
			workspace.addSaveParticipant(this, manager);
		} catch (RuntimeException e) {
			manager.shutdown();
			throw e;
		}
	}


	/*
	 * Internal updating of a variable values (null path meaning removal), allowing to change multiple variable values at once.
	 */
	private static void updateVariableValues(
		String[] variableNames,
		IPath[] variablePaths,
		IProgressMonitor monitor) throws JavaModelException {
	
		if (monitor != null && monitor.isCanceled()) return;
		
		if (JavaModelManager.CP_RESOLVE_VERBOSE){
			System.out.println("CPVariable SET  - setting variables: {" + org.eclipse.jdt.internal.compiler.util.Util.toString(variableNames)  //$NON-NLS-1$
				+ "} with values: " + org.eclipse.jdt.internal.compiler.util.Util.toString(variablePaths)); //$NON-NLS-1$
		}

		int varLength = variableNames.length;
		
		// gather classpath information for updating
		final HashMap affectedProjectClasspaths = new HashMap(5);
		JavaModelManager manager = JavaModelManager.getJavaModelManager();
		IJavaModel model = manager.getJavaModel();
	
		// filter out unmodified variables
		int discardCount = 0;
		for (int i = 0; i < varLength; i++){
			String variableName = variableNames[i];
			IPath oldPath = JavaModelManager.variableGet(variableName); // if reentering will provide previous session value 
			if (oldPath == JavaModelManager.VariableInitializationInProgress){
				IPath previousPath = (IPath)JavaModelManager.PreviousSessionVariables.get(variableName);
				if (previousPath != null){
					if (JavaModelManager.CP_RESOLVE_VERBOSE){
						System.out.println("CPVariable INIT - reentering access to variable: " + variableName+ " during its initialization, will see previous value: "+ previousPath); //$NON-NLS-1$ //$NON-NLS-2$
					}
					JavaModelManager.variablePut(variableName, previousPath); // replace value so reentering calls are seeing old value
				}
				oldPath = null;  //33695 - cannot filter out restored variable, must update affected project to reset cached CP
			}
			if (oldPath != null && oldPath.equals(variablePaths[i])){
				variableNames[i] = null;
				discardCount++;
			}
		}
		if (discardCount > 0){
			if (discardCount == varLength) return;
			int changedLength = varLength - discardCount;
			String[] changedVariableNames = new String[changedLength];
			IPath[] changedVariablePaths = new IPath[changedLength];
			for (int i = 0, index = 0; i < varLength; i++){
				if (variableNames[i] != null){
					changedVariableNames[index] = variableNames[i];
					changedVariablePaths[index] = variablePaths[i];
					index++;
				}
			}
			variableNames = changedVariableNames;
			variablePaths = changedVariablePaths;
			varLength = changedLength;
		}
		
		if (monitor != null && monitor.isCanceled()) return;

		if (model != null) {
			IJavaProject[] projects = model.getJavaProjects();
			nextProject : for (int i = 0, projectLength = projects.length; i < projectLength; i++){
				IJavaProject project = projects[i];
						
				// check to see if any of the modified variables is present on the classpath
				IClasspathEntry[] classpath = project.getRawClasspath();
				for (int j = 0, cpLength = classpath.length; j < cpLength; j++){
					
					IClasspathEntry entry = classpath[j];
					for (int k = 0; k < varLength; k++){
	
						String variableName = variableNames[k];						
						if (entry.getEntryKind() ==  IClasspathEntry.CPE_VARIABLE){
	
							if (variableName.equals(entry.getPath().segment(0))){
								affectedProjectClasspaths.put(project, project.getResolvedClasspath(true));
								continue nextProject;
							}
							IPath sourcePath, sourceRootPath;
							if (((sourcePath = entry.getSourceAttachmentPath()) != null	&& variableName.equals(sourcePath.segment(0)))
								|| ((sourceRootPath = entry.getSourceAttachmentRootPath()) != null	&& variableName.equals(sourceRootPath.segment(0)))) {
	
								affectedProjectClasspaths.put(project, project.getResolvedClasspath(true));
								continue nextProject;
							}
						}												
					}
				}
			}
		}
		// update variables
		for (int i = 0; i < varLength; i++){
			JavaModelManager.variablePut(variableNames[i], variablePaths[i]);
		}
		final String[] dbgVariableNames = variableNames;
				
		// update affected project classpaths
		if (!affectedProjectClasspaths.isEmpty()) {
			try {
				final boolean canChangeResources = !ResourcesPlugin.getWorkspace().isTreeLocked();
				JavaCore.run(
					new IWorkspaceRunnable() {
						public void run(IProgressMonitor progressMonitor) throws CoreException {
							// propagate classpath change
							Iterator projectsToUpdate = affectedProjectClasspaths.keySet().iterator();
							while (projectsToUpdate.hasNext()) {
			
								if (progressMonitor != null && progressMonitor.isCanceled()) return;
			
								JavaProject affectedProject = (JavaProject) projectsToUpdate.next();

								if (JavaModelManager.CP_RESOLVE_VERBOSE){
									System.out.println("CPVariable SET  - updating affected project: ["+affectedProject.getElementName() //$NON-NLS-1$
										+"] due to setting variables: "+ org.eclipse.jdt.internal.compiler.util.Util.toString(dbgVariableNames)); //$NON-NLS-1$
								}

								affectedProject
									.setRawClasspath(
										affectedProject.getRawClasspath(),
										SetClasspathOperation.ReuseOutputLocation,
										null, // don't call beginTask on the monitor (see http://bugs.eclipse.org/bugs/show_bug.cgi?id=3717)
										canChangeResources, 
										(IClasspathEntry[]) affectedProjectClasspaths.get(affectedProject),
										false, // updating - no need for early validation
										false); // updating - no need to save
							}
						}
					},
					null/*no need to lock anything*/,
					monitor);
			} catch (CoreException e) {
				if (JavaModelManager.CP_RESOLVE_VERBOSE){
					System.out.println("CPVariable SET  - FAILED DUE TO EXCEPTION: " //$NON-NLS-1$
						+org.eclipse.jdt.internal.compiler.util.Util.toString(dbgVariableNames)); 
					e.printStackTrace();
				}
				if (e instanceof JavaModelException) {
					throw (JavaModelException)e;
				} else {
					throw new JavaModelException(e);
				}
			}
		}
	}
}