class Main {
	static {
		try {
			Class cl;
			cl = Class.forName("A");
			cl = Class.forName("B");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a = new A ();

		B b = new B ();

		a.foo(b);

		a.bar(b);

		a.foo(b);
	}
}

class A {
	void foo(B x) {
		//does nothing
	}

	void bar(B x) {
		x.f = new B();
	}
}

class B {
	B f;
}
