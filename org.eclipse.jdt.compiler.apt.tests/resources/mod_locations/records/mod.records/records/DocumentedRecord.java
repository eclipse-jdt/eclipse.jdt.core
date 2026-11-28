package records;

/**
 * A record with a documented component.
 *
 * @param a the a
 * @param b the b
 */
public record DocumentedRecord(/** The comment of a. */ int a, String b) {
	/** The accessor of b. */
	public String b() {
		return this.b;
	}
}
