package test.wksp.eclipse;

/**
 * <p>This class provides an entry point for launching the OSGi framework.
 * It configures the OSGi framework according to the command line arguments:
 * <ul>
 * <li><b>-con[sole][:<i>port</i>]</b><br>
 *   Starts the OSGi framework with a console window.  Any command line arguments not recognized are passed
 *   to the console for it to execute.  If a port is specified the console will listen on that
 *   port for commands.  If no port is specified, the console will use System.in and System.out.
 * </li>
 * <li><b>-adaptor[:adaptor-name][adaptor-args]</b>
 * <pre>
 * [adaptor-name] := "" | fully qualified class name of the FrameworkAdapter
 * [adaptor-args] := *( ":" [value])
 * [value] := [token] | [quoted-string]
 *
 * This allows
 *
 * -adaptor::"bundledir=c:\jarbundles":reset 		DefaultAdaptor is chosen with args[] {"bundledir=c:\jarbundles", "reset"}
 * -adaptor:com.foo.MyAdaptor				com.foo.MyAdaptor chosen with args[] {}
 * </pre>
 *   <p>-adaptor specifies the implementation class for the FrameworkAdapter to be used.
 *   args contains a list of FrameworkAdaptor arguments, separated by ":".  FrameworkAdaptor arguments
 *   format is defined by the adaptor implementation class.  They
 *   are passed to the adaptor class as an array of Strings.
 *   Example arguments used by the DefaultAdaptor are:
 *   <ul>
 *   <li>"bundledir=<i>directory"</i>.  The directory to be used by the adaptor to store data.
 *   <li>reset</i>.  Perform the reset action to clear the bundledir.
 *   <p>Actions can be defined by an adaptor.  Multiple actions can be specified,
 *   separated by ":".
 *   </ul>
 *   <p>It is up to the adaptor implementation to define reasonable defaults if it's required
 *   arguments are not specified.
 *   <p>If -adaptor is not specified, or if no adaptor classname is specified, DefaultAdaptor will be
 *   used, which is file based and stores the files in the \bundles directory
 *   relative to the current directory.
 * </ul>
 * <li>-app[lication]:application-args
 * <pre>
 *    [application-args] := *( ":" [value])
 *    [value] := [token] | [quoted-string]
 * </pre>
 * <p>This argument allows arguments to be passed to specific applications at launch time.  This is for eclipse
 * plugins installed as applications.  The arguments are as Eclipse currently needs them - one list of key=value pairs 
 * which are parsed by the applications.  The application peels off only the args that apply to it.  Others are ignored. 
 * </li>
 * <p>
 * Any other command line arguments are passed on to the console window
 * of the framework if started with the -console option.  If the console is not started,
 * any unrecognized arguments will be ignored and a message displayed.
 * <p>
 * If none of the options above are specified, the OSGi framework is started:
 * <ul>
 * <li>with the Default FrameworkAdaptor
 * <li>without a console window
 * <li>without the remote agent
 * </ul>
 */
public class X04 {

}
