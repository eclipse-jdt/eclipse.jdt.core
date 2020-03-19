/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.PlatformUI;

/**
 * Base class for APT preference and property pages.
 */
public abstract class BasePreferencePage extends PropertyAndPreferencePage {
	private BaseConfigurationBlock fConfigurationBlock;

	protected Control createPreferenceContent(Composite composite) {
		return getConfigurationBlock().createPreferenceContent(composite);
	}

	@Override
	public void createControl(Composite parent) {
		super.createControl(parent);
		String contextId = getContextHelpId();
		if (contextId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(getControl(), contextId);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	public void dispose() {
		if (getConfigurationBlock() != null) {
			getConfigurationBlock().dispose();
		}
		super.dispose();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#enableProjectSpecificSettings(boolean)
	 */
	protected void enableProjectSpecificSettings(boolean useProjectSpecificSettings) {
		if (getConfigurationBlock() != null) {
			getConfigurationBlock().useProjectSpecificSettings(useProjectSpecificSettings);
		}
		super.enableProjectSpecificSettings(useProjectSpecificSettings);
	}

	protected BaseConfigurationBlock getConfigurationBlock() {
		return fConfigurationBlock;
	}

	/**
	 * Derived classes should override by returning a string that refers
	 * to a context topic entry in docs/contexts_APT.xml.  The default
	 * implementation returns null, which causes context help to be disabled.
	 */
	protected String getContextHelpId() {
		return null;
	}

	protected boolean hasProjectSpecificOptions(IProject project) {
		// Workaround for bug 106111 / 111144:
		// See BaseConfigurationBlock.hasProjectSpecificOptionsNoCache() for details.
		return getConfigurationBlock().hasProjectSpecificOptionsNoCache(project);
	}

	/*
	 * See bug 136498: don't show workspace preferences.
	 */
	@Override
	protected boolean offerLink() {
		return false;
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performApply()
	 */
	public void performApply() {
		if (getConfigurationBlock() != null) {
			getConfigurationBlock().performApply();
		}
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performDefaults()
	 */
	protected void performDefaults() {
		super.performDefaults();
		if (getConfigurationBlock() != null) {
			getConfigurationBlock().performDefaults();
		}
	}

	/*
	 * @see org.eclipse.jface.preference.IPreferencePage#performOk()
	 */
	public boolean performOk() {
		if (getConfigurationBlock() != null && !getConfigurationBlock().performOk()) {
			return false;
		}
		return super.performOk();
	}

	protected void setConfigurationBlock(BaseConfigurationBlock configurationBlock) {
		fConfigurationBlock = configurationBlock;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#setElement(org.eclipse.core.runtime.IAdaptable)
	 */
	public void setElement(IAdaptable element) {
		super.setElement(element);
		setDescription(null); // no description for property page
	}
}
