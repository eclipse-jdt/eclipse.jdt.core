package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.IPath;
import java.util.Vector;

import org.eclipse.jdt.core.IJavaModelStatusConstants;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaModelStatus;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.resources.IResourceStatus;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.core.runtime.CoreException;

/**
 * @see IJavaModelStatus
 */

public class JavaModelStatus
	extends Status
	implements IJavaModelStatus, IJavaModelStatusConstants, IResourceStatus {

	/**
	 * The elements related to the failure, or <code>null</code>
	 * if no elements are involved.
	 */
	protected IJavaElement[] fElements = new IJavaElement[0];
	/**
	 * The path related to the failure, or <code>null</code>
	 * if no path is involved.
	 */
	protected IPath fPath;
	/**
	 * The <code>String</code> related to the failure, or <code>null</code>
	 * if no <code>String</code> is involved.
	 */
	protected String fString;
	/**
	 * Singleton OK object
	 */
	public static final IJavaModelStatus VERIFIED_OK = new JavaModelStatus(OK);
	/**
	 * Empty children
	 */
	protected final static IStatus[] fgEmptyChildren = new IStatus[] {
	};

	protected IStatus[] fChildren = fgEmptyChildren;
	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus() {
		// no code for an multi-status
		super(ERROR, JavaCore.PLUGIN_ID, 0, "JavaModelStatus", null);
	}

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null);
		fElements = JavaElementInfo.fgEmptyChildren;
	}

	/**
	 * Constructs an Java model status with the given corresponding
	 * elements.
	 */
	public JavaModelStatus(int code, IJavaElement[] elements) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null);
		fElements = elements;
		fPath = null;
	}

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, String string) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null);
		fElements = JavaElementInfo.fgEmptyChildren;
		fPath = null;
		fString = string;
	}

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, Throwable throwable) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", throwable);
		fElements = JavaElementInfo.fgEmptyChildren;
	}

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(int code, IPath path) {
		super(ERROR, JavaCore.PLUGIN_ID, code, "JavaModelStatus", null);
		fElements = JavaElementInfo.fgEmptyChildren;
		fPath = path;
	}

	/**
	 * Constructs an Java model status with the given corresponding
	 * element.
	 */
	public JavaModelStatus(int code, IJavaElement element) {
		this(code, new IJavaElement[] { element });
	}

	/**
	 * Constructs an Java model status with the given corresponding
	 * element and string
	 */
	public JavaModelStatus(int code, IJavaElement element, String string) {
		this(code, new IJavaElement[] { element });
		fString = string;
	}

	/**
	 * Constructs an Java model status with no corresponding elements.
	 */
	public JavaModelStatus(CoreException coreException) {
		super(
			ERROR,
			JavaCore.PLUGIN_ID,
			CORE_EXCEPTION,
			"JavaModelStatus",
			coreException);
		fElements = JavaElementInfo.fgEmptyChildren;
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
		return fChildren;
	}

	/**
	 * @see IJavaModelStatus
	 */
	public IJavaElement[] getElements() {
		return fElements;
	}

	/**
	 * Returns the message that is relevant to the code of this status.
	 */
	public String getMessage() {
		if (getException() == null) {
			switch (getCode()) {
				case CORE_EXCEPTION :
					return "Core exception.";
				case BUILDER_INITIALIZATION_ERROR :
					return "Builder initialization error.";
				case BUILDER_SERIALIZATION_ERROR :
					return "Builder serialization error.";
				case DEVICE_PATH :
					return "Operation requires a path with no device. Path specified was: "
						+ getPath().toString();
				case DOM_EXCEPTION :
					return "JDOM error.";
				case ELEMENT_DOES_NOT_EXIST :
					return fElements[0].getElementName() + " does not exist.";
				case EVALUATION_ERROR :
					return "Evaluation error: " + getString();
				case INDEX_OUT_OF_BOUNDS :
					return "Index out of bounds.";
				case INVALID_CONTENTS :
					return "Invalid contents specified.";
				case INVALID_DESTINATION :
					return "Invalid destination: " + fElements[0].getElementName();
				case INVALID_ELEMENT_TYPES :
					StringBuffer buff =
						new StringBuffer("Operation not supported for specified element type(s): ");
					for (int i = 0; i < fElements.length; i++) {
						if (i > 0) {
							buff.append(", ");
						}
						buff.append(fElements[0].getElementName());
					}
					return buff.toString();
				case INVALID_NAME :
					return "Invalid name specified: " + getString();
				case INVALID_PACKAGE :
					return "Invalid package: " + getString();
				case INVALID_PATH :
					return "Invalid path: " + (getPath() == null ? "null" : getPath().toString());
				case INVALID_PROJECT :
					return "Invalid project: " + getString();
				case INVALID_RESOURCE :
					return "Invalid resource: " + getString();
				case INVALID_RESOURCE_TYPE :
					return "Invalid resource type for " + getString();
				case INVALID_SIBLING :
					return "Invalid sibling: " + fElements[0].getElementName();
				case IO_EXCEPTION :
					return "IO exception.";
				case NAME_COLLISION :
					if (fElements != null && fElements.length > 0) {
						IJavaElement element = fElements[0];
						String name = element.getElementName();
						if (element instanceof IPackageFragment
							&& name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
							return "Default package cannot be renamed.";
						}
					}
					return "Name collision.";
				case NO_ELEMENTS_TO_PROCESS :
					return "Operation requires one or more elements.";
				case NULL_NAME :
					return "Operation requires a name.";
				case NULL_PATH :
					return "Operation requires a path.";
				case NULL_STRING :
					return "Operation requires a string.";
				case PATH_OUTSIDE_PROJECT :
					return "Illegal path specified: " + getPath().toString();
				case READ_ONLY :
					IJavaElement element = fElements[0];
					String name = element.getElementName();
					if (element instanceof IPackageFragment
						&& name.equals(IPackageFragment.DEFAULT_PACKAGE_NAME)) {
						name = "Default package";
					}
					return name + " is read-only.";
				case RELATIVE_PATH :
					return "Operation requires an absolute path. Relative path specified was: "
						+ getPath().toString();
				case TARGET_EXCEPTION :
					return "Target exception.";
				case UPDATE_CONFLICT :
					return "Update conflict.";
				case NO_LOCAL_CONTENTS :
					return "Cannot find local contents for resource: " + getPath().toString();
			}
			return getString();
		} else {
			if (getCode() == CORE_EXCEPTION) {
				return "Core exception: " + getException().getMessage();
			}
			return getException().getMessage();
		}
	}

	/**
	 * @see IOperationStatus
	 */
	public IPath getPath() {
		return fPath;
	}

	/**
	 * @see IStatus
	 */
	public int getSeverity() {
		if (fChildren == fgEmptyChildren)
			return super.getSeverity();
		int severity = -1;
		for (int i = 0, max = fChildren.length; i < max; i++) {
			int childrenSeverity = fChildren[i].getSeverity();
			if (childrenSeverity > severity) {
				severity = childrenSeverity;
			}
		}
		return severity;
	}

	/**
	 * @see IJavaModelStatus
	 */
	public String getString() {
		return fString;
	}

	/**
	 * @see IJavaModelStatus
	 */
	public boolean isDoesNotExist() {
		return getCode() == ELEMENT_DOES_NOT_EXIST;
	}

	/**
	 * @see IStatus
	 */
	public boolean isMultiStatus() {
		return fChildren != fgEmptyChildren;
	}

	/**
	 * @see IJavaModelStatus
	 */
	public boolean isOK() {
		return getCode() == OK;
	}

	/**
	 * @see IStatus#matches
	 */
	public boolean matches(int mask) {
		if (!isMultiStatus()) {
			return matches(this, mask);
		} else {
			for (int i = 0, max = fChildren.length; i < max; i++) {
				if (matches((JavaModelStatus) fChildren[i], mask))
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
		return ((severityMask == 0) || (bits & severityMask) != 0)
			&& ((categoryMask == 0) || (bits & categoryMask) != 0);
	}

	/**
	 * Creates and returns a new <code>IJavaModelStatus</code> that is a
	 * a multi-status status.
	 *
	 * @see IStatus#.isMultiStatus()
	 */
	public static IJavaModelStatus newMultiStatus(IJavaModelStatus[] children) {
		JavaModelStatus jms = new JavaModelStatus();
		jms.fChildren = children;
		return jms;
	}

	/**
	 * Returns a printable representation of this exception for debugging
	 * purposes.
	 */
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("Java Model Status [");
		buffer.append(getMessage());
		buffer.append("]");
		return buffer.toString();
	}

}
