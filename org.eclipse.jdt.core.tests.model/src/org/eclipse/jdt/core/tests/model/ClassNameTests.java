/*******************************************************************************
 * Copyright (c) 2000, 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.internal.core.SourceType;

/**
 * Test retrieving types by their name.
 */
public class ClassNameTests extends ModifyingResourceTests {

	static IJavaProject TEST_PROJECT;
	final static int SF_LENGTH = 5;
	static int TESTS_COUNT;

public ClassNameTests(String name) {
	super(name);
}

static {
//	org.eclipse.jdt.internal.core.NameLookup.VERBOSE = true;
//	TESTS_NAMES = new String[] { "testFindSecondaryType_Bug72179" };
//	TESTS_PREFIX = "testReconcile";
}
public static Test suite() {
	Test suite = buildModelTestSuite(ClassNameTests.class);
	TESTS_COUNT = suite.countTestCases();
	return suite;
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#setUp()
 */
protected void setUp() throws Exception {
	super.setUp();
	if (TEST_PROJECT == null) {
		String[] sourceFolders = new String[SF_LENGTH];
		for (int i=0; i<SF_LENGTH; i++) {
			sourceFolders[i] = "src" + i;
		}
		TEST_PROJECT = createJavaProject("TestProject", sourceFolders, new String[] {"JCL_LIB"}, "bin");
		createFolder("/TestProject/src0/org/eclipse/jdt/core/test0");
		createFile(
			"/TestProject/src0/org/eclipse/jdt/core/test0/Foo.java",
			"package org.eclipse.jdt.core.test0;\n" +
			"public class Foo {\n" +
			"	class InFoo {}\n" +
			"}\n" +
			"class Secondary {\n" +
			"	class InSecondary {}\n" +
			"}\n"
		);
		createFile(
			"/TestProject/src1/Foo.java",
			"public class Foo {\n" +
			"	class InFoo {}\n" +
			"}\n" +
			"class Secondary {\n" +
			"	class InSecondary {}\n" +
			"}\n"
		);
		int length = SF_LENGTH - 1;
		createFolder("/TestProject/src"+length+"/org/eclipse/jdt/core/test"+length);
		createFile(
			"/TestProject/src"+length+"/org/eclipse/jdt/core/test"+length+"/Foo.java",
			"package org.eclipse.jdt.core.test"+length+";\n" +
			"public class Foo {\n" +
			"}\n" +
			"class Secondary {\n" +
			"}\n"
		);
		createFile(
			"/TestProject/src"+length+"/org/eclipse/jdt/core/test"+length+"/Test.java",
			"package org.eclipse.jdt.core.test"+length+";\n" +
			"public class Test {\n" +
			"	public static void main(String[] args) {\n" +
			"		Secondary s = new Secondary();\n" +
			"	}\n" +
			"}\n"
		);
	}
}

/* (non-Javadoc)
 * @see org.eclipse.jdt.core.tests.model.AbstractJavaModelTests#tearDown()
 */
protected void tearDown() throws Exception {
	TESTS_COUNT--;
	if (TEST_PROJECT != null && TESTS_COUNT == 0) {
		deleteResource(TEST_PROJECT.getProject());
	}
	super.tearDown();
}

protected void assertTypeFound(String typeName, String expectedResult) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(typeName, new NullProgressMonitor());
	assertTrue("type "+typeName+" should exist!", type != null && type.exists());
	assertEquals("Expected type "+typeName+" NOT found!",
		expectedResult,
		((SourceType)type).toStringWithAncestors()
	);
}
protected void assertTypeFound(String packageName, String typeName, String expectedResult) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(packageName, typeName, new NullProgressMonitor());
	assertTrue("type "+typeName+" should exist!", type != null && type.exists());
	assertEquals("Expected type "+typeName+" NOT found!",
		expectedResult,
		((SourceType)type).toStringWithAncestors()
	);
}

protected void assertTypeNotFound(String typeName) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(typeName, new NullProgressMonitor());
	assertNotNull("type "+typeName+" should NOT be null!", type);
	assertFalse("type "+typeName+" should NOT exist!", type.exists());
}
protected void assertTypeNotFound(String packageName, String typeName) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(packageName, typeName, new NullProgressMonitor());
	assertNotNull("type "+typeName+" should NOT be null!", type);
	assertFalse("type "+typeName+" should NOT exist!", type.exists());
}

