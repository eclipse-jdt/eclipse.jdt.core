package targets.model.pb;

import pa.AnnoZ;

@AnnoZ(annoZString = "annoZOnD")
public class D extends AB {
	public enum DEnum { DEnum1, DEnum2, DEnum3 }
	
	@AnnoZ(annoZString = "annoZOnDMethod", annoZint = 31)
	public void methodDvoid(DEnum dEnum1) {}
}
