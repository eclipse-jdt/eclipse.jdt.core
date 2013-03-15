public class X<E> {
	class Y {
		E e;

		E getOtherElement(Object other) {
			if (!(other instanceof @Marker X<?>.Y)) {
			}
			;
			return null;
		}
	}
}
