package test0515;

public class A {
   public void method() {
       if (true) 
          ;   // <--  this one is correct
       else if (true)
          ;  //  <--- this one will visit twice
       else 
          ;   // <---  this one didn't visit
   }
}