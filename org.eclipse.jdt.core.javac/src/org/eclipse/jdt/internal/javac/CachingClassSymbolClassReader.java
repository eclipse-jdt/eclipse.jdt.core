/*******************************************************************************
* Copyright (c) 2024 Red Hat, Inc. and others.
* All rights reserved. This program and the accompanying materials
* are made available under the terms of the Eclipse Public License 2.0
* which accompanies this distribution, and is available at
* https://www.eclipse.org/legal/epl-2.0/
*
* SPDX-License-Identifier: EPL-2.0
*******************************************************************************/
package org.eclipse.jdt.internal.javac;

import static com.sun.tools.javac.code.Flags.ABSTRACT;
import static com.sun.tools.javac.code.Flags.BRIDGE;
import static com.sun.tools.javac.code.Flags.PUBLIC;
import static com.sun.tools.javac.code.Flags.STATIC;
import static com.sun.tools.javac.code.Flags.SYNTHETIC;
import static com.sun.tools.javac.code.Kinds.Kind.MTH;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.stream.StreamSupport;

import javax.lang.model.element.ElementKind;
import javax.tools.JavaFileObject;

import org.eclipse.core.runtime.ILog;

import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.SymbolMetadata;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTag;
import com.sun.tools.javac.code.Types;
import com.sun.tools.javac.code.Attribute.Array;
import com.sun.tools.javac.code.Attribute.Compound;
import com.sun.tools.javac.code.Attribute.Constant;
import com.sun.tools.javac.code.Scope.WriteableScope;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.CompletionFailure;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.ModuleSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.TypeVariableSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.code.Type.TypeVar;
import com.sun.tools.javac.comp.Annotate;
import com.sun.tools.javac.comp.Annotate.AnnotationTypeCompleter;
import com.sun.tools.javac.comp.Annotate.AnnotationTypeMetadata;
import com.sun.tools.javac.file.PathFileObject;
import com.sun.tools.javac.jvm.ClassReader;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Convert;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javac.util.Pair;

/// This particular ClassReader keeps a "template" copy of the created symbols. So when asked again for reading a class
/// it will read in its cache and populate the symbols from the data is has in store (preventing from re-reading jars).
/// {@link Type} is stored as a signature.
/// For signatures and annotations, this requires access to various internal content.
public class CachingClassSymbolClassReader extends ClassReader {

	private static Map<JavaFileObject, ClassSymbolTemplate> CACHE = Collections.synchronizedMap(new HashMap<>());

	/// Allows to replace default strategy of ClassReader by one based on local templates
	private static class StoringQueriesAnnotate extends Annotate {

		private final CachingClassSymbolClassReader reader;
		private final List<AnnotationCompleterWrapper> requested = new ArrayList<>();

		public static void preRegister(Context context) {
			context.put(annotateKey, (Context.Factory<Annotate>)c -> new StoringQueriesAnnotate(c));
		}

		protected StoringQueriesAnnotate(Context context) {
			super(context);
			this.reader = (CachingClassSymbolClassReader)ClassReader.instance(context);
		}

		@Override
		public void normal(Runnable r) {
			super.normal(r);
			AnnotationCompleterWrapper annotationCompleter = new AnnotationCompleterWrapper(r);
			Symbol sym = annotationCompleter.annotationFor();
			if (sym != null) {
				requested.add(annotationCompleter);
			}
		}

		public List<CompoundTemplate> annotationsFor(Symbol sym) {
			return new ArrayList<>(requested).stream() // needs a copy to avoid Concurrent access
					.filter(annCompleter -> annCompleter.annotationFor() == sym)
					.map(annCompleter -> annCompleter.value(this.reader))
					.flatMap(List::stream)
					.toList();
		}

		public void normal(Symbol sym, List<CompoundTemplate> toAnnotate) {
			if (!toAnnotate.isEmpty()) {
				this.normal(() -> {
					var previousModule = reader.currentModule;
					try {
						reader.currentModule = findModule(sym);
						com.sun.tools.javac.util.List<Compound> newList = com.sun.tools.javac.util.List.from(toAnnotate.stream().map(template -> template.create(reader)).toList());
						if (sym.annotationsPendingCompletion()) {
							sym.setDeclarationAttributes(newList);
						} else {
							sym.appendAttributes(newList);
						}
					} finally {
						reader.currentModule = previousModule;
					}
				});
			}
		}
	}

