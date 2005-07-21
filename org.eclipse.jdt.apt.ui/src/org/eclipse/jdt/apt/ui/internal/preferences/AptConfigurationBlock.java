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
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
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
import org.eclipse.swt.widgets.Label;
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
		fAptEnabledField.setLabelText(Messages.AptConfigurationBlock_enable);
		
		fGenSrcDirField = new StringDialogField();
		fGenSrcDirField.setDialogFieldListener(adapter);
		fGenSrcDirField.setLabelText(Messages.AptConfigurationBlock_generatedSrcDir);
		
		fProcessorOptionsField = new StringDialogField();
		fProcessorOptionsField.setDialogFieldListener(adapter);
		fProcessorOptionsField.setLabelText(Messages.AptConfigurationBlock_options);

		updateControls();
	}
	
	@Override
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

		Label description= new Label(composite, SWT.WRAP);
		description.setText(Messages.AptConfigurationBlock_classpathAddedAutomaticallyNote); 
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 2;
		gd.widthHint= fPixelConverter.convertWidthInCharsToPixels(60);
		description.setLayoutData(gd);

		Dialog.applyDialogFont(composite);
		
		validateSettings(null, null, null);
		
		return composite;
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
		fProcessorOptionsField.setText(str == null ? "" : str); //$NON-NLS-1$
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
			newVal = fProcessorOptionsField.getText();
		}
		if (key != null) {
			String oldVal = setValue(key, newVal);
			validateSettings(key, oldVal, newVal);
		}
	}
	
}


