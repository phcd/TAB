import java.util.Arrays;

public class ArrayListImp {
	private int size = 0;
	private static final int DEFAULT_SIZE = 10;
	private Object arrayElements[];
	
	public ArrayListImp() {
		arrayElements = new Object[DEFAULT_SIZE];
	}

	private void resizeArray() {
		int newSize = arrayElements.length * 2;
		arrayElements = Arrays.copyOf(arrayElements, newSize);
	}

	public Object get(int i) {
		if (i>= arrayElements.length) {
			throw new IndexOutOfBoundsException("Index: " + i + ", Size " + i );
		}
		return (Object) arrayElements[i];
	}
	
	public void add(Object obj) {
		if (size == arrayElements.length) {
			resizeArray();
		}
		arrayElements[size++] = obj;
	}
	
	public Object delete(int i) {
		if (i>= arrayElements.length) {
			throw new IndexOutOfBoundsException("Index: " + i + ", Size " + i );
		}
		Object oldValue = arrayElements[i];
		  int numMoved = size - i - 1;
		  if (numMoved > 0)
		  System.arraycopy(arrayElements, i+1, arrayElements, i,numMoved);
		  arrayElements[--size] = null; 
		  return oldValue;
	}
}