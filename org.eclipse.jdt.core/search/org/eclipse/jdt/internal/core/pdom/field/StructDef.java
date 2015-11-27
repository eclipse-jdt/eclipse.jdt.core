package org.eclipse.jdt.internal.core.pdom.field;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.internal.core.pdom.IDestructable;
import org.eclipse.jdt.internal.core.pdom.ITypeFactory;
import org.eclipse.jdt.internal.core.pdom.PDOM;

/**
 * @since 3.12
 */
public final class StructDef<T> {
	Class<T> clazz;
	private StructDef<? super T> superClass;
	private List<IField> fields = new ArrayList<>();
	private boolean doneCalled;
	private boolean offsetsComputed;
	private List<StructDef<? extends T>> subClasses = new ArrayList<>();
	private int size;
	List<IDestructableField> destructableFields = new ArrayList<>();
	boolean refCounted;
	private List<IRefCountedField> refCountedFields = new ArrayList<>();
	boolean isAbstract;
	private ITypeFactory<T> factory;
	protected boolean hasUserDestructor;

	private StructDef(Class<T> clazz) {
		this(clazz, null);
	}

	private StructDef(Class<T> clazz, StructDef<? super T> superClass) {
		this(clazz, superClass, Modifier.isAbstract(clazz.getModifiers()));
	}

	private StructDef(Class<T> clazz, StructDef<? super T> superClass, boolean isAbstract) {
		this.clazz = clazz;
		this.superClass = superClass;
		if (this.superClass != null) {
			this.superClass.subClasses.add(this);
		}
		this.isAbstract = isAbstract;
		final String fullyQualifiedClassName = clazz.getName();

		final Constructor<T> constructor;
		if (!this.isAbstract) {
			try {
				constructor = clazz.getConstructor(new Class<?>[] { PDOM.class, long.class });
			} catch (NoSuchMethodException | SecurityException e) {
				throw new IllegalArgumentException("The node class " + fullyQualifiedClassName //$NON-NLS-1$
						+ " does not have an appropriate constructor for it to be used with PDOM"); //$NON-NLS-1$
			}
		} else {
			constructor = null;
		}

		this.hasUserDestructor = IDestructable.class.isAssignableFrom(clazz);

		this.factory = new ITypeFactory<T>() {
			public T create(PDOM dom, long address) {
				if (StructDef.this.isAbstract) {
					throw new UnsupportedOperationException(
							"Attempting to instantiate abstract class" + fullyQualifiedClassName); //$NON-NLS-1$
				}

				try {
					return constructor.newInstance(dom, address);
				} catch (InvocationTargetException e) {
					Throwable target = e.getCause();

					if (target instanceof RuntimeException) {
						throw (RuntimeException) target;
					}

					throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
				} catch (InstantiationException | IllegalAccessException e) {
					throw new RuntimeException("Error in AutoTypeFactory", e); //$NON-NLS-1$
				}
			}

			public int getRecordSize() {
				return StructDef.this.size();
			}

			public boolean hasDestructor() {
				return StructDef.this.hasUserDestructor || hasDestructableFields(); 
			}

			public Class<?> getElementClass() {
				return StructDef.this.clazz;
			}

			public void destruct(PDOM pdom, long record) {
				checkNotMutable();
				if (StructDef.this.hasUserDestructor) {
					IDestructable destructable = (IDestructable)create(pdom, record);
					destructable.destruct();
				}
				destructFields(pdom, record);
			}

			public void destructFields(PDOM dom, long address) {
				StructDef.this.destructFields(dom, address);
			}

			public boolean hasReferences(PDOM dom, long record) {
				return StructDef.this.hasReferences(dom, record);
			}

			public boolean isRefCounted() {
				return StructDef.this.refCounted;
			}
		};
	}

	public Class<T> getStructClass() {
		return this.clazz;
	}

	@Override
	public String toString() {
		return this.clazz.getName();
	}

	public static <T> StructDef<T> createAbstract(Class<T> clazz) {
		return new StructDef<T>(clazz, null, true);
	}

	public static <T> StructDef<T> createAbstract(Class<T> clazz, StructDef<? super T> superClass) {
		return new StructDef<T>(clazz, superClass, true);
	}

