/*******************************************************************************
 * Copyright (c) 2005, 2018 BEA Systems, Inc. and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     BEA Systems Inc. - initial API and implementation
 *     IBM Corporation  - fix deprecation warnings
 *     Fabian Steeg <steeg@hbz-nrw.de> - Update APT options documentation - https://bugs.eclipse.org/515329
 *******************************************************************************/
package org.eclipse.jdt.apt.ui.internal.preferences;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.util.AptConfig;
import org.eclipse.jdt.apt.core.util.AptPreferenceConstants;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.dialogs.StatusInfo;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IListAdapter;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.ListDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.SelectionButtonDialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.PixelConverter;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.osgi.service.prefs.BackingStoreException;


/**
 * Preference pane for most APT (Java annotation processing) settings.
 * see org.eclipse.jdt.ui.internal.preferences.TodoTaskConfigurationBlock
 * for the conceptual source of some of this code.
 */
public class AptConfigurationBlock extends BaseConfigurationBlock {

	private static final Key KEY_APTENABLED= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_ENABLED);
	private static final Key KEY_RECONCILEENABLED= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_RECONCILEENABLED);
	private static final Key KEY_GENSRCDIR= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_GENSRCDIR);
	private static final Key KEY_GENTESTSRCDIR= getKey(AptPlugin.PLUGIN_ID, AptPreferenceConstants.APT_GENTESTSRCDIR);

	private static Key[] getAllKeys() {
		return new Key[] {
				KEY_APTENABLED, KEY_RECONCILEENABLED, KEY_GENSRCDIR
		};
	}

	private static final int IDX_ADD= 0;
	private static final int IDX_EDIT= 1;
	private static final int IDX_REMOVE= 2;

	private final IJavaProject fJProj;

	private SelectionButtonDialogField fAptEnabledField;
	private final SelectionButtonDialogField fReconcileEnabledField;
	private final StringDialogField fGenSrcDirField;
	private final StringDialogField fGenTestSrcDirField;
	private final ListDialogField<ProcessorOption> fProcessorOptionsField;

	private PixelConverter fPixelConverter;
	private Composite fBlockControl;

	private Map<String, String> fOriginalProcOptions; // cache of saved values
	private String fOriginalGenSrcDir;
	private String fOriginalGenTestSrcDir;
	private boolean fOriginalAptEnabled;
	private boolean fOriginalReconcileEnabled;

	// used to distinguish actual changes from re-setting of same value - see useProjectSpecificSettings()
	private boolean fPerProjSettingsEnabled;

	/**
	 * Event handler for Processor Options list control.
	 */
	private class ProcessorOptionsAdapter implements IListAdapter<ProcessorOption>, IDialogFieldListener {

		public void customButtonPressed(ListDialogField<ProcessorOption> field, int index) {
			switch (index) {
			case IDX_ADD:
				editOrAddProcessorOption(null);
				break;
			case IDX_EDIT:
				tryToEdit(field);
				break;
			}
		}

		public void selectionChanged(ListDialogField<ProcessorOption> field) {
			List<ProcessorOption> selectedElements= field.getSelectedElements();
			field.enableButton(IDX_EDIT, canEdit(field, selectedElements));
		}

		public void doubleClicked(ListDialogField<ProcessorOption> field) {
			tryToEdit(field);
		}

		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
		}

		private boolean canEdit(DialogField field, List<ProcessorOption> selectedElements) {
			if (!field.isEnabled())
				return false;
			return selectedElements.size() == 1;
		}

		private void tryToEdit(ListDialogField<ProcessorOption> field) {
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
	private static class ProcessorOptionSorter extends ViewerComparator {
		public int compare(Viewer viewer, Object e1, Object e2) {
			return getComparator().compare(((ProcessorOption) e1).key, ((ProcessorOption) e2).key);
		}
	}

	/**
	 * Controls display of items in the Processor Options list control.
	 */
	private static class ProcessorOptionsLabelProvider extends LabelProvider implements ITableLabelProvider {

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

		fReconcileEnabledField= new SelectionButtonDialogField(SWT.CHECK);
		fReconcileEnabledField.setDialogFieldListener(adapter);
		fReconcileEnabledField.setLabelText(Messages.AptConfigurationBlock_enableReconcileProcessing);

		fGenSrcDirField = new StringDialogField();
		fGenSrcDirField.setDialogFieldListener(adapter);
		fGenSrcDirField.setLabelText(Messages.AptConfigurationBlock_generatedSrcDir);

		fGenTestSrcDirField = new StringDialogField();
		fGenTestSrcDirField.setDialogFieldListener(adapter);
		fGenTestSrcDirField.setLabelText(Messages.AptConfigurationBlock_generatedTestSrcDir);

		String[] buttons= new String[] {
			Messages.AptConfigurationBlock_add,
			Messages.AptConfigurationBlock_edit,
			Messages.AptConfigurationBlock_remove
		};
		ProcessorOptionsAdapter optionsAdapter = new ProcessorOptionsAdapter();
		fProcessorOptionsField = new ListDialogField<>(optionsAdapter, buttons, new ProcessorOptionsLabelProvider());
		fProcessorOptionsField.setDialogFieldListener(optionsAdapter);
		fProcessorOptionsField.setRemoveButtonIndex(IDX_REMOVE);
		String[] columnHeaders= new String[] {
			Messages.AptConfigurationBlock_key,
			Messages.AptConfigurationBlock_value
		};
		fProcessorOptionsField.setTableColumns(new ListDialogField.ColumnsDescription(columnHeaders, true));
		fProcessorOptionsField.setViewerComparator(new ProcessorOptionSorter());
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
		if (workspaceSettings)
			return null;
		// if the only thing that changed was the reconcile setting, return null: a rebuild is not necessary
		if (fOriginalGenSrcDir.equals(fGenSrcDirField.getText())
				&& fOriginalGenTestSrcDir.equals(fGenTestSrcDirField.getText())) {
			if (fOriginalAptEnabled == fAptEnabledField.isSelected()) {
				if (!procOptionsChanged()) {
					return null;
				}
			}
		}
		return super.getFullBuildDialogStrings(workspaceSettings);
	}

	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
	private List<ProcessorOption> getListElements() {
		return fProcessorOptionsField.getElements();
	}

	/*
	 * Helper to eliminate unchecked-conversion warning
	 */
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
		int indent= fPixelConverter.convertWidthInCharsToPixels(4);

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
					fReconcileEnabledField,
					fGenSrcDirField,
					fGenTestSrcDirField,
					fProcessorOptionsField,
				} :
				new DialogField[] {
					fReconcileEnabledField,
					fGenSrcDirField,
					fGenTestSrcDirField,
					fProcessorOptionsField,
				};
		LayoutUtil.doDefaultLayout(fBlockControl, fields, true, SWT.DEFAULT, SWT.DEFAULT);
		LayoutUtil.setHorizontalGrabbing(fProcessorOptionsField.getListControl(null));

		GridData reconcileGD= (GridData)fReconcileEnabledField.getSelectionButton(parent).getLayoutData();
		reconcileGD.horizontalIndent = indent;
		fReconcileEnabledField.getSelectionButton(parent).setLayoutData(reconcileGD);

		Dialog.applyDialogFont(fBlockControl);

		validateSettings(null, null, null);

		return fBlockControl;
	}

	@Override
	protected void cacheOriginalValues() {
		super.cacheOriginalValues();
		fOriginalProcOptions= AptConfig.getRawProcessorOptions(fJProj);
		fOriginalGenSrcDir = AptConfig.getGenSrcDir(fJProj);
		fOriginalGenTestSrcDir = AptConfig.getGenTestSrcDir(fJProj);
		fOriginalAptEnabled = AptConfig.isEnabled(fJProj);
		fOriginalReconcileEnabled = AptConfig.shouldProcessDuringReconcile(fJProj);
		fPerProjSettingsEnabled = hasProjectSpecificOptionsNoCache(fProject);
	}

	@Override
	protected void initContents() {
		loadProcessorOptions(fJProj);
	}

	@Override
	protected void saveSettings() {
		List<ProcessorOption> elements;
		boolean isProjSpecificDisabled = (fJProj != null) && !fBlockControl.isEnabled();
		if (isProjSpecificDisabled) {
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
		if (null != fAptProject) {
			if (isProjSpecificDisabled) { // compare against workspace defaults
				if (!fOriginalGenSrcDir.equals(AptConfig.getGenSrcDir(null))) {
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_GENSRCDIR);
				}
				if (!fOriginalGenTestSrcDir.equals(AptConfig.getGenTestSrcDir(null))) {
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_GENTESTSRCDIR);
				}
				if (fOriginalAptEnabled != AptConfig.isEnabled(null)) {
					// make JDT "processingEnabled" setting track APT "enabled" setting.
					setJDTProcessAnnotationsSetting(fAptEnabledField.isSelected());

					fAptProject.preferenceChanged(AptPreferenceConstants.APT_ENABLED);
				}
				if (fOriginalReconcileEnabled != AptConfig.shouldProcessDuringReconcile(null)) {
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_RECONCILEENABLED);
				}
			}
			else { // compare against current settings
				if (!fOriginalGenSrcDir.equals(fGenSrcDirField.getText()))
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_GENSRCDIR);
				if (!fOriginalGenTestSrcDir.equals(fGenTestSrcDirField.getText()))
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_GENTESTSRCDIR);
				boolean isAptEnabled = fAptEnabledField.isSelected();
				if (fOriginalAptEnabled != isAptEnabled) {
					// make JDT "processingEnabled" setting track APT "enabled" setting.
					setJDTProcessAnnotationsSetting(isAptEnabled);

					fAptProject.preferenceChanged(AptPreferenceConstants.APT_ENABLED);
				}
				if (fOriginalReconcileEnabled != fReconcileEnabledField.isSelected())
					fAptProject.preferenceChanged(AptPreferenceConstants.APT_RECONCILEENABLED);
			}
		}
	}

	/**
	 * Set the org.eclipse.jdt.core.compiler.processAnnotations setting.
	 * In Eclipse 3.3, this value replaces org.eclipse.jdt.apt.aptEnabled,
	 * but we continue to set both values in order to ensure backward
	 * compatibility with prior versions.
	 * the aptEnabled setting.
	 */
	private void setJDTProcessAnnotationsSetting(boolean enable) {
		IScopeContext context = (null != fJProj) ?
				new ProjectScope(fJProj.getProject()) : InstanceScope.INSTANCE;
		IEclipsePreferences node = context.getNode(JavaCore.PLUGIN_ID);
		final String value = enable ? AptPreferenceConstants.ENABLED : AptPreferenceConstants.DISABLED;
		node.put(AptPreferenceConstants.APT_PROCESSANNOTATIONS, value);
		try {
			node.flush();
		}
		catch (BackingStoreException e){
			AptPlugin.log(e, "Failed to save preference: " + AptPreferenceConstants.APT_PROCESSANNOTATIONS); //$NON-NLS-1$
		}
	}

	/**
	 * Check whether any processor options have changed.
	 * @return true if they did.
	 */
	private boolean procOptionsChanged() {
		Map<String, String> savedProcOptions = new HashMap<>(fOriginalProcOptions);
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
		return false;
	}

	/**
	 * Check whether any processor options have changed, as well as
	 * any of the settings tracked in the "normal" way (as Keys).
	 */
	@Override
	protected boolean settingsChanged(IScopeContext currContext) {
		if (procOptionsChanged())
			return true;
		else
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
			status = validateGenTestSrcDir();
		}

		if (status.getSeverity() == IStatus.OK) {
			status = validateProcessorOptions();
		}

		fContext.statusChanged(status);
	}

	/**
	 * Validate "generated source directory" setting.  It must be a valid
	 * pathname relative to a project, and must not be a source directory.
	 * @return true if current field value is valid
	 */
	private IStatus validateGenSrcDir() {
		String dirName = fGenSrcDirField.getText();
		if (!AptConfig.validateGenSrcDir(fJProj, dirName)) {
			return new StatusInfo(IStatus.ERROR, Messages.AptConfigurationBlock_genSrcDirMustBeValidRelativePath);
		}
		if (fJProj != null && !dirName.equals(fOriginalGenSrcDir)) {
			IFolder folder = fJProj.getProject().getFolder( dirName );
			if (folder != null && folder.exists() && !folder.isDerived()) {
				return new StatusInfo(IStatus.WARNING, Messages.AptConfigurationBlock_warningContentsMayBeDeleted);
			}
		}
		return new StatusInfo();
	}

	/**
	 * Validate "generated test source directory" setting.  It must be a valid
	 * pathname relative to a project, and must not be a source directory.
	 * @return true if current field value is valid
	 */
	private IStatus validateGenTestSrcDir() {
		String dirName = fGenSrcDirField.getText();
		String testDirName = fGenTestSrcDirField.getText();
		if (!AptConfig.validateGenSrcDir(fJProj, testDirName)) {
			return new StatusInfo(IStatus.ERROR, Messages.AptConfigurationBlock_genTestSrcDirMustBeValidRelativePath);
		}
		if (testDirName.equals(dirName)) {
			return new StatusInfo(IStatus.ERROR, Messages.AptConfigurationBlock_genTestSrcDirMustBeDifferent);
		}
		if (fJProj != null && !testDirName.equals(fOriginalGenTestSrcDir)) {
			IFolder folder = fJProj.getProject().getFolder( testDirName );
			if (folder != null && folder.exists() && !folder.isDerived()) {
				return new StatusInfo(IStatus.WARNING, Messages.AptConfigurationBlock_warningContentsMayBeDeleted);
			}
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
			boolean aptEnabled= Boolean.parseBoolean(getValue(KEY_APTENABLED));
			fAptEnabledField.setSelection(aptEnabled);
		}
		boolean reconcileEnabled= Boolean.parseBoolean(getValue(KEY_RECONCILEENABLED));
		fReconcileEnabledField.setSelection(reconcileEnabled);
		String str= getValue(KEY_GENSRCDIR);
		fGenSrcDirField.setText(str == null ? "" : str); //$NON-NLS-1$
		String teststr= getValue(KEY_GENTESTSRCDIR);
		fGenTestSrcDirField.setText(teststr == null ? "" : teststr); //$NON-NLS-1$
	}

	/**
	 * Update the values stored in the keys based on the UI.
	 */
	@Override
	protected final void updateModel(DialogField field) {

		if (fAptEnabledField != null && field == fAptEnabledField) {
			String newVal = String.valueOf(fAptEnabledField.isSelected());
			setValue(KEY_APTENABLED, newVal);
		} else if (field == fGenSrcDirField) {
			String newVal = fGenSrcDirField.getText();
			setValue(KEY_GENSRCDIR, newVal);
		} else if (field == fGenTestSrcDirField) {
			String newVal = fGenTestSrcDirField.getText();
			setValue(KEY_GENTESTSRCDIR, newVal);
		} else if (field == fReconcileEnabledField) {
			String newVal = String.valueOf(fReconcileEnabledField.isSelected());
			setValue(KEY_RECONCILEENABLED, newVal);
		}
		validateSettings(null, null, null); // params are ignored
	}

	/**
	 * Bugzilla 136498: when project-specific settings are enabled, force APT to be enabled.
	 */
	@Override
	public void useProjectSpecificSettings(boolean enable) {
		super.useProjectSpecificSettings(enable);
		if (enable ^ fPerProjSettingsEnabled) {
			fAptEnabledField.setSelection(enable);
			fPerProjSettingsEnabled = enable;
		}
	}

	/**
	 * Save the contents of the options list.
	 */
	private void saveProcessorOptions(List<ProcessorOption> elements) {
		Map<String, String> map = new LinkedHashMap<>(elements.size());
		for (ProcessorOption o : elements) {
			map.put(o.key, (o.value.length() > 0) ? o.value : null);
		}
		AptConfig.setProcessorOptions(map, fJProj);
	}

	/**
	 * Set the processor options list contents
	 */
	private void loadProcessorOptions(IJavaProject jproj) {
		List<ProcessorOption> options= new ArrayList<>();
		Map<String, String> parsedOptions = AptConfig.getRawProcessorOptions(jproj);
		for (Map.Entry<String, String> entry : parsedOptions.entrySet()) {
			ProcessorOption o = new ProcessorOption();
			o.key = entry.getKey();
			if (o.key == null || o.key.length() < 1) {
				// Don't allow defective entries
				continue;
			}
			o.value = (entry.getValue() == null) ? "" : entry.getValue(); //$NON-NLS-1$
			options.add(o);
		}
		fProcessorOptionsField.setElements(options);
	}

	@Override
	public void performDefaults() {
		fPerProjSettingsEnabled = false;
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


