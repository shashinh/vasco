import java.lang.reflect.*;

class Main {
	public static void main (String [] args) {
		try {
			Class clA = Class.forName("A");
			Object objA = clA.newInstance();
			Class clB = Class.forName("B");
			Object objB = clB.newInstance();

			Method m = clA.getDeclaredMethod("foo", B.class);
			Object ret = m.invoke(objA, objB);

			C retC = (C) ret;
			retC.bar();
		} catch (Exception ex) {
			System.out.println(ex);
		}
	}
}

class A {
	C foo (B b) {
		C ret = new C();
		return ret;
	}
}

class B {}

class C {
	void bar() { System.out.println("hello from C:bar"); }
}
