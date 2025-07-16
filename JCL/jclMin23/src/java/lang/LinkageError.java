package java.lang;

public
class LinkageError extends Error {

    public LinkageError() {
        super();
    }

    public LinkageError(String s) {
        super(s);
    }

    public LinkageError(String s, Throwable cause) {
        super(s, cause);
    }
}