protected void assertTypeUnknown(String typeName) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(typeName, new NullProgressMonitor());
	assertNull("type "+typeName+" should NOT be found!", type);
}
protected void assertTypeUnknown(String packageName, String typeName) throws JavaModelException {
	assertNotNull("TEST_PROJECT should not be null!!!", TEST_PROJECT);
	IType type = TEST_PROJECT.findType(packageName, typeName, new NullProgressMonitor());
	assertNull("type "+typeName+" should NOT be found!", type);
}

/**
 * Tests that a type in a jar with a name ending with $ can be retrieved.
 */
public void testClassNameWithDollar() throws JavaModelException, CoreException {
	try {
		byte[] tab = new byte[372];
		tab[0]=80;
		tab[1]=75;
		tab[2]=3;
		tab[3]=4;
		tab[4]=20;
		tab[5]=0;
		tab[6]=8;
		tab[7]=0;
		tab[8]=8;
		tab[9]=0;
		tab[10]=-63;
		tab[11]=88;
		tab[12]=-102;
		tab[13]=44;
		tab[14]=0;
		tab[15]=0;
		tab[16]=0;
		tab[17]=0;
		tab[18]=0;
		tab[19]=0;
		tab[20]=0;
		tab[21]=0;
		tab[22]=0;
		tab[23]=0;
		tab[24]=0;
		tab[25]=0;
		tab[26]=11;
		tab[27]=0;
		tab[28]=0;
		tab[29]=0;
		tab[30]=112;
		tab[31]=49;
		tab[32]=47;
		tab[33]=65;
		tab[34]=36;
		tab[35]=46;
		tab[36]=99;
		tab[37]=108;
		tab[38]=97;
		tab[39]=115;
		tab[40]=115;
		tab[41]=93;
		tab[42]=78;
		tab[43]=-63;
		tab[44]=74;
		tab[45]=-61;
		tab[46]=64;
		tab[47]=16;
		tab[48]=125;
		tab[49]=-45;
		tab[50]=77;
		tab[51]=-78;
		tab[52]=53;
		tab[53]=86;
		tab[54]=91;
		tab[55]=99;
		tab[56]=127;
		tab[57]=64;
		tab[58]=-24;
		tab[59]=-95;
		tab[60]=21;
		tab[61]=106;
		tab[62]=-16;
		tab[63]=-84;
		tab[64]=8;
		tab[65]=-91;
		tab[66]=-48;
		tab[67]=83;
		tab[68]=48;
		tab[69]=-121;
		tab[70]=74;
		tab[71]=-17;
		tab[72]=-101;
		tab[73]=-72;
		tab[74]=-42;
		tab[75]=45;
		tab[76]=49;
		tab[77]=43;
		tab[78]=49;
		tab[79]=-11;
		tab[80]=-69;
		tab[81]=-12;
		tab[82]=84;
		tab[83]=-16;
		tab[84]=-32;
		tab[85]=7;
		tab[86]=-8;
		tab[87]=81;
		tab[88]=-30;
		tab[89]=108;
		tab[90]=12;
		tab[91]=42;
		tab[92]=-50;
		tab[93]=-31;
		tab[94]=61;
		tab[95]=102;
		tab[96]=-26;
		tab[97]=-67;
		tab[98]=121;
		tab[99]=-13;
		tab[100]=-15;
		tab[101]=-7;
		tab[102]=-10;
		tab[103]=14;
		tab[104]=-127;
		tab[105]=41;
		tab[106]=-122;
		tab[107]=4;
		tab[108]=-1;
		tab[109]=-15;
		tab[110]=60;
		tab[111]=-98;
		tab[112]=-115;
		tab[113]=36;
		tab[114]=-120;
		tab[115]=48;
		tab[116]=-40;
		tab[117]=-88;
		tab[118]=103;
		tab[119]=21;
		tab[120]=23;
		tab[121]=-86;
		tab[122]=92;
		tab[123]=-57;
		tab[124]=105;
		tab[125]=-74;
		tab[126]=-47;
		tab[127]=121;
		tab[128]=45;
		tab[129]=33;
		tab[130]=8;
		tab[131]=-63;
		tab[132]=-91;
		tab[133]=41;
		tab[134]=77;
		tab[135]=125;
		tab[136]=69;
		tab[137]=16;
		tab[138]=-29;
		tab[139]=-55;
		tab[140]=-118;
		tab[141]=-32;
		tab[142]=-51;
		tab[143]=-19;
		tab[144]=-83;
		tab[145]=-18;
		tab[146]=-63;
		tab[147]=71;
		tab[148]=16;
		tab[149]=-62;
		tab[150]=67;
		tab[151]=-105;
		tab[152]=-48;
		tab[153]=79;
		tab[154]=76;
		tab[155]=-87;
		tab[156]=-81;
		tab[157]=-73;
		tab[158]=15;
		tab[159]=-103;
		tab[160]=-82;
		tab[161]=110;
		tab[162]=84;
		tab[163]=86;
		tab[164]=104;
		tab[165]=66;
		tab[166]=-108;
		tab[167]=-40;
		tab[168]=92;
		tab[169]=21;
		tab[170]=43;
		tab[171]=85;
		tab[172]=25;
		tab[173]=-41;
		tab[174]=-73;
		tab[175]=67;
		tab[176]=-81;
		tab[177]=-66;
		tab[178]=55;
		tab[179]=79;
		tab[180]=4;
		tab[181]=-103;
		tab[182]=52;
		tab[183]=121;
		tab[184]=23;
		tab[185]=124;
		tab[186]=-18;
		tab[187]=-50;
		tab[188]=90;
		tab[189]=-62;
		tab[190]=112;
		tab[191]=60;
		tab[192]=73;
		tab[193]=126;
		tab[194]=99;
		tab[195]=-105;
		tab[196]=117;
		tab[197]=101;
		tab[198]=-54;
		tab[199]=-75;
		tab[200]=91;
		tab[201]=46;
		tab[202]=-46;
		tab[203]=-76;
		tab[204]=-117;
		tab[205]=1;
		tab[206]=33;
		tab[207]=92;
		tab[208]=-38;
		tab[209]=109;
		tab[210]=-107;
		tab[211]=-21;
		tab[212]=-123;
		tab[213]=113;
		tab[214]=55;
		tab[215]=-28;
		tab[216]=108;
		tab[217]=116;
		tab[218]=-26;
		tab[219]=-60;
		tab[220]=56;
		tab[221]=65;
		tab[222]=-121;
		tab[223]=-61;
		tab[224]=93;
		tab[225]=117;
		tab[226]=64;
		tab[227]=-18;
		tab[228]=23;
		tab[229]=70;
		tab[230]=-55;
		tab[231]=93;
		tab[232]=-52;
		tab[233]=76;
		tab[234]=-52;
		tab[235]=-2;
		tab[236]=-23;
		tab[237]=14;
		tab[238]=123;
		tab[239]=-81;
		tab[240]=-51;
		tab[241]=58;
		tab[242]=100;
		tab[243]=12;
		tab[244]=-102;
		tab[245]=-95;
		tab[246]=-64;
		tab[247]=62;
		tab[248]=99;
		tab[249]=-17;
		tab[250]=91;
		tab[251]=-64;
		tab[252]=124;
		tab[253]=-64;
		tab[254]=76;
		tab[255]=56;
		tab[256]=68;
		tab[257]=-65;
		tab[258]=53;
		tab[259]=79;
		tab[260]=91;
		tab[261]=-77;
		tab[262]=-120;
		tab[263]=-114;
		tab[264]=94;
		tab[265]=-2;
		tab[266]=89;
		tab[267]=-125;
		tab[268]=63;
		tab[269]=86;
		tab[270]=-15;
		tab[271]=99;
		tab[272]=-115;
		tab[273]=26;
		tab[274]=-43;
		tab[275]=-15;
		tab[276]=23;
		tab[277]=80;
		tab[278]=75;
		tab[279]=7;
		tab[280]=8;
		tab[281]=122;
		tab[282]=-92;
		tab[283]=103;
		tab[284]=15;
		tab[285]=-20;
		tab[286]=0;
		tab[287]=0;
		tab[288]=0;
		tab[289]=78;
		tab[290]=1;
		tab[291]=0;
		tab[292]=0;
		tab[293]=80;
		tab[294]=75;
		tab[295]=1;
		tab[296]=2;
		tab[297]=20;
		tab[298]=0;
		tab[299]=20;
		tab[300]=0;
		tab[301]=8;
		tab[302]=0;
		tab[303]=8;
		tab[304]=0;
		tab[305]=-63;
		tab[306]=88;
		tab[307]=-102;
		tab[308]=44;
		tab[309]=122;
		tab[310]=-92;
		tab[311]=103;
		tab[312]=15;
		tab[313]=-20;
		tab[314]=0;
		tab[315]=0;
		tab[316]=0;
		tab[317]=78;
		tab[318]=1;
		tab[319]=0;
		tab[320]=0;
		tab[321]=11;
		tab[322]=0;
		tab[323]=0;
		tab[324]=0;
		tab[325]=0;
		tab[326]=0;
		tab[327]=0;
		tab[328]=0;
		tab[329]=0;
		tab[330]=0;
		tab[331]=0;
		tab[332]=0;
		tab[333]=0;
		tab[334]=0;
		tab[335]=0;
		tab[336]=0;
		tab[337]=0;
		tab[338]=0;
		tab[339]=112;
		tab[340]=49;
		tab[341]=47;
		tab[342]=65;
		tab[343]=36;
		tab[344]=46;
		tab[345]=99;
		tab[346]=108;
		tab[347]=97;
		tab[348]=115;
		tab[349]=115;
		tab[350]=80;
		tab[351]=75;
		tab[352]=5;
		tab[353]=6;
		tab[354]=0;
		tab[355]=0;
		tab[356]=0;
		tab[357]=0;
		tab[358]=1;
		tab[359]=0;
		tab[360]=1;
		tab[361]=0;
		tab[362]=57;
		tab[363]=0;
		tab[364]=0;
		tab[365]=0;
		tab[366]=37;
		tab[367]=1;
		tab[368]=0;
		tab[369]=0;
		tab[370]=0;
		tab[371]=0;
		IJavaProject javaProject = createJavaProject("P", new String[] {"src"}, "bin");
		IFile jarFile = createFile("P/lib.jar", tab);
		javaProject.setRawClasspath(new IClasspathEntry[] {JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null, false)}, new NullProgressMonitor());
		javaProject.findType("p1.A$");
	} catch (CoreException e) {
		e.printStackTrace();
		assertTrue(false);
	} finally {
		deleteProject("P");
	}
}
/**
 * Tests that a member type can be retrived using a dot qualified name.
 */
