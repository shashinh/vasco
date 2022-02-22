class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		F f1 = new F();
		F f2 = new F();

		a1.f = f1;
		a2.f = f2;

		int x = 10;
		while(x > 0) {
			a3.f = a2.f;
			a2.f = a1.f;

			x--;
		}

		a2.f.foo();

	}
}

class A { F f; }

class F { void foo () { System.out.println("hello from F foo"); } }
