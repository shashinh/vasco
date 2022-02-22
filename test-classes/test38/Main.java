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
		
		int x = 10;
		int y = 10;
		int z = 10;
		while(z > 0) {
			while (x > 0){
				
				while(y > 0) {
					a2 = a1;
					y--;
				}
				
				a1 = a3;
				x--;
			}
			
			a3 = a4;
			z--;	
		}
	}
}

class A {
	F f;
}

class F {
	void bar() {}
} 
