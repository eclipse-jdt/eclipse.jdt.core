package targets.bug407841;

import org.eclipse.jdt.apt.pluggable.tests.annotations.Module;

@Module(key=ModuleCore.KEY)
public class ModuleCore
{
	public static final String LEGACY_KEY = ModuleLegacy.KEY;
	public static final String KEY = "CORE";
}