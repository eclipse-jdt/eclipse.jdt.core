/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import java.io.IOException;
import java.util.*;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.FactoryContainer;
import org.eclipse.jdt.apt.core.util.FactoryPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.*;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Data and controls for the Java Annotation Factory Path preference page.
 */
public class FactoryPathConfigurationBlock extends BaseConfigurationBlock {

	private static final int IDX_UP= 0;
	private static final int IDX_DOWN= 1;
	//
	private static final int IDX_ADDJAR= 3;
	private static final int IDX_ADDEXTJAR= 4;
	private static final int IDX_ADDVAR= 5;
	private static final int IDX_REMOVE= 6;
	//
	private static final int IDX_ENABLEALL= 8;
	private static final int IDX_DISABLEALL= 9;

	private final static String[] buttonLabels = { 
		Messages.FactoryPathConfigurationBlock_up,
		Messages.FactoryPathConfigurationBlock_down,
		null,                    // 2
		Messages.FactoryPathConfigurationBlock_addJars,
		Messages.FactoryPathConfigurationBlock_addExternalJars,
		Messages.FactoryPathConfigurationBlock_addVariable,
		Messages.FactoryPathConfigurationBlock_remove,
		null,                    // 7
		Messages.FactoryPathConfigurationBlock_enableAll,
		Messages.FactoryPathConfigurationBlock_disableAll
	};

	private PixelConverter fPixelConverter;
	private Composite fBlockControl; // the control representing the entire configuration block

	private class ListEntry {
		public final String fS;
		public boolean fB;
		ListEntry(String s) {
			fS = s;
			fB = false;
		}
		public String toString() {
			return fS;
		}
	}

	private class FactoryPathAdapter implements IListAdapter, IDialogFieldListener {
		/**
		 * Can't remove a selection that contains a plugin.
		 */
		private boolean canRemove(ListDialogField field) {
			List selected= fFactoryPathList.getSelectedElements();
			boolean containsPlugin= false;
			for (Object o : selected) {
				if (((FactoryContainer)o).getType() == FactoryContainer.FactoryType.PLUGIN) {
					containsPlugin = true;
					break;
				}
			}
			return !containsPlugin;
		}

        public void customButtonPressed(ListDialogField field, int index) {
        	FactoryPathConfigurationBlock.this.customButtonPressed(index);
        }

        public void selectionChanged(ListDialogField field) {
        	boolean enableRemove = canRemove(field);
        	field.enableButton(IDX_REMOVE, enableRemove);
        }

		public void dialogFieldChanged(DialogField field) {
        }

        public void doubleClicked(ListDialogField field) {
        }
	}
	
	/**
	 * The factory path at the time this pref pane was launched.
	 * Use this to see if anything changed at save time.
	 */
	private Map<FactoryContainer, Boolean> fOriginalPath;
	
	/**
	 * True if the pref pane is for a project and project-specific
	 * settings were enabled when the pane was initialized.
	 * Use this to see if anything changed at save time.
	 */
	private boolean fOriginallyProjectSpecific;
	
	private final IJavaProject fJProj;

	private CheckedListDialogField fFactoryPathList;

	/**
	 * @param context
	 * @param project
	 * @param keys
	 * @param container
	 */
	public FactoryPathConfigurationBlock(IStatusChangeListener context,
			IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, new Key[] {}, container);
		
		fJProj = JavaCore.create(project);
		
		FactoryPathAdapter adapter= new FactoryPathAdapter();
		
