package test.comments.line;

public class X09 {

	void foo() {
		if (true) {
			if (true) {
				if (true) {
					if (true) {
						if (true) {
							if (true) {
								if (true) {
									if (true){
										// if a valid field was found, complain when another is found in an 'immediate' enclosing type (that is, not inherited)
									}
								}
							}
						}
					}
				}
			}
		}
	}
}
