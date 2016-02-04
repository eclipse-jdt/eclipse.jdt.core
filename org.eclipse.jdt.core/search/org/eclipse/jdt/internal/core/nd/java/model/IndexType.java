package org.eclipse.jdt.internal.core.nd.java.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
import org.eclipse.jdt.internal.compiler.lookup.LookupEnvironment;
import org.eclipse.jdt.internal.core.nd.DatabaseRef;
import org.eclipse.jdt.internal.core.nd.IReader;
import org.eclipse.jdt.internal.core.nd.java.JavaNames;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdAnnotationValuePair;
import org.eclipse.jdt.internal.core.nd.java.NdConstant;
import org.eclipse.jdt.internal.core.nd.java.NdConstantAnnotation;
import org.eclipse.jdt.internal.core.nd.java.NdConstantArray;
import org.eclipse.jdt.internal.core.nd.java.NdConstantClass;
import org.eclipse.jdt.internal.core.nd.java.NdConstantEnum;
import org.eclipse.jdt.internal.core.nd.java.NdMethodId;
import org.eclipse.jdt.internal.core.nd.java.NdType;
import org.eclipse.jdt.internal.core.nd.java.NdTypeSignature;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

/**
 *
 *
 */
public class IndexType implements IBinaryType {

	private final DatabaseRef<NdType> typeRef;

	private boolean enclosingInitialized;
	private char[] enclosingMethod;
	private char[] enclosingType;

	public IndexType(DatabaseRef<NdType> type) {
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
				return type.getFile().getFilename().getChars();
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
				NdAnnotation[] annotations = this.typeRef.get().getAnnotations();

				IBinaryAnnotation[] result = new IBinaryAnnotation[annotations.length];

				for (int idx = 0; idx < annotations.length; idx++) {
					result[idx] = createBinaryAnnotation(annotations[idx]);
				}

				return result;
			} else {
				return null;
			}
		}
	}

	public static IBinaryAnnotation createBinaryAnnotation(NdAnnotation ndAnnotation) {
		List<NdAnnotationValuePair> elementValuePairs = ndAnnotation.getElementValuePairs();

		final IBinaryElementValuePair[] resultingPair = new IBinaryElementValuePair[elementValuePairs.size()];

		for (int idx = 0; idx < elementValuePairs.size(); idx++) {
			NdAnnotationValuePair next = elementValuePairs.get(idx);

			resultingPair[idx] = new ElementValuePairInfo(next.getName().getChars(), unpackValue(next.getValue()));
		}

		final char[] binaryName = JavaNames.fieldDescriptorToBinaryName(ndAnnotation.getType().getRawType().getFieldDescriptor().getChars());

		return new IBinaryAnnotation() {
			@Override
			public char[] getTypeName() {
				return binaryName;
			}

			@Override
			public IBinaryElementValuePair[] getElementValuePairs() {
				return resultingPair;
			}
		};
	}

	private static Object unpackValue(NdConstant value) {
		if (value instanceof NdConstantAnnotation) {
			NdConstantAnnotation annotation = (NdConstantAnnotation)value;

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
			}
		}
		return result.toArray(new IBinaryTypeAnnotation[result.size()]);
	}

	private void buildAnnotations(List<IBinaryTypeAnnotation> result,
			ITypeAnnotationBuilder builder, NdTypeSignature signature) {
		ITypeAnnotationBuilder nextAnnotations = builder;
		List<NdTypeSignature> declaringTypes = signature.getDeclaringTypeChain();

		for (NdTypeSignature next : declaringTypes) {
			List<NdAnnotation> annotations = next.getAnnotations();

			result.addAll(buildAnnotations(nextAnnotations, annotations));

			NdTypeSignature arrayArgument = next.getArrayDimensionType();
			if (arrayArgument != null) {
				buildAnnotations(result, nextAnnotations.toNextArrayDimension(), arrayArgument);
			}

			asantoh
			nextAnnotations = nextAnnotations.toNextNestedType();
		}
	}

	private List<IBinaryTypeAnnotation> buildAnnotations(ITypeAnnotationBuilder builder,
			List<NdAnnotation> annotations) {

		if (annotations.isEmpty()) {
			return Collections.emptyList();
		}

		List<IBinaryTypeAnnotation> result = new ArrayList<>();
		for (NdAnnotation next : annotations) {
			result.add(builder.build(createBinaryAnnotation(next)));
		}

		return result;
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
						this.enclosingType = CharArrayUtils.subarray(methodName, 0, startIdx);
					}
				}
			}
		}
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
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getGenericSignature() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[][] getInterfaceNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBinaryNestedType[] getMemberTypes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public IBinaryMethod[] getMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[][][] getMissingTypeNames() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getSourceName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public char[] getSuperclassName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getTagBits() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isAnonymous() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isLocal() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isMember() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public char[] sourceFileName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ITypeAnnotationWalker enrichWithExternalAnnotationsFor(ITypeAnnotationWalker walker, Object member,
			LookupEnvironment environment) {
		// TODO Auto-generated method stub
		return null;
	}

}
