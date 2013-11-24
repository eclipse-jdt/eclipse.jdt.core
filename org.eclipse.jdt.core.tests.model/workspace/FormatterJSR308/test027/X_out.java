public class X {
	public static void main(String[] args) {
		X[] x = new @Marker X @Marker [5];
		X[] x2 = new @Marker X @Marker [] { null };
		Zork z;
	}
}
