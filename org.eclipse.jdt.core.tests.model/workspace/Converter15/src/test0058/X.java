package test0058;

@Jpf.Controller(
	    catches={
	       @Jpf.Catch(type=java.lang.Exception.class, method="handleException"),
	       @Jpf.Catch(type=PageFlowException.class, method="handlePageFlowException")
	    }
	)
	public class X {}