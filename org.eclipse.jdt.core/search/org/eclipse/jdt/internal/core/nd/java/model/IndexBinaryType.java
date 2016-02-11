package org.eclipse.jdt.internal.core.nd.java.model;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.classfmt.BinaryTypePrinter;
import org.eclipse.jdt.internal.compiler.classfmt.ElementValuePairInfo;
import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryNestedType;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.env.ITypeAnnotationWalker;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.nd.DatabaseRef;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotationValuePair;
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

/**
 * Implementation of {@link IBinaryType} that reads all its content from the index
 */
public class IndexBinaryType implements IBinaryType {
	private final DatabaseRef<NdType> typeRef;

	private boolean enclosingInitialized;
	private char[] enclosingMethod;
	private char[] enclosingType;

	public IndexBinaryType(DatabaseRef<NdType> type) {
		this.typeRef = type;
	}

	@Override
	public int getModifiers() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getModifiers();
			} else {
				return 0;
			}
		}
	}

	@Override
	public boolean isBinaryType() {
		return true;
	}

	@Override
	public char[] getFileName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getFile().getPath().toString().toCharArray();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public IBinaryAnnotation[] getAnnotations() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return toAnnotationArray(this.typeRef.get().getAnnotations());
			} else {
				return null;
			}
		}
	}

	private static IBinaryAnnotation[] toAnnotationArray(List<NdAnnotation> annotations) {
		if (annotations.isEmpty()) {
			return null;
		}
		IBinaryAnnotation[] result = new IBinaryAnnotation[annotations.size()];

		for (int idx = 0; idx < result.length; idx++) {
			result[idx] = createBinaryAnnotation(annotations.get(idx));
		}
		return result;
	}

	@Override
	public IBinaryTypeAnnotation[] getTypeAnnotations() {
		List<IBinaryTypeAnnotation> result = new ArrayList<>();
		ITypeAnnotationBuilder annotationBuilder = TypeAnnotationBuilder.create();
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				NdTypeSignature superclass = type.getSuperclass();
				if (superclass != null) {
					buildAnnotations(result, annotationBuilder.toSupertype((short)-1), superclass);
				}

				List<NdTypeInterface> interfaces = type.getInterfaces();

				for (short interfaceIdx = 0; interfaceIdx < interfaces.size(); interfaceIdx++) {
					NdTypeInterface next = interfaces.get(interfaceIdx);

					buildAnnotations(result, annotationBuilder.toSupertype(interfaceIdx), next.getInterface());
				}
			}
		}
		return toTypeAnnotationArray(result);
	}

	@Override
	public char[] getEnclosingMethod() {
		initEnclosing();

		return this.enclosingMethod;
	}

	@Override
	public char[] getEnclosingTypeName() {
		initEnclosing();

		return this.enclosingType;
	}

	@Override
	public IBinaryField[] getFields() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdVariable> variables = type.getVariables();

				if (variables.isEmpty()) {
					return null;
				}

				IBinaryField[] result = new IBinaryField[variables.size()];
				for (int fieldIdx = 0; fieldIdx < variables.size(); fieldIdx++) {
					result[fieldIdx] = createBinaryField(variables.get(fieldIdx));
				}
				return result;
			} else {
				return new IBinaryField[0];
			}
		}
	}

	@Override
	public char[] getGenericSignature() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				CharArrayBuffer buffer = new CharArrayBuffer();

				getSignature(buffer, type.getTypeParameters());

				NdTypeSignature superclass = type.getSuperclass();
				superclass.getSignature(buffer);
				for (NdTypeInterface nextInterface : type.getInterfaces()) {
					nextInterface.getInterface().getSignature(buffer);
				}
				return buffer.getContents();
			} else {
				return null;
			}
		}
	}

	@Override
	public char[][] getInterfaceNames() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdTypeInterface> interfaces = type.getInterfaces();

				if (interfaces.isEmpty()) {
					return null;
				}

				char[][] result = new char[interfaces.size()][];
				for (int idx = 0; idx < interfaces.size(); idx++) {
					NdTypeSignature nextInterface = interfaces.get(idx).getInterface();

					result[idx] = nextInterface.getRawType().getBinaryName();
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public IBinaryNestedType[] getMemberTypes() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdType> declaredTypes = type.getTypeId().getDeclaredTypes();
				if (declaredTypes.isEmpty()) {
					return null;
				}

				NdResourceFile resFile = type.getResourceFile();
				IString javaRoot = resFile.getPackageFragmentRoot();

				// Filter out all the declared types which are at different java roots (only keep the ones belonging
				// to the same .jar file or to another .class file in the same folder).
				List<IBinaryNestedType> result = new ArrayList<>();
				for (NdType next : declaredTypes) {
					NdResourceFile nextResFile = next.getResourceFile();

					if (nextResFile.getPackageFragmentRoot().compare(javaRoot, true) == 0) {
						result.add(createBinaryNestedType(next));
					}
				}
				return result.isEmpty() ? null : result.toArray(new IBinaryNestedType[result.size()]);
			} else {
				return null;
			}
		}
	}

	private IBinaryNestedType createBinaryNestedType(NdType next) {
		return new IndexBinaryNestedType(next.getTypeId().getBinaryName(), next.getDeclaringType().getBinaryName(),
				next.getModifiers());
	}

	@Override
	public IBinaryMethod[] getMethods() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				List<NdMethod> methods = type.getMethods();

				if (methods.isEmpty()) {
					return null;
				}

				IBinaryMethod[] result = new IBinaryMethod[methods.size()];
				for (int idx = 0; idx < result.length; idx++) {
					result[idx] = createBinaryMethod(methods.get(idx));
				}

				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public char[][][] getMissingTypeNames() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				IString string = type.getMissingTypeNames();
				if (string.length() == 0) {
					return null;
				}
				char[] missingTypeNames = string.getChars();
				char[][] paths = CharOperation.splitOn(',', missingTypeNames);
				char[][][] result = new char[paths.length][][];
				for (int idx = 0; idx < paths.length; idx++) {
					result[idx] = CharOperation.splitOn('/', paths[idx]);
				}
				return result;
			} else {
				return null;
			}
		}
	}

	@Override
	public char[] getName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getTypeId().getBinaryName();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public char[] getSourceName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getSourceName();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public char[] getSuperclassName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getSuperclass().getRawType().getBinaryName();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public long getTagBits() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getTagBits();
			} else {
				return 0;
			}
		}
	}

	@Override
	public boolean isAnonymous() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.isAnonymous();
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean isLocal() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.isLocal();
			} else {
				return false;
			}
		}
	}

	@Override
	public boolean isMember() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.isMember();
			} else {
				return false;
			}
		}
	}

	@Override
	public char[] sourceFileName() {
		try (IReader rl = this.typeRef.lock()) {
			NdType type = this.typeRef.get();
			if (type != null) {
				return type.getSourceFileName().getChars();
			} else {
				return new char[0];
			}
		}
	}

	@Override
	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member,
			LookupEnvironment environment) {
		return walker;
	}

	private void getSignature(CharArrayBuffer buffer, List<NdTypeParameter> params) {
		if (!params.isEmpty()) {
			buffer.append('<');
			for (NdTypeParameter next : params) {
				next.getSignature(buffer);
			}
			buffer.append('>');
		}
	}

	private IBinaryMethod createBinaryMethod(NdMethod ndMethod) {
		NdMethodId methodId = ndMethod.getMethodId();

		List<IBinaryTypeAnnotation> typeAnnotations = new ArrayList<>();
		ITypeAnnotationBuilder annotationBuilder = TypeAnnotationBuilder.create();

		List<NdTypeParameter> typeParameters = ndMethod.getTypeParameters();
		for (int parameterIdx = 0; parameterIdx < typeParameters.size(); parameterIdx++) {
			NdTypeParameter next = typeParameters.get(parameterIdx);

			List<NdTypeBound> bounds = next.getBounds();
			for (int boundsIdx = 0; boundsIdx < bounds.size(); boundsIdx++) {
				NdTypeBound nextBound = bounds.get(boundsIdx);

				NdTypeSignature type = nextBound.getType();

				if (type != null) {
					buildAnnotations(typeAnnotations,
							annotationBuilder.toTypeParameter(false, parameterIdx).toTypeBound((short) boundsIdx),
							type);
				}
			}
		}

		List<NdMethodParameter> args = ndMethod.getMethodParameters();
		for (int argIdx = 0; argIdx < args.size(); argIdx++) {
			buildAnnotations(typeAnnotations, annotationBuilder.toMethodParameter((short) argIdx),
					args.get(argIdx).getAnnotations());
		}

		buildAnnotations(typeAnnotations, annotationBuilder.toMethodReturn(), ndMethod.getReturnType());

		List<NdMethodException> exceptions = ndMethod.getExceptions();

		for (int exceptionIdx = 0; exceptionIdx < exceptions.size(); exceptionIdx++) {
			NdMethodException next = exceptions.get(exceptionIdx);

			buildAnnotations(typeAnnotations, annotationBuilder.toThrows(exceptionIdx), next.getExceptionType());
		}

		return IndexBinaryMethod.create().setAnnotations(toAnnotationArray(ndMethod.getAnnotations()))
				.setModifiers(ndMethod.getModifiers()).setIsConstructor(methodId.isConstructor())
				.setArgumentNames(getArgumentNames(ndMethod)).setDefaultValue(unpackValue(ndMethod.getDefaultValue()))
				.setExceptionTypeNames(getExceptionTypeNames(ndMethod))
				.setGenericSignature(getGenericSignatureFor(ndMethod))
				.setMethodDescriptor(methodId.getMethodDescriptor())
				.setParameterAnnotations(getParameterAnnotations(ndMethod))
				.setSelector(ndMethod.getMethodId().getSelector()).setTagBits(ndMethod.getTagBits())
				.setIsClInit(methodId.isClInit()).setTypeAnnotations(toTypeAnnotationArray(typeAnnotations));
	}

	private char[][] getArgumentNames(NdMethod ndMethod) {
		// Unlike what its JavaDoc says, IBinaryType returns an empty array if no argument names are available, so
		// we replicate this weird undocumented corner case here.
		char[][] result = ndMethod.getArgumentNames();
		int lastNonEmpty = -1;
		for (int idx = 0; idx < result.length; idx++) {
			if (result[idx] != null && result[idx].length != 0) {
				lastNonEmpty = idx;
			}
		}

		if (lastNonEmpty != result.length - 1) {
			char[][] newResult = new char[lastNonEmpty + 1][];
			System.arraycopy(result, 0, newResult, 0, lastNonEmpty + 1);
			return newResult;
		}
		return result;
	}

	private IBinaryTypeAnnotation[] toTypeAnnotationArray(List<IBinaryTypeAnnotation> result) {
		return result.isEmpty() ? null
				: (IBinaryTypeAnnotation[]) result.toArray(new IBinaryTypeAnnotation[result.size()]);
	}

	private IBinaryAnnotation[][] getParameterAnnotations(NdMethod ndMethod) {
		List<NdMethodParameter> parameters = ndMethod.getMethodParameters();
		if (parameters.isEmpty()) {
			return null;
		}

		IBinaryAnnotation[][] result = new IBinaryAnnotation[parameters.size()][];
		for (int idx = 0; idx < parameters.size(); idx++) {
			NdMethodParameter next = parameters.get(idx);

			result[idx] = toAnnotationArray(next.getAnnotations());
		}

		int newLength = result.length;
		while (newLength > 0 && result[newLength - 1] == null) {
			--newLength;
		}

		if (newLength < result.length) {
			if (newLength == 0) {
				return null;
			}
			IBinaryAnnotation[][] newResult = new IBinaryAnnotation[newLength][];
			System.arraycopy(result, 0, newResult, 0, newLength);
			result = newResult;
		}

		return result;
	}

	private char[] getGenericSignatureFor(NdMethod method) {
		CharArrayBuffer result = new CharArrayBuffer();
		getSignature(result, method.getTypeParameters());

		result.append('(');
		for (NdMethodParameter next : method.getMethodParameters()) {
			next.getType().getSignature(result);
		}
		result.append(')');
		NdTypeSignature returnType = method.getReturnType();
		if (returnType == null) {
			result.append('V');
		} else {
			returnType.getSignature(result);
		}
		List<NdMethodException> exceptions = method.getExceptions();
		for (NdMethodException next : exceptions) {
			result.append('^');
			next.getExceptionType().getSignature(result);
		}
		return result.getContents();
	}

	private char[][] getExceptionTypeNames(NdMethod ndMethod) {
		List<NdMethodException> exceptions = ndMethod.getExceptions();

		// Although the JavaDoc for IBinaryMethod says that the exception list will be null if empty,
		// the implementation in MethodInfo returns an empty array rather than null. We copy the
		// same behavior here in case something is relying on it. Uncomment the following if the "null"
		// version is deemed correct.

		// if (exceptions.isEmpty()) {
		// return null;
		// }

		char[][] result = new char[exceptions.size()][];
		for (int idx = 0; idx < exceptions.size(); idx++) {
			NdMethodException next = exceptions.get(idx);

			result[idx] = next.getExceptionType().getRawType().getBinaryName();
		}
		return result;
	}

	public static IBinaryField createBinaryField(NdVariable ndVariable) {
		char[] name = ndVariable.getName().getChars();
		Constant constant = null;
		NdConstant ndConstant = ndVariable.getConstant();
		if (ndConstant != null) {
			constant = ndConstant.getConstant();
		}
		if (constant == null) {
			constant = Constant.NotAConstant;
		}

		List<IBinaryTypeAnnotation> typeAnnotations = new ArrayList<>();
		NdTypeSignature type = ndVariable.getType();
		buildAnnotations(typeAnnotations, TypeAnnotationBuilder.create(), type);
		IBinaryTypeAnnotation[] typeAnnotationArray = typeAnnotations.isEmpty() ? null
				: (IBinaryTypeAnnotation[]) typeAnnotations.toArray(new IBinaryTypeAnnotation[typeAnnotations.size()]);

		IBinaryAnnotation[] annotations = toAnnotationArray(ndVariable.getAnnotations());

		CharArrayBuffer signature = new CharArrayBuffer();
		type.getSignature(signature);

		long tagBits = ndVariable.getTagBits();
		return new IndexBinaryField(annotations, constant, signature.getContents(), ndVariable.getModifiers(), name,
				tagBits, typeAnnotationArray, type.getRawType().getFieldDescriptor().getChars());
	}

	public static IBinaryAnnotation createBinaryAnnotation(NdAnnotation ndAnnotation) {
		List<NdAnnotationValuePair> elementValuePairs = ndAnnotation.getElementValuePairs();

		final IBinaryElementValuePair[] resultingPair = new IBinaryElementValuePair[elementValuePairs.size()];

		for (int idx = 0; idx < elementValuePairs.size(); idx++) {
			NdAnnotationValuePair next = elementValuePairs.get(idx);

			resultingPair[idx] = new ElementValuePairInfo(next.getName().getChars(), unpackValue(next.getValue()));
		}

		final char[] binaryName = JavaNames.fieldDescriptorToBinaryName(
				ndAnnotation.getType().getRawType().getFieldDescriptor().getChars());

		return new IBinaryAnnotation() {
			@Override
			public char[] getTypeName() {
				return binaryName;
			}

			@Override
			public IBinaryElementValuePair[] getElementValuePairs() {
				return resultingPair;
			}

			@Override
			public String toString() {
				return BinaryTypePrinter.printAnnotation(this);
			}
		};
	}

	private static void buildAnnotations(List<IBinaryTypeAnnotation> result, ITypeAnnotationBuilder builder,
			NdTypeSignature signature) {
		if (signature == null) {
			return;
		}
		ITypeAnnotationBuilder nextAnnotations = builder;
		List<NdTypeSignature> declaringTypes = signature.getDeclaringTypeChain();

		for (NdTypeSignature next : declaringTypes) {
			buildAnnotations(result, nextAnnotations, next.getAnnotations());

			NdTypeSignature arrayArgument = next.getArrayDimensionType();
			if (arrayArgument != null) {
				buildAnnotations(result, nextAnnotations.toNextArrayDimension(), arrayArgument);
			}

			List<NdTypeArgument> typeArguments = next.getTypeArguments();
			for (int rank = 0; rank < typeArguments.size(); rank++) {
				NdTypeArgument argument = typeArguments.get(rank);

				NdTypeSignature argumentType = argument.getType();
				if (argumentType != null) {
					buildAnnotations(result, nextAnnotations.toTypeArgument(rank), argumentType);
				}
			}

			nextAnnotations = nextAnnotations.toNextNestedType();
		}
	}

	private static void buildAnnotations(List<IBinaryTypeAnnotation> result, ITypeAnnotationBuilder builder,
			List<NdAnnotation> annotations) {
		for (NdAnnotation next : annotations) {
			result.add(builder.build(createBinaryAnnotation(next)));
		}
	}

	private void initEnclosing() {
		if (!this.enclosingInitialized) {
			this.enclosingInitialized = true;

			try (IReader rl = this.typeRef.lock()) {
				NdType type = this.typeRef.get();
				if (type != null) {
					NdMethodId methodId = type.getDeclaringMethod();

					if (methodId != null) {
						char[] methodName = methodId.getMethodName().getChars();
						int startIdx = CharArrayUtils.lastIndexOf('#', methodName);
						this.enclosingMethod = CharArrayUtils.substring(methodName, startIdx + 1);
						this.enclosingType = CharArrayUtils.subarray(methodName, 1, startIdx);
					} else {
						NdTypeId typeId = type.getDeclaringType();

						if (typeId != null) {
							this.enclosingType = typeId.getBinaryName();
						}
					}
				}
			}
		}
	}

	private static Object unpackValue(NdConstant value) {
		if (value == null) {
			return null;
		}
		if (value instanceof NdConstantAnnotation) {
			NdConstantAnnotation annotation = (NdConstantAnnotation) value;

			return createBinaryAnnotation(annotation.getValue());
		}
		if (value instanceof NdConstantArray) {
			NdConstantArray array = (NdConstantArray) value;

			List<NdConstant> arrayContents = array.getValue();

			Object[] result = new Object[arrayContents.size()];
			for (int idx = 0; idx < arrayContents.size(); idx++) {
				result[idx] = unpackValue(arrayContents.get(idx));
			}
			return result;
		}
		if (value instanceof NdConstantEnum) {
			NdConstantEnum ndConstantEnum = (NdConstantEnum) value;

			NdTypeSignature signature = ndConstantEnum.getType();

			return new EnumConstantSignature(signature.getRawType().getBinaryName(), ndConstantEnum.getValue());
		}
		if (value instanceof NdConstantClass) {
			NdConstantClass constant = (NdConstantClass) value;

			return new ClassSignature(constant.getValue().getRawType().getBinaryName());
		}

		return value.getConstant();
	}
}
