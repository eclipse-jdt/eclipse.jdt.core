package b;

import org.eclipse.jdt.annotation.NonNull;

import castle.extdata.autosar.v403.org.autosar.schema.r4.IDENTIFIER;

public class User {

	public void test2() {
		IDENTIFIER id = new IDENTIFIER();
		@NonNull String str = id.getValue();
		useIt( str );
	}
	
	public void useIt( @NonNull String str ) {
	}
	
}
