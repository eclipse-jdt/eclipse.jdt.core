package test.prefs.example;

/**
 * A <code>TreeViewerAdvisor</code> that works with TreeViewers. Two default
 * tree viewers are provided that support navigation:
 * <code>NavigableTreeViewer</code> and
 * <code>NavigableCheckboxTreeViewer</code>.
 * <p>
 * Note that this advisor can be used with any tree viewer. By default it
 * provides an expand all action, double click behavior on containers, and
 * navigation support for tree viewers.
 * </p>
 * <p>
 * By default this advisor supports hierarchical models and honour the
 * compressed folder Team preference for showing the sync set as compressed
 * folders. Subclasses can provide their own presentation models.
 * <p>
 * 
 * @since 3.0
 */
public class X13 {

	/**
	 * Create an advisor that will allow viewer contributions with the given
	 * <code>targetID</code>. This advisor will provide a presentation model
	 * based on the given sync info set. Note that it's important to call
	 * {@link #foo()} when finished with an advisor.
	 * 
	 * @param parent
	 * @param configuration
	 */
	X13(Object parent, Object configuration) {
	}

	void foo() {
	}
}
