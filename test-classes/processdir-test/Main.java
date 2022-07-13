class Main {
	public static void main (String [] args) {
		if(getBool()) {
			A a = new A ();
			a.foo();
		} else {
			A a = new B();
			a.foo();
		}
	}

	public static boolean getBool() {
		return true;
	}

}

class A {
	void foo () { System.out.println("hello from A:foo"); }
}

class B extends A {
	void foo () { System.out.println("hello from B:foo"); }
}
