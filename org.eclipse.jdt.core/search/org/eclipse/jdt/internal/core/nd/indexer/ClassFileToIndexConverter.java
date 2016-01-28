package org.eclipse.jdt.internal.core.nd.indexer;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.TypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.SignatureWrapper;
import org.eclipse.jdt.internal.core.ClassFile;
import org.eclipse.jdt.internal.core.JarPackageFragmentRoot;
import org.eclipse.jdt.internal.core.JavaModelManager;
import org.eclipse.jdt.internal.core.Openable;
import org.eclipse.jdt.internal.core.PackageFragment;
import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.java.JavaIndex;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotationValuePair;
import org.eclipse.jdt.internal.core.nd.java.NdBinding;
import org.eclipse.jdt.internal.core.nd.java.NdComplexTypeSignature;
import org.eclipse.jdt.internal.core.nd.java.NdConstant;
import org.eclipse.jdt.internal.core.nd.java.NdConstantAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdConstantArray;
import org.eclipse.jdt.internal.core.nd.java.NdConstantClass;
import org.eclipse.jdt.internal.core.nd.java.NdConstantEnum;
import org.eclipse.jdt.internal.core.nd.java.NdMethod;
import org.eclipse.jdt.internal.core.nd.java.NdMethodException;
import org.eclipse.jdt.internal.core.nd.java.NdMethodId;
import org.eclipse.jdt.internal.core.nd.java.NdMethodParameter;
import org.eclipse.jdt.internal.core.nd.java.NdResourceFile;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeArgument;
import org.eclipse.jdt.internal.core.nd.java.NdTypeBound;
import org.eclipse.jdt.internal.core.nd.java.NdTypeId;
import org.eclipse.jdt.internal.core.nd.java.NdTypeInterface;
import org.eclipse.jdt.internal.core.nd.java.NdTypeParameter;
import org.eclipse.jdt.internal.core.nd.java.NdTypeSignature;
import org.eclipse.jdt.internal.core.nd.java.NdVariable;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;
import org.eclipse.jdt.internal.core.util.CharArrayBuffer;
import org.eclipse.jdt.internal.core.util.Util;

public class ClassFileToIndexConverter {
	private static final char[] COMMA = new char[]{','};
	private static final char[][] EMPTY_CHAR_ARRAY_ARRAY = new char[0][];
	private static final boolean ENABLE_LOGGING = false;
	private static final char[] EMPTY_CHAR_ARRAY = new char[0];
	private static final char[] PATH_SEPARATOR = new char[]{'/'};
	private NdResourceFile resource;
	private JavaIndex index;

	public ClassFileToIndexConverter(NdResourceFile resourceFile) {
		this.resource = resourceFile;
		this.index = JavaIndex.getIndex(resourceFile.getNd());
	}

