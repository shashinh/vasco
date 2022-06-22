import java.lang.reflect.*;

class Main {
	static {
		try {
			Class cl = Class.forName("A");
			cl = Class.forName("B");
			cl = Class.forName("F");
		} catch (Exception ex) { System.out.println(ex); }
	}

	public static void main (String [] args) throws CloneNotSupportedException {
		A a = new A();
		A b = new A();
		A c = new A();
		A d = new A();
		A e = new A();
		A f = new A();

		B x = new B();
		x.foo();

		B y = (B) x.clone();

		y.foo();

	}
}

class A {
	F f;
}

class B implements Cloneable {
	void foo() { }

	public Object clone() throws CloneNotSupportedException {
		return super.clone();
		//return new B();
	}
} 

class F { }
