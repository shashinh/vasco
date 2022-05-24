import java.lang.reflect.*;
class Main {
	public static void main (String [] args) {
		try {
			Constructor cons = B.class.getConstructor();

			B obj = (B) cons.newInstance();

			D ret = obj.foo();

		} catch (Exception ex) { System.out.println(ex.toString()); }
	}
}

class F { }

class B {
	public B() { }
	F f;
	D foo () {
		B b1 = new B();
		B b2 = new B();

		C c = new C();

		System.out.println("hello from B:foo");

		return c.bar(b1, b2);
	}
}

class C {

	D bar( B b1, B b2) {
		b1.f = new F();
		b2.f = new F();
		System.out.println("hello from C:bar");
		return new D();
	}
} 

class D { }
