class Main {
	static {
		try {
			Class c;
			c = Class.forName("A");
			c = Class.forName("F");
		} catch (Exception ex) { }
	}
		
	public static void main (String [] args) {
		/*
		A f = A.f;
		//ci should show arg 1 = G
		bar(f);
		//foo should not be analyzed in this context
		f.foo();

		A a = new A();
		a.f = new A();
		a.f.f = new A();
		A.f = a;
		//at this point, the heap reachable from a should be summarised
		//ci should show args 2 3 = G (1 should remain untouched since its a var)
		fooBar(a, a.f, a.f.f);

		A.f = new A();
		*/

		B b = new B();
		C c = new C();
		b.f = c;
		c.f = b;

		A.g = b;

		//ci should show b.f and c.f = G (args 2 and 4)
		fooBarFoo(b, b.f, c, c.f);

		
	}

	public static void bar(A x) { }
	public static void fooBar(A p, A q, A r) { }
	public static void fooBarFoo(B p, C q, C r, B s) { }
}

class A {
	static A f;
	static B g;
	static {
		f = new A();
	}

	void foo() { System.out.println("hello from A:foo"); }
}

class G {}

class B {
	C f;
}

class C {
	B f;
}