public void testFindTypeWithDot() throws JavaModelException, CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P", new String[] {""}, "");
		this.createFolder("/P/p");
		this.createFile(
			"/P/p/X.java",
			"package p;\n" +
			"public class X {\n" +
			"  public class Y {\n" +
			"  }\n" +
			"}"
		);
		IType type = javaProject.findType("p.X.Y");
		assertEquals(
			"Unexpected type found",
			"class Y [in X [in X.java [in p [in <project root> [in P]]]]]",
			type == null ? "null" : type.toString()
		);
	} finally {
		deleteProject("P");
	}
}

/**
 * Tests that a type in a jar with a name ending with $ can be retrieved.
 */
public void testSearchTypeNameInJars() throws JavaModelException, CoreException {
	try {
		byte[] tab = new byte[478];
		tab[0]=80;
		tab[1]=75;
		tab[2]=3;
		tab[3]=4;
		tab[4]=20;
		tab[5]=0;
		tab[6]=8;
		tab[7]=0;
		tab[8]=8;
		tab[9]=0;
		tab[10]=19;
		tab[11]=87;
		tab[12]=-95;
		tab[13]=44;
		tab[14]=0;
		tab[15]=0;
		tab[16]=0;
		tab[17]=0;
		tab[18]=0;
		tab[19]=0;
		tab[20]=0;
		tab[21]=0;
		tab[22]=0;
		tab[23]=0;
		tab[24]=0;
		tab[25]=0;
		tab[26]=9;
		tab[27]=0;
		tab[28]=0;
		tab[29]=0;
		tab[30]=112;
		tab[31]=49;
		tab[32]=47;
		tab[33]=112;
		tab[34]=50;
		tab[35]=47;
		tab[36]=112;
		tab[37]=51;
		tab[38]=47;
		tab[39]=3;
		tab[40]=0;
		tab[41]=80;
		tab[42]=75;
		tab[43]=7;
		tab[44]=8;
		tab[45]=0;
		tab[46]=0;
		tab[47]=0;
		tab[48]=0;
		tab[49]=2;
		tab[50]=0;
		tab[51]=0;
		tab[52]=0;
		tab[53]=0;
		tab[54]=0;
		tab[55]=0;
		tab[56]=0;
		tab[57]=80;
		tab[58]=75;
		tab[59]=3;
		tab[60]=4;
		tab[61]=20;
		tab[62]=0;
		tab[63]=8;
		tab[64]=0;
		tab[65]=8;
		tab[66]=0;
		tab[67]=19;
		tab[68]=87;
		tab[69]=-95;
		tab[70]=44;
		tab[71]=0;
		tab[72]=0;
		tab[73]=0;
		tab[74]=0;
		tab[75]=0;
		tab[76]=0;
		tab[77]=0;
		tab[78]=0;
		tab[79]=0;
		tab[80]=0;
		tab[81]=0;
		tab[82]=0;
		tab[83]=16;
		tab[84]=0;
		tab[85]=0;
		tab[86]=0;
		tab[87]=112;
		tab[88]=49;
		tab[89]=47;
		tab[90]=112;
		tab[91]=50;
		tab[92]=47;
		tab[93]=112;
		tab[94]=51;
		tab[95]=47;
		tab[96]=88;
		tab[97]=46;
		tab[98]=99;
		tab[99]=108;
		tab[100]=97;
		tab[101]=115;
		tab[102]=115;
		tab[103]=93;
		tab[104]=79;
		tab[105]=-53;
		tab[106]=78;
		tab[107]=-61;
		tab[108]=64;
		tab[109]=12;
		tab[110]=28;
		tab[111]=55;
		tab[112]=-81;
		tab[113]=-110;
		tab[114]=6;
		tab[115]=8;
		tab[116]=-3;
		tab[117]=3;
		tab[118]=110;
		tab[119]=60;
		tab[120]=-44;
		tab[121]=-82;
		tab[122]=10;
		tab[123]=71;
		tab[124]=16;
		tab[125]=23;
		tab[126]=36;
		tab[127]=78;
		tab[128]=81;
		tab[129]=57;
		tab[130]=20;
		tab[131]=69;
		tab[132]=-67;
		tab[133]=110;
		tab[134]=-62;
		tab[135]=-74;
		tab[136]=108;
		tab[137]=-107;
		tab[138]=102;
		tab[139]=-93;
		tab[140]=52;
		tab[141]=-27;
		tab[142]=-65;
		tab[143]=56;
		tab[144]=33;
		tab[145]=113;
		tab[146]=-32;
		tab[147]=3;
		tab[148]=-8;
		tab[149]=40;
		tab[150]=-124;
		tab[151]=55;
		tab[152]=-115;
		tab[153]=0;
		tab[154]=117;
		tab[155]=37;
		tab[156]=-49;
		tab[157]=-56;
		tab[158]=-10;
		tab[159]=-116;
		tab[160]=-19;
		tab[161]=-3;
		tab[162]=-6;
		tab[163]=-2;
		tab[164]=-8;
		tab[165]=-124;
		tab[166]=-125;
		tab[167]=17;
		tab[168]=78;
		tab[169]=8;
		tab[170]=97;
		tab[171]=53;
		tab[172]=17;
		tab[173]=-43;
		tab[174]=-107;
		tab[175]=-88;
		tab[176]=-82;
		tab[177]=-59;
		tab[178]=60;
		tab[179]=0;
		tab[180]=17;
		tab[181]=-30;
		tab[182]=-107;
		tab[183]=124;
		tab[184]=-107;
		tab[185]=-94;
		tab[186]=-112;
		tab[187]=-27;
		tab[188]=82;
		tab[189]=60;
		tab[190]=102;
		tab[191]=43;
		tab[192]=-107;
		tab[193]=55;
		tab[194]=1;
		tab[195]=28;
		tab[196]=-126;
		tab[197]=127;
		tab[198]=-85;
		tab[199]=75;
		tab[200]=-35;
		tab[201]=-36;
		tab[202]=17;
		tab[203]=-100;
		tab[204]=-77;
		tab[205]=-13;
		tab[206]=-108;
		tab[207]=-32;
		tab[208]=-34;
		tab[209]=-101;
		tab[210]=103;
		tab[211]=21;
		tab[212]=-63;
		tab[213]=-125;
		tab[214]=31;
		tab[215]=-62;
		tab[216]=69;
		tab[217]=-97;
		tab[218]=112;
		tab[219]=-100;
		tab[220]=-24;
		tab[221]=82;
		tab[222]=77;
		tab[223]=-73;
		tab[224]=-21;
		tab[225]=76;
		tab[226]=-43;
		tab[227]=79;
		tab[228]=50;
		tab[229]=43;
		tab[230]=20;
		tab[231]=97;
		tab[232]=-104;
		tab[233]=-104;
		tab[234]=92;
		tab[235]=22;
		tab[236]=-87;
		tab[237]=-84;
		tab[238]=-75;
		tab[239]=-51;
		tab[240]=-69;
		tab[241]=-94;
		tab[242]=-37;
		tab[243]=-68;
		tab[244]=-24;
		tab[245]=13;
		tab[246]=33;
		tab[247]=74;
		tab[248]=-2;
		tab[249]=-106;
		tab[250]=-34;
		tab[251]=-16;
		tab[252]=-52;
		tab[253]=-123;
		tab[254]=49;
		tab[255]=124;
		tab[256]=-56;
		tab[257]=-52;
		tab[258]=108;
		tab[259]=-21;
		tab[260]=92;
		tab[261]=61;
		tab[262]=104;
		tab[263]=43;
		tab[264]=-12;
		tab[265]=-25;
		tab[266]=99;
		tab[267]=123;
		tab[268]=7;
		tab[269]=78;
		tab[270]=-47;
		tab[271]=-29;
		tab[272]=5;
		tab[273]=-10;
		tab[274]=-11;
		tab[275]=64;
		tab[276]=118;
		tab[277]=31;
		tab[278]=99;
		tab[279]=-64;
		tab[280]=-103;
		tab[281]=96;
		tab[282]=38;
		tab[283]=102;
		tab[284]=-17;
		tab[285]=-30;
		tab[286]=29;
		tab[287]=7;
		tab[288]=111;
		tab[289]=109;
		tab[290]=59;
		tab[291]=100;
		tab[292]=-12;
		tab[293]=-37;
		tab[294]=-94;
		tab[295]=-125;
		tab[296]=1;
		tab[297]=99;
		tab[298]=-76;
		tab[299]=19;
		tab[300]=48;
		tab[301]=31;
		tab[302]=-74;
		tab[303]=3;
		tab[304]=-114;
		tab[305]=126;
		tab[306]=-51;
		tab[307]=-105;
		tab[308]=28;
		tab[309]=-74;
		tab[310]=71;
		tab[311]=-5;
		tab[312]=70;
		tab[313]=-9;
		tab[314]=-97;
		tab[315]=-111;
		tab[316]=58;
		tab[317]=35;
		tab[318]=-1;
		tab[319]=-83;
		tab[320]=85;
		tab[321]=-59;
		tab[322]=63;
		tab[323]=80;
		tab[324]=75;
		tab[325]=7;
		tab[326]=8;
		tab[327]=-99;
		tab[328]=105;
		tab[329]=77;
		tab[330]=-38;
		tab[331]=-36;
		tab[332]=0;
		tab[333]=0;
		tab[334]=0;
		tab[335]=53;
		tab[336]=1;
		tab[337]=0;
		tab[338]=0;
		tab[339]=80;
		tab[340]=75;
		tab[341]=1;
		tab[342]=2;
		tab[343]=20;
		tab[344]=0;
		tab[345]=20;
		tab[346]=0;
		tab[347]=8;
		tab[348]=0;
		tab[349]=8;
		tab[350]=0;
		tab[351]=19;
		tab[352]=87;
		tab[353]=-95;
		tab[354]=44;
		tab[355]=0;
		tab[356]=0;
		tab[357]=0;
		tab[358]=0;
		tab[359]=2;
		tab[360]=0;
		tab[361]=0;
		tab[362]=0;
		tab[363]=0;
		tab[364]=0;
		tab[365]=0;
		tab[366]=0;
		tab[367]=9;
		tab[368]=0;
		tab[369]=0;
		tab[370]=0;
		tab[371]=0;
		tab[372]=0;
		tab[373]=0;
		tab[374]=0;
		tab[375]=0;
		tab[376]=0;
		tab[377]=0;
		tab[378]=0;
		tab[379]=0;
		tab[380]=0;
		tab[381]=0;
		tab[382]=0;
		tab[383]=0;
		tab[384]=0;
		tab[385]=112;
		tab[386]=49;
		tab[387]=47;
		tab[388]=112;
		tab[389]=50;
		tab[390]=47;
		tab[391]=112;
		tab[392]=51;
		tab[393]=47;
		tab[394]=80;
		tab[395]=75;
		tab[396]=1;
		tab[397]=2;
		tab[398]=20;
		tab[399]=0;
		tab[400]=20;
		tab[401]=0;
		tab[402]=8;
		tab[403]=0;
		tab[404]=8;
		tab[405]=0;
		tab[406]=19;
		tab[407]=87;
		tab[408]=-95;
		tab[409]=44;
		tab[410]=-99;
		tab[411]=105;
		tab[412]=77;
		tab[413]=-38;
		tab[414]=-36;
		tab[415]=0;
		tab[416]=0;
		tab[417]=0;
		tab[418]=53;
		tab[419]=1;
		tab[420]=0;
		tab[421]=0;
		tab[422]=16;
		tab[423]=0;
		tab[424]=0;
		tab[425]=0;
		tab[426]=0;
		tab[427]=0;
		tab[428]=0;
		tab[429]=0;
		tab[430]=0;
		tab[431]=0;
		tab[432]=0;
		tab[433]=0;
		tab[434]=0;
		tab[435]=0;
		tab[436]=57;
		tab[437]=0;
		tab[438]=0;
		tab[439]=0;
		tab[440]=112;
		tab[441]=49;
		tab[442]=47;
		tab[443]=112;
		tab[444]=50;
		tab[445]=47;
		tab[446]=112;
		tab[447]=51;
		tab[448]=47;
		tab[449]=88;
		tab[450]=46;
		tab[451]=99;
		tab[452]=108;
		tab[453]=97;
		tab[454]=115;
		tab[455]=115;
		tab[456]=80;
		tab[457]=75;
		tab[458]=5;
		tab[459]=6;
		tab[460]=0;
		tab[461]=0;
		tab[462]=0;
		tab[463]=0;
		tab[464]=2;
		tab[465]=0;
		tab[466]=2;
		tab[467]=0;
		tab[468]=117;
		tab[469]=0;
		tab[470]=0;
		tab[471]=0;
		tab[472]=83;
		tab[473]=1;
		tab[474]=0;
		tab[475]=0;
		tab[476]=0;
		tab[477]=0;
		IJavaProject javaProject = createJavaProject("P1", new String[] {"src"}, "bin");
		IFile jarFile = createFile("P1/lib.jar", tab);
		javaProject.setRawClasspath(new IClasspathEntry[] {JavaCore.newLibraryEntry(jarFile.getFullPath(), null, null, false)}, new NullProgressMonitor());
		assertNotNull(javaProject.findType("p1.p2.p3.X"));
	} catch(JavaModelException e) {
		e.printStackTrace();
		assertTrue(false);
	} catch (CoreException e) {
		e.printStackTrace();
		assertTrue(false);
	} finally {
		deleteProject("P1");
	}
}

