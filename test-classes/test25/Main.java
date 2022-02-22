class Main {
	public static void main (String [] args) {
		A a;
		B b1;
		B b2;
		
		a = new A(); // bci 0
		a.f = new F(); //0.f -> bci 9
		b1 = new B(); // bci 19
		b1.f = new F(); //19.f -> bci 28
		b2 = new B(); // bci 38
		b2.f = new F(); //38.f -> bci 47
		
		//instance invokes - to be treated context-insensitively
		a.bar(b1, b2); // this -> 0, b1 -> 19, b2 -> 38
		
		a.bar(b2, b2); // this -> 0, b2-> 38, b2 -> 38
		
		int x = 10;
		while(x > 0) { //invariant - b1 -> 19, 38 | b2 -> 38
			b1 = b2;
			x--;
		}
		
		x = 10;
		while(x > 0) { //invariant - 19.f -> 28, 47 | 38.f -> 47 
			b1.f = b2.f;
			x--;
		}
		
		//static
		bar();
			
	}
	
	public static void bar() { 
		System.out.println("hello from static bar!"); 
	}
}

class A {
	F f;
	void bar(B x, B y) { 
		C c1 = new C();
		c1.foobar(y);
		C c2 = new C();
		c2.foobar(x);
		System.out.println("hello from A:bar"); }
}

class B { F f; }

class F {}
		
class C {
	void foobar(B b) {
		System.out.println("hello from C:foobar");
	}
}
