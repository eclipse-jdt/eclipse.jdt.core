package org.eclipse.jdt.internal.core.search;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.*;

public class JavaSearchDocument extends SearchDocument {
	
	private IFile file;
	protected byte[] byteContents;
	protected char[] charContents;
	
	public JavaSearchDocument(String documentPath, SearchParticipant participant) {
		super(documentPath, participant);
	}
	public JavaSearchDocument(IFile file, SearchParticipant participant) {
		super(file.getFullPath().toString(), participant);
		this.file = file;
	}
	public JavaSearchDocument(java.util.zip.ZipEntry zipEntry, IPath zipFilePath, byte[] contents, SearchParticipant participant) {
		super(zipFilePath + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + zipEntry.getName(), participant);
		this.byteContents = contents;
	}

	public byte[] getByteContents() {
		if (this.byteContents != null) return this.byteContents;
		try {
			return org.eclipse.jdt.internal.compiler.util.Util.getFileByteContent(getLocation().toFile());
		} catch (IOException e) {
			/// TODO (jerome) log in VERBOSE mode e.printStackTrace();
			return null;
		}
	}
	public char[] getCharContents() {
		if (this.charContents != null) return this.charContents;
		try {
			return org.eclipse.jdt.internal.compiler.util.Util.getFileCharContent(getLocation().toFile(), getEncoding());
		} catch (IOException e) {
			/// TODO (jerome) log in VERBOSE mode e.printStackTrace();
			return null;
		}
	}
	public String getEncoding() {
		IFile resource = getFile();
		if (resource != null)
			return JavaCore.create(resource.getProject()).getOption(JavaCore.CORE_ENCODING, true);
		return JavaCore.getOption(JavaCore.CORE_ENCODING);
	}
	private IFile getFile() {
		if (this.file == null)
			this.file = (IFile) ResourcesPlugin.getWorkspace().getRoot().findMember(new Path(this.documentPath));
		return this.file;
	}
	private IPath getLocation() {
		IFile resource = getFile();
		if (resource != null)
			return resource.getLocation();
		return new Path(this.documentPath); // external file
	}
	public String toString() {
		return "SearchDocument for " + this.documentPath; //$NON-NLS-1$
	}
}
