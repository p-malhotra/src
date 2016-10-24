
public class MyThreadImplements implements Runnable{

	String name ;
	public MyThreadImplements(String n) {
		this.name = n;
		// TODO Auto-generated constructor stub
	}
	@Override
	public void run() {
		for (int i = 0; i < 7; i++) {
			System.out.println("Thread: "+this.name+" : "+i);
			
		}
		// TODO Auto-generated method stub
		
	}
	

}
