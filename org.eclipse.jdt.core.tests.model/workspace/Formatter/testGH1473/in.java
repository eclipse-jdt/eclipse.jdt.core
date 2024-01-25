class Test {
	void foo(int i) {
		int k = switch(i) {
		case 1,2,3: {
			yield 33;
		}
		case 4:
			yield 44;
		case 5:{}
			yield 55;
		default: {
			throw new RuntimeException();
		}
		};
		
		switch(k) {
		case 1,2,3: {
			k += 4;
		}
		{
			k += 55;
			break;
		}
		case 4:
			k += 43;
			break;
		default: {
			throw new RuntimeException();
		}
		}
		
		int m = switch(i) {
		case 1,2,3 -> {
			k++;
			yield 33;
		}
		case 4 -> 66;
		default -> {
			throw new RuntimeException();
		}
		};
		
		switch (m) {
		case 1,2,3 -> {
			k += 4;
		}
		case 4 -> k +=55;
		default -> {
			throw new RuntimeException();
		}
		}
	}
}