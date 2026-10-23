/*******************************************************************************
 * Copyright (c) 2007 - 2023 BEA Systems, Inc. and others
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *    Walter Harley - initial API and implementation
 *    IBM Corporation - fix for 342598, 382590
 *    Jean-Marie Henaff <jmhenaff@google.com> (Google) - Bug 481555
 *******************************************************************************/

package org.eclipse.jdt.internal.compiler.apt.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.IntersectionType;
import javax.lang.model.type.NoType;
import javax.lang.model.type.NullType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.WildcardType;
import javax.lang.model.util.Types;
import org.eclipse.jdt.core.compiler.CharOperation;
import org.eclipse.jdt.internal.compiler.apt.dispatch.BaseProcessingEnvImpl;
import org.eclipse.jdt.internal.compiler.ast.Wildcard;
import org.eclipse.jdt.internal.compiler.lookup.*;

/**
 * Utilities for working with types (as opposed to elements).
 * There is one of these for every ProcessingEnvironment.
 */
public class TypesImpl implements Types {

    private final BaseProcessingEnvImpl _env;

    /*
     * The processing env creates and caches a TypesImpl.  Other clients should
     * not create their own; they should ask the env for it.
     */
    public TypesImpl(BaseProcessingEnvImpl env) {
        this._env = env;
    }

    /* (non-Javadoc)
     * @see javax.lang.model.util.Types#asElement(javax.lang.model.type.TypeMirror)
     */
    @Override
    public Element asElement(TypeMirror t) {
        switch(t.getKind()) {
        case DECLARED :
        case TYPEVAR :
            return this._env.getFactory().newElement(((TypeMirrorImpl)t).binding());
        default:
            break;
        }
        return null;
    }

