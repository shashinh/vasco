class Main {
	static {
		try {
			Class cl;
			cl = Class.forName("A");
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}

	public static void main (String [] args) {

		A a = new A();
		a.foo(5);
	}

}

//class A {
//	F f;
//	A foo (int x) {
//		while(x > 0) {
//			System.out.println(x);
//			x--;
//			this.bar(x);
//		}
//		return new A();
//	}
//
//	A bar (int x) {
//		while(x > 0) {
//			System.out.println(x);
//			x--;
//			this.foo(x);
//		}
//
//		return new A();
//	}
//}
//
class A {
	void foo(int x) {
		while(x > 0) {
			x--;
//			A a = new A();
//			a.foo(x);
			this.foo(x);
			//f.fooBar();
		}
	}
}


class F { 
	void bar() { }
	void fooBar() { }
}
