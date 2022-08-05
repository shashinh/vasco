class Main {

	static {
		try {
			Class cl;
			cl = Class.forName("P");
			cl = Class.forName("Q");
			cl = Class.forName("R");
			cl = Class.forName("S");
		} catch (Exception ex) { }
	}
	public static void main (String [] args) {
		//A a = new A ();
		Q q = new Q();
		R r = new R();
		S s = new S();
		A.f = new P();
		A.f.bar();
	}
}

class A {
	static P f;

	void foo() {
		f.f = new P();
		f.bar();
	}

}

class P {
	static P f;
	void bar() { }
}

class Q extends P {
	static P f;
	void bar() { 
		f.bar();
	}
}

class R extends P {
	static P f;
	void bar() { 
		f.bar();
	}
}

class S extends P {
	static P f;
	void bar() { 
		f.bar();
	}
}
