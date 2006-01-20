/*******************************************************************************
 * Copyright (c) 2005 BEA Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BEA Systems Inc. - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.apt.core.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

/**
 * Preference pane for most APT (Java annotation processing) settings.
 * @see org.eclipse.jdt.ui.internal.preferences.TodoTaskConfigurationBlock
 * for the conceptual source of some of this code.
 * <p>
 * 
 */
public class AptConfigurationBlock extends BaseConfigurationBlock {
		
	private static final Key KEY_APTENABLED= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_ENABLED);
	private static final Key KEY_GENSRCDIR= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_GENSRCDIR);
	
	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_APTENABLED, KEY_GENSRCDIR
		};	
	}
	
	private static final int IDX_ADD= 0;
	private static final int IDX_EDIT= 1;
	private static final int IDX_REMOVE= 2;
	
	private final IJavaProject fJProj;

	private SelectionButtonDialogField fAptEnabledField;
	private StringDialogField fGenSrcDirField;
	private ListDialogField fProcessorOptionsField;
	
	private PixelConverter fPixelConverter;
	private Composite fBlockControl;
	
	private Map<String, String> fOriginalProcOptions; // cache of saved values
	private String fOriginalGenSrcDir;
	private boolean fOriginalAptEnabled;
	
	/**
	 * Event handler for Processor Options list control.
	 */
	private class ProcessorOptionsAdapter implements IListAdapter, IDialogFieldListener {
		
		public void customButtonPressed(ListDialogField field, int index) {
			switch (index) {
			case IDX_ADD:
				editOrAddProcessorOption(null);
				break;
			case IDX_EDIT:
				tryToEdit(field);
				break;
			}
		}

		public void selectionChanged(ListDialogField field) {
			List selectedElements= field.getSelectedElements();
			field.enableButton(IDX_EDIT, canEdit(field, selectedElements));
		}
			
		public void doubleClicked(ListDialogField field) {
			tryToEdit(field);
		}

		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
		}			

		private boolean canEdit(DialogField field, List selectedElements) {
			if (!field.isEnabled())
				return false;
			return selectedElements.size() == 1;
		}
		
		private void tryToEdit(ListDialogField field) {
			List<ProcessorOption> selection= getListSelection();
			if (canEdit(field, selection)) {
				editOrAddProcessorOption(selection.get(0));
			}
		}
	}
	
	/**
	 * An entry in the Processor Options list control.
	 */
	public static class ProcessorOption {
		public String key;
		public String value;
	}

	/**
	 * Sorts items in the Processor Options list control.
	 */
	private static class ProcessorOptionSorter extends ViewerSorter {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return collator.compare(((ProcessorOption) e1).key, ((ProcessorOption) e2).key);
		}
	}
	
	/**
	 * Controls display of items in the Processor Options list control.
	 */
	private class ProcessorOptionsLabelProvider extends LabelProvider implements ITableLabelProvider {
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnImage(java.lang.Object, int)
		 */
		public Image getColumnImage(Object element, int columnIndex) {
			return null;
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.jface.viewers.ITableLabelProvider#getColumnText(java.lang.Object, int)
		 */
		public String getColumnText(Object element, int columnIndex) {
			ProcessorOption o = (ProcessorOption) element;
			if (columnIndex == 0) {
				return o.key;
			}
			else if (columnIndex == 1) {
				return o.value;
			}
			else {
				return ""; //$NON-NLS-1$
			}
		}
	}

	public AptConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		
		fJProj = JavaCore.create(project);
		
		UpdateAdapter adapter= new UpdateAdapter();
		
		if (fJProj != null) {
			fAptEnabledField= new SelectionButtonDialogField(SWT.CHECK);
			fAptEnabledField.setDialogFieldListener(adapter);
			fAptEnabledField.setLabelText(Messages.AptConfigurationBlock_enable);
		}
		else {
			fAptEnabledField = null;
		}
		
		fGenSrcDirField = new StringDialogField();
		fGenSrcDirField.setDialogFieldListener(adapter);
		fGenSrcDirField.setLabelText(Messages.AptConfigurationBlock_generatedSrcDir);
		
		String[] buttons= new String[] {
			Messages.AptConfigurationBlock_add,
			Messages.AptConfigurationBlock_edit,
			Messages.AptConfigurationBlock_remove
		};
		ProcessorOptionsAdapter optionsAdapter = new ProcessorOptionsAdapter();
		fProcessorOptionsField = new ListDialogField(optionsAdapter, buttons, new ProcessorOptionsLabelProvider());
		fProcessorOptionsField.setDialogFieldListener(optionsAdapter);
		fProcessorOptionsField.setRemoveButtonIndex(IDX_REMOVE);
		String[] columnHeaders= new String[] {
			Messages.AptConfigurationBlock_key,
			Messages.AptConfigurationBlock_value
		};
		fProcessorOptionsField.setTableColumns(new ListDialogField.ColumnsDescription(columnHeaders, true));
		fProcessorOptionsField.setViewerSorter(new ProcessorOptionSorter());
		fProcessorOptionsField.setLabelText(Messages.AptConfigurationBlock_options);
		
		updateControls();
		
		if (fProcessorOptionsField.getSize() > 0) {
			fProcessorOptionsField.selectFirstElement();
		} else {
			fProcessorOptionsField.enableButton(IDX_EDIT, false);
		}
		
	}
	
	/* 
	 * At workspace level, don't ask for a rebuild.
	 */
	@Override
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		return workspaceSettings ? null : super.getFullBuildDialogStrings(workspaceSettings);
	}

	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
	@SuppressWarnings("unchecked") 
	private List<ProcessorOption> getListElements() {
		return fProcessorOptionsField.getElements();
	}
	
	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
	@SuppressWarnings("unchecked") 
	private List<ProcessorOption> getListSelection() {
		return fProcessorOptionsField.getSelectedElements();
	}
	
	private void editOrAddProcessorOption(ProcessorOption original) {
		ProcessorOptionInputDialog dialog= new ProcessorOptionInputDialog(getShell(), original, getListElements());
		if (dialog.open() == Window.OK) {
			if (original != null) {
				fProcessorOptionsField.replaceElement(original, dialog.getResult());
			} else {
				fProcessorOptionsField.addElement(dialog.getResult());
			}
		}
	}
	
	@Override
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		
		fPixelConverter= new PixelConverter(parent);
		
		fBlockControl = new Composite(parent, SWT.NONE);
		fBlockControl.setFont(parent.getFont());
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		
		fBlockControl.setLayout(layout);
		
		DialogField[] fields = fAptEnabledField != null ? 
				new DialogField[] {
					fAptEnabledField,
					fGenSrcDirField,
					fProcessorOptionsField,
				} :
				new DialogField[] {
					fGenSrcDirField,
					fProcessorOptionsField,
				};
		LayoutUtil.doDefaultLayout(fBlockControl, fields, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(fProcessorOptionsField.getListControl(null));

		Label description= new Label(fBlockControl, SWT.WRAP);
		description.setText(Messages.AptConfigurationBlock_classpathAddedAutomaticallyNote); 
		GridData gdLabel= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gdLabel.horizontalSpan= 2;
		gdLabel.widthHint= fPixelConverter.convertWidthInCharsToPixels(60);
		description.setLayoutData(gdLabel);
		
		Dialog.applyDialogFont(fBlockControl);
		
		validateSettings(null, null, null);
		
		return fBlockControl;
	}
	
	@Override
	protected void cacheOriginalValues() {
		super.cacheOriginalValues();
		fOriginalProcOptions= AptConfig.getRawProcessorOptions(fJProj);
		fOriginalGenSrcDir = AptConfig.getGenSrcDir(fJProj);
		fOriginalAptEnabled = AptConfig.isEnabled(fJProj);
	}

	protected void initContents() {
		loadProcessorOptions(fJProj);
	}

	@Override
	protected void saveSettings() {
		List<ProcessorOption> elements;
		if ((fJProj != null) && !fBlockControl.isEnabled()) {
			// We're in a project properties pane but the entire configuration 
			// block control is disabled.  That means the per-project settings checkbox 
			// is unchecked.  To save that state, we'll clear the proc options map.
			elements = Collections.<ProcessorOption>emptyList();
		}
		else {
			elements = getListElements();
		}
		saveProcessorOptions(elements);
		super.saveSettings();
		if (!fOriginalGenSrcDir.equals(fGenSrcDirField.getText()))
			fAptProject.preferenceChanged(AptPreferenceConstants.APT_GENSRCDIR);
		if (fOriginalAptEnabled != fAptEnabledField.isSelected())
			fAptProject.preferenceChanged(AptPreferenceConstants.APT_ENABLED);
	}

	/**
	 * Check whether any processor options have changed, as well as
	 * any of the settings tracked in the "normal" way (as Keys).
	 */
	@Override
	protected boolean settingsChanged(IScopeContext currContext) {
		Map<String, String> savedProcOptions = new HashMap<String, String>(fOriginalProcOptions);
		for (ProcessorOption o : getListElements()) {
			final String savedVal = savedProcOptions.get(o.key);
			if (savedVal != null && savedVal.equals(o.value)) {
				savedProcOptions.remove(o.key);
			}
			else {
				// found an unsaved option in the list
				return true;
			}
		}
		if (!savedProcOptions.isEmpty()) {
			// found a saved option that has been removed
			return true;
		}
		return super.settingsChanged(currContext);
	}

	/**
	 * Call after updating key values, to warn user if new values are invalid.
	 * @param changedKey may be null, e.g. if called from createContents.
	 * @param oldValue may be null
	 * @param newValue may be null
	 */
	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		IStatus status = null;
		
		status = validateGenSrcDir();
		if (status.getSeverity() == IStatus.OK) {
			status = validateProcessorOptions();
		}

		fContext.statusChanged(status);
	}	
	
	/**
	 * Validate "generated source directory" setting.  It must be a valid
	 * pathname relative to a project, and must not be a source directory.
	 * @return
	 */
	private IStatus validateGenSrcDir() {
		String dirName = fGenSrcDirField.getText();
		if (!AptConfig.validateGenSrcDir(fJProj, dirName)) {
			return new StatusInfo(IStatus.ERROR, Messages.AptConfigurationBlock_genSrcDirMustBeValidRelativePath);
		}
		return new StatusInfo();
	}

	/**
	 * Validate the currently set processor options.  We do this by
	 * looking at the table contents rather than the packed string,
	 * just because it's easier.
	 * @return a StatusInfo containing a warning if appropriate.
	 */
	private IStatus validateProcessorOptions() {
		List<ProcessorOption> elements = getListElements();
		for (ProcessorOption o : elements) {
			if (AptConfig.isAutomaticProcessorOption(o.key)) {
				return new StatusInfo(IStatus.WARNING, 
						Messages.AptConfigurationBlock_warningIgnoredOptions + ": " + o.key); //$NON-NLS-1$
			}
		}
		return new StatusInfo();
	}
	
	/**
	 * Update the UI based on the values presently stored in the keys.
	 */
	@Override
	protected void updateControls() {
		if (fAptEnabledField != null) {
			boolean aptEnabled= Boolean.valueOf(getValue(KEY_APTENABLED)).booleanValue();
			fAptEnabledField.setSelection(aptEnabled);
		}
		String str= getValue(KEY_GENSRCDIR);
		fGenSrcDirField.setText(str == null ? "" : str); //$NON-NLS-1$
	}	
	
	/**
	 * Update the values stored in the keys based on the UI.
	 */
	protected final void updateModel(DialogField field) {
		
		if (fAptEnabledField != null && field == fAptEnabledField) {
			String newVal = String.valueOf(fAptEnabledField.isSelected());
			setValue(KEY_APTENABLED, newVal);
		} else if (field == fGenSrcDirField) {
			String newVal = fGenSrcDirField.getText();
			setValue(KEY_GENSRCDIR, newVal);
		} 
		validateSettings(null, null, null); // params are ignored
	}

	/**
	 * Save the contents of the options list.
	 */
	private void saveProcessorOptions(List<ProcessorOption> elements) {
		Map<String, String> map = new LinkedHashMap<String, String>(elements.size());
		for (ProcessorOption o : elements) {
			map.put(o.key, o.value);
		}
		AptConfig.setProcessorOptions(map, fJProj);
	}

	/**
	 * Set the processor options list contents
	 */
	private void loadProcessorOptions(IJavaProject jproj) {
		List<ProcessorOption> options= new ArrayList<ProcessorOption>();
		Map<String, String> parsedOptions = AptConfig.getRawProcessorOptions(jproj);
		for (Map.Entry<String, String> entry : parsedOptions.entrySet()) {
			ProcessorOption o = new ProcessorOption();
			o.key = entry.getKey();
			o.value = entry.getValue();
			options.add(o);
		}
		fProcessorOptionsField.setElements(options);
	}

	@Override
	public void performDefaults() {
		if (fJProj != null) {
			// If project-specific, load workspace settings
			loadProcessorOptions(null);
		}
		else {
			// If workspace, load "factory default," which is empty.
			fProcessorOptionsField.removeAllElements();
		}
		super.performDefaults();
	}

}


