package targets.model.pc;

@AnnoY("on F")
public class F<T1> {
	public class FChild {
	}
	
	enum FEnum { FEnum1, FEnum2 }
	
	public interface FChildI {}
	
	protected T1 _fieldT1_protected;
	private T1 _fieldT1_private;
	
	int fieldInt;
	
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