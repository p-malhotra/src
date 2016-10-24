package examples;

import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class BlockingQueueImpl<T> {
	Condition isFull;
	Condition isEmpty;
	Lock lock;
	int limit;
    private Queue<T> q = new LinkedList<T>();

	
	public BlockingQueueImpl() {
	    this(Integer.MAX_VALUE);
	}

	public BlockingQueueImpl(int limit) {
	    this.limit = limit;
	    lock = new ReentrantLock();
	    isFull = lock.newCondition();
	    isEmpty = lock.newCondition();
	}

	public void put (T t) {
	    lock.lock();
	    try {
	       while (isFull()) {
	            try {
	                isFull.await();
	            } catch (InterruptedException ex) {}
	        }
	        q.add(t);
	        isEmpty.signalAll();
	    } finally {
	        lock.unlock();
	    }
	 }

	public T get() {
	    T t = null;
	    lock.lock();
	    try {
	        while (isEmpty()) {
	            try {
	                isEmpty.await();
	            } catch (InterruptedException ex) {}
	        }
	        t = q.poll();
	        isFull.signalAll();
	    } finally { 
	        lock.unlock();
	    }
	    return t;
	}

	private boolean isEmpty() {
        return q.size() == 0;
    }
    private boolean isFull() {
        return q.size() == limit;
    }
}
