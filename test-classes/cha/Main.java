class Main {
	static {
		try {
			Class c;
			c = Class.forName("Z");
			c = Class.forName("A");
			c = Class.forName("B");
			c = Class.forName("C");
			c = Class.forName("D");
		} catch (Exception ex) { } 
	}
	public static void main (String [] args) {
		A a = new A();
		a.g = new A();
		A.f = a;

		a.g.foo();
	}
}

class Z {
	void foo() { }
}
class A extends Z {
	static A f;
	A g;

	//void foo() { }
}

class B extends A {
	void foo() { }
} 

class C extends B {}

class D extends C {
	void foo() { }
}
