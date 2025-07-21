package javax.lang.model.element;

public enum Modifier {


    PUBLIC,
    PROTECTED,
    PRIVATE,
    ABSTRACT,
    DEFAULT,
    STATIC,

    SEALED,

    NON_SEALED {
        public String toString() {
            return "non-sealed";
        }
    },
    FINAL,
    TRANSIENT,
    VOLATILE,
    SYNCHRONIZED,
    NATIVE,
    STRICTFP;

    @Override
    public String toString() {
        return name().toLowerCase(java.util.Locale.US);
    }
}
