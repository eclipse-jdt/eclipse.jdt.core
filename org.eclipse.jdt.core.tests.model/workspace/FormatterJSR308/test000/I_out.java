public interface I<U extends J<? extends I<U>>> {
}

interface J<T extends I<? extends J<T>>> {
}

class CI<U extends CJ<T, U> & @Marker J<@Marker T>, T extends CI<U, T> & @Marker I<U>>
		implements I<U> {
}

class CJ<T extends CI<U, T> & @Marker I<@Marker U>, U extends CJ<T, U> & J<T>>
		implements J<T> {
}
