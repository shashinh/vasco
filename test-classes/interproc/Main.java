class Main {
	public static void main(String [] args) {
		A a = new A();
		B b1 = new B();
		B b2 = new B();
		C c = new C();

		D d = a.foo(b1, c);
		d.bar();
		
		d = a.foo(b2, c);
		
		int x = 10;
		while(x > 0) {
			b1 = b2;
			x --;
		}
	}
}

class A {
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
