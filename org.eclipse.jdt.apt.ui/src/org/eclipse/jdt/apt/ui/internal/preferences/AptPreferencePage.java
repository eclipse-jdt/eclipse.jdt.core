/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     BEA Systems Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.jdt.apt.ui.internal.AptUIPlugin;
import org.eclipse.jdt.apt.ui.internal.util.IAptHelpContextIds;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/*
 * The page to configure the naming style options.
 */
public class AptPreferencePage extends BasePreferencePage {

	private static final String PREF_ID= "org.eclipse.jdt.apt.ui.preferences.aptPreferences"; //$NON-NLS-1$
	private static final String PROP_ID= "org.eclipse.jdt.apt.ui.propertyPages.aptPreferences"; //$NON-NLS-1$

	public AptPreferencePage() {
		setPreferenceStore(AptUIPlugin.getDefault().getPreferenceStore());

		// only shown for workspace preferences
		setDescription(Messages.AptPreferencePage_preferences);

		// only used when page is shown programatically
		setTitle(Messages.AptPreferencePage_preferencesTitle);
	}

	/*
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		IWorkbenchPreferenceContainer container= (IWorkbenchPreferenceContainer) getContainer();
		setConfigurationBlock(new AptConfigurationBlock(getNewStatusChangedListener(), getProject(), container));

		super.createControl(parent);
	}

	@Override
	protected String getContextHelpId() {
		return IAptHelpContextIds.APTCONFIGURATION_PREFERENCE_PAGE;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPreferencePageID()
	 */
	protected String getPreferencePageID() {
		return PREF_ID;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.PropertyAndPreferencePage#getPropertyPageID()
	 */
	protected String getPropertyPageID() {
		return PROP_ID;
	}



}



