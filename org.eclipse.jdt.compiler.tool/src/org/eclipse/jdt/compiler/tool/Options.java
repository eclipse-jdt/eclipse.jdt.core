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
		if ("-encoding".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-d".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-classpath".equals(option)//$NON-NLS-1$
				|| "-cp".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-bootclasspath".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-sourcepath".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-extdirs".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-endorseddirs".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		return -1;
	}

	public static int processOptions(String option) {
		if (option == null) return -1;
		if ("-log".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-repeat".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-maxProblems".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-source".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-encoding".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-1.3".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-1.4".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-1.5".equals(option) || "5".equals(option) || "-5.0".equals(option)) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			return 0;
		}
		if ("-1.6".equals(option) || "6".equals(option) || "-6.0".equals(option)) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
			return 0;
		}
		if ("-d".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-classpath".equals(option)//$NON-NLS-1$
				|| "-cp".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-bootclasspath".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-sourcepath".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-extdirs".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-endorseddirs".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("progress".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-proceedOnError".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-time".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-v".equals(option) || "-version".equals(option)) {//$NON-NLS-1$//$NON-NLS-2$
			return 0;
		}
		if ("-showversion".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-deprecation".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-help".equals(option) || "-?".equals(option)) {//$NON-NLS-1$//$NON-NLS-2$
			return 0;
		}
		if ("-help:warn".equals(option) || "-?:warn".equals(option)) {//$NON-NLS-1$//$NON-NLS-2$
			return 0;
		}
		if ("-noExit".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-verbose".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-referenceInfo".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-inlineJSR".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if (option.startsWith("-g")) { //$NON-NLS-1$
			int length = option.length();
			if (length == 2) {
				return 0;
			}
			if (length > 3) {
				if (length == 7 && option.equals("-g:none"))//$NON-NLS-1$
					return 0;
				StringTokenizer tokenizer =
					new StringTokenizer(option.substring(3, option.length()), ",");//$NON-NLS-1$
				while (tokenizer.hasMoreTokens()) {
					String token = tokenizer.nextToken();
					if ("vars".equals(token) || "lines".equals(token) || "source".equals(token)) {//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
						continue;
					}
					return -1;
				}
				return 0;
			}
			return -1;
		}
		if ("-nowarn".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if (option.startsWith("-warn")) {//$NON-NLS-1$
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
				if ("constructorName".equals(token)//$NON-NLS-1$
						|| token.equals("pkgDefaultMethod")//$NON-NLS-1$
						|| token.equals("packageDefaultMethod")//$NON-NLS-1$
						|| token.equals("maskedCatchBlock")//$NON-NLS-1$
						|| token.equals("maskedCatchBlocks")//$NON-NLS-1$
						|| token.equals("deprecation")//$NON-NLS-1$
						|| token.equals("allDeprecation")//$NON-NLS-1$
						|| token.equals("unusedLocal")//$NON-NLS-1$
						|| token.equals("unusedLocals")//$NON-NLS-1$
						|| token.equals("unusedArgument")//$NON-NLS-1$
						|| token.equals("unusedArguments")//$NON-NLS-1$
						|| token.equals("unusedImport")//$NON-NLS-1$
						|| token.equals("unusedImports")//$NON-NLS-1$
						|| token.equals("unusedPrivate")//$NON-NLS-1$
						|| token.equals("unusedLabel")//$NON-NLS-1$
						|| token.equals("localHiding")//$NON-NLS-1$
						|| token.equals("fieldHiding")//$NON-NLS-1$
						|| token.equals("specialParamHiding")//$NON-NLS-1$
						|| token.equals("conditionAssign")//$NON-NLS-1$
						|| token.equals("syntheticAccess")//$NON-NLS-1$
						|| token.equals("synthetic-access")//$NON-NLS-1$
						|| token.equals("nls")//$NON-NLS-1$
						|| token.equals("staticReceiver")//$NON-NLS-1$
						|| token.equals("indirectStatic")//$NON-NLS-1$
						|| token.equals("noEffectAssign")//$NON-NLS-1$
						|| token.equals("intfNonInherited")//$NON-NLS-1$
						|| token.equals("interfaceNonInherited")//$NON-NLS-1$
						|| token.equals("charConcat")//$NON-NLS-1$
						|| token.equals("noImplicitStringConversion")//$NON-NLS-1$
						|| token.equals("semicolon")//$NON-NLS-1$
						|| token.equals("serial")//$NON-NLS-1$
						|| token.equals("emptyBlock")//$NON-NLS-1$
						|| token.equals("uselessTypeCheck")//$NON-NLS-1$
						|| token.equals("unchecked")//$NON-NLS-1$
						|| token.equals("unsafe")//$NON-NLS-1$
						|| token.equals("raw")//$NON-NLS-1$
						|| token.equals("finalBound")//$NON-NLS-1$
						|| token.equals("suppress")//$NON-NLS-1$
						|| token.equals("warningToken")//$NON-NLS-1$
						|| token.equals("unnecessaryElse")//$NON-NLS-1$
						|| token.equals("javadoc")//$NON-NLS-1$
						|| token.equals("allJavadoc")//$NON-NLS-1$
						|| token.equals("assertIdentifier")//$NON-NLS-1$
						|| token.equals("enumIdentifier")//$NON-NLS-1$
						|| token.equals("finally")//$NON-NLS-1$
						|| token.equals("unusedThrown")//$NON-NLS-1$
						|| token.equals("unqualifiedField")//$NON-NLS-1$
						|| token.equals("unqualified-field-access")//$NON-NLS-1$
						|| token.equals("typeHiding")//$NON-NLS-1$
						|| token.equals("varargsCast")//$NON-NLS-1$
						|| token.equals("null")//$NON-NLS-1$
						|| token.equals("boxing")//$NON-NLS-1$
						|| token.equals("over-ann")//$NON-NLS-1$
						|| token.equals("dep-ann")//$NON-NLS-1$
						|| token.equals("intfAnnotation")//$NON-NLS-1$
						|| token.equals("enumSwitch")//$NON-NLS-1$
						|| token.equals("incomplete-switch")//$NON-NLS-1$
						|| token.equals("hiding")//$NON-NLS-1$
						|| token.equals("static-access")//$NON-NLS-1$
						|| token.equals("unused")//$NON-NLS-1$
						|| token.equals("paramAssign")//$NON-NLS-1$
						|| token.equals("discouraged")//$NON-NLS-1$
						|| token.equals("forbidden")//$NON-NLS-1$
						|| token.equals("fallthrough")) {//$NON-NLS-1$
					continue;
    			} else if (token.equals("tasks")) {//$NON-NLS-1$
    				String taskTags = "";//$NON-NLS-1$
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
		if ("-target".equals(option)) {//$NON-NLS-1$
			return 1;
		}
		if ("-preserveAllLocals".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-enableJavadoc".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-Xemacs".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-X".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-J".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		if ("-O".equals(option)) {//$NON-NLS-1$
			return 0;
		}
		return -1;
	}
}