	private Nd getNd() {
		return this.resource.getNd();
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
			throw new CoreException(Package.createStatus("Unable to parse JAR file", e)); //$NON-NLS-1$
		} finally {
			JavaModelManager.getJavaModelManager().closeZipFile(zipFile);
		}
		return info;
	}

	public NdType addType(IBinaryType binaryType, IProgressMonitor monitor) throws CoreException {
		char[] binaryName = binaryType.getName();
		logInfo("adding binary type " + new String(binaryName)); //$NON-NLS-1$

		NdTypeId name = createTypeIdFromBinaryName(binaryName);
		NdType type = name.findTypeByResourceAddress(this.resource.address);

		if (type == null) {
			type = new NdType(getNd(), this.resource);
		}

		ITypeAnnotationWalker typeAnnotations = getTypeAnnotationWalker(binaryType.getTypeAnnotations());
		ITypeAnnotationWalker supertypeAnnotations = typeAnnotations.toSupertype((short) -1,
				binaryType.getSuperclassName());

		type.setTypeId(name);

		char[][] interfaces = binaryType.getInterfaceNames();
		if (interfaces == null) {
			interfaces = EMPTY_CHAR_ARRAY_ARRAY;
		}
		// Create the default generic signature if the .class file didn't supply one
		SignatureWrapper signatureWrapper = GenericSignatures.getGenericSignature(binaryType);

		type.setModifiers(binaryType.getModifiers());
		type.setDeclaringType(createTypeIdFromBinaryName(binaryType.getEnclosingTypeName()));

		readTypeParameters(type, typeAnnotations, signatureWrapper);
		type.setSuperclass(createTypeSignature(supertypeAnnotations, signatureWrapper));

		short interfaceIdx = 0;
		while (signatureWrapper.start < signatureWrapper.signature.length) {
			// Note that there may be more interfaces listed in the generic signature than in the interfaces list.
			// Although the VM spec doesn't discuss this case specifically, there are .class files in the wild with
			// this characteristic. In such cases, we take what's in the generic signature and discard what's in the
			// interfaces list.
			char[] interfaceSpec = interfaceIdx < interfaces.length ? interfaces[interfaceIdx] : EMPTY_CHAR_ARRAY;
			new NdTypeInterface(getNd(), type,
					createTypeSignature(typeAnnotations.toSupertype(interfaceIdx, interfaceSpec), signatureWrapper));
			interfaceIdx++;
		}

		IBinaryAnnotation[] annotations = binaryType.getAnnotations();
		attachAnnotations(type, annotations);

		type.setDeclaringMethod(createMethodId(binaryType.getEnclosingTypeName(), binaryType.getEnclosingMethod()));

		IBinaryField[] fields = binaryType.getFields();

		if (fields != null) {
			for (IBinaryField nextField : fields) {
				addField(type, nextField);
			}
		}

		IBinaryMethod[] methods = binaryType.getMethods();

		if (methods != null) {
			for (IBinaryMethod next : methods) {
				addMethod(type, next, binaryName);
			}
		}

		char[][][] missingTypeNames = binaryType.getMissingTypeNames();
		char[] missingTypeString = getMissingTypeString(missingTypeNames);

		type.setMissingTypeNames(missingTypeString);
		type.setSourceFileName(binaryType.sourceFileName());
		type.setAnonymous(binaryType.isAnonymous());
		type.setIsLocal(binaryType.isLocal());
		type.setIsMember(binaryType.isMember());

		return type;
	}

	private static char[] getMissingTypeString(char[][][] missingTypeNames) {
		char[] missingTypeString = null;
		if (missingTypeNames != null) {
			CharArrayBuffer builder = new CharArrayBuffer();
			for (int typeIdx = 0; typeIdx < missingTypeNames.length; typeIdx++) {
				char[][] next = missingTypeNames[typeIdx];
				if (typeIdx != 0) {
					builder.append(COMMA);
				}
				if (next == null) {
					continue;
				}
				for (int segmentIdx = 0; segmentIdx < next.length; segmentIdx++) {
					char[] segment = next[segmentIdx];
					if (segment == null) {
						continue;
					}
					if (segmentIdx != 0) {
						builder.append(PATH_SEPARATOR);
					}
					builder.append(segment);
				}
			}
			missingTypeString = builder.getContents();
		}
		return missingTypeString;
	}

	private void attachAnnotations(NdBinding type, IBinaryAnnotation[] annotations) {
		if (annotations != null) {
			for (IBinaryAnnotation next : annotations) {
				createAnnotation(next).setParent(type);
			}
		}
	}

	/**
	 * Adds the given method to the given type
	 *
	 * @throws CoreException
	 */
	private void addMethod(NdType type, IBinaryMethod next, char[] binaryTypeName) throws CoreException {
		NdMethod method = new NdMethod(type);

		attachAnnotations(method, next.getAnnotations());

		ITypeAnnotationWalker typeAnnotations = getTypeAnnotationWalker(next.getTypeAnnotations());
		SignatureWrapper signature = GenericSignatures.getGenericSignature(next);
		readTypeParameters(method, typeAnnotations, signature);

		if (signature.charAtStart() == '(') {
			signature.start++;
		}

		char[][] parameterNames = next.getArgumentNames();
		short parameterIdx = 0;
		while (!signature.atEnd()) {
			if (signature.charAtStart() == ')') {
				signature.start++;
				break;
			}
			NdMethodParameter parameter = new NdMethodParameter(method,
					createTypeSignature(typeAnnotations.toMethodParameter(parameterIdx), signature));

			if (parameterNames != null && parameterNames.length > parameterIdx) {
				parameter.setName(parameterNames[parameterIdx]);
			}
			parameterIdx++;
		}

		method.setReturnType(createTypeSignature(typeAnnotations.toMethodReturn(), signature));

		int throwsIdx = 0;
		while (!signature.atEnd() && signature.charAtStart() == '^') {
			signature.start++;
			new NdMethodException(method, createTypeSignature(typeAnnotations.toThrows(throwsIdx), signature));
			throwsIdx++;
		}

		Object defaultValue = next.getDefaultValue();
		if (defaultValue != null) {
			method.setDefaultValue(createConstantFromMixedType(defaultValue));
		}

		method.setMethodId(createMethodId(binaryTypeName, next.getSelector(), next.getMethodDescriptor()));

		method.setModifiers(next.getModifiers());
	}

	/**
	 * Adds the given field to the given type
	 */
	private void addField(NdType type, IBinaryField nextField) throws CoreException {
		NdVariable variable = new NdVariable(type);

		variable.setName(nextField.getName());

		IBinaryAnnotation[] binaryAnnotations = nextField.getAnnotations();
		if (binaryAnnotations != null) {
			for (IBinaryAnnotation nextAnnotation : binaryAnnotations) {
				createAnnotation(nextAnnotation).setParent(variable);
			}
		}

		variable.setConstant(NdConstant.create(getNd(), nextField.getConstant()));
		variable.setModifiers(nextField.getModifiers());
		SignatureWrapper nextTypeSignature = GenericSignatures.getGenericSignatureFor(nextField);

		ITypeAnnotationWalker annotationWalker = getTypeAnnotationWalker(nextField.getTypeAnnotations());
		variable.setType(createTypeSignature(annotationWalker, nextTypeSignature));
	}

	/**
	 * Reads and attaches any generic type parameters at the current start position in the given wrapper. Sets
	 * wrapper.start to the character following the type parameters.
	 *
	 * @throws CoreException
	 */
	private void readTypeParameters(NdBinding type, ITypeAnnotationWalker annotationWalker, SignatureWrapper wrapper)
			throws CoreException {
		char[] genericSignature = wrapper.signature;
		if (genericSignature.length == 0 || wrapper.charAtStart() != '<') {
			return;
		}

		int parameterIndex = 0;
		int boundIndex = 0;
		int indexOfClosingBracket = wrapper.skipAngleContents(wrapper.start) - 1;
		wrapper.start++;
		NdTypeParameter parameter = null;
		while (wrapper.start < indexOfClosingBracket) {
			int colonPos = CharOperation.indexOf(':', genericSignature, wrapper.start, indexOfClosingBracket);

			if (colonPos > wrapper.start) {
				char[] identifier = CharOperation.subarray(genericSignature, wrapper.start, colonPos);
				parameter = new NdTypeParameter(type, identifier);
				wrapper.start = colonPos + 1;
				parameterIndex++;
				boundIndex = 0;
			}

			// Class files insert an empty bound if there is an interface bound but no class bound. We just omit
			// the bound entirely.
			while (genericSignature[wrapper.start] == ':') {
				wrapper.start++;
			}

			NdTypeSignature boundSignature = createTypeSignature(
					annotationWalker.toTypeParameter(true, parameterIndex).toTypeBound((short) boundIndex), wrapper);

			new NdTypeBound(parameter, boundSignature);
			boundIndex++;
		}

		if (genericSignature[wrapper.start] == '>') {
			wrapper.start++;
		}
	}

	/**
	 * Reads a type signature from the given {@link SignatureWrapper}, starting at the character pointed to by
	 * wrapper.start. On return, wrapper.start will point to the first character following the type signature. Returns
	 * null if given an empty signature or the signature for the void type.
	 *
	 * @param annotations
	 *            the type annotations for this type
	 * @param wrapper
	 *            the generic signature to parse
	 * @throws CoreException
	 */
	private NdTypeSignature createTypeSignature(ITypeAnnotationWalker annotations, SignatureWrapper wrapper)
			throws CoreException {
		char[] genericSignature = wrapper.signature;

		if (genericSignature == null || genericSignature.length == 0) {
			return null;
		}

		char firstChar = genericSignature[wrapper.start];
		switch (firstChar) {
			case 'T': {
				// Skip the 'T' prefix
				wrapper.start++;
				NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
				typeSignature.setVariableIdentifier(wrapper.nextWord());
				attachAnnotations(typeSignature, annotations);
				// Skip the trailing semicolon
				wrapper.start++;
				return typeSignature;
			}
			case '[': {
				// Skip the '[' prefix
				wrapper.start++;
				// We encode arrays as though they were a one-argument generic type called '[' whose element
				// type is the generic argument.
				NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
				typeSignature.setRawType(createTypeIdFromFieldDescriptor(new char[] { '[' }));
				NdTypeArgument typeArgument = new NdTypeArgument(getNd(), typeSignature);
				NdTypeSignature elementType = createTypeSignature(annotations.toNextArrayDimension(), wrapper);
				typeArgument.setType(elementType);
				attachAnnotations(typeSignature, annotations);
				return typeSignature;
			}
			case 'V':
				return null;
			case 'B':
			case 'C':
			case 'D':
			case 'F':
			case 'I':
			case 'J':
			case 'S':
			case 'Z':
				wrapper.start++;
				return createTypeIdFromFieldDescriptor(new char[] { firstChar });
			case 'L':
				return parseClassTypeSignature(null, annotations, wrapper);
			case '+':
			case '-':
			case '*':
				throw new CoreException(Package.createStatus("Unexpected wildcard in top-level of generic signature: " //$NON-NLS-1$
						+ wrapper.toString()));
			default:
				throw new CoreException(Package.createStatus("Generic signature starts with unknown character: " //$NON-NLS-1$
						+ wrapper.toString()));
		}
	}

	/**
	 * Parses a ClassTypeSignature (as described in section 4.7.9.1 of the Java VM Specification Java SE 8 Edition). The
	 * read pointer should be located just after the identifier. The caller is expected to have already read the field
	 * descriptor for the type.
	 */
	private NdTypeSignature parseClassTypeSignature(NdComplexTypeSignature parentTypeOrNull,
			ITypeAnnotationWalker annotations, SignatureWrapper wrapper) throws CoreException {
		char[] identifier = wrapper.nextName();
		char[] fieldDescriptor;

		if (parentTypeOrNull != null) {
			fieldDescriptor = CharArrayUtils.concat(parentTypeOrNull.getRawType().getFieldDescriptor().getChars(),
					new char[] { '$' }, identifier);
		} else {
			fieldDescriptor = identifier;
		}

		char[] genericSignature = wrapper.signature;
		boolean hasGenericArguments = (genericSignature.length > wrapper.start)
				&& genericSignature[wrapper.start] == '<';
		boolean isRawTypeWithNestedClass = genericSignature[wrapper.start] == '.';
		NdTypeId rawType = createTypeIdFromFieldDescriptor(fieldDescriptor);
		NdTypeSignature result = rawType;

		boolean checkForSemicolon = true;
		// Special optimization for signatures with no type annotations, no arrays, and no generic arguments that
		// are not an inner type of a class that can't use this optimization. Basically, if there would be no attributes
		// set on a PDOMComplexTypeSignature besides what it picks up from its raw type, we just use the raw type.
		IBinaryAnnotation[] annotationList = annotations.getAnnotationsAtCursor(0);
		if (annotationList.length != 0 || hasGenericArguments || parentTypeOrNull != null || isRawTypeWithNestedClass) {
			NdComplexTypeSignature typeSignature = new NdComplexTypeSignature(getNd());
			typeSignature.setRawType(rawType);
			attachAnnotations(typeSignature, annotations);

			if (hasGenericArguments) {
				wrapper.start++;
				short argumentIndex = 0;
				while (wrapper.start < genericSignature.length && (genericSignature[wrapper.start] != '>')) {
					NdTypeArgument typeArgument = new NdTypeArgument(getNd(), typeSignature);

					switch (genericSignature[wrapper.start]) {
						case '+': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_SUPER);
							wrapper.start++;
							break;
						}
						case '-': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_EXTENDS);
							wrapper.start++;
							break;
						}
						case '*': {
							typeArgument.setWildcard(NdTypeArgument.WILDCARD_QUESTION);
							wrapper.start++;
							argumentIndex++;
							continue;
						}
					}

					NdTypeSignature nextSignature = createTypeSignature(annotations.toTypeArgument(argumentIndex),
							wrapper);
					typeArgument.setType(nextSignature);
					argumentIndex++;
				}

				// Skip over the trailing '>'
				wrapper.start++;
			}
			result = typeSignature;

			if (parentTypeOrNull != null) {
				typeSignature.setGenericDeclaringType(parentTypeOrNull);
			}

			if (genericSignature[wrapper.start] == '.') {
				// Don't check for a semicolon if we hit this branch since the recursive call to parseClassTypeSignature
				// will do this
				checkForSemicolon = false;
				result = parseClassTypeSignature(typeSignature, annotations.toNextNestedType(), wrapper);
			}
		}

		if (checkForSemicolon && wrapper.start < genericSignature.length && genericSignature[wrapper.start] == ';') {
			wrapper.start++;
		}

		return result;
	}

	/**
	 * @param typeSignature
	 * @param annotations
	 */
	private void attachAnnotations(NdComplexTypeSignature typeSignature, ITypeAnnotationWalker annotations) {
		IBinaryAnnotation[] annotationList = annotations.getAnnotationsAtCursor(0);

		for (IBinaryAnnotation next : annotationList) {
			NdAnnotation annotation = createAnnotation(next);

			annotation.setParent(typeSignature);
		}
	}

	private ITypeAnnotationWalker getTypeAnnotationWalker(IBinaryTypeAnnotation[] typeAnnotations) {
		if (typeAnnotations == null) {
			return ITypeAnnotationWalker.EMPTY_ANNOTATION_WALKER;
		}
		return new TypeAnnotationWalker(typeAnnotations);
	}

	private NdTypeId createTypeIdFromFieldDescriptor(char[] typeName) {
		if (typeName == null) {
			return null;
		}
		return this.index.createTypeId(typeName);
	}

	/**
	 * Creates a method ID given a method selector, method descriptor, and binary type name
	 */
	private NdMethodId createMethodId(char[] binaryTypeName, char[] methodSelector, char[] methodDescriptor) {
		if (methodSelector == null || binaryTypeName == null || methodDescriptor == null) {
			return null;
		}

		char[] methodId = JavaNames.getMethodId(binaryTypeName, methodSelector, methodDescriptor);
		return this.index.createMethodId(methodId);
	}

	/**
	 * Creates a method ID given a method name (which is a method selector followed by a method descriptor.
	 */
	private NdMethodId createMethodId(char[] binaryTypeName, char[] methodName) {
		if (methodName == null || binaryTypeName == null) {
			return null;
		}

		char[] methodId = JavaNames.getMethodId(binaryTypeName, methodName);
		return this.index.createMethodId(methodId);
	}

	private NdAnnotation createAnnotation(IBinaryAnnotation next) {
		NdAnnotation result = new NdAnnotation(getNd(), createTypeIdFromBinaryName(next.getTypeName()));

		IBinaryElementValuePair[] pairs = next.getElementValuePairs();

		if (pairs != null) {
			for (IBinaryElementValuePair element : pairs) {
				NdAnnotationValuePair nextPair = new NdAnnotationValuePair(result, element.getName());
				nextPair.setValue(createConstantFromMixedType(element.getValue()));
			}
		}

		return result;
	}

	private void logInfo(String string) {
		if (ENABLE_LOGGING) {
			Package.logInfo(string);
		}
	}

	private NdTypeId createTypeIdFromBinaryName(char[] binaryName) {
		if (binaryName == null) {
			return null;
		}

		return this.index.createTypeId(JavaNames.binaryNameToFieldDescriptor(binaryName));
	}

	/**
	 *
	 * @param value
	 *            accepts all values returned from {@link IBinaryElementValuePair#getValue()}
	 */
	public NdConstant createConstantFromMixedType(Object value) {
		if (value instanceof Constant) {
			Constant constant = (Constant) value;

			return NdConstant.create(getNd(), constant);
		} else if (value instanceof ClassSignature) {
			ClassSignature signature = (ClassSignature) value;

			char[] binaryName = JavaNames.binaryNameToFieldDescriptor(signature.getTypeName());
			NdTypeSignature typeId = this.index.createTypeId(binaryName);
			return NdConstantClass.create(getNd(), typeId);
		} else if (value instanceof IBinaryAnnotation) {
			IBinaryAnnotation binaryAnnotation = (IBinaryAnnotation) value;

			return NdConstantAnnotation.create(getNd(), createAnnotation(binaryAnnotation));
		} else if (value instanceof Object[]) {
			NdConstantArray result = new NdConstantArray(getNd());
			Object[] array = (Object[]) value;

			for (Object next : array) {
				NdConstant nextConstant = createConstantFromMixedType(next);
				nextConstant.setParent(result);
			}
			return result;
		} else if (value instanceof EnumConstantSignature) {
			EnumConstantSignature signature = (EnumConstantSignature) value;

			NdConstantEnum result = NdConstantEnum.create(createTypeIdFromBinaryName(signature.getTypeName()),
					new String(signature.getEnumConstantName()));

			return result;
		}
		throw new IllegalStateException("Unknown constant type " + value.getClass().getName()); //$NON-NLS-1$
	}
}