    @Override
	public TypeMirror asMemberOf(DeclaredType containing, Element element) {
		// throw new UnsupportedOperationException("NYI: TypesImpl.asMemberOf("
		// + containing + ", " + element + ")"); //$NON-NLS-1$ //$NON-NLS-2$
		// //$NON-NLS-3$
		ElementImpl elementImpl = (ElementImpl) element;
		DeclaredTypeImpl declaredTypeImpl = (DeclaredTypeImpl) containing;
		ReferenceBinding referenceBinding = (ReferenceBinding) declaredTypeImpl._binding;
		TypeMirror typeMirror;

		switch (element.getKind()) {
		case CONSTRUCTOR:
		case METHOD:
			typeMirror = findMemberInHierarchy(referenceBinding, elementImpl._binding, new MemberInTypeFinder() {
				@Override
				public TypeMirror find(ReferenceBinding typeBinding, Binding memberBinding) {
					MethodBinding methodBinding = ((MethodBinding) memberBinding);
					for (MethodBinding method : typeBinding.methods()) {
						if (CharOperation.equals(method.selector, methodBinding.selector)
								&& (method.original() == methodBinding
										|| method.areParameterErasuresEqual(methodBinding))) {
							return TypesImpl.this._env.getFactory().newTypeMirror(method);
						}
					}
					return null;
				}
			});

			if (typeMirror != null) {
				return typeMirror;
			}
			break;
		case TYPE_PARAMETER:
			typeMirror = findMemberInHierarchy(referenceBinding, elementImpl._binding, new MemberInTypeFinder() {
				@Override
				public TypeMirror find(ReferenceBinding typeBinding, Binding memberBinding) {
					if (typeBinding instanceof ParameterizedTypeBinding) {
						TypeVariableBinding variableBinding = ((TypeVariableBinding) memberBinding);
						ReferenceBinding binding = ((ParameterizedTypeBinding) typeBinding).genericType();
						if (variableBinding.declaringElement == binding) { // check in advance avoid looking into type parameters unnecessarily.
							TypeVariableBinding[] typeVariables = binding.typeVariables();
							TypeBinding[] typeArguments = ((ParameterizedTypeBinding) typeBinding).typeArguments();
							if (typeVariables.length == typeArguments.length) {
								for(int i = 0; i < typeVariables.length; i++) {
									if (typeVariables[i] == memberBinding) {
										return TypesImpl.this._env.getFactory().newTypeMirror(typeArguments[i]);
									}
								}
							}
						}
					}
					return null;
				}
			});

			if (typeMirror != null) {
				return typeMirror;
			}
			break;
		case FIELD:
		case ENUM_CONSTANT:
		case RECORD_COMPONENT:
			typeMirror = findMemberInHierarchy(referenceBinding, elementImpl._binding, new MemberInTypeFinder() {
				@Override
				public TypeMirror find(ReferenceBinding typeBinding, Binding memberBinding) {
					VariableBinding variableBinding = (VariableBinding) memberBinding;
					for (FieldBinding field : typeBinding.fields()) {
						if (CharOperation.equals(field.name, variableBinding.name)) {
							return TypesImpl.this._env.getFactory().newTypeMirror(field);
						}
					}
					return null;
				}
			});

			if (typeMirror != null) {
				return typeMirror;
			}
			break;
		case ENUM:
		case ANNOTATION_TYPE:
		case INTERFACE:
		case CLASS:
		case RECORD:
			typeMirror = findMemberInHierarchy(referenceBinding, elementImpl._binding, new MemberInTypeFinder() {
				@Override
				public TypeMirror find(ReferenceBinding typeBinding, Binding memberBinding) {
					ReferenceBinding elementBinding = (ReferenceBinding) memberBinding;
					// If referenceBinding is a ParameterizedTypeBinding, this
					// will return only ParameterizedTypeBindings
					// for member types, even if the member happens to be a
					// static nested class. That's probably a bug;
					// static nested classes are not parameterized by their
					// outer class.
					for (ReferenceBinding memberReferenceBinding : typeBinding.memberTypes()) {
						if (CharOperation.equals(elementBinding.compoundName, memberReferenceBinding.compoundName)) {
							return TypesImpl.this._env.getFactory().newTypeMirror(memberReferenceBinding);
						}
					}
					return null;
				}
			});

			if (typeMirror != null) {
				return typeMirror;
			}
			break;
		default:
			throw new IllegalArgumentException("element " + element + //$NON-NLS-1$
					" has unrecognized element kind " + element.getKind()); //$NON-NLS-1$
		}
		throw new IllegalArgumentException("element " + element + //$NON-NLS-1$
				" is not a member of the containing type " + containing + //$NON-NLS-1$
				" nor any of its superclasses"); //$NON-NLS-1$
	}

	private static interface MemberInTypeFinder {
		TypeMirror find(ReferenceBinding typeBinding, Binding memberBinding);
	}

	private TypeMirror findMemberInHierarchy(ReferenceBinding typeBinding, Binding memberBinding,
			MemberInTypeFinder finder) {
		TypeMirror result = null;

		if (typeBinding == null) {
			return null;
		}

		result = finder.find(typeBinding, memberBinding);
		if (result != null) {
			return result;
		}

		result = findMemberInHierarchy(typeBinding.superclass(), memberBinding, finder);
		if (result != null) {
			return result;
		}

		for (ReferenceBinding superInterface : typeBinding.superInterfaces()) {
			result = findMemberInHierarchy(superInterface, memberBinding, finder);
			if (result != null) {
				return result;
			}
		}

		return null;
	}
	private void validateRealType(TypeMirror t) {
		switch (t.getKind()) {
			case EXECUTABLE:
			case PACKAGE:
			case MODULE:
				throw new IllegalArgumentException(
						"Executable, package and module are illegal argument for Types.contains(..)"); //$NON-NLS-1$
			default:
				break;
		}
	}
	private void validateRealTypes(TypeMirror t1, TypeMirror t2) {
		validateRealType(t1);
		validateRealType(t2);
	}

