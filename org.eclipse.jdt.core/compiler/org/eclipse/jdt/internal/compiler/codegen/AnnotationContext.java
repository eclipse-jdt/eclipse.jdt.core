package org.eclipse.jdt.internal.compiler.codegen;

public class AnnotationContext {
	public static final int METHOD_RECEIVER = 0x06;
	public static final int METHOD_RECEIVER_GENERIC_OR_ARRAY = 0x07;
	public static final int METHOD_RETURN_TYPE = 0x0A;
	public static final int METHOD_RETURN_TYPE_GENERIC_OR_ARRAY = 0x0B;
	public static final int METHOD_PARAMETER = 0x0C;
	public static final int METHOD_PARAMETER_GENERIC_OR_ARRAY = 0x0D;
	public static final int FIELD = 0x0E;
	public static final int FIELD_GENERIC_OR_ARRAY = 0x0F;
	public static final int CLASS_TYPE_PARAMETER_BOUND = 0x10;
	public static final int CLASS_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = 0x11;
	public static final int METHOD_TYPE_PARAMETER_BOUND = 0x12;
	public static final int METHOD_TYPE_PARAMETER_BOUND_GENERIC_OR_ARRAY = 0x13;
	public static final int CLASS_EXTENDS_IMPLEMENTS = 0x14;
	public static final int CLASS_EXTENDS_IMPLEMENTS_GENERIC_OR_ARRAY = 0x15;
	public static final int THROWS = 0x16;
	public static final int THROWS_GENERIC_OR_ARRAY = 0x17;
	public static final int WILDCARD_BOUND = 0x1C;
	public static final int WILDCARD_BOUND_GENERIC_OR_ARRAY = 0x1D;
	public static final int METHOD_TYPE_PARAMETER = 0x20;
	public static final int METHOD_TYPE_PARAMETER_GENERIC_OR_ARRAY = 0x21;
	public static final int CLASS_TYPE_PARAMETER = 0x22;
	public static final int CLASS_TYPE_PARAMETER_GENERIC_OR_ARRAY = 0x23;
	public static final int TYPE_CAST = 0x00;
	public static final int TYPE_CAST_GENERIC_OR_ARRAY = 0x01;
	public static final int TYPE_INSTANCEOF = 0x02;
	public static final int TYPE_INSTANCEOF_GENERIC_OR_ARRAY = 0x03;
	public static final int OBJECT_CREATION = 0x04;
	public static final int OBJECT_CREATION_GENERIC_OR_ARRAY = 0x05;
	public static final int LOCAL_VARIABLE = 0x08;
	public static final int LOCAL_VARIABLE_GENERIC_OR_ARRAY = 0x09;
	public static final int TYPE_ARGUMENT_CONSTRUCTOR_CALL = 0x18;
	public static final int TYPE_ARGUMENT_CONSTRUCTOR_CALL_GENERIC_OR_ARRAY = 0x19;
	public static final int TYPE_ARGUMENT_METHOD_CALL = 0x1A;
	public static final int TYPE_ARGUMENT_METHOD_CALL_GENERIC_OR_ARRAY = 0x1B;
	public static final int CLASS_LITERAL = 0x1E;
	public static final int CLASS_LITERAL_GENERIC_OR_ARRAY = 0x1F;

	public int targetType;
	public int typeIndex;
	
	public AnnotationContext(int targetType) {
		this.targetType = targetType;
	}
	
	public void setTypeIndex(int typeIndex) {
		this.typeIndex = typeIndex;
	}
}
