import java.lang.reflect.*;

class Main {
	static {
		try {
			Class.forName("A");
			Class.forName("B");
		} catch (Exception ex) {}
	}

	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		B b1 = new B();
		B b2 = new B();

//		a1.f = b1;
//		a2.f = b2;

		int x = 10;
		while (x > 0) {
			a2 = a1;
			a1 = a3;
			a1.foo();
		a2.foo();
		a3.foo();

			x--;
		}


	}

}

class A {
	B f;
	void foo () { }
}

class B { }
