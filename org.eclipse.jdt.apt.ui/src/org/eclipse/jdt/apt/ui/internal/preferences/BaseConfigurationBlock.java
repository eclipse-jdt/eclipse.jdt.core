/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   {INITIAL_AUTHOR} - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * The ConfigurationBlock hierarchy is used to organize controls and keys 
 * within a property/preference page.  The implementor derives from this
 * class and creates dialog controls, layout, and response code.
 */
public abstract class BaseConfigurationBlock extends OptionsConfigurationBlock {

	protected class UpdateAdapter implements IDialogFieldListener {
		
		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
		}
	}
	
	protected static Key getAptCoreKey(String name) {
		return getKey("org.eclipse.jdt.apt.core", name);
	}
	
	protected abstract String[] getFullBuildDialogStrings(boolean workspaceSettings);

	/*
	 * Parent class hides this method; re-expose it here. 
	 */
	protected abstract Control createContents(Composite parent);

	public BaseConfigurationBlock(IStatusChangeListener context, IProject project, Key[] keys, IWorkbenchPreferenceContainer container) {
		super(context, project, keys, container);
	}
	
	protected abstract void updateModel(DialogField field);

	/* (non-javadoc)
	 * Update fields and validate.
	 * @param changedKey Key that changed, or null, if all changed.
	 */	
	protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);
}
