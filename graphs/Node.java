package graphs;

public class Node {
	int data;
	Node left;
	Node right;
	Node parent;
	int size = 0;

	Node(int d) {
		this.data = d;
		size = 1;

	}
	// To insert data, it calls set left or set right child, depending on data.
	public void insertInOrder(int d) {
		if (d <= data) {
			if (left == null) {
				setLeftChild(new Node(d));
			} else {
				insertInOrder(d);
			}

		} else {
			if (right == null) {
				setRightChild(new Node(d));
			} else {
				insertInOrder(d);
			}
		}
	}

	private void setLeftChild(Node left) {
		this.left = left;
		if (left != null) {
			left.parent = this;
		}
	}

	private void setRightChild(Node right) {
		this.right = right;
		if (right != null) {
			right.parent = this;
		}
	}

	
	public boolean isBST() {
		if (left != null) {
			if (data < left.data  || !left.isBST())
				return false;
		}
		if(right != null)
		{
			if(data > right.data || !right.isBST())
				return false;
		}
		return true;
	}

}
