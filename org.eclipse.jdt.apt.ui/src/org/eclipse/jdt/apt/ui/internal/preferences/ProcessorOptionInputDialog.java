/*******************************************************************************
 * Copyright (c) 2005, 2015 BEA Systems, Inc.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   wharley@bea.com - initial API and implementation
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.ui.internal.preferences.AptConfigurationBlock.ProcessorOption;
import org.eclipse.jdt.apt.ui.internal.util.IAptHelpContextIds;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to edit or add an APT processor option
 */
public class ProcessorOptionInputDialog extends StatusDialog {

	private class FieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
			doValidation();
		}
	}

	private final StringDialogField fKeyField;
	private final StringDialogField fValueField;

	private final List<String> fExistingNames;

	public ProcessorOptionInputDialog(Shell parent, ProcessorOption option, List<ProcessorOption> existingEntries) {
		super(parent);

		fExistingNames= new ArrayList<>(existingEntries.size());
		for (ProcessorOption o : existingEntries) {
			if (!o.equals(option)) {
				fExistingNames.add(o.key);
			}
		}

		if (option == null) {
			setTitle(Messages.ProcessorOptionInputDialog_newProcessorOption);
		} else {
			setTitle(Messages.ProcessorOptionInputDialog_editProcessorOption);
		}

		FieldAdapter adapter= new FieldAdapter();

		fKeyField= new StringDialogField();
		fKeyField.setLabelText(Messages.ProcessorOptionInputDialog_key);
		fKeyField.setDialogFieldListener(adapter);

		fValueField= new StringDialogField();
		fValueField.setLabelText(Messages.ProcessorOptionInputDialog_value);
		fValueField.setDialogFieldListener(adapter);

		fKeyField.setText((option != null) ? option.key : ""); //$NON-NLS-1$
		fValueField.setText((option != null) ? option.value : ""); //$NON-NLS-1$
	}

	public ProcessorOption getResult() {
		ProcessorOption option = new ProcessorOption();
		option.key= fKeyField.getText().trim();
		option.value= fValueField.getText().trim();

		return option;
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);

		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		inner.setLayout(layout);

		fKeyField.doFillIntoGrid(inner, 2);
		fValueField.doFillIntoGrid(inner, 2);

		LayoutUtil.setHorizontalGrabbing(fKeyField.getTextControl(null));
		LayoutUtil.setWidthHint(fKeyField.getTextControl(null), convertWidthInCharsToPixels(50));
		LayoutUtil.setHorizontalGrabbing(fValueField.getTextControl(null));
		LayoutUtil.setWidthHint(fValueField.getTextControl(null), convertWidthInCharsToPixels(50));

		fKeyField.postSetFocusOnDialogField(parent.getDisplay());

		applyDialogFont(composite);
		return composite;
	}

	private void doValidation() {
		StatusInfo status= new StatusInfo();
		String newKey= fKeyField.getText();
		String newVal= fValueField.getText();
		// TODO: thorough validation of both key and value
		if (newKey.length() == 0) {
			status.setError(Messages.ProcessorOptionInputDialog_emptyKey);
		} else if (fExistingNames.contains(newKey)) {
			status.setError(Messages.ProcessorOptionInputDialog_keyAlreadyInUse);
		} else if (newVal.indexOf('=') >= 0) {
			status.setError(Messages.ProcessorOptionInputDialog_equalsSignNotValid);
		} else if (AptConfig.isAutomaticProcessorOption(newKey)) {
			status.setWarning(Messages.AptConfigurationBlock_warningIgnoredOptions);
		}
		updateStatus(status);
	}

	/*
	 * @see org.eclipse.jface.window.Window#configureShell(Shell)
	 */
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(newShell, IAptHelpContextIds.PROCESSOR_OPTION_INPUT_DIALOG);
	}
}
