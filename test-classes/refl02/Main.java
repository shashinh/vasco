import java.lang.reflect.*;

class Main {
	public static void main (String [] args) throws Exception  {
		
			/*Class cl = Class.forName("M");
			
			Method m = cl.getDeclaredMethod("main", new Class[] {String[].class});
			m.invoke(null, new Object[] { null });*/
			
			String str1 = "hello world";
			String str2 = str1.replace("!/harness", "");
			
	}
	
}


class M {
	public static void main (String [] args) {
		System.out.println("hello from M:main");
	}
	
}

class N {
	public void foo (O o) {
		System.out.println("hello from N foo");
	}
}

class O {}

/*
class A {
	public A ()  throws Exception  {
	
		int x = 10;
		while(x > 0) {
			Class cl = Class.forName("B");
			Object obj = cl.newInstance();
			
			x--;
		}
	}
}

class B {
	public B ()  throws Exception   {
	
		int x = 10;
		while(x > 0) {
			Class cl = Class.forName("C");
			Object obj = cl.newInstance();
			
			x--;
		}
	}
}


class C {
	public C ()  throws Exception  {
		System.out.println("C ctor says hellooooo");
		//Class cl = Class.forName("D");
		//Object obj = cl.newInstance();
	}
}
*/

/*
class D {
	public D () {
		System.out.println("D ctor says hellooooo");
	}
}
*/
