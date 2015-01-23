public int foo(int size, int max) {
	if (size < max) {
		try {
			size = (long) stream.available();
		} catch (IOException e) {
		}
	} else if (size == max) {
		++size;
	} else {
		--size;
	}
	return size;
}