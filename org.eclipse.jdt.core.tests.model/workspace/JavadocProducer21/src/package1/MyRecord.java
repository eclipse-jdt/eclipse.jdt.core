package package1;
/**
 * Javadoc for MyRecord
 *
 * @param one param1
 * @param two param2
 */
public record MyRecord(String one, String two) {

	/**
	 * Javadoc for one
	 * @return something
	 */
	public String one() {
		return one;
	}
}