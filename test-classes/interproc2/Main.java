class Main {
	public static void main(String [] args) {
		A a1 = new A();
		A a2 = new A();
		B b1 = new B();
		B b2 = new B();
		C c = new C();

		/*D d = a.foo(b1, c);
		d.bar();
		
		d = a.foo(b2, c);*/
		
		a1.f = b1;
		a2.f = b2;
		
		int x = 10;
		while(x > 0) {
			a1.f = a2.f;
			x--;
		}
		
		a1.f.foobar();
	}
}

class A {
	B f;
	D foo (B b, C c) {
		D ret = new D();
		ret.f = 99;

		return ret;
	}
}

class B {
	void foobar() { System.out.println("hello"); }
}
class C{}

class D {
	int f;
	void bar () { System.out.println(f); }
}
