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
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.apt.core.internal.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.JarFactoryContainer;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.*;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
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
	private static final int IDX_ADDEXTJAR= 3;
	private static final int IDX_REMOVE= 4;
	//
	private static final int IDX_ENABLEALL= 6;
	private static final int IDX_DISABLEALL= 7;

	private final static String[] buttonLabels = { 
		"Up",                   // 0
		"Down",                 // 1
		null,                   // 2
		"Add External Jar...",  // 3
		"Remove",               // 4
		null,                   // 5
		"Enable All",           // 6
		"Disable All"           // 7
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
        	buttonPressed(index);
        }

        public void selectionChanged(ListDialogField field) {
        	boolean enableRemove = canRemove(field);
        	field.enableButton(IDX_REMOVE, enableRemove);
        }

		public void dialogFieldChanged(DialogField field) {
        	updateModel(field);
        }

        public void doubleClicked(ListDialogField field) {
        }
	}
	
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
		fFactoryPathList.setLabelText("Plugins and jars that contain annotation processors:");  
		fFactoryPathList.setUpButtonIndex(0);
		fFactoryPathList.setDownButtonIndex(1);
		fFactoryPathList.setRemoveButtonIndex(4);
		fFactoryPathList.setCheckAllButtonIndex(6);
		fFactoryPathList.setUncheckAllButtonIndex(7);		
	}

	/**
	 * Respond to a button in the button bar.
	 * Most buttons are handled by code in CheckedListDialogField;
	 * this method is for the rest, e.g., Add External Jar.
	 * @param index
	 */
	public void buttonPressed(int index) {
		if (index == IDX_ADDEXTJAR) { // add new
			FactoryContainer[] newEntries= openExtJarFileDialog(null);
			int insertAt;
			List selectedElements= fFactoryPathList.getSelectedElements();
			if (selectedElements.size() == 1) {
				insertAt= fFactoryPathList.getIndexOfElement(selectedElements.get(0)) + 1;
			} else {
				insertAt= fFactoryPathList.getSize();
			}
			for (int i = 0; i < newEntries.length; ++i) {
				fFactoryPathList.addElement(newEntries[i], insertAt + i);
				fFactoryPathList.setChecked(newEntries[i], true);
			}
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
		
		initListContents();

		//TODO: enable help
		//PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IJavaHelpContextIds.BUILD_PATH_BLOCK);				
		return fBlockControl;
	}

	/**
	 * 
	 */
	private void initListContents() {
		Map<FactoryContainer, Boolean> containers = AptConfig.getAllContainers(fJProj);
		for (Map.Entry<FactoryContainer, Boolean> e : containers.entrySet()) {
			FactoryContainer fc = (FactoryContainer)e.getKey();
			fFactoryPathList.addElement(fc);
			fFactoryPathList.setChecked(fc, ((Boolean)e.getValue()).booleanValue());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#getFullBuildDialogStrings(boolean)
	 */
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return null; // TODO: figure this out.
	}

	//TODO: figure out how to edit an existing jar file - see LibrariesWorkbookPage for example
	private FactoryContainer[] openExtJarFileDialog(FactoryContainer existing) {
		if (existing == null) {
			IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(getShell());
			if (selected != null) {
				ArrayList<FactoryContainer> res= new ArrayList<FactoryContainer>();
				for (int i= 0; i < selected.length; i++) {
					res.add(new JarFactoryContainer(selected[i].toFile()));
				}
				return (FactoryContainer[]) res.toArray(new FactoryContainer[res.size()]);
			}
		} 		
		return null;
	}
		
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.ui.internal.preferences.BaseConfigurationBlock#updateModel(org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField)
	 */
	protected void updateModel(DialogField field) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jdt.apt.ui.internal.preferences.BaseConfigurationBlock#validateSettings(org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock.Key, java.lang.String, java.lang.String)
	 */
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
	}
	
	private void saveContainers() {
		Map<FactoryContainer, Boolean> containers;
		// If the entire configuration block control is disabled in a project 
		// properties dialog, it's because the per-project settings checkbox 
		// is unchecked.  To save that state, we need to delete the settings file.
		if ((fJProj == null) || fBlockControl.isEnabled()) {
			List listElems = fFactoryPathList.getElements();
			containers = new LinkedHashMap<FactoryContainer, Boolean>();
			int count = fFactoryPathList.getSize();
			for (int i = 0; i < count; ++i) {
				FactoryContainer fc = (FactoryContainer)fFactoryPathList.getElement(i);
				Boolean enabled = fFactoryPathList.isChecked(fc);
				containers.put(fc, new Boolean(enabled));
			}
		}
		else {
			containers = null;
		}
		
		try {
			AptConfig.setContainers(fJProj, containers);
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

	public boolean performOk() {
		// TODO: if the project-specific settings is checked.
		if (true) {
			saveContainers();
		}
		return super.performOk();
	}
	
	public void performDefaults() {
		try {
			AptConfig.setContainers(fJProj, null);
		}
		catch (IOException e) {
			// TODO: what?
			e.printStackTrace();
		}
		catch (CoreException e) {
			// TODO: what?
			e.printStackTrace();
		}
		initListContents();

		super.performDefaults();
	}
	
	public boolean performApply() {
		saveContainers();
		return super.performApply();
	}
	
}
