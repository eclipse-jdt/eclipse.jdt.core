package org.eclipse.jdt.internal.core.lookup;

public class ReferenceInfo {

	/**
	 * The reference names
	 */
	protected char[][] fNames;

	/**
	 * The reference kinds associated with each name
	 */
	protected byte[] fKinds;

	/**
	 * The kinds of references -- maskable
	 */
	public static final byte REFTYPE_unknown = 0x01;
	public static final byte REFTYPE_call = 0x02;
	public static final byte REFTYPE_var = 0x04;
	public static final byte REFTYPE_import = 0x08;
	public static final byte REFTYPE_derive = 0x10;
	public static final byte REFTYPE_type = 0x20;
	public static final byte REFTYPE_class = 0x40;
	public static final byte REFTYPE_constant = (byte) 0x80;
	//	public static final byte REFTYPE_label = 256;

	/**
	 * Creates a new ReferenceInfo object.  There should be one per compilation unit.
	 */
	public ReferenceInfo(char[][] names, byte[] kinds) {
		fNames = names;
		fKinds = kinds;
	}

	/**
	 * Returns the reference kinds array.
	 */
	public byte[] getKinds() {
		return fKinds;
	}

	/**
	 * Returns the reference names array.
	 */
	public char[][] getNames() {
		return fNames;
	}

	/**
	 * For debugging only
	 */
	public String toString() {
		StringBuffer buf = new StringBuffer("ReferenceInfo(");
		for (int i = 0; i < fNames.length; i++) {
			buf.append(fNames[i]);
			buf.append(" ");
		}
		buf.append(")");
		return buf.toString();
	}

}
