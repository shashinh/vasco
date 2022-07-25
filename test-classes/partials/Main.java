class Main {
	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();

//		S s = new S();

		A a3 = S.foo(a1, a2);

	}

}

class A { }

class S {
	static A foo(A x, A y) {
		return new A();
	}
}
