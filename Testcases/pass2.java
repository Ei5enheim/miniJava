class A
{
	A a;
	static int i;
	public static void main (String[] args)
	{
		A a = new A();
		a.a = null;
		if (a.a == null)
			System.out.println(2);

	}
}
