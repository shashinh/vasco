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
		
		
		a2 = a1;
		
		
		int x = 10;
		while (x > 0){
			a2 = a1;
			x--;
		}
		
		a2.f.bar();
	}
}

class A {
	F f;
}

class F {
	void bar() {}
} 
