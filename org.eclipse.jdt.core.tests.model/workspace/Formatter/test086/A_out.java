public class FormatterProblem
{
	public static boolean isZero(int x)
	{
		if (x==0)
		{
			return true;
		} // toto
		else // here is the comment that the formatter doesn't like 
		{
			return false;
		}
	}
}