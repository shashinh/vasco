class Main {
	static {
		try {
			Class cl = Class.forName("A");
			cl = Class.forName("B");
			cl = Class.forName("C");
			cl = Class.forName("D");
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
	public static void main (String [] args) {
		B b = new B();
		
		b.bar();

		A a = (A) b;

		C c = new C();
		D d = new D();

		a.foo(c, d);
	}
}
class B extends A {
	public void bar() { }

	public void iterate() {
		System.out.println(" hello from B: iterate");
	}
}

class C {}
class D {}
