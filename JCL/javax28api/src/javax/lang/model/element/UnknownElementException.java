package javax.lang.model.element;

import javax.lang.model.UnknownEntityException;

public class UnknownElementException extends UnknownEntityException {

    private static final long serialVersionUID = 269L;

    private transient Element element;
    private transient Object parameter;

    public UnknownElementException(Element e, Object p) {
        super("Unknown element: \"" + e + "\"");
        element = e;
        this.parameter = p;
    }

    public Element getUnknownElement() {
        return element;
    }

    public Object getArgument() {
        return parameter;
    }
}
