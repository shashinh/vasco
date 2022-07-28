class Main {
	public static void main (String [] args) {
//		if(getBool()) {
//			A a = new A();
//			a.foo();
//		} else {
//			B b = new B();
//			C c = new C();
//
//			X x = b.bar(c);
//		}
		B b = new B();
		X p = new X();
		X q = b.bar(p);
		p.fooBar();
		q.fooBar();
	}

	public static boolean getBool() {
		return true;
	}
}

class A { 
	void foo() { }
}

class B {
	X bar(X x) {
		return new X();
	}
} 

class X { 
	void fooBar() { }
}

class C { }
