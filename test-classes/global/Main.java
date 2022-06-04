class Main {
	public static void main (String [] args) {
		A a = new A();
		F f = new F();
		f.g = 99;
		B b = new B();
		b.h = 77;
		F.b = b;
		a.f = f;
		a.foo();
	}

}

class A {
	F f;

	void foo () {
		System.out.println(f.g);
		System.out.println(F.b.h);
	}
}

class F {
	int g;
	public static B b;
}

class B {
	int h;
}
