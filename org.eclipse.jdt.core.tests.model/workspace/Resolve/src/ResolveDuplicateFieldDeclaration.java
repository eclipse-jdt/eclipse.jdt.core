public class ResolveDuplicateFieldDeclaration {
	class Inner {
		int var;/*1*/
		int var;/*1*/
	}
	class Inner {
		int var;/*2*/
		int var;/*2*/
	}
}
