package b77388;
class Test {
	Test(int a, int b) {	}
	void take(Test mc) { }
	void run() {
		take( new Test(1, 2) ); // space in ") )" is in match
	}
}
