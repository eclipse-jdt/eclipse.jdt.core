package javax.lang.model.element;

import javax.lang.model.UnknownEntityException;

public class UnknownAnnotationValueException extends UnknownEntityException {

    private static final long serialVersionUID = 269L;

    private transient AnnotationValue av;
    private transient Object parameter;

    public UnknownAnnotationValueException(AnnotationValue av, Object p) {
        super("Unknown annotation value: \"" + av + "\"");
        this.av = av;
        this.parameter = p;
    }

    public AnnotationValue getUnknownAnnotationValue() {
        return av;
    }

    public Object getArgument() {
        return parameter;
    }
}
