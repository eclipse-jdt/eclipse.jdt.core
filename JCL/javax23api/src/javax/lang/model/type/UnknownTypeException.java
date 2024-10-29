package javax.lang.model.type;

import javax.lang.model.UnknownEntityException;

public class UnknownTypeException extends UnknownEntityException {

    private static final long serialVersionUID = 269L;

    private transient TypeMirror type;
    private transient Object parameter;

    public UnknownTypeException(TypeMirror t, Object p) {
        super("Unknown type: \"" + t + "\"");
        type = t;
        this.parameter = p;
    }

    public TypeMirror getUnknownType() {
        return type;
    }

    public Object getArgument() {
        return parameter;
    }
}
