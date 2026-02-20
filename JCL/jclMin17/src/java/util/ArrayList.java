package java.util;

public class ArrayList<T> implements List<T>, java.io.Serializable
{
    public ArrayList(int initialCapacity) { }

    public ArrayList() { }

    public ArrayList(Collection<? extends T> c) { }

    public Iterator<T> iterator() { return null; }
    public int size() { return 0; }
    public T get(int index) { return null; }
    public boolean addAll(Collection<? extends T> c) { return false; }
    public T[] toArray(T[] a) { return a; }
    
    
    
    
    
    
//    public void trimToSize() { }
//
//    public void ensureCapacity(int minCapacity) { }
//
//    public int size() { return 0; }
//
//    public boolean isEmpty() { return true; }
//
//    public boolean contains(Object o) { return false; }
//
//    public int indexOf(Object o) { return -1; }
//
//    public int lastIndexOf(Object o) { return -1; }
//
//    public Object clone() { return null; }
//
//    public Object[] toArray() { return new Object[0]; }
//
//    @SuppressWarnings("unchecked")
//    public <T> T[] toArray(T[] a) { return a; }
//
//    public E get(int index) { return null; }
//
//    public E set(int index, E element) { return null; }
//
//    public boolean add(E e) { return false; }
//
//    public void add(int index, E element) { }
//
//    public E remove(int index) { return null; }
//
//    public boolean equals(Object o) { return this == o; }
//
//    public int hashCode() { return 0; }
//
//    public boolean remove(Object o) { return false; }
//
//    public void clear() { }
//
//    public boolean addAll(Collection<? extends E> c) { return false; }
//
//    public boolean addAll(int index, Collection<? extends E> c) { return false; }
//
//    public boolean removeAll(Collection<?> c) { return false; }
//
//    public boolean retainAll(Collection<?> c) { return false; }
//
//    public List<T> subList(int fromIndex, int toIndex) { return null; }
//
//    public ListIterator<T> listIterator(int index) { return null; }
//
//    public ListIterator<T> listIterator() { return null; }
//
//    public Iterator<T> iterator() { return null; }
//
//    @Override
//    public void forEach(Consumer<? super E> action) { }
//
//    @Override
//    public Spliterator<T> spliterator() { return null; }
//
//    @Override
//    public boolean removeIf(Predicate<? super E> filter) { return false; }
//
//    @Override
//    public void replaceAll(UnaryOperator<T> operator) { }
//
//    @Override
//    @SuppressWarnings("unchecked")
//    public void sort(Comparator<? super E> c) { }
//
//    public void checkInvariants() { }
}