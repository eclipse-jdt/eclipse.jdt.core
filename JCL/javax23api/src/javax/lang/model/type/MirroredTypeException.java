package javax.lang.model.type;

import java.io.ObjectInputStream;
import java.io.IOException;
import javax.lang.model.element.Element;


public class MirroredTypeException extends MirroredTypesException {

    private static final long serialVersionUID = 269;

    private transient TypeMirror type;          // cannot be serialized

    public MirroredTypeException(TypeMirror type) {
        super("Attempt to access Class object for TypeMirror " + type.toString(), type);
        this.type = type;
    }

    public TypeMirror getTypeMirror() {
        return type;
    }

    private void readObject(ObjectInputStream s)
        throws IOException, ClassNotFoundException {
        s.defaultReadObject();
        type = null;
        types = null;
    }
}
