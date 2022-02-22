
class Main{

	public static void main(String [] args) {
		
		
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		int y = 99;
		A a4 = new A();
		
		
		
		
		a1.foobar(y);
		
		F f1 = new F();
		F f2 = new F();
		a1.f = f1;
		a2.f = f2;
		
		int x = 10;
		while(x > 0 && y > 0){
			a2 = a1;
			a1 = a3;
			a3 = a4;
			
			x--;
			y--;
		}
		
		a1.foo().bar();
		a2.foo().bar();
		
		(new A()).foo().bar();
		
	}

}

class A {
	F f;
	
	A foo(){
	
		return this;
	}
	
	void bar(){
	
	}
	
	void foobar(int x) {
	
	}
}

class B {}
class C {}
class D {}

class F {

	void bar() { }
} 
