public class A {
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		try {
			IJavaProject javaProject = getJavaProject(configuration);
			if ((javaProject == null) || !javaProject.exists()) {
				abort(PDEPlugin
						.getResourceString("JUnitLaunchConfiguration.error.invalidproject"), null, IJavaLaunchConfigurationConstants.ERR_NOT_A_JAVA_PROJECT); //$NON-NLS-1$
			}
			IType[] testTypes = getTestTypes(configuration, javaProject,
					new SubProgressMonitor(monitor, 1));
			if (testTypes.length == 0) {
				abort(PDEPlugin
						.getResourceString("JUnitLaunchConfiguration.error.notests"), null, IJavaLaunchConfigurationConstants.ERR_UNSPECIFIED_MAIN_TYPE); //$NON-NLS-1$
			}
			monitor.worked(1);

			IVMInstall launcher = LauncherUtils.createLauncher(configuration);
			monitor.worked(1);

			int port = SocketUtil.findFreePort();
			VMRunnerConfiguration runnerConfig = createVMRunner(configuration,
					testTypes, port, mode);
			if (runnerConfig == null) {
				monitor.setCanceled(true);
				return;
			}
			monitor.worked(1);

			launch.setAttribute(ILauncherSettings.CONFIG_LOCATION,
					(configFile == null) ? null : configFile.getParent());

			String workspace = configuration.getAttribute(LOCATION + "0",
					getDefaultWorkspace(configuration));
			LauncherUtils.clearWorkspace(configuration, workspace);

			setDefaultSourceLocator(launch, configuration);
			launch.setAttribute(PORT_ATTR, Integer.toString(port));
			launch.setAttribute(TESTTYPE_ATTR,
					testTypes[0].getHandleIdentifier());
			PDEPlugin.getDefault().getLaunchesListener().manage(launch);
			launcher.getVMRunner(mode).run(runnerConfig, launch, monitor);
			monitor.worked(1);
		} catch (CoreException e) {
			monitor.setCanceled(true);
			throw e;
		}
	}
}