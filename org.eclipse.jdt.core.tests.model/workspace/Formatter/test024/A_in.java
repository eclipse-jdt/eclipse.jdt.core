// test024
public class A {
	public	void foo() {
		if (shortCondition) fitOnSameLine();	
		if (this.condition.isQuiteLong()) cannotFitOnSameLineAsIf("some argument", "some other argument"); 
		if (should-split-first) if (should-split-second) if (remainCompact) if (remainCompact) whatever();	
	}
}