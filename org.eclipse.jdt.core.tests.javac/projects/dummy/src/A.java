class A {
	String method(Object element, int columnIndex) {
		return element instanceof String data ?
			switch (columnIndex) {
				case 0 -> data;
				case 1 -> data.toUpperCase();
				default -> "";
			} : "";
  }
}
