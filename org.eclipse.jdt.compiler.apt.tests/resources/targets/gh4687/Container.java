package targets.gh4687;
import org.eclipse.jdt.compiler.apt.tests.annotations.Value;

interface Container {

	@Value.Style 
	@Value.Builder
	public static record BrokenRecord(String member) {
	}

}