package org.eclipse.jdt.internal.core.search;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.search.SearchDocument;
import org.eclipse.jdt.core.search.SearchParticipant;

public class JavaSearchDocument extends SearchDocument {
	
	private String documentPath;
	private SearchParticipant participant;
	protected byte[] byteContents;
	protected char[] charContents;
	
	public JavaSearchDocument(String documentPath, SearchParticipant participant) {
		this.documentPath = documentPath;
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
		IFile file = getFile();
		if (file == null) {
			return JavaCore.getOption(JavaCore.CORE_ENCODING);
		} else {
			return JavaCore.create(file.getProject()).getOption(JavaCore.CORE_ENCODING, true);
		}
	}
	private IFile getFile() {
		IPath path = new Path(this.documentPath);
		return (IFile)ResourcesPlugin.getWorkspace().getRoot().findMember(path);
	}
	private IPath getLocation() {
		IFile file = getFile();
		if (file == null) {
			return new Path(this.documentPath); // extenal file
		} else {
			return file.getLocation();
		}
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
