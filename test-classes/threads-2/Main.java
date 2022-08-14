/*
 * base case of threads with modifications
 * 	1. uses thread.start and run
 * 	2. receiver is BOT
 */
class Main {
	static A f;

	static {
		try {
			Class cl = Class.forName("A");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		A a = new A();
		f = a;
		f.start();
	}
}

class A extends Thread {
	public void xstart() {
		System.out.println("this is the wrong start method");
	}
	public void run() {
		foo();
	}

	void foo() {
		System.out.println("hello world");
	}
}
