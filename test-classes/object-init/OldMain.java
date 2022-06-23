import java.lang.reflect.*;

class Main {
//	static {
//		try {
//			Class cl = Class.forName("A");
//			cl = Class.forName("B");
//			cl = Class.forName("F");
//		} catch (Exception ex) { System.out.println(ex); }
//	}

	public static void main (String [] args) throws CloneNotSupportedException {

		A a = new A(); // O1
		a.f = new A(); // O2
		a.f.f = new A(); // O3
		a.f.f.f = new B(); // O4

		A a2 = a.f.f;

		a.toughnut(a);

		a2.f.foo(); // B.foo if no call to toughnut present. otherwise \bot

		bar(a, a.f, a.f.f, a.f.f.f, a2, a2.f);

	}

	public static void bar(A a, A b, A c, A d, A e, A f) { } 
}

class A {
	A f;

	void foo() { }
	void toughnut(A x){
		x.f.f.f = new A();
	}

}

class B extends A {
	void foo() { }

} 

