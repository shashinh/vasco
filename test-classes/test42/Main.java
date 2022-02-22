import java.lang.reflect.*;

class Main {
	public static void main (String [] args) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	
		Class a = Class.forName("A");
		Class[] params = null;
		Constructor cons = a.getConstructor(params);
		
		Base base = (Base) cons.newInstance();
		
		base.run();
	
		
	}
	
}


class X {
	void run (String str) { }
}

class Base {
	void run () throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException  {}
}

class A extends Base { 

	public A () {}

	@Override
	void run () throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException  {
		Class b = Class.forName("BB");
		Class[] params = null;
		Constructor cons = b.getConstructor(params);
		
		Base base = (Base) cons.newInstance();
		
		base.run();
	
	}
}

class BB extends Base {

	public BB ()  {}
	
	@Override
	void run () {
		System.out.println("hello from bar");
	}
}

