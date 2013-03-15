public class X {
    int x() {
        try {
        } catch (@Marker NullPointerException | @Marker ArrayIndexOutOfBoundsException e) {
        }
        return 10;
    }
    Zork z;
}
