/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.util.Util;

/**
 * @see IJavaModelStatus
 */

public class JavaModelStatus extends Status implements IJavaModelStatus, IJavaModelStatusConstants, IResourceStatus {

	/**
	 * The elements related to the failure, or <code>null</code>
	 * if no elements are involved.
	 */
	protected IJavaElement[] elements = new IJavaElement[0];
	/**
	 * The path related to the failure, or <code>null</code>
	 * if no path is involved.
	 */
	protected IPath path;
	/**
	 * The <code>String</code> related to the failure, or <code>null</code>
	 * if no <code>String</code> is involved.
	 */
	protected String string;
	/**
	 * Empty children
	 */
	protected final static IStatus[] NO_CHILDREN = new IStatus[] {};
	protected IStatus[] children= NO_CHILDREN;

	/**
	 * Singleton OK object
	 */
	public static final IJavaModelStatus VERIFIED_OK = new JavaModelStatus(OK, OK, Util.bind("status.OK")); //$NON-NLS-1$

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus() {
		// no code for an multi-status
		super(ERROR, JavaCore.PLUGIN_ID, 0, "JavaModelStatus", null); //$NON-NLS-1$
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * elements.
	 */
	public JavaModelStatus(int code, IJavaElement[] elements) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		this.elements= elements;
		this.path= null;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, String string) {
		this(ERROR, code, string);
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int severity, int code, String string) {
		super(severity, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
		this.path= null;
		this.string = string;
	}	
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, Throwable throwable) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", throwable); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
	}
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, IPath path) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null); //$NON-NLS-1$
		this.elements= JavaElement.NO_ELEMENTS;
		this.path= path;
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element.
	 */
	public JavaModelStatus(int code, IJavaElement element) {
		this(code, new IJavaElement[]{element});
	}
	/**
	 * Constructs an Java model status with the given corresponding
	 * element and string
	 */
	public JavaModelStatus(int code, IJavaElement element, String string) {
		this(code, new IJavaElement[]{element});
		this.string = string;
	}
	
	/**
	 * Constructs an Java model status with the given corresponding
	 * element and path
	 */
	public JavaModelStatus(int code, IJavaElement element, IPath path) {
		this(code, new IJavaElement[]{element});
		this.path = path;
	}	
	/**
	 * Constructs an Java model status with the given corresponding
	 * element, path and string
	 */
	public JavaModelStatus(int code, IJavaElement element, IPath path, String string) {
		this(code, new IJavaElement[]{element});
		this.path = path;
		this.string = string;
	}	
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(CoreException coreException) {
		super(ERROR, JavaCore.PLUGIN_ID, CORE_EXCEPTION, "JavaModelStatus", coreException); //$NON-NLS-1$
		elements= JavaElement.NO_ELEMENTS;
	}
	protected int getBits() {
		int severity = 1 << (getCode() % 100 / 33);
		int category = 1 << ((getCode() / 100) + 3);
		return severity | category;
	}
	/**
	 * @see IStatus
	 */
	public IStatus[] getChildren() {
		return children;
	}
	/**
	 * @see IJavaModelStatus
	 */
	public IJavaElement[] getElements() {
		return elements;
	}
	/**
	 * Returns the message that is relevant to the code of this status.
	 */
	public String getMessage() {
		Throwable exception = getException();
		if (exception == null) {
			switch (getCode()) {
				case CORE_EXCEPTION :
					return Util.bind("status.coreException"); //$NON-NLS-1$

				case BUILDER_INITIALIZATION_ERROR:
					return Util.bind("build.initializationError"); //$NON-NLS-1$

				case BUILDER_SERIALIZATION_ERROR:
					return Util.bind("build.serializationError"); //$NON-NLS-1$

				case DEVICE_PATH:
					return Util.bind("status.cannotUseDeviceOnPath", getPath().toString()); //$NON-NLS-1$

				case DOM_EXCEPTION:
					return Util.bind("status.JDOMError"); //$NON-NLS-1$

				case ELEMENT_DOES_NOT_EXIST:
					return Util.bind("element.doesNotExist",((JavaElement)elements[0]).toStringWithAncestors()); //$NON-NLS-1$

				case EVALUATION_ERROR:
					return Util.bind("status.evaluationError", string); //$NON-NLS-1$

				case INDEX_OUT_OF_BOUNDS:
					return Util.bind("status.indexOutOfBounds"); //$NON-NLS-1$

				case INVALID_CONTENTS:
					return Util.bind("status.invalidContents"); //$NON-NLS-1$

				case INVALID_DESTINATION:
					return Util.bind("status.invalidDestination", ((JavaElement)elements[0]).toStringWithAncestors()); //$NON-NLS-1$

				case INVALID_ELEMENT_TYPES:
					StringBuffer buff= new StringBuffer(Util.bind("operation.notSupported")); //$NON-NLS-1$
					for (int i= 0; i < elements.length; i++) {
						if (i > 0) {
							buff.append(", "); //$NON-NLS-1$
						}
						buff.append(((JavaElement)elements[i]).toStringWithAncestors());
					}
					return buff.toString();

				case INVALID_NAME:
					return Util.bind("status.invalidName", string); //$NON-NLS-1$

				case INVALID_PACKAGE:
					return Util.bind("status.invalidPackage", string); //$NON-NLS-1$

				case INVALID_PATH:
					if (string != null) {
						return string;
					} else {
						return Util.bind("status.invalidPath", getPath() == null ? "null" : getPath().toString()); //$NON-NLS-1$ //$NON-NLS-2$
					}

				case INVALID_PROJECT:
					return Util.bind("status.invalidProject", string); //$NON-NLS-1$

				case INVALID_RESOURCE:
					return Util.bind("status.invalidResource", string); //$NON-NLS-1$

				case INVALID_RESOURCE_TYPE:
					return Util.bind("status.invalidResourceType", string); //$NON-NLS-1$

				case INVALID_SIBLING:
					if (string != null) {
						return Util.bind("status.invalidSibling", string); //$NON-NLS-1$
					} else {
						return Util.bind("status.invalidSibling", ((JavaElement)elements[0]).toStringWithAncestors()); //$NON-NLS-1$
					}

				case IO_EXCEPTION:
					return Util.bind("status.IOException"); //$NON-NLS-1$

				case NAME_COLLISION:
					if (elements != null && elements.length > 0) {
						IJavaElement element = elements[0];
						if (element instanceof PackageFragment && ((PackageFragment) element).isDefaultPackage()) {
							return Util.bind("operation.cannotRenameDefaultPackage"); //$NON-NLS-1$
						}
					}
					if (string != null) {
						return string;
					} else {
						return Util.bind("status.nameCollision", ""); //$NON-NLS-1$ //$NON-NLS-2$
					}
				case NO_ELEMENTS_TO_PROCESS:
					return Util.bind("operation.needElements"); //$NON-NLS-1$

				case NULL_NAME:
					return Util.bind("operation.needName"); //$NON-NLS-1$

				case NULL_PATH:
					return Util.bind("operation.needPath"); //$NON-NLS-1$

				case NULL_STRING:
					return Util.bind("operation.needString"); //$NON-NLS-1$

				case PATH_OUTSIDE_PROJECT:
					return Util.bind("operation.pathOutsideProject", string, ((JavaElement)elements[0]).toStringWithAncestors()); //$NON-NLS-1$

				case READ_ONLY:
					IJavaElement element = elements[0];
					String name = element.getElementName();
					if (element instanceof IPackageFragment && name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						return Util.bind("status.defaultPackageReadOnly"); //$NON-NLS-1$
					}
					return  Util.bind("status.readOnly", name); //$NON-NLS-1$

				case RELATIVE_PATH:
					return Util.bind("operation.needAbsolutePath", getPath().toString()); //$NON-NLS-1$

				case TARGET_EXCEPTION:
					return Util.bind("status.targetException"); //$NON-NLS-1$

				case UPDATE_CONFLICT:
					return Util.bind("status.updateConflict"); //$NON-NLS-1$

				case NO_LOCAL_CONTENTS :
					return Util.bind("status.noLocalContents", getPath().toString()); //$NON-NLS-1$

				case CP_CONTAINER_PATH_UNBOUND:
					IJavaProject javaProject = (IJavaProject)elements[0];
					ClasspathContainerInitializer initializer = JavaCore.getClasspathContainerInitializer(this.path.segment(0));
					String description = null;
					if (initializer != null) description = initializer.getDescription(this.path, javaProject);
					if (description == null) description = path.makeRelative().toString();
					return Util.bind("classpath.unboundContainerPath", description, javaProject.getElementName()); //$NON-NLS-1$

				case INVALID_CP_CONTAINER_ENTRY:
					javaProject = (IJavaProject)elements[0];
					IClasspathContainer container = null;
					description = null;
					try {
						container = JavaCore.getClasspathContainer(path, javaProject);
					} catch(JavaModelException e){
						// project doesn't exist: ignore
					}
					if (container == null) {
						 initializer = JavaCore.getClasspathContainerInitializer(path.segment(0));
						if (initializer != null) description = initializer.getDescription(path, javaProject);
					} else {
						description = container.getDescription();
					}
					if (description == null) description = path.makeRelative().toString();
					return Util.bind("classpath.invalidContainer", description, javaProject.getElementName()); //$NON-NLS-1$

			case CP_VARIABLE_PATH_UNBOUND:
				javaProject = (IJavaProject)elements[0];
				return Util.bind("classpath.unboundVariablePath", path.makeRelative().toString(), javaProject.getElementName()); //$NON-NLS-1$
					
			case CLASSPATH_CYCLE: 
				javaProject = (IJavaProject)elements[0];
				return Util.bind("classpath.cycle", javaProject.getElementName()); //$NON-NLS-1$
												 
			case DISABLED_CP_EXCLUSION_PATTERNS:
				javaProject = (IJavaProject)elements[0];
				String projectName = javaProject.getElementName();
				IPath newPath = path;
				if (path.segment(0).toString().equals(projectName)) {
					newPath = path.removeFirstSegments(1);
				}
				return Util.bind("classpath.disabledInclusionExclusionPatterns", newPath.makeRelative().toString(), projectName); //$NON-NLS-1$

			case DISABLED_CP_MULTIPLE_OUTPUT_LOCATIONS:
				javaProject = (IJavaProject)elements[0];
				projectName = javaProject.getElementName();
				newPath = path;
				if (path.segment(0).toString().equals(projectName)) {
					newPath = path.removeFirstSegments(1);
				}
				return Util.bind("classpath.disabledMultipleOutputLocations", newPath.makeRelative().toString(), projectName); //$NON-NLS-1$

			case INCOMPATIBLE_JDK_LEVEL:
					javaProject = (IJavaProject)elements[0];
					return Util.bind("classpath.incompatibleLibraryJDKLevel", new String[]{	//$NON-NLS-1$
						javaProject.getElementName(), 
						javaProject.getOption(JavaCore.COMPILER_CODEGEN_TARGET_PLATFORM, true), 
						path.makeRelative().toString(),
						string,
					});
			}
			if (string != null) {
				return string;
			} else {
				return ""; // //$NON-NLS-1$
			}
		} else {
			String message = exception.getMessage();
			if (message != null) {
				return message;
			} else {
				return exception.toString();
			}
		}
	}
	/**
	 * @see IJavaModelStatus#getPath()
	 */
	public IPath getPath() {
		return path;
	}
	/**
	 * @see IStatus#getSeverity()
	 */
	public int getSeverity() {
		if (children == NO_CHILDREN) return super.getSeverity();
		int severity = -1;
		for (int i = 0, max = children.length; i < max; i++) {
			int childrenSeverity = children[i].getSeverity();
			if (childrenSeverity > severity) {
				severity = childrenSeverity;
			}
		}
		return severity;
	}
	/**
	 * @see IJavaModelStatus#getString()
	 * @deprecated
	 */
	public String getString() {
		return string;
	}
	/**
	 * @see IJavaModelStatus#isDoesNotExist()
	 */
	public boolean isDoesNotExist() {
		return getCode() == ELEMENT_DOES_NOT_EXIST;
	}
	/**
	 * @see IStatus#isMultiStatus()
	 */
	public boolean isMultiStatus() {
		return children != NO_CHILDREN;
	}
	/**
	 * @see IStatus#isOK()
	 */
	public boolean isOK() {
		return getCode() == OK;
	}
	/**
	 * @see IStatus#matches(int)
	 */
	public boolean matches(int mask) {
		if (! isMultiStatus()) {
			return matches(this, mask);
		} else {
			for (int i = 0, max = children.length; i < max; i++) {
				if (matches((JavaModelStatus) children[i], mask))
					return true;
			}
			return false;
		}
	}
	/**
	 * Helper for matches(int).
	 */
	protected boolean matches(JavaModelStatus status, int mask) {
		int severityMask = mask & 0x7;
		int categoryMask = mask & ~0x7;
		int bits = status.getBits();
		return ((severityMask == 0) || (bits & severityMask) != 0) && ((categoryMask == 0) || (bits & categoryMask) != 0);
	}
	/**
	 * Creates and returns a new <code>IJavaModelStatus</code> that is a
	 * a multi-status status.
	 *
	 * @see IStatus#isMultiStatus()
	 */
	public static IJavaModelStatus newMultiStatus(IJavaModelStatus[] children) {
		JavaModelStatus jms = new JavaModelStatus();
		jms.children = children;
		return jms;
	}
	/**
	 * Returns a printable representation of this exception for debugging
	 * purposes.
	 */
	public String toString() {
		if (this == VERIFIED_OK){
			return "JavaModelStatus[OK]"; //$NON-NLS-1$
		}
		StringBuffer buffer = new StringBuffer();
		buffer.append("Java Model Status ["); //$NON-NLS-1$
		buffer.append(getMessage());
		buffer.append("]"); //$NON-NLS-1$
		return buffer.toString();
	}
}
