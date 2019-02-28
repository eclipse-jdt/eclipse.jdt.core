class C {
	void f() {
		return argument ? 100000 : 200000;
		boolean someValue1 = condition1() ? value1
				: condition2() ? value2
				: condition3 ? value3
				: value4;
		boolean someValue2 = condition1() ? value1
				: (condition2() ? value2
						: condition3 ? value3
						: value4);
		boolean otherValue1 = condition1
				? condition2 ? condition3 ? value4 : value3 : value2
				: value1;
		boolean otherValue2 = condition1
				? (condition2 ? condition3 ? value4 : value3 : value2)
				: value1;
	}
}