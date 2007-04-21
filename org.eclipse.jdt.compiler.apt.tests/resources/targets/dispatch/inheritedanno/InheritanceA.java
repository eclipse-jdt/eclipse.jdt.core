@InheritedAnno
@NotInheritedAnno
public class InheritanceA {
	@InheritedAnno
	@NotInheritedAnno
	public InheritanceA() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public InheritanceA(int i) {}
	
	// Not a constructor: has a return value
	@InheritedAnno
	@NotInheritedAnno
	public void InheritanceA() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public class AChild {}
	
	public class ANotAnnotated {}
	
	@InheritedAnno
	@NotInheritedAnno
	public interface AIntf {}
	
	@InheritedAnno
	@NotInheritedAnno
	public void foo() {}
	
	@InheritedAnno
	@NotInheritedAnno
	public int i;
	
	@InheritedAnno
	@NotInheritedAnno
	public enum AEnum { A, B }
}

class InheritanceB extends InheritanceA {
	public class BChild extends AChild {}
	
	public class BNotAnnotated extends ANotAnnotated {}
	
	public interface BIntf extends AIntf {}
	
	public void foo() {}
	
	public int i;
}