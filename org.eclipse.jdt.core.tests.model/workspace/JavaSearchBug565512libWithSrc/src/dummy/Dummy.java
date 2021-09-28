package dummy;

public class Dummy {

	// note that "String" implicit means "java.lang.String":
	public static void main(String[] args) {
	}
//	// TODO: somehow it makes a difference during search if "java.lang.String" is used in the class:
	//bug 576306
	void any(java.lang.String[] a) {
	}
}
