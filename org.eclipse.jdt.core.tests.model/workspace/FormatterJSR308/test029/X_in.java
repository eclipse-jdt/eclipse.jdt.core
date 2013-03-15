import java.io.Serializable;
import java.util.List;
public class X<T extends Comparable<T> & Serializable> {
	void foo(List<? extends @Marker Comparable<T>> p) {} 
}
