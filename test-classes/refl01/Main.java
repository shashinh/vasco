import java.lang.reflect.*;

class Main {
	public static void main(String [] args) {
		try {
			//call to clinit
			Class cl = Class.forName("A");
			
				//fetch all constructors of class
				Constructor [] ctors = cl.getConstructors();
				for(Constructor ctor : ctors)
					System.out.println("\t" + ctor);
			
			//call to default constructor
			Object obj = cl.newInstance();
			
				//another call to default constructor
				Constructor ctor = cl.getConstructor();
				obj = ctor.newInstance();
			
			//call to other constructor, determined by signature
			//ctor = cl.getConstructor(new Class[] { int.class });
			//obj = ctor.newInstance(5);
			
				//call to other other constructor
				//ctor = cl.getConstructor(new Class[] { int.class, String.class });
				//obj = ctor.newInstance(5, "hello");
			
			//method invoke
			Method met = cl.getDeclaredMethod("foo", new Class[] { int.class } );
				//met --> SootMethod --> getParameters -> string, int
				
				
			Object ret = met.invoke(obj, new Object[] { 99 } );
				//InvokeExpression -> of type reflection -> getArguments -> 
			
			
			
			
			System.out.println(ret);
			
				//fetch all methods of class
				Method [] mets = cl.getDeclaredMethods();
				for(Method m : mets)
					System.out.println("\t" + m);
				
			
			//field get and set
			Field fi = cl.getDeclaredField("f");
			//setting to true suppresses checks for java language's access control
			fi.setAccessible(true);
			fi.set(obj, 199);
			
			Object f = fi.get(obj);
			System.out.println(f);
			
		} catch (Exception ex) {
			System.out.println(ex);
		 }
	
	}
}

class A {
	static { System.out.println("hello from A's clinit"); }
	
	private int f;
	
	public A () { System.out.println("hello from A's default constructor"); }
	
	public A (int x) { System.out.println("hello from A's other constructor"); }
	
	public A (int x, String y) { System.out.println("hello from A's other-other constructor"); }
	
	public String foo(int x) {
		System.out.println("hello from foo, you sent " + x);
		return "you sent " + x;
	}
	
	public int bar (String x) { return 0; }
}
