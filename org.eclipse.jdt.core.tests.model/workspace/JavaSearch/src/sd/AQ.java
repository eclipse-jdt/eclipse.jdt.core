package sd;

/* Test case for PR 1GKCH3N: ITPJCORE:WIN2000 - search: method refs - super call not found */
public class AQ{
	public void k(){}
}

class AQE extends AQ{
	public void k(){
		super.k();
	}
}