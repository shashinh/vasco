class Main {
	public static void main (String [] args) {
		A a = new A();
		a.start();
	}
}

class A extends Thread {
	public void xstart() {
		System.out.println("haha this is broken");
	}
	public void run() {
		foo();
	}

	void foo() {
		System.out.println("hello world");
	}
}
