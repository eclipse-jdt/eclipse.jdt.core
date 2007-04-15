package targets.model.pc;

/**
 * Javadoc on element F
 * @param <T1> a type parameter
 */
@AnnoY("on F")
public class F<T1> {
	/**
	 * Javadoc on nested element FChild
	 */
	public class FChild {
	}
	
	/**
	 * Javadoc on nested enum FEnum
	 * Two lines long
	 */
	enum FEnum { FEnum1, FEnum2 }

	/**
	 * Javadoc on nested interface FChildI
	 */
	public interface FChildI {}
	
	/** Javadoc on field _fieldT1_protected, inline format */
	protected T1 _fieldT1_protected;
	private T1 _fieldT1_private;
	
	int fieldInt;
	
	/**
	 * Javadoc on F.method_T1
	 */
	@AnnoY("on F.method_T1")
	T1 method_T1(T1 param1) 
	{
		return null;
	}
	
	String method_String(T1 param1)
	{
		_fieldT1_private = param1;
		return _fieldT1_private.toString();
	}

	@SuppressWarnings("deprecation")
	@Deprecated
	void deprecatedMethod()
	{
	}
}