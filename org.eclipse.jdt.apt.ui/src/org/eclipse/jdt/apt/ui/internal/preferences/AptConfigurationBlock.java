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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.apt.core.util.AptConfig.ProcessorOptionsParser;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.*;
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
 */
public class AptConfigurationBlock extends BaseConfigurationBlock {
		
	private static final Key KEY_APTENABLED= getAptCoreKey(AptPreferenceConstants.APT_ENABLED);
	private static final Key KEY_GENSRCDIR= getAptCoreKey(AptPreferenceConstants.APT_GENSRCDIR);
	private static final Key KEY_PROCESSOROPTIONS= getAptCoreKey(AptPreferenceConstants.APT_PROCESSOROPTIONS);
	
	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_APTENABLED, KEY_GENSRCDIR, KEY_PROCESSOROPTIONS
		};	
	}
	
	private static final int IDX_ADD= 0;
	private static final int IDX_EDIT= 1;
	private static final int IDX_REMOVE= 2;
	
	private SelectionButtonDialogField fAptEnabledField;
	private StringDialogField fGenSrcDirField;
	private ListDialogField fProcessorOptionsField;
	
	private PixelConverter fPixelConverter;
	private Composite fBlockControl;
	
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
			field.enableButton(IDX_EDIT, canEdit(selectedElements));
		}
			
		public void doubleClicked(ListDialogField field) {
			tryToEdit(field);
		}

		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
		}			

		private boolean canEdit(List selectedElements) {
			return selectedElements.size() == 1;
		}
		
		private void tryToEdit(ListDialogField field) {
			List<ProcessorOption> selection= getListSelection();
			if (canEdit(selection)) {
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

	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private List<ProcessorOption> getListElements() {
		return fProcessorOptionsField.getElements();
	}
	
	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
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
	
	public AptConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		
		UpdateAdapter adapter= new UpdateAdapter();
		
		fAptEnabledField= new SelectionButtonDialogField(SWT.CHECK);
		fAptEnabledField.setDialogFieldListener(adapter);
		fAptEnabledField.setLabelText(Messages.AptConfigurationBlock_enable);
		
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
		
		DialogField[] fields = new DialogField[] {
			fAptEnabledField,
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
	
	/**
	 * Call after updating key values, to warn user if new values are invalid.
	 * @param changedKey may be null, e.g. if called from createContents.
	 * @param oldValue may be null
	 * @param newValue may be null
	 */
	@Override
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		IStatus status = null;
		
		if (changedKey == KEY_PROCESSOROPTIONS) {
			status = validateProcessorOptions(newValue);
		}

		if (null != status) {
			fContext.statusChanged(status);
		}
	}	
	
	/**
	 * @param newValue
	 * @return
	 */
	private IStatus validateProcessorOptions(String newValue) {
		if (newValue != null && (newValue.contains("-Aclasspath") || newValue.contains("-Asourcepath"))) { //$NON-NLS-1$ //$NON-NLS-2$
			return new StatusInfo(IStatus.WARNING, Messages.AptConfigurationBlock_warningIgnoredOptions);
		}
		else {
			return new StatusInfo();
		}
	}
	
	/**
	 * Update the UI based on the values presently stored in the keys.
	 */
	@Override
	protected void updateControls() {
		boolean aptEnabled= Boolean.valueOf(getValue(KEY_APTENABLED)).booleanValue();
		fAptEnabledField.setSelection(aptEnabled);
		String str= getValue(KEY_GENSRCDIR);
		fGenSrcDirField.setText(str == null ? "" : str); //$NON-NLS-1$
		str= getValue(KEY_PROCESSOROPTIONS);
		unpackProcessorOptions(str);
	}	
	
	protected final void updateModel(DialogField field) {
		String newVal = null;
		Key key = null;
		
		if (field == fAptEnabledField) {
			key = KEY_APTENABLED;
			newVal = String.valueOf(fAptEnabledField.isSelected());
		} else if (field == fGenSrcDirField) {
			key = KEY_GENSRCDIR;
			newVal = fGenSrcDirField.getText();
		} else if (field == fProcessorOptionsField) {
			key = KEY_PROCESSOROPTIONS;
			newVal = packProcessorOptions();
		}
		if (key != null) {
			String oldVal = setValue(key, newVal);
			validateSettings(key, oldVal, newVal);
		}
	}

	/**
	 * @return all the options, packaged as an apt-style command line ("-Afoo=bar -Abaz=quux").
	 * If there are no options, return null.
	 */
	private String packProcessorOptions() {
		List<ProcessorOption> elements = getListElements();
		Map<String, String> map = new LinkedHashMap<String, String>(elements.size());
		for (ProcessorOption o : elements) {
			map.put(o.key, o.value);
		}
		return AptConfig.serializeProcessorOptions(map);
	}

	/**
	 * Set the processor options list contents by parsing an apt-style
	 * command line ("-Afoo=bar -Abaz=quux")
	 * @param str may be null
	 */
	private void unpackProcessorOptions(String str) {
		List<ProcessorOption> options= new ArrayList<ProcessorOption>();
		if (str != null) {
			ProcessorOptionsParser parser = new ProcessorOptionsParser(str);
			Map<String, String> parsedOptions = parser.parse();
			for (Map.Entry<String, String> entry : parsedOptions.entrySet()) {
				ProcessorOption o = new ProcessorOption();
				o.key = entry.getKey();
				o.value = entry.getValue();
				options.add(o);
			}
		}
		fProcessorOptionsField.setElements(options);
	}

}


