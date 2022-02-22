class Main {	

	static {
		try {
			var clA = Class.forName("A");
			var clB = Class.forName("B");
			var clC = Class.forName("C");
			var clD = Class.forName("D");
			var clE = Class.forName("E");
			
		} catch (ClassNotFoundException ex) {
		
		}		
	}
	
	public static void main (String [] args){
		
		A a;
		B b;
		C c;
		
		a = new A();
		b = new B();
		c = new C();
		
		D d = new D();
		
		E e1 = d.foo1(a, b, c);
		
		E e = new E();
		E e2 = d.foo2(a, b, e);
	}
}

class A {
	void printA() { System.out.println("hello A"); }	
}
class B {
	void printB() { System.out.println("hello B"); }	
}
class C {
	void printC() { System.out.println("hello C"); }	
}
class D {
	E foo1(A x, B y, C z){
		x.printA();
		y.printB();
		z.printC();
		return new E();
	}
	
	E foo2(A x, B y, E z){
		x.printA();
		y.printB();
		//z.printC();
		return z;
	}
}
class E {
	void printE() { System.out.println("hello E"); }	
}
