package javax.lang.model;

public class UnknownEntityException extends RuntimeException {

    private static final long serialVersionUID = 269L;

    protected UnknownEntityException(String message) {
        super(message);
    }
}
