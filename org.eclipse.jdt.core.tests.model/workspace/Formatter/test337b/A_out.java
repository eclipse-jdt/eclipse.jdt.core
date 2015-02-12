package org.eclipse.update.configurator;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import org.eclipse.core.runtime.*;
import org.eclipse.osgi.service.environment.DebugOptions;
import org.eclipse.osgi.service.environment.EnvironmentInfo;
import org.osgi.framework.*;
import org.osgi.service.packageadmin.PackageAdmin;
import org.osgi.service.startlevel.StartLevel;
import org.osgi.util.tracker.ServiceTracker;
public class ConfigurationActivator implements BundleActivator {
	private final static String DEFAULT_CONVERTER = "org.eclipse.update.configurator.migration.PluginConverter"; //$NON-NLS-1$
	public static String PI_CONFIGURATOR = "org.eclipse.update.configurator";
	// debug options
	public static String OPTION_DEBUG = PI_CONFIGURATOR + "/debug";
	public static String OPTION_DEBUG_CONVERTER = PI_CONFIGURATOR
			+ "/converter/debug";
	// debug values
	public static boolean DEBUG = false;
	public static boolean DEBUG_CONVERTER = false;
	private static BundleContext context;
	private ServiceTracker platformTracker;
	private ServiceRegistration configurationFactorySR;
	private String[] allArgs;
	// location used to put the generated manfests
	private String cacheLocation = (String) System.getProperties().get(
			"osgi.manifest.cache"); //PASCAL Need to set this value somewhere (probably from boot)
	private IPluginConverter converter;
	private Set ignore;
	private BundleListener reconcilerListener;
	public void start(BundleContext ctx) throws Exception {
		context = ctx;
		loadOptions();
		if (DEBUG)
			System.out.println("Starting update configurator...");
		computeIgnoredBundles();
		loadConverter();
		obtainArgs();
		installBundles();
	}
	private void computeIgnoredBundles() {
		String ignoreList = System.getProperty("eclipse.ignore",
				"org.eclipse.osgi,org.eclipse.core.boot,org.eclipse.core.runtime.adaptor");
		ignore = new HashSet();
		StringTokenizer tokenizer = new StringTokenizer(ignoreList, ",");
		while (tokenizer.hasMoreTokens())
			ignore.add(tokenizer.nextToken().trim());
	}
	private boolean shouldIgnore(String bundleName) {
		if (ignore == null)
			return false;
		StringTokenizer tokenizer = new StringTokenizer(bundleName, "._");
		String partialName = "";
		while (tokenizer.hasMoreTokens()) {
			partialName += tokenizer.nextToken();
			if (ignore.contains(partialName))
				return true;
			partialName += ".";
		}
		return false;
	}
	private void loadConverter() {
		// TODO look at making this an extension
		String converterClassName = System.getProperty(
				"eclipse.manifestconverter", DEFAULT_CONVERTER);
		if (converterClassName == null)
			return;
		Class converterClass;
		try {
			converterClass = Class.forName(converterClassName);
		} catch (ClassNotFoundException e) {
			return;
		}
		try {
			converter = (IPluginConverter) converterClass.newInstance();
		} catch (InstantiationException e1) {
			return;
		} catch (IllegalAccessException e1) {
			return;
		} catch (ClassCastException cce) {
			return;
		}
	}
	private void obtainArgs() {
		// all this is only to get the application args		
		EnvironmentInfo envInfo = null;
		ServiceReference envInfoSR = context.getServiceReference(
				EnvironmentInfo.class.getName());
		if (envInfoSR != null)
			envInfo = (EnvironmentInfo) context.getService(envInfoSR);
		if (envInfo == null)
			throw new IllegalStateException();
		this.allArgs = envInfo.getAllArgs();
		// we have what we want - release the service
		context.ungetService(envInfoSR);
	}
	public void stop(BundleContext ctx) throws Exception {
		releasePlatform();
		configurationFactorySR.unregister();
	}
	private void releasePlatform() {
		if (platformTracker == null)
			return;
		platformTracker.close();
		platformTracker = null;
	}
	private IPlatform acquirePlatform() {
		if (platformTracker == null) {
			platformTracker = new ServiceTracker(context, IPlatform.class
					.getName(), null);
			platformTracker.open();
		}
		IPlatform result = (IPlatform) platformTracker.getService();
		while (result == null) {
			try {
				platformTracker.waitForService(1000);
				result = (IPlatform) platformTracker.getService();
			} catch (InterruptedException ie) {
			}
		}
		return result;
	}
	private void installBundles() {
		IPlatform platform = acquirePlatform();

		String metaPath = platform.getLocation().append(".metadata").toOSString();
		URL installURL = platform.getInstallURL();
		ServiceReference reference = context.getServiceReference(StartLevel.class.getName());
		StartLevel start = null;
		if (reference != null) 
			start = (StartLevel) context.getService(reference);
		try {
			configurationFactorySR = context.registerService(IPlatformConfigurationFactory.class.getName(), new PlatformConfigurationFactory(), null);
			PlatformConfiguration config = getPlatformConfiguration(allArgs, metaPath, installURL);
			URL[] plugins = config.getPluginPath();
			ArrayList installed = new ArrayList(plugins.length);
			for (int i = 0; i < plugins.length; i++) {
				try {
				String location = plugins[i].toExternalForm();
				checkOrGenerateManifest(location);
				location = "reference:" + location.substring(0, location.lastIndexOf('/'));
				if (!isInstalled(location)) {
					try {
						Bundle target = context.installBundle(location);
						installed.add(target);
						if (start != null)
							start.setBundleStartLevel(target, 4);
					} catch (Exception e) {
						System.err.println("Ignoring bundle at: " + location);
						System.err.println(e.getMessage());
					}
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			context.ungetService(reference);
			refreshPackages((Bundle[]) installed.toArray(new Bundle[installed.size()]));
			if (System.getProperty("eclipse.application") == null || System.getProperty("eclipse.application").equals(PlatformConfiguration.RECONCILER_APP))
				System.setProperty("eclipse.application", config.getApplicationIdentifier());
//			if (config.getApplicationIdentifier().equals(PlatformConfiguration.RECONCILER_APP) ) {
//				reconcilerListener = reconcilerListener();
//				context.addBundleListener(reconcilerListener);
//			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			releasePlatform();
		}
	}
	private BundleListener reconcilerListener() {
		return new BundleListener() {
			public void bundleChanged(BundleEvent event) {
				String buid = event.getBundle().getUniqueId();
				if (event.getType() == BundleEvent.STOPPED && buid != null
						&& buid.equals("org.eclipse.update.core"))
					runPostReconciler();
			}
		};
	}
	private void runPostReconciler() {
		Runnable postReconciler = new Runnable() {
			public void run() {
				try {
					Bundle apprunner = context.getBundle(
							"org.eclipse.core.applicationrunner");
					apprunner.stop();
					context.removeBundleListener(reconcilerListener);
					try {
						PlatformConfiguration.shutdown();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					installBundles();
					apprunner.start();
				} catch (BundleException be) {
					be.printStackTrace();
				}
			}
		};
		new Thread(postReconciler, "Post reconciler").start();
	}
	/**
	 * @param location
	 */
	private void checkOrGenerateManifest(String pluginManifestLocationURL) {
		if (converter == null)
			return;
		String pluginManifestLocation = null;
		try {
			pluginManifestLocation = new URL(pluginManifestLocationURL)
					.getPath();
		} catch (MalformedURLException e) {
			return;
		}
		File pluginDir = new File(pluginManifestLocation).getParentFile();
		if (shouldIgnore(pluginDir.getName()))
			return;
		File manifest = new File(pluginDir, "META-INF/MANIFEST.MF");
		if (manifest.exists())
			return;
		// bail if the install location is not writable and we don't know where else to write to
		if (cacheLocation == null)
			return;
		File generationLocation = new File(cacheLocation, computeFileName(
				pluginDir.getPath()) + ".MF");
		if (generationLocation.exists())
			return;
		if (!converter.convertManifest(pluginDir, generationLocation))
			System.out.println(pluginDir + " manifest generation failed");
	}
	/*
	 * Derives a file name corresponding to a path:
	 * c:\autoexec.bat -> c__autoexec.bat
	 */
	private String computeFileName(String filePath) {
		StringBuffer newName = new StringBuffer(filePath);
		for (int i = 0; i < filePath.length(); i++) {
			char c = newName.charAt(i);
			if (c == ':' || c == '/' || c == '\\')
				newName.setCharAt(i, '_');
		}
		return newName.toString();
	}
	/**
	 * This is a major hack to try to get the reconciler application running. However we should find a way to not run it.
	 * @param args
	 * @param metaPath
	 * @return
	 */
	private PlatformConfiguration getPlatformConfiguration(String[] args,
			String metaPath, URL installURL) {
		try {
			PlatformConfiguration.startup(args, null, null, metaPath,
					installURL);
		} catch (Exception e) {
			if (platformTracker != null) {
				String message = e.getMessage();
				if (message == null)
					message = "";
				IStatus status = new Status(IStatus.ERROR, IPlatform.PI_RUNTIME,
						IStatus.OK, message, e);
				((IPlatform) platformTracker.getService()).getLog(context
						.getBundle()).log(status);
			}
		}
		return PlatformConfiguration.getCurrent();
	}
	/**
	 * Do PackageAdmin.refreshPackages() in a synchronous way.  After installing
	 * all the requested bundles we need to do a refresh and want to ensure that 
	 * everything is done before returning.
	 * @param bundles
	 */
	private void refreshPackages(Bundle[] bundles) {
		if (bundles.length == 0)
			return;
		ServiceReference packageAdminRef = context.getServiceReference(
				PackageAdmin.class.getName());
		PackageAdmin packageAdmin = null;
		if (packageAdminRef != null) {
			packageAdmin = (PackageAdmin) context.getService(packageAdminRef);
			if (packageAdmin == null)
				return;
		}
		// TODO this is such a hack it is silly.  There are still cases for race conditions etc
		// but this should allow for some progress...
		final Object semaphore = new Object();
		FrameworkListener listener = new FrameworkListener() {
			public void frameworkEvent(FrameworkEvent event) {
				if (event.getType() == FrameworkEvent.PACKAGES_REFRESHED)
					synchronized (semaphore) {
						semaphore.notifyAll();
					}
			}
		};
		context.addFrameworkListener(listener);
		packageAdmin.refreshPackages(bundles);
		synchronized (semaphore) {
			try {
				semaphore.wait();
			} catch (InterruptedException e) {
			}
		}
		context.removeFrameworkListener(listener);
		context.ungetService(packageAdminRef);
	}
	private boolean isInstalled(String location) {
		Bundle[] installed = context.getBundles();
		for (int i = 0; i < installed.length; i++) {
			Bundle bundle = installed[i];
			if (location.equalsIgnoreCase(bundle.getLocation()))
				return true;
		}
		return false;
	}
	private void loadOptions() {
		// all this is only to get the application args		
		DebugOptions service = null;
		ServiceReference reference = context.getServiceReference(
				DebugOptions.class.getName());
		if (reference != null)
			service = (DebugOptions) context.getService(reference);
		if (service == null)
			return;
		try {
			DEBUG = service.getBooleanOption(OPTION_DEBUG, false);
			if (!DEBUG)
				return;
			DEBUG_CONVERTER = service.getBooleanOption(OPTION_DEBUG_CONVERTER,
					false);
		} finally {
			// we have what we want - release the service
			context.ungetService(reference);
		}
	}
	public static BundleContext getBundleContext() {
		return context;
	}
}