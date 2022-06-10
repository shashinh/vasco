class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		a1.f = new F();
		a2.f = new F();
		a3.f = new F();

		boolean x = false;
		int y = 100;
		while(x) {
//			int z;
//			if(x) 
//				z = 99;
//			else z = 109;
			a3.f = a2.f;
			a2.f = a1.f;

			//x = !x;
			//y--;
		}

		F f1 = a3.f;
		F f2 = a2.f;
		F f3 = a1.f;
	}
}

class A {
	F f;
}

class F { }

class B {
	boolean getBool() { return false; }
}
