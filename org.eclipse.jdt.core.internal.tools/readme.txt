How to support a new unicode version in the scanner:

1) Go to http://www.unicode.org/Public/
2) Select the folder that corresponds to the unicode version for which you want to generate the scanner resource files
3) Select the ucdxml folder and download the file called ucd.all.flat.zip.
4) Unzip that file on your disk. This creates a file called ucd.all.flat.xml.
5) To generate the resource files for identifier starts, you need to invoke
org.eclipse.jdt.core.internal.tools.unicode.GenerateIdentifierStartResources with the following arguments:
	- first argument: unicode version
	- second argument: path to the ucd.all.flat.xml file.
	- third argument: folder in which the resource files will be generated
For example:
	8.0 c:/unicode8.0.0/ucd.all.flat.xml c:/unicode8.0.0/res

6) To generate the resource files for identifier parts, you need to invoke
org.eclipse.jdt.core.internal.tools.unicode.GenerateIdentifierPartResources with the same arguments used previously.
7) Once this is done, you need to edit org.eclipse.jdt.internal.compiler.parser.ScannerHelper to add a new table for the new unicode support.

For example:
- add the new method:
	static void initializeTable19() {
		Tables9 = initializeTables("unicode8"); //$NON-NLS-1$
	}
- add the new static field Tables9.
- add a new folder unicode8 as a sub folder of org/eclipse/jdt/internal/compiler/parser/.
- put into this folder all resource files generated in step 5 and 6.
- modify
	org.eclipse.jdt.internal.compiler.parser.ScannerHelper.isJavaIdentifierPart(long, int)
	org.eclipse.jdt.internal.compiler.parser.ScannerHelper.isJavaIdentifierStart(long, int)
To use the new Tables9 values based on the compliance value by adding a new else if condition.

For org.eclipse.jdt.internal.compiler.parser.ScannerHelper.isJavaIdentifierPart(long, int) this becomes
	The last else becomes an else if that supports the previous 1.8 compliance
	else if (complianceLevel <= ClassFileConstants.JDK1_8) {
		// java 7 supports Unicode 6.2
		if (Tables8 == null) {
			initializeTable18();
		}
		switch((codePoint & 0x1F0000) >> 16) {
			case 0 :
				return isBitSet(Tables8[PART_INDEX][0], codePoint & 0xFFFF);
			case 1 :
				return isBitSet(Tables8[PART_INDEX][1], codePoint & 0xFFFF);
			case 2 :
				return isBitSet(Tables8[PART_INDEX][2], codePoint & 0xFFFF);
			case 14 :
				return isBitSet(Tables8[PART_INDEX][3], codePoint & 0xFFFF);
		}	
	} else {
		// java 9 supports Unicode 8
		if (Tables9 == null) {
			initializeTable19();
		}
		switch((codePoint & 0x1F0000) >> 16) {
			case 0 :
				return isBitSet(Tables9[PART_INDEX][0], codePoint & 0xFFFF);
			case 1 :
				return isBitSet(Tables9[PART_INDEX][1], codePoint & 0xFFFF);
			case 2 :
				return isBitSet(Tables9[PART_INDEX][2], codePoint & 0xFFFF);
			case 14 :
				return isBitSet(Tables9[PART_INDEX][3], codePoint & 0xFFFF);
		}
	}

8) Do the same set of changes for org.eclipse.jdt.internal.compiler.parser.ScannerHelper.isJavaIdentifierStart(long, int).
9) You need to add a regression test class in org.eclipse.jdt.core.tests.compiler.regression similar to org.eclipse.jdt.core.tests.compiler.regression.Unicode18Test.
You can get the character value for the regression test by checking the ucd.all.flat.xml file and searching for an entry that has the age parameter equals to the
unicode version you want to check (i.e. for unicode 8, age="8.0").

If you have any questions regarding this tool, please comment in the bug report 506870: https://bugs.eclipse.org/bugs/show_bug.cgi?id=506870