class Main {
	static {
		try {
			Class c;
			c = Class.forName("A");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a = new A ();
		a.foo();
	}
}

class A {
	A f;
	void foo() {
		A p = new A();
		A q = new A();
		A r = new A();
		A s = new A();

		q.f = new A();
		s.f = new A();

		bar(p,q);
		bar(r,s);

	}

	void bar(A x, A y) {
		int i = 5;
		while (i > 0) {
			x.f = y.f;
			i--;
		}
	}
}
