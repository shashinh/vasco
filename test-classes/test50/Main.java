class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		B b1 = new B();
		B b2 = new B();

		a1.b = b1;
		a2.b = b2;

		int x = 10;
		while(x > 0) {
			a2.b = a1.b;
			a1.b = a3.b;
			x--;
		}

		C ret = a1.foo(b2);
	}
}

class A {
	B b;
	C foo (B b) { return new C(); } 
}
class B {}
class C {}
