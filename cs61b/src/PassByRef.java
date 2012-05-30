public class PassByRef {
	static String s = "";
	public static void main(String args[]) {
		s= "hi";
		System.out.println("before change - "+s);
		changeit(s);
		System.out.println("after change - "+s);
	}
	private static void changeit(String s) {
		System.out.println("before changing - "+s);
		s="changed";
		System.out.println("after changing - "+s);
	}
	static {
		System.out.println("inside static - "+s);
	}
	
}
