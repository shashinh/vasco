
class Main {
	
	public static void main (String [] args) {
		B b = new B();
		b.run();
		
	}
	
}

class B {
	byte [] f;
	void run(){
		byte [] x = A.staticBar(10);
		this.f = x;
	}
}


class A {
	 static byte [] staticBar(int i) {
	 	return new byte[i];
	 }
}
