package org.eclipse.jdt.internal.core.builder.impl;

/*
 * (c) Copyright IBM Corp. 2000, 2001.
 * All Rights Reserved.
 */
import org.eclipse.core.runtime.*;

import org.eclipse.jdt.internal.core.Assert;
import org.eclipse.jdt.internal.core.builder.*;

import java.io.*;

class StateSnapConstantPool {
	OrderedSet fSet;

	/**
	 * The number of special pool entries at the beginning.
	 */
	static final int NUM_SPECIAL = 11;
/**
 * StateSnapConstantPool constructor comment.
 */
StateSnapConstantPool(IDevelopmentContext dc) {
	init(dc, 500);
}
/**
 * Read a StateSnapConstantPool from an input stream.
 */
StateSnapConstantPool(IDevelopmentContext dc, DataInputStream in, StateSnapV5 snap) throws IOException {
	read(dc, in, snap);
}
/** 
 * Add a Number to the pool.
 */
public void add(Number num) {
	fSet.put(num);
}
/** 
 * Add a String to the pool.
 */
public void add(String str) {
	fSet.put(str);
}
/** 
 * Add an IPath to the pool.
 */
public void add(IPath path) {
	if (!fSet.includes(path)) {
		// Special handling needed to preserve device
		if (path.isRoot()) {
			fSet.put(path.toString());
		} else {
			IPath parent = path.removeLastSegments(1);
			add(parent);
			fSet.put(path.lastSegment());
		}
		fSet.put(path);
	}
}
/** 
 * Add an IPackage to the pool.
 */
public void add(IHandle handle) {
	switch (handle.kind()) {
		case IHandle.K_JAVA_PACKAGE:
			add((IPackage)handle);
			break;
		case IHandle.K_JAVA_TYPE:
			add((IType)handle);
			break;
		case IHandle.K_JAVA_FIELD:
			if (!fSet.includes(handle)) {
				IField f = (IField)handle;
				add(f.getDeclaringClass());
				add(f.getName());
				fSet.put(handle);
			}
			break;
		case IHandle.K_JAVA_CONSTRUCTOR:
			if (!fSet.includes(handle)) {
				IConstructor c = (IConstructor)handle;
				add(c.getDeclaringClass());
				IType[] parms = c.getParameterTypes();
				for (int i = 0; i < parms.length; ++i) {
					add(parms[i]);
				}
				fSet.put(handle);
			}
			break;
		case IHandle.K_JAVA_METHOD:
			if (!fSet.includes(handle)) {
				IMethod m = (IMethod)handle;
				add(m.getDeclaringClass());
				add(m.getName());
				IType[] parms = m.getParameterTypes();
				for (int i = 0; i < parms.length; ++i) {
					add(parms[i]);
				}
				fSet.put(handle);
			}
			break;
		case IHandle.K_JAVA_IMAGE:
			// NOP
			break;
		default:
			Assert.isTrue(false, "Unknown kind of handle");
	}
}
/** 
 * Add a SourceEntry to the pool.
 */
public void add(SourceEntry entry) {
	Assert.isNotNull(entry, "Null SourceEntry being added to StateSnapConstantPool");
	if (!fSet.includes(entry)) {
		add(entry.getPath());
		String zipEntryPath = entry.fZipEntryPath;
		if (zipEntryPath != null) {
			fSet.put(zipEntryPath);
		}
		String zipEntryFileName = entry.fZipEntryFileName;
		if (zipEntryFileName != null) {
			fSet.put(zipEntryFileName);
		}
		fSet.put(entry);
	}
}
/** 
 * Add an IPackage to the pool.
 */
public void add(IPackage pkg) {
	if (!fSet.includes(pkg)) {
		fSet.put(pkg.getName());
		fSet.put(pkg);
	}
}
/** 
 * Add an IType to the pool.
 */
public void add(IType type) {
	Assert.isTrue(!type.isStateSpecific());
	if (type.isPrimitive()) {
		return;  // Already added
	}
	if (!fSet.includes(type)) {
		if (type.isArray()) {
			add(((ArrayTypeHandleImpl)type).getElementType());
		}
		else {
			ClassOrInterfaceHandleImpl cls = (ClassOrInterfaceHandleImpl)type;
			add(cls.getPackage());
			fSet.put(cls.getSimpleName());
		}
		fSet.put(type);
	}
}
/**
 * Returns the IHandle at the given index.  It must not be null.
 */
public IHandle getHandle(int index) throws IOException {
	try {
		IHandle result = (IHandle)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the Object at the given index.  It may be null.
 */
public Object getObject(int index) throws IOException {
	return fSet.get(index);
}
/**
 * Returns the IPackage at the given index.  It must not be null.
 */
public IPackage getPackage(int index) throws IOException {
	try {
		IPackage result = (IPackage)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the IPath at the given index.  It must not be null.
 */
public IPath getPath(int index) throws IOException {
	try {
		IPath result = (IPath)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the SourceEntry at the given index.  It must not be null.
 */
public SourceEntry getSourceEntry(int index) throws IOException {
	try {
		SourceEntry result = (SourceEntry)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the String at the given index.  It must not be null.
 */
public String getString(int index) throws IOException {
	try {
		String result = (String)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the String at the given index.  It may be null.
 */
public String getStringOrNull(int index) throws IOException {
	return index == 0 ? null : getString(index);
}
/**
 * Returns the String or Number at the given index.  It may be null.
 */
public Object getStringOrNumber(int index) throws IOException {
	if (index == 0) {
		return null;
	}
	Object result = (Object)fSet.get(index);
	if (result != null && ((result instanceof Number) || (result instanceof String))) {
		return result;
	}
	else {
		throw new IOException("Error in format");
	}
}
/**
 * Returns the IType at the given index.  It must not be null.
 */
public IType getType(int index) throws IOException {
	try {
		IType result = (IType)fSet.get(index);
		if (result == null) {
			throw new IOException("Error in format");
		}
		return result;
	}
	catch (ClassCastException e) {
		throw new IOException("Error in format");
	}
}
/** 
 * For debugging.
 */
public String histogram() {
	int nStr = 0, nID = 0, nSE = 0, nH = 0, nN = 0;

	int n = fSet.size();
	for (int i = 10; i < n; ++i) {
		Object obj = fSet.get(i);
		if (obj instanceof String) {
			++nStr;
		}
		else if (obj instanceof IPath) {
			++nID;
		}
		else if (obj instanceof SourceEntry) {
			++nSE;
		}
		else if (obj instanceof IHandle) {
			++nH;
		}
		else if (obj instanceof Number) {
			++nN;
		}
		else {
			Assert.isTrue(false, "Unexpected pool item");
		}
	}
	return "nStr=" + nStr + ", nID=" + nID + ", nSE=" + nSE + ", nH=" + nH + ",nN=" + nN;
	
}
/** 
 * Returns the index of the given object.
 */
public int index(Object obj) {
	if (obj == null) {
		return 0;
	}
	try {
		return fSet.index(obj);
	}
	catch (IllegalArgumentException e) {
		throw new IllegalArgumentException("Internal error in state serialization. Expected object missing from constant pool: " + obj);
	}
}
/**
 * Initialize the constant pool for the given DC and initial size estimate.
 */
private void init (IDevelopmentContext dc, int initSize) {
	// Set up constant pool with the special entries.
	// Number of entries must correspond with NUM_SPECIAL
	IImage image = dc.getImage();
	fSet = new OrderedSet(initSize*2+1, 0.5f);
	fSet.put(new Object());  		// 0: placeholder for null
	fSet.put(image.booleanType());	// 1: type boolean
	fSet.put(image.byteType());		// 2: type byte
	fSet.put(image.charType());		// 3: type char
	fSet.put(image.doubleType());	// 4: type double
	fSet.put(image.floatType());	// 5: type float
	fSet.put(image.intType());		// 6: type int
	fSet.put(image.longType());		// 7: type long
	fSet.put(image.shortType());	// 8: type short
	fSet.put(image.voidType());		// 9: type void
	fSet.put(image);				// 10: image handle

}
/**
 * Read a StateSnapConstantPool from an input stream.
 */
private void read(IDevelopmentContext dc, DataInputStream in, StateSnapV5 snap) throws IOException {
	int n = in.readInt();  // Actual size of pool is (NUM_SPECIAL + n)
	init(dc, n);

	int i = NUM_SPECIAL;
	for (int j = 0; j < n; ++j, ++i) {
		int tag = in.readByte();
		switch (tag) {
			case 1:
				// String
				String str = in.readUTF();
				fSet.put(i, str);
				break;
			case 2:
				// IPath
				int temp = in.readInt();
				IPath parent = temp == 0 ? null : getPath(temp);
				String elementName = getString(in.readInt());
				if (parent == null) {
					fSet.put(i, new Path(elementName));
				}
				else {
					fSet.put(i, parent.append(elementName));
				}
				break;
			case 3:
				// SourceEntry
				SourceEntry entry = snap.readSourceEntry(this, in);
				fSet.put(i, entry);
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
			case 9:
			case 10:
			case 11:
				fSet.put(i, readHandle(dc, in, tag));
				break;
			case 12:
			case 13:
			case 14:
			case 15:
				fSet.put(i, readNumber(in, tag));
				break;
			default:
				throw new IOException("Unexpected kind of pool item");
		}
	}
}
/**
 * Internal -- Read an IHandle from an input stream into the pool.
 */
private IHandle readHandle(IDevelopmentContext dc, DataInputStream in, int tag) throws IOException {
	switch (tag) {
		case 4: {
			// package
			String name = getString(in.readInt());
			boolean isUnnamed = in.readBoolean();
			return dc.getImage().getPackageHandle(name, isUnnamed);
		}
		case 5: {
			// primitive type
			// Should not occur since primitive types are well known and not written
			throw new IOException("Internal error");
		}
		case 6: {
			// array type
			TypeImpl elementType = (TypeImpl) getType(in.readInt());
			int nesting = in.readByte() & 0xFF;
			return new ArrayTypeHandleImpl(elementType, nesting);
		}
		case 7: {
			// class or interface type
			IPackage pkg = getPackage(in.readInt());
			String simpleName = getString(in.readInt());
			return pkg.getClassHandle(simpleName);
		}
		case 8: {
			// method
			IType declaringClass = getType(in.readInt());
			if (declaringClass.isPrimitive() || declaringClass.isArray()) {
				throw new IOException("Bad format");
			}
			String name = getString(in.readInt());
			int numParams = in.readByte() & 0xFF;
			IType[] params = new IType[numParams];
			for (int i = 0; i < numParams; ++i) {
				params[i] = getType(in.readInt());
			}
			return declaringClass.getMethodHandle(name, params);
		}
		case 9: {
			// constructor
			IType declaringClass = getType(in.readInt());
			if (declaringClass.isPrimitive() || declaringClass.isArray()) {
				throw new IOException("Bad format");
			}
			int numParams = in.readByte() & 0xFF;
			IType[] params = new IType[numParams];
			for (int i = 0; i < numParams; ++i) {
				params[i] = getType(in.readInt());
			}
			return declaringClass.getConstructorHandle(params);
		}
		case 10: {
			// field 
			IType declaringClass = getType(in.readInt());
			if (declaringClass.isPrimitive() || declaringClass.isArray()) {
				throw new IOException("Bad format");
			}
			String name = getString(in.readInt());
			return declaringClass.getFieldHandle(name);
		}
		case 11:
			// image
			return dc.getImage();
		default:
			throw new IOException("Unexpected kind of pool item");
	}
}
/**
 * Internal -- Read a Number from an input stream into the pool.
 */
private Number readNumber(DataInputStream in, int tag) throws IOException {
	switch (tag) {
		case 12:
			// Integer
			return new Integer(in.readInt());
		case 13:
			// Long
			return new Long(in.readLong());
		case 14:
			// Float
			return new Float(Float.intBitsToFloat(in.readInt()));
		case 15:
			// Double
			return new Double(Double.longBitsToDouble(in.readLong()));
		default:
			throw new IOException("Unexpeced kind of Number");
	}
}
/** 
 * Returns the number of entries in the pool.
 */
public int size() {
	return fSet.size();
}
/** 
 * Write the constant pool to the given stream.
 */
public void write(DataOutputStream out) throws IOException {
	int n = fSet.size();
	out.writeInt(n - NUM_SPECIAL);
	for (int i = NUM_SPECIAL; i < n; ++i) {
		Object obj = fSet.get(i);
		if (obj instanceof String) {
			out.writeByte(1);
			out.writeUTF((String)obj);
		}
		else if (obj instanceof IHandle) {
			writeHandle((IHandle)obj, out);  // tags 4 through 11
		}
		else if (obj instanceof IPath) {
			IPath path = (IPath)obj;
			out.writeByte(2);
			// Special handling needed to preserve device
			if (path.isRoot()) {
				out.writeInt(0);
				out.writeInt(index(path.toString()));
			}
			else {
				IPath parent = path.removeLastSegments(1);
				out.writeInt(index(parent));
				out.writeInt(index(path.lastSegment()));
			}
		}
		else if (obj instanceof SourceEntry) {
			SourceEntry e = (SourceEntry)obj;
			out.writeByte(3);
			out.writeInt(index(e.getPath()));
			out.writeInt(index(e.fZipEntryPath));
			out.writeInt(index(e.fZipEntryFileName));
		}
		else if (obj instanceof Number) {
			writeNumber((Number)obj, out);  // tags 12 through 15
		}
		else {
			Assert.isTrue(false, "Unexpected pool item");
		}
	}
}
/** 
 * Write a handle to the given stream.
 */
private void writeHandle(IHandle h, DataOutputStream out) throws IOException {
	switch (h.kind()) {
		case IHandle.K_JAVA_PACKAGE:
			IPackage pkg = (IPackage)h;
			out.writeByte(4);
			out.writeInt(index(pkg.getName()));
			out.writeBoolean(pkg.isUnnamed());
			break;
		case IHandle.K_JAVA_TYPE:
			IType t = (IType)h;
			if (t.isPrimitive()) {
				// tag=5
				// Primitive types should not show up since they are well known and are not written.
				throw new IOException("Internal error");
			}
			else if (t.isArray()) {
				ArrayTypeHandleImpl at = (ArrayTypeHandleImpl)t;
				out.writeByte(6);
				out.writeInt(index(at.getElementType()));
				int nesting = at.getNestingDepth();
				Assert.isTrue(nesting < 256);
				out.writeByte(nesting);
			}
			else {
				Assert.isTrue(t instanceof ClassOrInterfaceHandleImpl);
				out.writeByte(7);
				out.writeInt(index(t.getPackage()));
				out.writeInt(index(t.getSimpleName()));
			}
			break;
		case IHandle.K_JAVA_METHOD: {
			IMethod m = (IMethod)h;
			out.writeByte(8);
			out.writeInt(index(m.getDeclaringClass()));
			out.writeInt(index(m.getName()));
			IType[] params = m.getParameterTypes();
			Assert.isTrue(params.length < 256);
			out.writeByte(params.length);
			for (int j = 0; j < params.length; ++j) {
				out.writeInt(index(params[j]));
			}
			break;
		}
		case IHandle.K_JAVA_CONSTRUCTOR: {
			IConstructor c = (IConstructor)h;
			out.writeByte(9);
			out.writeInt(index(c.getDeclaringClass()));
			IType[] params = c.getParameterTypes();
			Assert.isTrue(params.length < 256);
			out.writeByte(params.length);
			for (int j = 0; j < params.length; ++j) {
				out.writeInt(index(params[j]));
			}
			break;
		}
		case IHandle.K_JAVA_FIELD:
			IField f = (IField)h;
			out.writeByte(10);
			out.writeInt(index(f.getDeclaringClass()));
			out.writeInt(index(f.getName()));
			break;
		case IHandle.K_JAVA_IMAGE:
			out.writeByte(11);
			break;
		default:
			Assert.isTrue(false, "Unknown handle type");
	}
}
/** 
 * Write a Number to the given stream.
 */
private void writeNumber(Number num, DataOutputStream out) throws IOException {
	if (num instanceof Integer) {
		out.writeByte(12);
		out.writeInt(num.intValue());
	} 
	else if (num instanceof Long) {
		out.writeByte(13);
		out.writeLong(num.longValue());
	} 
	else if (num instanceof Float) {
		out.writeByte(14);
		out.writeInt(Float.floatToIntBits(num.floatValue()));
	}
	else if (num instanceof Double) {
		out.writeByte(15);
		out.writeLong(Double.doubleToLongBits(num.doubleValue()));
	}
	else {
		Assert.isTrue(false, "Unexpeced kind of Number");
	}
}
}
