class Seq<E> {
	E head;
	Seq<E> tail;

	Seq() {
		this(null, null);
	}

	boolean isEmpty() {
		return this.tail == null;
	}

	Seq(E head, Seq<E> tail) {
		this.head = head;
		this.tail = tail;
	}

	<T> Seq<Pair<E, T>> zip(Seq<T> that) {
		if (this.isEmpty() || that.isEmpty())
			return new Seq<Pair<E, T>>();
		else
			return new Seq<Pair<E, T>>(new Pair<E, T>(this.head, that.head),
					this.tail.zip(that.tail));
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		if (head != null) {
			buffer.append(head);
		}
		if (tail != null) {
			buffer.append(tail);
		}
		return String.valueOf(buffer);
	}

	public class Zipper<T> {
		Seq<Pair<E, T>> zip(Seq<T> that) {
			if (Seq.this.isEmpty() || that.isEmpty())
				return new Seq<Pair<E, T>>();
			else
				return new Seq<Pair<E, T>>(
						new Pair<E, T>(Seq.this.head, that.head),
						Seq.this.tail.zip(that.tail));
		}
	}
}

class Pair<A, B> {
	A fst;
	B snd;

	Pair(A a, B b) {
		this.fst = a;
		this.snd = b;
	}

	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("(");
		buffer.append(fst);
		buffer.append(", ");
		buffer.append(snd);
		buffer.append(")");
		return String.valueOf(buffer);
	}

	public boolean equals(Object other) {
		return other instanceof Pair && equals(fst, ((Pair) other).fst)
				&& equals(snd, ((Pair) other).snd);
	}

	private boolean equals(Object x, Object y) {
		return x == null && y == null || x != null && x.equals(y);
	}
}

public class A {
	public static void main(String[] args) {
		Seq<String> strs = new Seq<String>("a",
				new Seq<String>("b", new Seq<String>()));
		Seq<Number> nums = new Seq<Number>(new Integer(1),
				new Seq<Number>(new Double(1.5), new Seq<Number>()));
		Seq<String>.Zipper<Number> zipper = strs.new Zipper<Number>();
		Seq<Pair<String, Number>> combined = zipper.zip(nums);
		System.out.println(combined);
	}
}