package org.eclipse.jdt.internal.core.nd.indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;

public class IndexTester {

	private static final class TypeAnnotationWrapper {
		private IBinaryTypeAnnotation annotation;

		public TypeAnnotationWrapper(IBinaryTypeAnnotation next) {
			this.annotation = next;
		}

		@Override
		public int hashCode() {
			int hashCode;
			int[] typePath = this.annotation.getTypePath();

			hashCode = Arrays.hashCode(typePath);
			hashCode = hashCode * 31 + this.annotation.getTargetType();
			hashCode = hashCode * 31 + this.annotation.getTypeParameterIndex();
			return hashCode;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj.getClass() != TypeAnnotationWrapper.class) {
				return false;
			}

			TypeAnnotationWrapper wrapper = (TypeAnnotationWrapper) obj;
			IBinaryTypeAnnotation otherAnnotation = wrapper.annotation;

			int[] typePath = this.annotation.getTypePath();
			int[] otherTypePath = otherAnnotation.getTypePath();

			if (!Arrays.equals(typePath, otherTypePath)) {
				return false;
			}

			if (this.annotation.getTargetType() != otherAnnotation.getTargetType()) {
				return false;
			}

			if (this.annotation.getBoundIndex() != otherAnnotation.getBoundIndex()) {
				return false;
			}

			if (this.annotation.getMethodFormalParameterIndex() != otherAnnotation.getMethodFormalParameterIndex()) {
				return false;
			}

			if (this.annotation.getSupertypeIndex() != otherAnnotation.getSupertypeIndex()) {
				return false;
			}

			if (this.annotation.getThrowsTypeIndex() != otherAnnotation.getThrowsTypeIndex()) {
				return false;
			}

			if (this.annotation.getTypeParameterIndex() != otherAnnotation.getTypeParameterIndex()) {
				return false;
			}

			IBinaryAnnotation binaryAnnotation = this.annotation.getAnnotation();
			IBinaryAnnotation otherBinaryAnnotation = otherAnnotation.getAnnotation();

			IBinaryElementValuePair[] elementValuePairs = binaryAnnotation.getElementValuePairs();
			IBinaryElementValuePair[] otherElementValuePairs = otherBinaryAnnotation.getElementValuePairs();

			if (elementValuePairs.length != otherElementValuePairs.length) {
				return false;
			}

			for (int idx = 0; idx < elementValuePairs.length; idx++) {
				IBinaryElementValuePair next = elementValuePairs[idx];
				IBinaryElementValuePair otherNext = otherElementValuePairs[idx];

				char[] nextName = next.getName();
				char[] otherNextName = otherNext.getName();

				if (!Arrays.equals(nextName, otherNextName)) {
					return false;
				}

				if (!constantsEqual(next.getValue(), otherNext.getValue())) {
					return false;
				}
			}
			return true;
		}
	}

	public static void testType(IBinaryType expected, IBinaryType actual) {
		Set<TypeAnnotationWrapper> expectedAnnotations = new HashSet<>();

		IBinaryTypeAnnotation[] expectedTypeAnnotations = expected.getTypeAnnotations();

		if (expectedTypeAnnotations != null) {
			for (IBinaryTypeAnnotation next : expectedTypeAnnotations) {
				expectedAnnotations.add(new TypeAnnotationWrapper(next));
			}
		}

		Set<TypeAnnotationWrapper> actualAnnotations = new HashSet<>();

		IBinaryTypeAnnotation[] actualTypeAnnotations = actual.getTypeAnnotations();
		if (actualTypeAnnotations != null) {
			for (IBinaryTypeAnnotation next : actualTypeAnnotations) {
				actualAnnotations.add(new TypeAnnotationWrapper(next));
			}
		}

		for (TypeAnnotationWrapper nextExpected : expectedAnnotations) {
			if (!actualAnnotations.contains(nextExpected)) {
				throw new IllegalStateException(
						"The index was missing an expected type annotation: " + nextExpected.toString()); //$NON-NLS-1$
			}
		}

		for (TypeAnnotationWrapper nextActual : actualAnnotations) {
			if (!expectedAnnotations.contains(nextActual)) {
				throw new IllegalStateException(
						"The index contained an unexpected type annotation: " + nextActual.toString()); //$NON-NLS-1$
			}
		}
	}

	public static boolean constantsEqual(Object value, Object value2) {
		if (value instanceof ClassSignature) {
			if (!(value2 instanceof ClassSignature)) {
				return false;
			}

			ClassSignature sig1 = (ClassSignature) value;
			ClassSignature sig2 = (ClassSignature) value2;

			return Arrays.equals(sig1.getTypeName(), sig2.getTypeName());
		}
		if (value instanceof Constant) {
			if (!(value2 instanceof Constant)) {
				return false;
			}

			Constant const1 = (Constant) value;
			Constant const2 = (Constant) value2;

			return const1.hasSameValue(const2);
		}
		if (value instanceof EnumConstantSignature) {
			if (!(value2 instanceof EnumConstantSignature)) {
				return false;
			}

			EnumConstantSignature enum1 = (EnumConstantSignature) value;
			EnumConstantSignature enum2 = (EnumConstantSignature) value2;

			return Arrays.equals(enum1.getEnumConstantName(), enum2.getEnumConstantName())
					&& Arrays.equals(enum1.getTypeName(), enum2.getTypeName());
		}

		return false;
	}

}
