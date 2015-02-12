/*******************************************************************************
 * Copyright (c) 2015 GK Software AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.internal.compiler.classfmt.ExternalAnnotationProvider;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;
import org.eclipse.jdt.internal.core.ClasspathEntry;

/**
 * Utilities for accessing and manipulating text files that externally define annotations for a given Java type.
 * Files are assumed to be in ".eea format", a textual representation of annotated signatures of members of a given type.
 * 
 * @since 3.11
 * @noinstantiate This class is not intended to be instantiated by clients.
 */
public final class ExternalAnnotationUtil {

	/** Representation of a 'nullable' annotation, independent of the concrete annotation name used in Java sources. */
	public static final char NULLABLE = '0';

	/** Representation of a 'nonnull' annotation, independent of the concrete annotation name used in Java sources. */
	public static final char NONNULL = '1';

	/**
	 * Represents absence of a null annotation. Useful for removing an existing null annotation.
	 * This character is used only internally, it is not part of the Eclipse External Annotation file format.
	 */
	public static final char NO_ANNOTATION = '@';

	/** Strategy for merging a new signature with an existing (possibly annotated) signature. */
	public static enum MergeStrategy {
		/** Unconditionally replace the signature. */
		REPLACE_SIGNATURE,
		/** Override existing annotations, keeping old annotations in locations that are not annotated in the new signature. */
		OVERWRITE_ANNOTATIONS,
		/** Only add new annotations, never remove or overwrite existing annotations. */
		ADD_ANNOTATIONS
	}

	private static final int POSITION_RETURN_TYPE = -1;
	private static final int POSITION_FULL_SIGNATURE = -2;

	/**
	 * Answer the give method's signature in class file format.
	 * @param methodBinding binding representing a method
	 * @return a signature in class file format
	 */
	public static String extractGenericSignature(IMethodBinding methodBinding) {
		// Note that IMethodBinding.binding is not accessible, hence we need to reverse engineer from the key:
		
		// method key contains the signature between '(' and '|': "class.selector(params)return|throws"
		int open= methodBinding.getKey().indexOf('(');
		int throwStart= methodBinding.getKey().indexOf('|');
		return throwStart == -1 ? methodBinding.getKey().substring(open) : methodBinding.getKey().substring(open, throwStart);
	}

	/**
	 * Insert an encoded annotation into the given methodSignature affecting its return type.
	 * <p>
	 * This method is suitable for declaration annotations.
	 * </p>
	 * @param methodSignature a method signature in class file format
	 * @param annotation one of {@link #NULLABLE} and {@link #NONNULL}.
	 * @param mergeStrategy when passing {@link MergeStrategy#ADD_ANNOTATIONS} this method will
	 * 	refuse to overwrite any existing annotation in the specified location
	 * @return the modified method signature, or the original signature if modification would
	 *	conflict with the given merge strategy.
	 * @throws IllegalArgumentException if the method signature is malformed or its return type is not a reference type.
	 */
	public static String insertReturnAnnotation(String methodSignature, char annotation, MergeStrategy mergeStrategy) {
		int close = methodSignature.indexOf(')');
		if (close == -1 || close > methodSignature.length()-4)
			throw new IllegalArgumentException("Malformed method signature"); //$NON-NLS-1$
		switch (methodSignature.charAt(close+1)) {
			case 'L': case 'T': case '[':
				return insertAt(methodSignature, close+2, annotation, mergeStrategy);
		}
		throw new IllegalArgumentException("Return type is not a reference type"); //$NON-NLS-1$
	}

	/**
	 * Insert an encoded annotation into the given methodSignature affecting one of its parameters.
	 * <p>
	 * This method is suitable for declaration annotations.
	 * </p>
	 * @param methodSignature a method signature in class file format
	 * @param paramIdx 0-based index of the parameter to which the annotation should be attached
	 * @param annotation one of {@link #NULLABLE} and {@link #NONNULL}.
	 * @param mergeStrategy when passing {@link MergeStrategy#ADD_ANNOTATIONS} this method will
	 * 	refuse to overwrite any existing annotation in the specified location
	 * @return the modified method signature, or the original signature if modification would
	 *	conflict with the given merge strategy.
	 * @throws IllegalArgumentException if the method signature is malformed or its specified parameter type is not a reference type.
	 */
	public static String insertParameterAnnotation(String methodSignature, int paramIdx, char annotation, MergeStrategy mergeStrategy)
	{
		SignatureWrapper wrapper = new SignatureWrapper(methodSignature.toCharArray());
		wrapper.start = 1;
		for (int i = 0; i < paramIdx; i++)
			wrapper.start = wrapper.computeEnd() + 1;
		int start = wrapper.start;
		switch (methodSignature.charAt(start)) {
			case 'L': case 'T': case '[':
				return insertAt(methodSignature, start+1, annotation, mergeStrategy);
		}
		throw new IllegalArgumentException("Paramter type is not a reference type"); //$NON-NLS-1$
	}

