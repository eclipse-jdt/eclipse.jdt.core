package java.lang;

public
class NoClassDefFoundError extends LinkageError {

	static final long serialVersionUID = 1L;

    public NoClassDefFoundError() {
        super();
    }

    public NoClassDefFoundError(String s) {
        super(s);
    }
}
