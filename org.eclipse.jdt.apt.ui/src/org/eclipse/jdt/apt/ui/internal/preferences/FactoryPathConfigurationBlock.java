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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.core.internal.util.FactoryPathUtil;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath.Attributes;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.IFactoryPath;
import org.eclipse.jdt.apt.ui.internal.util.ExceptionHandler;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.CheckedListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.ui.wizards.BuildPathDialogAccess;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Data and controls for the Java Annotation Factory Path preference page.
 */
public class FactoryPathConfigurationBlock extends BaseConfigurationBlock {

	private static final int IDX_UP= 0;
	private static final int IDX_DOWN= 1;
	// 2
	private static final int IDX_ADDJAR= 3;
	private static final int IDX_ADDEXTJAR= 4;
	private static final int IDX_ADDVAR= 5;
	// 6
	private static final int IDX_EDIT= 7;
	private static final int IDX_ADVANCED= 8;
	private static final int IDX_REMOVE= 9;
	// 10
	private static final int IDX_ENABLEALL= 11;
	private static final int IDX_DISABLEALL= 12;

	private final static String[] buttonLabels = { 
		Messages.FactoryPathConfigurationBlock_up,
		Messages.FactoryPathConfigurationBlock_down,
		null,                    // 2
		Messages.FactoryPathConfigurationBlock_addJars,
		Messages.FactoryPathConfigurationBlock_addExternalJars,
		Messages.FactoryPathConfigurationBlock_addVariable,
		null,                    // 6
		Messages.FactoryPathConfigurationBlock_edit,
		Messages.FactoryPathConfigurationBlock_advanced,
		Messages.FactoryPathConfigurationBlock_remove,
		null,                    // 10
		Messages.FactoryPathConfigurationBlock_enableAll,
		Messages.FactoryPathConfigurationBlock_disableAll
	};

	/**
	 * Event handler for factory path list control
	 */
	private class FactoryPathAdapter implements IListAdapter, IDialogFieldListener {
        public void customButtonPressed(ListDialogField field, int index) {
        	FactoryPathConfigurationBlock.this.customButtonPressed(index);
        }

        public void selectionChanged(ListDialogField field) {
        	boolean enableRemove = canRemove();
        	field.enableButton(IDX_REMOVE, enableRemove);
        	boolean enableEdit = canEdit();
        	field.enableButton(IDX_EDIT, enableEdit);
        	boolean enableAdvanced = canAdvanced();
        	field.enableButton(IDX_ADVANCED, enableAdvanced);
        }

        /**
         * This method gets called when, among other things, a checkbox is
         * clicked.  However, it doesn't get any information about which
         * item it was whose checkbox was clicked, so it's pretty useless.
         * Instead of maintaining the FactoryPathEntry states here, we
         * hook into the list control's CheckboxTableViewer event listener.
         */
		public void dialogFieldChanged(DialogField field) {
        }

        public void doubleClicked(ListDialogField field) {
        	if (canEdit()) {
        		editSelectedItem();
        	}
        }
	}
	
	private class FactoryPathLabelProvider extends LabelProvider implements ITableLabelProvider {

		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}

