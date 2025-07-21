/*******************************************************************************
 * Copyright (c) 2005, 2007 BEA Systems, Inc and others.
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
 *   Fabian Steeg <steeg@hbz-nrw.de> - Update APT options documentation - https://bugs.eclipse.org/515329
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import java.io.IOException;
import java.util.Map.Entry;
import org.eclipse.jdt.apt.core.internal.util.FactoryContainer;
import org.eclipse.jdt.apt.core.internal.util.FactoryPath;
import org.eclipse.jdt.apt.ui.internal.util.ExceptionHandler;
import org.eclipse.jdt.apt.ui.internal.util.IAptHelpContextIds;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;

/**
 * Dialog to display "advanced options" on a FactoryPathEntry,
 * typically in the context of the factory path config UI.
 * Advanced options are those which do not normally need to
 * be configured, and which may require deeper-than-usual
 * understanding of the annotation processing architecture.
 */
public class AdvancedFactoryPathOptionsDialog extends StatusDialog {

	private final static int LIST_WIDTH= 70; // width (in chars) of factory list
	private final static int LIST_HEIGHT= 10; // number of lines in factory list

	private static class FieldAdapter implements IDialogFieldListener {
		public void dialogFieldChanged(DialogField field) {
		}
	}

	// shallow copies, because they are not changed by this code
	private final FactoryContainer _fc;
	private final FactoryPath.Attributes _attr;

	// Dialog controls
	private SelectionButtonDialogField _batchModeField;
	private ListViewer _contentsField;

	public AdvancedFactoryPathOptionsDialog(
			Shell parent, FactoryContainer fc, FactoryPath.Attributes attr) {
		super(parent);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		_fc= fc;
		_attr= attr;
	}

    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText(Messages.AdvancedFactoryPathOptionsDialog_advancedOptions);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(shell, IAptHelpContextIds.ADVANCED_FACTORYPATH_OPTIONS_DIALOG);
    }

	protected Control createDialogArea(Composite parent) {
		Composite dlgArea= (Composite) super.createDialogArea(parent);

		// Set up "batch mode" checkbox.
		FieldAdapter adapter = new FieldAdapter();
		_batchModeField = new SelectionButtonDialogField(SWT.CHECK);
		_batchModeField.setSelection(_attr.runInBatchMode());
		_batchModeField.setLabelText(Messages.AdvancedFactoryPathOptionsDialog_batchMode);
		_batchModeField.setDialogFieldListener(adapter);
		_batchModeField.doFillIntoGrid(dlgArea, 2);
			// Plugins can't run in APT compatibility mode.
		boolean isPlugin = _fc.getType() == FactoryContainer.FactoryType.PLUGIN;
		_batchModeField.setEnabled(!isPlugin);

		DialogField.createEmptySpace(dlgArea, 1);

		// Set up label for processor contents list
		Label description= new Label(dlgArea, SWT.WRAP);
		description.setText(Messages.AdvancedFactoryPathOptionsDialog_label_processorsInThisContainer);
		GridData gdLabel= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gdLabel.horizontalSpan= 2;
		description.setLayoutData(gdLabel);

		// Set up processor contents list
		_contentsField= new ListViewer(dlgArea, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
        data.heightHint = convertHeightInCharsToPixels(LIST_HEIGHT);
        data.widthHint = convertWidthInCharsToPixels(LIST_WIDTH);
        _contentsField.getList().setLayoutData(data);
        _contentsField.getList().setFont(parent.getFont());
        try {
	        for (Entry<String, String> entry : _fc.getFactoryNames().entrySet()) {
	        	String name = entry.getKey();
	        	_contentsField.add(name);
	        	//TODO: display the processor type (i.e., entry.getValue())
	        }
        }
        catch (IOException e) {
			final String message = "Unable to load factory names from container [" + _fc.getId() + "]"; //$NON-NLS-1$ //$NON-NLS-2$
			ExceptionHandler.log(e, message);
        }
        _contentsField.setSelection(null, false);

		applyDialogFont(dlgArea);
		return dlgArea;
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
