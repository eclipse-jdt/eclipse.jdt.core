public class SomeClass {
	public void classifySetTwo(final Set<?> objects, final int value,
	        final long other, final int device) {
		for (final Object element : objects) {
			System.out.println(element.getClass().getName());
		}
	}
}
