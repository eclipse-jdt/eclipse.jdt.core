package test.tags.param;

interface X02 {
	/**
	 * Description on several lines formatted in only one.
	 * 
	 * @param a
	 *        The first parameter. For an optimum result, this should be an odd
	 *        number between 0 and 100. We may also want to know if the
	 *        formatter is able to handle more than two lines in a tag
	 *        description.
	 * 
	 * @param b
	 *        The second parameter.
	 */
	int foo(int a, int b);
}
