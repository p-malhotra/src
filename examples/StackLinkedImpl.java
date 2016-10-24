package examples;

public class StackLinkedImpl <T>{
	private StackNode<T> top;
	
	
	public void push(T d)
	{
		StackNode<T> newNode =new StackNode<T>(d);
		if(top==null)
			top = newNode;
		else
		{
			newNode.next =top;
			top =newNode;
		}
			
	}
	
	public T pop()
	{
		if(top == null)
		{
			System.out.println("Error no data");
		}
		else
		{
			T popNode = top.getData();
			top =top.next;
			return popNode;
		}
		return null;
	}
	
	public T peek()
	{
		if(top == null)
			return null;
		else return top.data;
	}
	
	public boolean isEmpty()
	{
		if(top == null)
			return true;
		else
			return false;
	}
	
private static class StackNode<T>{
		
		private T  data;
		private StackNode<T> next;
		public StackNode(T d) {
			this.data = d;
			// TODO Auto-generated constructor stub
		}
		
		public T getData()
		{
			return data;
		}
	}

}
