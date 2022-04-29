class Main {
	public static void main (String [] args) {
		try {
			A a1 = new A();
			A a2 = new A();

			a1.start();
			a2.start();

			a1.join();
			a2.join();
		} catch (Exception ex) { }
	}
}

class A extends Thread {
	public	void run () {
		B b = new B();
	}
}

class B {}
