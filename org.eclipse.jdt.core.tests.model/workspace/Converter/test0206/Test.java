package test0206;
import java.util.*;
public class Test {
	public Test field;
	public int i;

	public int foo() {
		return field.field.field.field.i;
	}	
}