class Main {
	public static void main(String [] args) {
	
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		A a4 = new A();
		/*
		B b = new B();
		C c = new C();
		D d = new D();
		*/
		
		//a1.f = new F();
		//a2.f = a1.f;
		int x = 100;
		while (x > 0){
			a4 = a3;
			a3 = a2;
			a2 = a1;
			
			x--;
		}
		
		//E e = a.foo(b, c, d);
		
	}
	
}

class A {
	F f;
	
	E foo(B x, C y, D z) { 
		AA aa = new AA();
		E res = aa.bar(x, y, z);
		
		return res;
	}
}

class AA {
	E bar(B x, C y, D z) {
		E res = new E();
		
		return res;
	}
}

class B{}
class C{}
class D{}
class E{}
class F{}
