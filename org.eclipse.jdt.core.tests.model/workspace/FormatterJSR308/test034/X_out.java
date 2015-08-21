public class X {
	X x;

	class Y {
	}

	Y y1 = x.new @Marker Y();
	Y y2 = x.new <String>@Marker Y();
}
