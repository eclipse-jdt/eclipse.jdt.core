package e1;

public class Test {
    public static void main(String[] args) {
        for (Team t : Team.values()) {
            int age = t.age();
            String location = location(t).toString();
            t.setRole(t.isManager());
        }
    }

   private enum Location { SNZ, OTT }

    private static Location location(Team t) {
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
