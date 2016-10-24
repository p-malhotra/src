package examples;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
/**
 * Create cache objt with time & data
 * Hashmap will have  id as key and cacheobjt as data.
 * Incleanup method cacheimpl object if time to live is
 * Take a hashmap
 * @author poojamalhotra
 *
 * @param <K>
 * @param <T>
 */
public class CacheImpl<K, T> {

	private HashMap<K, T> cacheMap;
	private long timeToLive;

	private class CacheObjt {
		private long lastAccessed = System.currentTimeMillis();
		private String data;

		public CacheObjt(String val) {
			this.data = val;
		}
	}
	public CacheImpl(long timeToLive, long timeInterval, int max) {
		this.timeToLive =timeToLive*2000;
		cacheMap = new HashMap<K,T>(max);
		  if (timeToLive > 0 && timeInterval > 0) {
	            
	            Thread t = new Thread(new Runnable() {
	                public void run() {
	                    while (true) {
	                        try {
	                            Thread.sleep(timeInterval * 1000);
	                        } catch (InterruptedException ex) {
	                        }
	                        
	                    }
	                }
	            });
	            
	            t.setDaemon(true);
	            t.start();
	        }	}

	// PUT method

	public void put(K key, T val) {
		synchronized (cacheMap) {
			cacheMap.put(key, val);
		}
	}

	// GET method
	public T get(K key)
	{
		if(!cacheMap.containsKey(key))
			return null;
		synchronized (cacheMap) {
			CacheObjt objt = (CacheImpl<K, T>.CacheObjt) cacheMap.get(key);
			objt.lastAccessed =System.currentTimeMillis();
			return (T)objt.data;
			
		}
	}
	
	// REMOVE  method
	public void remove(K key)
	{
		synchronized (cacheMap) {
			cacheMap.remove(key);
		}
	}
	// CLEANUP method
    public void cleanup() {
        
        long now = System.currentTimeMillis();
        ArrayList<String> deleteKey = null;
        
        synchronized (cacheMap) {
            Iterator<?> itr = cacheMap.entrySet().iterator();
            
            deleteKey = new ArrayList<String>((cacheMap.size() / 2) + 1);
            CacheObjt c = null;
            
            while (itr.hasNext()) {
                String key = (String) itr.next();
                c = (CacheObjt) ((Entry<?, ?>) itr).getValue();
                if (c != null && (now > (timeToLive + c.lastAccessed))) {
                    deleteKey.add(key);
                }
            }
        }
        
        for (String key : deleteKey) {
            synchronized (cacheMap) {
                cacheMap.remove(key);
            }
            
            Thread.yield();
        }
    }
}
