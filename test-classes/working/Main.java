import java.lang.reflect.*;
class Main {
	public static void main (String [] args) {
		try {
			Constructor cons = B.class.getConstructor();

			B obj = (B) cons.newInstance();

			B ret = obj.foo();

			ret.f = new F();
		} catch (Exception ex) { System.out.println(ex.toString()); }
	}
}

class F { }

class B {
	public B() { }
	F f;
	B foo () {
		B b = new B();
		System.out.println("hello from B");
		return b;
	}
}
