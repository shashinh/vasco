class Main {
	public static void main (String [] args) {
		A a = new A ();
		a.foo();
	}
}

class A {
	int [] x;

	void foo() {
		int i = 10;
		x = get();

		while (i > 0) {
			System.out.println("hi");
			i --;
		}
	}

	int [] get() {
		return new int[10];
	}
}
