package e1;
import static e1.T.*;

public class X {
    public static void main(String[] args) {
        for (T t : T.values()) {
            int age = t.age();
            String location = location(t).toString();
            t.setRole(t.isManager());
        }
    }

   private enum Location { SNZ, OTT }

    private static Location location(T t) {
        switch(t) {
          case PHILIPPE:  
          case DAVID:
          case JEROME:
          case FREDERIC:
          	return Location.SNZ;
          case OLIVIER:
          case KENT:
            return Location.OTT;
          default:
          	return null;
        }
    }
}
