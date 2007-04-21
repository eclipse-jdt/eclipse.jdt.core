@InheritedAnno
@NotInheritedAnno
public class InheritanceA {
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