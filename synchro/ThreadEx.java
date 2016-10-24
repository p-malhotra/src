package synchro;

public class ThreadEx {
	public static void main(String[] args) {
		Counter c = new Counter();
		Thread t1= new ThreadCounter(c, "T1");
		Thread t2= new ThreadCounter(c, "T2");
		
		t1.start();
		t2.start();
	}
	
}
	class ThreadCounter extends Thread{
		
		protected Counter c;
		String name;
		public ThreadCounter(Counter c, String name) {
			// TODO Auto-generated constructor stub
			this.c =c;
			this.name = name;
		}
		
		public void run()
		{
			for (int i = 0; i < 5; i++) {
				System.out.println("Thread "+ name+ " : "+i);

				c.addSynchronous(i);
			}
		}
	}


