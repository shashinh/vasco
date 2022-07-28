class Main {
	public static void main (String [] args) {
		A a = new A();
		a.foo();
	}
}

class A {
	private class B {

	}

	void foo() {
		B b = new B();
	}
}
