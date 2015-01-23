if (JavaModelManager.CP_RESOLVE_VERBOSE) {
	System.out.println("CPContainer SET  - setting container: [" + containerPath //$NON-NLS-1$
			+ "] for projects: {" //$NON-NLS-1$
			+ (org.eclipse.jdt.internal.compiler.util.Util.toString(
					affectedProjects,
					new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
						public String displayString(Object o) {
							return ((IJavaProject) o).getElementName();
						}
					}))
			+ "} with values: " //$NON-NLS-1$
			+ (org.eclipse.jdt.internal.compiler.util.Util.toString(
					respectiveContainers,
					new org.eclipse.jdt.internal.compiler.util.Util.Displayable() {
						public String displayString(Object o) {
							return ((IClasspathContainer) o).getDescription();
						}
					})));
}