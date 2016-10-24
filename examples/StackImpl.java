package examples;

public class StackImpl {
	
	int pointer ;
	int[] arr;
	int size;
	public StackImpl(int length) {
		pointer =-1;
		this.size = length;
		arr = new int[size];
		
		// TODO Auto-generated constructor stub
	}
	
	public boolean isEmpty()
	{
		if(pointer <0)
			return true;
		return false;
	}
	
	public int peek()
	{
		return arr[pointer];
	}
	
	public boolean isFull()
	{
		if(pointer <size-1)
			return false;
		return true;
	}
	
	public void push(int d)
	{
		if(!isFull())
			arr[++pointer] = d;
	}
	
	public int pop()
	{
		if(!isEmpty())
			return arr[pointer--];
		else
			return -1;
	}
	
	
	public void display()
	{
		for (int i = 0; i <= pointer; i++) {
			System.out.println("Values "+ arr[i]);
			
		}
	}
	
	public static void main(String[] args) {
		StackImpl impl = new StackImpl(3);
		impl.push(2);
		impl.push(1);
		impl.push(5);
		impl.display();
		impl.push(7);
		impl.display();
		impl.pop();
		impl.pop();
		impl.display();
	}

}
