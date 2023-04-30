class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		a1.f = new A();
		a2.f = new A();
		//a3.f = new A();
		//
		int x = 5;
		while(x > 0){
			a2.f = a1.f;
			a1.f = a3.f;

			x--;
		}

		A a = new A();
		a.foo(a2.f);
	}
}

class A {
	A f;

	void foo(A x) { }
}
