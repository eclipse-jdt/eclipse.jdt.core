/*******************************************************************************
 * Copyright (c) 2018, 2020 Andrey Loskutov and others.
 *
 * This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Andrey Loskutov - initial API and implementation
 *******************************************************************************/
package org.eclipse.jdt.core.tests.compiler.regression;

import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

import junit.framework.Test;

public class XLargeTest2 extends AbstractRegressionTest {
	static {
//		TESTS_NAMES = new String[] { "testBug550063" };
	}

	public XLargeTest2(String name) {
		super(name);
	}

	public static Test suite() {
		return buildMinimalComplianceTestSuite(testClass(), F_1_5);
	}

	public static Class<?> testClass() {
		return XLargeTest2.class;
	}

	/**
	 * Check if we hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_error() {

		int enumsCount = getEnumsCountForError();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		String expectedCompilerLog;
		if (this.complianceLevel > ClassFileConstants.JDK1_8) {
			expectedCompilerLog =
					"""
						1. ERROR in X.java (at line 2)
							enum Y {
							     ^
						The code for the static initializer is exceeding the 65535 bytes limit
						""";
		} else {
			expectedCompilerLog =
					"""
						1. ERROR in X.java (at line 6)
							switch(y){
						        case A0:
						            System.out.println("a");
						            break;
						        default:
						            System.out.println("default");
						            break;
						        }
							^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
						The code for the switch table on enum X.Y is exceeding the 65535 bytes limit
						""";
		}
		runNegativeTest(
				new String[] {
					"X.java",
					"public class X {\n" +
					"    enum Y {\n" +
						 lotOfEnums.toString() +
					"    }\n" +
					"    public static void main(String[] args) {\n" +
					"        X.Y y = X.Y.A0;\n" +
					"        switch(y){\n" + // Reported error should be here
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"    public void z2(Y y) {\n" +  // Should not report error on second switch
					"        switch(y){\n" +
					"        case A0:\n" +
					"            System.out.println(\"a\");\n" +
					"            break;\n" +
					"        default:\n" +
					"            System.out.println(\"default\");\n" +
					"            break;\n" +
					"        }\n" +
					"    }\n" +
					"}"
				},
				"----------\n" +
				expectedCompilerLog +
				"----------\n");
	}

	/**
	 * Check if we don't hit the 64Kb limit on generated table switch method code in
	 * class files. See https://bugs.eclipse.org/bugs/show_bug.cgi?id=542084
	 */
	public void testBug542084_no_error() {
		int enumsCount = getEnumsCountForSuccess();
		StringBuilder lotOfEnums = new StringBuilder(enumsCount * 7);
		for (int i = 0; i < enumsCount; i++) {
			lotOfEnums.append("A").append(i).append(", ");
		}

		// Javac can't compile such big enums
		runConformTest(
				true,
				JavacTestOptions.SKIP,
				new String[] {
						"X.java",
						"public class X {\n" +
								"    enum Y {\n" +
								lotOfEnums.toString() +
								"    }\n" +
								"    public static void main(String[] args) {\n" +
								"        X.Y y = X.Y.A0;\n" +
								"        switch(y){\n" +
								"        case A0:\n" +
								"            System.out.println(\"SUCCESS\");\n" +
								"            break;\n" +
								"        default:\n" +
								"            System.out.println(\"default\");\n" +
								"            break;\n" +
								"        }\n" +
								"    }\n" +
								"}"
				},
				"SUCCESS");
	}

