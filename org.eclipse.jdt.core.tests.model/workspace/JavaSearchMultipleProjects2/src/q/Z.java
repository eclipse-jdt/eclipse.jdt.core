package q;
import p.X;
import p.Y;
public class Z {
  X onlyHereForTheImport = null;
  Y alsoOnlyHereForTheImport = null;
  void foo(){
    p.X x = (p.X)null;
    p.Y y = (p.Y)null;
  }
}