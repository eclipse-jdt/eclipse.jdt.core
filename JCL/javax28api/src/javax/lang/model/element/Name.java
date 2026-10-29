package javax.lang.model.element;

public interface Name extends CharSequence {
    boolean equals(Object obj);

    int hashCode();

    boolean contentEquals(CharSequence cs);
}
