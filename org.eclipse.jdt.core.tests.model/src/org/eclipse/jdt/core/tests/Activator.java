/*******************************************************************************
 * Copyright (c) 2014, 2015 Stephan Herrmann and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Stephan Herrmann - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Plugin;
import org.eclipse.core.runtime.Status;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

@SuppressWarnings({"rawtypes", "unchecked"})
public class Activator extends Plugin {

	private static final String PLUGIN_ID = "org.eclipse.jdt.core.tests.model";

	/**
	 * @deprecated uses deprecated class PackageAdmin.
	 */
	static org.osgi.service.packageadmin.PackageAdmin packageAdmin = null;

	static Plugin instance;


	@SuppressWarnings("deprecation")
	public void start(BundleContext context) throws Exception {

		ServiceReference ref= context.getServiceReference(org.osgi.service.packageadmin.PackageAdmin.class.getName());
		if (ref!=null)
			packageAdmin = (org.osgi.service.packageadmin.PackageAdmin)context.getService(ref);
		else
			getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Failed to load PackageAdmin service. Will not be able to access bundles org.eclipse.jdt.annotation."));

		instance = this;
	}

	public void stop(BundleContext context) throws Exception {
		// nothing
	}

	/**
	 * Make the PackageAdmin service accessible to tests.
	 *
	 * @deprecated uses deprecated class PackageAdmin.
	 */
	public static org.osgi.service.packageadmin.PackageAdmin getPackageAdmin() {
		return packageAdmin;
	}

	public static Plugin getInstance() {
		return instance;
	}
}
