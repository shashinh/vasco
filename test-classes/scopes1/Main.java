class Main {
	public static void main (String [] args) {
		try {
			A a = new A();
			a.foo();

			if(getBool()) {
				B b = new B();
				b.bar();
			}
		} catch (Exception ex) { }
	}

	static boolean getBool() { return true; }
}

class A {
	void foo(){}
}

class B {
	void bar(){}
}
