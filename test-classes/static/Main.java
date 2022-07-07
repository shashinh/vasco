class Main {
	static {
		try {
			Class c;
			c = Class.forName("A");
			c = Class.forName("F");
		} catch (Exception ex) { }
	}
		
	public static void main (String [] args) {
		F f = A.f;
		f.foo();
	}
}

class A {
	static F f;
	static {
		f = new F();
	}
}

class F {
	void foo() {
		System.out.println("hello from F:foo");
	}
}
