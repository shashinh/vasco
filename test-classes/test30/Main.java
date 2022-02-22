class Main{
	public static void main(String [] args){
		A a1 = new A();
		A a2 = new A();
		F f = new F();
		
		a1.f = a2.f;
		//a1.f = null;
		
		a1.f.bar();
	}
}

class A {
	F f;
}

class F {
	void bar() {} 
}