		fFactoryPathList= new CheckedListDialogField(adapter, buttonLabels, new LabelProvider());
		fFactoryPathList.setDialogFieldListener(adapter);
		fFactoryPathList.setLabelText(Messages.FactoryPathConfigurationBlock_pluginsAndJars);
		fFactoryPathList.setUpButtonIndex(IDX_UP);
		fFactoryPathList.setDownButtonIndex(IDX_DOWN);
		fFactoryPathList.setRemoveButtonIndex(IDX_REMOVE);
		fFactoryPathList.setCheckAllButtonIndex(IDX_ENABLEALL);
		fFactoryPathList.setUncheckAllButtonIndex(IDX_DISABLEALL);		
	}

	/**
	 * Respond to a button in the button bar.
	 * Most buttons are handled by code in CheckedListDialogField;
	 * this method is for the rest, e.g., Add External Jar.
	 * @param index
	 */
	public void customButtonPressed(int index) {
		FactoryContainer[] newEntries = null;
		switch (index) {
		case IDX_ADDJAR: // add jars in project
			newEntries= openJarFileDialog(null);
			addEntries(newEntries);
			break;
			
		case IDX_ADDEXTJAR: // add external jars
			newEntries= openExtJarFileDialog(null);
			addEntries(newEntries);
			break;
			
		case IDX_ADDVAR: // add jar from classpath variable
			newEntries= openVariableSelectionDialog(null);
			addEntries(newEntries);
			break;
		}
		
	}
	
	private void addEntries(FactoryContainer[] entries) {
		if (null == entries) {
			return;
		}
		int insertAt;
		List selectedElements= fFactoryPathList.getSelectedElements();
		if (selectedElements.size() == 1) {
			insertAt= fFactoryPathList.getIndexOfElement(selectedElements.get(0)) + 1;
		} else {
			insertAt= fFactoryPathList.getSize();
		}
		for (int i = 0; i < entries.length; ++i) {
			fFactoryPathList.addElement(entries[i], insertAt + i);
			fFactoryPathList.setChecked(entries[i], true);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.ui.internal.preferences.BaseConfigurationBlock#createContents(org.eclipse.swt.widgets.Composite)
	 */
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		
		fPixelConverter= new PixelConverter(parent);
		
		fBlockControl= new Composite(parent, SWT.NONE);
		fBlockControl.setFont(parent.getFont());

		Dialog.applyDialogFont(fBlockControl);
		
		LayoutUtil.doDefaultLayout(fBlockControl, new DialogField[] { fFactoryPathList }, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(fFactoryPathList.getListControl(null));

		int buttonBarWidth= fPixelConverter.convertWidthInCharsToPixels(24);
		fFactoryPathList.setButtonsMinWidth(buttonBarWidth);
		
		cacheOriginalValues();
		initListContents();

		//TODO: enable help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJavaHelpContextIds.BUILD_PATH_BLOCK);				
		return fBlockControl;
	}

	/**
	 * (Re-)initialize the contents of the list control to the currently saved factory path.
	 * This relies on the cached values being correct (@see #cacheOriginalValues()).
	 */
	private void initListContents() {
		fFactoryPathList.removeAllElements();
		for (Map.Entry<FactoryContainer, Boolean> e : fOriginalPath.entrySet()) {
			FactoryContainer fc = e.getKey();
			fFactoryPathList.addElement(fc);
			fFactoryPathList.setChecked(fc, e.getValue());
		}
	}
	
	/**
	 * Save reference copies of the settings, so we can see if anything changed.
	 * This must stay in sync with the actual saved values for the rebuild logic
	 * to work; so be sure to call this any time you save (eg in performApply()).
	 */
	private void cacheOriginalValues() {
		fOriginalPath = FactoryPath.getAllContainers(fJProj);
		fOriginallyProjectSpecific = FactoryPath.hasProjectSpecificFactoryPath(fJProj);
	}
	
	private FactoryContainer[] openJarFileDialog(FactoryContainer existing) {
		IWorkspaceRoot root= fJProj.getProject().getWorkspace().getRoot();
		if (existing == null) {
			// TODO: instantiate existingPaths, to prevent duplicate entries.
			IPath[] existingPaths = new IPath[] {};
			IPath[] selected= BuildPathDialogAccess.chooseJAREntries(getShell(), fJProj.getPath(), existingPaths);
			if (selected != null) {
				ArrayList<FactoryContainer> res= new ArrayList<FactoryContainer>();
				for (int i= 0; i < selected.length; i++) {
					// verify that the path points to an actual resource.
					//TODO: how to handle missing jars?
					IResource resource= root.findMember(selected[i]);
					if (resource instanceof IFile) {
						res.add(FactoryPath.newWkspJarFactoryContainer(selected[i]));
					}
				}
				return res.toArray(new FactoryContainer[res.size()]);
			}
		} 		
		return null;
	}

	//TODO: figure out how to edit an existing jar file - see LibrariesWorkbookPage for example
	private FactoryContainer[] openExtJarFileDialog(FactoryContainer existing) {
		if (existing == null) {
			IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(getShell());
			if (selected != null) {
				ArrayList<FactoryContainer> res= new ArrayList<FactoryContainer>();
				for (int i= 0; i < selected.length; i++) {
					res.add(FactoryPath.newExtJarFactoryContainer(selected[i].toFile()));
				}
				return res.toArray(new FactoryContainer[res.size()]);
			}
		} 		
		return null;
	}
	
	/*
	 * Helper method to get rid of unchecked conversion warning
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private List<FactoryContainer> getListContents() {
		List<FactoryContainer> contents= fFactoryPathList.getElements();
		return contents;
	}
	
	private FactoryContainer[] openVariableSelectionDialog(FactoryContainer existing) {
		if (existing == null) {
			// TODO: instantiate existingPaths, to prevent duplicate entries.
			IPath[] existingPaths = new IPath[] {};
			IPath[] selected= BuildPathDialogAccess.chooseVariableEntries(getShell(), existingPaths);
			if (selected != null) {
				ArrayList<FactoryContainer> res= new ArrayList<FactoryContainer>();
				for (int i= 0; i < selected.length; i++) {
					res.add(FactoryPath.newVarJarFactoryContainer(selected[i]));
				}
				return res.toArray(new FactoryContainer[res.size()]);
			}
		} 		
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.ui.internal.preferences.BaseConfigurationBlock#updateModel(org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
	 */
	protected void updateModel(DialogField field) {
		// We don't use IEclipsePreferences for this pane, so no need to do anything.
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.ui.internal.preferences.BaseConfigurationBlock#validateSettings(org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock.Key, java.lang.String, java.lang.String)
	 */
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		// Nothing to validate.
	}
	
	private void saveSettings() {
		Map<FactoryContainer, Boolean> containers;
		if ((fJProj != null) && !fBlockControl.isEnabled()) {
			// We're in a project properties pane but the entire configuration 
			// block control is disabled.  That means the per-project settings checkbox 
			// is unchecked.  To save that state, we'll delete the settings file.
			containers = null;
		}
		else {
			containers = new LinkedHashMap<FactoryContainer, Boolean>();
			int count = fFactoryPathList.getSize();
			for (int i = 0; i < count; ++i) {
				FactoryContainer fc = (FactoryContainer)fFactoryPathList.getElement(i);
				Boolean enabled = fFactoryPathList.isChecked(fc);
				containers.put(fc, new Boolean(enabled));
			}
		}
		
		try {
			FactoryPath.setContainers(fJProj, containers);
		}
		catch (IOException e) {
			// TODO: what?
			e.printStackTrace();
		}
		catch (CoreException e) {
			// TODO: what?
			e.printStackTrace();
		}
	}
	
	/**
	 * If per-project, restore list contents to current workspace settings;
	 * the per-project settings checkbox will be cleared for us automatically.
	 * If workspace, restore list contents to factory-default settings.
	 */
	public void performDefaults() {
		Map<FactoryContainer, Boolean> defaults = FactoryPath.getDefaultFactoryPath(fJProj);
		fFactoryPathList.removeAllElements();
		for (Map.Entry<FactoryContainer, Boolean> e : defaults.entrySet()) {
			FactoryContainer fc = e.getKey();
			fFactoryPathList.addElement(fc);
			fFactoryPathList.setChecked(fc, e.getValue());
		}
		super.performDefaults();
	}
	
	/**
	 * If there are changed settings, save them and ask user whether to rebuild.
	 * This is called by performOk() and performApply().
	 * @param container null when called from performApply().
	 */
	protected boolean processChanges(IWorkbenchPreferenceContainer container) {
		if (!settingsChanged()) {
			return true;
		}
		
		int response= 1; // "NO" rebuild unless we put up the dialog.
		String[] strings= getFullBuildDialogStrings(fProject == null);
		if (strings != null) {
			MessageDialog dialog= new MessageDialog(
					getShell(), 
					strings[0], 
					null, 
					strings[1], 
					MessageDialog.QUESTION, 
					new String[] { 
						IDialogConstants.YES_LABEL, 
						IDialogConstants.NO_LABEL, 
						IDialogConstants.CANCEL_LABEL 
					}, 
					2);
			response= dialog.open();
		}
		if (response == 0 || response == 1) { // "YES" or "NO" - either way, save.
			saveSettings();
			if (container == null) {
				// we're doing an Apply, so update the reference values.
				cacheOriginalValues();
			}
		}
		if (response == 0) { // "YES", rebuild
			if (container != null) {
				// build after dialog exits
				container.registerUpdateJob(CoreUtility.getBuildJob(fProject));
			} else {
				// build immediately
				CoreUtility.getBuildJob(fProject).schedule();
			}
		} else if (response != 1) { // "CANCEL" - no save, no rebuild.
			return false;
		}
		return true;
	}

	/**
	 * @return true if settings or project-specificness changed since
	 * the pane was launched - that is, if there is anything to save.
	 */
	private boolean settingsChanged() {
		boolean isProjectSpecific= (fJProj != null) && fBlockControl.getEnabled();
		if (fOriginallyProjectSpecific ^ isProjectSpecific) {
			// the project-specificness changed.
			return true;
		} else if ((fJProj != null) && !isProjectSpecific) {
			// no project specific data, and there never was, so nothing could have changed.
			return false;
		}
		int count = fFactoryPathList.getSize();
		if (fOriginalPath.size() != count) {
			// something was added or removed
			return true;
		}
		// now we know both lists are the same size
		Iterator<Map.Entry<FactoryContainer, Boolean>> iOriginal = fOriginalPath.entrySet().iterator();
		for (int i = 0; i < count; ++i) {
			Map.Entry<FactoryContainer, Boolean> entry = iOriginal.next();
			Boolean wasEnabled = entry.getValue();
			FactoryContainer fc = (FactoryContainer)fFactoryPathList.getElement(i);
			if (!fc.equals(entry.getKey())) {
				return true;
			}
			Boolean isEnabled = fFactoryPathList.isChecked(fc);
			if (isEnabled ^ wasEnabled) {
				return true;
			}
		}
		return false;
	}
	
}
