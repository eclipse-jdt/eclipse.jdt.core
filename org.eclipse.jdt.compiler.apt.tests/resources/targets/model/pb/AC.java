package targets.model.pb;

import java.util.*;
import targets.model.pa.IA;

public class AC<T1 extends String & Iterator, T2> implements IC, IA {
	
	private List<String> _fieldListString = new ArrayList<String>();
	
	public String methodIAString(int int1) {
		return _fieldListString.iterator().next();
	}
	
	public T1 methodGetT1(T2 paramT2) {
		return null;
	}
	
	public List<T1> methodGetListT1() {
		return null;
	}
	
	public Map<T1, List<T2>> methodGetMapT1ListT2( Iterator<T2> paramIterT2 ) {
		return null;
	}
	
	public List<? extends T1> methodGetQExtendsT1() {
		return null;
	}
	
	public <T3 extends List<T2>> void methodT3Void(T3 paramT3) {
	}
}
