class Main {

	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		B b1 = new B();
		B b2 = new B();
		C c1 = new C();
		C c2 = new C();
		
		a1.foo(b1, c1);
		
		a2.foo(b2, c2);
		
		int x = 99;
		a2.bar(x);	
		
		a1.fooBar("hello");
	}
}

class A {
	void foo (B b, C c) { }
	
	void bar (int x) {}
	
	void fooBar (String x){}
}

class B {} 
class C {}
