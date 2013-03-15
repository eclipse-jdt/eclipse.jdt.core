public class X {
    int x() {
        for (@Marker int i: new int[3]) {}
        for (final @Marker int i: new int[3]) {}
        for (@Marker final int i: new int[3]) {}
        return 10;
    }
    Zork z;
}
@java.lang.annotation.Target (java.lang.annotation.ElementType.TYPE_PARAMETER)
@interface Marker {}
