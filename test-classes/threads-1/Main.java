/*
 * base case of threads
 * 	1. uses thread.start and run
 * 	2. receiver is concrete (non-bot and non-null)
 */
class Main {
	public static void main (String [] args) {
		A a = new A();
		a.start();
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
