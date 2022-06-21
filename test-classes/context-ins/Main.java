class Main {
	public static void main (String [] args) {
		A a = new A();
		a.run();
	}
}

class A {
	void run() {
		B b = new B();
		F x = new F();
		F y = new F();

		b.f = x;
		b.foo();

		b.bar();

		b.f = y;
		b.foo();

	}

}

class B {
	F f;

	F foo() {
		return this.f;
	}

	void bar() {
		int z = 100;
		F p;
		while(z > 0) {
			p = this.foo();

			z--;
		}

		f.fooBar();
	}

}

class F {
	void fooBar() {
		System.out.println("visited F"); 
	}
}
