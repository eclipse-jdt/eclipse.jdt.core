package org.eclipse.jdt.internal.core.nd;

import org.eclipse.core.runtime.IPath;

/**
 * Stores a local path, either as a path on the local filesystem or a workspace-relative path.
 * @since 3.12
 */
public final class LocalPath {
	private final boolean isWorkspacePath;
	private final IPath path;

	private LocalPath(boolean isWorkspacePath, IPath path) {
		super();
		this.isWorkspacePath = isWorkspacePath;
		this.path = path;
	}

	public static LocalPath createFileSystem(IPath path) {
		return new LocalPath(false, path);
	}

	public static LocalPath createWorkspace(IPath path) {
		return new LocalPath(true, path);
	}

	public boolean isWorkspacePath() {
		return this.isWorkspacePath;
	}

	public IPath getPath() {
		return this.path;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.isWorkspacePath ? 1231 : 1237);
		result = prime * result + ((this.path == null) ? 0 : this.path.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LocalPath other = (LocalPath) obj;
		if (this.isWorkspacePath != other.isWorkspacePath)
			return false;
		if (this.path == null) {
			if (other.path != null)
				return false;
		} else if (!this.path.equals(other.path))
			return false;
		return true;
	}
}
