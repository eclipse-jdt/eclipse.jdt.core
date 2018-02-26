/**
 * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
 * tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam,
 * quis nostrud exercitation ullamco.
 * 
 * @author kilroy
 * @see Example2
 * @see http://wiki.ecipse.org
 * @deprecated Do not use this class, it's only to test formatting on. One two
 *             three four five six seven eight nine ten.
 */
public class Example {
	/**
	 * Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod
	 * tempor incididunt ut labore et dolore magna aliqua.
	 * 
	 * @param first the first param. Nullam nec ex vitae felis eleifend lacinia in
	 *        in velit.
	 * @param second a second apram. Curabitur commodo tortor vel sapien elementum,
	 *        sit amet varius eros varius.
	 * @param x a third param. Pellentesque ac ipsum. Quisque ac nunc odioque id
	 *        nunc convallis nec mi.
	 * @throws Exception1 when the first thing happens. Nam nec sodales sem.
	 *         Curabitur odio sem, sodales ac lorem eget, pretium tincidunt ligula.
	 *         Ut eget placerat justo.
	 * @throws OtherException when some other thing happens. Duis feugiat ultricies
	 *         magna, sit amet sagittis nibh eleifend eu. Curabitur in egestas
	 *         velit.
	 * @return Lorem consectetur adipiscing ut enim ad minim veniam, quis nostrud
	 *         exercitation ullamco. Nullam nec ex vitae felis eleifend lacinia in
	 *         in velit.
	 * @since 1.1.1
	 */
	void method1() {
	}

	/**
	 * Curabitur commodo tortor vel sapien elementum, sit amet varius eros varius.
	 * Morbi posuere ex sit amet lorem vestibulum pulvinar. In in sagittis urna,
	 * euismod tempus eros.
	 * 
	 * @param parameterOne the first param.
	 *        <li>item1</li>
	 *        <li>item2</li>
	 *        <li>item3</li>
	 * @param theSecondParameter the second param. <code>line1
	 * line2</code>
	 * @deprecated method1 should be used instead
	 */
	void method2() {
	}

	/**
	 * Curabitur rhoncus felis non elit malesuada, et gravida enim tempus. Morbi sit
	 * amet viverra est. Sed eu libero in mauris facilisis condimentum.
	 * 
	 * @see <a href="http://bugs.eclipse.org">bugzilla</a>
	 * @see method1
	 * @return
	 * @throws RuntimeException Curabitur morbi sit amet viverra est. Sed eu libero
	 *         in mauris facilisis condimentum.
	 */
	void method3() {
	}

	/**
	 * @param argument Integer diam sapien, interdum a dolor sit amet, vestibulum
	 *        vehicula ante. Mauris ultricies odio sit amet nunc laoreet, sed
	 *        venenatis enim efficitur.
	 * @throws e Proin dignissim enim eu erat cursus fringilla.
	 *         <p>
	 *         Aliquam efficitur sed turpis ut cursus.
	 *         <p>
	 *         Nunc pharetra aliquam massa et sagittis.
	 * @param param
	 * 
	 *        <pre>
	 *        int i = 1234;
	 *        </pre>
	 * 
	 * @return Nunc ultricies neque eu elit porttitor, vel scelerisque metus
	 *         accumsan. Curabitur dictum arcu magna, eget mattis lacus congue id.
	 * @gibberish invalid tag?
	 */
	void method5() {
	}

	/**
	 * Draws as much of the specified image as is currently available with its
	 * northwest corner at the specified coordinate (x, y). This method will return
	 * immediately in all cases, even if the entire image has not yet been scaled,
	 * dithered and converted for the current output device.
	 * <p>
	 * If the current output representation is not yet complete then the method will
	 * return false and the indicated {@link ImageObserver} object will be notified
	 * as the conversion process progresses.
	 *
	 * @param img the image to be drawn
	 * @param x the x-coordinate of the northwest corner of the destination
	 *        rectangle in pixels
	 * @param y the y-coordinate of the northwest corner of the destination
	 *        rectangle in pixels
	 * @param observer the image observer to be notified as more of the image is
	 *        converted. May be <code>null</code>
	 * @return <code>true</code> if the image is completely loaded and was painted
	 *         successfully; <code>false</code> otherwise.
	 * @see Image
	 * @see ImageObserver
	 * @since 1.0
	 */
	public abstract boolean drawImage(Image img, int x, int y, ImageObserver observer);

}