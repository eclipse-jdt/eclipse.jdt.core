package javax.lang.model.util;

import javax.lang.model.element.*;
import static javax.lang.model.element.ElementKind.*;
import javax.annotation.processing.SupportedSourceVersion;
import static javax.lang.model.SourceVersion.*;
import javax.lang.model.SourceVersion;


@SupportedSourceVersion(RELEASE_6)
public class ElementKindVisitor6<R, P>
                  extends SimpleElementVisitor6<R, P> {
    @Deprecated(since="9")
    protected ElementKindVisitor6() {
        super(null);
    }

    @Deprecated(since="9")
    protected ElementKindVisitor6(R defaultValue) {
        super(defaultValue);
    }

    @Override
    public R visitPackage(PackageElement e, P p) {
        assert e.getKind() == PACKAGE: "Bad kind on PackageElement";
        return defaultAction(e, p);
    }

    @Override
    public R visitType(TypeElement e, P p) {
        ElementKind k = e.getKind();
        switch(k) {
        case ANNOTATION_TYPE:
            return visitTypeAsAnnotationType(e, p);

        case CLASS:
            return visitTypeAsClass(e, p);

        case ENUM:
            return visitTypeAsEnum(e, p);

        case INTERFACE:
            return visitTypeAsInterface(e, p);

        case RECORD:
            return visitTypeAsRecord(e, p);

        default:
            throw new AssertionError("Bad kind " + k + " for TypeElement" + e);
        }
    }

    public R visitTypeAsAnnotationType(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitTypeAsClass(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitTypeAsEnum(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitTypeAsInterface(TypeElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitTypeAsRecord(TypeElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override
    public R visitVariable(VariableElement e, P p) {
        ElementKind k = e.getKind();
        switch(k) {
        case ENUM_CONSTANT:
            return visitVariableAsEnumConstant(e, p);

        case EXCEPTION_PARAMETER:
            return visitVariableAsExceptionParameter(e, p);

        case FIELD:
            return visitVariableAsField(e, p);

        case LOCAL_VARIABLE:
            return visitVariableAsLocalVariable(e, p);

        case PARAMETER:
            return visitVariableAsParameter(e, p);

        case RESOURCE_VARIABLE:
            return visitVariableAsResourceVariable(e, p);

        case BINDING_VARIABLE:
            return visitVariableAsBindingVariable(e, p);

        default:
            throw new AssertionError("Bad kind " + k + " for VariableElement" + e);
        }
    }

    public R visitVariableAsEnumConstant(VariableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariableAsExceptionParameter(VariableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariableAsField(VariableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariableAsLocalVariable(VariableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariableAsParameter(VariableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitVariableAsResourceVariable(VariableElement e, P p) {
        return visitUnknown(e, p);
    }

    public R visitVariableAsBindingVariable(VariableElement e, P p) {
        return visitUnknown(e, p);
    }

    @Override
    public R visitExecutable(ExecutableElement e, P p) {
        ElementKind k = e.getKind();
        switch(k) {
        case CONSTRUCTOR:
            return visitExecutableAsConstructor(e, p);

        case INSTANCE_INIT:
            return visitExecutableAsInstanceInit(e, p);

        case METHOD:
            return visitExecutableAsMethod(e, p);

        case STATIC_INIT:
            return visitExecutableAsStaticInit(e, p);

        default:
            throw new AssertionError("Bad kind " + k + " for ExecutableElement" + e);
        }
    }

    public R visitExecutableAsConstructor(ExecutableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitExecutableAsInstanceInit(ExecutableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitExecutableAsMethod(ExecutableElement e, P p) {
        return defaultAction(e, p);
    }

    public R visitExecutableAsStaticInit(ExecutableElement e, P p) {
        return defaultAction(e, p);
    }

    @Override
    public R visitTypeParameter(TypeParameterElement e, P p) {
        assert e.getKind() == TYPE_PARAMETER: "Bad kind on TypeParameterElement";
        return defaultAction(e, p);
    }
}
