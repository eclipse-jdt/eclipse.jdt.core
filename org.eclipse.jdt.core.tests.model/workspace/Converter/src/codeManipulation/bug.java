package codeManipulation;

public class bug {
	static private int dummyFct3() {
		return 3;	
	}	
	static private int dummyFct2() {
		return 3;	
	}		
	static private void pipo () {
		int z = bug.dummyFct3(),y=bug.dummyFct2();

	}
}