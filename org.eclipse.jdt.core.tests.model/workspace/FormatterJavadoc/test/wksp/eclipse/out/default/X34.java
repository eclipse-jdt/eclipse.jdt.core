package test.wksp.eclipse;

public class X34 {

	/**
	 * Service property (named &quot;service.ranking&quot;) identifying a
	 * service's ranking number (of type <tt>java.lang.Integer</tt>).
	 *
	 * <p>
	 * This property may be supplied in the <tt>properties
	 * Dictionary</tt> object passed to the
	 * <tt>BundleContext.registerService</tt> method.
	 *
	 * <p>
	 * The service ranking is used by the Framework to determine the
	 * <i>default</i> service to be returned from a call to the
	 * {@link BundleContext#getServiceReference}method: If more than one service
	 * implements the specified class, the <tt>ServiceReference</tt> object with
	 * the highest ranking is returned.
	 *
	 * <p>
	 * The default ranking is zero (0). A service with a ranking of
	 * <tt>Integer.MAX_VALUE</tt> is very likely to be returned as the default
	 * service, whereas a service with a ranking of <tt>Integer.MIN_VALUE</tt>
	 * is very unlikely to be returned.
	 *
	 * <p>
	 * If the supplied property value is not of type <tt>java.lang.Integer</tt>,
	 * it is deemed to have a ranking value of zero.
	 */
	public static final String SERVICE_RANKING = "service.ranking";
}
