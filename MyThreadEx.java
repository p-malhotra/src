import java.util.Date;

public class MyThreadEx {
	public static boolean isOdd(int x) {

		return x%2 == 1;
	}
	public static void main(String[] args) {
		
		
		System.out.println("Main thread name :" +Thread.currentThread().getName());

		int i= -3;
		System.out.println("bj"+(-5 %2 ==1));
		//String d = (String)(new Date());
		Thread t1 = new MyThreadExtends("extends");
		
		Thread t2 = new Thread(new MyThreadImplements("Implements"),"t2");
		
		Thread t3 = new Thread (new Runnable(){
			public void run(){
				for (int i = 0; i < 7; i++) {
					System.out.println("Hidden : "+ i);
					
				}
			}
		});
		t1.start();
		t2.start();
		t3.start();
		System.out.println("Main thread name :" +Thread.currentThread().getName());
	}

}
