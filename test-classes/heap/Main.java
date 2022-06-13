import java.lang.reflect.*;

class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clB = Class.forName("B");
			Class clF = Class.forName("F");
		} catch (Exception ex) { } 
	}

	public static void main (String [] args) {
		A a1 = new A();
		a1.f = new F();
		A a2 = new A();
		a2.f = new F();

		B b = new B();
		F f = b.foo(a1, a2);
	}

}

class A {
	F f;
}

class B {
	F foo (A x, A y) {
		x.f = y.f;
		return y.f;
	}
}

class F { }
