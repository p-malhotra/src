package examples.algo;

/**
 * method
 * insertFirst
 * insert
 * insertLast
 * 
 * @author poojamalhotra
 *
 */
public class LinkedListImpl {
	
	
	Node head;
	
	public LinkedListImpl() {
		// TODO Auto-generated constructor stub
		head = null;
	}
	/**
	 * if data is not null
	 * if data is null
	 */
	public Node insertFirst(int d)
	{
		Node n = new Node(d);
		if(head == null)
			head = n;
		else{
			n.next = head;
			head = n;
		}
		
		return head;
	}

	public Node insert(int d)
	{
		Node n = new Node(d);
		
		if(head == null)
			head = n;
		else
		{
			Node current = head;
			while(current.next != null)
			{
				current = current.next;
			}
			current.next = n;
		}
		return head;
	}
	
	public String toString(){
		Node current = head;
		StringBuilder sb = new StringBuilder();
		while(current != null)
		{
			sb.append(current.data+ "  ");
			current = current.next;
			
		}
		return sb.toString();
	}
	public Node KthFromEnd(Node n, int k)
	{
		Node  ele = KthFromEnd(n, 3, 0);
		System.out.println(ele.data);
		return ele;

	}
	public Node KthFromEnd(Node n, int k, int indx)
	{
		if(n ==null)
			return null;
		
		Node nth =KthFromEnd(n.next, k, indx);
		indx =indx+1;
		
		if(k == indx)
			return n;
		
		return head;
	}
	
	public static class Index {
		public int value = 0;
	}	
	
	public  Node kthToLast(Node head, int k) {
		Index idx = new Index();
		return kthToLast(head, k, idx);
	}
	
	public  Node kthToLast(Node head, int k, Index idx) {
		if (head == null) {
			return null;
		}
		Node node = kthToLast(head.next, k, idx);
		idx.value = idx.value + 1;
		if (idx.value == k) {
			return head;
		} 
		return node;
	}
	public Node nFromLast(Node head, int n){
		Node fast =head;
		Node slow = head;
		
		for(int i=0;i<n-1;i ++)
			fast =fast.next;
		
		while(fast.next != null)
		{
			fast = fast.next;
			slow = slow.next;
		}
		return slow;
	}
	
	public Node removeDups(Node head){
		Node current = head;
		while(current != null)
		{
			Node runner = current;

			while(runner.next != null){
				if(runner.next.data == current.data){
					runner.next = runner.next.next;
				}
				else
					runner = runner.next;
			}
			current = current.next;
			
		}
		return head;
	}
	
	public Node insertOrder (int d){
		
		Node n = new Node(d);
		if(head == null)
		{
			head = n;

			return head;
		}
		Node current = head;
		Node previous = null;
		while(current != null && current.data < d){
			previous = current;
			current = current.next;
			
		}
		if(previous != null)
		{
			previous.next= n;
			n.next = current;
			
		}
		else{
			head =n;
			n.next =current;
			
		}
			return head ;
	}
	public static void main(String[] args) {
		LinkedListImpl impl = new LinkedListImpl();
		impl.insertFirst(45);
		impl.insert(3);
		impl.insert(31);
		impl.insert(33);
		impl.insert(13);
		impl.insert(23);
		impl.insert(23);
		impl.insert(23);

		impl.insert(31);
		impl.insert(23);


		impl.insert(2);

		System.out.println(impl.toString());
		//impl.KthFromEnd(impl.head, 4);
		System.out.println(impl.nFromLast(impl.head,5).data	);
		//System.out.println(" "+ node.data);
		impl.removeDups(impl.head);
		System.out.println(impl.toString());
		
		LinkedListImpl impOrder = new LinkedListImpl();
		impOrder.insertOrder(45);
		impOrder.insertOrder(3);
		impOrder.insertOrder(31);
		impOrder.insertOrder(33);
		impOrder.insertOrder(13);
		//impOrder.insert(23);
		//impOrder.insert(23);
		//impOrder.insert(23);
		System.out.println("************");
		System.out.println(impOrder.toString());

		
		

		
		
	}
}
