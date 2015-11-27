package org.eclipse.jdt.internal.core.pdom.indexer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.pdom.PDOM;
import org.eclipse.jdt.internal.core.pdom.java.JavaIndex;
import org.eclipse.jdt.internal.core.pdom.java.JavaNames;
import org.eclipse.jdt.internal.core.pdom.java.PDOMAnnotation;
import org.eclipse.jdt.internal.core.pdom.java.PDOMAnnotationValuePair;
import org.eclipse.jdt.internal.core.pdom.java.PDOMConstant;
import org.eclipse.jdt.internal.core.pdom.java.PDOMConstantAnnotation;
import org.eclipse.jdt.internal.core.pdom.java.PDOMConstantArray;
import org.eclipse.jdt.internal.core.pdom.java.PDOMConstantClass;
import org.eclipse.jdt.internal.core.pdom.java.PDOMConstantEnum;
import org.eclipse.jdt.internal.core.pdom.java.PDOMMethodId;
import org.eclipse.jdt.internal.core.pdom.java.PDOMResourceFile;
import org.eclipse.jdt.internal.core.pdom.java.PDOMType;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTypeId;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTypeInterface;
import org.eclipse.jdt.internal.core.pdom.java.PDOMTypeSignature;
import org.eclipse.jdt.internal.core.pdom.java.PDOMVariable;
import org.eclipse.jdt.internal.core.util.Util;

public class ClassFileToIndexConverter {
	private static final boolean ENABLE_LOGGING = false;
	private PDOMResourceFile resource;
	private JavaIndex index;

	public ClassFileToIndexConverter(PDOMResourceFile resourceFile) {
		this.resource = resourceFile;
		this.index = JavaIndex.getIndex(resourceFile.getPDOM());
	}

	private PDOM getPDOM() {
		return this.resource.getPDOM();
	}

	public static IBinaryType getTypeFromClassFile(IClassFile iClassFile, IProgressMonitor monitor)
			throws CoreException {
		ClassFile classFile = (ClassFile) iClassFile;
		// cache binary type binding
		IBinaryType binaryType = (IBinaryType) JavaModelManager.getJavaModelManager().getInfo(classFile.getType());
		if (binaryType == null) {
			// create binary type from file
			if (classFile.getPackageFragmentRoot().isArchive()) {
				binaryType = createInfoFromClassFileInJar(classFile);
			} else {
				binaryType = createInfoFromClassFile(classFile.resource());
			}
		}

		return binaryType;
	}

	/**
	 * Creates the type info from the given class file on disk and adds it to the given list of infos.
	 * 
	 * @throws CoreException
	 */
	protected static IBinaryType createInfoFromClassFile(IResource file) throws CoreException {
		IBinaryType info = null;
		try {
			info = Util.newClassFileReader(file);
		} catch (Exception e) {
			throw new CoreException(Package.createStatus("Unable to parse class file", e)); //$NON-NLS-1$
		}
		return info;
	}

	/**
	 * Create a type info from the given class file in a jar and adds it to the given list of infos.
	 * 
	 * @throws CoreException
	 */
	protected static IBinaryType createInfoFromClassFileInJar(Openable classFile) throws CoreException {
		PackageFragment pkg = (PackageFragment) classFile.getParent();
		String classFilePath = Util.concatWith(pkg.names, classFile.getElementName(), '/');
		IBinaryType info = null;
		java.util.zip.ZipFile zipFile = null;
		try {
			zipFile = ((JarPackageFragmentRoot) pkg.getParent()).getJar();
			info = org.eclipse.jdt.internal.compiler.classfmt.ClassFileReader.read(zipFile, classFilePath);
		} catch (Exception e) {
			throw new CoreException(Package.createStatus("Unable to parse JAR file", e));
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
		}
		return info;
	}

	public PDOMType addType(IBinaryType binaryType, IProgressMonitor monitor) {
		char[] binaryName = binaryType.getName();
		logInfo("adding binary type " + new String(binaryName));

		PDOMTypeId name = createTypeIdFromBinaryName(binaryName);
		PDOMType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new PDOMType(getPDOM(), this.resource);
		}

		type.setTypeId(name);
		type.setSuperclass(createTypeIdFromBinaryName(binaryType.getSuperclassName()));
		type.setModifiers(binaryType.getModifiers());
		type.setDeclaringType(createTypeIdFromBinaryName(binaryType.getEnclosingTypeName()));

		IBinaryAnnotation[] annotations = binaryType.getAnnotations();
		if (annotations != null) {
			for (IBinaryAnnotation next : annotations) {
				createAnnotation(next).setParent(type);
			}
		}

