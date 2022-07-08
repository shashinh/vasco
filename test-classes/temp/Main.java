class Main {
	public static void main (String [] args) {
		if(getBool()) {
			A a = new A();
			a.foo();
		} else {
			B b = new B();
			b.bar();
		}
	}

	public static boolean getBool() { return false; } 
}

class A {
	void foo() { System.out.println("hello from A"); }
}

class B {
	void bar() { System.out.println("hello from B"); }
}
