public class X {
    class Y {
    }
    Y y1 = new @Marker X().new @Marker Y();
    Y y2 = new @Marker X().new <String> @Marker Y();
}
