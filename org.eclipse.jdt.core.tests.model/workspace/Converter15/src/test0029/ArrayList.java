 interface Predicate<T> {
 	  boolean is(T t);
 	 }
 	 interface List<T> {
 	  List<T> select(Predicate<T> p);
 	 }
 	 class ArrayList<T> implements List<T>, Iterable<T> {
 	  public List<T> select(Predicate<T> p) {
 	   ArrayList<T> result = new ArrayList<T>();
 	   for (T t : this) {
 	    if (p.is(t)) result.add(t);
 	   }
 	   return result;
 	  }
 	 }