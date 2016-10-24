package examples;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;

public class SingletonEx implements Serializable{
	
	public static SingletonEx INSTANCE = null;
	
	private SingletonEx()
	{
		
	}
	
	public static synchronized SingletonEx getInstance()
	{
		if(INSTANCE == null)
		{
			INSTANCE = new SingletonEx();
		}
		
		return INSTANCE;
	}
	
	
	private void readObject(ObjectInputStream ois) throws ClassNotFoundException, IOException
	{
		ois.defaultReadObject();
		synchronized(SingletonEx.class){
			if(INSTANCE == null)
				INSTANCE = this;
		}
		
	}
	
	private Object readResolve() {
		assert(INSTANCE != null);
		return INSTANCE;
	}
	
	public static void main(String[] args) throws Throwable {
        assert(getInstance() == getInstance());

            java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(baos);
            oos.writeObject(getInstance());
            oos.close();

            java.io.InputStream is = new java.io.ByteArrayInputStream(baos.toByteArray());
            ObjectInputStream ois = new ObjectInputStream(is);
            SingletonEx s = (SingletonEx)ois.readObject();
            assert(s == getInstance());
    }

}
