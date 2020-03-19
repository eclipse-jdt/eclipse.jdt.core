/*******************************************************************************
 * Copyright (c) 2007 - 2018 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    wharley@bea.com - initial API and implementation
 *
 *******************************************************************************/

package org.eclipse.jdt.internal.apt.pluggable.core.filer;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.annotation.processing.FilerException;
import javax.lang.model.element.Element;
import javax.tools.FileObject;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.StandardLocation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.AptCompilationParticipant;
import org.eclipse.jdt.apt.core.internal.generatedfile.GeneratedSourceFolderManager;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.apt.pluggable.core.Apt6Plugin;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeAnnotationProcessorManager;
import org.eclipse.jdt.internal.apt.pluggable.core.dispatch.IdeProcessingEnvImpl;

/**
 * Implementation of the Filer interface that is used in IDE mode.
 * @see org.eclipse.jdt.internal.compiler.apt.dispatch.BatchFilerImpl
 * @since 3.3
 */
public class IdeFilerImpl implements Filer {

	//private final IdeAnnotationProcessorManager _dispatchManager;
	private final IdeProcessingEnvImpl _env;

	public IdeFilerImpl(IdeAnnotationProcessorManager dispatchManager,
			IdeProcessingEnvImpl env) {
		//_dispatchManager = dispatchManager;
		_env = env;
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createClassFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createClassFile(CharSequence name, Element... originatingElements)
			throws IOException {

		// Pre-emptively check parameters here, rather than later on when the resource is written and closed.
		if (null == name) {
			throw new IllegalArgumentException("Name is null");
		}

    	String nameAsString = name.toString();
		IFile file = _env.getAptProject().getGeneratedFileManager(_env.isTestCode()).getIFileForTypeName(nameAsString);

    	GeneratedSourceFolderManager gsfm = _env.getAptProject().getGeneratedSourceFolderManager(_env.isTestCode());
    	IPath path = null;
    	try {
			path = gsfm.getBinaryOutputLocation();
		} catch (JavaModelException e) {
			Apt6Plugin.log(e, "Failure getting the binary output location"); //$NON-NLS-1$
			throw new IOException(e);
		}
    	int index = nameAsString.lastIndexOf('.');
    	String pkg = null;
    	if (index != -1) {
    		name = nameAsString.substring(index + 1);
    		pkg = nameAsString.substring(0, index);
    	} else {
    		pkg = "";
    	}
    	file = getFileFromOutputLocation(StandardLocation.CLASS_OUTPUT, pkg, name + ".class");
		path = path.append(nameAsString);
		path = new Path(path.toString() + ".class");

		return new IdeOutputClassFileObject(_env, file, nameAsString);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#createResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence, javax.lang.model.element.Element[])
	 * In the IDE implementation, we support only two Locations: SOURCE_OUTPUT, which means the APT generated source folder,
	 * and CLASS_OUTPUT, which means the binary output folder associated with the APT generated source folder.
	 */
	@Override
	public FileObject createResource(Location location, CharSequence pkg,
			CharSequence relativeName, Element... originatingElements) throws IOException
	{
		// Pre-emptively check parameters here, rather than later on when the resource is written and closed.
		if (null == location) {
			throw new IllegalArgumentException("Location is null");
		}
		if (!location.isOutputLocation()) {
			throw new IllegalArgumentException("Location " + location.getName() + " is not an output location");
		}

		if (null == pkg) {
			throw new IllegalArgumentException("Package is null");
		}
		if (null == relativeName) {
			throw new IllegalArgumentException("Relative name is null");
		}
		if ( relativeName.length() == 0) {
			throw new IllegalArgumentException("Relative name is zero length");
		}
		IFile file = getFileFromOutputLocation(location, pkg, relativeName);
		if (AptCompilationParticipant.getInstance().getJava6GeneratedFiles().contains(file)) {
			throw new FilerException("Source file already created: " + file.getFullPath()); //$NON-NLS-1$
		}

		Set<IFile> parentFiles;
		if (originatingElements != null && originatingElements.length > 0) {
			parentFiles = new HashSet<IFile>(originatingElements.length);
			for (Element elem : originatingElements) {
				IFile enclosing = _env.getEnclosingIFile(elem);
				if (null != enclosing) {
					parentFiles.add(enclosing);
				}
			}
		} else {
			parentFiles = Collections.emptySet();
		}
		return new IdeOutputNonSourceFileObject(_env, file, parentFiles);
	}

	/**
	 * @param originatingElements should all be source types; binary types (ie elements in jar files)
	 * will be ignored.
	 * @see javax.annotation.processing.Filer#createSourceFile(java.lang.CharSequence, javax.lang.model.element.Element[])
	 */
	@Override
	public JavaFileObject createSourceFile(CharSequence name, Element... originatingElements)
			throws IOException
	{
		// Pre-emptively check parameters here, rather than later on when the resource is written and closed.
		if (null == name) {
			throw new IllegalArgumentException("Name is null");
		}
		IFile file = _env.getAptProject().getGeneratedFileManager(_env.isTestCode()).getIFileForTypeName(name.toString());
		if (AptCompilationParticipant.getInstance().getJava6GeneratedFiles().contains(file)) {
			throw new FilerException("Source file already created: " + file.getFullPath()); //$NON-NLS-1$
		}
		// TODO: is the following correct?
		// JDK 9's createSourceFile API mentions '/' as separator for a module prefix.
		// Otherwise shouldn't <code>name</code> already be "."-separated?
//		name = name.toString().replace('/', '.');
//
//		ModuleBinding m = _env._current_module;
//		if (m == null)
//			m = _env.getCompiler().lookupEnvironment.UnNamedModule;
//		ReferenceBinding type = m.environment.getType(CharOperation.splitOn('.', name.toString().toCharArray()), m);
//		if (type != null && (type.tagBits & TagBits.HasMissingType) == 0) {
//			IFile classFile = getFileFromOutputLocation(StandardLocation.CLASS_OUTPUT, CharOperation.toString(type.fPackage.compoundName), new String(type.sourceName()) + ".class");
//			String fileName = new String(type.getFileName());
//			if (fileName != null) {
//				String osString = classFile.getFullPath().toOSString();
//				if (!osString.equals(fileName)) {
//					throw new FilerException("Source file already exists : " + name); //$NON-NLS-1$
//				}
//			}
//		}
		Set<IFile> parentFiles = Collections.emptySet();
		if (originatingElements != null && originatingElements.length > 0) {
			parentFiles = new HashSet<IFile>(originatingElements.length);
			for (Element elem : originatingElements) {
				IFile enclosing = _env.getEnclosingIFile(elem);
				if (null != enclosing) {
					parentFiles.add(enclosing);
				}
			}
		}
		return new IdeOutputJavaFileObject(_env, name, parentFiles);
	}

	/* (non-Javadoc)
	 * @see javax.annotation.processing.Filer#getResource(javax.tools.JavaFileManager.Location, java.lang.CharSequence, java.lang.CharSequence)
	 * Returns a FileObject representing the specified resource.  The only supported locations
	 * are CLASS_OUTPUT and SOURCE_OUTPUT.
	 */
	@Override
	public FileObject getResource(Location location, CharSequence pkg, CharSequence relativeName)
			throws IOException
	{
		IFile file = getFileFromOutputLocation(location, pkg, relativeName);
		return new IdeInputFileObject(file);
	}

    /**
     * Return a project-relative path.  This does not create the file nor its parent directories,
     * but it does validate the path.
     * @param pkg must be non-null but can be empty.
     * @param relPath must be non-null and non-empty.
     * @throws IOException if the path is not valid.
     */
    protected IFile getFileFromOutputLocation( Location loc, CharSequence pkg, CharSequence relPath )
    	throws IOException
    {
    	GeneratedSourceFolderManager gsfm = _env.getAptProject().getGeneratedSourceFolderManager(_env.isTestCode());
    	IPath path = null;
    	if ( loc == StandardLocation.CLASS_OUTPUT )
    	{
    		try
    		{
    			path = gsfm.getBinaryOutputLocation();
    		}
    		catch ( JavaModelException e )
    		{
    			Apt6Plugin.log(e, "Failure getting the binary output location"); //$NON-NLS-1$
    			throw new IOException(e);
    		}
    	}
    	else if ( loc == StandardLocation.SOURCE_OUTPUT ) {
    		path = gsfm.getFolder().getProjectRelativePath();
    	}
    	else {
    		throw new IllegalArgumentException("Unsupported location: " + loc);
    	}

        if( pkg.length() > 0 )
            path = path.append(pkg.toString().replace('.', File.separatorChar) );

        path = path.append(relPath.toString());

        IFile file = _env.getProject().getFile(path);

		validatePath(file);

    	return file;
    }

    /**
     * Validate that a path fits the rules for being created.
     * @see IWorkspace#validatePath()
     * @throws IOException
     */
    private void validatePath(IFile file) throws IOException
	{
    	IStatus status = _env.getProject().getWorkspace().validatePath(file.getFullPath().toOSString(), IResource.FILE);
    	if (!status.isOK()) {
        	CoreException ce = new CoreException(status);
        	throw new IOException("Invalid path: " + file.toString(), ce); //$NON-NLS-1$
        }
	}

}