	/**
	 * Answer the external annotation file corresponding to the given type as seen from the given project.
	 * Note that manipulation of external annotations is only supported for annotation files in the workspace,
	 * and only in directory layout, not from zip files.
	 * @param project current project that references the given type from a jar file.
	 * @param type the type for which external annotations are sought
	 * @param monitor progress monitor to be passed through into file operations
	 * @return a file assumed (but not checked) to be in .eea format. The file may not "exist".
	 * 	Can be null if the given type is not contained in a jar file for which an external annotation path
	 *  has been defined in the context of the given project.
	 * @throws CoreException Signals a problem in accessing any of the relevant elements: the project, the type,
	 * the containing jar file and finally the sought annotation file.
	 */
	public static IFile getAnnotationFile(IJavaProject project, ITypeBinding type, IProgressMonitor monitor) throws CoreException {
	
		IType targetType = project.findType(type.getErasure().getQualifiedName());
		if (!targetType.exists())
			return null;

		String binaryTypeName = targetType.getFullyQualifiedName('.').replace('.', '/');
		
		IPackageFragmentRoot packageRoot = (IPackageFragmentRoot) targetType.getAncestor(IJavaElement.PACKAGE_FRAGMENT_ROOT);
		IClasspathEntry entry = packageRoot.getResolvedClasspathEntry();
		IPath annotationPath = ClasspathEntry.getExternalAnnotationPath(entry, project.getProject(), false);
	
		if (annotationPath == null) 
			return null;
		IWorkspaceRoot workspaceRoot = project.getProject().getWorkspace().getRoot();
		IFile annotationZip = workspaceRoot.getFile(annotationPath);
		if (annotationZip.exists())
			return null;
	
		annotationPath = annotationPath.append(binaryTypeName).addFileExtension(ExternalAnnotationProvider.ANNOTION_FILE_EXTENSION);
		return workspaceRoot.getFile(annotationPath);
	}

	/**
	 * Update the given external annotation file with details regarding annotations of one specific method or field.
	 * If the specified member already has external annotations, old and new annotations will be merged,
	 * with priorities controlled by the parameter 'mergeStrategy'.
	 * <p>
	 * This method is suitable for declaration annotations and type use annotations.
	 * </p>
	 * @param typeName binary name (slash separated) of the type being annotated
	 * @param file a file assumed to be in .eea format, will be created if it doesn't exist.
	 * @param selector selector of the method or field
	 * @param originalSignature unannotated signature of the member, used for identification
	 * @param annotatedSignature new signatures whose annotations should be superimposed on the member
	 * @param mergeStrategy controls how old and new signatures should be merged
	 * @param monitor progress monitor to be passed through into file operations, or null if no reporting is desired
	 * @throws CoreException if access to the file fails
	 * @throws IOException if reading file content fails
	 */
	public static void annotateMember(String typeName, IFile file, String selector, String originalSignature, String annotatedSignature,
										MergeStrategy mergeStrategy, IProgressMonitor monitor)
			throws CoreException, IOException
	{
		annotateMember(typeName, file, selector, originalSignature, annotatedSignature, POSITION_FULL_SIGNATURE, mergeStrategy, monitor);
	}

	/**
	 * Update the given external annotation file with details regarding annotations of the return type of a given method.
	 * If the specified method already has external annotations, old and new annotations will be merged,
	 * with priorities controlled by the parameter 'mergeStrategy'.
	 * <p>
	 * This method is suitable for declaration annotations and type use annotations.
	 * </p>
	 * @param typeName binary name (slash separated) of the type being annotated
	 * @param file a file assumed to be in .eea format, will be created if it doesn't exist.
	 * @param selector selector of the method
	 * @param originalSignature unannotated signature of the member, used for identification
	 * @param annotatedReturnType signature of the new return type whose annotations should be superimposed on the method
	 * @param mergeStrategy controls how old and new signatures should be merged
	 * @param monitor progress monitor to be passed through into file operations, or null if no reporting is desired
	 * @throws CoreException if access to the file fails
	 * @throws IOException if reading file content fails
	 */
	public static void annotateMethodReturnType(String typeName, IFile file, String selector, String originalSignature,
										String annotatedReturnType, MergeStrategy mergeStrategy, IProgressMonitor monitor)
			throws CoreException, IOException
	{
		annotateMember(typeName, file, selector, originalSignature, annotatedReturnType, POSITION_RETURN_TYPE, mergeStrategy, monitor);
	}

