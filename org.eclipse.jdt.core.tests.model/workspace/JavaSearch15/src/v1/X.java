package v1;
class X {
	void vargs(int a, int b) {}
	void vargs(int a, int... args) {}
	void vargs(String... args) {}
	void vargs(String str, boolean... args) {}
	void bar() {
		vargs(1, 2);
		vargs(1, 2, 3);
		vargs(1, 2, 3, 4, 5, 6);
		vargs("x", "a","'b", "c");
		vargs("x", false, true);
    }
}
