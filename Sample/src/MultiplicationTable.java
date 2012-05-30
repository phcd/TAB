import java.util.ArrayList;

class MultiplicationTable {
	public static void main(String args[]) {
		int[][] mul_table_array = new int[10][10];
		int i, j;
ArrayList a = new ArrayList();
		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				mul_table_array[i][j] = (i + 1) * (j + 1);
			}
		}

		for (i = 0; i < 10; i++) {
			for (j = 0; j < 10; j++) {
				System.out.println(mul_table_array[i][j]);
			}
		}
	}

}