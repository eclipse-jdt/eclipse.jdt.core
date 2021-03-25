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
 *   IBM Corporation - fix deprecation warnings
 *******************************************************************************/

package org.eclipse.jdt.apt.ui.internal.preferences;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.apt.core.internal.AptPlugin;
import org.eclipse.jdt.apt.core.internal.AptProject;
import org.eclipse.jdt.apt.ui.internal.util.ExceptionHandler;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jdt.internal.ui.preferences.ScrolledPageContent;
import org.eclipse.jdt.internal.ui.util.CoreUtility;
import org.eclipse.jdt.internal.ui.wizards.IStatusChangeListener;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.jdt.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Scrollable;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.events.ExpansionAdapter;
import org.eclipse.ui.forms.events.ExpansionEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.preferences.WorkingCopyManager;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;
import org.eclipse.ui.preferences.IWorkingCopyManager;
import org.osgi.service.prefs.BackingStoreException;

/**
 * The ConfigurationBlock hierarchy is used to organize controls and keys
 * within a property/preference page.  The implementor derives from this
 * class and creates dialog controls, layout, and response code.
 * <p>
 * This code is largely a copy of OptionsConfigurationBlock (JDT UI), modified
 * to fix bugs and to improve extensibility for preference pages that contain
 * a mix of preference-based and externally serialized data.
 */
public abstract class BaseConfigurationBlock {

	protected static class ControlData {
		private Key fKey;
		private String[] fValues;

		public ControlData(Key key, String[] values) {
			fKey= key;
			fValues= values;
		}

		public Key getKey() {
			return fKey;
		}

		public int getSelection(String value) {
			if (value != null) {
				for (int i= 0; i < fValues.length; i++) {
					if (value.equals(fValues[i])) {
						return i;
					}
				}
			}
			return fValues.length -1; // assume the last option is the least severe
		}

		public String getValue(boolean selection) {
			int index= selection ? 0 : 1;
			return fValues[index];
		}

		public String getValue(int index) {
			return fValues[index];
		}
	}

	public static final class Key {

		private String fKey;
		private String fQualifier;

		public Key(String qualifier, String key) {
			fQualifier= qualifier;
			fKey= key;
		}

		public String getName() {
			return fKey;
		}

		private IEclipsePreferences getNode(IScopeContext context, IWorkingCopyManager manager) {
			IEclipsePreferences node= context.getNode(fQualifier);
			if (manager != null) {
				return manager.getWorkingCopy(node);
			}
			return node;
		}

		public String getQualifier() {
			return fQualifier;
		}

		public String getStoredValue(IScopeContext context, IWorkingCopyManager manager) {
			return getNode(context, manager).get(fKey, null);
		}

		public String getStoredValue(IScopeContext[] lookupOrder, boolean ignoreTopScope, IWorkingCopyManager manager) {
			for (int i= ignoreTopScope ? 1 : 0; i < lookupOrder.length; i++) {
				String value= getStoredValue(lookupOrder[i], manager);
				if (value != null) {
					return value;
				}
			}
			return null;
		}

		public void setStoredValue(IScopeContext context, String value, IWorkingCopyManager manager) {
			if (value != null) {
				getNode(context, manager).put(fKey, value);
			} else {
				getNode(context, manager).remove(fKey);
			}
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#toString()
		 */
		@Override
		public String toString() {
			return fQualifier + '/' + fKey;
		}

	}

	protected class UpdateAdapter implements IDialogFieldListener {

		public void dialogFieldChanged(DialogField field) {
			updateModel(field);
		}
	}

	private static final String SETTINGS_EXPANDED= "expanded"; //$NON-NLS-1$

	protected final Key[] fAllKeys;
	private boolean fOriginallyHadProjectSettings; // updated in cacheOriginalValues
	private Map<Key, String> fDisabledProjectSettings; // null when project specific settings are turned off
	protected IScopeContext[] fLookupOrder;
	protected final IWorkingCopyManager fManager;

	protected final ArrayList<Button> fCheckBoxes;
	protected final ArrayList<Combo> fComboBoxes;
	protected final ArrayList<ExpandableComposite> fExpandedComposites;
	protected final HashMap<Scrollable, Label> fLabels;
	protected final ArrayList<Text> fTextBoxes;

	private ModifyListener fTextModifyListener;
	protected IStatusChangeListener fContext;
	private SelectionListener fSelectionListener;

	protected final IProject fProject; // project or null
	protected final AptProject fAptProject; // null for workspace prefs

