package test0244;
import java.util.*;
public class Test {
	void m(){
		try{
		} catch (RuntimeException e){m();}
		catch(Exception e) {}
	}
}
