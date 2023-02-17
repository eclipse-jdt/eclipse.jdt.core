package javax.lang.model.element;

public enum NestingKind {
    TOP_LEVEL,

    MEMBER,

    LOCAL,

    ANONYMOUS;

    public boolean isNested() {
        return this != TOP_LEVEL;
    }
}
