/*
 * @see ITypeBinding#getDeclaredMethods()
 */
public IMethodBinding[] getDeclaredMethods() {
	if (this.binding.isClass() || this.binding.isInterface()) {
		ReferenceBinding referenceBinding = (ReferenceBinding) this.binding;
		org.eclipse.jdt.internal.compiler.lookup.MethodBinding[] methods = referenceBinding
				.methods();
		int length = methods.length;
		int removeSyntheticsCounter = 0;
		IMethodBinding[] newMethods = new IMethodBinding[length];
		for (int i = 0; i < length; i++) {
			org.eclipse.jdt.internal.compiler.lookup.MethodBinding methodBinding = methods[i];
			if (!shouldBeRemoved(methodBinding)) {
				newMethods[removeSyntheticsCounter++] = this.resolver
						.getMethodBinding(methodBinding);
			}
		}
		if (removeSyntheticsCounter != length) {
			System.arraycopy(newMethods, 0,
					(newMethods = new IMethodBinding[removeSyntheticsCounter]),
					0, removeSyntheticsCounter);
		}
		return newMethods;
	} else {
		return NO_DECLARED_METHODS;
	}
}
// comment 1
// comment 2
// comment 3
// comment 4
