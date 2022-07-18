class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();

		C c = new C();
		C.foo(a1, a2);
	}
}

class A {
	B foo (A x, A y) {
		return new B();
	}
}

class B {
	void bar() {
		System.out.println("hello from B bar");
	}
}

class C {
	void fooBar() { }
	static B foo (A x, A y) {
		return new B();
	}
}
