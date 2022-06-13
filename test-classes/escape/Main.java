import java.lang.reflect.*;
class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clC = Class.forName("C");
			//Class clB = Class.forName("B");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		a1.f = new F();
		a2.f = new F();

		B b = new B();
		b.f = new F();

		D d = b.foo(a1, a2);

		C c = new C();
		c.bar(a1, a2, b);

	}

}

class A {
	F f;
}

class B {
	F f;
	D foo(A x, A y) {

		return new D();
	}
}

class C {
	void bar (A x, A y, B z) { } 
}

class D { }

class F { } 
