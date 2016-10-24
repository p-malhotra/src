package examples;

import java.util.HashMap;

public class LRUImpl {
	
	int capacity;
	HashMap<Integer, Node> map = new HashMap<>();
	Node head = null;
	Node end = head;
	
	public LRUImpl(int c) {
		this.capacity =c;
		// TODO Auto-generated constructor stub
	}
	
	public int get(int k)
	{
		if(map.containsKey(k))
		{
			Node n = map.get(k);
			remove(n);
			setHead(n);
			return n.val;
			}
		return -1;
		}
	
	public void remove(Node n){
		if(n.prev != null)
			n.prev.next = n.next;
		else{
			head = n.next;
		}
		if(n.next != null)
			n.next.prev =n.prev;
		else
			end =n.prev;
	}
	
	public void setHead(Node n)
	{
		n.next = head;
		n.prev = null;
		
		if(head != null)
			head.prev = n;
		head = n;
		
		if(end == null)
		end = head;
		}
	public void set(int k, int v)
	{
		if(map.containsKey(k))
		{
			Node node = map.get(k);
			node.val = v;
			remove(node);
			setHead(node);
			
		}
		else{
			Node newNode = new Node(k, v);
			if(map.size()>= capacity)
			{
				remove(end);
				map.remove(end.key);
				setHead(newNode);
				
			}
			else
				setHead(newNode);
			map.put(k, newNode);

		}
		
	}
	static class Node{
		
		int key;
		int val;
		Node next;
		Node prev;
		
		public Node(int key, int v) {
			this.key =key;
			this.val = v;
			// TODO Auto-generated constructor stub
		}
		
	}

}
