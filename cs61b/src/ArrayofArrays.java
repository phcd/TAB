public class ArrayofArrays {

	public static void main(String args[]) {
		fillArrayValues(4, 4);
	}

	private static void fillArrayValues(int i, int j) {
		int[][] array = new int[i][j];
		int var = 1;
		for (int k = 0; k < array.length; k++) {
			for (int k2 = 0; k2 < array.length; k2++) {

				array[k][k2] = var;
				System.out.println(array[k][k2]);
				var++;
			}
		}
		printInSpiral(array, 4);
	}

	public static void printInSpiral(int[][] numbers, int size) {
		for (int i = size - 1, j = 0; i >= 0; i--, j++) {
			for (int k = j; k < i; k++)
				System.out.print(numbers[j][k] + " ");
			for (int k = j; k < i; k++)
				System.out.print(numbers[k][i] + " ");
			for (int k = i; k > j; k--)
				System.out.print(numbers[i][k] + " ");
			for (int k = i; k > j; k--)
				System.out.print(numbers[k][j] + " ");
		}
	}
}