	private IWorkbenchPreferenceContainer fContainer;
	private Shell fShell;

	private Control fBlockControl;

	protected static Key getKey(String plugin, String name) {
		return new Key(plugin, name);
	}

	public BaseConfigurationBlock(IStatusChangeListener context, IProject project, Key[] keys, IWorkbenchPreferenceContainer container) {
		fContext= context;
		fProject= project;
		fAllKeys= keys;
		fContainer= container;
		/*
		if (container == null) {
			fManager= new WorkingCopyManager();
		} else {
			fManager= container.getWorkingCopyManager();
		}
		*/
		// Workaround for Bugzilla 115731 - always use our own WCM.
		fManager = new WorkingCopyManager();

		if (fProject != null) {
			fLookupOrder= new IScopeContext[] {
				new ProjectScope(fProject),
				InstanceScope.INSTANCE,
				DefaultScope.INSTANCE
			};
			fAptProject = AptPlugin.getAptProject(JavaCore.create(fProject));
		} else {
			fLookupOrder= new IScopeContext[] {
				InstanceScope.INSTANCE,
				DefaultScope.INSTANCE
			};
			fAptProject = null;
		}

		testIfOptionsComplete(keys);
		if (fProject == null || hasProjectSpecificOptionsNoCache(fProject)) {
			fDisabledProjectSettings= null;
		} else {
			fDisabledProjectSettings= new IdentityHashMap<>();
			for (int i= 0; i < keys.length; i++) {
				Key curr= keys[i];
				fDisabledProjectSettings.put(curr, curr.getStoredValue(fLookupOrder, false, fManager));
			}
		}

		settingsUpdated();

		fCheckBoxes= new ArrayList<>();
		fComboBoxes= new ArrayList<>();
		fTextBoxes= new ArrayList<>(2);
		fLabels= new HashMap<>();
		fExpandedComposites= new ArrayList<>();
	}

	protected Button addCheckBox(Composite parent, String label, Key key, String[] values, int indent) {
		ControlData data= new ControlData(key, values);

		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		gd.horizontalSpan= 3;
		gd.horizontalIndent= indent;

		Button checkBox= new Button(parent, SWT.CHECK);
		checkBox.setFont(JFaceResources.getDialogFont());
		checkBox.setText(label);
		checkBox.setData(data);
		checkBox.setLayoutData(gd);
		checkBox.addSelectionListener(getSelectionListener());

		makeScrollableCompositeAware(checkBox);

		String currValue= getValue(key);
		checkBox.setSelection(data.getSelection(currValue) == 0);

		fCheckBoxes.add(checkBox);

		return checkBox;
	}

	protected Combo addComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
		GridData gd= new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1);
		gd.horizontalIndent= indent;

		Label labelControl= new Label(parent, SWT.LEFT);
		labelControl.setFont(JFaceResources.getDialogFont());
		labelControl.setText(label);
		labelControl.setLayoutData(gd);

		Combo comboBox= newComboControl(parent, key, values, valueLabels);
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		fLabels.put(comboBox, labelControl);

