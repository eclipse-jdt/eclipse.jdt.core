/*******************************************************************************
 * Copyright (c) 2006 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.compiler.tool;

import java.util.StringTokenizer;

/**
 * Class used to handle options in the EclipseFileManager and the EclipseCompiler
 */
public final class Options {
	public static int processOptionsFileManager(String option) {
		if (option == null) return -1;
		if ("-encoding".equals(option)) {
			return 1;
		}
		if ("-d".equals(option)) {
			return 1;
		}
		if ("-classpath".equals(option)
				|| "-cp".equals(option)) {
			return 1;
		}
		if ("-bootclasspath".equals(option)) {
			return 1;
		}
		if ("-sourcepath".equals(option)) {
			return 1;
		}
		if ("-extdirs".equals(option)) {
			return 1;
		}
		if ("-endorseddirs".equals(option)) {
			return 1;
		}
		return -1;
	}

	public static int processOptions(String option) {
		if (option == null) return -1;
		if ("-log".equals(option)) {
			return 1;
		}
		if ("-repeat".equals(option)) {
			return 1;
		}
		if ("-maxProblems".equals(option)) {
			return 1;
		}
		if ("-source".equals(option)) {
			return 1;
		}
		if ("-encoding".equals(option)) {
			return 1;
		}
		if ("-1.3".equals(option)) {
			return 0;
		}
		if ("-1.4".equals(option)) {
			return 0;
		}
		if ("-1.5".equals(option) || "5".equals(option) || "-5.0".equals(option)) {
			return 0;
		}
		if ("-1.6".equals(option) || "6".equals(option) || "-6.0".equals(option)) {
			return 0;
		}
		if ("-d".equals(option)) {
			return 1;
		}
		if ("-classpath".equals(option)
				|| "-cp".equals(option)) {
			return 1;
		}
		if ("-bootclasspath".equals(option)) {
			return 1;
		}
		if ("-sourcepath".equals(option)) {
			return 1;
		}
		if ("-extdirs".equals(option)) {
			return 1;
		}
		if ("-endorseddirs".equals(option)) {
			return 1;
		}
		if ("progress".equals(option)) {
			return 0;
		}
		if ("-proceedOnError".equals(option)) {
			return 0;
		}
		if ("-time".equals(option)) {
			return 0;
		}
		if ("-v".equals(option) || "-version".equals(option)) {
			return 0;
		}
		if ("-showversion".equals(option)) {
			return 0;
		}
		if ("-deprecation".equals(option)) {
			return 0;
		}
		if ("-help".equals(option) || "-?".equals(option)) {
			return 0;
		}
		if ("-help:warn".equals(option) || "-?:warn".equals(option)) {
			return 0;
		}
		if ("-noExit".equals(option)) {
			return 0;
		}
		if ("-verbose".equals(option)) {
			return 0;
		}
		if ("-referenceInfo".equals(option)) {
			return 0;
		}
		if ("-inlineJSR".equals(option)) {
			return 0;
		}
		if (option.startsWith("-g")) { //$NON-NLS-1$
			int length = option.length();
			if (length == 2) {
				return 0;
			}
			if (length > 3) {
				if (length == 7 && option.equals("-g:none"))
					return 0;
				StringTokenizer tokenizer =
					new StringTokenizer(option.substring(3, option.length()), ",");
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if ("vars".equals(token) || "lines".equals(token) || "source".equals(token)) {
						continue;
					}
					return -1;
				}
				return 0;
			}
			return -1;
		}
		if ("-nowarn".equals(option)) {
			return 0;
		}
		if (option.startsWith("-warn")) {
			int length = option.length();
			if (length == 10 && option.equals("-warn:none")) { //$NON-NLS-1$
				return 0;
			}
			if (length <= 6) {
				return -1;
			}
			int warnTokenStart;
			switch (option.charAt(6)) {
				case '+' : 
					warnTokenStart = 7;
					break;
				case '-' :
					warnTokenStart = 7;
					break;
				default:
					warnTokenStart = 6;
			}
		
			StringTokenizer tokenizer =
				new StringTokenizer(option.substring(warnTokenStart, option.length()), ","); //$NON-NLS-1$
			int tokenCounter = 0;

			while (tokenizer.hasMoreTokens()) {
				String token = tokenizer.nextToken();
				tokenCounter++;
				if ("constructorName".equals(token)
						|| token.equals("pkgDefaultMethod")
						|| token.equals("packageDefaultMethod")
						|| token.equals("maskedCatchBlock")
						|| token.equals("maskedCatchBlocks")
						|| token.equals("deprecation")
						|| token.equals("allDeprecation")
						|| token.equals("unusedLocal")
						|| token.equals("unusedLocals")
						|| token.equals("unusedArgument")
						|| token.equals("unusedArguments")
						|| token.equals("unusedImport")
						|| token.equals("unusedImports")
						|| token.equals("unusedPrivate")
						|| token.equals("unusedLabel")
						|| token.equals("localHiding")
						|| token.equals("fieldHiding")
						|| token.equals("specialParamHiding")
						|| token.equals("conditionAssign")
						|| token.equals("syntheticAccess")
						|| token.equals("synthetic-access")
						|| token.equals("nls")
						|| token.equals("staticReceiver")
						|| token.equals("indirectStatic")
						|| token.equals("noEffectAssign")
						|| token.equals("intfNonInherited")
						|| token.equals("interfaceNonInherited")
						|| token.equals("charConcat")
						|| token.equals("noImplicitStringConversion")
						|| token.equals("semicolon")
						|| token.equals("serial")
						|| token.equals("emptyBlock")
						|| token.equals("uselessTypeCheck")
						|| token.equals("unchecked")
						|| token.equals("unsafe")
						|| token.equals("raw")
						|| token.equals("finalBound")
						|| token.equals("suppress")
						|| token.equals("warningToken")
						|| token.equals("unnecessaryElse")
						|| token.equals("javadoc")
						|| token.equals("allJavadoc")
						|| token.equals("assertIdentifier")
						|| token.equals("enumIdentifier")
						|| token.equals("finally")
						|| token.equals("unusedThrown")
						|| token.equals("unqualifiedField")
						|| token.equals("unqualified-field-access")
						|| token.equals("typeHiding")
						|| token.equals("varargsCast")
						|| token.equals("null")
						|| token.equals("boxing")
						|| token.equals("over-ann")
						|| token.equals("dep-ann")
						|| token.equals("intfAnnotation")
						|| token.equals("enumSwitch")
						|| token.equals("incomplete-switch")
						|| token.equals("hiding")
						|| token.equals("static-access")
						|| token.equals("unused")
						|| token.equals("paramAssign")
						|| token.equals("discouraged")
						|| token.equals("forbidden")
						|| token.equals("fallthrough")) {
					continue;
    			} else if (token.equals("tasks")) {
    				String taskTags = "";
    				int start = token.indexOf('(');
    				int end = token.indexOf(')');
    				if (start >= 0 && end >= 0 && start < end){
    					taskTags = token.substring(start+1, end).trim();
    					taskTags = taskTags.replace('|',',');
    				}
    				if (taskTags.length() == 0){
    					return -1;
    				}
    				continue;
    			} else {
    				return -1;
    			}
			}
			if (tokenCounter == 0) {
				return -1;
			} else {
				return 0;
			}
		}
		if ("-target".equals(option)) {
			return 1;
		}
		if ("-preserveAllLocals".equals(option)) {
			return 0;
		}
		if ("-enableJavadoc".equals(option)) {
			return 0;
		}
		if ("-Xemacs".equals(option)) {
			return 0;
		}
		if ("-X".equals(option)) {
			return 0;
		}
		if ("-J".equals(option)) {
			return 0;
		}
		if ("-O".equals(option)) {
			return 0;
		}
		return -1;
	}
}