		public String getColumnText(Object element, int columnIndex) {
			if (!(element instanceof FactoryPathEntry)) {
				return ""; //$NON-NLS-1$
			}
			FactoryPathEntry fpe = (FactoryPathEntry)element;
			if (columnIndex == 0) {
				return fpe._fc.toString();
			}
			else {
				return ""; //$NON-NLS-1$
			}
		}
	}
	
	/**
	 * The factory path is a list of containers, plus some information about
	 * each container.  That makes it a list of FactoryPathEntry.  
	 */
	private static class FactoryPathEntry {
		/* shallow copies - beware! */
		public final FactoryContainer _fc;
		public FactoryPath.Attributes _attr;
		
		// CONSTRUCTORS
		public FactoryPathEntry(FactoryContainer fc, FactoryPath.Attributes attr) {
			_fc = fc;
			_attr = attr;
		}

		// CONVERSION TO/FROM INDIVIDUAL ELEMENTS
		public static Map<FactoryContainer, Attributes> pathMapFromList(List<FactoryPathEntry> list) {
			Map<FactoryContainer, FactoryPath.Attributes> map = 
				new LinkedHashMap<FactoryContainer, FactoryPath.Attributes>(list.size());
			for (FactoryPathEntry fpe : list) {
				map.put(fpe._fc, fpe._attr);
			}
			return map;
		}
		public static List<FactoryPathEntry> pathListFromMap(Map<FactoryContainer, Attributes> map) {
			List<FactoryPathEntry> list = new ArrayList<FactoryPathEntry>(map.size());
			for (Map.Entry<FactoryContainer, Attributes> entry : map.entrySet()) {
				FactoryPathEntry fpe = new FactoryPathEntry(entry.getKey(), entry.getValue());
				list.add(fpe);
			}
			return list;
		}

		// SUPPORT FOR COMPARISON
		public boolean equals(Object obj) {
			if (!(obj instanceof FactoryPathEntry))
				return false;
			FactoryPathEntry fpe = (FactoryPathEntry)obj;
			return _fc.equals(fpe._fc) && _attr.equals(fpe._attr);
		}
		public int hashCode() {
			return _fc.hashCode() ^ _attr.hashCode();
		}
		
	}
	
	private PixelConverter fPixelConverter;
	private Composite fBlockControl; // the control representing the entire configuration block

	/**
	 * The factory path at the time this pref pane was launched.
	 * Use this to see if anything changed at save time.
	 */
	private List<FactoryPathEntry> fOriginalPath;
	
	/**
	 * True if the pref pane is for a project and project-specific
	 * settings were enabled when the pane was initialized.
	 * Use this to see if anything changed at save time.
	 */
	private boolean fOriginallyProjectSpecific;
	
	private final IJavaProject fJProj;

	/** 
	 * The GUI control representing the factory path.  Its data items
	 * are of type FactoryPathEntry. 
	 */
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
		FactoryPathLabelProvider labelProvider = new FactoryPathLabelProvider();
		
		fFactoryPathList= new CheckedListDialogField(adapter, buttonLabels, labelProvider);
		fFactoryPathList.setDialogFieldListener(adapter);
		fFactoryPathList.setLabelText(Messages.FactoryPathConfigurationBlock_pluginsAndJars);
		fFactoryPathList.setUpButtonIndex(IDX_UP);
		fFactoryPathList.setDownButtonIndex(IDX_DOWN);
		fFactoryPathList.setRemoveButtonIndex(IDX_REMOVE);
		fFactoryPathList.setCheckAllButtonIndex(IDX_ENABLEALL);
		fFactoryPathList.setUncheckAllButtonIndex(IDX_DISABLEALL);		
	}

	/**
	 * Respond to the user checking the "enabled" checkbox of an entry
	 * in the factory path control, by replacing the FactoryPathEntry
	 * with a new one with the correct "enabled" value.
	 */
	protected void doCheckStateChanged(CheckStateChangedEvent e) {
		Object o = e.getElement();
		if (o == null || !(o instanceof FactoryPathEntry)) {
			return;  // shouldn't ever happen
		}
		FactoryPathEntry fpe = (FactoryPathEntry)o;
		boolean isChecked = e.getChecked();
		fpe._attr.setEnabled(isChecked);
	}

	/**
	 * Respond to a button in the button bar.
	 * Most buttons are handled by code in CheckedListDialogField;
	 * this method is for the rest, e.g., Add External Jar.
	 * @param index
	 */
	public void customButtonPressed(int index) {
		FactoryPathEntry[] newEntries = null;
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
			
		case IDX_EDIT: // edit selected item
			if (canEdit()) {
				editSelectedItem();
			}
			break;
			
		case IDX_ADVANCED: // advanced options
			advancedOptionsDialog();
			break;
		}
		
	}
	
	/**
	 * Can't remove a selection that contains a plugin.
	 */
	private boolean canRemove() {
		List<FactoryPathEntry> selected= getSelectedListContents();
		boolean containsPlugin= false;
		for (FactoryPathEntry fpe : selected) {
			if (fpe._fc.getType() == FactoryContainer.FactoryType.PLUGIN) {
				containsPlugin = true;
				break;
			}
		}
		return !containsPlugin;
	}
	
	/**
	 * Can only edit a single item at a time.  Can't edit plugins.
	 */
	private boolean canEdit() {
		List<FactoryPathEntry> selected= getSelectedListContents();
		if (selected.size() != 1) {
			return false;
		}
		FactoryContainer fc = selected.get(0)._fc;
		return (fc.getType() != FactoryContainer.FactoryType.PLUGIN);
	}

	/**
	 * Can only launch the 'advanced' dialog on a single item at a time.
	 */
	private boolean canAdvanced() {
		List<FactoryPathEntry> selected= getSelectedListContents();
		return (selected.size() == 1);
	}

	private void addEntries(FactoryPathEntry[] entries) {
		if (null == entries) {
			return;
		}
		int insertAt;
		List<FactoryPathEntry> selectedElements= getSelectedListContents();
		if (selectedElements.size() == 1) {
			insertAt= fFactoryPathList.getIndexOfElement(selectedElements.get(0)) + 1;
		} else {
			insertAt= fFactoryPathList.getSize();
		}
		for (int i = 0; i < entries.length; ++i) {
			fFactoryPathList.addElement(entries[i], insertAt + i);
			fFactoryPathList.setChecked(entries[i], entries[i]._attr.isEnabled());
		}
	}
	
	/**
	 * Edit the item selected.
	 * Precondition: exactly one item is selected in the list,
	 * and it is an editable item (not a plugin).
	 * @param field a listbox of FactoryContainers.
	 */
	private void editSelectedItem() {
		List<FactoryPathEntry> selected= getSelectedListContents();
		if (selected.size() != 1) {
			return;
		}
		FactoryPathEntry original = selected.get(0);
		FactoryPathEntry[] edited = null;
		switch (original._fc.getType()) {
		case PLUGIN:
			return;
		case EXTJAR:
			edited= openExtJarFileDialog(original);
			break;
		case VARJAR:
			edited= openVariableSelectionDialog(original);
			break;
		case WKSPJAR:
			edited= openJarFileDialog(original);
			break;
		}
		if (edited != null && edited.length > 0) {
			fFactoryPathList.replaceElement(original, edited[0]);
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

		fFactoryPathList.enableButton(IDX_ADDJAR, (fJProj != null));
		int buttonBarWidth= fPixelConverter.convertWidthInCharsToPixels(24);
		fFactoryPathList.setButtonsMinWidth(buttonBarWidth);
		
		cacheOriginalValues();
		initListContents();

		// Register a change listener on the checkboxes
		CheckboxTableViewer tableViewer = (CheckboxTableViewer)fFactoryPathList.getTableViewer();
		tableViewer.addCheckStateListener(new ICheckStateListener() {
			public void checkStateChanged(CheckStateChangedEvent e) {
				doCheckStateChanged(e);
			}
		});

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
		for (FactoryPathEntry originalFpe : fOriginalPath) {
			// clone, because we may later modify it and we want to compare with the original.
			FactoryPathEntry fpe = new FactoryPathEntry(originalFpe._fc, new Attributes(originalFpe._attr));
			fFactoryPathList.addElement(fpe);
			fFactoryPathList.setChecked(fpe, fpe._attr.isEnabled());
		}
	}
	
	/**
	 * Save reference copies of the settings, so we can see if anything changed.
	 * This must stay in sync with the actual saved values for the rebuild logic
	 * to work; so be sure to call this any time you save (eg in performApply()).
	 */
	private void cacheOriginalValues() {
		IFactoryPath ifp = AptConfig.getFactoryPath(fJProj);
		// we'll risk this downcast because we're such good buddies with apt.core.
		FactoryPath fp = (FactoryPath)ifp;
		Map<FactoryContainer, FactoryPath.Attributes> path = fp.getAllContainers();
		fOriginalPath = FactoryPathEntry.pathListFromMap(path);
		fOriginallyProjectSpecific = AptConfig.hasProjectSpecificFactoryPath(fJProj);
	}
	
	/*
	 * Helper method to get rid of unchecked conversion warning
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private List<FactoryPathEntry> getListContents() {
		List<FactoryPathEntry> contents= fFactoryPathList.getElements();
		return contents;
	}
	
	/*
	 * Helper method to get rid of unchecked conversion warning
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private List<FactoryPathEntry> getSelectedListContents() {
		List<FactoryPathEntry> contents= fFactoryPathList.getSelectedElements();
		return contents;
	}
	
	/**
	 * Get all the containers of a certain type currently on the list.
	 * The format of the returned paths will depend on the container type:
	 * for EXTJAR it will be an absolute path; for WKSPJAR it will be a
	 * path relative to the workspace root; for VARJAR it will be a path
	 * whose first segment is the name of a classpath variable.
	 * @param type may not be PLUGIN
	 * @param ignore null, or an item to not put on the list (used when
	 * editing an existing item).
	 * @return an array, possibly empty (but never null)
	 */
	private IPath[] getExistingPaths(FactoryContainer.FactoryType type, FactoryContainer ignore) {
		if (type == FactoryContainer.FactoryType.PLUGIN) {
			throw new IllegalArgumentException();
		}
		List<FactoryPathEntry> all = getListContents();
		// find out how many entries there are of this type
		int countType = 0;
		for (FactoryPathEntry fpe : all) {
			FactoryContainer fc = fpe._fc;
			if (fc.getType() == type && fc != ignore) {
				++countType;
			}
		}
		// create an array of paths, one per entry of this type 
		IPath[] some = new IPath[countType];
		int i = 0;
		for (FactoryPathEntry fpe : all) {
			FactoryContainer fc = fpe._fc;
			if (fc.getType() == type && fc != ignore) {
				some[i++] = new Path(fc.getId());
			}
		}
		return some;
	}
	
	/**
	 * Launch the "advanced options" dialog, which displays the factory classes
	 * contained by the selected container and allows the user to specify
	 * options that are needed only in certain special cases.
	 * 
	 * We treat advanced options as an attribute of the factory path, not of the 
	 * container; the same container may have different advanced options in different
	 * projects.  We treat advanced options the same way as the "enabled" flag.
	 */
	private void advancedOptionsDialog() {
		List<FactoryPathEntry> selected= getSelectedListContents();
		if (selected.size() != 1) {
			return;
		}
		FactoryPathEntry original= selected.get(0);
		AdvancedFactoryPathOptionsDialog dialog= 
			new AdvancedFactoryPathOptionsDialog(getShell(), original._fc, original._attr);
		if (dialog.open() == Window.OK) {
			original._attr = dialog.getResult();
			// If the dialog could change the enabled attribute, we would also
			// need to update the checkbox in the GUI here.  But it doesn't.
		}
	}

	/**
	 * Add or edit a project-relative jar file.  Only possible when editing
	 * project properties; this method is disabled in workspace prefs.
	 * @param original null, or an existing list entry to be edited
	 * @return a list of additional factory path entries to be added
	 */
	private FactoryPathEntry[] openJarFileDialog(FactoryPathEntry original) {
		if (fJProj == null) {
			return null;
		}
		IWorkspaceRoot root= fJProj.getProject().getWorkspace().getRoot();
		
		if (original == null) {
			IPath[] results= BuildPathDialogAccess.chooseJAREntries(getShell(), fJProj.getPath(), new IPath[0]);
			if (results == null) {
				return null;
			}
			ArrayList<FactoryPathEntry> res= new ArrayList<FactoryPathEntry>();
			for (int i= 0; i < results.length; i++) {
				IResource resource= root.findMember(results[i]);
				if (resource instanceof IFile) {
					FactoryContainer fc = FactoryPathUtil.newWkspJarFactoryContainer(results[i]);
					// assume defaults of enabled=true, runInAptMode=false
					FactoryPath.Attributes attr = new FactoryPath.Attributes(true, false);
					FactoryPathEntry fpe = new FactoryPathEntry(fc, attr);
					res.add(fpe);
				}
				//TODO: handle missing jars
			}
			return res.toArray(new FactoryPathEntry[res.size()]);
		}
		else {
			IPath[] existingPaths = getExistingPaths(FactoryContainer.FactoryType.WKSPJAR, original._fc);
			IPath result= BuildPathDialogAccess.configureJAREntry(getShell(), new Path(original._fc.getId()), existingPaths);
			if (result == null) {
				return null;
			}
			IResource resource= root.findMember(result);
			if (resource instanceof IFile) {
				FactoryPathEntry[] edited = new FactoryPathEntry[1];
				FactoryContainer fc= FactoryPathUtil.newWkspJarFactoryContainer(result);
				// Use prior value for isEnabled.  Assume default of runInAptMode=false
				FactoryPath.Attributes attr = new FactoryPath.Attributes(original._attr.isEnabled(), false);
				edited[0]= new FactoryPathEntry(fc, attr);
				return edited;
			}
			//TODO: handle missing jars
			return null;
 		}
	}

	/**
	 * Add or edit an external (not project-relative) jar file.  
	 * @param original null, or an existing list entry to be edited
	 * @return a list of additional factory path entries to be added
	 */
	private FactoryPathEntry[] openExtJarFileDialog(FactoryPathEntry original) {
		if (original == null) {
			IPath[] selected= BuildPathDialogAccess.chooseExternalJAREntries(getShell());
			if (selected == null) {
				return null;
			}
			ArrayList<FactoryPathEntry> res= new ArrayList<FactoryPathEntry>();
			for (int i= 0; i < selected.length; i++) {
				FactoryContainer fc = FactoryPathUtil.newExtJarFactoryContainer(selected[i].toFile());
				// assume defaults of enabled=true, runInAptMode=false
				FactoryPath.Attributes attr = new FactoryPath.Attributes(true, false);
				FactoryPathEntry fpe = new FactoryPathEntry(fc, attr);
				res.add(fpe);
			}
			return res.toArray(new FactoryPathEntry[res.size()]);
		}
		else {
			IPath result= BuildPathDialogAccess.configureExternalJAREntry(getShell(), new Path(original._fc.getId()));
			if (result == null) {
				return null;
			}
			FactoryPathEntry[] edited= new FactoryPathEntry[1];
			FactoryContainer fc= FactoryPathUtil.newExtJarFactoryContainer(result.toFile());
			// Use prior value for isEnabled.  Assume default of runInAptMode=false
			FactoryPath.Attributes attr = new FactoryPath.Attributes(original._attr.isEnabled(), false);
			edited[0]= new FactoryPathEntry(fc, attr);
			return edited;
		}
	}
	
	/**
	 * Add or edit an external (not project-relative) jar file whose
	 * location includes a classpath variable name.  
	 * @param original null, or an existing list entry to be edited
	 * @return a list of additional factory path entries to be added
	 */
	private FactoryPathEntry[] openVariableSelectionDialog(FactoryPathEntry original) {
		if (original == null) {
			IPath[] selected= BuildPathDialogAccess.chooseVariableEntries(getShell(), new IPath[0]);
			if (selected == null) {
				return null;
			}
			ArrayList<FactoryPathEntry> res= new ArrayList<FactoryPathEntry>();
			for (int i= 0; i < selected.length; i++) {
				FactoryContainer fc= FactoryPathUtil.newVarJarFactoryContainer(selected[i]);
				// assume defaults of enabled=true, runInAptMode=false
				FactoryPath.Attributes attr = new FactoryPath.Attributes(true, false);
				FactoryPathEntry fpe = new FactoryPathEntry(fc, attr);
				res.add(fpe);
			}
			return res.toArray(new FactoryPathEntry[res.size()]);
		}
		else {
			IPath[] existingPaths = getExistingPaths(FactoryContainer.FactoryType.VARJAR, original._fc);
			IPath result= BuildPathDialogAccess.configureVariableEntry(getShell(), new Path(original._fc.getId()), existingPaths);
			if (result == null) {
				return null;
			}
			FactoryPathEntry[] edited= new FactoryPathEntry[1];
			FactoryContainer fc= FactoryPathUtil.newVarJarFactoryContainer(result);
			// Use prior value for isEnabled.  Assume default of runInAptMode=false
			FactoryPath.Attributes attr = new FactoryPath.Attributes(original._attr.isEnabled(), false);
			edited[0]= new FactoryPathEntry(fc, attr);
			return edited;
		}
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
		// TODO: validate that all the specified factory containers exist?
	}
	
	private void saveSettings() {
		List<FactoryPathEntry> containers;
		if ((fJProj != null) && !fBlockControl.isEnabled()) {
			// We're in a project properties pane but the entire configuration 
			// block control is disabled.  That means the per-project settings checkbox 
			// is unchecked.  To save that state, we'll delete the settings file.
			containers = null;
		}
		else {
			containers = getListContents();
		}
		
		Map<FactoryContainer, FactoryPath.Attributes> map = FactoryPathEntry.pathMapFromList(containers);
		
		try {
			FactoryPath fp = new FactoryPath();
			fp.setContainers(map);
			AptConfig.setFactoryPath(fJProj, fp);
		}
		catch (CoreException e) {
			final String title = Messages.FactoryPathConfigurationBlock_unableToSaveFactorypath_title;
			final String message = Messages.FactoryPathConfigurationBlock_unableToSaveFactorypath_message;
			ExceptionHandler.handle(e, fBlockControl.getShell(), title, message);
		}
	}
	
	/**
	 * If per-project, restore list contents to current workspace settings;
	 * the per-project settings checkbox will be cleared for us automatically.
	 * If workspace, restore list contents to factory-default settings.
	 */
	public void performDefaults() {
		IFactoryPath ifp = AptConfig.getDefaultFactoryPath(fJProj);
		// we'll risk this downcast because we're such good buddies with apt.core.
		FactoryPath fp = (FactoryPath)ifp;
		Map<FactoryContainer, FactoryPath.Attributes> map = fp.getAllContainers();
		List<FactoryPathEntry> defaults = FactoryPathEntry.pathListFromMap(map);
		fFactoryPathList.removeAllElements();
		for (FactoryPathEntry fpe : defaults) {
			fFactoryPathList.addElement(fpe);
			fFactoryPathList.setChecked(fpe, fpe._attr.isEnabled());
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
		if (fOriginalPath == null) {
			// shouldn't happen, but just in case it does, consider it a change.
			return true;
		}
		// Is the new path the same size, containing the same items
		// in the same order?  We rely on FactoryPathEntry.equals() here.
		List<FactoryPathEntry> newPath = getListContents();
		return !fOriginalPath.equals(newPath);
	}
	
}
