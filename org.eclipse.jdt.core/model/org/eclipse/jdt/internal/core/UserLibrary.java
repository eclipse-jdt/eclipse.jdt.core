/*******************************************************************************
 * Copyright (c) 2000, 2004 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.internal.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.core.util.Messages;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Internal model element to represent a user library and code to serialize / deserialize.
 */
public class UserLibrary {

	private static final String CURRENT_VERSION= "1"; //$NON-NLS-1$

	private static final String TAG_VERSION= "version"; //$NON-NLS-1$
	private static final String TAG_USERLIBRARY= "userlibrary"; //$NON-NLS-1$
	private static final String TAG_SOURCEATTACHMENT= "sourceattachment"; //$NON-NLS-1$
	private static final String TAG_SOURCEATTACHMENTROOT= "sourceattachmentroot"; //$NON-NLS-1$
	private static final String TAG_PATH= "path"; //$NON-NLS-1$
	private static final String TAG_ARCHIVE= "archive"; //$NON-NLS-1$
	private static final String TAG_SYSTEMLIBRARY= "systemlibrary"; //$NON-NLS-1$
	
	private boolean isSystemLibrary;
	private IClasspathEntry[] entries;

	public UserLibrary(IClasspathEntry[] entries, boolean isSystemLibrary) {
		Assert.isNotNull(entries);
		this.entries= entries;
		this.isSystemLibrary= isSystemLibrary;
	}
	
	public IClasspathEntry[] getEntries() {
		return this.entries;
	}
	
	public boolean isSystemLibrary() {
		return this.isSystemLibrary;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj) {
		if (obj != null && obj.getClass() == getClass()) {
			UserLibrary other= (UserLibrary) obj;
			if (this.entries.length == other.entries.length && this.isSystemLibrary == other.isSystemLibrary) {
				for (int i= 0; i < this.entries.length; i++) {
					if (!this.entries[i].equals(other.entries[i])) {
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		int hashCode= 0;
		if (this.isSystemLibrary) {
			hashCode++;
		}
		for (int i= 0; i < this.entries.length; i++) {
			hashCode= hashCode * 17 + this.entries.hashCode();
		}
		return hashCode;
	}
	
	/* package */  String serialize() throws IOException {
		ByteArrayOutputStream s = new ByteArrayOutputStream();
		OutputStreamWriter writer = new OutputStreamWriter(s, "UTF8"); //$NON-NLS-1$
		XMLWriter xmlWriter = new XMLWriter(writer);
		
		HashMap library = new HashMap();
		library.put(TAG_VERSION, String.valueOf(CURRENT_VERSION));
		library.put(TAG_SYSTEMLIBRARY, String.valueOf(this.isSystemLibrary));
		xmlWriter.printTag(TAG_USERLIBRARY, library, true, true, false);
		
		for (int i = 0; i < this.entries.length; ++i) {
			IClasspathEntry curr= this.entries[i];
			
			HashMap archive = new HashMap();
			archive.put(TAG_PATH, curr.getPath().toString());
			IPath sourceAttach= curr.getSourceAttachmentPath();
			if (sourceAttach != null)
				archive.put(TAG_SOURCEATTACHMENT, sourceAttach);
			IPath sourceAttachRoot= curr.getSourceAttachmentRootPath();
			if (sourceAttachRoot != null)
				archive.put(TAG_SOURCEATTACHMENTROOT, sourceAttachRoot);				
			xmlWriter.printTag(TAG_ARCHIVE, archive, true, true, true);
		}	
		xmlWriter.endTag(TAG_USERLIBRARY, true);
		writer.flush();
		writer.close();
		return s.toString("UTF8");//$NON-NLS-1$
	}
	
	/* package */ static UserLibrary createFromString(Reader reader) throws IOException {
		Element cpElement;
		try {
			DocumentBuilder parser = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			cpElement = parser.parse(new InputSource(reader)).getDocumentElement();
		} catch (SAXException e) {
			throw new IOException(Messages.file_badFormat); 
		} catch (ParserConfigurationException e) {
			throw new IOException(Messages.file_badFormat); 
		} finally {
			reader.close();
		}
		
		if (!cpElement.getNodeName().equalsIgnoreCase(TAG_USERLIBRARY)) { //$NON-NLS-1$
			throw new IOException(Messages.file_badFormat); 
		}
		// String version= cpElement.getAttribute(TAG_VERSION);
		// in case we update the format: add code to read older versions
		
		boolean isSystem= Boolean.valueOf(cpElement.getAttribute(TAG_SYSTEMLIBRARY)).booleanValue();
		
		NodeList list= cpElement.getChildNodes();
		int length = list.getLength();
		
		ArrayList res= new ArrayList(length);
		for (int i = 0; i < length; ++i) {
			Node node = list.item(i);
			
			if (node.getNodeType() == Node.ELEMENT_NODE) {
				Element element= (Element) node;
				if (element.getNodeName().equals(TAG_ARCHIVE)) {
					String path = element.getAttribute(TAG_PATH);
					IPath sourceAttach= element.hasAttribute(TAG_SOURCEATTACHMENT) ? new Path(element.getAttribute(TAG_SOURCEATTACHMENT)) : null;
					IPath sourceAttachRoot= element.hasAttribute(TAG_SOURCEATTACHMENTROOT) ? new Path(element.getAttribute(TAG_SOURCEATTACHMENTROOT)) : null;
					res.add(JavaCore.newLibraryEntry(new Path(path), sourceAttach, sourceAttachRoot));
				}
			}
		}
		
		IClasspathEntry[] entries= (IClasspathEntry[]) res.toArray(new IClasspathEntry[res.size()]);
		
		return new UserLibrary(entries, isSystem);
	}
	
	public String toString() {
		if (this.entries == null)
			return "null"; //$NON-NLS-1$
		StringBuffer buffer = new StringBuffer();
		int length = this.entries.length;
		for (int i=0; i<length; i++) {
			buffer.append(this.entries[i].toString()+'\n');
		}
		return buffer.toString();
	}
}
