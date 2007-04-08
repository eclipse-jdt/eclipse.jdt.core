package targets.model.pc;

@SuppressWarnings("deprecation")
@Deprecated
public class Deprecation {
	@Deprecated
	public class deprecatedClass {}
	
	@Deprecated
	public enum deprecatedEnum { Val1 }
	
	@Deprecated
	public interface deprecatedInterface {}
	
	@Deprecated
	public String deprecatedField;
	
	@Deprecated
	void deprecatedMethod() {}
	
	public class nonDeprecatedClass {}
	
	public enum nonDeprecatedEnum { Val1 }
	
	public interface nonDeprecatedInterface {}
	
	public String nonDeprecatedField;
	
	void nonDeprecatedMethod() {}
}