    @Override
    public TypeElement boxedClass(PrimitiveType p) {
        PrimitiveTypeImpl primitiveTypeImpl = (PrimitiveTypeImpl) p;
        BaseTypeBinding baseTypeBinding = (BaseTypeBinding)primitiveTypeImpl._binding;
        TypeBinding boxed = this._env.getLookupEnvironment().computeBoxingType(baseTypeBinding);
        return (TypeElement) this._env.getFactory().newElement(boxed);
    }

    @Override
    public TypeMirror capture(TypeMirror t) {
    	validateRealType(t);
    	TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) t;
    	if (typeMirrorImpl._binding instanceof ParameterizedTypeBinding) {
    		throw new UnsupportedOperationException("NYI: TypesImpl.capture(...)"); //$NON-NLS-1$
    	}
        return t;
    }

    @Override
    public boolean contains(TypeMirror t1, TypeMirror t2) {
    	validateRealTypes(t1, t2);
        throw new UnsupportedOperationException("NYI: TypesImpl.contains(" + t1 + ", " + t2 + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    @Override
    public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    	validateRealType(t);
    	if (t instanceof IntersectionType intersectionType) {
    		// JLS §4.10.2: the direct supertypes of an intersection type are its components
    		return intersectionType.getBounds();
    	}
        TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) t;
        Binding binding = typeMirrorImpl._binding;
        if (binding instanceof ReferenceBinding) {
            ReferenceBinding referenceBinding = (ReferenceBinding) binding;
            ArrayList<TypeMirror> list = new ArrayList<>();
            ReferenceBinding superclass = referenceBinding.superclass();
            if (superclass != null) {
                list.add(this._env.getFactory().newTypeMirror(superclass));
            }
            for (ReferenceBinding interfaceBinding : referenceBinding.superInterfaces()) {
                list.add(this._env.getFactory().newTypeMirror(interfaceBinding));
            }
            return Collections.unmodifiableList(list);
        }
        return Collections.emptyList();
    }

    @Override
    public TypeMirror erasure(TypeMirror t) {
    	validateRealType(t);
    	if (t instanceof IntersectionType intersectionType) {
    		// JLS §4.6: the erasure of an intersection type is the erasure of its leftmost component
    		return erasure(intersectionType.getBounds().get(0));
    	}
        TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) t;
        Binding binding = typeMirrorImpl._binding;
        if (binding instanceof ReferenceBinding) {
        	TypeBinding type = ((ReferenceBinding) binding).erasure();
        	if (type.isGenericType()) {
        		type = this._env.getLookupEnvironment().convertToRawType(type, false);
        	}
            return this._env.getFactory().newTypeMirror(type);
        }
        if (binding instanceof ArrayBinding) {
            TypeBinding typeBinding = (TypeBinding) binding;
            TypeBinding leafType = typeBinding.leafComponentType().erasure();
            if (leafType.isGenericType()) {
            	leafType = this._env.getLookupEnvironment().convertToRawType(leafType, false);
            }
            return this._env.getFactory().newTypeMirror(
                    this._env.getLookupEnvironment().createArrayType(leafType,
                            typeBinding.dimensions()));
        }
        return t;
    }

    @Override
    public ArrayType getArrayType(TypeMirror componentType) {
        if (componentType.getKind() == TypeKind.INTERSECTION) {
            throw new IllegalArgumentException("Invalid array component type: " + componentType); //$NON-NLS-1$
        }
        TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) componentType;
        TypeBinding typeBinding = (TypeBinding) typeMirrorImpl._binding;
        return (ArrayType) this._env.getFactory().newTypeMirror(
                this._env.getLookupEnvironment().createArrayType(
                        typeBinding.leafComponentType(),
                        typeBinding.dimensions() + 1));
    }

    /*
     * (non-Javadoc)
     * Create a type instance by parameterizing a type element. If the element is a member type,
     * its container won't be parameterized (if it needs to be, you would need to use the form of
     * getDeclaredType that takes a container TypeMirror). If typeArgs is empty, and typeElem
     * is not generic, then you should use TypeElem.asType().  If typeArgs is empty and typeElem
     * is generic, this method will create the raw type.
     */
    @Override
    public DeclaredType getDeclaredType(TypeElement typeElem, TypeMirror... typeArgs) {
        int typeArgsLength = typeArgs.length;
        TypeElementImpl typeElementImpl = (TypeElementImpl) typeElem;
        ReferenceBinding elementBinding = (ReferenceBinding) typeElementImpl._binding;
        TypeVariableBinding[] typeVariables = elementBinding.typeVariables();
        int typeVariablesLength = typeVariables.length;
        if (typeArgsLength == 0) {
            if (elementBinding.isGenericType()) {
                // per javadoc,
                return (DeclaredType) this._env.getFactory().newTypeMirror(this._env.getLookupEnvironment().createRawType(elementBinding, null));
            }
            return (DeclaredType)typeElem.asType();
        } else if (typeArgsLength != typeVariablesLength) {
            throw new IllegalArgumentException("Number of typeArguments doesn't match the number of formal parameters of typeElem"); //$NON-NLS-1$
        }
        TypeBinding[] typeArguments = new TypeBinding[typeArgsLength];
        for (int i = 0; i < typeArgsLength; i++) {
            TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) typeArgs[i];
            Binding binding = typeMirrorImpl._binding;
            if (!(binding instanceof TypeBinding)) {
                throw new IllegalArgumentException("Invalid type argument: " + typeMirrorImpl); //$NON-NLS-1$
            }
            typeArguments[i] = (TypeBinding) binding;
        }

        ReferenceBinding enclosing = elementBinding.enclosingType();
        if (enclosing != null) {
            enclosing = this._env.getLookupEnvironment().createRawType(enclosing, null);
        }

        return (DeclaredType) this._env.getFactory().newTypeMirror(
                this._env.getLookupEnvironment().createParameterizedType(elementBinding, typeArguments, enclosing));
    }

    /* (non-Javadoc)
     * Create a specific type from a member element. The containing type can be parameterized,
     * e.g. Outer<String>.Inner, but it cannot be generic, i.e., Outer<T>.Inner. It only makes
     * sense to use this method when the member element is parameterized by its container; so,
     * for example, it makes sense for an inner class but not for a static member class.
     * Otherwise you should just use getDeclaredType(TypeElement, TypeMirror ...), if you need
     * to specify type arguments, or TypeElement.asType() directly, if not.
     */
    @Override
    public DeclaredType getDeclaredType(DeclaredType containing, TypeElement typeElem,
            TypeMirror... typeArgs) {
        int typeArgsLength = typeArgs.length;
        TypeElementImpl typeElementImpl = (TypeElementImpl) typeElem;
        ReferenceBinding elementBinding = (ReferenceBinding) typeElementImpl._binding;
        TypeVariableBinding[] typeVariables = elementBinding.typeVariables();
        int typeVariablesLength = typeVariables.length;
        DeclaredTypeImpl declaredTypeImpl = (DeclaredTypeImpl) containing;
        ReferenceBinding enclosingType = (ReferenceBinding) declaredTypeImpl._binding;
        if (typeArgsLength == 0) {
            if (elementBinding.isGenericType()) {
                // e.g., Outer.Inner<T> but T is not specified
                // Per javadoc on interface, must return the raw type Outer.Inner
                return (DeclaredType) this._env.getFactory().newTypeMirror(
                        this._env.getLookupEnvironment().createRawType(elementBinding, enclosingType));
            } else {
                // e.g., Outer<Long>.Inner
                ParameterizedTypeBinding ptb = this._env.getLookupEnvironment().createParameterizedType(elementBinding, null, enclosingType);
                return (DeclaredType) this._env.getFactory().newTypeMirror(ptb);
            }
        } else if (typeArgsLength != typeVariablesLength) {
            throw new IllegalArgumentException("Number of typeArguments doesn't match the number of formal parameters of typeElem"); //$NON-NLS-1$
        }
        TypeBinding[] typeArguments = new TypeBinding[typeArgsLength];
        for (int i = 0; i < typeArgsLength; i++) {
            TypeMirrorImpl typeMirrorImpl = (TypeMirrorImpl) typeArgs[i];
            Binding binding = typeMirrorImpl._binding;
            if (!(binding instanceof TypeBinding)) {
                throw new IllegalArgumentException("Invalid type for a type arguments : " + typeMirrorImpl); //$NON-NLS-1$
            }
            typeArguments[i] = (TypeBinding) binding;
        }
        return (DeclaredType) this._env.getFactory().newTypeMirror(
                this._env.getLookupEnvironment().createParameterizedType(elementBinding, typeArguments, enclosingType));
    }

    @Override
    public NoType getNoType(TypeKind kind) {
        return this._env.getFactory().getNoType(kind);
    }

    @Override
    public NullType getNullType() {
        return this._env.getFactory().getNullType();
    }

    @Override
    public PrimitiveType getPrimitiveType(TypeKind kind) {
        return this._env.getFactory().getPrimitiveType(kind);
    }

    @Override
    public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
        if (extendsBound != null && superBound != null) {
            throw new IllegalArgumentException("Extends and super bounds cannot be set at the same time"); //$NON-NLS-1$
        }
        if ((extendsBound != null && extendsBound.getKind() == TypeKind.INTERSECTION)
                || (superBound != null && superBound.getKind() == TypeKind.INTERSECTION)) {
            throw new IllegalArgumentException("Invalid wildcard bound: " + (extendsBound != null ? extendsBound : superBound)); //$NON-NLS-1$
        }
        if (extendsBound != null) {
            TypeMirrorImpl extendsBoundMirrorType = (TypeMirrorImpl) extendsBound;
            TypeBinding typeBinding = (TypeBinding) extendsBoundMirrorType._binding;
            return (WildcardType) this._env.getFactory().newTypeMirror(
                    this._env.getLookupEnvironment().createWildcard(
                            null,
                            0,
                            typeBinding,
                            null,
                            Wildcard.EXTENDS));
        }
        if (superBound != null) {
            TypeMirrorImpl superBoundMirrorType = (TypeMirrorImpl) superBound;
            TypeBinding typeBinding = (TypeBinding) superBoundMirrorType._binding;
            return new WildcardTypeImpl(this._env, this._env.getLookupEnvironment().createWildcard(
                    null,
                    0,
                    typeBinding,
                    null,
                    Wildcard.SUPER));
        }
        return new WildcardTypeImpl(this._env, this._env.getLookupEnvironment().createWildcard(
                null,
                0,
                null,
                null,
                Wildcard.UNBOUND));
    }

    /* (non-Javadoc)
     * @return true if a value of type t1 can be assigned to a variable of type t2, i.e., t2 = t1.
     */
    @Override
    public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    	validateRealTypes(t1, t2);
        if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
        	return false;
        }
        Binding b1 = ((TypeMirrorImpl)t1).binding();
        Binding b2 = ((TypeMirrorImpl)t2).binding();
        if (!(b1 instanceof TypeBinding) || !(b2 instanceof TypeBinding)) {
            // package, method, import, etc.
            throw new IllegalArgumentException();
        }
        if (b1 == b2) {
            return true;
        }
        if (isSameIntersectionType((TypeBinding) b1, (TypeBinding) b2)) {
            return true;
        }
        if (t2 instanceof IntersectionType intersectionType) {
            for (TypeMirror bound : intersectionType.getBounds()) {
                if (!isAssignable(t1, bound)) {
                    return false;
                }
            }
            return true;
        }
        if (((TypeBinding)b1).isCompatibleWith((TypeBinding)b2)) {
            return true;
        }

        TypeBinding convertedType = this._env.getLookupEnvironment().computeBoxingType((TypeBinding)b1);
        return null != convertedType && convertedType.isCompatibleWith((TypeBinding)b2);
    }

    @Override
    public boolean isSameType(TypeMirror t1, TypeMirror t2) {
        if (t1 instanceof NoTypeImpl) {
            if (t2 instanceof NoTypeImpl) {
                return ((NoTypeImpl) t1).getKind() == ((NoTypeImpl) t2).getKind();
            }
            return false;
        } else if (t2 instanceof NoTypeImpl) {
            return false;
        }
        if (t1.getKind() == TypeKind.WILDCARD || t2.getKind() == TypeKind.WILDCARD) {
            // Wildcard types are never equal, according to the spec of this method
            return false;
        }
        if (t1 == t2) {
            return true;
        }
        if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
            return false;
        }
        Binding b1 = ((TypeMirrorImpl)t1).binding();
        Binding b2 = ((TypeMirrorImpl)t2).binding();

        if (b1 == b2) {
            return true;
        }
        if (!(b1 instanceof TypeBinding) || !(b2 instanceof TypeBinding)) {
            return false;
        }
        TypeBinding type1 = ((TypeBinding) b1);
        TypeBinding type2 = ((TypeBinding) b2);
        if (TypeBinding.equalsEquals(type1,  type2))
        	return true;
        if (IntersectionComparison.mayMentionIntersection(type1) || IntersectionComparison.mayMentionIntersection(type2)) {
            IntersectionComparison comparison = new IntersectionComparison();
            if (comparison.mentionsIntersection(type1) || comparison.mentionsIntersection(type2)) {
                return comparison.isSameType(type1, type2);
            }
        }
        return CharOperation.equals(type1.computeUniqueKey(), type2.computeUniqueKey());
    }

    /*
     * The compiler compares intersection bindings by identity, which may differ for equal types (see IntersectionComparison).
     */
    private static boolean isSameIntersectionType(TypeBinding type1, TypeBinding type2) {
        if (!IntersectionComparison.mayMentionIntersection(type1) && !IntersectionComparison.mayMentionIntersection(type2)) {
            return false;
        }
        IntersectionComparison comparison = new IntersectionComparison();
        return (comparison.mentionsIntersection(type1) || comparison.mentionsIntersection(type2)) && comparison.isSameType(type1, type2);
    }

    /*
     * Compares types that contain intersections. The unique key of an intersection binding is the one of its erasure,
     * so it cannot be used. Equivalent intersections share one binding, but bindings of equal types may still differ,
     * e.g. type variables of a member of differently annotated parameterizations of a type, so compare the structure.
     * Type arguments may be shared, so results are remembered by binding.
     */
    private static class IntersectionComparison {
        private final Map<TypeBinding, Boolean> intersectionMentions = new IdentityHashMap<>();
        private final Map<TypeBinding, Set<TypeBinding>> sameTypes = new IdentityHashMap<>();

        /*
         * Cheap check without traversing type arguments: false for the kinds of types that cannot embed an intersection,
         * which are the ones compared most often, so that no comparison needs to be created for them.
         */
        static boolean mayMentionIntersection(TypeBinding type) {
            if (type == null) {
                return false;
            }
            switch (type.kind()) {
                case Binding.INTERSECTION_TYPE18:
                case Binding.PARAMETERIZED_TYPE:
                case Binding.WILDCARD_TYPE:
                    return true;
                case Binding.ARRAY_TYPE:
                    return mayMentionIntersection(type.leafComponentType());
                case Binding.RAW_TYPE:
                    return mayMentionIntersection(type.enclosingType());
                case Binding.TYPE_PARAMETER:
                    return ((TypeVariableBinding) type).declaringElement instanceof MethodBinding method
                            && mayMentionIntersection(method.declaringClass);
                default:
                    return false;
            }
        }

        boolean mentionsIntersection(TypeBinding type) {
            if (type == null) {
                return false;
            }
            Boolean result = this.intersectionMentions.get(type);
            if (result == null) {
                result = mentionsIntersection0(type);
                this.intersectionMentions.put(type, result);
            }
            return result;
        }

        private boolean mentionsIntersection0(TypeBinding type) {
            switch (type.kind()) {
                case Binding.INTERSECTION_TYPE18:
                    return true;
                case Binding.ARRAY_TYPE:
                    return mentionsIntersection(type.leafComponentType());
                case Binding.PARAMETERIZED_TYPE:
                    ParameterizedTypeBinding parameterizedType = (ParameterizedTypeBinding) type;
                    if (parameterizedType.arguments != null) {
                        for (TypeBinding argument : parameterizedType.arguments) {
                            if (mentionsIntersection(argument)) {
                                return true;
                            }
                        }
                    }
                    return mentionsIntersection(parameterizedType.enclosingType());
                case Binding.RAW_TYPE:
                    return mentionsIntersection(type.enclosingType());
                case Binding.WILDCARD_TYPE:
                    return mentionsIntersection(((WildcardBinding) type).bound);
                case Binding.TYPE_PARAMETER:
                    // the unique key of a type variable of a generic method includes the parameterization of its declaring type
                    return ((TypeVariableBinding) type).declaringElement instanceof MethodBinding method
                            && mentionsIntersection(method.declaringClass);
                default:
                    return false;
            }
        }

        boolean isSameType(TypeBinding type1, TypeBinding type2) {
            if (TypeBinding.equalsEquals(type1, type2)) {
                return true;
            }
            if (type1 == null || type2 == null) {
                return false;
            }
            Set<TypeBinding> sameTypes1 = this.sameTypes.get(type1);
            if (sameTypes1 != null && sameTypes1.contains(type2)) {
                return true;
            }
            boolean result;
            if (!mentionsIntersection(type1) && !mentionsIntersection(type2)) {
                result = CharOperation.equals(type1.computeUniqueKey(), type2.computeUniqueKey());
            } else if (type1.kind() != type2.kind()) {
                result = false;
            } else {
                switch (type1.kind()) {
                    case Binding.INTERSECTION_TYPE18:
                        result = isSameTypes(type1.getIntersectingTypes(), type2.getIntersectingTypes());
                        break;
                    case Binding.ARRAY_TYPE:
                        result = type1.dimensions() == type2.dimensions() && isSameType(type1.leafComponentType(), type2.leafComponentType());
                        break;
                    case Binding.PARAMETERIZED_TYPE:
                        ParameterizedTypeBinding parameterizedType1 = (ParameterizedTypeBinding) type1;
                        ParameterizedTypeBinding parameterizedType2 = (ParameterizedTypeBinding) type2;
                        result = TypeBinding.equalsEquals(parameterizedType1.genericType(), parameterizedType2.genericType())
                                && isSameTypes(parameterizedType1.arguments, parameterizedType2.arguments)
                                && isSameType(parameterizedType1.enclosingType(), parameterizedType2.enclosingType());
                        break;
                    case Binding.RAW_TYPE:
                        result = TypeBinding.equalsEquals(((RawTypeBinding) type1).genericType(), ((RawTypeBinding) type2).genericType())
                                && isSameType(type1.enclosingType(), type2.enclosingType());
                        break;
                    case Binding.WILDCARD_TYPE:
                        WildcardBinding wildcard1 = (WildcardBinding) type1;
                        WildcardBinding wildcard2 = (WildcardBinding) type2;
                        result = wildcard1.boundKind == wildcard2.boundKind && isSameType(wildcard1.bound, wildcard2.bound);
                        break;
                    case Binding.TYPE_PARAMETER:
                        // type variables of a generic method are copied for each parameterization of its declaring type
                        TypeVariableBinding variable1 = (TypeVariableBinding) type1;
                        TypeVariableBinding variable2 = (TypeVariableBinding) type2;
                        result = variable1.rank == variable2.rank
                                && variable1.declaringElement instanceof MethodBinding method1
                                && variable2.declaringElement instanceof MethodBinding method2
                                && method1.original() == method2.original()
                                && isSameType(method1.declaringClass, method2.declaringClass);
                        break;
                    default:
                        result = false;
                }
            }
            if (result) {
                this.sameTypes.computeIfAbsent(type1, key -> Collections.newSetFromMap(new IdentityHashMap<>())).add(type2);
            }
            return result;
        }

        private boolean isSameTypes(TypeBinding[] types1, TypeBinding[] types2) {
            int length = types1 == null ? 0 : types1.length;
            if (length != (types2 == null ? 0 : types2.length)) {
                return false;
            }
            for (int i = 0; i < length; i++) {
                if (!isSameType(types1[i], types2[i])) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
        MethodBinding methodBinding1 = (MethodBinding) ((ExecutableTypeImpl) m1)._binding;
        MethodBinding methodBinding2 = (MethodBinding) ((ExecutableTypeImpl) m2)._binding;
        if (!CharOperation.equals(methodBinding1.selector, methodBinding2.selector))
            return false;
        return methodBinding1.areParameterErasuresEqual(methodBinding2) && methodBinding1.areTypeVariableErasuresEqual(methodBinding2);
    }

    /* (non-Javadoc)
     * @return true if t1 is a subtype of t2, or if t1 == t2.
     */
    @Override
    public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    	validateRealTypes(t1, t2);
        if (t1 instanceof NoTypeImpl) {
            if (t2 instanceof NoTypeImpl) {
                return ((NoTypeImpl) t1).getKind() == ((NoTypeImpl) t2).getKind();
            }
            return false;
        } else if (t2 instanceof NoTypeImpl) {
            return false;
        }
        if (!(t1 instanceof TypeMirrorImpl) || !(t2 instanceof TypeMirrorImpl)) {
        	throw new IllegalArgumentException();
        }
        if (t1 == t2) {
            return true;
        }
        Binding b1 = ((TypeMirrorImpl)t1).binding();
        Binding b2 = ((TypeMirrorImpl)t2).binding();
        if (b1 == b2) {
            return true;
        }
        if (!(b1 instanceof TypeBinding) || !(b2 instanceof TypeBinding)) {
            // package, method, import, etc.
        	 throw new IllegalArgumentException();
        }
        if (isSameIntersectionType((TypeBinding) b1, (TypeBinding) b2)) {
            return true;
        }
        if (t2 instanceof IntersectionType intersectionType) {
            // JLS §4.10.2: a subtype of all components is a subtype of the intersection type
            for (TypeMirror bound : intersectionType.getBounds()) {
                if (!isSubtype(t1, bound)) {
                    return false;
                }
            }
            return true;
        }
        if (b1.kind() == Binding.BASE_TYPE || b2.kind() == Binding.BASE_TYPE) {
            if (b1.kind() != b2.kind()) {
                return false;
            }
            else {
                // for primitives, compatibility implies subtype
                return ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
            }
        }
        return ((TypeBinding)b1).isCompatibleWith((TypeBinding)b2);
    }

    @Override
    public PrimitiveType unboxedType(TypeMirror t) {
        if (!(((TypeMirrorImpl)t)._binding instanceof ReferenceBinding)) {
            // Not an unboxable type - could be primitive, array, not a type at all, etc.
            throw new IllegalArgumentException("Given type mirror cannot be unboxed"); //$NON-NLS-1$
        }
        ReferenceBinding boxed = (ReferenceBinding)((TypeMirrorImpl)t)._binding;
        TypeBinding unboxed = this._env.getLookupEnvironment().computeBoxingType(boxed);
        if (unboxed.kind() != Binding.BASE_TYPE) {
            // No boxing conversion was found
            throw new IllegalArgumentException();
        }
        return (PrimitiveType) this._env.getFactory().newTypeMirror(unboxed);
    }

	@Override
	@SuppressWarnings("unchecked")
	public <T extends TypeMirror> T stripAnnotations(T t) {
        if (t instanceof IntersectionType) {
            // an intersection type has no top-level annotations, keep its components as they are
            return t;
        }
        if (t instanceof TypeMirrorImpl typeImpl) {
        	Binding b = typeImpl.binding();
        	if (b instanceof TypeBinding typeBinding) {
        		 return (T) this._env.getFactory().newTypeMirror(typeBinding.unannotated());
        	}
        }
        return t;
    }
}
