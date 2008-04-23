package test.wksp.eclipse;

/**
 * A compare operation which can present its results in a special editor.
 * Running the compare operation and presentating the results in a compare editor
 * are combined in one class because it allows a client to keep the implementation
 * all in one place while separating it from the innards of a specific UI implementation of compare/merge.
 * <p> 
 * A <code>CompareEditorInput</code> defines methods for the following sequence steps:
 * <UL>
 * <LI>running a lengthy compare operation under progress monitor control,
 * <LI>creating a UI for displaying the model and initializing the some widgets with the compare result,
 * <LI>tracking the dirty state of the model in case of merge,
 * <LI>saving the model.
 * </UL>
 * The Compare plug-in's <code>openCompareEditor</code> method takes an <code>ICompareEditorInput</code>
 * and starts sequencing through the above steps. If the compare result is not empty a new compare editor
 * is opened and takes over the sequence until eventually closed.
 * <p>
 * The <code>prepareInput</code> method should contain the
 * code of the compare operation. It is executed under control of a progress monitor
 * and can be canceled. If the result of the compare is not empty, that is if there are differences
 * that needs to be presented, the <code>ICompareEditorInput</code> should hold onto them and return them with
 * the <code>getCompareResult</code> method.
 * If the value returned from <code>getCompareResult</code> is not <code>null</code>
 * a compare editor is opened on the <code>ICompareEditorInput</code> with title and title image initialized by the
 * corresponding methods of the <code>ICompareEditorInput</code>.
 * <p>
 * Creation of the editor's SWT controls is delegated to the <code>createContents</code> method.
 * Here the SWT controls must be created and initialized  with the result of the compare operation.
 * <p>
 * If merging is allowed, the modification state of the compared constituents must be tracked and the dirty
 * state returned from method <code>isSaveNeeded</code>. The value <code>true</code> triggers a subsequent call
 * to <code>save</code> where the modified resources can be saved.
 * <p>
 * The most important part of this implementation is the setup of the compare/merge UI.
 * The UI uses a simple browser metaphor to present compare results.
 * The top half of the layout shows the structural compare results (e.g. added, deleted, and changed files),
 * the bottom half the content compare results (e.g. textual differences between two files).
 * A selection in the top pane is fed to the bottom pane. If a content viewer is registered
 * for the type of the selected object, this viewer is installed in the pane.
 * In addition if a structure viewer is registered for the selection type the top pane
 * is split horizontally to make room for another pane and the structure viewer is installed
 * in it. When comparing Java files this second structure viewer would show the structural
 * differences within a Java file, e.g. added, deleted or changed methods and fields.
 * <p>
 * Subclasses provide custom setups, e.g. for a Catchup/Release operation
 * by passing a subclass of <code>CompareConfiguration</code> and by implementing the <code>prepareInput</code> method.
 * If a subclass cannot use the <code>DiffTreeViewer</code> which is installed by default in the
 * top left pane, method <code>createDiffViewer</code> can be overridden.
 * 
 * @see CompareUI
 * @see CompareEditorInput
 */
public class X08 {

}
