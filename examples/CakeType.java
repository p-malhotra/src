package examples;

public class CakeType {

	int weight;
    int value;

    public CakeType(int weight, int value) {
        this.weight = weight;
        this.value  = value;
    }
    
    int capacity = 20;
    CakeType[] cakeTypes = new CakeType[]{
    	    new CakeType(7, 160),
    	    new CakeType(3, 90),
    	    new CakeType(2, 15),
    	};

    public long maxDuffelBagValue(CakeType[] cakeTypes, int capacity){

    long maxValC =0L;
    long[] maxValuesAtCapacities = new long[capacity + 1];
    for (int currentCapacity = 0; currentCapacity <= capacity; currentCapacity++) {
        long currentMaxValue = 0;

    for(CakeType ct:cakeTypes)
    {
    	if (ct.weight <= capacity) {
            long maxValueUsingCake = ct.value + maxValuesAtCapacities[currentCapacity - ct.weight];
            currentMaxValue = Math.max(maxValueUsingCake, currentMaxValue);

    	}
    }
    maxValuesAtCapacities[currentCapacity] = currentMaxValue;

    }
    return maxValC;
    
    }
}
