public class A {
	public void getDocument() {
		while (true) {
			newFilename = (new StringBuffer(_workingDir).append(File.separator)
					.append(_localFilename).append(documentCount)
					.append(EXTENTION)).toString();
		}
	}
}