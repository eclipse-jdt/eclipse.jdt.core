public class CompletionNonStaticFieldRelevance {
	void foo() {
		CompletionNonStaticFieldRelevance2 var = null;
		int i = 0 + var.Ii
	}

}
class CompletionNonStaticFieldRelevance2 {
	public static Object Ii0;
	public Object ii1;
}
