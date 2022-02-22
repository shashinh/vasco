class Main {	

	static {
		try {
			var clA = Class.forName("A");
			
		} catch (ClassNotFoundException ex) {
		
		}		
	}
	
	public static void main (String [] args){
		
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		A a4 = new A();
		
		a1.f = new F();
		a2.f = new F();
		a3.f = new F();
		a4.f = new F();
		
		int x = 100;
		do {
			a4.f = a3.f;
			a3.f = a2.f;
			a2.f = a1.f;
		
			x --;
		} while(x > 0);
	}
}

class A {
	F f;
}

class F { }
