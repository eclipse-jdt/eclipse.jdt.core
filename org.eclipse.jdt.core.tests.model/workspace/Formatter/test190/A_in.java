public class A {
	private void updateAnnotation() {
		while (this.astLengthPtr >= 0) {
			// Starting with the stack top, so get references (eg. Expression) coming from @see declarations
			if (this.astLengthPtr == 2) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.references = new Expression[size];
					for (int i=(size-1); i>=0; i--) {
						this.annotation.references[i] = (Expression) this.astStack[astPtr--];
					}
				}
			}

			// Then continuing with class names (eg. TypeReference) coming from @throw/@exception declarations
			else if (this.astLengthPtr == 1) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.thrownExceptions = new TypeReference[size];
					for (int i=(size-1); i>=0; i--) {
						this.annotation.thrownExceptions[i] = (TypeReference) this.astStack[astPtr--];
					}
				}
			}

			// Finally, finishing with parameters nales (ie. Argument) coming from @param declaration
			else if (this.astLengthPtr == 0) {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					this.annotation.parameters = new AnnotationArgument[size];
					for (int i=(size-1); i>=0; i--) {
						this.annotation.parameters[i] = (AnnotationArgument) this.astStack[astPtr--];
					}
				}
			}

			// Flag all nodes got from other ast length stack pointer values as invalid....
			// TODO: (frederic) To be changed when mixed tags declaration will be accepted 
			else {
				int size = this.astLengthStack[this.astLengthPtr--];
				if (size > 0) {
					for (int i=0; i<size; i++) {
						AstNode node = this.astStack[astPtr--];
						this.sourceParser.problemReporter().annotationUnexpectedTag(node.sourceStart, node.sourceEnd);
					}
				}
			}
		}
	}
}