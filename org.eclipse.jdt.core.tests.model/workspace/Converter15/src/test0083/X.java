package test0083;

import java.util.List;

public class X<T> {
    List<Integer> list1;
    List<Number> list2;
    List<? extends Number> list3;
    List<T> list4;
}