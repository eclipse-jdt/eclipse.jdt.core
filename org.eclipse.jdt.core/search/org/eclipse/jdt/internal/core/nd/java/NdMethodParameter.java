package org.eclipse.jdt.internal.core.nd.java;

import java.util.List;

import org.eclipse.jdt.internal.core.nd.Nd;
import org.eclipse.jdt.internal.core.nd.NdNode;
import org.eclipse.jdt.internal.core.nd.db.IString;
import org.eclipse.jdt.internal.core.nd.field.FieldByte;
import org.eclipse.jdt.internal.core.nd.field.FieldManyToOne;
import org.eclipse.jdt.internal.core.nd.field.FieldOneToMany;
import org.eclipse.jdt.internal.core.nd.field.FieldString;
import org.eclipse.jdt.internal.core.nd.field.StructDef;

public class NdMethodParameter extends NdNode {
	public static final FieldManyToOne<NdMethod> PARENT;
	public static final FieldManyToOne<NdTypeSignature> ARGUMENT_TYPE;
	public static final FieldString NAME;
	public static final FieldOneToMany<NdAnnotation> ANNOTATIONS;
	public static final FieldByte FLAGS;

	private static final byte FLG_COMPILER_DEFINED = 0x01;

	@SuppressWarnings("hiding")
	public static StructDef<NdMethodParameter> type;

	static {
		type = StructDef.create(NdMethodParameter.class, NdNode.type);
		PARENT = FieldManyToOne.create(type, NdMethod.PARAMETERS);
		ARGUMENT_TYPE = FieldManyToOne.create(type, NdTypeSignature.USED_AS_METHOD_ARGUMENT);
		NAME = type.addString();
		ANNOTATIONS = FieldOneToMany.create(type, NdAnnotation.PARENT_METHOD_PARAMETER);
		FLAGS = type.addByte();
		type.done();
	}

	public NdMethodParameter(Nd pdom, long address) {
		super(pdom, address);
	}

	public NdMethodParameter(NdMethod parent, NdTypeSignature argumentType) {
		super(parent.getNd());

		PARENT.put(getNd(), this.address, parent);
		ARGUMENT_TYPE.put(getNd(), this.address, argumentType);
	}

	public NdTypeSignature getType() {
		return ARGUMENT_TYPE.get(getNd(), this.address);
	}

	public void setName(char[] name) {
		NAME.put(getNd(), this.address, name);
	}

	public IString getName() {
		return NAME.get(getNd(), this.address);
	}

	public List<NdAnnotation> getAnnotations() {
		return ANNOTATIONS.asList(getNd(), this.address);
	}

	private void setFlag(byte flagConstant, boolean value) {
		int oldFlags = FLAGS.get(getNd(), this.address);
		int newFlags = ((oldFlags & ~flagConstant) | (value ? flagConstant : 0));
		FLAGS.put(getNd(), this.address, (byte) newFlags);
	}

	private boolean getFlag(byte flagConstant) {
		return (FLAGS.get(getNd(), this.address) & flagConstant) != 0;
	}

	public void setCompilerDefined(boolean isCompilerDefined) {
		setFlag(FLG_COMPILER_DEFINED, isCompilerDefined);
	}

	public boolean isCompilerDefined() {
		return getFlag(FLG_COMPILER_DEFINED);
	}
}
