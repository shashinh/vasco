import java.lang.reflect.*;

class Main {
	public static void main (String [] args) throws Exception  {
		
			Class cl = Class.forName("B");
			Constructor ctor = cl.getConstructor();
			A a = (A) ctor.newInstance();
			//A a = (A) new B();
			a.run();
			
			
	}
	
}



class A {
	void run () { System.out.println ("hello from A run"); }
}

class B extends A {
	public B () {}
} 