		type.setDeclaringMethod(createMethodId(binaryType.getEnclosingTypeName(), binaryType.getEnclosingMethod()));

		IBinaryField[] fields = binaryType.getFields();

		if (fields != null) {
			for (IBinaryField nextField : fields) {
				PDOMVariable variable = new PDOMVariable(getPDOM(), type);

				variable.setType(createTypeIdFromFieldDescriptor(nextField.getTypeName()));
				variable.setName(new String(nextField.getName()));

				IBinaryAnnotation[] binaryAnnotations = nextField.getAnnotations();
				if (binaryAnnotations != null) {
					for (IBinaryAnnotation nextAnnotation : binaryAnnotations) {
						createAnnotation(nextAnnotation).setParent(variable);
					}
				}

				variable.setConstant(PDOMConstant.create(getPDOM(), nextField.getConstant()));
				variable.setModifiers(nextField.getModifiers());

				// TODO(sxenos): Finish pulling in the rest of the fields from IBinaryField
			}
		}

		// genericSignature = binaryType.getGenericSignature();

		char[][] interfaces = binaryType.getInterfaceNames();
		if (interfaces != null) {
			for (char[] next : interfaces) {
				new PDOMTypeInterface(getPDOM(), type, createTypeIdFromBinaryName(next));
			}
		}

		return type;
	}

	private PDOMTypeSignature createTypeIdFromFieldDescriptor(char[] typeName) {
		if (typeName == null) {
			return null;
		}
		return this.index.createTypeId(typeName);
	}

	/**
	 * Creates a method ID given a method descriptor (which is a method selector followed by a method descriptor. For
	 * example: "
	 * 
	 * @param methodName
	 * @return
	 */
	private PDOMMethodId createMethodId(char[] binaryTypeName, char[] methodName) {
		if (methodName == null || binaryTypeName == null) {
			return null;
		}

		String methodId = JavaNames.methodNameToMethodId(
				JavaNames.binaryNameToFieldDescriptor(new String(binaryTypeName)), new String(methodName));
		return this.index.createMethodId(methodId);
	}

	private PDOMAnnotation createAnnotation(IBinaryAnnotation next) {
		PDOMAnnotation result = new PDOMAnnotation(getPDOM(), createTypeIdFromBinaryName(next.getTypeName()));

		IBinaryElementValuePair[] pairs = next.getElementValuePairs();

		if (pairs != null) {
			for (IBinaryElementValuePair element : pairs) {
				PDOMAnnotationValuePair nextPair = new PDOMAnnotationValuePair(result, new String(element.getName()));
				nextPair.setValue(createFromMixedType(element.getValue()));
			}
		}

		return result;
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private PDOMTypeId createTypeIdFromBinaryName(char[] binaryName) {
		if (binaryName == null) {
			return null;
		}

		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(new String(binaryName)));
	}

	/**
	 * 
	 * @param value
	 *            accepts all values returned from {@link {@link IBinaryElementValuePair#getValue()}
	 */
	public PDOMConstant createFromMixedType(Object value) {
		if (value instanceof Constant) {
			Constant constant = (Constant) value;

			return PDOMConstant.create(getPDOM(), constant);
		} else if (value instanceof ClassSignature) {
			ClassSignature signature = (ClassSignature) value;

			String binaryName = JavaNames.binaryNameToFieldDescriptor(new String(signature.getTypeName()));
			PDOMTypeSignature typeId = this.index.createTypeId(binaryName);
			return PDOMConstantClass.create(getPDOM(), typeId);
		} else if (value instanceof IBinaryAnnotation) {
			IBinaryAnnotation binaryAnnotation = (IBinaryAnnotation) value;

			return PDOMConstantAnnotation.create(getPDOM(), createAnnotation(binaryAnnotation));
		} else if (value instanceof Object[]) {
			PDOMConstantArray result = new PDOMConstantArray(getPDOM());
			Object[] array = (Object[]) value;

			for (Object next : array) {
				PDOMConstant nextConstant = createFromMixedType(next);
				nextConstant.setParent(result);
			}
			return result;
		} else if (value instanceof EnumConstantSignature) {
			EnumConstantSignature signature = (EnumConstantSignature) value;

			PDOMConstantEnum result = PDOMConstantEnum.create(createTypeIdFromBinaryName(signature.getTypeName()),
					new String(signature.getEnumConstantName()));

			return result;
		}
		throw new IllegalStateException("Unknown constant type " + value.getClass().getName()); //$NON-NLS-1$
	}
}
