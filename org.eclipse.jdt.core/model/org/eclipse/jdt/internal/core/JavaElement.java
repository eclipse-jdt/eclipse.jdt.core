package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.jdom.*;

import java.util.Vector;

/**
 * Root of Java element handle hierarchy.
 *
 * @see IJavaElement
 */
public abstract class JavaElement extends PlatformObject implements IJavaElement {

	public static final char JEM_JAVAPROJECT= '=';
	public static final char JEM_PACKAGEFRAGMENTROOT= Path.SEPARATOR;
	public static final char JEM_PACKAGEFRAGMENT= '<';
	public static final char JEM_FIELD= '^';
	public static final char JEM_METHOD= '~';
	public static final char JEM_INITIALIZER= '|';
	public static final char JEM_COMPILATIONUNIT= '{';
	public static final char JEM_CLASSFILE= '(';
	public static final char JEM_TYPE= '[';
	public static final char JEM_PACKAGEDECLARATION= '%';
	public static final char JEM_IMPORTDECLARATION= '#';

	/**
	 * A count to uniquely identify this element in the case
	 * that a duplicate named element exists. For example, if
	 * there are two fields in a compilation unit with the
	 * same name, the occurrence count is used to distinguish
	 * them.  The occurrence count starts at 1 (i.e. the first 
	 * occurrence is occurrence 1, not occurrence 0).
	 */
	protected int fOccurrenceCount = 1;


	/**
	 * This element's type - one of the constants defined
	 * in IJavaLanguageElementTypes.
	 */
	protected int fLEType = 0;

	/**
	 * This element's parent, or <code>null</code> if this
	 * element does not have a parent.
	 */
	protected IJavaElement fParent;

	/**
	 * This element's name, or an empty <code>String</code> if this
	 * element does not have a name.
	 */
	protected String fName;

	/**
	 * Direct access to the Java Model Manager
	 */
	protected static JavaModelManager fgJavaModelManager = JavaModelManager.getJavaModelManager();
	
/**
 * Constructs a handle for a java element of the specified type, with
 * the given parent element and name.
 *
 * @param type - one of the constants defined in IJavaLanguageElement
 *
 * @exception IllegalArgumentException if the type is not one of the valid
 *		Java element type constants
 *
 */
protected JavaElement(int type, IJavaElement parent, String name) throws IllegalArgumentException {
	if (type < JAVA_MODEL || type > IMPORT_DECLARATION) {
		throw new IllegalArgumentException(Util.bind("element.invalidType")); //$NON-NLS-1$
	}
	fLEType= type;
	fParent= parent;
	fName= name;
}
/**
 * @see IOpenable
 */
public void close() throws JavaModelException {
	Object info = fgJavaModelManager.peekAtInfo(this);
	if (info != null) {
		if (this instanceof IParent) {
			IJavaElement[] children = ((JavaElementInfo) info).getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				JavaElement child = (JavaElement) children[i];
				child.close();
			}
		}
		closing(info);
		fgJavaModelManager.removeInfo(this);
	}
}
/**
 * This element is being closed.  Do any necessary cleanup.
 */
protected void closing(Object info) throws JavaModelException {
}
/**
 * Returns true if this handle represents the same Java element
 * as the given handle. By default, two handles represent the same
 * element if they are identical or if they represent the same type
 * of element, have equal names, parents, and occurrence counts.
 *
 * <p>If a subclass has other requirements for equality, this method
 * must be overridden.
 *
 * @see Object#equals
 */
public boolean equals(Object o) {
	if (this == o)
		return true;
	if (o instanceof JavaElement) {
		JavaElement other = (JavaElement) o;
		if (fLEType != other.fLEType)
			return false;
		return fName.equals(other.fName) &&
				fParent.equals(other.fParent) &&
				fOccurrenceCount == other.fOccurrenceCount;
	}
	return false;
}
/**
 * Returns true if this <code>JavaElement</code> is equivalent to the given
 * <code>IDOMNode</code>.
 */
protected boolean equalsDOMNode(IDOMNode node) throws JavaModelException {
	return false;
}
/**
 * @see IJavaElement
 */
