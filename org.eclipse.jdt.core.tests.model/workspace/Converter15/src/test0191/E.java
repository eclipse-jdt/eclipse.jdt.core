package test0191;

import java.io.*;
import java.util.*;

public class E<T, U extends Number> {
    boolean bool= false;
    char c= 0;
    byte b= 0;
    short s= 0;
    int i= 0;
    long l= 0;
    float f= 0;
    double d= 0;

    Boolean bool_class= null;
    Character c_class= null;
    Byte b_class= null;
    Short s_class= null;
    Integer i_class= null;
    Long l_class= null;
    Float f_class= null;
    Double d_class= null;

    Object object= null;
    Vector vector= null;
    Socket socket= null;
    Cloneable cloneable= null;
    Collection collection= null;
    Serializable serializable= null;
    Object[] objectArr= null;
    int[] int_arr= null;
    long[] long_arr= null;
    Vector[] vector_arr= null;
    Collection[] collection_arr= null;
    Object[][] objectArrArr= null;
    Collection[][] collection_arrarr= null;
    Vector[][] vector_arrarr= null;

    Collection<String> collection_string= null;
    Collection<Object> collection_object= null;
    Collection<Number> collection_number= null;
    Collection<Integer> collection_integer= null;
    Collection<? extends Number> collection_upper_number= null;
    Collection<? super Number> collection_lower_number= null;
    Vector<Object> vector_object= null;
    Vector<Number> vector_number= null;
    Vector<Integer> vector_integer= null;
    Vector<? extends Number> vector_upper_number= null;
    Vector<? super Number> vector_lower_number= null;
    Vector<? extends Exception> vector_upper_exception= null;
    Vector<? super Exception> vector_lower_exception= null;

    T t= null;
    U u= null;
    Vector<T> vector_t= null;
    Vector<U> vector_u= null;
    Vector<? extends T> vector_upper_t= null;
    Vector<? extends U> vector_upper_u= null;
    Vector<? super T> vector_lower_t= null;
    Vector<? super U> vector_lower_u= null;
}