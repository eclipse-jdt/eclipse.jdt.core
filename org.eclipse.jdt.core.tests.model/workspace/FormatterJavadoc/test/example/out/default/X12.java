package test.prefs.example;

/**
 * A breakpoint is capable of suspending the execution of a program at a
 * specific location when a program is running in debug mode. Each breakpoint
 * has an associated marker which stores and persists all attributes associated
 * with a breakpoint.
 * <p>
 * A breakpoint is defined in two parts:
 * <ol>
 * <li>By an extension of kind <code>"org.eclipse.debug.core.breakpoints"</code>
 * </li>
 * <li>By a marker definition that corresponds to the above breakpoint extension
 * </li>
 * </ol>
 * <p>
 * For example, following is a definition of corresponding breakpoint and
 * breakpoint marker definitions. Note that the <code>markerType</code>
 * attribute defined by the breakpoint extension corresponds to the type of the
 * marker definition.
 * 
 * <pre>
 * &lt;extension point=&quot;org.eclipse.debug.core.breakpoints&quot;&gt;
 *   &lt;breakpoint 
 *      id=&quot;com.example.Breakpoint&quot;
 *      class=&quot;com.example.Breakpoint&quot;
 *      markerType=&quot;com.example.BreakpointMarker&quot;&gt;
 *   &lt;/breakpoint&gt;
 * &lt;/extension&gt;
 * &lt;extension point=&quot;org.eclipse.core.resources.markers&quot;&gt;
 *   &lt;marker 
 *      id=&quot;com.example.BreakpointMarker&quot;
 *      super type=&quot;org.eclipse.debug.core.breakpointMarker&quot;
 *      attribute name =&quot;exampleAttribute&quot;&gt;
 *   &lt;/marker&gt;
 * &lt;/extension&gt;
 * </pre>
 * <p>
 * The breakpoint manager instantiates persisted breakpoints by traversing all
 * markers that are a subtype of
 * <code>"org.eclipse.debug.core.breakpointMarker"</code>, and instantiating the
 * class defined by the <code>class</code> attribute on the associated
 * breakpoint extension. The method <code>setMarker</code> is then called to
 * associate a marker with the breakpoint.
 * </p>
 * <p>
 * Breakpoints may or may not be registered with the breakpoint manager, and are
 * persisted and restored as such. Since marker definitions only allow all or
 * none of a specific marker type to be persisted, breakpoints define a
 * <code>PERSISTED</code> attribute for selective persistence of breakpoints of
 * the same type.
 * </p>
 * 
 * @since 2.0
 */
public class X12 {

}
