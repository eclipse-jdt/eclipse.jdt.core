record Range(int lo,int hi,Strign aasofdi,String aoifjasoifdj) implements Interface1, Interface2, Interfacee3 {
	public Range {
		this.lo = lo;
		this.hi = hi;
		if (lo > hi)
			throw new IllegalArgumentException(String.format("(%d,%d)", lo, hi));
	}
}