class Main {
/*
	static {
		try {
			var clA = Class.forName("A");
			var clF = Class.forName("F");
			
		} catch (ClassNotFoundException ex) {
		
		}		
	}
	*/
	
	public static void main(String [] args){
	
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		A a4 = new A();
		A a5 = new A();
		
		int x = 100;
		while (x > 0){
			a4 = a3;
			a3 = a2;
			a2 = a1;
			
			x--;
		}
		
		a4.f.bar();
	}
}

class A {
	F f;
}

class F {
	void bar() {}
} 
