import java.lang.reflect.*;

class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clF = Class.forName("F");
		} catch (Exception ex) { } 
	}

	public static void main (String [] args) {

		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		a1.f = new F();
		a2.f = new F();
		
		int x = 100;
		while(x > 0) {
			a2.f = a1.f;
			a1.f = a3.f;

			x--;
		}

		a1.f.foo();

	}
}

class A {
	F f;
}

class F {
	void foo () { 
	}
}
