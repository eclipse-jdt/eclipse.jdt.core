package targets.model.pc;

@AnnoY("on H")
public class H extends G {
	int fieldInt; // hides definition in G
	
	public String methodIAString(int int1)
	{
		return null;
	}

	// hides G.staticMethod and F.staticMethod
	public static void staticMethod()
	{
	}
	
	// different signature; does not hide G.staticMethod
	public static void staticMethod(int int1)
	{
	}

	public class FChild {} // hides definition in F
	public class IFChild {} // hides definition in IF
}