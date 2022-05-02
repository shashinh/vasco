class Main {
	public static void main (String [] args) {
		A a = new A();
		a.foo();
	}
}

class A {
	void foo(){
		B b = new B();
		b.bar();
	}
}

class B {
	void bar() {
		C c = new C();
		c.fooBar();
	}
}

class C {
	void fooBar(){
		System.out.println("hello from c foobar");
	}
}