/**
 * Bug 36032: JavaProject.findType() fails to find second type in source file
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=36032"
 */
public void testFindSecondaryType_Exist01() throws JavaModelException, CoreException {
	int length = SF_LENGTH - 1;
	assertTypeFound(
		"org.eclipse.jdt.core.test"+length+".Foo",
		"Foo [in Foo.java [in org.eclipse.jdt.core.test"+length+" [in src"+length+" [in TestProject]]]]"
	);
}
public void testFindSecondaryType_Exist02() throws JavaModelException, CoreException {
	int length = SF_LENGTH - 1;
	assertTypeFound(
		"org.eclipse.jdt.core.test"+length+".Secondary",
		"Secondary [in Foo.java [in org.eclipse.jdt.core.test"+length+" [in src"+length+" [in TestProject]]]]"
	);
}
public void testFindSecondaryType_Exist03() throws JavaModelException, CoreException {
	assertTypeFound(
		"org.eclipse.jdt.core.test0.Foo.InFoo",
		"InFoo [in Foo [in Foo.java [in org.eclipse.jdt.core.test0 [in src0 [in TestProject]]]]]"
	);
}
public void testFindSecondaryType_Exist04() throws JavaModelException, CoreException {
	assertTypeFound(
		"org.eclipse.jdt.core.test0.Secondary.InSecondary",
		"InSecondary [in Secondary [in Foo.java [in org.eclipse.jdt.core.test0 [in src0 [in TestProject]]]]]"
	);
}
public void testFindSecondaryType_Exist05() throws JavaModelException, CoreException {
	assertTypeFound(
		"Foo",
		"Foo [in Foo.java [in <default> [in src1 [in TestProject]]]]"
	);
}
public void testFindSecondaryType_Exist06() throws JavaModelException, CoreException {
	assertTypeFound(
		"Secondary",
		"Secondary [in Foo.java [in <default> [in src1 [in TestProject]]]]"
	);
}
// duplicate bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=72179
public void testFindSecondaryType_Bug72179() throws JavaModelException, CoreException {
	try {
		IJavaProject javaProject = createJavaProject("P", new String[] {""}, "");
		createFolder("/P/p1");
		createFile(
			"/P/p1/jc.java",
			"package p1;\n" +
			"class jc008{}\n" +
			"class jc009{}\n" +
			"class jc010 extends jc009 {\n" +
			"	jc008 a;\n" +
			"}\n"
		);
		IType type = javaProject.findType("p1", "jc008", new NullProgressMonitor());
		assertTrue("type 'jc008' should exist!", type != null && type.exists());
		assertEquals("Expected type 'jc008' NOT found!",
			"class jc008 [in jc.java [in p1 [in <project root> [in P]]]]",
			type.toString()
		);
		type = javaProject.findType("p1", "jc009", new NullProgressMonitor());
		assertTrue("type 'jc009' should exist!", type != null && type.exists());
		assertEquals("Expected type 'jc009' NOT found!",
			"class jc009 [in jc.java [in p1 [in <project root> [in P]]]]",
			type.toString()
		);
		type = javaProject.findType("p1", "jc010", new NullProgressMonitor());
		assertTrue("type 'jc010' should exist!", type != null && type.exists());
		assertEquals("Expected type 'jc010' NOT found!",
			"class jc010 [in jc.java [in p1 [in <project root> [in P]]]]\n" +
			"  jc008 a",
			type.toString()
		);
	} finally {
		deleteProject("P");
	}
}
public void testFindSecondaryType_NotFound01() throws JavaModelException, CoreException {
	assertTypeUnknown("test.Foo");
}
public void testFindSecondaryType_NotFound02() throws JavaModelException, CoreException {
	assertTypeUnknown("InFoo");
}
public void testFindSecondaryType_NotFound03() throws JavaModelException, CoreException {
	assertTypeUnknown("InSecondary");
}
public void testFindSecondaryType_NotFound04() throws JavaModelException, CoreException {
	assertTypeUnknown("Foo.inFoo");
}
public void testFindSecondaryType_NotFound05() throws JavaModelException, CoreException {
	assertTypeUnknown("Secondary.inBar");
}
public void testFindSecondaryType_Unknown01() throws JavaModelException, CoreException {
	assertTypeUnknown("Unknown");
}
public void testFindSecondaryType_Unknown02() throws JavaModelException, CoreException {
	assertTypeUnknown("Foo.Unknown");
}
public void testFindSecondaryType_Unknown03() throws JavaModelException, CoreException {
	assertTypeUnknown("org.eclipse.jdt.core.test.Unknown");
}

