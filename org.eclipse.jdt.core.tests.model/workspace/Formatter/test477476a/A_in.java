package org.example;

import java.util.Arrays;

public class Example //
extends //
Object //
implements //
Runnable//
, //
Serializable //
{
	public enum ExampleEnum //
implements //
Runnable//
, //
Serializable {
		/** doc */
		AAA//
,
		/** doc */
		BBB,
		/** doc */
		CCC//
, DDD,//
 EEE, FFF
	}

	public int[] array = { 11111//
, 22222, //
33333, //
44444//
, 55555//
 };
	public String[] array2 = //
			new String[] {//
			"aaaaa", "bbbbb"
	};
	public Object[] array3 =//
	{ null,//
			null//
	};

	public void function1(Object //
param1//
, Object param2, //
Object param3, Object param4//
) throws Exc1, Ex2, Ex3 {
		List<String> strings = Arrays.asList("aaa", //
"bbb", "ccc"//
, "ddd"//
);

		int aa = 11111, //
bb = 22222, cc = 33333//
, dd = 44444//
;

		param1//
.field1.field2.//
field3.function(//
);

		param2.function1()//
.function2(//
).function3().//
function4();

		String sss1 = this.array.length > 3 //
? "yes" //
: "no";
		String sss2 = this.array.length > 3 ? //
"yes" : //
"no";

		try (InputStream in = new IStream();//
 OutputStream out = new OStream()//
; Object o = null) {
			System.out.println(//
aa * //
(aa + bb) //
* cc * (cc //
+ aa//
)//
);
		} catch (Exception1 //
| Exception2 |//
 Exception3 e) {
			System.out.println(//
param3.function1(//
aa, //
bb//
)//
);
		}

		this.getWriter().println("aaaaa" + //
				"bbbbbb" + //
				"ccc");
	}
}