	public static <T> StructDef<T> create(Class<T> clazz) {
		return new StructDef<T>(clazz);
	}

	public static <T> StructDef<T> create(Class<T> clazz, StructDef<? super T> superClass) {
		return new StructDef<T>(clazz, superClass);
	}

	protected boolean hasReferences(PDOM dom, long record) {
		for (IRefCountedField next : this.refCountedFields) {
			if (next.hasReferences(dom, record)) {
				return true;
			}
		}

		if (StructDef.this.superClass != null) {
			return StructDef.this.superClass.hasReferences(dom, record);
		}
		return false;
	}

	protected boolean hasDestructableFields() {
		return (!StructDef.this.destructableFields.isEmpty() || 
				(StructDef.this.superClass != null && StructDef.this.superClass.hasDestructableFields()));
	}

	/**
	 * Call this once all the fields have been added to the struct definition and it is
	 * ready to use.
	 */
	public void done() {
		if (this.doneCalled) {
			throw new IllegalStateException("May not call done() more than once"); //$NON-NLS-1$
		}
		this.doneCalled = true;

		if (this.superClass == null || this.superClass.areOffsetsComputed()) {
			computeOffsets();
		}
	}

	public void add(IField toAdd) {
		checkMutable();

		this.fields.add(toAdd);
	}

	public void addDestructableField(IDestructableField field) {
		checkMutable();

		this.destructableFields.add(field);
	}

	public StructDef<T> useStandardRefCounting() {
		checkMutable();

		this.refCounted = true;
		return this;
	}

	public void addRefCountedField(IRefCountedField result) {
		checkMutable();

		this.refCountedFields.add(result);
	}

	public boolean areOffsetsComputed() {
		return this.offsetsComputed;
	}

	public int size() {
		checkNotMutable();
		return this.size;
	}

	void checkNotMutable() {
		if (!this.offsetsComputed) {
			throw new IllegalStateException("Must call done() before using the struct"); //$NON-NLS-1$
		}
	}

	private void checkMutable() {
		if (this.doneCalled) {
			throw new IllegalStateException("May not modify a StructDef after done() has been called"); //$NON-NLS-1$
		}
	}

	private void computeOffsets() {
		int offset = this.superClass == null ? 0 : this.superClass.size();

		for (IField next : this.fields) {
			next.setOffset(offset);
			offset += next.getRecordSize();
		}

		this.size = offset;
		this.offsetsComputed = true;

		for (StructDef<? extends T> next : this.subClasses) {
			if (next.doneCalled) {
				next.computeOffsets();
			}
		}
	}

	public FieldPointer addPointer() {
		FieldPointer result = new FieldPointer();
		add(result);
		return result;
	}

	public FieldShort addShort() {
		FieldShort result = new FieldShort();
		add(result);
		return result;
	}

	public FieldInt addInt() {
		FieldInt result = new FieldInt();
		add(result);
		return result;
	}

	public FieldLong addLong() {
		FieldLong result = new FieldLong();
		add(result);
		return result;
	}

	public FieldString addString() {
		FieldString result = new FieldString();
		add(result);
		addDestructableField(result);
		return result;
	}

	public FieldDouble addDouble() {
		FieldDouble result = new FieldDouble();
		add(result);
		return result;
	}

	public FieldFloat addFloat() {
		FieldFloat result = new FieldFloat();
		add(result);
		return result;
	}

	public FieldByte addByte() {
		FieldByte result = new FieldByte();
		add(result);
		return result;
	}

	public FieldChar addChar() {
		FieldChar result = new FieldChar();
		add(result);
		return result;
	}

	public <F> Field<F> add(ITypeFactory<F> factory1) {
		Field<F> result = new Field<>(factory1);
		add(result);
		if (result.factory.hasDestructor()) {
			this.destructableFields.add(result);
		}
		return result;
	}

	public ITypeFactory<T> getFactory() {
		return this.factory;
	}

	void destructFields(PDOM dom, long address) {
		for (IDestructableField next : StructDef.this.destructableFields) {
			next.destruct(dom, address);
		}

		if (this.superClass != null) {
			this.superClass.destructFields(dom, address);
		}
	}
}
