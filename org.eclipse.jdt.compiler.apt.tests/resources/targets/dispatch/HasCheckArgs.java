package targets.dispatch;

import org.eclipse.jdt.compiler.apt.tests.annotations.CheckArgs;

/**
 * Target for annotation processing test.  Processing this
 * has no effect, but invokes a processor that complains
 * if it does not see expected environment variables.
 * @see org.eclipse.jdt.compiler.apt.tests.processors.checkargs.CheckArgsProc.
 */
@CheckArgs
public class HasCheckArgs {
}