public class B {
	private void computeDocumentNames(String[] onDiskNames, int[] positions, Object indexedDocuments) {
		int onDiskLength = onDiskNames.length;
		Object[] docNames = new Object[0];
		Object[] referenceTables = new Object[1];
		if (onDiskLength == 0) {
			// disk index was empty, so add every indexed document
			for (int i = 0, l = referenceTables.length; i < l; i++)
				if (referenceTables[i] != null)
					indexedDocuments.equals(null); // remember each new document
		}
	}
}
