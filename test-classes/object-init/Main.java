class Main {
	public static void main (String [] args) throws Exception {
		A a = new A();
		a.f = new A();
		a.f.f = new A();

		A x = (A) a.clone();

		bar(a, a.f, a.f.f, x, x.f, x.f.f);

	}

	public static void bar (A x, A y, A z, A p, A q, A r) { }
}

class A implements Cloneable {
	A f;
	public Object clone () throws CloneNotSupportedException {
		return super.clone();
	}
}
