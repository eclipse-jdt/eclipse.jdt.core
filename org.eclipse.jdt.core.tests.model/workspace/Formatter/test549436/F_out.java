public class Example {
	String s = """
		aaa

		bbb


		ccc
		""";

	String s2 = """
		 	aaa

			bbb


		ccc""" + Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6,
			7, 8, 9, 0, 1, 2, 3, 4, 5);

	Object[] s3 = { "aaaa", """
		bbb

		ccc


		ddd
		""", 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2,
			3, 4, 5 };

	Object[] s4 = { "aaaa", //
			"""
				bbb

				ccc


				ddd
				""", 1, 2, 3, 4, 5, 6, 7, 8, 9, 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 0,
			1, 2, 3, 4, 5 };

	void foo() {
		Arrays.asList("""
			""", """
			1""", """
			2
			""", //
				"", """



					""", /**/
				"""
					aaa
					            """);

		Object o = new Object("""
			a,b,c,d""".split("""
			,""")) {
			{
				System.out.println("""
					aaaaaaaaaaaaaaa

					bbbbbbbbbbbbbb""");
			}

			String bar(boolean arg) {
				return (arg ? """
					aaaa
					""" : """
					bbb
					""") + (arg
						? "cccccccccc" + "ddddddddddd" + "eeeeeeeeee"
								+ "fffffffffff" + "ggggggggg" + """
									hhhhhhhh"""
						: """
							aaaaaaaaa""" + """
							bbbbbbbbb""" + """
							cccccccccccc""" + """
							ddddddddddddd
							""" + "eeeeeeee" + "fffffffffffff"
								+ "ggggggggggggggggg" + "hhhhhhhhhhhhhhh"
								+ "iiiiiiiiiiiiiiii"
								+ "jjjjjjjjjjjjjjjjjjjjjjj");
			}
		};
	}
}
