package examples;

public class SortStack {
	
	public static void sort(StackImpl stack)
	{
		StackImpl impl = new StackImpl(stack.size);
		System.out.println("***************");

		
		while(!stack.isEmpty())
		{
			int tmp =stack.pop();
			while(!impl.isEmpty() && impl.peek() > tmp)
			{
				stack.push(impl.pop());
				
			}
					
			impl.push(tmp);
			
			
		}
		impl.display();
		System.out.println("***************");

		while(!impl.isEmpty())
		{
			stack.push(impl.pop());
		}
		stack.display();
		
	}
	
	public static void main(String[] args) {
		StackImpl impl = new StackImpl(7);
		impl.push(2);
		impl.push(1);
		impl.push(5);
		impl.push(7);
		impl.push(8);
		impl.display();
		SortStack.sort(impl);
	}
}
