class Main {
	public static void main(String [] args) throws ClassNotFoundException {
	
		Class x = Class.forName("A");
		System.out.println(x.getPackageName());
	}
}

class A {}
