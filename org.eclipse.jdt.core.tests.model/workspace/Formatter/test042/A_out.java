class F {
	{
		if (size < currentSize) {
			try {
				size = (long) inStream.available();
			} catch (IOException e) {
			}
		} else if (size == currentSize) {
			++size;
		} else {
			--size;
		}
	}
}