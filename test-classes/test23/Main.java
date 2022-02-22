import java.util.Random;
class Main{

	public static void main(String [] args) {
	
		A a = new A();  // 0
		C c = new C();
		B b = new B();
		D d = new D();
		
		int x = 100;
		if(x > 100) {
			F f1 = new G(); //44
			a.f = f1;
		}
		else { 
			F f2 = new F(); //62
			a.f = f2;
		}
		
		
		while(x > 0){    //bci 79
			b.f = a.f;
			c.f = b.f;
			a.f = d.f;
			
			x--;
		}
		
		/* ------------------------------------ */
		// 8.f -> 62 142 44 124
		
		
		
		int y = 100;
		if(y > 100) {
			F f1 = new G(); //124
			a.f = f1;
		}
		else { 
			F f2 = new F(); //142
			a.f = f2;
		}
		
		
		while(y > 0){ //bci 159
			b.f = a.f;
			c.f = b.f;
			a.f = d.f;
			
			y--;
		}
		
		
		a.f.bar();
	}

}

class A {
	F f;
	
	void foo(){}
}

class B extends A {} 
class C extends B {} 
class D extends C {} 

class F {

	void bar() { }
} 

class G extends F {}
