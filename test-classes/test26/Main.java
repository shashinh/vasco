class Main {

	static {
		try {
			var clA = Class.forName("A");
			var clF = Class.forName("F");
			
		} catch (ClassNotFoundException ex) {
		
		}		
	}
	
	
	public static void main(String [] args){
	
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		
		F f1 = new F();
		F f2 = new F();
		
		a1.f = f1;
		a2.f = f2;
		
		
		int x = 10;
		while (x > 0){
			a2 = a1;
			a1 = a3;
			x--;
		}
		
		//a1.f.bar();
		a2.f.bar();
	}
}

class A {
	F f;
}

class F {
	void bar() {}
} 
