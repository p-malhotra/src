import java.util.Iterator;

public class MyThreadExtends extends Thread{

	String name ;
	
	public MyThreadExtends(String n) {
		// TODO Auto-generated constructor stub
		this.name = n;
	}
	@Override
	public void run(){
		for (int i = 0; i < 7; i++) {
			System.out.println("Name "+ this.name + " : "+ i );

		}
	}
}
