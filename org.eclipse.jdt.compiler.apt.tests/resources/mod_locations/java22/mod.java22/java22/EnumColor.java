package java22;
@Deprecated
public enum EnumColor {
	BLUE() {
		public boolean foo() {
			return true;
		}
	},
	RED() {
		public boolean hasOptionalBody() {
			return true;
		}
	}

}