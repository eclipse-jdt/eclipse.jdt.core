package java.lang;

public class ClassNotFoundException extends ReflectiveOperationException {

    public ClassNotFoundException() {
        super((Throwable)null);  // Disallow initCause
    }

    public ClassNotFoundException(String s) {
        super(s, null);  //  Disallow initCause
    }

    public ClassNotFoundException(String s, Throwable ex) {
        super(s, ex);  //  Disallow initCause
    }

    public Throwable getException() {
        return getCause();
    }
}