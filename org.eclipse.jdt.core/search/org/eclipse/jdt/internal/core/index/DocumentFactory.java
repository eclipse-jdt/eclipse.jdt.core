package org.eclipse.jdt.internal.core.index;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.resources.*;

import org.eclipse.jdt.internal.core.index.impl.*;

import java.io.*;

public class DocumentFactory {

	public static IDocument newDocument(File file) {
		return new FileDocument(file);
	}

	public static IDocument newDocument(IFile file) {
		return new IFileDocument(file);
	}

}
