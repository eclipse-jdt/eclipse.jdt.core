package javax.lang.model.util;

import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.type.*;
import static javax.lang.model.SourceVersion.*;

@SupportedSourceVersion(RELEASE_6)
public class TypeKindVisitor6<R, P> extends SimpleTypeVisitor6<R, P> {
    @Deprecated(since="9")
    protected TypeKindVisitor6() {
        super(null);
    }


    @Deprecated(since="9")
    protected TypeKindVisitor6(R defaultValue) {
        super(defaultValue);
    }

    @Override
    public R visitPrimitive(PrimitiveType t, P p) {
        TypeKind k = t.getKind();
        switch (k) {
        case BOOLEAN:
            return visitPrimitiveAsBoolean(t, p);

        case BYTE:
            return visitPrimitiveAsByte(t, p);

        case SHORT:
            return visitPrimitiveAsShort(t, p);

        case INT:
            return visitPrimitiveAsInt(t, p);

        case LONG:
            return visitPrimitiveAsLong(t, p);

        case CHAR:
            return visitPrimitiveAsChar(t, p);

        case FLOAT:
            return visitPrimitiveAsFloat(t, p);

        case DOUBLE:
            return visitPrimitiveAsDouble(t, p);

        default:
            throw new AssertionError("Bad kind " + k + " for PrimitiveType" + t);
        }
    }

    public R visitPrimitiveAsBoolean(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsByte(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsShort(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsInt(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsLong(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsChar(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsFloat(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    public R visitPrimitiveAsDouble(PrimitiveType t, P p) {
        return defaultAction(t, p);
    }

    @Override
    public R visitNoType(NoType t, P p) {
        TypeKind k = t.getKind();
        switch (k) {
        case VOID:
            return visitNoTypeAsVoid(t, p);

        case PACKAGE:
            return visitNoTypeAsPackage(t, p);

        case MODULE:
            return visitNoTypeAsModule(t, p);

        case NONE:
            return visitNoTypeAsNone(t, p);

        default:
            throw new AssertionError("Bad kind " + k + " for NoType" + t);
        }
    }

    public R visitNoTypeAsVoid(NoType t, P p) {
        return defaultAction(t, p);
    }

    public R visitNoTypeAsPackage(NoType t, P p) {
        return defaultAction(t, p);
    }

    public R visitNoTypeAsModule(NoType t, P p) {
        return visitUnknown(t, p);
    }

    public R visitNoTypeAsNone(NoType t, P p) {
        return defaultAction(t, p);
    }
}
