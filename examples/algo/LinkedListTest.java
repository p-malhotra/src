package examples.algo;

public class LinkedListTest {
	// MERGE RECURSIVE
	public static Node mergeRecursive(Node l1, Node l2)
	{
		if(l1 == null && l2 == null)
			return null;
		else if(l1 == null)
			return l2;
		else if(l2 == null)
			return l1;
		
		if(l1.data < l2.data)
		{
			l1.next = mergeRecursive(l1.next, l2);
			return l1;
		}
		else
		{
			l2.next = mergeRecursive(l1, l2.next);
		}
			return l2;
		
	}
	
	//MERGE
	public static Node merge(Node l1, Node l2)
	{
		if (l1 == null && l2 == null)
			return null;
		else if (l1 == null)
			return l2;
		else if (l2 == null)
			return l1;
		
		Node newHead = null;
		
		if(l1.data < l2.data)
		{
			newHead = l1;
			l1 = l1.next;
		}
		else
		{
			newHead = l2;
			l2 = l2.next;
		}
		Node current = newHead;
		System.out.println("Here @@@@");
		
		while( (l1 != null) || (l2 != null))
		{
			
			if(l1 == null) {
		         current.next = l2;
		         break;
		      }
			else if (l2 == null) {
		         current.next = l1;
		         break;
		      }			
			else if(l1.data < l2.data)
			{
				current.next = l1;
				l1 = l1.next;

				
			}
			else
			{
				current.next = l2;
				l2 = l2.next;
				
			}	
			current = current.next;

		}
		
		current.next = null;
		System.out.println("***"+ newHead.data);
		return newHead;
	}
	
	// REVERSE
	public static Node reverseList(Node l1)
	{
		if(l1 == null || l1.next == null)
			return l1;
		Node prev = l1;
		Node next= l1.next;
		l1.next= null;
		while(prev != null && next != null)
		{
			Node temp = next.next;
			next.next = prev;
			prev = next;
			next = temp;
		}
		return prev;
	}
	
	// CIRCULAR
	public boolean isCircular(Node head)
	{
		if(head == null || head.next == null)
			return false;
		if(head.next.next == null)
		{
			if(head.next == head)
				return true;
		}
		Node slow = head.next;
		Node fast = head.next.next;
		while(fast != null && fast.next.next != null)
		{
			fast = fast.next.next;
			slow = slow.next;
			if(slow ==fast)
				return true;
		}
		return false;
	}
	public static Node reverse(Node l1)
	{
		 System.out.println("HERE 111*****");

		if(l1 == null || l1.next == null)
			return l1;
		 System.out.println("HERE 222*****");

		Node current = l1;
		Node second = l1.next;
		Node third = second.next ;
		l1.next = null;
		second.next=l1;
		 System.out.println("HERE 3333*****");

		if(third == null)
		{
			return second;
		}
		
		Node previous = second;
		 current = third;
		 
		 System.out.println("HERE *****");
		 while(current != null)
		 {
			 System.out.println("curr  "+current.data);
			 Node tmp = current.next;
			 current.next = previous;
			 previous = current;
			 current = tmp;
		 }
		 l1 = previous;
		 return l1;
				 
		


	}
	/// find loop
	public static boolean compareLinkedList(Node l1, Node l2)
	{
		if(l1 == null && l2 == null)
			return true;
		else if(l1 == null || l2== null)
			return false;
		else if(l1.data != l2.data)
			return false;
		return compareLinkedList(l1.next, l2.next);
		
	}
	
	static Node   MergeLists(Node node1,Node node2)
	{
	   if(node1 == null)
	      return node2;
	   else if(node2 == null)
	      return node1;

	   Node head;
	   if(node1.data < node2.data)
	   {
	      head = node1;
	      node1 = node1.next;
	   }
	   else
	   {
	      head = node2;
	      node2 = node2.next;
	   }

	   Node current = head;
	   while((node1 != null) ||( node2 != null))
	   {
	      if(node1 == null) {
	         current.next = node2;
	         return head;
	      }
	      else if (node2 == null) {
	         current.next = node1;
	         return head;
	      }

	      if(node1.data < node2.data)
	      {
	          current.next = node1;
	          current = current.next;

	          node1 = node1.next;
	      }
	      else
	      {
	          current.next = node2;
	          current = current.next;

	          node2 = node2.next;
	      }
	   }
	   current.next = null ;// needed to complete the tail of the merged list
	   return head;
	}

	public static void main(String[] args) {
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
		
		LinkedListImpl impOrder1 = new LinkedListImpl();
		impOrder1.insertOrder(43);
		impOrder1.insertOrder(35);
		impOrder1.insertOrder(36);
		impOrder1.insertOrder(23);
		impOrder1.insertOrder(73);
		//impOrder.insert(23);
		//impOrder.insert(23);
		//impOrder.insert(23);
		System.out.println("************");
		System.out.println(impOrder1.toString());
		
		LinkedListImpl impComp = new LinkedListImpl();
		impComp.insertOrder(43);
		impComp.insertOrder(35);
		impComp.insertOrder(36);
		impComp.insertOrder(23);
		impComp.insertOrder(73);
		//impOrder.insert(23);
		//impOrder.insert(23);
		//impOrder.insert(23);
		System.out.println("************ Will reverse too");
		System.out.println(impComp.toString());
		System.out.println("************");
		System.out.println("Compare c1 and c2" +LinkedListTest.compareLinkedList(impOrder.head, impOrder1.head));
		System.out.println("Compare c2 and c3" +LinkedListTest.compareLinkedList(impComp.head, impOrder1.head));
		System.out.println("************");

		Node n = LinkedListTest.mergeRecursive(impOrder.head, impOrder1.head);
		
		while(n.next != null)
		{
			System.out.print(" :"+n.data);
			n =n.next;
		}
		System.out.println("*************** Merge");
		 Node h =LinkedListTest.reverseList(impComp.head);
		 while(h != null)
			{
				System.out.print(" :"+h.data);
				h =h.next;
			}
		 
		 Node h1 =LinkedListTest.reverse(impOrder.head);
		 while(h1 != null)
			{
				System.out.print(" ****"+h1.data);
				h1 =h1.next;
			}
			System.out.println("*************** Mergeagain");

			LinkedListImpl merge1 = new LinkedListImpl();
			System.out.println("*************** Mergeagain");

			merge1.insertOrder(3);
			merge1.insertOrder(9);
			LinkedListImpl merge2 = new LinkedListImpl();
merge2.insertOrder(11);
merge2.insertOrder(5);
		Node n1 = LinkedListTest.merge(merge1.head, merge2.head);
		
		while(n1.next != null)
		{
			System.out.print(" :"+n1.data);
			n1 =n1.next;
		}
	}

}
