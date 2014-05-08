package targets.bug387956;

/*
 * The import triggers a compilation error, if SomeAnnotation has a reference to
 * Integer.MAX_VALUE: "The import generated.GeneratedEntity cannot be resolved"
 */
import generated.GeneratedEntity;
 

@SuppressWarnings("unused")
public class SomeClass {

	/**
	 * The problem only occurs when you do a Project > Clean. It doesn't occur
	 * when saving the file without cleaning (incremental compilation), because
	 * cleaning removes generated.GeneratedEntity, which is then generated
	 * again.
	 * 
	 * If you comment "@SomeAnnotation(Integer.MAX_VALUE)", the error
	 * disappears.
	 * 
	 * If you replace Integer.MAX_VALUE with its value, the error disappears.
	 * 
	 * This seems to be because {@link SomeAnnotation} fires the Eclipse
	 * annotation processing, which sees a reference that needs to be resolved
	 * (Integer.MAX_VALUE), and therefore tries to resolve imports, where it
	 * finds import {@link generated.GeneratedEntity} and cannot resolve it.
	 * 
	 * This can happen with any annotation java 6 processor that generates code.
	 * Also note that I couldn't reproduce this on the simple java 5 processor
	 * given here: http://www.eclipse.org/jdt/apt/introToAPT.html
	 */
	@SomeAnnotation(Integer.MAX_VALUE)
	Object none;
}