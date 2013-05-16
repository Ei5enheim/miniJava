class A
{
	static A a;
	A b;
	int i;

	public static void main (String[] args)
	{
		a = new A();
		a.b = new A();
		a.b.i = 7;
		a.i = 77;
		print(a.b);
		print(a);
		
	}
	
	public static void print(A a)
	{
		System.out.println(a.i);
	}

}
