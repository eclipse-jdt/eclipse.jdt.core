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
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.AptPlugin;
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
		return getKey(AptPlugin.PLUGIN_ID, name);
	}

	/**
	 * Provide the strings needed to ask the user whether to rebuild.
	 * Derived classes can override this to change the strings, or to
	 * return null, in which case the dialog will not be shown and the
	 * rebuild will not be triggered.
	 * @param workspaceSettings true if workspace settings have changed,
	 * false if only project-specific settings have changed.
	 * @return an array whose first entry is the dialog title, and whose 
	 * second entry is a query asking the user whether to rebuild.
	 */
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		String[] strings= new String[2];
		strings[0] = Messages.BaseConfigurationBlock_settingsChanged;
		if (workspaceSettings) {
			strings[1]= Messages.BaseConfigurationBlock_fullRebuildRequired;
		}
		else {
			strings[1]= Messages.BaseConfigurationBlock_rebuildRequired;
		}
		return strings;
	}

	public BaseConfigurationBlock(IStatusChangeListener context, IProject project, Key[] keys, IWorkbenchPreferenceContainer container) {
		super(context, project, keys, container);
	}
	
	/* TODO: temporary fix for Bugzilla 105623.  When OptionsConfigurationBlock#performDefaults()
	 * is fixed, remove this overriding method. */
	@Override
	public void performDefaults() {
		IScopeContext defaultScope= (fProject == null) ? new DefaultScope() : new InstanceScope();
		for (int i= 0; i < fAllKeys.length; i++) {
			Key curr= fAllKeys[i];
			String defValue= curr.getStoredValue(defaultScope, null);
			setValue(curr, defValue);
		}
		
		settingsUpdated();
		updateControls();
		validateSettings(null, null, null);
	}


	/* Increase visibility from declaration in superclass */
	@Override
	protected abstract Control createContents(Composite parent);

	protected abstract void updateModel(DialogField field);
	
	/* (non-javadoc)
	 * Update fields and validate.
	 * @param changedKey Key that changed, or null, if all changed.
	 */	
	protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);

	/**
	 * TODO: this method is a workaround for Bugzilla 111144 and 106111.  When
	 * 111144 is fixed, remove this method and call hasProjectSpecificOptions() 
	 * instead.  The difference is that this one does not cause project prefs nodes
	 * to be cached in the WorkingCopyManager.
	 * @return true if the project has project-specific options.
	 */
	public boolean hasProjectSpecificOptionsNoCache(IProject project) {
		if (project != null) {
			IScopeContext projectContext= new ProjectScope(project);
			Key[] allKeys= fAllKeys;
			for (int i= 0; i < allKeys.length; i++) {
				if (allKeys[i].getStoredValue(projectContext, null) != null) {
					return true;
				}
			}
		}
		return false;
	}
}
