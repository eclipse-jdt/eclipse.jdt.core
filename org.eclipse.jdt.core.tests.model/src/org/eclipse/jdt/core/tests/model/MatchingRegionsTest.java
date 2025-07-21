/*******************************************************************************
 * Copyright (c) 2000, 2020 IBM Corporation and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.model;

import junit.framework.Test;
import org.eclipse.jdt.core.search.SearchPattern;

/**
 * Class to test the matching regions API method added on {@link SearchPattern}
 *
 * @see SearchPattern#getMatchingRegions(String, String, int)
 * @see "https://bugs.eclipse.org/bugs/show_bug.cgi?id=218605"
 *
 * @since 3.5
 */
public class MatchingRegionsTest extends AbstractJavaModelTests {

public MatchingRegionsTest(String name) {
	super(name);
}

public static Test suite() {
	return buildTestSuite(MatchingRegionsTest.class);
}

/*
 * Print regions in a string.
 */
private String printRegions(String name, int[] regions) {
	if (regions == null) return null;
	if (regions.length == 0) return name;
	StringBuilder buffer = new StringBuilder();
	int start = 0;
	for (int i=0; i<regions.length; i+=2) {
		int segmentStart = regions[i];
		int rLength = regions[i+1];
		if (start != segmentStart) {
			if (start > 0) buffer.append(']');
			buffer.append(name.substring(start, segmentStart));
			buffer.append('[');
		} else if (start == 0) {
			buffer.append('[');
		}
		buffer.append(name.substring(segmentStart, segmentStart+rLength));
		start = segmentStart+rLength;
	}
	buffer.append(']');
	int nLength= name.length();
	if (nLength > start) {
		buffer.append(name.substring(start, nLength));
	}
	return buffer.toString();
}

// Tests generated while running JDT/Core Model tests
public void test0001() {
	String name = "P";
	int[] regions = SearchPattern.getMatchingRegions("P",  name, SearchPattern.R_EXACT_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[P]", printRegions(name, regions));
}
public void test0002() {
	String name = "class_path";
	int[] regions = SearchPattern.getMatchingRegions("class*path",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[class]_[path]", printRegions(name, regions));
}
public void test0003() {
	String name = "p3.p2.p";
	int[] regions = SearchPattern.getMatchingRegions("p3*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[p3].p2.p", printRegions(name, regions));
}
public void test0004() {
	String name = "j1";
	int[] regions = SearchPattern.getMatchingRegions("j?",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[j]1", printRegions(name, regions));
}
public void test0005() {
	String name = "j1";
	int[] regions = SearchPattern.getMatchingRegions("j*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[j]1", printRegions(name, regions));
}
public void test0006() {
	String name = "j7.qua.li.fied";
	int[] regions = SearchPattern.getMatchingRegions("j7.*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[j7.]qua.li.fied", printRegions(name, regions));
}
public void test0007() {
	String name = "j7.qua.li.fied";
	int[] regions = SearchPattern.getMatchingRegions("j7.*.*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[j7.]qua[.]li.fied", printRegions(name, regions));
}
public void test0008() {
	String name = "java.lang";
	int[] regions = SearchPattern.getMatchingRegions("????.????",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "java[.]lang", printRegions(name, regions));
}
public void test0009() {
	String name = "java";
	int[] regions = SearchPattern.getMatchingRegions("*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "java", printRegions(name, regions));
}
public void test0010() {
	String name = "p2";
	int[] regions = SearchPattern.getMatchingRegions("*p2",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[p2]", printRegions(name, regions));
}
public void test0011() {
	String name = "p3.p2.p";
	int[] regions = SearchPattern.getMatchingRegions("*p2.*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "p3.[p2.]p", printRegions(name, regions));
}
public void test0012() {
	String name = "foo/1";
	int[] regions = SearchPattern.getMatchingRegions("foo*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[foo]/1", printRegions(name, regions));
}
public void test0013() {
	String name = "p24741.A";
	int[] regions = SearchPattern.getMatchingRegions("p24741.*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[p24741.]A", printRegions(name, regions));
}
public void test0014() {
	String name = "RuntimeException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("RE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[R]untime[E]xception/java.lang//!\0", printRegions(name, regions));
}
public void test0015() {
	String name = "RuntimeException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("RException",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[R]untime[Exception]/java.lang//!\0", printRegions(name, regions));
}
public void test0016() {
	String name = "RuntimeException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("RuntimeException",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[RuntimeException]/java.lang//!\0", printRegions(name, regions));
}
public void test0017() {
	String name = "RuntimeException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("r*e*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[R]untim[e]Exception/java.lang//!\0", printRegions(name, regions));
}
public void test0018() {
	String name = "CloneNotSupportedException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("CNS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[C]lone[N]ot[S]upportedException/java.lang//!\0", printRegions(name, regions));
}
public void test0019() {
	String name = "AA/d8//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("AA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AA]/d8//\1\0", printRegions(name, regions));
}
public void test0020() {
	String name = "AbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz/c9//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("AA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[A]bcdefghijklmnopqrstuvwxyz[A]bcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyzAbcdefghijklmnopqrstuvwxyz/c9//\0\0/S", printRegions(name, regions));
}
public void test0021() {
	String name = "gen_obj";
	int[] regions = SearchPattern.getMatchingRegions("gen_???",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[gen_]obj", printRegions(name, regions));
}
public void test0022() {
	String name = "gen_exc";
	int[] regions = SearchPattern.getMatchingRegions("gen_*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[gen_]exc", printRegions(name, regions));
}
public void test0023() {
	String name = "qgen_obj";
	int[] regions = SearchPattern.getMatchingRegions("?gen_*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "q[gen_]obj", printRegions(name, regions));
}
public void test0024() {
	String name = "qgen_run";
	int[] regions = SearchPattern.getMatchingRegions("qgen_*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[qgen_]run", printRegions(name, regions));
}
public void test0025() {
	String name = "complete/4";
	int[] regions = SearchPattern.getMatchingRegions("complete/*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[complete/]4", printRegions(name, regions));
}
public void test0026() {
	String name = "generic/1";
	int[] regions = SearchPattern.getMatchingRegions("*e?e*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "g[e]n[e]ric/1", printRegions(name, regions));
}
public void test0027() {
	String name = "generic/1";
	int[] regions = SearchPattern.getMatchingRegions("generic/*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[generic/]1", printRegions(name, regions));
}
public void test0028() {
	String name = "A/e8//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[A]/e8//\1\0", printRegions(name, regions));
}
public void test0029() {
	String name = "java.lang";
	int[] regions = SearchPattern.getMatchingRegions("*.lang",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "java[.lang]", printRegions(name, regions));
}
public void test0030() {
	String name = "Collection/b87627//?\0";
	int[] regions = SearchPattern.getMatchingRegions("*tion/*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "Collec[tion/]b87627//?\0", printRegions(name, regions));
}
public void test0031() {
	String name = "Collection";
	int[] regions = SearchPattern.getMatchingRegions("*tion",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "Collec[tion]", printRegions(name, regions));
}
public void test0032() {
	String name = "java.lang.annotation";
	int[] regions = SearchPattern.getMatchingRegions("*.lang*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "java[.lang].annotation", printRegions(name, regions));
}
public void test0033() {
	String name = "pack.age.Test";
	int[] regions = SearchPattern.getMatchingRegions("*.test*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "pack.age[.Test]", printRegions(name, regions));
}
public void test0034() {
	String name = "b124645.test";
	int[] regions = SearchPattern.getMatchingRegions("b12*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[b12]4645.test", printRegions(name, regions));
}
public void test0035() {
	String name = "ELPM/pack//!\0";
	int[] regions = SearchPattern.getMatchingRegions("e*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[E]LPM/pack//!\0", printRegions(name, regions));
}
public void test0036() {
	String name = "IDocumentExtension3";
	int[] regions = SearchPattern.getMatchingRegions("IDE3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[ID]ocument[E]xtension[3]", printRegions(name, regions));
}
public void test0037() {
	String name = "IDocumentExtension135";
	int[] regions = SearchPattern.getMatchingRegions("IDE3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[ID]ocument[E]xtension1[3]5", printRegions(name, regions));
}
public void test0038() {
	String name = "IDocumentProviderExtension3";
	int[] regions = SearchPattern.getMatchingRegions("IDPE3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[ID]ocument[P]rovider[E]xtension[3]", printRegions(name, regions));
}
public void test0039() {
	String name = "IDocumentProviderExtension12345";
	int[] regions = SearchPattern.getMatchingRegions("IDPE3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[ID]ocument[P]rovider[E]xtension12[3]45", printRegions(name, regions));
}
public void test0040() {
	String name = "IPerspectiveListener3";
	int[] regions = SearchPattern.getMatchingRegions("IPL3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[IP]erspective[L]istener[3]", printRegions(name, regions));
}
public void test0041() {
	String name = "IPropertySource2";
	int[] regions = SearchPattern.getMatchingRegions("IPS2",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[IP]roperty[S]ource[2]", printRegions(name, regions));
}
public void test0042() {
	String name = "IWorkbenchWindowPulldownDelegate2";
	int[] regions = SearchPattern.getMatchingRegions("IWWPD2",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 10, regions.length);
	assertEquals("Unexpected matching regions", "[IW]orkbench[W]indow[P]ulldown[D]elegate[2]", printRegions(name, regions));
}
public void test0043() {
	String name = "UTF16DocumentScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTF16DSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[UTF16D]ocument[S]canner[S]upport", printRegions(name, regions));
}
public void test0044() {
	String name = "UTF16DocumentScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTF1DSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[UTF1]6[D]ocument[S]canner[S]upport", printRegions(name, regions));
}
public void test0045() {
	String name = "UTF1DocScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTF1DSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[UTF1D]oc[S]canner[S]upport", printRegions(name, regions));
}
public void test0046() {
	String name = "UTF16DocumentScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTF6DSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[UTF]1[6D]ocument[S]canner[S]upport", printRegions(name, regions));
}
public void test0047() {
	String name = "UTF6DocScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTF6DSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[UTF6D]oc[S]canner[S]upport", printRegions(name, regions));
}
public void test0048() {
	String name = "UTF16DocumentScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTFDSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[UTF]16[D]ocument[S]canner[S]upport", printRegions(name, regions));
}
public void test0049() {
	String name = "UTF1DocScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTFDSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[UTF]1[D]oc[S]canner[S]upport", printRegions(name, regions));
}
public void test0050() {
	String name = "UTFDocScannerSupport";
	int[] regions = SearchPattern.getMatchingRegions("UTFDSS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[UTFD]oc[S]canner[S]upport", printRegions(name, regions));
}
public void test0051() {
	String name = "AaAaAa";
	int[] regions = SearchPattern.getMatchingRegions("AA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[A]a[A]aAa", printRegions(name, regions));
}
public void test0052() {
	String name = "AxxAyy";
	int[] regions = SearchPattern.getMatchingRegions("AA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[A]xx[A]yy", printRegions(name, regions));
}
public void test0053() {
	String name = "AAxx";
	int[] regions = SearchPattern.getMatchingRegions("AAx",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AAx]x", printRegions(name, regions));
}
public void test0054() {
	String name = "AxxAyy";
	int[] regions = SearchPattern.getMatchingRegions("AxxA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AxxA]yy", printRegions(name, regions));
}
public void test0055() {
	String name = "AAa";
	int[] regions = SearchPattern.getMatchingRegions("AAa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AAa]", printRegions(name, regions));
}
public void test0056() {
	String name = "AaAaAa";
	int[] regions = SearchPattern.getMatchingRegions("AAa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[A]a[Aa]Aa", printRegions(name, regions));
}
public void test0057() {
	String name = "AxAyAz";
	int[] regions = SearchPattern.getMatchingRegions("AxA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AxA]yAz", printRegions(name, regions));
}
public void test0058() {
	String name = "AxxAyy";
	int[] regions = SearchPattern.getMatchingRegions("AxA",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ax]x[A]yy", printRegions(name, regions));
}
public void test0059() {
	String name = "A1/#/?\0/pack";
	int[] regions = SearchPattern.getMatchingRegions("a*a**",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[A]1/#/?\0/p[a]ck", printRegions(name, regions));
}
public void test0060() {
	String name = "AAAA";
	int[] regions = SearchPattern.getMatchingRegions("a*a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[AA]AA", printRegions(name, regions));
}
public void test0061() {
	String name = "aMethodWith1Digit";
	int[] regions = SearchPattern.getMatchingRegions("aMWD",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[aM]ethod[W]ith1[D]igit", printRegions(name, regions));
}
public void test0062() {
	String name = "aMethodWithNothingSpecial";
	int[] regions = SearchPattern.getMatchingRegions("aMW",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[aM]ethod[W]ithNothingSpecial", printRegions(name, regions));
}
public void test0063() {
	String name = "aMethodWithNothingSpecial";
	int[] regions = SearchPattern.getMatchingRegions("aMethod",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[aMethod]WithNothingSpecial", printRegions(name, regions));
}
public void test0064() {
	String name = "aMethodWith1Digit";
	int[] regions = SearchPattern.getMatchingRegions("aMethodWith1",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[aMethodWith1]Digit", printRegions(name, regions));
}
public void test0065() {
	String name = "aMethodWithNothingSpecial";
	int[] regions = SearchPattern.getMatchingRegions("*method*with*a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "a[MethodWith]NothingSpeci[a]l", printRegions(name, regions));
}
public void test0066() {
	String name = "aMethodWith1Digit";
	int[] regions = SearchPattern.getMatchingRegions("aMW1D",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[aM]ethod[W]ith[1D]igit", printRegions(name, regions));
}
public void test0067() {
	String name = "aMethodWith1Or2_Or_3_Or__4__DigitsAnd_Several_Underscores";
	int[] regions = SearchPattern.getMatchingRegions("aMWOOODASU",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 18, regions.length);
	assertEquals("Unexpected matching regions", "[aM]ethod[W]ith1[O]r2_[O]r_3_[O]r__4__[D]igits[A]nd_[S]everal_[U]nderscores", printRegions(name, regions));
}
public void test0068() {
	String name = "aFieldWithS$Dollar";
	int[] regions = SearchPattern.getMatchingRegions("aFWSD",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[aF]ield[W]ith[S]$[D]ollar", printRegions(name, regions));
}
public void test0069() {
	String name = "aFieldWith$Several$DollarslAnd1DigitAnd_1Underscore";
	int[] regions = SearchPattern.getMatchingRegions("aFWSD",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 8, regions.length);
	assertEquals("Unexpected matching regions", "[aF]ield[W]ith$[S]everal$[D]ollarslAnd1DigitAnd_1Underscore", printRegions(name, regions));
}
public void test0070() {
	String name = "aFieldWithS$Dollar";
	int[] regions = SearchPattern.getMatchingRegions("aFWS$",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[aF]ield[W]ith[S$]Dollar", printRegions(name, regions));
}
public void test0071() {
	String name = "aStrangeFieldWith$$$$$$$$$$$$$$$SeveraContiguousDollars";
	int[] regions = SearchPattern.getMatchingRegions("aSFWSCD",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 12, regions.length);
	assertEquals("Unexpected matching regions", "[aS]trange[F]ield[W]ith$$$$$$$$$$$$$$$[S]evera[C]ontiguous[D]ollars", printRegions(name, regions));
}
public void test0072() {
	String name = "otherFieldWhichStartsWithAnotherLetter";
	int[] regions = SearchPattern.getMatchingRegions("oF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[o]ther[F]ieldWhichStartsWithAnotherLetter", printRegions(name, regions));
}
public void test0073() {
	String name = "oF";
	int[] regions = SearchPattern.getMatchingRegions("oF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[oF]", printRegions(name, regions));
}
public void test0074() {
	String name = "foo/1";
	int[] regions = SearchPattern.getMatchingRegions("*/1",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "foo[/1]", printRegions(name, regions));
}
public void test0075() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ash[M]ap", printRegions(name, regions));
}
public void test0076() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ax[M]apxxxx", printRegions(name, regions));
}
public void test0077() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HaM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]sh[M]ap", printRegions(name, regions));
}
public void test0078() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HaM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]x[M]apxxxx", printRegions(name, regions));
}
public void test0079() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HashM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[HashM]ap", printRegions(name, regions));
}
public void test0080() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HMa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ash[Ma]p", printRegions(name, regions));
}
public void test0081() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HMa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ax[Ma]pxxxx", printRegions(name, regions));
}
public void test0082() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HaMa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]sh[Ma]p", printRegions(name, regions));
}
public void test0083() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HaMa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]x[Ma]pxxxx", printRegions(name, regions));
}
public void test0084() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HashMa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[HashMa]p", printRegions(name, regions));
}
public void test0085() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HMap",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ash[Map]", printRegions(name, regions));
}
public void test0086() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HMap",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ax[Map]xxxx", printRegions(name, regions));
}
public void test0087() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HaMap",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]sh[Map]", printRegions(name, regions));
}
public void test0088() {
	String name = "HaxMapxxxx";
	int[] regions = SearchPattern.getMatchingRegions("HaMap",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ha]x[Map]xxxx", printRegions(name, regions));
}
public void test0089() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("HashMap",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[HashMap]", printRegions(name, regions));
}
public void test0090() {
	String name = "NullPointerException";
	int[] regions = SearchPattern.getMatchingRegions("NuPoEx",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[Nu]ll[Po]inter[Ex]ception", printRegions(name, regions));
}
public void test0091() {
	String name = "NullPointerException";
	int[] regions = SearchPattern.getMatchingRegions("NPE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[N]ull[P]ointer[E]xception", printRegions(name, regions));
}
public void test0092() {
	String name = "NullPointerException";
	int[] regions = SearchPattern.getMatchingRegions("NullPE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[NullP]ointer[E]xception", printRegions(name, regions));
}
public void test0093() {
	String name = "TZ";
	int[] regions = SearchPattern.getMatchingRegions("TZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[TZ]", printRegions(name, regions));
}
public void test0094() {
	String name = "TimeZone";
	int[] regions = SearchPattern.getMatchingRegions("TZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[T]ime[Z]one", printRegions(name, regions));
}
public void test0095() {
	String name = "TimeZone";
	int[] regions = SearchPattern.getMatchingRegions("TiZo",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ti]me[Zo]ne", printRegions(name, regions));
}
public void test0096() {
	String name = "IllegalMonitorStateException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("IllegalMSException",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[IllegalM]onitor[S]tate[Exception]/java.lang//!\0", printRegions(name, regions));
}
public void test0097() {
	String name = "CloneNotSupportedException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("CloneNotSupportedEx",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CloneNotSupportedEx]ception/java.lang//!\0", printRegions(name, regions));
}
public void test0098() {
	String name = "CloneNotSupportedException/java.lang//!\0";
	int[] regions = SearchPattern.getMatchingRegions("CloneNotSupportedException",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CloneNotSupportedException]/java.lang//!\0", printRegions(name, regions));
}
public void test0099() {
	String name = "CxxxxCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CCase",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]xxxx[Case]xx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0100() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CCase",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]at[Case]xx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0101() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaCase",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]mel[Case]xxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0102() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaCase",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]t[Case]xx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0103() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CamelCase",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CamelCase]xxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0104() {
	String name = "CxxxxCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]xxxx[C]asexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0105() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]at[C]asexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0106() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]mel[C]asexxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0107() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]t[C]asexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0108() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CamelC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CamelC]asexxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0109() {
	String name = "CxxxxCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CCa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]xxxx[Ca]sexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0110() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CCa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[C]at[Ca]sexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0111() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaCa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]mel[Ca]sexxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0112() {
	String name = "CatCasexx/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CaCa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ca]t[Ca]sexx/b201064//\0\0/S", printRegions(name, regions));
}
public void test0113() {
	String name = "CamelCasexxEntry/b201064//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CamelCa",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CamelCa]sexxEntry/b201064//\0\0/S", printRegions(name, regions));
}
public void test0114() {
	String name = "test.Bug";
	int[] regions = SearchPattern.getMatchingRegions("*bug",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "test.[Bug]", printRegions(name, regions));
}
public void test0115() {
	String name = "pack.TestInner$Member";
	int[] regions = SearchPattern.getMatchingRegions("*member",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "pack.TestInner$[Member]", printRegions(name, regions));
}
public void test0116() {
	String name = "TestConstructor1";
	int[] regions = SearchPattern.getMatchingRegions("TestConstructor",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[TestConstructor]1", printRegions(name, regions));
}
public void test0117() {
	String name = "oneTwoThree";
	int[] regions = SearchPattern.getMatchingRegions("oTT",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[o]ne[T]wo[T]hree", printRegions(name, regions));
}
public void test0118() {
	String name = "FFFTest";
	int[] regions = SearchPattern.getMatchingRegions("FF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[FF]FTest", printRegions(name, regions));
}
public void test0119() {
	String name = "FoFoFo";
	int[] regions = SearchPattern.getMatchingRegions("FF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[F]o[F]oFo", printRegions(name, regions));
}
public void test0120() {
	String name = "IZZAException";
	int[] regions = SearchPattern.getMatchingRegions("IZZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[IZZ]AException", printRegions(name, regions));
}
public void test0121() {
	String name = "IZZBException";
	int[] regions = SearchPattern.getMatchingRegions("*exception*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "IZZB[Exception]", printRegions(name, regions));
}
public void test0122() {
	String name = "ABC/p2//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("ABC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ABC]/p2//\1\0", printRegions(name, regions));
}
public void test0123() {
	String name = "field";
	int[] regions = SearchPattern.getMatchingRegions("Fiel",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[fiel]d", printRegions(name, regions));
}
public void test0124() {
	String name = "java";
	int[] regions = SearchPattern.getMatchingRegions("Ja",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ja]va", printRegions(name, regions));
}
public void test0125() {
	String name = "XX01";
	int[] regions = SearchPattern.getMatchingRegions("xx",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[XX]01", printRegions(name, regions));
}
public void test0126() {
	String name = "NAM_TYPE_NAME_REQUESTOR";
	int[] regions = SearchPattern.getMatchingRegions("nam",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[NAM]_TYPE_NAME_REQUESTOR", printRegions(name, regions));
}
public void test0127() {
	String name = "PX/pack1.pack3//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("PX",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[PX]/pack1.pack3//\1\0", printRegions(name, regions));
}
public void test0128() {
	String name = "pack1.pack3";
	int[] regions = SearchPattern.getMatchingRegions("pack1.P",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[pack1.p]ack3", printRegions(name, regions));
}
public void test0129() {
	String name = "ZInner2";
	int[] regions = SearchPattern.getMatchingRegions("ZInner",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZInner]2", printRegions(name, regions));
}
public void test0130() {
	String name = "ZZZZ";
	int[] regions = SearchPattern.getMatchingRegions("ZZZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZZZ]Z", printRegions(name, regions));
}
public void test0131() {
	String name = "AClass2";
	int[] regions = SearchPattern.getMatchingRegions("AClas",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AClas]s2", printRegions(name, regions));
}
public void test0132() {
	String name = "CompletionInsideExtends10";
	int[] regions = SearchPattern.getMatchingRegions("CompletionInsideExtends",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionInsideExtends]10", printRegions(name, regions));
}
public void test0133() {
	String name = "CompletionInsideGenericClass";
	int[] regions = SearchPattern.getMatchingRegions("CompletionInsideGenericClas",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionInsideGenericClas]s", printRegions(name, regions));
}
public void test0134() {
	String name = "WWWCompletionInstanceof3///\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("WWWCompletionInstanceof",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[WWWCompletionInstanceof]3///\0\0/S", printRegions(name, regions));
}
public void test0135() {
	String name = "ClassWithComplexName";
	int[] regions = SearchPattern.getMatchingRegions("cla",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Cla]ssWithComplexName", printRegions(name, regions));
}
public void test0136() {
	String name = "Default";
	int[] regions = SearchPattern.getMatchingRegions("def",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Def]ault", printRegions(name, regions));
}
public void test0137() {
	String name = "SuperClass";
	int[] regions = SearchPattern.getMatchingRegions("sup",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Sup]erClass", printRegions(name, regions));
}
public void test0138() {
	String name = "Throwable";
	int[] regions = SearchPattern.getMatchingRegions("thr",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Thr]owable", printRegions(name, regions));
}
public void test0139() {
	String name = "MemberType";
	int[] regions = SearchPattern.getMatchingRegions("MemberType",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[MemberType]", printRegions(name, regions));
}
public void test0140() {
	String name = "MemberException";
	int[] regions = SearchPattern.getMatchingRegions("MemberE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[MemberE]xception", printRegions(name, regions));
}
public void test0141() {
	String name = "CloneNotSupportedException";
	int[] regions = SearchPattern.getMatchingRegions("clon",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Clon]eNotSupportedException", printRegions(name, regions));
}
public void test0142() {
	String name = "ii1";
	int[] regions = SearchPattern.getMatchingRegions("Ii",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ii]1", printRegions(name, regions));
}
public void test0143() {
	String name = "Qla1";
	int[] regions = SearchPattern.getMatchingRegions("ql",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Ql]a1", printRegions(name, regions));
}
public void test0144() {
	String name = "CompletionRepeatedOtherType///\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("CompletionRepeated",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionRepeated]OtherType///\0\0/S", printRegions(name, regions));
}
public void test0145() {
	String name = "CompletionSameClass///\1\0";
	int[] regions = SearchPattern.getMatchingRegions("CompletionSameClas",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionSameClas]s///\1\0", printRegions(name, regions));
}
public void test0146() {
	String name = "CompletionSuperType2";
	int[] regions = SearchPattern.getMatchingRegions("CompletionSuper",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionSuper]Type2", printRegions(name, regions));
}
public void test0147() {
	String name = "CompletionToplevelType1/p3//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("CompletionToplevelType1",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CompletionToplevelType1]/p3//\1\0", printRegions(name, regions));
}
public void test0148() {
	String name = "CompletionType1";
	int[] regions = SearchPattern.getMatchingRegions("CT1",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[C]ompletion[T]ype[1]", printRegions(name, regions));
}
public void test0149() {
	String name = "CT1/q2//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("CT1",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[CT1]/q2//\1\0", printRegions(name, regions));
}
public void test0150() {
	String name = "preTheFooBarsuf";
	int[] regions = SearchPattern.getMatchingRegions("prethe",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[preThe]FooBarsuf", printRegions(name, regions));
}
public void test0151() {
	String name = "preFooBarsuf";
	int[] regions = SearchPattern.getMatchingRegions("prefo",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[preFo]oBarsuf", printRegions(name, regions));
}
public void test0152() {
	String name = "preThefoFooBarsuf";
	int[] regions = SearchPattern.getMatchingRegions("prethefo",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[preThefo]FooBarsuf", printRegions(name, regions));
}
public void test0153() {
	String name = "mypackage";
	int[] regions = SearchPattern.getMatchingRegions("My",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[my]package", printRegions(name, regions));
}
public void test0154() {
	String name = "ZZZType1";
	int[] regions = SearchPattern.getMatchingRegions("ZZZTy",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZZZTy]pe1", printRegions(name, regions));
}
public void test0155() {
	String name = "Bug127628Type2/deprecation//\1\20";
	int[] regions = SearchPattern.getMatchingRegions("Bug127628Ty",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Bug127628Ty]pe2/deprecation//\1\20", printRegions(name, regions));
}
public void test0156() {
	String name = "TestEvaluationContextCompletion3";
	int[] regions = SearchPattern.getMatchingRegions("TestEvaluationContextCompletion3",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[TestEvaluationContextCompletion3]", printRegions(name, regions));
}
public void test0157() {
	String name = "AllConstructors01b";
	int[] regions = SearchPattern.getMatchingRegions("AllConstructors",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[AllConstructors]01b", printRegions(name, regions));
}
public void test0158() {
	String name = "XX2/b//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("XX",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[XX]2/b//\1\0", printRegions(name, regions));
}
public void test0159() {
	String name = "XZXSuper/test0004//\0\0/S";
	int[] regions = SearchPattern.getMatchingRegions("XZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[XZ]XSuper/test0004//\0\0/S", printRegions(name, regions));
}
public void test0160() {
	String name = "XYX";
	int[] regions = SearchPattern.getMatchingRegions("XY",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[XY]X", printRegions(name, regions));
}
public void test0161() {
	String name = "Z0022ZZ";
	int[] regions = SearchPattern.getMatchingRegions("Z0022Z",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Z0022Z]Z", printRegions(name, regions));
}
public void test0162() {
	String name = "QQAnnotation";
	int[] regions = SearchPattern.getMatchingRegions("QQAnnot",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[QQAnnot]ation", printRegions(name, regions));
}
public void test0163() {
	String name = "ZZClass";
	int[] regions = SearchPattern.getMatchingRegions("ZZ",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZZ]Class", printRegions(name, regions));
}
public void test0164() {
	String name = "AType";
	int[] regions = SearchPattern.getMatchingRegions("ATy",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ATy]pe", printRegions(name, regions));
}
public void test0165() {
	String name = "VAR";
	int[] regions = SearchPattern.getMatchingRegions("va",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[VA]R", printRegions(name, regions));
}
public void test0166() {
	String name = "Test0233Z";
	int[] regions = SearchPattern.getMatchingRegions("Test0233Z",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Test0233Z]", printRegions(name, regions));
}
public void test0167() {
	String name = "ProviderImpl";
	int[] regions = SearchPattern.getMatchingRegions("ProviderImp",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ProviderImp]l", printRegions(name, regions));
}
public void test0168() {
	String name = "Annotation";
	int[] regions = SearchPattern.getMatchingRegions("ann",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Ann]otation", printRegions(name, regions));
}
public void test0169() {
	String name = "MyEnum";
	int[] regions = SearchPattern.getMatchingRegions("MyEnum",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[MyEnum]", printRegions(name, regions));
}
public void test0170() {
	String name = "ZZZNeedsImportEnum";
	int[] regions = SearchPattern.getMatchingRegions("ZZZN",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZZZN]eedsImportEnum", printRegions(name, regions));
}
public void test0171() {
	String name = "B2";
	int[] regions = SearchPattern.getMatchingRegions("b",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[B]2", printRegions(name, regions));
}
public void test0172() {
	String name = "ZTest3";
	int[] regions = SearchPattern.getMatchingRegions("ZTes",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZTes]t3", printRegions(name, regions));
}
public void test0173() {
	String name = "MyEnum";
	int[] regions = SearchPattern.getMatchingRegions("MyEnu",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[MyEnu]m", printRegions(name, regions));
}
public void test0174() {
	String name = "Enum";
	int[] regions = SearchPattern.getMatchingRegions("enu",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Enu]m", printRegions(name, regions));
}
public void test0175() {
	String name = "BasicTestReferences/org.eclipse.jdt.core.tests//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("BasicTest",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTest]References/org.eclipse.jdt.core.tests//\1\0", printRegions(name, regions));
}
public void test0176() {
	String name = "BasicTestTypesMember";
	int[] regions = SearchPattern.getMatchingRegions("BasicTestTypesM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTestTypesM]ember", printRegions(name, regions));
}
public void test0177() {
	String name = "BasicTestTypesSecondary";
	int[] regions = SearchPattern.getMatchingRegions("BasicTestTypesS",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTestTypesS]econdary", printRegions(name, regions));
}
public void test0178() {
	String name = "BasicTestTypes";
	int[] regions = SearchPattern.getMatchingRegions("BTT",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[B]asic[T]est[T]ypes", printRegions(name, regions));
}
public void test0179() {
	String name = "ZBasicTestTypes";
	int[] regions = SearchPattern.getMatchingRegions("ZBasi",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[ZBasi]cTestTypes", printRegions(name, regions));
}
public void test0180() {
	String name = "OtherFields";
	int[] regions = SearchPattern.getMatchingRegions("oth",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Oth]erFields", printRegions(name, regions));
}
public void test0181() {
	String name = "BasicTestMethodsException1";
	int[] regions = SearchPattern.getMatchingRegions("BasicTestMethodsE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTestMethodsE]xception1", printRegions(name, regions));
}
public void test0182() {
	String name = "BasicTestMethods";
	int[] regions = SearchPattern.getMatchingRegions("ba",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Ba]sicTestMethods", printRegions(name, regions));
}
public void test0183() {
	String name = "InterruptedException";
	int[] regions = SearchPattern.getMatchingRegions("in",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[In]terruptedException", printRegions(name, regions));
}
public void test0184() {
	String name = "InterruptedException";
	int[] regions = SearchPattern.getMatchingRegions("int",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Int]erruptedException", printRegions(name, regions));
}
public void test0185() {
	String name = "BasicTestMethods";
	int[] regions = SearchPattern.getMatchingRegions("BTM",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[B]asic[T]est[M]ethods", printRegions(name, regions));
}
public void test0186() {
	String name = "BasicTestReferences/org.eclipse.jdt.core.tests//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("BasicTestRef",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTestRef]erences/org.eclipse.jdt.core.tests//\1\0", printRegions(name, regions));
}
public void test0187() {
	String name = "BasicTestTextIns";
	int[] regions = SearchPattern.getMatchingRegions("BasicTestTextIns",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[BasicTestTextIns]", printRegions(name, regions));
}
public void test0188() {
	String name = "short";
	int[] regions = SearchPattern.getMatchingRegions("S",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[s]hort", printRegions(name, regions));
}
public void test0189() {
	String name = "Victory";
	int[] regions = SearchPattern.getMatchingRegions("v",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[V]ictory", printRegions(name, regions));
}
public void test0190() {
	String name = "A.java";
	int[] regions = SearchPattern.getMatchingRegions("*.java",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "A[.java]", printRegions(name, regions));
}
public void test0191() {
	String name = "ArrayAllocationExpression/com.ibm.compiler.java.ast//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("*rr*/com.ibm.compiler.java.ast/*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "A[rr]ayAllocationExpression[/com.ibm.compiler.java.ast/]/\1\0", printRegions(name, regions));
}
public void test0192() {
	String name = "SuperReference";
	int[] regions = SearchPattern.getMatchingRegions("*rr*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "Supe[rR]eference", printRegions(name, regions));
}
public void test0193() {
	String name = "ConditionalExpression/com.ibm.compiler.java.ast//\1\0";
	int[] regions = SearchPattern.getMatchingRegions("*expression*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "Conditional[Expression]/com.ibm.compiler.java.ast//\1\0", printRegions(name, regions));
}
public void test0194() {
	String name = "boolean";
	int[] regions = SearchPattern.getMatchingRegions("Boo",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[boo]lean", printRegions(name, regions));
}

// Test generated while running JDT/Text tests
public void test0201() {
	String name = "methodCallWithParams";
	int[] regions = SearchPattern.getMatchingRegions("mCW",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[m]ethod[C]all[W]ithParams", printRegions(name, regions));
}
public void test0202() {
	String name = "methodCallWithParams";
	int[] regions = SearchPattern.getMatchingRegions("mCWith",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[m]ethod[C]all[With]Params", printRegions(name, regions));
}
public void test0203() {
	String name = "multiCamelCaseField";
	int[] regions = SearchPattern.getMatchingRegions("mCC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[m]ulti[C]amel[C]aseField", printRegions(name, regions));
}
public void test0204() {
	String name = "DuplicateFormatFlagsException/java.util//!";
	int[] regions = SearchPattern.getMatchingRegions("DF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[D]uplicate[F]ormatFlagsException/java.util//!", printRegions(name, regions));
}
public void test0205() {
	String name = "DecimalFormatSymbols/java.text//1";
	int[] regions = SearchPattern.getMatchingRegions("DF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[D]ecimal[F]ormatSymbols/java.text//1", printRegions(name, regions));
}
public void test0206() {
	String name = "DateFormat/java.text//?";
	int[] regions = SearchPattern.getMatchingRegions("DF",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[D]ate[F]ormat/java.text//?", printRegions(name, regions));
}
public void test0207() {
	String name = "hashCode";
	int[] regions = SearchPattern.getMatchingRegions("hC",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[h]ash[C]ode", printRegions(name, regions));
}
public void test0208() {
	String name = "StringBuffer/java.lang//1";
	int[] regions = SearchPattern.getMatchingRegions("StringBuffer",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[StringBuffer]/java.lang//1", printRegions(name, regions));
}
public void test0209() {
	String name = "StringBuilder/java.lang//1";
	int[] regions = SearchPattern.getMatchingRegions("SB",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[S]tring[B]uilder/java.lang//1", printRegions(name, regions));
}
public void test0210() {
	String name = "ScatteringByteChannel/java.nio.channels//?";
	int[] regions = SearchPattern.getMatchingRegions("SB",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[S]cattering[B]yteChannel/java.nio.channels//?", printRegions(name, regions));
}
public void test0211() {
	String name = "ShortBuffer/java.nio//?";
	int[] regions = SearchPattern.getMatchingRegions("SB",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[S]hort[B]uffer/java.nio//?", printRegions(name, regions));
}
public void test0212() {
	String name = "IndexOutOfBoundsException/java.lang//!";
	int[] regions = SearchPattern.getMatchingRegions("IO",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[I]ndex[O]utOfBoundsException/java.lang//!", printRegions(name, regions));
}
public void test0213() {
	String name = "InvalidObjectException/java.io//!";
	int[] regions = SearchPattern.getMatchingRegions("IO",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[I]nvalid[O]bjectException/java.io//!", printRegions(name, regions));
}
public void test0214() {
	String name = "IOException/java.io//!";
	int[] regions = SearchPattern.getMatchingRegions("IO",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[IO]Exception/java.io//!", printRegions(name, regions));
}
public void test0215() {
	String name = "JarEntry/java.util.jar//!";
	int[] regions = SearchPattern.getMatchingRegions("JaEn",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ja]r[En]try/java.util.jar//!", printRegions(name, regions));
}
public void test0216() {
	String name = "JarEntry/java.util.jar//!";
	int[] regions = SearchPattern.getMatchingRegions("JaE",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Ja]r[E]ntry/java.util.jar//!", printRegions(name, regions));
}
public void test0217() {
	String name = "InvalidObjectException/java.io//!";
	int[] regions = SearchPattern.getMatchingRegions("IOExce",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[I]nvalid[O]bject[Exce]ption/java.io//!", printRegions(name, regions));
}
public void test0218() {
	String name = "IOException/java.io//!";
	int[] regions = SearchPattern.getMatchingRegions("IOExce",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[IOExce]ption/java.io//!", printRegions(name, regions));
}
public void test0219() {
	String name = "InvalidObjectException";
	int[] regions = SearchPattern.getMatchingRegions("IOException",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "[I]nvalid[O]bject[Exception]", printRegions(name, regions));
}
public void test0220() {
	String name = "SecureCacheResponse";
	int[] regions = SearchPattern.getMatchingRegions("se",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Se]cureCacheResponse", printRegions(name, regions));
}

// Addtional 'manual' tests
public void test0300() {
	String name = "HmacCore";
	int[] regions = SearchPattern.getMatchingRegions("HMac",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Hmac]Core", printRegions(name, regions));
}
public void test0301() {
	String name = "HmacMD5";
	int[] regions = SearchPattern.getMatchingRegions("HMac",  name, SearchPattern.R_PREFIX_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Hmac]MD5", printRegions(name, regions));
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=264816
public void test0302() {
	String name = "java.util";
	int[] regions = SearchPattern.getMatchingRegions("?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "java.util", printRegions(name, regions));
}
public void test0303() {
	String name = "test";
	int[] regions = SearchPattern.getMatchingRegions("????",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "test", printRegions(name, regions));
}
public void test0304() {
	String name = "test";
	int[] regions = SearchPattern.getMatchingRegions("??*??",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "test", printRegions(name, regions));
}
public void test0305() {
	String name = "test";
	int[] regions = SearchPattern.getMatchingRegions("?*?*?*?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "test", printRegions(name, regions));
}
public void test0306() {
	String name = "test";
	int[] regions = SearchPattern.getMatchingRegions("????*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 0, regions.length);
	assertEquals("Unexpected matching regions", "test", printRegions(name, regions));
}
public void test0307() {
	String name = "test";
	int[] regions = SearchPattern.getMatchingRegions("?????",  name, SearchPattern.R_PATTERN_MATCH);
	assertNull("Unexpected regions", regions);
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=264817
public void test0308() {
	String name = "array";
	int[] regions = SearchPattern.getMatchingRegions("A*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[a]rray", printRegions(name, regions));
}
public void test0309() {
	String name = "Array";
	int[] regions = SearchPattern.getMatchingRegions("a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[A]rray", printRegions(name, regions));
}
public void test0310() {
	String name = "array";
	int[] regions = SearchPattern.getMatchingRegions("*RR*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "a[rr]ay", printRegions(name, regions));
}
public void test0311() {
	String name = "ARRAY";
	int[] regions = SearchPattern.getMatchingRegions("*rr*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "A[RR]AY", printRegions(name, regions));
}
public void test0312() {
	String name = "Array";
	int[] regions = SearchPattern.getMatchingRegions("ArRa?",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Arra]y", printRegions(name, regions));
}
public void test0313() {
	String name = "Array";
	int[] regions = SearchPattern.getMatchingRegions("aRrA?",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Arra]y", printRegions(name, regions));
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=264822
public void test0314() {
	String name = "_ActivatorImplBase";
	int[] regions = SearchPattern.getMatchingRegions("**A*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "_[A]ctivatorImplBase", printRegions(name, regions));
}
public void test0315() {
	String name = "_ActivatorImplBase";
	int[] regions = SearchPattern.getMatchingRegions("**a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "_[A]ctivatorImplBase", printRegions(name, regions));
}
public void test0316() {
	String name = "_ActivatorImplBase";
	int[] regions = SearchPattern.getMatchingRegions("**a*a**a*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "_[A]ctiv[a]torImplB[a]se", printRegions(name, regions));
}
public void test0317() {
	String name = "_ActivatorImplBase";
	int[] regions = SearchPattern.getMatchingRegions("**A*A**A*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 6, regions.length);
	assertEquals("Unexpected matching regions", "_[A]ctiv[a]torImplB[a]se", printRegions(name, regions));
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=267773
public void test0318() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("H?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[H]ashMap", printRegions(name, regions));
}
public void test0319() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("H?*M?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[H]ash[M]ap", printRegions(name, regions));
}
public void test0320() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("*?M?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "Hash[M]ap", printRegions(name, regions));
}
public void test0321() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("H?*?",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[H]ashMap", printRegions(name, regions));
}
public void test0322() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("H*?*",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[H]ashMap", printRegions(name, regions));
}
public void test0323() {
	String name = "HashMap";
	int[] regions = SearchPattern.getMatchingRegions("H*?",  name, SearchPattern.R_PATTERN_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[H]ashMap", printRegions(name, regions));
}

// bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=271284
public void test0324() {
	String name = "IOException";
	int[] regions = SearchPattern.getMatchingRegions("IOö",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertNull("Unexpected regions", regions);
}
public void test0325() {
	String name = "IOExceptiön";
	int[] regions = SearchPattern.getMatchingRegions("IOExceptiö",  name, SearchPattern.R_CAMELCASE_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[IOExceptiö]n", printRegions(name, regions));
}
public void test0326() {
	String name = "removeBarBar";
	int[] regions = SearchPattern.getMatchingRegions("bar",  name, SearchPattern.R_SUBSTRING_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "remove[Bar]Bar", printRegions(name, regions));
}
public void test0327() {
	String name = "Bar2Bar";
	int[] regions = SearchPattern.getMatchingRegions("bar",  name, SearchPattern.R_SUBSTRING_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[Bar]2Bar", printRegions(name, regions));
}
public void test0328() {
	String name = "bar1Bar";
	int[] regions = SearchPattern.getMatchingRegions("bar",  name, SearchPattern.R_SUBSTRING_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "[bar]1Bar", printRegions(name, regions));
}
public void test0329() {
	String name = "checkBackground";
	int[] regions = SearchPattern.getMatchingRegions("k",  name, SearchPattern.R_SUBSTRING_MATCH);
	assertEquals("Unexpected regions length", 2, regions.length);
	assertEquals("Unexpected matching regions", "chec[k]Background", printRegions(name, regions));
}
public void testSubword1() {
	String name = "LinkedHashMap";
	int[] regions = SearchPattern.getMatchingRegions("linkedmap",  name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[Linked]Hash[Map]", printRegions(name, regions));
}
public void testSubword2() {
	String name = "addEnlistListener";
	int[] regions = SearchPattern.getMatchingRegions("addlist",  name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected regions length", 4, regions.length);
	assertEquals("Unexpected matching regions", "[add]Enlist[List]ener", printRegions(name, regions));
}
public void testSubword_backtrack() {
	String name = "addListListener";
	int[] regions = SearchPattern.getMatchingRegions("addlisten", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "[add]List[Listen]er", printRegions(name, regions));
}
public void testSubword_backtrackAndFail() {
	String name = "addListString";
	int[] regions = SearchPattern.getMatchingRegions("addlisten", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_backtrackTwice() {
	String name = "addListListenListener";
	int[] regions = SearchPattern.getMatchingRegions("addlistener", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "[add]ListListen[Listener]", printRegions(name, regions));
}
public void testSubword_backtrackWithin() {
	String name = "addListListenerWordTest";
	int[] regions = SearchPattern.getMatchingRegions("addlistentest", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "[add]List[Listen]erWord[Test]", printRegions(name, regions));
}
public void testSubword_backtrackWithinAndFail() {
	String name = "addListListenerWordTest";
	int[] regions = SearchPattern.getMatchingRegions("addlistentestnotfound", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_backtrackStart() {
	String name = "listListener";
	int[] regions = SearchPattern.getMatchingRegions("listener", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "list[Listener]", printRegions(name, regions));
}
public void testSubword_backtrackStartAndFail() {
	String name = "listString";
	int[] regions = SearchPattern.getMatchingRegions("listener", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_fieldPrefix() {
	String name = "_field";
	int[] regions = SearchPattern.getMatchingRegions("field", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "_[field]", printRegions(name, regions));
}
public void testSubword_contentAssistFilter() {
	String name = "substring(int beginIndex)";
	int[] regions = SearchPattern.getMatchingRegions("index", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_caps_boundaries1() {
	String name = "CASE_INSENSITIVE_ORDER";
	int[] regions = SearchPattern.getMatchingRegions("ind", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_caps_boundaries2() {
	String name = "CASE_INSENSITIVE_ORDER";
	int[] regions = SearchPattern.getMatchingRegions("ini", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_caps_boundaries3() {
	String name = "CASE_INSENSITIVE_ORDER";
	int[] regions = SearchPattern.getMatchingRegions("sensitive", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", null, printRegions(name, regions));
}
public void testSubword_caps_backtracking() {
	String name = "LIST_LISTENER";
	int[] regions = SearchPattern.getMatchingRegions("listener", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "LIST_[LISTENER]", printRegions(name, regions));
}
public void testSubword_snakeCase() {
	String name = "add_list_listener";
	int[] regions = SearchPattern.getMatchingRegions("addlistener", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "[add]_list_[listener]", printRegions(name, regions));
}
public void testSubword_mixedCamelCase1() {
	String name = "IImportWizard";
	int[] regions = SearchPattern.getMatchingRegions("import", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "I[Import]Wizard", printRegions(name, regions));
}
public void testSubword_mixedCamelCase2() {
	String name = "HTMLTable";
	int[] regions = SearchPattern.getMatchingRegions("table", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "HTML[Table]", printRegions(name, regions));
}
public void testSubword_mixedCamelCase3() {
	String name = "CustomHTMLTable";
	int[] regions = SearchPattern.getMatchingRegions("table", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "CustomHTML[Table]", printRegions(name, regions));
}
public void testSubword_mixedCamelCase4() {
	String name = "ImportHTML";
	int[] regions = SearchPattern.getMatchingRegions("html", name, SearchPattern.R_SUBWORD_MATCH);
	assertEquals("Unexpected matching regions", "Import[HTML]", printRegions(name, regions));
}
}
