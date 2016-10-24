package synchro;

public class Counter {
	int counterS = 0;
	int counter = 0;

	public synchronized void addSynchronous(int val) {
		counterS += val;
		System.out.println("CounterS "+ counterS);
	}

	public void add(int val) {
		counter += val;
		System.out.println("CounterS "+ counter);

	}

}
