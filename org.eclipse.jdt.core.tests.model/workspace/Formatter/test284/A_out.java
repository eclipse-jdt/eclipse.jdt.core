public class FormaterBug
{
	public static void main(String[] args)
	{
		Object o = null;
		o = null;
		o = o;
		o = null;
		synchronized (o)
		{
			// DO something
		}
		// Why does the code formater indent that lines?
		o = o;
		o = null;
		o = o;
		o = null;
	}
}