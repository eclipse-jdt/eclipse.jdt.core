



package foo.bar.baz;




import java.util.List;
import java.util.Vector;




import org.eclipse.jdt.core.dom.ASTParser;




import java.net.Socket;




public class Another {
}




public class Example {




	public class Nested {
	}




	public static class Pair {




		public String first;




		public String second;
// Between here...




// ...and here are 10 blank lines
	};




	private LinkedList fList;




	public int counter;




	public Example(LinkedList list) {




		fList = list;
		counter = 0;
	}



	public void push(Pair p) {




		fList.add(p);
		++counter;
	}



	public Object pop() {




		--counter;
		return (Pair) fList.getLast();
	}
}
