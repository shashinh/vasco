import java.util.Random;
class Main{

	public static void main(String [] args) {
		//Random r = new Random();
		//int x = r.nextInt(10);
		
		
		A a1 = new A(); //symRef 354 <- bci 3
		A a2 = new A(); //symRef 355 <- bci 11
		A a3 = new A(); //symRef 356 <- bci 19
		
		F f1 = new F();
		F f2 = new F();
		
		a1.f = f1;
		a2.f = f2;
		
		/* 
		int x = 10;
		while(x > 0){
			a2 = a1; //symRef 355 bci 3
			a1 = a3; //symRef 354 bci 19
			
			x--;
		}
		
		a2.f.bar();
		*/
	}

}

class A {
	F f;
	
	void foo(){}
}

class F {

	void bar() { }
} 
