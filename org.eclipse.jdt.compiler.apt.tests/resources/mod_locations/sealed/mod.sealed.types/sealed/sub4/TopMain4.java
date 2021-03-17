package sealed.sub4;
public class TopMain4 {
    public static int ZZ;
    public void ZZ() {}
    non-sealed interface ZZ extends TopMain4Test {}
}

sealed interface TopMain4Test<A> permits TopMain4.ZZ {
    public default int fun() {
        return 1;
    }
}