	public static void preRegister(Context context) {
		// this classReader requires the custom annotate
		StoringQueriesAnnotate.preRegister(context);
		context.put(classReaderKey, (Context.Factory<ClassReader>)c -> new CachingClassSymbolClassReader(c));
	}

	private final Types localTypes;
	private final Names localNames;
	private final Symtab localSyms;
	private final StoringQueriesAnnotate localAnnotate;
	private final byte[] signatureBuffer = new byte[2000]; // big enough to handle any signature
	private Method superSigToTypeMethod = null;

	protected CachingClassSymbolClassReader(Context context) {
		super(context);
		this.localTypes = Types.instance(context);
		this.localNames = Names.instance(context);
		this.localSyms = Symtab.instance(context);
		this.localAnnotate = (StoringQueriesAnnotate)Annotate.instance(context);
		//
		try {
			Field utf8ValidationField = ClassReader.class.getDeclaredField("utf8validation");
			utf8ValidationField.setAccessible(true);
			utf8ValidationField.set(this, Convert.Validation.STRICT);
			Field field = ClassReader.class.getDeclaredField("signatureBuffer");
			field.setAccessible(true);
			field.set(this, signatureBuffer); // long enough to tolerate any type
			superSigToTypeMethod = ClassReader.class.getDeclaredMethod("sigToType", byte[].class, int.class, int.class);
			superSigToTypeMethod.setAccessible(true);
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
		}
	}

	private static class VarSymbolTemplate {
		private final long flags;
		private final Name name;
		private final int pos;
		private final String typeSignature;
		private final Object constantValue;
		private boolean isDataExceptionParameter;
		private boolean isDataResourceVariable;
		private final SymbolMetadataTemplate metadata;
		private List<CompoundTemplate> toAnnotate;
		// + other data ?

		public VarSymbolTemplate(VarSymbol sym, CachingClassSymbolClassReader reader) {
			this.flags = sym.flags_field;
			this.name = sym.name;
			this.pos = sym.pos;
			this.typeSignature = reader.typeToSig(sym.type);
			this.constantValue = sym.getConstantValue();
			this.isDataExceptionParameter = sym.isExceptionParameter();
			this.isDataResourceVariable = sym.isResourceVariable();
			this.metadata = new SymbolMetadataTemplate(sym.getMetadata(), reader);
			this.toAnnotate = reader.localAnnotate.annotationsFor(sym);
		}

		public VarSymbol create(Symbol owner, CachingClassSymbolClassReader reader) {
			VarSymbol res = new VarSymbol(flags, name, reader.sigToType(typeSignature), owner);
			res.pos = this.pos;
			if (isDataExceptionParameter) {
				res.setData(ElementKind.EXCEPTION_PARAMETER);
			} else if (isDataResourceVariable) {
				res.setData(ElementKind.RESOURCE_VARIABLE);
			} else {
				res.setData(this.constantValue);
			}
			this.metadata.applyTo(res, reader);
			reader.localAnnotate.normal(res, this.toAnnotate);
			return res;
		}
	}

	private static class TypeVariableTemplate {
		private final long flags;
		private final Name name;
		private final String lowerBound;
		private final String upperBound;
		private final SymbolMetadataTemplate metadata;
		private final List<CompoundTemplate> toAnnotate;

