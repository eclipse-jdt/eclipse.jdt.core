package q6;
/* Test case for bug 5069 search: method reference in super missing */
class AQ {
	public void k(){}
}

class AQE extends AQ{
	public void k(){
		super.k();
	}
}
