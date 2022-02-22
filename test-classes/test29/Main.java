class Main {
/*
	static {
		try {
			var clA = Class.forName("A");
			var clF = Class.forName("F");
			
		} catch (ClassNotFoundException ex) {
		
		}		
	}
	*/
	
	public static void main(String [] args){
	
		A a1 = new A();
		A a2 = new A();
		A a3 = new A();
		a2.f = new F();
		a3.f = new F();
		
		int x = 10;
		do {
			a2.f = a1.f;
			x--;
		} while (x > 0);
		
		/*
		while (x > 0){
			a2.f = a1.f;
			x--;
		}*/
		
		
		/*
		n3n       BBStart <block_3>                                                                   [0x7f204de198f0] bci=[-1,50,23] rc=0 vc=0 vn=- li=- udi=- nc=0
		n77n      ificmple --> block_5 BBStart at n1n ()                                              [0x7f204de757d0] bci=[-1,52,23] rc=0 vc=0 vn=- li=- udi=- nc=2 flg=0x20
		n73n        iload  <auto slot 4>[#360  Auto] [flags 0x3 0x0 ]                                 [0x7f204de75690] bci=[-1,50,23] rc=1 vc=0 vn=- li=- udi=- nc=0
		n74n        iconst 0                                                                          [0x7f204de756e0] bci=[-1,52,23] rc=1 vc=0 vn=- li=- udi=- nc=0
		n4n       BBEnd </block_3> =====                                                              [0x7f204de19940] bci=[-1,52,23] rc=0 vc=0 vn=- li=- udi=- nc=0

		*/

		a2.f.bar();
		
		int y = 10;
		while(y > 0){
			a3.f = a2.f;
			y--;
		}
		
		a3.f.bar();
	}
}

class A {
	F f;
}

class F {
	void bar() {}
} 
