package org.eclipse.jdt.internal.core;

public interface JavadocConstants {

	String ANCHOR_PREFIX_END = "\""; //$NON-NLS-1$
	String ANCHOR_PREFIX_START = "<A NAME=\""; //$NON-NLS-1$
	String ANCHOR_SUFFIX = "</A>"; //$NON-NLS-1$
	int ANCHOR_SUFFIX_LENGTH = JavadocConstants.ANCHOR_SUFFIX.length();
	String CONSTRUCTOR_DETAIL = "<!-- ========= CONSTRUCTOR DETAIL ======== -->"; //$NON-NLS-1$
	String CONSTRUCTOR_SUMMARY = "<!-- ======== CONSTRUCTOR SUMMARY ======== -->"; //$NON-NLS-1$
	String END_OF_CLASS_DATA = "<!-- ========= END OF CLASS DATA ========= -->"; //$NON-NLS-1$
	String HTML_EXTENSION = ".html"; //$NON-NLS-1$
	String INDEX_FILE_NAME = "index.html"; //$NON-NLS-1$
	String METHOD_DETAIL = "<!-- ============ METHOD DETAIL ========== -->"; //$NON-NLS-1$
	String METHOD_SUMMARY = "<!-- ========== METHOD SUMMARY =========== -->"; //$NON-NLS-1$
	String NESTED_CLASS_SUMMARY = "<!-- ======== NESTED CLASS SUMMARY ======== -->"; //$NON-NLS-1$
	String PACKAGE_FILE_NAME = "package-summary.html"; //$NON-NLS-1$
	String START_OF_CLASS_DATA = "<!-- ======== START OF CLASS DATA ======== -->"; //$NON-NLS-1$
	int START_OF_CLASS_DATA_LENGTH = JavadocConstants.START_OF_CLASS_DATA.length();
}
