public class ResolveDuplicateTypeDeclaration5 {
	class Inner {
		class Inner2/*1*/ {}
		class Inner2/*1*/ {}
	}
	class Inner {
		class Inner2/*2*/ {}
		class Inner2/*2*/ {}
	}
}
