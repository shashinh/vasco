class Main {
	public static void main (String [] args) {
		
		A a = new A();
		
		for(int i = 0; i < 100; i ++) {
			System.out.println(a.foo());
		}
	}
}

class A{
	int foo () { return 1; }
}

