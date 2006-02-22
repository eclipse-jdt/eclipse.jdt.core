package org.eclipse.jdt.apt.core.internal.declaration;

import org.eclipse.jdt.core.dom.ITypeBinding;

import com.sun.mirror.type.TypeMirror;

/**
 * The base type for all Mirror type objects
 * @author thanson
 *
 */
public interface EclipseMirrorType extends EclipseMirrorObject, TypeMirror {
	public boolean isAssignmentCompatible(EclipseMirrorType left);
	public boolean isSubTypeCompatible(EclipseMirrorType type);
	
	public ITypeBinding getTypeBinding();
}
