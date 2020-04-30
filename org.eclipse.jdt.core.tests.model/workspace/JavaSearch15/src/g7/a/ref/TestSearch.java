package g7.a.ref;
import g7.a.def.ISearchTest;
import g7.a.def.SearchTestImpl;

public class TestSearch {
	public static <T> ISearchTest<T> getSearchTestImpl() {
		return new SearchTestImpl<>();
	}
	public static ISearchTest<Integer> getSearchTestImpl2() {
		return new SearchTestImpl<>();
	}
}