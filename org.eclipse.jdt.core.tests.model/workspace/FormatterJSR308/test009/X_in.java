public class X {
    int x() {
        @Marker int p;
        final @Marker int q;
        @Marker final int r;
        return 10;
    }
    Zork z;
}
@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
@interface Marker {}
