interface I {
}

interface J {
}

interface K extends @Marker I, @Marker J {
}

interface L {
}

public class X implements @Marker K, @Marker L {
}
