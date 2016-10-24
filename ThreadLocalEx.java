import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalEx {

	 // Atomic integer containing the next thread ID to be assigned
	private static final AtomicInteger nextId = new AtomicInteger(0);

    // Thread local variable containing each thread's ID
    private static final ThreadLocal<Integer> threadId =
        new ThreadLocal<Integer>() {
            @Override protected Integer initialValue() {
                return nextId.getAndIncrement();
        }
    };

    // Returns the current thread's unique ID, assigning it if necessary
    public static int get() {
        return threadId.get();
    }

    /////////////////////
    ThreadLocal<SimpleDateFormat> formatter = new ThreadLocal<SimpleDateFormat>(){
		 @Override
	        protected SimpleDateFormat initialValue()
	        {
	            return new SimpleDateFormat("yyyyMMdd HHmm");
	        }
	    };
	    
	    public String formatIt(Date date)
	    {
	        return formatter.get().format(date);
	    }
	public static void main(String[] args) {
		
		
		
		
	}
	
}