public boolean exists() {
	try {
		getRawInfo();
		return true;
	} catch (JavaModelException e) {
		return false;
	}
}
/**
 * Returns the <code>IDOMNode</code> that corresponds to this <code>JavaElement</code>
 * or <code>null</code> if there is no corresponding node.
 */
public IDOMNode findNode(IDOMCompilationUnit dom) {
	int type = getElementType();
	if (type == IJavaElement.COMPILATION_UNIT || 
		type == IJavaElement.FIELD || 
		type == IJavaElement.IMPORT_DECLARATION || 
		type == IJavaElement.INITIALIZER || 
		type == IJavaElement.METHOD || 
		type == IJavaElement.PACKAGE_DECLARATION || 
		type == IJavaElement.TYPE) {
		Vector path = new Vector();
		IJavaElement element = this;
		while (element != null && element.getElementType() != IJavaElement.COMPILATION_UNIT) {
			if (element.getElementType() != IJavaElement.IMPORT_CONTAINER) {
				// the DOM does not have import containers, so skip them
				path.insertElementAt(element, 0);
			}
			element = element.getParent();
		}
		if (path.size() == 0) {
			try {
				if (equalsDOMNode(dom)) {
					return dom;
				} else {
					return null;
				}
			} catch(JavaModelException e) {
				return null;
			}
		}
		return ((JavaElement) path.elementAt(0)).followPath(path, 0, dom.getFirstChild());
	} else {
		return null;
	}
}
/**
 */
protected IDOMNode followPath(Vector path, int position, IDOMNode node) {

	try {
		if (equalsDOMNode(node)) {
			if (position == (path.size() - 1)) {
				return node;
			} else {
				if (node.getFirstChild() != null) {
					position++;
					return ((JavaElement)path.elementAt(position)).followPath(path, position, node.getFirstChild());
				} else {
					return null;
				}
			}
		} else if (node.getNextNode() != null) {
			return followPath(path, position, node.getNextNode());
		} else {
			return null;
		}
	} catch (JavaModelException e) {
		return null;
	}

}
/**
 * @see IParent 
 */
public IJavaElement[] getChildren() throws JavaModelException {
	return getElementInfo().getChildren();
}
/**
 * Returns a collection of (immediate) children of this node of the
 * specified type.
 *
 * @param type - one of constants defined by IJavaLanguageElementTypes
 */
public Vector getChildrenOfType(int type) throws JavaModelException {
	IJavaElement[] children = getChildren();
	int size = children.length;
	Vector v = new Vector(size);
	for (int i = 0; i < size; ++i) {
		JavaElement elt = (JavaElement)children[i];
		if (elt.getElementType() == type) {
			v.addElement(elt);
		}
	}
	return v;
}
/**
 * @see IMember
 */
public IClassFile getClassFile() {
	return null;
}
/**
 * @see IMember
 */
public ICompilationUnit getCompilationUnit() {
	return null;
}
/**
 * Returns the info for this handle.  
 * If this element is not already open, it and all of its parents are opened.
 * Does not return null.
 *
 * @exception JavaModelException if the element is not present or not accessible
 */
public JavaElementInfo getElementInfo() throws JavaModelException {
	synchronized(fgJavaModelManager){
		Object info = fgJavaModelManager.getInfo(this);
		if (info == null) {
			openHierarchy();
			info= fgJavaModelManager.getInfo(this);
			if (info == null) {
				throw newNotPresentException();
			}
		}
		return (JavaElementInfo)info;
	}
}
/**
 * @see IAdaptable
 */
public String getElementName() {
	return fName;
}
/**
 * @see IJavaElement
 */
public int getElementType() {
	return fLEType;
}
/**
 * @see IJavaElement
 */
public String getHandleIdentifier() {
	return getHandleMemento();
}
/**
 * @see JavaElement#getHandleMemento()
 */
public String getHandleMemento(){
	StringBuffer buff= new StringBuffer(((JavaElement)getParent()).getHandleMemento());
	buff.append(getHandleMementoDelimiter());
	buff.append(getElementName());
	return buff.toString();
}
/**
 * Returns the <code>char</code> that marks the start of this handles
 * contribution to a memento.
 */
protected abstract char getHandleMementoDelimiter();
/**
 * @see IJavaElement
 */
