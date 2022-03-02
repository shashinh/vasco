class Main {
	public static void main (String [] args) {
		A a = new A();
		a.f = new F();;
		a.g = new G();
		a.h = new H();
	}
}

class F {}
class G {}
class H {}

class A { F f; G g; H h; }
