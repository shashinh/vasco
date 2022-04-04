import java.lang.reflect.*;
class Main {
	static {
		try {
			Class.forName("A");
		} catch (Exception ex) {}
	}

	public static void main (String [] args) {
		A a = new A();
		B b1 = new B();
		B b2 = a.foo(99, null);

		String str = "hello";
		b1 = null;
		a.bar(str, b1);

		a.fooBar(b1, 99);
	}
}

class A { 
	B foo(int y, B x) {
		return new B();
	}

	void bar(String x, B y) {
		fooBarFoo(x);
	}

	void fooBar(B x, int y) {}	

	void fooBarFoo(String x) {}
}

class B {}
