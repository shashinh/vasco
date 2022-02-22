class Main {

	public static void main(String [] args){

		A a1, a2;
		a1 = new A();
		//a2 = new A();
		
		a1.f.bar();
	}
}

class A {
	F f;
	
	public A() { f = new F(); }
	void foo() {}
}

class F {
	void bar() {}
}
