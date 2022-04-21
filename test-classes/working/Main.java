import java.lang.reflect.*;

class Main {
	static {
		try {
			Class.forName("A");
			Class.forName("B");
		} catch (Exception ex) {}
	}

	public static void main (String [] args) {
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();

		a1.f = new F();
		a2.f = new F();
		
		a1.g = new G();
		a2.g = new G();

		int x = 10;
		while(x > 10) {
			a2.f = a1.f;
			a2.g = a1.g;
			a1.f = a3.f;
			a1.g = a3.g;

			x--;

		}
	}

}

class A {
	F f;
	G g;
}

class F { }
class G { }
