package test.wksp.eclipse;

/**
 * <p>
 * <code><pre>
 // printing a crude representation of the posterchild
 IElementContentVisitor visitor=
 new IElementContentVisitor() {
 public void visitElement(ElementTree tree, IPathRequestor requestor, Object elementContents) {
 System.out.println(requestor.requestPath() + " -> " + elementContent);
 }
 });
 ElementTreeIterator iterator = new ElementTreeIterator(tree, Path.ROOT);
 iterator.iterate(visitor);
 </pre></code>
 */
public class X17 {

}
