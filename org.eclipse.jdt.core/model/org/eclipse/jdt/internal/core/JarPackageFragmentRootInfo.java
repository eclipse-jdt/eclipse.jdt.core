package org.eclipse.jdt.internal.core;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
/**
 * The element info for <code>JarPackageFragmentRoot</code>s.
 */
import org.eclipse.core.resources.IResource;

class JarPackageFragmentRootInfo extends PackageFragmentRootInfo {
	/**
	 * The SourceMapper for this JAR (or <code>null</code> if
	 * this JAR does not have source attached).
	 */
	protected SourceMapper fSourceMapper = null;
	/**
	 * Returns an array of non-java resources contained in the receiver.
	 */
	public Object[] getNonJavaResources() {
		fNonJavaResources = NO_NON_JAVA_RESOURCES;
		return fNonJavaResources;
	}

	/**
	 * Retuns the SourceMapper for this JAR, or <code>null</code>
	 * if this JAR does not have attached source.
	 */
	protected SourceMapper getSourceMapper() {
		return fSourceMapper;
	}

	/**
	 * Sets the SourceMapper for this JAR.
	 */
	protected void setSourceMapper(SourceMapper mapper) {
		fSourceMapper = mapper;
	}

}
