package p1.p2.test;

import java.io.Serializable;
import java.io.IOException;

interface I {
	int id(int x, int y);
}

public abstract class A extends java.lang.Object
		implements Runnable, Cloneable, Serializable {
	public void run() {
	}

	public static class X {
		I i = (x, y)->x;
	}

	public void lambdas() {
		Runnable a = ()->
		{
			thisIsCrazy();
			iJustMet(you);
			here.number.callMe();
		};

		Func idA = x->x;

		Func idB = x->
		{
			return x;
		};

		Func idC = x->x;

		Func id2 = x->
		{
			if (x == null) {
				return null;
			} else
				return x;
		};

		ImaginableFunction<String, Integer> f = (String input)->input.length();

		ImaginableFunction<String, Integer> f2 = (String input)->
		{
			return input.length() + new LetsPretend() {
				int howMany() {
					return 42;
				}
			}

					.howMany();
		};
	}

}