public class JNITest {
	static { System.loadLibrary("native"); }

	public static void main(String [] args) {
	
	
		new JNITest().sayHello();
		
		A a1 = new JNITest().getData();
		
		
		A a2 = new A();
		A a3 = new A();
		
		int x = 100;
		
		while (x > 0) {
			a2 = a1;
			a1 = a3;
			
			x--;
		}
		
	}

	private native void sayHello();
	
	private native A getData();
}

class A {
	F f;
	
	void foo() {}
}

class F { }
