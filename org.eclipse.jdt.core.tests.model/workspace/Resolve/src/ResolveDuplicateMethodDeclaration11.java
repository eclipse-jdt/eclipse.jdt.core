public class ResolveDuplicateMethodDeclaration11 {
	class Inner {
		void foo(/*1*/Zork o) {}
		void foo(/*1*/Zork o) {}
	}
	class Inner {
		void foo(/*2*/Zork o) {}
		void foo(/*2*/Zork o) {}
	}
}
