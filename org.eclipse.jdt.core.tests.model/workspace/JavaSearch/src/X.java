/* Test case for PR 1GK7K17: ITPJCORE:WIN2000 - search: missing type reference */
public class X{
  static void s(){};
}
class AA{
   AA(){ 
     X.s();   //<< 
   };   
}