public IJavaModel getJavaModel() {
	return getParent().getJavaModel();
}
/**
 * Returns the JavaModelManager
 */
public JavaModelManager getJavaModelManager() {
	return fgJavaModelManager;
}
/**
 * @see IJavaElement
 */
public IJavaProject getJavaProject() {
	return getParent().getJavaProject();
}
/**
 * Returns the occurrence count of the handle.
 */
protected int getOccurrenceCount() {
	return fOccurrenceCount;
}
/**
 * Return the first instance of IOpenable in the parent
 * hierarchy of this element.
 *
 * <p>Subclasses that are not IOpenable's must override this method.
 */
public IOpenable getOpenableParent() {
	
	return (IOpenable)fParent;
}
/**
 * @see IJavaElement
 */
public IJavaElement getParent() {
	return fParent;
}
/**
 * Returns the info for this handle.  
 * If this element is not already open, it and all of its parents are opened.
 * Does not return null.
 *
 * @exception JavaModelException if the element is not present or not accessible
 */
public Object getRawInfo() throws JavaModelException {
	Object info = fgJavaModelManager.getInfo(this);
	if (info == null) {
		openHierarchy();
		info= fgJavaModelManager.getInfo(this);
		if (info == null) {
			throw newNotPresentException();
		}
	}
	return info;
}
/**
 * Returns the element that is located at the given source position
 * in this element.  This is a helper method for <code>ICompilationUnit#getElementAt</code>,
 * and only works on compilation units and types. The position given is
 * known to be within this element's source range already, and if no finer
 * grained element is found at the position, this element is returned.
 */
protected IJavaElement getSourceElementAt(int position) throws JavaModelException {
	if (this instanceof ISourceReference) {
		IJavaElement[] children = getChildren();
		int i;
		for (i = 0; i < children.length; i++) {
			IJavaElement aChild = children[i];
			if (aChild instanceof SourceRefElement) {
				SourceRefElement child = (SourceRefElement) children[i];
				ISourceRange range = child.getSourceRange();
				if (position < range.getOffset() + range.getLength() && position >= range.getOffset()) {
					if (child.getElementType() == TYPE) {
						return child.getSourceElementAt(position);
					} else {
						return child;
					}
				}
			}
		}
	} else {
		// should not happen
		Assert.isTrue(false);
	}
	return this;
}
/**
 * Returns the SourceMapper facility for this element, or
 * <code>null</code> if this element does not have a
 * SourceMapper.
 */
public SourceMapper getSourceMapper() {
	return ((JavaElement)getParent()).getSourceMapper();
}
public abstract IResource getUnderlyingResource() throws JavaModelException;
/**
 * Returns the workspace associated with this object.
 */
public IWorkspace getWorkspace() {
	return getJavaModel().getWorkspace();
}
/**
 * Returns the hash code for this Java element. By default,
 * the hash code for an element is a combination of its name
 * and parent's hash code. Elements with other requirements must
 * override this method.
 */
public int hashCode() {
	return Util.combineHashCodes(fName.hashCode(), fParent.hashCode());
}
/**
 * Returns true if this element is an ancestor of the given element,
 * otherwise false.
 */
protected boolean isAncestorOf(IJavaElement e) {
	IJavaElement parent= e.getParent();
	while (parent != null && !parent.equals(this)) {
		parent= parent.getParent();
	}
	return parent != null;
}
/**
 * @see IJavaElement
 */
public boolean isReadOnly() {
	return false;
}
/**
 * @see IJavaElement
 */
public boolean isStructureKnown() throws JavaModelException {
	return getElementInfo().isStructureKnown();
}
/**
 * Creates and returns and not present exception for this element.
 */
protected JavaModelException newNotPresentException() {
	return new JavaModelException(new JavaModelStatus(IJavaModelStatusConstants.ELEMENT_DOES_NOT_EXIST, this));
}
/**
 * Default is to not do any source indices updates.
 */
public void offsetSourceEndAndChildren(int amount, IJavaElement child) {
	
}
/**
 * Default behaviour is not to change the source range
 * for the Java element
 */
public void offsetSourceRange(int amount) {
}
/**
 * Opens this element and all parents that are not already open.
 *
 * @exception JavaModelException this element is not present or accessible
 */
