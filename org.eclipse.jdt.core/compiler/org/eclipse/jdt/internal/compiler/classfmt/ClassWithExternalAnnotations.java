package org.eclipse.jdt.internal.compiler.classfmt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.ZipFileProducer;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.lookup.BinaryTypeBinding.ExternalAnnotationStatus;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.compiler.lookup.TypeConstants;

public class ClassWithExternalAnnotations implements IBinaryType {
	private IBinaryType inputType;
	private ExternalAnnotationProvider annotationProvider;
	private boolean isFromSource;

	public ClassWithExternalAnnotations(IBinaryType toDecorate, ExternalAnnotationProvider externalAnnotationProvider) {
		this.inputType = toDecorate;
		this.annotationProvider = externalAnnotationProvider;
	}

	public ClassWithExternalAnnotations(IBinaryType toDecorate, boolean isFromSource) {
		this.isFromSource = isFromSource;
		this.inputType = toDecorate;
	}

	@Override
	public char[] getFileName() {
		return this.inputType.getFileName();
	}

	@Override
	public boolean isBinaryType() {
		return this.inputType.isBinaryType();
	}

	@Override
	public IBinaryAnnotation[] getAnnotations() {
		return this.inputType.getAnnotations();
	}

	@Override
	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		return this.inputType.getTypeAnnotations();
	}

	@Override
	public char[] getEnclosingMethod() {
		return this.inputType.getEnclosingMethod();
	}

	@Override
	public char[] getEnclosingTypeName() {
		return this.inputType.getEnclosingTypeName();
	}

	@Override
	public IBinaryField[] getFields() {
		return this.inputType.getFields();
	}

	@Override
	public char[] getGenericSignature() {
		return this.inputType.getGenericSignature();
	}

	@Override
	public char[][] getInterfaceNames() {
		return this.inputType.getInterfaceNames();
	}

	@Override
	public IBinaryNestedType[] getMemberTypes() {
		return this.inputType.getMemberTypes();
	}

	@Override
	public IBinaryMethod[] getMethods() {
		return this.inputType.getMethods();
	}

	@Override
	public char[][][] getMissingTypeNames() {
		return this.inputType.getMissingTypeNames();
	}

	@Override
	public char[] getName() {
		return this.inputType.getName();
	}

	@Override
	public char[] getSourceName() {
		return this.inputType.getSourceName();
	}

	@Override
	public char[] getSuperclassName() {
		return this.inputType.getSuperclassName();
	}

	@Override
	public long getTagBits() {
		return this.inputType.getTagBits();
	}

	@Override
	public boolean isAnonymous() {
		return this.inputType.isAnonymous();
	}

	@Override
	public boolean isLocal() {
		return this.inputType.isLocal();
	}

	@Override
	public boolean isMember() {
		return this.inputType.isMember();
	}

	@Override
	public char[] sourceFileName() {
		return this.inputType.sourceFileName();
	}

	@Override
	public int getModifiers() {
		return this.inputType.getModifiers();
	}

	/**
	 * Returns the zip file containing external annotations, if any. Returns null if there are no external annotations
	 * or if the basePath refers to a directory.
	 * 
	 * @param basePath
	 *            resolved filesystem path of either directory or zip file
	 * @param producer
	 *            an optional helper to produce the zipFile when needed.
	 * @return the client provided zip file; or else a fresh new zip file, to let clients cache it, if desired; or null
	 *         to signal that basePath is not a zip file, but a directory.
	 * @throws IOException
	 *             any unexpected errors during file access. File not found while accessing an individual file if
	 *             basePath is a directory <em>is</em> expected, and simply answered with null. If basePath is neither a
	 *             directory nor a zip file, this is unexpected.
	 */
	public static ZipFile getAnnotationZipFile(String basePath, ZipFileProducer producer) throws IOException {
		File annotationBase = new File(basePath);
		if (annotationBase.isDirectory()) {
			return null; // no zipFile
		}
		if (!annotationBase.exists())
			return null; // no zipFile, treat as not-yet-created directory
		return (producer != null ? producer.produce() : new ZipFile(annotationBase));
	}

	/**
	 * Creates an external annotation provider for external annotations using the given basePath, which is either a
	 * directory holding .eea text files, or a zip file of entries of the same format.
	 * 
	 * @param basePath
	 *            resolved filesystem path of either directory or zip file
	 * @param qualifiedBinaryTypeName
	 *            slash-separated type name
	 * @param zipFile
	 *            an existing zip file for the same basePath, or null.
	 * @return the annotation provider or null if there are no external annotations.
	 * @throws IOException
	 *             any unexpected errors during file access. File not found while accessing an individual file if
	 *             basePath is a directory <em>is</em> expected, and simply answered with null. If basePath is neither a
	 *             directory nor a zip file, this is unexpected.
	 */
	public static ExternalAnnotationProvider externalAnnotationProvider(String basePath, String qualifiedBinaryTypeName,
			ZipFile zipFile) throws IOException {
		String qualifiedBinaryFileName = qualifiedBinaryTypeName + ExternalAnnotationProvider.ANNOTATION_FILE_SUFFIX;
		if (zipFile == null) {
			File annotationBase = new File(basePath);
			if (annotationBase.isDirectory()) {
				String filePath = annotationBase.getAbsolutePath() + '/' + qualifiedBinaryFileName;
				return new ExternalAnnotationProvider(new FileInputStream(filePath), qualifiedBinaryTypeName);
			}
		} else {
			ZipEntry entry = zipFile.getEntry(qualifiedBinaryFileName);
			if (entry != null) {
				return new ExternalAnnotationProvider(zipFile.getInputStream(entry), qualifiedBinaryTypeName);
			}
		}
		return null;
	}

	/**
	 * Create and remember a provider for external annotations using the given basePath, which is either a directory
	 * holding .eea text files, or a zip file of entries of the same format.
	 * 
	 * @param basePath
	 *            resolved filesystem path of either directory or zip file
	 * @param qualifiedBinaryTypeName
	 *            slash-separated type name
	 * @param zipFile
	 *            an existing zip file for the same basePath, or null. Output: wl be filled with
	 * @return the client provided zip file; or else a fresh new zip file, to let clients cache it, if desired; or null
	 *         to signal that basePath is not a zip file, but a directory.
	 * @throws IOException
	 *             any unexpected errors during file access. File not found while accessing an individual file if
	 *             basePath is a directory <em>is</em> expected, and simply answered with null. If basePath is neither a
	 *             directory nor a zip file, this is unexpected.
	 */
	public static ClassWithExternalAnnotations create(IBinaryType toDecorate, String basePath,
			String qualifiedBinaryTypeName, ZipFile zipFile) throws IOException {
		return new ClassWithExternalAnnotations(toDecorate,
				externalAnnotationProvider(basePath, qualifiedBinaryTypeName, zipFile));
	}

	@Override
	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member,
			LookupEnvironment environment) {
		if (walker == ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER && this.annotationProvider != null) {
			if (member == null) {
				return this.annotationProvider.forTypeHeader(environment);
			} else if (member instanceof IBinaryField) {
				IBinaryField field = (IBinaryField) member;
				char[] fieldSignature = field.getGenericSignature();
				if (fieldSignature == null)
					fieldSignature = field.getTypeName();
				return this.annotationProvider.forField(field.getName(), fieldSignature, environment);
			} else if (member instanceof IBinaryMethod) {
				IBinaryMethod method = (IBinaryMethod) member;
				char[] methodSignature = method.getGenericSignature();
				if (methodSignature == null)
					methodSignature = method.getMethodDescriptor();
				return this.annotationProvider.forMethod(
						method.isConstructor() ? TypeConstants.INIT : method.getSelector(), methodSignature,
						environment);
			}
		}
		return walker;
	}

	@Override
	public ExternalAnnotationStatus getExternalAnnotationStatus() {
		if (this.annotationProvider == null) {
			if (this.isFromSource) {
				return ExternalAnnotationStatus.FROM_SOURCE;
			}
			return ExternalAnnotationStatus.NO_EEA_FILE;
		}
		return ExternalAnnotationStatus.TYPE_IS_ANNOTATED;
	}
}
