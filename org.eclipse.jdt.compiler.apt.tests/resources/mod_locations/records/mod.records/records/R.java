package records;
import java.lang.annotation.Repeatable;
record R(@Marker @Marker int a, int j) {
	public R(int a, int j) {
    this.a = a;
    this.j = j;
  }
}
@interface Markers {
	Marker[] value(); 
}
@Repeatable (Markers.class)
@interface Marker{
}

