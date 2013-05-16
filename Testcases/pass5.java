class A
{
	static A a;
	A b;
	int i;

	public static void main (String[] args)
	{
		
		a = new A();
		a.b = new A();
		a.b.i = 55;
		a.i = 5;
		a.print();
		a.b.print();
		
		
	}

	void print()
	{
		System.out.println(this.i);
	}
}