protected void openHierarchy() throws JavaModelException {
	if (this instanceof IOpenable) {
		((Openable) this).openWhenClosed(null);
	} else {
		Openable openableParent = (Openable)getOpenableParent();
		if (openableParent != null) {
			JavaElementInfo openableParentInfo = (JavaElementInfo) fgJavaModelManager.getInfo((IJavaElement) openableParent);
			if (openableParentInfo == null) {
				openableParent.openWhenClosed(null);
			} else {
				throw newNotPresentException();
			}
		}
	}
}
/**
 * This element has just been opened.  Do any necessary setup.
 */
protected void opening(Object info) {
}
/**
 */
public String readableName() {

	return this.getElementName();
}
/**
 * Removes all cached info from the Java Model, including all children,
 * but does not close this element.
 */
protected void removeInfo() {
	Object info = fgJavaModelManager.peekAtInfo(this);
	if (info != null) {
		if (this instanceof IParent) {
			IJavaElement[] children = ((JavaElementInfo)info).getChildren();
			for (int i = 0, size = children.length; i < size; ++i) {
				JavaElement child = (JavaElement) children[i];
				child.removeInfo();
			}
		}
		fgJavaModelManager.removeInfo(this);
	}
}
/**
 * Runs a Java Model Operation
 */
protected void runOperation(JavaModelOperation operation, IProgressMonitor monitor) throws JavaModelException {
	fgJavaModelManager.runOperation(operation, monitor);
}
/**
 * Sets the occurrence count of the handle.
 */
protected void setOccurrenceCount(int count) {
	fOccurrenceCount = count;
}
protected String tabString(int tab) {
	StringBuffer buffer = new StringBuffer();
	for (int i = tab; i > 0; i--)
		buffer.append("  "); //$NON-NLS-1$
	return buffer.toString();
}
/**
 * @private Debugging purposes
 */
protected String toDebugString() {
	StringBuffer buffer = new StringBuffer();
	Object info = fgJavaModelManager.getInfo(this);
	this.toStringInfo(0, buffer, info);
	return buffer.toString();
}
/**
 * @private Debugging purposes
 */
public String toString() {
	StringBuffer buffer = new StringBuffer();
	toString(0, buffer);
	return buffer.toString();
}
/**
 * @private Debugging purposes
 */
protected void toString(int tab, StringBuffer buffer) {
	buffer.append(this.tabString(tab));
	Object info = fgJavaModelManager.getInfo(this);
	this.toStringInfo(tab, buffer, info);
	if (tab == 0) {
		this.toStringAncestors(buffer);
	}
	this.toStringChildren(tab, buffer, info);
}
/**
 * @private Debugging purposes
 */
protected void toStringAncestors(StringBuffer buffer) {
	JavaElement parent = (JavaElement)this.getParent();
	if (parent != null) {
		buffer.append(" [in "); //$NON-NLS-1$
		Object parentInfo = fgJavaModelManager.getInfo(parent);
		parent.toStringInfo(0, buffer, parentInfo);
		parent.toStringAncestors(buffer);
		buffer.append("]"); //$NON-NLS-1$
	}
}
/**
 * @private Debugging purposes
 */
protected void toStringChildren(int tab, StringBuffer buffer, Object info) {
	if (info == null || !(info instanceof JavaElementInfo)) return;
	IJavaElement[] children = ((JavaElementInfo)info).getChildren();
	for (int i = 0; i < children.length; i++) {
		buffer.append("\n"); //$NON-NLS-1$
		((JavaElement)children[i]).toString(tab + 1, buffer);
	}
}
/**
 * @private Debugging purposes
 */
protected void toStringInfo(int tab, StringBuffer buffer, Object info) {
	buffer.append(getElementName());
	if (info == null) {
		buffer.append(" (not open)"); //$NON-NLS-1$
	}
}
/**
 * Updates the source end position for this element.
 * Default behaviour is to do nothing.
 */
public void triggerSourceEndOffset(int amount, int nameStart, int nameEnd) {
}
/**
 * Updates the source positions for this element.
 * Default behaviour is to do nothing.
 */
public void triggerSourceRangeOffset(int amount, int nameStart, int nameEnd) {

}
}