	static void annotateMember(String typeName, IFile file, String selector, String originalSignature, String annotatedSignature,
										int updatePosition, MergeStrategy mergeStrategy, IProgressMonitor monitor)
			throws CoreException, IOException
	{

		if (!file.exists()) {
			StringBuffer newContent= new StringBuffer();
			// header:
			newContent.append(ExternalAnnotationProvider.CLASS_PREFIX);
			newContent.append(typeName).append('\n');
			// new entry:
			newContent.append(selector).append('\n');
			newContent.append(' ').append(originalSignature).append('\n');
			newContent.append(' ').append(annotatedSignature).append('\n');

			createNewFile(file, newContent.toString(), monitor);
		} else {
			BufferedReader reader = new BufferedReader(new InputStreamReader(file.getContents()));
			StringBuffer newContent = new StringBuffer();
			try {
				newContent.append(reader.readLine()).append('\n'); // skip class name
				String line;
				while ((line = reader.readLine()) != null) {
					if (line.isEmpty()) {
						newContent.append('\n');
						continue;
					}
					if (!Character.isJavaIdentifierStart(line.charAt(0))) {
						newContent.append(line).append('\n');
						continue;
					}
					// compare selectors:
					int relation = line.compareTo(selector);
					if (relation > 0) { // past the insertion point
						break;
					}
					if (relation < 0) {
						newContent.append(line).append('\n');
						continue;
					}
					if (relation == 0) {
						StringBuffer pending = new StringBuffer(line).append('\n');
						pending.append(line = reader.readLine());
						// compare original signatures:
						relation = line.trim().compareTo(originalSignature);
						if (relation > 0) { // past the insertion point
							// add new entry (below)
							line = pending.toString(); // push back
							break;
						}
						newContent.append(pending).append('\n');
						if (relation < 0)
							continue;
						if (relation == 0) {
							// update existing entry:
							String nextLine = reader.readLine();
							if (nextLine == null)
								nextLine = line; // no annotated line yet, use unannotated line instead
							if (nextLine.startsWith(" ")) { //$NON-NLS-1$
								switch (mergeStrategy) {
									case REPLACE_SIGNATURE:
										break; // unconditionally use annotatedSignature
									case OVERWRITE_ANNOTATIONS:
									case ADD_ANNOTATIONS:
										if (updatePosition == POSITION_FULL_SIGNATURE) {
											annotatedSignature = addAnnotationsTo(annotatedSignature, nextLine.trim(), mergeStrategy);
										} else if (updatePosition == POSITION_RETURN_TYPE) {
											annotatedSignature = updateMethodReturnType(annotatedSignature, nextLine.trim(), mergeStrategy);
										} else {
											// parameter i
										}
										break;
									default:
										JavaCore.getJavaCore().getLog().log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID,
																				"Unexpected value for enum MergeStrategy")); //$NON-NLS-1$
								}
								nextLine = null; // discard old annotated signature (may have been merged above)
							}
							writeFile(file, newContent, annotatedSignature, nextLine, reader, monitor);
							return;
						}
					}
				}
				// add new entry:
				newContent.append(selector).append('\n');
				newContent.append(' ').append(originalSignature).append('\n');
				if (updatePosition == POSITION_FULL_SIGNATURE) {
					// annotatedSignature is already complete
				} else if (updatePosition == POSITION_RETURN_TYPE) {
					annotatedSignature = updateMethodReturnType(annotatedSignature, originalSignature, mergeStrategy);
				} else {
					// parameter i
				}
				writeFile(file, newContent, annotatedSignature, line, reader, monitor);
			} finally {
				reader.close();
			}
		}
	}

	/**
	 * Insert that given annotation at the given position into the given signature. 
	 * @param mergeStrategy if set to {@link MergeStrategy#ADD_ANNOTATIONS}, refuse to
	 *   overwrite any existing annotation in the specified location.
	 */
	private static String insertAt(String signature, int position, char annotation, MergeStrategy mergeStrategy) {
		StringBuffer result = new StringBuffer();
		result.append(signature, 0, position);
		result.append(annotation);
		char next = signature.charAt(position);
		switch (next) {
			case NULLABLE: case NONNULL:
				if (mergeStrategy == MergeStrategy.ADD_ANNOTATIONS)
					return signature; // refuse any change
				position++; // skip old annotation
		}
		result.append(signature, position, signature.length());
		return result.toString();
	}

	private static String addAnnotationsTo(String newSignature, String oldSignature, MergeStrategy mergeStategy) {
		// TODO: consider rewrite using updateType() below
		StringBuffer buf = new StringBuffer();
		assert newSignature.charAt(0) == '(' : "signature must start with '('"; //$NON-NLS-1$
		assert oldSignature.charAt(0) == '(' : "signature must start with '('"; //$NON-NLS-1$
		buf.append('(');
		SignatureWrapper wrapperNew = new SignatureWrapper(newSignature.toCharArray(), true); // when using annotations we must be at 1.5+
		wrapperNew.start = 1;
		SignatureWrapper wrapperOld = new SignatureWrapper(oldSignature.toCharArray(), true);
		wrapperOld.start = 1;
		while (!wrapperNew.atEnd() && !wrapperOld.atEnd()) {
			int startNew = wrapperNew.start;
			int startOld = wrapperOld.start;
			if (wrapperNew.signature[startNew] == ')') {
				if (wrapperOld.signature[startOld] != ')')
					throw new IllegalArgumentException("Structural difference between signatures "+newSignature+" and "+oldSignature);  //$NON-NLS-1$//$NON-NLS-2$
				startNew = ++wrapperNew.start;
				startOld = ++wrapperOld.start;
				buf.append(')');
			}			
			int endNew = wrapperNew.computeEnd();
			int endOld = wrapperOld.computeEnd();
			int lenNew = endNew-startNew+1;
			int lenOld = endOld-startOld+1;
			 // TODO detailed comparison / merging:
			if (lenNew == lenOld) {
				switch (mergeStategy) {
					case OVERWRITE_ANNOTATIONS:
						buf.append(wrapperNew.signature, startNew, lenNew);
						break;
					case ADD_ANNOTATIONS:
						buf.append(wrapperOld.signature, startOld, lenOld);
						break;
					//$CASES-OMITTED$ should only be called with the two strategies handled above
					default:
						JavaCore.getJavaCore().getLog().log(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID,
																"Unexpected value for enum MergeStrategy")); //$NON-NLS-1$
				}
			} else if (lenNew > lenOld) {
				buf.append(wrapperNew.signature, startNew, lenNew);
			} else {				
				buf.append(wrapperOld.signature, startOld, lenOld);
			}
		}
		return buf.toString();
	}

	private static String updateMethodReturnType(String newReturnType, String oldSignature, MergeStrategy mergeStrategy) {
		StringBuffer buf = new StringBuffer();
		assert oldSignature.charAt(0) == '(' : "signature must start with '('"; //$NON-NLS-1$
		int close = oldSignature.indexOf(')');
		buf.append(oldSignature, 0, close+1);
		updateType(buf, oldSignature.substring(close+1).toCharArray(), newReturnType.toCharArray(), mergeStrategy);
		return buf.toString();
	}

	/**
	 * Update 'oldType' with annotations from 'newType' guided by 'mergeStrategy'.
	 * The result is written into 'buf' as we go.
	 */
	private static boolean updateType(StringBuffer buf, char[] oldType, char[] newType, MergeStrategy mergeStrategy) {
		SignatureWrapper oWrap = new SignatureWrapper(oldType, true);
		SignatureWrapper nWrap = new SignatureWrapper(newType, true);
		if (match(buf, oWrap, nWrap, 'L', false)
			|| match(buf, oWrap, nWrap, 'T', false))
		{
			mergeAnnotation(buf, oWrap, nWrap, mergeStrategy);
			buf.append(oWrap.nextName());
			nWrap.nextName(); // skip
			if (match(buf, oWrap, nWrap, '<', false)) {
				do {
					int oStart = oWrap.start;
					int nStart = nWrap.start;
					oWrap.computeEnd();
					nWrap.computeEnd();
					if (updateType(buf, oWrap.getFrom(oStart), nWrap.getFrom(nStart), mergeStrategy))
						mergeAnnotation(buf, oWrap, nWrap, mergeStrategy);
				} while (!match(buf, oWrap, nWrap, '>', false));
			}
			match(buf, oWrap, nWrap, ';', true);
		} else if (match(buf, oWrap, nWrap, '[', false)) {
			mergeAnnotation(buf, oWrap, nWrap, mergeStrategy);
			updateType(buf, oWrap.tail(), nWrap.tail(), mergeStrategy);
		} else if (match(buf, oWrap, nWrap, '*', false)
				|| match(buf, oWrap, nWrap, '+', false)
				|| match(buf, oWrap, nWrap, '-', false))
		{
			return true; // annotation allowed after this (not included in oldType / newType)
		} else {			
			buf.append(oldType);
		}
		return false;
	}
	/**
	 * Does the current char at both given signatures match the 'expected' char?
	 * If yes, print it into 'buf' and answer true.
	 * If no, if 'force' raise an exception, else quietly answer false without updating 'buf'.
	 */
	private static boolean match(StringBuffer buf, SignatureWrapper sig1, SignatureWrapper sig2, char expected, boolean force) {
		boolean match1 = sig1.signature[sig1.start] == expected;
		boolean match2 = sig2.signature[sig2.start] == expected;
		if (match1 != match2) {
			throw new IllegalArgumentException("Mismatching type structures" //$NON-NLS-1$
					+ new String(sig1.signature)+" vs "+new String(sig2.signature)); //$NON-NLS-1$ 
		}
		if (match1) {
			buf.append(expected);
			sig1.start++;
			sig2.start++;
			return true;
		} else if (force) {
			throw new IllegalArgumentException("Expected char "+expected+" not found in "+new String(sig1.signature)); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			return false;
		}
	}

	/**
	 * If a current char of 'oldS' and/or 'newS' represents a null annotation, insert it into 'buf' guided by 'mergeStrategy'.
	 * If the new char is NO_ANNOTATION and strategy is OVERWRITE_ANNOTATIONS, silently skip over any null annotations in 'oldS'. 
	 */
	private static void mergeAnnotation(StringBuffer buf, SignatureWrapper oldS, SignatureWrapper newS, MergeStrategy mergeStrategy) {
		 // if atEnd use a char that's different from NULLABLE, NONNULL and NO_ANNOTATION:
		char oldAnn = !oldS.atEnd() ? oldS.signature[oldS.start] : '\0';
		char newAnn = !newS.atEnd() ? newS.signature[newS.start] : '\0';
		switch (mergeStrategy) {
			case ADD_ANNOTATIONS:
				switch (oldAnn) {
					case NULLABLE: case NONNULL:
						oldS.start++;
						buf.append(oldAnn); // old exists, so it remains
						switch (newAnn) { case NULLABLE: case NONNULL: newS.start++; } // just skip
						return;
				}
				//$FALL-THROUGH$
			case OVERWRITE_ANNOTATIONS:
				switch (newAnn) {
					case NULLABLE: case NONNULL:
						newS.start++;
						buf.append(newAnn); // new exists and is not suppressed by "ADD & old exists"
						switch (oldAnn) { case NULLABLE: case NONNULL: oldS.start++; } // just skip
						break;
					case NO_ANNOTATION:
						newS.start++; // don't insert
						switch (oldAnn) { case NULLABLE: case NONNULL: oldS.start++; } // just skip // skip
						break;
				}
		}
	}

	/**
	 * Write back the given annotationFile, with the following content:
	 * - head (assumed to include a member and its original signature
	 * - annotatedSignature
	 * - nextLines (optionally, may be null)
	 * - the still unconsumed content of tailReader
	 */
	private static void writeFile(IFile annotationFile, StringBuffer head, String annotatedSignature,
									String nextLines, BufferedReader tailReader, IProgressMonitor monitor)
			throws CoreException, IOException
	{
		head.append(' ').append(annotatedSignature).append('\n'); 
		if (nextLines != null)
			head.append(nextLines).append('\n');
		String line;
		while ((line = tailReader.readLine()) != null)
			head.append(line).append('\n');
		ByteArrayInputStream newContent = new ByteArrayInputStream(head.toString().getBytes("UTF-8")); //$NON-NLS-1$
		annotationFile.setContents(newContent, IResource.KEEP_HISTORY, monitor);
	}

	private static void createNewFile(IFile file, String newContent, IProgressMonitor monitor) throws CoreException {
		ensureExists(file.getParent(), monitor);
		
		try {
			file.create(new ByteArrayInputStream(newContent.getBytes("UTF-8")), false, monitor); //$NON-NLS-1$
		} catch (UnsupportedEncodingException e) {
			throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, e.getMessage(), e));
		}
	}

	private static void ensureExists(IContainer container, IProgressMonitor monitor) throws CoreException {
		if (container.exists()) return;
		if (!(container instanceof IFolder)) throw new CoreException(new Status(IStatus.ERROR, JavaCore.PLUGIN_ID, "not a folder: "+container)); //$NON-NLS-1$
		IContainer parent= container.getParent();
		if (parent instanceof IFolder) {
			ensureExists(parent, monitor);
		}
		((IFolder) container).create(false, true, monitor);
	}
}
