public class IndentationSwitchCaseBug {

	public static void IBreakIndentation() {
		// Runnable with correct indentation
		new Runnable() {
			public void run() {
				// Do nothing
			}
		};
	
		int i = 5;
		
		switch (i) {
		case 0:
			// Indentation works right here
			break;
		case 1:
			// Runnable with bugged indentation due to case
			new Runnable() {
			public void run() {
				// Do nothing
			}
		};
		// You can see the indentation gets bugged when a runnable
		// is used inside a switch-case statemente
		
		break;
		}

		i = 7;		
		System.out.println(i);
	}
}
