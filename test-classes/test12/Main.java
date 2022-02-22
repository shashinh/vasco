class Main {
	public static void main(String [] args) {
	
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		
		A a4 = new A();
		A a5 = a4.foo(a1, a2, a3);
	}
}

class A {

	A foo(A p, A q, A r) {
		return r;
	}
}
