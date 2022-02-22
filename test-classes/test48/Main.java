class Main {
	public static void main(String [] args) {
		A a = new A();
		B b = new B ();

		a.f = b;
	}
}

class A {
	B f;
}

class B {}

