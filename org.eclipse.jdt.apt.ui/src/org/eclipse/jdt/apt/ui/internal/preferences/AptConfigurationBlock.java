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

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.internal.ui.util.PixelConverter;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

public class AptConfigurationBlock extends BaseConfigurationBlock {
		
	private static final Key KEY_APTENABLED= getAptCoreKey(AptPreferenceConstants.APT_ENABLED);
	private static final Key KEY_GENSRCDIR= getAptCoreKey(AptPreferenceConstants.APT_GENSRCDIR);
	private static final Key KEY_PROCESSOROPTIONS= getAptCoreKey(AptPreferenceConstants.APT_PROCESSOROPTIONS);
	
	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_APTENABLED, KEY_GENSRCDIR, KEY_PROCESSOROPTIONS
		};	
	}
	
	private SelectionButtonDialogField fAptEnabledField;
	private StringDialogField fGenSrcDirField;
	private StringDialogField fProcessorOptionsField;
	
	private PixelConverter fPixelConverter;
	
	public AptConfigurationBlock(IStatusChangeListener context, IProject project, IWorkbenchPreferenceContainer container) {
		super(context, project, getAllKeys(), container);
		
		UpdateAdapter adapter= new UpdateAdapter();
		
		fAptEnabledField= new SelectionButtonDialogField(SWT.CHECK);
		fAptEnabledField.setDialogFieldListener(adapter);
		fAptEnabledField.setLabelText("Enable annotation processing"); 
		
		fGenSrcDirField = new StringDialogField();
		fGenSrcDirField.setDialogFieldListener(adapter);
		fGenSrcDirField.setLabelText("Generated source directory:");
		
		fProcessorOptionsField = new StringDialogField();
		fProcessorOptionsField.setDialogFieldListener(adapter);
		fProcessorOptionsField.setLabelText("Processor options (-Akey=val):");

		updateControls();
	}
	
	protected Control createContents(Composite parent) {
		setShell(parent.getShell());
		
		fPixelConverter= new PixelConverter(parent);
		
		Composite composite= new Composite(parent, SWT.NONE);
		composite.setFont(parent.getFont());
		
		GridLayout layout= new GridLayout();
		layout.numColumns= 2;
		layout.marginWidth= 0;
		layout.marginHeight= 0;
		
		composite.setLayout(layout);
		
		fAptEnabledField.doFillIntoGrid(composite, 2);
		
		fGenSrcDirField.doFillIntoGrid(composite, 2);
		((GridData) fGenSrcDirField.getTextControl(null).getLayoutData()).grabExcessHorizontalSpace= true;
		
		fProcessorOptionsField.doFillIntoGrid(composite, 2);
		((GridData) fProcessorOptionsField.getTextControl(null).getLayoutData()).grabExcessHorizontalSpace= true;

		Dialog.applyDialogFont(composite);
		
		return composite;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#validateSettings(java.lang.String, java.lang.String)
	 */
	protected void validateSettings(Key changedKey, String oldValue, String newValue) {
		// no validation
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.jdt.internal.ui.preferences.OptionsConfigurationBlock#updateControls()
	 */
	protected void updateControls() {
		boolean aptEnabled= Boolean.valueOf(getValue(KEY_APTENABLED)).booleanValue();
		fAptEnabledField.setSelection(aptEnabled);
		String str= getValue(KEY_GENSRCDIR);
		fGenSrcDirField.setText(str == null ? "" : str);
		str= getValue(KEY_PROCESSOROPTIONS);
		fProcessorOptionsField.setText(str == null ? "" : str);
	}	
	
	protected final void updateModel(DialogField field) {

		if (field == fAptEnabledField) {
			setValue(KEY_APTENABLED, fAptEnabledField.isSelected());
		} else if (field == fGenSrcDirField) {
			setValue(KEY_GENSRCDIR, fGenSrcDirField.getText());
		} else if (field == fProcessorOptionsField) {
			setValue(KEY_PROCESSOROPTIONS, fProcessorOptionsField.getText());
		}
	}
	
}


