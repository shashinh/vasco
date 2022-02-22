import java.lang.reflect.*;

class Main {
	public static void main (String [] args) {
		try {
		
		Class clA = Class.forName("A");
		Object obj = clA.newInstance();
		//Constructor ctor = clA.getConstructor();
		obj = clA.newInstance();

		Method met = clA.getDeclaredMethod("foo");
		Object ret = met.invoke(obj);
		( (B) ret).print();
		} catch (Exception ex) {
			System.out.println(ex);
		}

	}
}

class A {
	B foo () {
		return new B();
		//System.out.println("hello from foo");
	}
}

class B {
	void print() {
		System.out.println("hello from print");
	}
}
