package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import junit.framework.TestSuite;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.core.Util;

/**
 * @author oliviert
 */
public class EncodingTests extends ModifyingResourceTests {


	public EncodingTests(String name) {
		super(name);
	}
	
	public static Test suite() {
		TestSuite suite = new Suite(EncodingTests.class.getName());
		suite.addTest(new EncodingTests("testCreateCompilationUnitAndImportContainer"));
		return suite;
	}


	
	/**
	 * Check that the compilation unit is saved with the proper encoding.
	 */
	public void testCreateCompilationUnitAndImportContainer() throws JavaModelException, CoreException {
		String savedEncoding = null;
		try {
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			
			savedEncoding = preferences.getString(ResourcesPlugin.PREF_ENCODING);
			preferences.setValue(ResourcesPlugin.PREF_ENCODING, "UTF-8");
			
			ResourcesPlugin.getPlugin().savePluginPreferences();

			IJavaProject javaProject = createJavaProject("P", new String[] { "" }, "");
			IPackageFragment pkg = getPackageFragment("P", "", "");
			ICompilationUnit cu= pkg.createCompilationUnit("A.java", 
				("public class A {\r\n" +
				"	public static main(String[] args) {\r\n" +
				"		System.out.println(\"é\");\r\n" +
				"	}\r\n" +
				"}"), false, new NullProgressMonitor());
			assertCreation(cu);
			cu.rename("B.java", true, new NullProgressMonitor());
			cu = pkg.getCompilationUnit("B.java");
			cu.rename("A.java", true, new NullProgressMonitor());
			cu = pkg.getCompilationUnit("A.java");
			byte[] tab = new byte[90];
			tab[0]=112;
			tab[1]=117;
			tab[2]=98;
			tab[3]=108;
			tab[4]=105;
			tab[5]=99;
			tab[6]=32;
			tab[7]=99;
			tab[8]=108;
			tab[9]=97;
			tab[10]=115;
			tab[11]=115;
			tab[12]=32;
			tab[13]=65;
			tab[14]=32;
			tab[15]=123;
			tab[16]=13;
			tab[17]=10;
			tab[18]=9;
			tab[19]=112;
			tab[20]=117;
			tab[21]=98;
			tab[22]=108;
			tab[23]=105;
			tab[24]=99;
			tab[25]=32;
			tab[26]=115;
			tab[27]=116;
			tab[28]=97;
			tab[29]=116;
			tab[30]=105;
			tab[31]=99;
			tab[32]=32;
			tab[33]=109;
			tab[34]=97;
			tab[35]=105;
			tab[36]=110;
			tab[37]=40;
			tab[38]=83;
			tab[39]=116;
			tab[40]=114;
			tab[41]=105;
			tab[42]=110;
			tab[43]=103;
			tab[44]=91;
			tab[45]=93;
			tab[46]=32;
			tab[47]=97;
			tab[48]=114;
			tab[49]=103;
			tab[50]=115;
			tab[51]=41;
			tab[52]=32;
			tab[53]=123;
			tab[54]=13;
			tab[55]=10;
			tab[56]=9;
			tab[57]=9;
			tab[58]=83;
			tab[59]=121;
			tab[60]=115;
			tab[61]=116;
			tab[62]=101;
			tab[63]=109;
			tab[64]=46;
			tab[65]=111;
			tab[66]=117;
			tab[67]=116;
			tab[68]=46;
			tab[69]=112;
			tab[70]=114;
			tab[71]=105;
			tab[72]=110;
			tab[73]=116;
			tab[74]=108;
			tab[75]=110;
			tab[76]=40;
			tab[77]=34;
			tab[78]=-61;
			tab[79]=-87;
			tab[80]=34;
			tab[81]=41;
			tab[82]=59;
			tab[83]=13;
			tab[84]=10;
			tab[85]=9;
			tab[86]=125;
			tab[87]=13;
			tab[88]=10;
			tab[89]=125;
			byte[] encodedContents = Util.getResourceContentsAsByteArray(javaProject.getProject().getWorkspace().getRoot().getFile(cu.getPath()));
			assertTrue("wrong size of encoded string", tab.length == encodedContents.length);
			for (int i = 0, max = tab.length; i < max; i++) {
				assertTrue("wrong size of encoded character at" + i, tab[i] == encodedContents[i]);
			}
		} finally {
			deleteProject("P");
			
			Preferences preferences = ResourcesPlugin.getPlugin().getPluginPreferences();
			
			preferences.setValue(ResourcesPlugin.PREF_ENCODING, savedEncoding);
			
			ResourcesPlugin.getPlugin().savePluginPreferences();
		}
	}	
}
