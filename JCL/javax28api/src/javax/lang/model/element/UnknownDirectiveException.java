package javax.lang.model.element;

import javax.lang.model.UnknownEntityException;

public class UnknownDirectiveException extends UnknownEntityException {

    private static final long serialVersionUID = 269L;

    private final transient ModuleElement.Directive directive;
    private final transient Object parameter;

    public UnknownDirectiveException(ModuleElement.Directive d, Object p) {
        super("Unknown directive: " + d);
        directive = d;
        parameter = p;
    }

    public ModuleElement.Directive getUnknownDirective() {
        return directive;
    }

    public Object getArgument() {
        return parameter;
    }
}
