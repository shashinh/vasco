import java.lang.reflect.*;

class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clB = Class.forName("B");
			Class clF = Class.forName("F");
			Class clI = Class.forName("I");
			Class clD = Class.forName("D");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a1 = new A(); //360 --> 1-0
		a1.f = new F();

		A a2 = new A();
		a2.f = new F();

		A a3 = new A();

		A a4;
		if(getBool()) {
			a4 = a1;
		} else {
			//icall - isstatic
			if(getBool()) {
				a4 = a2;
			} else {
				a4 = a3;
			}
		}

		F f = a4.f;

		int x = 100;
		while(x>0) {
			a4 = a3;
			a3 = a2;
			a2 = a1;
			x--;
		}

		B b = new B();
		int j = 10;
		//invokevirtual - isVirtual
		D d = b.foo(a4, j, a3);

		//invokestatic - isStatic
		B.fooBar(a4, a3);

		I i = new B();
		i.baz(a3, a4);

	}

	public static boolean getBool() {
		return false;
	}
}

class A {
	F f;
}

class B implements I {
	static void fooBar(A x, A y) {
		F fx = x.f;
		F fy = y.f;
	}

	D foo(A x, int i, A y) {
		F fx = x.f;
		F fy = y.f;
		//invokespecial - isSpecial
		bar(y, x);
		
		y.f = x.f;

		return new D();
	}

	private void bar(A x, A y) {

		F fx = x.f;
		F fy = y.f;
	} 

	public void baz(A x, A y) {
		F fx = x.f;
		F fy = y.f;
	}
}

interface I {
	public void baz(A x, A y);
}

class F { }

class D { }
