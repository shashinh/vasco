class Main {
	public static void main(String [] args) {
	
		A a = new A();
		B b = new B();
		C c = new C();
		D d = new D();
		
		E e = a.foo(b, c, d);
		
	}
	
}

class A {
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
