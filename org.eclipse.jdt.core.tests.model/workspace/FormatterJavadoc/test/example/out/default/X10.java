package test.prefs.example;

/**
 * An abstract compare and merge viewer with two side-by-side content areas and
 * an optional content area for the ancestor. The implementation makes no
 * assumptions about the content type.
 * <p>
 * <code>ContentMergeViewer</code>
 * <ul>
 * <li>implements the overall layout and defines hooks so that subclasses can
 * easily provide an implementation for a specific content type,
 * <li>implements the UI for making the areas resizable,
 * <li>has an action for controlling whether the ancestor area is visible or
 * not,
 * <li>has actions for copying one side of the input to the other side,
 * <li>tracks the dirty state of the left and right sides and send out
 * notification on state changes.
 * </ul>
 * A <code>ContentMergeViewer</code> accesses its model by means of a content
 * provider which must implement the <code>IMergeViewerContentProvider</code>
 * interface.
 * </p>
 * <p>
 * Clients may wish to use the standard concrete subclass
 * <code>TextMergeViewer</code>, or define their own subclass.
 * 
 * @see IMergeViewerContentProvider
 * @see TextMergeViewer
 */
public class X10 {

}
