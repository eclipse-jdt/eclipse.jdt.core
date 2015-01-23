package test.comments.block;

public class X17 {

	public JavaModelManager_PerProjectInfo foo(String project) {
		JavaModelManager_PerProjectInfo info = getPerProjectInfo(project, false /*
																				 * don
																				 * 't
																				 * create
																				 * info
																				 */);
		if (info == null) {
		}
		return info;
	}

	private JavaModelManager_PerProjectInfo getPerProjectInfo(String project,
			boolean b) {
		return null;
	}
}

class JavaModelManager_PerProjectInfo {
}
