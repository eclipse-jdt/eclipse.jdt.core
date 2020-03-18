package records;
@Deprecated
public record Point(@MyAnnot int comp_) {
	public Point  {
	}
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