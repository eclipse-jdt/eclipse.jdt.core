package g7.a.def;
import java.util.ArrayList;
import java.util.List;

public class SearchTestImpl<T> implements ISearchTest<T> {
	@Override
	public List<T> getList() {
		return new ArrayList<>();
	}
}