		public TypeVariableTemplate(TypeVariableSymbol base, CachingClassSymbolClassReader reader) {
			this.flags = base.flags_field;
			this.name = base.name;
			this.lowerBound = base.type instanceof TypeVar typeVar && typeVar.getLowerBound() != null && typeVar.getLowerBound() != reader.localSyms.botType ? reader.typeToSig(typeVar.getLowerBound()) : null;
			this.upperBound = base.type instanceof TypeVar typeVar && typeVar.getUpperBound() != null && typeVar.getUpperBound() != reader.localSyms.botType ? reader.typeToSig(typeVar.getUpperBound()) : null;
			this.metadata = new SymbolMetadataTemplate(base.getMetadata(), reader);
			this.toAnnotate = reader.localAnnotate.annotationsFor(base);
		}
		public TypeVariableSymbol create(Symbol owner, CachingClassSymbolClassReader reader) {
			TypeVar tvar = new TypeVar(name, owner, this.lowerBound != null ? reader.sigToType(this.lowerBound) : reader.localSyms.botType);
			tvar.tsym.flags_field = this.flags;
			// this line needs to be before resolving upper bound as upper bound can reference this type
			reader.typevars.enter(tvar.tsym);
			if (this.upperBound != null) {
				tvar.setUpperBound(reader.sigToType(this.upperBound));
			}
			this.metadata.applyTo(tvar.tsym, reader);
			reader.localAnnotate.normal(tvar.tsym, this.toAnnotate);
			return (TypeVariableSymbol)tvar.tsym;
		}
	}

	private static class MethodSymbolTemplate {
		private final long flags;
		private final String name;
		private final String methodTypeSignature;
 		private final List<VarSymbolTemplate> params;
		private final List<TypeVariableTemplate> typeVariables;
		private final SymbolMetadataTemplate metadata;
		private final AttributeTemplate<?> defaultValue;
		private final List<CompoundTemplate> toAnnotate;
		// + other data ?

		public MethodSymbolTemplate(MethodSymbol sym, CachingClassSymbolClassReader reader) {
			this.flags = sym.flags_field;
			this.name = sym.name.toString();
			this.params = sym.params().map(p -> new VarSymbolTemplate(p, reader));
			this.methodTypeSignature = reader.typeToSig(sym.type);
			this.typeVariables = sym.getTypeParameters().map(t -> new TypeVariableTemplate(t, reader));
			this.metadata = new SymbolMetadataTemplate(sym.getMetadata(), reader);
			this.defaultValue = AttributeTemplate.of(sym.getDefaultValue(), reader);
			this.toAnnotate = reader.localAnnotate.annotationsFor(sym);
		}

		public MethodSymbol create(TypeSymbol owner, CachingClassSymbolClassReader reader) {
			reader.typevars = reader.typevars.dup(owner);
			this.typeVariables.stream()
				.map(template -> template.create(owner, reader))
				.forEach(reader.typevars::enter);
			Type type = reader.sigToType(methodTypeSignature);
			var res = new MethodSymbol(this.flags, reader.localNames.fromString(this.name), type, owner);
			res.params = com.sun.tools.javac.util.List.from(this.params.stream().map(param -> param.create(res, reader)).toList());
			if (this.defaultValue != null) {
				res.defaultValue = this.defaultValue.create(reader);
			}
			this.metadata.applyTo(owner, reader);
			reader.localAnnotate.normal(res, this.toAnnotate);
			reader.typevars = reader.typevars.leave();
			return res;
		}
	}

	private static class SymbolMetadataTemplate {
		public SymbolMetadataTemplate(SymbolMetadata base, CachingClassSymbolClassReader reader) {
		}

		public void applyTo(Symbol owner, CachingClassSymbolClassReader reader) {
		}
	}

	private static abstract class AttributeTemplate<T extends Attribute> {
		protected final String type;
		protected AttributeTemplate(Type type, CachingClassSymbolClassReader reader) {
			this.type = type != null && reader != null ? reader.typeToSig(type) : null;
		}
		protected AttributeTemplate(Attribute source, CachingClassSymbolClassReader reader) {
			this(source != null ? source.type : null, reader);
		}
		public abstract T create(CachingClassSymbolClassReader reader);

