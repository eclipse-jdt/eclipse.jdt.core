package org.eclipse.jdt.internal.core.search;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.*;

public class JavaSearchDocument extends SearchDocument {
	
	private String documentPath;
	private SearchParticipant participant;
	private IFile file;
	protected byte[] byteContents;
	protected char[] charContents;
	
	public JavaSearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
		this.participant = participant;
	}
	public JavaSearchDocument(IFile file, SearchParticipant participant) {
		this.documentPath = file.getFullPath().toString();
		this.participant = participant;
		this.file = file;
	}
	public JavaSearchDocument(java.util.zip.ZipEntry zipEntry, IPath zipFilePath, byte[] contents, SearchParticipant participant) {
		this.documentPath = zipFilePath + IJavaSearchScope.JAR_FILE_ENTRY_SEPARATOR + zipEntry.getName();
		this.byteContents = contents;
		this.participant = participant;
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
	public SearchParticipant getParticipant() {
		return this.participant;
	}
	public String getPath() {
		return this.documentPath;
	}
	public String toString() {
		return "SearchDocument for " + this.documentPath; //$NON-NLS-1$
	}
}
