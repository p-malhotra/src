
public class HashMapImpl<K,V> {
	 private Entry<K,V>[] table;
	int capacity =10;
	
	public HashMapImpl(int c)
	{
		this.capacity =c;
		 table = new Entry[capacity];
	}
	
	// hash the key
	private int hash(K key)
	{
		return Math.abs(key.hashCode() %capacity);
	}
	
	// first  check table for has, if it is not there , add entry object at the hash.
	// else, find next null element like Linkedlist and add it there.
	public void put(K k, V v)
	{
		if(k == null)
			return ;
		Entry entry = new Entry<K, V>(k, v, null);
		int hash = hash(k);
		if(table[hash] == null)
		{
			table[hash]= entry;
		}
		else{
			Entry<K,V> prev = null;
			Entry <K,V> current = table[hash];
			while(current.next != null)
			{
				if(current.key == k)
				{
					if(prev== null)
					{
						entry.next = current.next;
						table[hash] = entry;
						return;
					}
					else
					{
						entry.next= current.next;
						prev.next = entry;
						return;
					}
					
					
				}
				prev = current;
				current = current.next;
				
			}
			prev.next = entry;

		}
	}
	
	public V get(K key)
	{
		int hash = hash(key);
		if(table[hash] == null)
			return null;
			
		else{
			Entry<K,V> tmp = table[hash];
			while(tmp != null)
			{
				if(tmp.key.equals(key))
					return tmp.value;
			}
			tmp = tmp.next;
		}
		return null;
	}
	
	// to Remove iterate through has linkedlist
	// if it is first elem, make second has first elem
	// else prev.next = current.next
	public boolean remove(K key)
	{
		int hash = hash(key);
		if(table[hash] == null)
			return false;
		else
		{
			Entry<K,V> prev= null;
			Entry<K,V> current = table[hash];
			while(current != null)
			{
				if(current.key.equals(key))
				{
					if(prev == null)
					{
						table[hash] = current.next;
						return true;
					}
					else{
						prev.next = current.next;
						return true;
						
					}
				}
				prev= current;
				current = current.next;
			}
			return false;
		}
	}
	static class Entry<K,V>{
		K key;
		V value;
		Entry<K,V> next;
		
		public Entry(K k, V v, Entry<K,V> next){
			this.key =k;
			this.value =v;
			this.next = next;
			
		}
	}
	
	

}
