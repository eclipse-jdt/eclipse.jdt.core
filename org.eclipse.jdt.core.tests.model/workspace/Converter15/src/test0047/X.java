package test0047;

public enum X {
	TOTO ;

	private static final long[] overflows = { 
        0, // unused
        Long.MAX_VALUE / 1000,
        Long.MAX_VALUE / (1000 * 1000),
        Long.MAX_VALUE / (1000 * 1000 * 1000) 
    };
}