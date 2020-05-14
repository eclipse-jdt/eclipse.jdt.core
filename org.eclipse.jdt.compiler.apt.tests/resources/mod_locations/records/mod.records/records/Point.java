package records;
@Deprecated
public record Point(@MyAnnot int comp_, 
		@MyAnnot2 int comp2_, 
		@MyAnnot3 int comp3_,
		@MyAnnot4 int comp4_,
		@MyAnnot5 int comp5_) {
    private static String field1;
    public static double field2;
    protected static Character field3;
	public Point  {}
	public boolean equals(Object o) {
		return false;
	}
	public int hashCode() {
		return -1;
	}
	public String toString() {
		return null;
	}
}