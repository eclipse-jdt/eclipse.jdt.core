package e1;
public enum T {
	PHILIPPE(37) {
		public boolean isManager() {
			return true;
		}
	},
	DAVID(27),
	JEROME(33),
	OLIVIER(35),
	KENT(40),
	FREDERIC;

   public enum Role { MANAGER, DEVELOPPER }

   int age;
	Role role;

	T() {}
	T(int age) {
		this.age = age;
	}
	public int age() { return this.age; }
	public boolean isManager() { return false; }
	void setRole(boolean mgr) {
		this.role = mgr ? Role.MANAGER : Role.DEVELOPPER;
	}
}
