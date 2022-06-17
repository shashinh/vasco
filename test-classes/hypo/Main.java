class Main {
	public static void main (String [] args) {
		A a = new A();

		B b = new B();

		b.foo(a);

		a.f = new F();

		b.foo(a);
	}
}

class A {
	F f;
}

class F { }

class B {
	void foo (A x) { } 
}
