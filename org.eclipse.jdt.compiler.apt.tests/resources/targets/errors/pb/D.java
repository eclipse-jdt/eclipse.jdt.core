package targets.errors.pb;

import target.errors.pa.AnnoZ;

@AnnoZ(
		annoZString = "annoZOnD")
@SuppressWarnings("all")
public class D {
	public enum DEnum { DEnum1, DEnum2, DEnum3 }
	
	@AnnoZ(annoZString = "annoZOnDMethod", annoZint = 31)
	public void methodDvoid(DEnum dEnum1) {
	}
}

