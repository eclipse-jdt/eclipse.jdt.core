package org.eclipse.jdt.core.tools.comparators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;

public final class DirectoryComparator {

	private static void collectAllFiles(File root, ArrayList collector) {
		File[] files = root.listFiles();for (int i = 0; i < files.length; i++) {
			final File currentFile = files[i];if (currentFile.isDirectory()) {
				collectAllFiles(currentFile, collector);
			} else {
				collector.add(currentFile);
			}
		}
	}

	private static File[] getAllFiles(File root) {
		ArrayList files = new ArrayList();
		if (root.isDirectory()) {
			collectAllFiles(root, files);
			File[] result = new File[files.size()];
			files.toArray(result);
			return result;
		} else {
			return null;
		}
	}
	public static void main(String[] args) {
		if (args.length != 3) {
			System.out.println("Usage: firstDirectory secondDirectory logFile");
			return;
		}
		new DirectoryComparator(args).compare();
	}

	private boolean abortComparison;

	private File firstDirectory;

	private File resultFile;

	private File secondDirectory;
	
	private Writer writer;

	private static final int DEFAULT_READING_SIZE = 8192;

	public DirectoryComparator(String[] args) {
		this.firstDirectory = new File(args[0]);
		this.secondDirectory = new File(args[1]);
		this.resultFile = new File(args[2]);
		this.abortComparison = false;
		if (!firstDirectory.isDirectory()) {
			System.out.println("The first argument has to be a directory");
			this.abortComparison = true;
		}
		if (!secondDirectory.isDirectory()) {
			System.out.println("The second argument has to be a directory");
			this.abortComparison = true;
		}
		if (resultFile.isDirectory()) {
			System.out.println("The third argument has to be a file");
			this.abortComparison = true;
		}
	}

	public void compare() {
		if (this.abortComparison) {
			return;
		}
		File[] files = getAllFiles(firstDirectory);
		if (files == null) {
			return;
		}
		File[] secondFiles = getAllFiles(secondDirectory);
		if (secondFiles == null) {
			return;
		}
		final String firstDirectoryAbsolutePath = firstDirectory.getAbsolutePath();
		final String secondDirectoryAbsolutePath = secondDirectory.getAbsolutePath();
		if (resultFile.exists()) {
			if (!resultFile.delete()) {
				System.out.println("Could not delete " + resultFile);
				return;
			}
		}
		if (secondFiles.length != files.length) {
			final String errorMessage = "Different number of jars files:\n" +
				"\t" + secondFiles.length + " in " + secondDirectoryAbsolutePath + "\n" +
				"\t" + files.length + " in " + firstDirectoryAbsolutePath + "\n";
			logError(errorMessage);
		}
		for (int i = 0, max = files.length; i < max; i++) {
			String currentFile = files[i].getAbsolutePath();
			String firstFileName = currentFile;
			// extract the second file name
			String secondFileName = secondDirectoryAbsolutePath + File.separator + currentFile.substring(firstDirectoryAbsolutePath.length() + 1);
			if (new File(secondFileName).exists()) {
				if (firstFileName.toLowerCase().endsWith(".jar")) {
					new JarFileComparator(new String[]{firstFileName, secondFileName,
							resultFile.getAbsolutePath()}).compare();
				} else {
					// do a binary compare byte per byte
					File firstFile = new File(firstFileName);
					File secondFile = new File(secondFileName);
					byte[] contentsFile1 = getBytes(firstFile);
					byte[] contentsFile2 = getBytes(secondFile);
					if (!equals(contentsFile1, contentsFile2)) {
						logError("DIFFERENT CONTENTS: " + firstFile.getName() + "\n");
					}
				}
			} else {
				logError(secondFileName + " doesn't exist");
			}
		}
		System.out.println("DONE. Check the file "
				+ resultFile.getAbsolutePath() + " to see the result.");
		if (this.writer != null) {
			try {
				writer.flush();
				writer.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private boolean equals(byte[] contentsFile1, byte[] contentsFile2) {
		if (contentsFile1 == null) {
			return contentsFile2 == null;
		}
		if (contentsFile2 == null) return false;
		int contentsFile1Length = contentsFile1.length;
		int contentsFile2Length = contentsFile2.length;
		if (contentsFile1Length != contentsFile2Length) return false;
		for (int i = 0; i < contentsFile1Length; i++) {
			if (contentsFile1[i] != contentsFile2[i]) {
				return false;
			}
		}
		return true;
	}
	
	public byte[] getBytes(File file) {
	byte[] contents;
		try {contents = new byte[0];
			int contentsLength = 0;
			int amountRead = -1;
			FileInputStream stream = new FileInputStream(file);
			do {
				int amountRequested = Math.max(stream.available(), DEFAULT_READING_SIZE);  // read at least 8K
				
				// resize contents if needed
				if (contentsLength + amountRequested > contents.length) {
					System.arraycopy(
						contents,
						0,
						contents = new byte[contentsLength + amountRequested],
						0,
						contentsLength);
				}

				// read as many bytes as possible
				amountRead = stream.read(contents, contentsLength, amountRequested);

				if (amountRead > 0) {
					// remember length of contents
					contentsLength += amountRead;
				}
			} while (amountRead != -1); 

			// resize contents if necessary
			if (contentsLength < contents.length) {
				System.arraycopy(
					contents,
					0,
					contents = new byte[contentsLength],
					0,
					contentsLength);
			}
return contents;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
		e.printStackTrace();
}
	return null;
}
	
	private void logError(String message) {
		try {
			if (this.writer == null) {
				this.writer = new BufferedWriter(new FileWriter(this.resultFile, true));
			}
			writer.write(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}