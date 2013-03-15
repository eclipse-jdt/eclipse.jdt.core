public class X {
	int x(@Marker1 int[] @Marker2... p) {
		return 10;
	};

	Zork z;
}

interface Zork {
}

@interface Marker1 {
}

@interface Marker2 {
}