/**
 * @bug 152841: [model] IJavaProject.findType(name, monitor) doesn't find secondary type
 * @test Ensure that secondary type is found just after having created the compilation unit
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=152841"
 */
public void testBug152841() throws Exception{
	try {
		IJavaProject project= createJavaProject("P", new String[] { "src" }, new String[] { "JCL_LIB" }, "bin");
		IPackageFragmentRoot root = (IPackageFragmentRoot) project.getChildren()[0];
		IPackageFragment pack= root.createPackageFragment("p", true, null);

		String source= "package p;\n" +
		"//use Object\n" +
		"class A {\n" +
		"	public void foo(){};\n" +
		"}";
		pack.createCompilationUnit("A.java", source, true, null);

		source= "package p;\n" +
		"\n" +
		"class Test{\n" +
		"	void test(){\n" +
		"		A a= new A();\n" +
		"		test(a);\n" +
		"	}\n" +
		"	void test(Object o){\n" +
		"		o.hashCode();\n" +
		"	}\n" +
		"}";
		ICompilationUnit cu= pack.createCompilationUnit("Test.java", source, true, null);

		ASTParser parser= ASTParser.newParser(AST.JLS3);
		parser.setSource(cu);
		parser.setResolveBindings(true);
		parser.createAST(null);

		source= "package p;\n" +
		"//use C\n" +
		"interface I{}\n" +
		"class C implements I{\n" +
		"}\n" +
		"class B extends C{\n" +
		"}\n" +
		"class A extends B{" +
		"}\n" +
		"class Test{\n" +
		"	void f(){\n" +
		"		A c= new A();\n" +
		"		c.toString();\n" +
		"	}\n" +
		"}";

		ICompilationUnit unit= pack.createCompilationUnit("I.java", source, true, null);
		IType type= project.findType("p.I", (IProgressMonitor) null);
		assertNotNull(type);

		// C exists
		assertTrue(unit.getType("C").exists());

		// but can't be found
		type= project.findType("p.C", (IProgressMonitor) null);
		assertNotNull(type);
	}
	finally {
		deleteProject("P");
	}
}
}
