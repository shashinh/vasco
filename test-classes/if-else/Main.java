class Main {
	static {
		try {
			Class cl;
			cl = Class.forName("A");
			cl = Class.forName("B");
			cl = Class.forName("C");
		} catch (Exception ex) { }
	}

	public static void main (String [] args) {
		A a;

		if(getBool()) {
			a = new B();
		} else {
			a = new C();
		}

//		a = new B();

		a.foo();

		A x = new C ();
		x.foo();
	}

	static boolean getBool() { return false; }
}

class A { void foo() { System.out.println("A"); } }

class B extends A { 
	void foo() { 
		System.out.println("B");
		System.out.println("B");
		System.out.println("B");
		System.out.println("B");
	} 
}

class C extends A { 
	void foo() { 
		System.out.println("C");
		System.out.println("C");
	}
}

