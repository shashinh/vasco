import java.lang.reflect.*;

class Main {
	static {
		try {
			Class.forName("A");
			Class.forName("F");
		} catch (Exception ex) {}
	}

	public static void main (String [] args) {
		A a1;
		A a2 = new A();
		A a3 = new A();

		if(getBool()) {
			a1 = a2;
		} else {
			a1 = a3;
		}

		a1.f = new F();
		a2.f = new F();

		int x = 10;
		while(x > 0) {
			a2.f = a1.f;
			a1.f = a3.f;
			x--;
		}

		B b = new B();

		b.foo(a1, a3);

		F f = b.bar(a2, a3);

	}

	public static boolean getBool() {
		return false;
	}

}

class A {
	F f;
	G g;
}

class F { }
class G { }

class B {
	void foo (A x, A y) {
		F i = x.f;
		F j = y.f;
	}

	F bar (A x, A y) {
		return x.f;
	}
}