		return comboBox;
	}

	protected Combo addInversedComboBox(Composite parent, String label, Key key, String[] values, String[] valueLabels, int indent) {
		GridData gd= new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
		gd.horizontalIndent= indent;
		gd.horizontalSpan= 3;

		Composite composite= new Composite(parent, SWT.NONE);
		GridLayout layout= new GridLayout();
		layout.marginHeight= 0;
		layout.marginWidth= 0;
		layout.numColumns= 2;
		composite.setLayout(layout);
		composite.setLayoutData(gd);

		Combo comboBox= newComboControl(composite, key, values, valueLabels);
		comboBox.setFont(JFaceResources.getDialogFont());
		comboBox.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		Label labelControl= new Label(composite, SWT.LEFT | SWT.WRAP);
		labelControl.setText(label);
		labelControl.setLayoutData(new GridData());

		fLabels.put(comboBox, labelControl);
		return comboBox;
	}

	protected Text addTextField(Composite parent, String label, Key key, int indent, int widthHint) {
		Label labelControl= new Label(parent, SWT.WRAP);
		labelControl.setText(label);
		labelControl.setFont(JFaceResources.getDialogFont());
		labelControl.setLayoutData(new GridData());

		Text textBox= new Text(parent, SWT.BORDER | SWT.SINGLE);
		textBox.setData(key);
		textBox.setLayoutData(new GridData());

		makeScrollableCompositeAware(textBox);

		fLabels.put(textBox, labelControl);

		String currValue= getValue(key);
		if (currValue != null) {
			textBox.setText(currValue);
		}
		textBox.addModifyListener(getTextModifyListener());

		GridData data= new GridData(GridData.HORIZONTAL_ALIGN_FILL);
		if (widthHint != 0) {
			data.widthHint= widthHint;
		}
		data.horizontalIndent= indent;
		data.horizontalSpan= 2;
		textBox.setLayoutData(data);

		fTextBoxes.add(textBox);
		return textBox;
	}

	protected boolean checkValue(Key key, String value) {
		return value.equals(getValue(key));
	}

	protected void controlChanged(Widget widget) {
		ControlData data= (ControlData) widget.getData();
		String newValue= null;
		if (widget instanceof Button) {
			newValue= data.getValue(((Button)widget).getSelection());
		} else if (widget instanceof Combo) {
			newValue= data.getValue(((Combo)widget).getSelectionIndex());
		} else {
			return;
		}
		String oldValue= setValue(data.getKey(), newValue);
		validateSettings(data.getKey(), oldValue, newValue);
	}

	/**
	 * Called from BasePreferencePage#createPreferenceContent.
	 */
	public final Control createPreferenceContent(Composite parent) {
		fBlockControl = createContents(parent);
		if (fBlockControl != null) {
			cacheOriginalValues();
			initContents();
		}
		return fBlockControl;
	}

	/**
	 * Derived classes must override this in order to create
	 * their visual content.  After this is called, initContents()
	 * will be called.
	 * @return a Composite representing the entire pane.
	 */
	protected abstract Control createContents(Composite parent);

	/**
	 * This will be called when settings are first loaded and
	 * whenever changes are applied.
	 * Derived classes may use this to cache the saved settings
	 * values, for later comparison to see if anything changed.
	 */
	protected void cacheOriginalValues() {
		fOriginallyHadProjectSettings= hasProjectSpecificOptionsNoCache(fProject);
	}

	/**
	 * This will be called exactly once during initialization, after
	 * createContents() and cacheOriginalValues().
	 * Derived classes may override this to initialize any fields
	 * that are not based on a Key.
	 */
	protected void initContents() {
		// Base method does nothing.
	}

	protected ExpandableComposite createStyleSection(Composite parent, String label, int nColumns) {
		ExpandableComposite excomposite= new ExpandableComposite(parent, SWT.NONE, ExpandableComposite.TWISTIE | ExpandableComposite.CLIENT_INDENT);
		excomposite.setText(label);
		excomposite.setExpanded(false);
		excomposite.setFont(JFaceResources.getFontRegistry().getBold(JFaceResources.DIALOG_FONT));
		excomposite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, false, nColumns, 1));
		excomposite.addExpansionListener(new ExpansionAdapter() {
			public void expansionStateChanged(ExpansionEvent e) {
				expandedStateChanged((ExpandableComposite) e.getSource());
			}
		});
		fExpandedComposites.add(excomposite);
		makeScrollableCompositeAware(excomposite);
		return excomposite;
	}

	/**
	 * Called from BasePreferencePage#dispose().
	 * Derived classes may override.
	 */
	public void dispose() {
	}

	protected final void expandedStateChanged(ExpandableComposite expandable) {
		ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(expandable);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.reflow(true);
		}
	}

	protected Control findControl(Key key) {
		Combo comboBox= getComboBox(key);
		if (comboBox != null) {
			return comboBox;
		}
		Button checkBox= getCheckBox(key);
		if (checkBox != null) {
			return checkBox;
		}
		Text text= getTextControl(key);
		if (text != null) {
			return text;
		}
		return null;
	}

	protected boolean getBooleanValue(Key key) {
		return Boolean.parseBoolean(getValue(key));
	}

	protected Button getCheckBox(Key key) {
		for (int i= fCheckBoxes.size() - 1; i >= 0; i--) {
			Button curr= fCheckBoxes.get(i);
			ControlData data= (ControlData) curr.getData();
			if (key.equals(data.getKey())) {
				return curr;
			}
		}
		return null;
	}

	protected Combo getComboBox(Key key) {
		for (int i= fComboBoxes.size() - 1; i >= 0; i--) {
			Combo curr= fComboBoxes.get(i);
			ControlData data= (ControlData) curr.getData();
			if (key.equals(data.getKey())) {
				return curr;
			}
		}
		return null;
	}

	/**
	 * Provide the strings needed to ask the user whether to rebuild.
	 * Derived classes can override this to change the strings, or to
	 * return null, in which case the dialog will not be shown and the
	 * rebuild will not be triggered.
	 * @param workspaceSettings true if workspace settings have changed,
	 * false if only project-specific settings have changed.
	 * @return an array whose first entry is the dialog title, and whose
	 * second entry is a query asking the user whether to rebuild.
	 */
	protected String[] getFullBuildDialogStrings(boolean workspaceSettings) {
		String[] strings= new String[2];
		strings[0] = Messages.BaseConfigurationBlock_settingsChanged;
		if (workspaceSettings) {
			strings[1]= Messages.BaseConfigurationBlock_fullRebuildRequired;
		}
		else {
			strings[1]= Messages.BaseConfigurationBlock_rebuildRequired;
		}
		return strings;
	}

	protected ExpandableComposite getParentExpandableComposite(Control control) {
		Control parent= control.getParent();
		while (!(parent instanceof ExpandableComposite) && parent != null) {
			parent= parent.getParent();
		}
		if (parent instanceof ExpandableComposite) {
			return (ExpandableComposite) parent;
		}
		return null;
	}

	protected ScrolledPageContent getParentScrolledComposite(Control control) {
		Control parent= control.getParent();
		while (!(parent instanceof ScrolledPageContent) && parent != null) {
			parent= parent.getParent();
		}
		if (parent instanceof ScrolledPageContent) {
			return (ScrolledPageContent) parent;
		}
		return null;
	}

	protected final IWorkbenchPreferenceContainer getPreferenceContainer() {
		return fContainer;
	}

	protected SelectionListener getSelectionListener() {
		if (fSelectionListener == null) {
			fSelectionListener= new SelectionListener() {
				public void widgetDefaultSelected(SelectionEvent e) {}

				public void widgetSelected(SelectionEvent e) {
					controlChanged(e.widget);
				}
			};
		}
		return fSelectionListener;
	}

	protected Shell getShell() {
		return fShell;
	}

	/**
	 * Retuens the value as actually stored in the preference store.
	 * @param key
	 * @return the value as actually stored in the preference store.
	 */
	protected String getStoredValue(Key key) {
		return key.getStoredValue(fLookupOrder, false, fManager);
	}

	protected Text getTextControl(Key key) {
		for (int i= fTextBoxes.size() - 1; i >= 0; i--) {
			Text curr= fTextBoxes.get(i);
			ControlData data= (ControlData) curr.getData();
			if (key.equals(data.getKey())) {
				return curr;
			}
		}
		return null;
	}

	protected ModifyListener getTextModifyListener() {
		if (fTextModifyListener == null) {
			fTextModifyListener= new ModifyListener() {
				public void modifyText(ModifyEvent e) {
					textChanged((Text) e.widget);
				}
			};
		}
		return fTextModifyListener;
	}

	protected String[] getTokens(String text, String separator) {
		StringTokenizer tok= new StringTokenizer(text, separator);
		int nTokens= tok.countTokens();
		String[] res= new String[nTokens];
		for (int i= 0; i < res.length; i++) {
			res[i]= tok.nextToken().trim();
		}
		return res;
	}

	protected String getValue(Key key) {
		if (fDisabledProjectSettings != null) {
			return fDisabledProjectSettings.get(key);
		}
		return key.getStoredValue(fLookupOrder, false, fManager);
	}

	/**
	 * TODO: this method is a workaround for Bugzilla 111144 and 106111.  When
	 * 111144 is fixed, remove this method and call hasProjectSpecificOptions()
	 * instead.  The difference is that this one does not cause project prefs nodes
	 * to be cached in the WorkingCopyManager.
	 * @return true if the project has project-specific options.
	 */
	public boolean hasProjectSpecificOptionsNoCache(IProject project) {
		if (project != null) {
			IScopeContext projectContext= new ProjectScope(project);
			Key[] allKeys= fAllKeys;
			for (int i= 0; i < allKeys.length; i++) {
				if (allKeys[i].getStoredValue(projectContext, null) != null) {
					return true;
				}
			}
		}
		return false;
	}

	private void makeScrollableCompositeAware(Control control) {
		ScrolledPageContent parentScrolledComposite= getParentScrolledComposite(control);
		if (parentScrolledComposite != null) {
			parentScrolledComposite.adaptChild(control);
		}
	}

	protected Combo newComboControl(Composite composite, Key key, String[] values, String[] valueLabels) {
		ControlData data= new ControlData(key, values);

		Combo comboBox= new Combo(composite, SWT.READ_ONLY);
		comboBox.setItems(valueLabels);
		comboBox.setData(data);
		comboBox.addSelectionListener(getSelectionListener());
		comboBox.setFont(JFaceResources.getDialogFont());

		makeScrollableCompositeAware(comboBox);

		String currValue= getValue(key);
		comboBox.select(data.getSelection(currValue));

		fComboBoxes.add(comboBox);
		return comboBox;
	}

	public boolean performApply() {
		return processChanges(null); // apply directly
	}


	public void performDefaults() {
		IScopeContext[] lookupOrder; // not same as fLookupOrder!  Starts one layer deeper.
		if (fProject != null) {
			lookupOrder= new IScopeContext[] {
				InstanceScope.INSTANCE,
				DefaultScope.INSTANCE
			};
		} else {
			lookupOrder= new IScopeContext[] {
				DefaultScope.INSTANCE
			};
		}

		for (int i= 0; i < fAllKeys.length; i++) {
			Key curr= fAllKeys[i];
			String defValue= curr.getStoredValue(lookupOrder, false, null);
			setValue(curr, defValue);
		}

		settingsUpdated();
		updateControls();
		validateSettings(null, null, null);
	}

	public boolean performOk() {
		return processChanges(fContainer);
	}

	/**
	 * @since 3.1
	 */
	public void performRevert() {
		for (int i= 0; i < fAllKeys.length; i++) {
			Key curr= fAllKeys[i];
			String origValue= curr.getStoredValue(fLookupOrder, false, null);
			setValue(curr, origValue);
		}

		settingsUpdated();
		updateControls();
		validateSettings(null, null, null);
	}

	/**
	 * If there are changed settings, save them and ask user whether to rebuild.
	 * This is called by performOk() and performApply().
	 * @param container null when called from performApply().
	 * @return false to abort exiting the preference pane.
	 */
	protected boolean processChanges(IWorkbenchPreferenceContainer container) {

		boolean projectSpecificnessChanged = false;
		boolean isProjectSpecific= (fProject != null) && fBlockControl.getEnabled();
		if (fOriginallyHadProjectSettings ^ isProjectSpecific) {
			// the project-specificness changed.
			projectSpecificnessChanged= true;
		} else if ((fProject != null) && !isProjectSpecific) {
			// no project specific data, and there never was, and this
			// is a project preferences pane, so nothing could have changed.
			return true;
		}

		if (!projectSpecificnessChanged && !settingsChanged(fLookupOrder[0])) {
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
	 * Save dialog information to persistent storage.
	 * Derived classes should override this if they have settings
	 * that are managed using means other than the Key infrastructure.
	 */
	protected void saveSettings() {
		try {
			fManager.applyChanges();
		} catch (BackingStoreException e) {
			ExceptionHandler.log(e, "Unable to save preferences"); //$NON-NLS-1$
		}
	}

	protected void restoreSectionExpansionStates(IDialogSettings settings) {
		for (int i= 0; i < fExpandedComposites.size(); i++) {
			ExpandableComposite excomposite= fExpandedComposites.get(i);
			if (settings == null) {
				excomposite.setExpanded(i == 0); // only expand the first node by default
			} else {
				excomposite.setExpanded(settings.getBoolean(SETTINGS_EXPANDED + String.valueOf(i)));
			}
		}
	}

	public void selectOption(Key key) {
		Control control= findControl(key);
		if (control != null) {
			if (!fExpandedComposites.isEmpty()) {
				ExpandableComposite expandable= getParentExpandableComposite(control);
				if (expandable != null) {
					for (int i= 0; i < fExpandedComposites.size(); i++) {
						ExpandableComposite curr= fExpandedComposites.get(i);
						curr.setExpanded(curr == expandable);
					}
					expandedStateChanged(expandable);
				}
			}
			control.setFocus();
		}
	}

	public void selectOption(String key, String qualifier) {
		for (int i= 0; i < fAllKeys.length; i++) {
			Key curr= fAllKeys[i];
			if (curr.getName().equals(key) && curr.getQualifier().equals(qualifier)) {
				selectOption(curr);
			}
		}
	}

	protected void setComboEnabled(Key key, boolean enabled) {
		Combo combo= getComboBox(key);
		Label label= fLabels.get(combo);
		combo.setEnabled(enabled);
		label.setEnabled(enabled);
	}

	protected void setShell(Shell shell) {
		fShell= shell;
	}

	/**
	 * Checks the state of all Keys in the dialog to see whether there have been changes.
	 * Derived classes which include settings managed outside of the Key infrastructure
	 * should override this method, in order to check whether the additional settings have changed.
	 * @return true if there is anything that needs to be saved.
	 */
	protected boolean settingsChanged(IScopeContext currContext) {
		boolean needsBuild= false;
		for (int i= 0; i < fAllKeys.length; i++) {
			Key key= fAllKeys[i];
			String oldVal= key.getStoredValue(currContext, null);
			String val= key.getStoredValue(currContext, fManager);
			if (val == null) {
				if (oldVal != null) {
					needsBuild |= !oldVal.equals(key.getStoredValue(fLookupOrder, true, fManager));
				}
			} else if (!val.equals(oldVal)) {
				needsBuild |= oldVal != null || !val.equals(key.getStoredValue(fLookupOrder, true, fManager));
			}
		}
		return needsBuild;
	}

	protected void settingsUpdated() {
	}

	protected String setValue(Key key, boolean value) {
		return setValue(key, String.valueOf(value));
	}

	protected String setValue(Key key, String value) {
		if (fDisabledProjectSettings != null) {
			return fDisabledProjectSettings.put(key, value);
		}
		String oldValue= getValue(key);
		key.setStoredValue(fLookupOrder[0], value, fManager);
		return oldValue;
	}

	protected void storeSectionExpansionStates(IDialogSettings settings) {
		for (int i= 0; i < fExpandedComposites.size(); i++) {
			ExpandableComposite curr= fExpandedComposites.get(i);
			settings.put(SETTINGS_EXPANDED + String.valueOf(i), curr.isExpanded());
		}
	}

	private void testIfOptionsComplete(Key[] allKeys) {
		for (int i= 0; i < allKeys.length; i++) {
			if (allKeys[i].getStoredValue(fLookupOrder, false, fManager) == null) {
				JavaPlugin.logErrorMessage("preference option missing: " + allKeys[i] + " (" + this.getClass().getName() +')');  //$NON-NLS-1$//$NON-NLS-2$
			}
		}
	}

	protected void textChanged(Text textControl) {
		Key key= (Key) textControl.getData();
		String number= textControl.getText();
		String oldValue= setValue(key, number);
		validateSettings(key, oldValue, number);
	}

	protected void updateCheckBox(Button curr) {
		ControlData data= (ControlData) curr.getData();

		String currValue= getValue(data.getKey());
		curr.setSelection(data.getSelection(currValue) == 0);
	}

	protected void updateCombo(Combo curr) {
		ControlData data= (ControlData) curr.getData();

		String currValue= getValue(data.getKey());
		curr.select(data.getSelection(currValue));
	}

	protected void updateControls() {
		// update the UI
		for (int i= fCheckBoxes.size() - 1; i >= 0; i--) {
			updateCheckBox(fCheckBoxes.get(i));
		}
		for (int i= fComboBoxes.size() - 1; i >= 0; i--) {
			updateCombo(fComboBoxes.get(i));
		}
		for (int i= fTextBoxes.size() - 1; i >= 0; i--) {
			updateText(fTextBoxes.get(i));
		}
	}

	protected abstract void updateModel(DialogField field);

	protected void updateText(Text curr) {
		Key key= (Key) curr.getData();

		String currValue= getValue(key);
		if (currValue != null) {
			curr.setText(currValue);
		}
	}

	public void useProjectSpecificSettings(boolean enable) {
		boolean hasProjectSpecificOption= fDisabledProjectSettings == null;
		if (enable != hasProjectSpecificOption && fProject != null) {
			if (enable) {
				for (int i= 0; i < fAllKeys.length; i++) {
					Key curr= fAllKeys[i];
					String val= fDisabledProjectSettings.get(curr);
					curr.setStoredValue(fLookupOrder[0], val, fManager);
				}
				fDisabledProjectSettings= null;
				updateControls();
			} else {
				fDisabledProjectSettings= new IdentityHashMap<>();
				for (int i= 0; i < fAllKeys.length; i++) {
					Key curr= fAllKeys[i];
					String oldSetting= curr.getStoredValue(fLookupOrder, false, fManager);
					fDisabledProjectSettings.put(curr, oldSetting);
					curr.setStoredValue(fLookupOrder[0], null, fManager); // clear project settings
				}
			}
		}
	}

	/* (non-javadoc)
	 * Update fields and validate.
	 * @param changedKey Key that changed, or null, if all changed.
	 */
	protected abstract void validateSettings(Key changedKey, String oldValue, String newValue);
}
