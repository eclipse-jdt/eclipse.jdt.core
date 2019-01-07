class C {

	boolean firstIsGreater = 11111111 + 22222222 + 33333333 + 44444444
			+ 55555555 + 66666666 > 1.11111111 * 2.22222222 * 3.33333333
					* 4.44444444 * 5.55555555 * 6.66666666;

	String concatenatedString = "one two three four " + "five six seven eight "
			+ "nine ten eleven twelve";

	int shiftedInteger = 0xCAFEFACE >>> 0x00000001 >>> 0x00000002 << 0x00000003 >>> 0x00000004;

	int bitAritmetic = 0xCAFEFACE |
						0x01010101 &
									0x02020202 ^
									0x03030303 ^
									0x04040404 |
						0x05050505;

	boolean multipleConditions = conditionOne && conditionTwo
			|| conditionThree && conditionFour || conditionFive;

}
