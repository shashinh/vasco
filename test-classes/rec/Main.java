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
		A x = a.foo(5);
	}

}

class A {
	F f;
	A foo (int x) {
		while(x > 0) {
			System.out.println(x);
			x--;
			this.bar(x);
		}
		return new A();
	}

	A bar (int x) {
		while(x > 0) {
			System.out.println(x);
			x--;
			this.foo(x);
		}

		return new A();
	}
}

class F { }
