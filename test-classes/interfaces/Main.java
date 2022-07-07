class Main {
	public static void main (String [] args) {
		I a = new A();
		a.foo();
	}
}

interface I {
	void foo();
}

class A implements I {
	public void foo() {
		System.out.println("hello from A:foo");
	}
}
