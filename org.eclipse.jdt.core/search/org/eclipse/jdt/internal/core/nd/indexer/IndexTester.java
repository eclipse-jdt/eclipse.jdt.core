package org.eclipse.jdt.internal.core.nd.indexer;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jdt.internal.compiler.env.ClassSignature;
import org.eclipse.jdt.internal.compiler.env.EnumConstantSignature;
import org.eclipse.jdt.internal.compiler.env.IBinaryAnnotation;
import org.eclipse.jdt.internal.compiler.env.IBinaryElementValuePair;
import org.eclipse.jdt.internal.compiler.env.IBinaryField;
import org.eclipse.jdt.internal.compiler.env.IBinaryMethod;
import org.eclipse.jdt.internal.compiler.env.IBinaryType;
import org.eclipse.jdt.internal.compiler.env.IBinaryTypeAnnotation;
import org.eclipse.jdt.internal.compiler.impl.Constant;
import org.eclipse.jdt.internal.core.nd.util.CharArrayUtils;

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
		public String toString() {
			return this.annotation.toString();
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

			return IndexTester.isEqual(this.annotation.getAnnotation(), otherAnnotation.getAnnotation());
		}
	}

	public static void testType(IBinaryType expected, IBinaryType actual) {

		IBinaryTypeAnnotation[] expectedTypeAnnotations = expected.getTypeAnnotations();
		IBinaryTypeAnnotation[] actualTypeAnnotations = actual.getTypeAnnotations();

		compareTypeAnnotations(expectedTypeAnnotations, actualTypeAnnotations);

		IBinaryAnnotation[] expectedBinaryAnnotations = expected.getAnnotations();
		IBinaryAnnotation[] actualBinaryAnnotations = actual.getAnnotations();

		compareAnnotations(expectedBinaryAnnotations, actualBinaryAnnotations);

		if (expected.getGenericSignature() != null) {
			assertEquals("The generic signature did not match", expected.getGenericSignature(), //$NON-NLS-1$
					actual.getGenericSignature());
		}

		assertEquals("The enclosing method name did not match", expected.getEnclosingMethod(), //$NON-NLS-1$
				actual.getEnclosingMethod());
		assertEquals("The enclosing method name did not match", expected.getEnclosingTypeName(), //$NON-NLS-1$
				actual.getEnclosingTypeName());

		IBinaryField[] expectedFields = expected.getFields();
		IBinaryField[] actualFields = actual.getFields();

		if (expectedFields != actualFields) {
			if (expectedFields == null && actualFields != null) {
				throw new IllegalStateException("Expected fields was null -- actual fields were not"); //$NON-NLS-1$
			}
			if (expectedFields.length != actualFields.length) {
				throw new IllegalStateException("The expected and actual number of fields did not match"); //$NON-NLS-1$
			}

			for (int fieldIdx = 0; fieldIdx < actualFields.length; fieldIdx++) {
				compareFields(expectedFields[fieldIdx], actualFields[fieldIdx]);
			}
		}

		// Commented this out because the "expected" values often appear to be invalid paths when the "actual"
		// ones are correct.
		// assertEquals("The file name did not match", expected.getFileName(), actual.getFileName()); //$NON-NLS-1$
		assertEquals("The interface names did not match", expected.getInterfaceNames(), actual.getInterfaceNames()); //$NON-NLS-1$

		// Member types are not expected to match during indexing since the index uses discovered cross-references,
		// not the member types encoded in the .class file.
		// expected.getMemberTypes() != actual.getMemberTypes()

		IBinaryMethod[] expectedMethods = expected.getMethods();
		IBinaryMethod[] actualMethods = actual.getMethods();

		if (expectedMethods != actualMethods) {
			if (expectedMethods == null || actualMethods == null) {
				throw new IllegalStateException("One of the method arrays was null"); //$NON-NLS-1$
			}

			if (expectedMethods.length != actualMethods.length) {
				throw new IllegalStateException("The number of methods didn't match"); //$NON-NLS-1$
			}

			for (int i = 0; i < actualMethods.length; i++) {
				IBinaryMethod actualMethod = actualMethods[i];
				IBinaryMethod expectedMethod = expectedMethods[i];

				compareMethods(expectedMethod, actualMethod);
			}
		}

		assertEquals("The missing type names did not match", expected.getMissingTypeNames(), //$NON-NLS-1$
				actual.getMissingTypeNames());
		assertEquals("The modifiers don't match", expected.getModifiers(), actual.getModifiers()); //$NON-NLS-1$
		assertEquals("The names don't match.", expected.getName(), actual.getName()); //$NON-NLS-1$
		assertEquals("The source name doesn't match", expected.getSourceName(), actual.getSourceName()); //$NON-NLS-1$
		assertEquals("The superclass name doesn't match", expected.getSuperclassName(), actual.getSuperclassName()); //$NON-NLS-1$
		assertEquals("The tag bits don't match.", expected.getTagBits(), actual.getTagBits()); //$NON-NLS-1$

		compareTypeAnnotations(expected.getTypeAnnotations(), actual.getTypeAnnotations());
	}

	private static <T> void assertEquals(String message, T o1, T o2) {
		if (!isEqual(o1, o2)) {
			throw new IllegalStateException(message);
		}
	}

	static <T> boolean isEqual(T o1, T o2) {
		if (o1 == o2) {
			return true;
		}

		if (o1 == null || o2 == null) {
			return false;
		}

		if (o1 instanceof ClassSignature) {
			if (!(o2 instanceof ClassSignature)) {
				return false;
			}

			ClassSignature sig1 = (ClassSignature) o1;
			ClassSignature sig2 = (ClassSignature) o2;

			return Arrays.equals(sig1.getTypeName(), sig2.getTypeName());
		}

		if (o1 instanceof IBinaryAnnotation) {
			IBinaryAnnotation binaryAnnotation = (IBinaryAnnotation) o1;
			IBinaryAnnotation otherBinaryAnnotation = (IBinaryAnnotation) o2;
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

				if (!isEqual(next.getValue(), otherNext.getValue())) {
					return false;
				}
			}
			return true;
		}

		if (o1 instanceof Constant) {
			if (!(o2 instanceof Constant)) {
				return false;
			}

			Constant const1 = (Constant) o1;
			Constant const2 = (Constant) o2;

			return const1.hasSameValue(const2);
		}

		if (o1 instanceof EnumConstantSignature) {
			if (!(o2 instanceof EnumConstantSignature)) {
				return false;
			}

			EnumConstantSignature enum1 = (EnumConstantSignature) o1;
			EnumConstantSignature enum2 = (EnumConstantSignature) o2;

			return Arrays.equals(enum1.getEnumConstantName(), enum2.getEnumConstantName())
					&& Arrays.equals(enum1.getTypeName(), enum2.getTypeName());
		}

		if (o1 instanceof char[]) {
			char[] c1 = (char[]) o1;
			char[] c2 = (char[]) o2;

			return CharArrayUtils.equals(c1, c2);
		}

		if (o1 instanceof char[][]) {
			char[][] c1 = (char[][]) o1;
			char[][] c2 = (char[][]) o2;

			return CharArrayUtils.equals(c1, c2);
		}

		if (o1 instanceof char[][][]) {
			char[][][] c1 = (char[][][]) o1;
			char[][][] c2 = (char[][][]) o2;

			if (c1.length != c2.length) {
				return false;
			}

			for (int i = 0; i < c1.length; i++) {
				if (!isEqual(c1[i], c2[i])) {
					return false;
				}
			}
			return true;
		}

		if (o1 instanceof IBinaryMethod[]) {
			IBinaryMethod[] a1 = (IBinaryMethod[]) o1;
			IBinaryMethod[] a2 = (IBinaryMethod[]) o2;

			if (a1.length != a2.length) {
				return false;
			}

			for (int i = 0; i < a1.length; i++) {
				IBinaryMethod m1 = a1[i];
				IBinaryMethod m2 = a2[i];

				compareMethods(m1, m2);
			}
		}

		return Objects.equals(o1, o2);
	}

	private static void compareMethods(IBinaryMethod expectedMethod, IBinaryMethod actualMethod) {
		assertEquals("The annotated parameter count didn't match", expectedMethod.getAnnotatedParametersCount(), //$NON-NLS-1$
				actualMethod.getAnnotatedParametersCount());

		compareAnnotations(expectedMethod.getAnnotations(), actualMethod.getAnnotations());

		assertEquals("The argument names didn't match.", expectedMethod.getArgumentNames(), //$NON-NLS-1$
				actualMethod.getArgumentNames());

		assertEquals("The default values didn't match.", expectedMethod.getDefaultValue(), //$NON-NLS-1$
				actualMethod.getDefaultValue());

		assertEquals("The exception type names did not match.", expectedMethod.getExceptionTypeNames(), //$NON-NLS-1$
				actualMethod.getExceptionTypeNames());

		if (expectedMethod.getGenericSignature() != null) {
			assertEquals("The method's generic signature did not match", expectedMethod.getGenericSignature(), //$NON-NLS-1$
					actualMethod.getGenericSignature());
		}

		assertEquals("The method descriptors did not match.", expectedMethod.getMethodDescriptor(), //$NON-NLS-1$
				actualMethod.getMethodDescriptor());
		assertEquals("The modifiers didn't match.", expectedMethod.getModifiers(), actualMethod.getModifiers()); //$NON-NLS-1$

		for (int idx = 0; idx < actualMethod.getAnnotatedParametersCount(); idx++) {
			char[] classFileName = "".toCharArray(); //$NON-NLS-1$
			compareAnnotations(expectedMethod.getParameterAnnotations(idx, classFileName),
					actualMethod.getParameterAnnotations(idx, classFileName));
		}

		assertEquals("The selectors did not match", expectedMethod.getSelector(), actualMethod.getSelector()); //$NON-NLS-1$
		assertEquals("The tag bits did not match", expectedMethod.getTagBits(), actualMethod.getTagBits()); //$NON-NLS-1$

		compareTypeAnnotations(expectedMethod.getTypeAnnotations(), actualMethod.getTypeAnnotations());
	}

	private static void compareTypeAnnotations(IBinaryTypeAnnotation[] expectedTypeAnnotations,
			IBinaryTypeAnnotation[] actualTypeAnnotations) {
		Set<TypeAnnotationWrapper> expectedAnnotations = new HashSet<>();
		if (expectedTypeAnnotations != null) {
			for (IBinaryTypeAnnotation next : expectedTypeAnnotations) {
				expectedAnnotations.add(new TypeAnnotationWrapper(next));
			}
		}

		Set<TypeAnnotationWrapper> actualAnnotations = new HashSet<>();

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

	private static void compareAnnotations(IBinaryAnnotation[] expectedBinaryAnnotations,
			IBinaryAnnotation[] actualBinaryAnnotations) {
		if (expectedBinaryAnnotations == null) {
			if (actualBinaryAnnotations != null) {
				throw new IllegalStateException("Expected null for the binary annotations"); //$NON-NLS-1$
			} else {
				return;
			}
		}
		if (actualBinaryAnnotations == null) {
			throw new IllegalStateException("Actual null for the binary annotations"); //$NON-NLS-1$
		}
		if (expectedBinaryAnnotations.length != actualBinaryAnnotations.length) {
			throw new IllegalStateException("The expected and actual number of annotations differed"); //$NON-NLS-1$
		}

		for (int idx = 0; idx < expectedBinaryAnnotations.length; idx++) {
			if (!isEqual(expectedBinaryAnnotations[idx], actualBinaryAnnotations[idx])) {
				throw new IllegalStateException("An annotation had an unexpected value"); //$NON-NLS-1$
			}
		}
	}

	private static void compareFields(IBinaryField field1, IBinaryField field2) {
		compareAnnotations(field1.getAnnotations(), field2.getAnnotations());
		assertEquals("Constants not equal", field1.getConstant(), field2.getConstant()); //$NON-NLS-1$
		if (field1.getGenericSignature() != null) {
			assertEquals("The generic signature did not match", field1.getGenericSignature(), //$NON-NLS-1$
					field2.getGenericSignature());
		}
		assertEquals("The modifiers did not match", field1.getModifiers(), field2.getModifiers()); //$NON-NLS-1$
		assertEquals("The tag bits did not match", field1.getTagBits(), field2.getTagBits()); //$NON-NLS-1$
		assertEquals("The names did not match", field1.getName(), field2.getName()); //$NON-NLS-1$

		compareTypeAnnotations(field1.getTypeAnnotations(), field2.getTypeAnnotations());
		assertEquals("The type names did not match", field1.getTypeName(), field2.getTypeName()); //$NON-NLS-1$
	}

}
