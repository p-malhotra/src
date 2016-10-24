package examples;

public class StackMin {
	
	private StackMinNode top;

	public void push(int d)
	{
		StackMinNode minNode = new StackMinNode(d, getMin(d));
		if(top == null)
			top = minNode;
		else
		{
			minNode.next =top;
			top = minNode;
		}
	}
	
	public int pop()
	{
		if(top == null)
			return -1;
		else
		{ 
			int popped = top.data;
			top=top.next;
			return popped;
		}
	}
	private int getMin(int d){
		if(top ==null)
			return d;
		else{
			return Math.min(d, top.data);
		}
	}
	
	public void display()
	{
		
		StackMinNode current =top;
		while(current!= null)
		{
			System.out.print(" " +current.data+"min: "+current.min);
			current = current.next;
		}
	}
	private class StackMinNode{
		
		int data;
		int min;
		StackMinNode next;
		
		public StackMinNode(int d, int min) {
			this.data =d;
			this.min =min;
			// TODO Auto-generated constructor stub
		}
		
	}
	
	public static void main(String[] args) {
		StackMin stackMin = new StackMin();
		stackMin.push(10);
		stackMin.push(3);
		stackMin.push(5);
		stackMin.display();
		System.out.println("**************");
		stackMin.push(2);
		stackMin.display();
		System.out.println("**************");

		stackMin.pop();
		stackMin.display();
		
		
		
		
	}
}
