class Main {
	public static void main(String [] args) {
		A a0 = new A(); // 353 - 0
		A a1 = new A(); // 354 - 8
		A a2 = a1.foo(a0); 

	}

}

class A {
	A foo(A a){
		return this;
	}
}