	/**
	 * @return Generated code for enums that exceeds the limit
	 */
	private int getEnumsCountForError() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2800;
		}
		return 4500;
	}

	/**
	 * @return Generated code for enums that does not exceeds the limit
	 */
	private int getEnumsCountForSuccess() {
		if(this.complianceLevel > ClassFileConstants.JDK1_8) {
			return 2300;
		}
		return 4300;
	}

	public void testBug550063() {
		runConformTest(
			new String[] {
				"p001/bip.java",
				"package p001;\n" +
				"\n" +
				getManyInterfaceDeclarations() +
				"\n" +
				"class bip implements brj, brk, cem, cen, cey, cez, cfk, cfl, cgu, cgx, che, chh, chq, chr, cji, cjj, ckk, ckl, clb, clc, clf, cli, cnk,\n" +
				"	cnl, cok, cqa, cqd, cqw, cqx, crs, crv, csu, csv, ctq, ctt, cvg, cvj, cvo, cvp, cwk, cwn, cwu, cww, cxh, cxk, daz, dba, dbr, dbu, dck,\n" +
				"	dcl, deh, dei, dep, deq, dff, dfg, dfl, dfo, dsp, dss, dtp, dtq, dtt, dtw, duj, duk, dvm, dvp, dvs, dvv, dwe, dwh, dxd, dxg, dyq, dys,\n" +
				"	dyv, dyw, dzh, dzk, dzn, dzo, dzx, eaa, ecw, ecx, edr, eds, efc, efd, eiw, eiz, ejy, ekb, emi, eml, eor, eou, epe, eph, epk, epl, eqi,\n" +
				"	eqj, erv, erw, etd, etg, etm, eto, fbc, fbd, feu, fev, ffc, fff, fgf, fgh, fgo, fgp, fhm, fhn, fib, fki, fkj, fkw, fkx, fmh, fmk, fnk,\n" +
				"	fnl, fnz, foc, fof, foi, fvk, fvn, fvv, fvw, fwy, fxb, fyb, fye, fyl, fym, fyv, fyy, fzq, fzs, gad, gag, gaq, gas, gav, gax, gbc, gbd,\n" +
				"	gco, gcr, gdc, gdf, gdn, gdq, gei, gej, gih, gik, gku, gkx, gln, glo, gmi, gmj, gmu, gmv, gpx, gpy, gqb, gqe, gqp, gqs, grb, grc, grh,\n" +
				"	gri, grn, gro, grv, grw, gtr, gtu, gxc, gvt, gvw, gwz {\n" +
				"}\n"
			});
	}

	public void testBug550063_b() {
		runNegativeTest(
			new String[] {
				"p001/bip.java",
				"package p001;\n" +
				"\n" +
				getManyInterfaceDeclarations() +
				"\n" +
				"class bop implements missing,\n" +
				"	brj, brk, cem, cen, cey, cez, cfk, cfl, cgu, cgx, che, chh, chq, chr, cji, cjj, ckk, ckl, clb, clc, clf, cli, cnk,\n" +
				"	cnl, cok, cqa, cqd, cqw, cqx, crs, crv, csu, csv, ctq, ctt, cvg, cvj, cvo, cvp, cwk, cwn, cwu, cww, cxh, cxk, daz, dba, dbr, dbu, dck,\n" +
				"	dcl, deh, dei, dep, deq, dff, dfg, dfl, dfo, dsp, dss, dtp, dtq, dtt, dtw, duj, duk, dvm, dvp, dvs, dvv, dwe, dwh, dxd, dxg, dyq, dys,\n" +
				"	dyv, dyw, dzh, dzk, dzn, dzo, dzx, eaa, ecw, ecx, edr, eds, efc, efd, eiw, eiz, ejy, ekb, emi, eml, eor, eou, epe, eph, epk, epl, eqi,\n" +
				"	eqj, erv, erw, etd, etg, etm, eto, fbc, fbd, feu, fev, ffc, fff, fgf, fgh, fgo, fgp, fhm, fhn, fib, fki, fkj, fkw, fkx, fmh, fmk, fnk,\n" +
				"	fnl, fnz, foc, fof, foi, fvk, fvn, fvv, fvw, fwy, fxb, fyb, fye, fyl, fym, fyv, fyy, fzq, fzs, gad, gag, gaq, gas, gav, gax, gbc, gbd,\n" +
				"	gco, gcr, gdc, gdf, gdn, gdq, gei, gej, gih, gik, gku, gkx, gln, glo, gmi, gmj, gmu, gmv, gpx, gpy, gqb, gqe, gqp, gqs, grb, grc, grh,\n" +
				"	gri, grn, gro, grv, grw, gtr, gtu, gxc, gvt, gvw, gwz {\n" +
				"}\n"
			},
			"""
				----------
				1. ERROR in p001\\bip.java (at line 200)
					class bop implements missing,
					                     ^^^^^^^
				missing cannot be resolved to a type
				----------
				""");
	}

	private String getManyInterfaceDeclarations() {
		return 	"""
			interface brj {}
			interface brk {}
			interface cem {}
			interface cen {}
			interface cey {}
			interface cez {}
			interface cfk {}
			interface cfl {}
			interface cgu {}
			interface cgx {}
			interface che {}
			interface chh {}
			interface chq {}
			interface chr {}
			interface cji {}
			interface cjj {}
			interface ckk {}
			interface ckl {}
			interface clb {}
			interface clc {}
			interface clf {}
			interface cli {}
			interface cnk {}
			interface cnl {}
			interface cok {}
			interface cqa {}
			interface cqd {}
			interface cqw {}
			interface cqx {}
			interface crs {}
			interface crv {}
			interface csu {}
			interface csv {}
			interface ctq {}
			interface ctt {}
			interface cvg {}
			interface cvj {}
			interface cvo {}
			interface cvp {}
			interface cwk {}
			interface cwn {}
			interface cwu {}
			interface cww {}
			interface cxh {}
			interface cxk {}
			interface daz {}
			interface dba {}
			interface dbr {}
			interface dbu {}
			interface dck {}
			interface dcl {}
			interface deh {}
			interface dei {}
			interface dep {}
			interface deq {}
			interface dff {}
			interface dfg {}
			interface dfl {}
			interface dfo {}
			interface dsp {}
			interface dss {}
			interface dtp {}
			interface dtq {}
			interface dtt {}
			interface dtw {}
			interface duj {}
			interface duk {}
			interface dvm {}
			interface dvp {}
			interface dvs {}
			interface dvv {}
			interface dwe {}
			interface dwh {}
			interface dxd {}
			interface dxg {}
			interface dyq {}
			interface dys {}
			interface dyv {}
			interface dyw {}
			interface dzh {}
			interface dzk {}
			interface dzn {}
			interface dzo {}
			interface dzx {}
			interface eaa {}
			interface ecw {}
			interface ecx {}
			interface edr {}
			interface eds {}
			interface efc {}
			interface efd {}
			interface eiw {}
			interface eiz {}
			interface ejy {}
			interface ekb {}
			interface emi {}
			interface eml {}
			interface eor {}
			interface eou {}
			interface epe {}
			interface eph {}
			interface epk {}
			interface epl {}
			interface eqi {}
			interface eqj {}
			interface erv {}
			interface erw {}
			interface etd {}
			interface etg {}
			interface etm {}
			interface eto {}
			interface fbc {}
			interface fbd {}
			interface feu {}
			interface fev {}
			interface ffc {}
			interface fff {}
			interface fgf {}
			interface fgh {}
			interface fgo {}
			interface fgp {}
			interface fhm {}
			interface fhn {}
			interface fib {}
			interface fki {}
			interface fkj {}
			interface fkw {}
			interface fkx {}
			interface fmh {}
			interface fmk {}
			interface fnk {}
			interface fnl {}
			interface fnz {}
			interface foc {}
			interface fof {}
			interface foi {}
			interface fvk {}
			interface fvn {}
			interface fvv {}
			interface fvw {}
			interface fwy {}
			interface fxb {}
			interface fyb {}
			interface fye {}
			interface fyl {}
			interface fym {}
			interface fyv {}
			interface fyy {}
			interface fzq {}
			interface fzs {}
			interface gad {}
			interface gag {}
			interface gaq {}
			interface gas {}
			interface gav {}
			interface gax {}
			interface gbc {}
			interface gbd {}
			interface gco {}
			interface gcr {}
			interface gdc {}
			interface gdf {}
			interface gdn {}
			interface gdq {}
			interface gei {}
			interface gej {}
			interface gih {}
			interface gik {}
			interface gku {}
			interface gkx {}
			interface gln {}
			interface glo {}
			interface gmi {}
			interface gmj {}
			interface gmu {}
			interface gmv {}
			interface gpx {}
			interface gpy {}
			interface gqb {}
			interface gqe {}
			interface gqp {}
			interface gqs {}
			interface grb {}
			interface grc {}
			interface grh {}
			interface gri {}
			interface grn {}
			interface gro {}
			interface grv {}
			interface grw {}
			interface gtr {}
			interface gtu {}
			interface gvt {}
			interface gvw {}
			interface gwz {}
			interface gxc {}
			""";
	}
	public void testBug550480() {
		StringBuilder source = new StringBuilder();
		source.append("package p;\n");
		String[] names = new String[571];
		for (int i = 0; i < 571; i++) {
			names[i] = "I"+i;
			source.append("interface ").append(names[i]).append(" {}\n");
		}
		source.append("public abstract class hft implements ");
		source.append(String.join(", ", names));
		source.append("\n{\n}\n");
		runConformTest(
			new String[] {
				"p/hft.java",
				source.toString()
			});
	}

	/**
	 * Test that using many generic type arguments doesn't result in a compiler hang.
	 * See: https://github.com/eclipse-jdt/eclipse.jdt.core/issues/177
	 */
	public void testManyGenericsHangGh177() {
		this.runConformTest(
			new String[] {
				"C0.java",
				"""
				public class C0
				<
				A1 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A2 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A3 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A4 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A5 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A6 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A7 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A8 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A9 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A10 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A11 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A12 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A13 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A14 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A15 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A16 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A17 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A18 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A19 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>,
				A20 extends C0<A1, A2, A3, A4, A5, A6, A7, A8, A9, A10, A11, A12, A13, A14, A15, A16, A17, A18, A19, A20>
				>
				{
					public A1 a1 = null;
					public A1 getA1() {
						return a1;
					}
					public static void main (String[] args) {
					}
				}
				"""
			},
			""
		);
	}

}
