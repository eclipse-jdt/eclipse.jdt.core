package sealed;
@Deprecated
non-sealed class SealedExample implements SealedI1 {
  public static void main(String[] args){
     System.out.println(0);
  }
}
sealed interface SealedI1 permits SealedExample, NonSealed1 {}
non-sealed class NonSealed1 implements SealedI1 {}