		public static AttributeTemplate<? extends Attribute> of(Attribute attribute, CachingClassSymbolClassReader reader) {
			if (attribute == null) {
				return null;
			}
			if (attribute instanceof Compound compound) {
				return new CompoundTemplate(compound, reader);
			}
			if (attribute instanceof Constant constant) {
				return new ConstantAttrTemplate(constant, reader);
			}
			if (attribute instanceof com.sun.tools.javac.code.Attribute.Class classAttr) {
				return new ClassAttrTemplate(classAttr, reader);
			}
			if (attribute instanceof com.sun.tools.javac.code.Attribute.Enum enumAttr) {
				return new EnumAttrTemplate(enumAttr, reader);
			}
			if (attribute instanceof Array arrayAttr) {
				return new ArrayAttrTemplate(arrayAttr, reader);
			}
			// Some *Proxy support, using reflection 😟
			Class<? extends Attribute> clazz = attribute.getClass();
			if (clazz.getName().equals(ClassReader.class.getName() + "$CompoundAnnotationProxy")) {
				try {
					Field valuesField = clazz.getDeclaredField("values");
					valuesField.setAccessible(true);
					return new CompoundTemplate(attribute, (List<Pair<Name, Attribute>>)valuesField.get(attribute), reader);
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			if (clazz.getName().equals(ClassReader.class.getName() + "$EnumAttributeProxy")) {
				try {
					Field enumType = clazz.getDeclaredField("enumType");
					enumType.setAccessible(true);
					Type type = (Type)enumType.get(attribute);
					Field enumValue = clazz.getDeclaredField("enumerator");
					enumValue.setAccessible(true);
					String value = enumValue.get(attribute).toString();
					return new EnumAttrTemplate(type, value, reader);
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			if (clazz.getName().equals(ClassReader.class.getName() + "$ArrayAttributeProxy")) {
				try {
					Field valuesField = clazz.getDeclaredField("values");
					valuesField.setAccessible(true);
					List<Attribute> values = (List<Attribute>)valuesField.get(attribute);
					return new ArrayAttrTemplate(null, values, reader);
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			if (clazz.getName().equals(ClassReader.class.getName() + "$ClassAttributeProxy")) {
				try {
					Field classField = clazz.getDeclaredField("classType");
					classField.setAccessible(true);
					Type c = (Type)classField.get(attribute);
					return new ClassAttrTemplate(c, reader);
				} catch (NoSuchFieldException | IllegalAccessException ex) {
					ILog.get().error(ex.getMessage(), ex);
				}
			}
			// not supported so far, just create a dummy
			return new DummyAttributeTemplate();
		}
	}

	private static class CompoundTemplate extends AttributeTemplate<Compound> {
		private final boolean synthetized;
		private Map<String, AttributeTemplate<?>> values = new HashMap<>();

		public CompoundTemplate(Compound target, CachingClassSymbolClassReader reader) {
			super(target, reader);
			this.synthetized = target.isSynthesized();
			target.getElementValues().forEach((method, attr) -> {
				this.values.put(method.getSimpleName().toString(), AttributeTemplate.of(attr, reader));
			});
		}

		public CompoundTemplate(Attribute attr, List<Pair<Name, Attribute>> list, CachingClassSymbolClassReader reader) {
			super(attr, reader);
			this.synthetized = false;
			list.forEach(pair -> values.put(pair.fst.toString(), AttributeTemplate.of(pair.snd, reader)));
		}

		@Override
		public Compound create(CachingClassSymbolClassReader reader) {
			com.sun.tools.javac.util.List<Pair<MethodSymbol, Attribute>> values = com.sun.tools.javac.util.List.nil();
			Type type = reader.sigToType(this.type);
			for (Entry<String, AttributeTemplate<?>> entry : this.values.entrySet()) {
				MethodSymbol method = findAccessMethod(type, reader.localNames.fromString(entry.getKey()), reader);
				values = values.append(new Pair<>(method, entry.getValue().create(reader)));
			}
			var res = new Compound(reader.sigToType(this.type), values, null);
			res.setSynthesized(this.synthetized);
			return res;
		}

		// copied from super ClassReader
		private MethodSymbol findAccessMethod(Type container, Name name, CachingClassSymbolClassReader reader) {
			CompletionFailure failure = null;
			ModuleSymbol previousModule = reader.currentModule;
			// accessing members may cause class to be loaded, and currentModule to change
			try {
				 for (Symbol sym : container.tsym.members().getSymbolsByName(name)) {
					 if (sym.kind == MTH && sym.type.getParameterTypes().length() == 0)
						 return (MethodSymbol) sym;
				 }
			 } catch (CompletionFailure ex) {
				 failure = ex;
			 } finally {
				 reader.currentModule = previousModule;
			 }
			 // The method wasn't found: emit a warning and recover
//			 JavaFileObject prevSource = log.useSource(requestingOwner.classfile);
//			 try {
//				 if (lintClassfile) {
//					 if (failure == null) {
//						 log.warning(Warnings.AnnotationMethodNotFound(container, name));
//					 } else {
//						 log.warning(Warnings.AnnotationMethodNotFoundReason(container,
//					 }
//				 }
//			 } finally {
//				 log.useSource(prevSource);
//			 }
			// Construct a new method type and symbol.  Use bottom
			// type (typeof null) as return type because this type is
			// a subtype of all reference types and can be converted
			// to primitive types by unboxing.
			MethodType mt = new MethodType(
				com.sun.tools.javac.util.List.nil(),
				reader.localSyms.botType,
				com.sun.tools.javac.util.List.nil(),
				reader.localSyms.methodClass);
			return new MethodSymbol(PUBLIC | ABSTRACT, name, mt, container.tsym);
	    }
	}

	private static class ConstantAttrTemplate extends AttributeTemplate<Constant> {
		private final Object value;
		public ConstantAttrTemplate(Constant target, CachingClassSymbolClassReader reader) {
			super(target, reader);
			this.value = target.getValue();
		}

		@Override
		public Constant create(CachingClassSymbolClassReader reader) {
			return new Constant(reader.sigToType(this.type), value);
		}
	}

	private static class ClassAttrTemplate extends AttributeTemplate<com.sun.tools.javac.code.Attribute.Class> {
		private final String classType;
		public ClassAttrTemplate(Type classType, CachingClassSymbolClassReader reader) {
			super((Type)null, reader);
			this.classType = reader.typeToSig(classType);
		}
		public ClassAttrTemplate(com.sun.tools.javac.code.Attribute.Class target, CachingClassSymbolClassReader reader) {
			super(target, reader);
			this.classType = reader.typeToSig(target.getValue());
		}

		@Override
		public com.sun.tools.javac.code.Attribute.Class create(CachingClassSymbolClassReader reader) {
			return new com.sun.tools.javac.code.Attribute.Class(reader.localTypes, reader.sigToType(this.classType));
		}
	}

	private static class EnumAttrTemplate extends AttributeTemplate<com.sun.tools.javac.code.Attribute.Enum> {
		private final String name;
		public EnumAttrTemplate(Type enumType, String value, CachingClassSymbolClassReader reader) {
			super(enumType, reader);
			name = value;
		}
		public EnumAttrTemplate(com.sun.tools.javac.code.Attribute.Enum target, CachingClassSymbolClassReader reader) {
			super(target, reader);
			name = target.getValue().getSimpleName().toString();
		}

		@Override
		public com.sun.tools.javac.code.Attribute.Enum create(CachingClassSymbolClassReader reader) {
			Type enumType = reader.sigToType(this.type);
			ModuleSymbol previousModule = reader.currentModule;
			VarSymbol sym = (VarSymbol)enumType.tsym.members().findFirst(reader.localNames.fromString(this.name), s -> s instanceof VarSymbol vSym && vSym.isEnum());
			reader.currentModule = previousModule;
			return new com.sun.tools.javac.code.Attribute.Enum(enumType, sym);
		}
	}

	private static class ArrayAttrTemplate extends AttributeTemplate<Array> {
		private List<? extends AttributeTemplate<?>> elements;
		public ArrayAttrTemplate(Type type, List<Attribute> values, CachingClassSymbolClassReader reader) {
			super(type, reader);
			this.elements = values.stream().map(elt -> AttributeTemplate.of(elt, reader)).toList();
		}
		public ArrayAttrTemplate(Array array, CachingClassSymbolClassReader reader) {
			super(array, reader);
			this.elements = array.getValue().stream().map(elt -> AttributeTemplate.of(elt, reader)).toList();
		}
		@Override
		public Array create(CachingClassSymbolClassReader reader) {
			return new Array(reader.sigToType(this.type), this.elements.stream().map(elt -> elt.create(reader)).toArray(Attribute[]::new));
		}
	}


	private static class DummyAttributeTemplate extends AttributeTemplate<Attribute> {
		public DummyAttributeTemplate() {
			super((Attribute)null, null);
		}
		@Override
		public Attribute create(CachingClassSymbolClassReader reader) {
			return new Attribute(null) {
				@Override
				public void accept(Visitor arg0) {
					// do nothing
				}
			};
		}
	}

	private static class AnnotationTypeMetadataTemplate {
		private final CompoundTemplate target;
		private final CompoundTemplate repeatable;

		public AnnotationTypeMetadataTemplate(AnnotationTypeMetadata annotationTypeMetadata, CachingClassSymbolClassReader reader) {
			this.target = annotationTypeMetadata.getTarget() != null ? new CompoundTemplate(annotationTypeMetadata.getTarget(), reader) : null;
			this.repeatable = annotationTypeMetadata.getRepeatable() != null ? new CompoundTemplate(annotationTypeMetadata.getRepeatable(), reader) : null;
		}

		public AnnotationTypeMetadata create(ClassSymbol c, CachingClassSymbolClassReader reader) {
			return new AnnotationTypeMetadata(c, new AnnotationTypeCompleter() {
				@Override
				public void complete(ClassSymbol sym) throws CompletionFailure {
					if (target != null) {
						sym.getAnnotationTypeMetadata().setTarget(target.create(reader));
					}
					if (repeatable != null) {
						sym.getAnnotationTypeMetadata().setRepeatable(repeatable.create(reader));
					}
				}
			});
		}

	}

	private static class ClassSymbolTemplate {

		private final Instant creationTime;
		private final long flags;
		private final String superSymbol;
		private final List<String> interfaces;
		private final List<String> permitted;
		private final List<TypeVariableTemplate> typeParams;
		private final boolean isPermittedExplicit;
		private final List<?> members;
		private final List<String> innerTypes;
		private final JavaFileObject classFile;
		private final SymbolMetadataTemplate metadata;
		private final AnnotationTypeMetadataTemplate annotationTypeMetadataTemplate;
		private List<CompoundTemplate> toAnnotate;

		public ClassSymbolTemplate(ClassSymbol base, Instant creationTime, CachingClassSymbolClassReader reader) {
			this.creationTime = creationTime;
			this.classFile = base.classfile;
			this.flags = base.flags_field;
			this.superSymbol = reader.typeToSig(base.getSuperclass());
			this.interfaces = base.getInterfaces().map(reader::typeToSig);
			this.permitted = base.getPermittedSubclasses().map(reader.localTypes::erasure).map(reader::typeToSig);
			this.typeParams = base.getTypeParameters().map(t -> new TypeVariableTemplate(t, reader));
			this.isPermittedExplicit = base.isPermittedExplicit;
			this.members = StreamSupport.stream(base.members().getSymbols().spliterator(), false).map(member ->
				member instanceof VarSymbol varSymbol ? new VarSymbolTemplate(varSymbol, reader) :
				member instanceof MethodSymbol methodSymbol ? new MethodSymbolTemplate(methodSymbol, reader) :
				null).toList();
			this.innerTypes = StreamSupport.stream(base.members().getSymbols(ClassSymbol.class::isInstance).spliterator(), false)
					.map(ClassSymbol.class::cast)
					.map(ClassSymbol::getSimpleName)
					.map(Name::toString)
					.toList();
			this.metadata = new SymbolMetadataTemplate(base.getMetadata(), reader);
			this.annotationTypeMetadataTemplate = base.isAnnotationType() ?
				new AnnotationTypeMetadataTemplate(base.getAnnotationTypeMetadata(), reader) :
				null;
			this.toAnnotate = reader.localAnnotate.annotationsFor(base);
		}

		public boolean isObsolete() {
			return this.classFile.getLastModified() > this.creationTime.toEpochMilli();
		}

		public void applyTo(ClassSymbol target, CachingClassSymbolClassReader reader) {
			if (target.classfile == null) {
				target.classfile = this.classFile;
			}
			reader.currentOwner = target;
	        reader.currentClassFile = target.classfile;

			target.flags_field = this.flags;
			reader.currentModule = findModule(target);

			target.isPermittedExplicit = this.isPermittedExplicit;

			ClassType ct = (ClassType)target.type;
			// allocate scope for members
			target.members_field = WriteableScope.create(target);

			// prepare type variable table
			reader.typevars = reader.typevars.dup(reader.currentOwner);
			if (ct.getEnclosingType().hasTag(TypeTag.CLASS))
				reader.enterTypevars(target.owner, ct.getEnclosingType());
			this.typeParams.stream()
				.map(template -> template.create(target, reader))
				.forEach(reader.typevars::enter);

			//
			members.stream().map(template -> toSymbol(template, target, reader))
				.filter(Symbol.class::isInstance)
				.map(Symbol.class::cast)
				.forEach(target.members_field::enter);

			ct.supertype_field = reader.sigToType(this.superSymbol);
			ct.interfaces_field = com.sun.tools.javac.util.List.from(this.interfaces.stream()
					.map(reader::sigToType)
					.toArray(Type[]::new));
			ct.typarams_field = com.sun.tools.javac.util.List.from(this.typeParams.stream()
					.map(t -> t.name)
					.map(reader.typevars::findFirst)
					.filter(Objects::nonNull)
					.map(s -> s.type)
					.toArray(Type[]::new));
			target.isPermittedExplicit = this.isPermittedExplicit;
			this.permitted.stream()
				.map(reader::sigToType)
				.map(type -> type.tsym)
				.filter(ClassSymbol.class::isInstance)
				.map(ClassSymbol.class::cast)
				.forEach(tsym -> target.addPermittedSubclass(tsym, 0));
			this.metadata.applyTo(target, reader);
			if (target.isAnnotationType() && this.annotationTypeMetadataTemplate != null) {
				target.setAnnotationTypeMetadata(this.annotationTypeMetadataTemplate.create(target, reader));
			}
			for (String innerClass : this.innerTypes) {
				// from ClassFinder.readInnerClasses`
				ClassSymbol member = reader.enterClass(reader.localNames.fromString(innerClass), target);
				if ((flags & STATIC) == 0) {
					((ClassType)member.type).setEnclosingType(target.type);
					if (member.erasure_field != null) {
						((ClassType)member.erasure_field).setEnclosingType(reader.localTypes.erasure(target.type));
					}
				}
				// from super enterMembers
				if ((member.flags_field & (SYNTHETIC|BRIDGE)) != SYNTHETIC || member.name.startsWith(reader.localNames.lambda))
		            target.members().enter(member);
			}

			reader.localAnnotate.normal(target, this.toAnnotate);

			reader.typevars = reader.typevars.leave();
		}

		private Object toSymbol(Object o, Symbol owner, CachingClassSymbolClassReader reader) {
			if (o instanceof VarSymbolTemplate varTemplate) {
				return varTemplate.create(owner, reader);
			}
			if (o instanceof MethodSymbolTemplate methodTemplate && owner instanceof TypeSymbol typeOwner) {
				return methodTemplate.create(typeOwner, reader);
			}
			// other
			return null;
		}
	}

	@Override
	public void readClassFile(ClassSymbol c) {
		if (c.classfile == null ||
			// currently not cache content from system library as same path can have different content according to context
			c.classfile.getClass().getSimpleName().equals("JRTFileObject") ||
			c.classfile.getClass().getSimpleName().endsWith("SigJavaFileObject") ||
			(c.classfile instanceof PathFileObject pathFileObject && "JrtPath".equals(pathFileObject.getPath().getClass().getSimpleName())) ||
			// TODO support caching module-info too
			Objects.equals(localNames.module_info, c.getSimpleName())) {
			super.readClassFile(c);
		} else {
			ClassSymbolTemplate template = CACHE.get(c.classfile);
			if (template == null || template.isObsolete()) {
				Instant now = Instant.now(); // before actually reading the class file
				super.readClassFile(c);
				CACHE.put(c.classfile, new ClassSymbolTemplate(c, now, this));
			} else {
				template.applyTo(c, this);
			}
		}
	}

	public static ModuleSymbol findModule(Symbol target) {
		Symbol moduleSymbol = target;
		while (!(moduleSymbol instanceof ModuleSymbol) && !(moduleSymbol instanceof PackageSymbol) && moduleSymbol != null) {
			moduleSymbol = moduleSymbol.owner;
		}
		if (moduleSymbol instanceof ModuleSymbol theModuleSymbol) {
			return theModuleSymbol;
		}
		if (moduleSymbol instanceof PackageSymbol packageSymbol) {
			return packageSymbol.modle;
		}
		return null;
	}

	Type sigToType(String typeSignature) {
		if (typeSignature == null || superSigToTypeMethod == null) {
			return Type.noType;
		}
		try {
			byte[] bytes = typeSignature.getBytes();
			return (Type)superSigToTypeMethod.invoke(this, bytes, 0, bytes.length);
		} catch (Exception ex) {
			ILog.get().error(ex.getMessage(), ex);
			return null;
		}
	}

	String typeToSig(Type type) {
		if (type == Type.noType) {
			return null;
		}
		StringBuilder res = new StringBuilder();
		var generator = localTypes.new SignatureGenerator() {
			@Override
			protected void append(char ch) {
				res.append(ch);
			}

			@Override
			protected void append(byte[] ba) {
				res.append(new String(ba));
			}

			@Override
			protected void append(Name name) {
				res.append(name.toString());
			}

			// workaround to ensure exceptions are added
			@Override
			public boolean hasTypeVar(com.sun.tools.javac.util.List<Type> l) {
				return l != null && !l.isEmpty();
			}
		};
		generator.assembleSig(type);
		return res.toString();
	}

	/// Workaround the fact that ClassReader.AnnotationCompleter is not visible
	private static class AnnotationCompleterWrapper {
		private static final Class<?> ANNOTATION_COMPLETER_CLASS;
		private static final Field SYM_FIELD;
		private static final Field VALUE_FIELD;
		static {
			Class<?> c = null;
			Field sym = null;
			Field value = null;
			try {
				c = ClassReader.class.getClassLoader().loadClass(ClassReader.class.getName() + "$AnnotationCompleter");
				sym = c.getDeclaredField("sym");
				sym.setAccessible(true);
				value = c.getDeclaredField("l");
				value.setAccessible(true);
			} catch (ClassNotFoundException | NoClassDefFoundError | NoSuchFieldException err) {
				ILog.get().error(err.getMessage(), err);
			}
			ANNOTATION_COMPLETER_CLASS = c;
			SYM_FIELD = sym;
			VALUE_FIELD = value;
		}
		private final Object annotationCompleter;
		public AnnotationCompleterWrapper(Object o) {
			annotationCompleter = ANNOTATION_COMPLETER_CLASS.isInstance(o) ? o : null;
		}
		public Symbol annotationFor() {
			if (this.annotationCompleter == null) {
				return null;
			}
			try {
				return (Symbol)SYM_FIELD.get(annotationCompleter);
			} catch (IllegalArgumentException | IllegalAccessException e) {
				ILog.get().error(e.getMessage(), e);
			}
			return null;
		}
		public List<CompoundTemplate> value(CachingClassSymbolClassReader reader) {
			if (this.annotationCompleter == null) {
				return List.of();
			}
			try {
				Object o = VALUE_FIELD.get(annotationCompleter);
				if (o instanceof List<?> attributes) {
					return attributes.stream().map(Attribute.class::cast).map(attr -> AttributeTemplate.of(attr, reader)).map(CompoundTemplate.class::cast).toList();
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				ILog.get().error(e.getMessage(), e);
			}
			return List.of();
		}
	}
}
