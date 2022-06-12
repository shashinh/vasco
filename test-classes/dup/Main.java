class Main {
	static {
		try {
			Class clA = Class.forName("A");
			Class clB = Class.forName("B");
			Class clF = Class.forName("F");
		} catch (Exception ex) { }
	}

	public static void main (String [] args) {

		A a = new A();
		F f = new F();
		a.f = f;
		(new B()).foo(a);
	}

}

class A { 
	F f;
}

class B {
	void foo (A x) {
		A a = new A();
	}
}
class F { }
