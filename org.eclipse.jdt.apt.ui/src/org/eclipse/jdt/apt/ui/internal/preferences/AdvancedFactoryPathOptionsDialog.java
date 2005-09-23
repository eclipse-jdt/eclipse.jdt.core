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

import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

/**
 * Dialog to display "advanced options" on a FactoryPathEntry,
 * typically in the context of the factory path config UI.
 * Advanced options are those which do not normally need to
 * be configured, and which may require deeper-than-usual
 * understanding of the annotation processing architecture.
 */
public class AdvancedFactoryPathOptionsDialog extends StatusDialog {
	
	private class FieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
		}
	}
	
	// shallow copies, because they are not changed by this code
	private final FactoryContainer _fc;
	private final FactoryPath.Attributes _attr;
	
	private SelectionButtonDialogField _batchModeField;
	
	public AdvancedFactoryPathOptionsDialog(
			Shell parent, FactoryContainer fc, FactoryPath.Attributes attr) {
		super(parent);
		_fc= fc;
		_attr= attr;
		
		setTitle(Messages.AdvancedFactoryPathOptionsDialog_advancedOptions);
		
		FieldAdapter adapter = new FieldAdapter();
		
		_batchModeField = new SelectionButtonDialogField(SWT.CHECK);
		_batchModeField.setSelection(_attr.runInBatchMode());
		_batchModeField.setLabelText(Messages.AdvancedFactoryPathOptionsDialog_batchMode);
		_batchModeField.setDialogFieldListener(adapter);
	}

	protected Control createDialogArea(Composite parent) {
		Composite composite= (Composite) super.createDialogArea(parent);
		
		Composite inner= new Composite(composite, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		inner.setLayout(layout);
		
		_batchModeField.doFillIntoGrid(inner, 2);
		
		// Plugins can't run in APT compatibility mode.
		boolean isPlugin = _fc.getType() == FactoryContainer.FactoryType.PLUGIN;
		_batchModeField.setEnabled(!isPlugin);
		
		applyDialogFont(composite);		
		return composite;
	}
		
	/**
	 * Return a new Attributes representing the original value updated
	 * with any changes made by the user.  Changes will be included even
	 * if the dialog was cancelled, so this should only be called if the
	 * dialog returned OK.
	 */
	public FactoryPath.Attributes getResult() {
		boolean batchMode = _batchModeField.isSelected();
		return new FactoryPath.Attributes(_attr.isEnabled(), batchMode);
	}
}
