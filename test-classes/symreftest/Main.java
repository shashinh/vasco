class Main {
	static {
		try {
			Class c = Class.forName("A");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a = new A();
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		a.foo(a1, a2, a3);
	}
}

class A {
	void foo (A x, A y, A z) { }
}
