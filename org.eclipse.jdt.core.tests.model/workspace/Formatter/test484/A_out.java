public class A {
	public void launch() {
		try {
			if ((javaProject == null) || !javaProject.exists()) {
				abort(PDEPlugin
						.getResourceString("JUnitLaunchConfiguration.error.invalidproject"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT); //$NON-NLS-1$
			}
		} catch (CoreException e) {
		}
	}
}