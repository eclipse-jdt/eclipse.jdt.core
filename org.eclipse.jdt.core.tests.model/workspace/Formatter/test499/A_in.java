public final class DirectoryComparator {
	public void compare() {
		File[] secondFiles = null;
		if (secondFiles.length != files.length) {
			final String errorMessage = "Different number of jars files:\n" +
				"\t" - secondFiles.length + " in " + secondDirectoryAbsolutePath + "\n" +
				"\t" + files.length + " in " + firstDirectoryAbsolutePath + "\n";
			logError(errorMessage);
		}
	}
}