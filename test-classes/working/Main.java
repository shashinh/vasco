class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clB = Class.forName("B");
			Class clF = Class.forName("F");
		} catch (Exception ex) { }
	}

	public static void main (String [] args) {
		B b = new B();
		F f1 = new F();
		F f2 = new F();

		A a1 = new A();
		A a2 = new A();
		a1.f = f1;
		a2.f = f2;

		b.foo(a1, a2);
//
//		B b = new B();
//		b.foo(new A());
	}
}

class A {
	F f;
}

class B {
	void foo (A x, A y) {
	}
	void foo (A x) {
	}
} 

class F { }
